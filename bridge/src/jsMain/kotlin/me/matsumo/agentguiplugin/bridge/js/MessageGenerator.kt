@file:OptIn(DelicateCoroutinesApi::class)

package me.matsumo.agentguiplugin.bridge.js

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.promise
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import me.matsumo.agentguiplugin.bridge.js.external.ReadlineInterface
import kotlin.coroutines.resume

private const val TYPE_PERMISSION_RESPONSE = "permission_response"
private const val TYPE_USER_MESSAGE = "user_message"
private const val TYPE_ABORT = "abort"

internal class StdinReader(
    private val readline: ReadlineInterface,
    private val scope: kotlinx.coroutines.CoroutineScope =
        kotlinx.coroutines.CoroutineScope(SupervisorJob() + kotlinx.coroutines.Dispatchers.Default),
) {
    private val mutex = Mutex()

    private val messageQueue = ArrayDeque<dynamic>()
    private val waiters = ArrayDeque<(dynamic) -> Unit>()

    // stdin の line を受け取る入口（順序保証のため 1本の dispatcher が消費する）
    private val inbox = Channel<dynamic>(capacity = Channel.UNLIMITED)

    /** stdin の購読開始 + dispatcher 起動 */
    fun setup() {
        readline.on("line") { line ->
            runCatching {
                val msg = js("JSON.parse(line)")
                inbox.trySend(msg)
            }
        }

        scope.launch {
            for (msg in inbox) {
                dispatchMessage(msg)
            }
        }
    }

    /** waiter がいればそれを優先、いなければキューへ */
    private suspend fun dispatchMessage(msg: dynamic) {
        mutex.withLock {
            val waiter = waiters.removeFirstOrNull()
            if (waiter != null) {
                waiter(msg)
            } else {
                messageQueue.addLast(msg)
            }
        }
    }

    suspend fun waitForMessage(vararg expectedTypes: String): dynamic = waitForMatchingMessage { msg ->
        val t = msg.type as? String
        t != null && t in expectedTypes
    }

    suspend fun waitForPermissionResponse(
        requestId: String,
        timeoutMs: Long = 60_000L,
    ): dynamic = withTimeoutOrNull(timeoutMs) {
        waitForMatchingMessage { msg ->
            val type = msg.type as? String
            val id = msg.requestId as? String
            type == TYPE_PERMISSION_RESPONSE && id == requestId
        }
    } ?: js("""({behavior: "deny", message: "Permission request timed out"})""")

    /** 条件に合うメッセージが来るまで待つ（合わないものはキューへ戻す） */
    private suspend fun waitForMatchingMessage(
        predicate: (dynamic) -> Boolean,
    ): dynamic = suspendCancellableCoroutine { cont ->
        scope.launch {
            val immediate: dynamic? = mutex.withLock {
                val index = messageQueue.indexOfFirst(predicate)
                if (index >= 0) messageQueue.removeAt(index) else null
            }

            if (immediate != null) {
                cont.resume(immediate)
                return@launch
            }

            // waiter を登録：一致すれば resume、違えばキューへ戻して waiter を再登録
            lateinit var waiter: (dynamic) -> Unit
            waiter = let@{ msg ->
                if (!cont.isActive) return@let

                val matched = predicate(msg)
                scope.launch {
                    if (matched) {
                        if (cont.isActive) cont.resume(msg)
                    } else {
                        mutex.withLock {
                            messageQueue.addLast(msg)
                            // まだ待っているなら自分を戻す（元コードと同じ挙動）
                            if (cont.isActive) waiters.addLast(waiter)
                        }
                    }
                }
            }

            mutex.withLock {
                if (cont.isActive) waiters.addLast(waiter)
            }

            // キャンセル時に waiter を掃除（仕様は変えないが、リーク防止）
            cont.invokeOnCancellation {
                scope.launch {
                    mutex.withLock {
                        waiters.removeAll { it === waiter }
                    }
                }
            }
        }
    }
}

/**
 * SDK の query() が要求する AsyncIterable<SDKUserMessage> を生成する。
 * ここが interop の要：SDK は `prompt` として AsyncIterable を期待する。
 */
internal fun createMessageAsyncIterable(
    firstPrompt: String,
    stdinReader: StdinReader,
): dynamic {
    val firstMessage = createUserMessage(firstPrompt)

    // Promise<IteratorResult> を返す provider
    val nextProvider: () -> dynamic = {
        GlobalScope.promise {
            val msg = stdinReader.waitForMessage(TYPE_USER_MESSAGE, TYPE_ABORT)
            when (msg.type as? String) {
                TYPE_ABORT -> createIteratorResult(done = true)
                else -> {
                    val content = buildUserContent(msg)
                    createIteratorResult(
                        value = createUserMessage(content),
                        done = false,
                    )
                }
            }
        }
    }

    return createAsyncIterableJs(firstMessage, nextProvider)
}

/**
 * user メッセージ形式の JS オブジェクトを作る。
 *
 * NOTE: apply のレシーバ内で `this.xxx` が赤線になる環境があるため、
 *       Kotlin の `this` を使わず、ローカル変数に入れてからプロパティ代入する。
 */
private fun createUserMessage(content: dynamic): dynamic {
    val msg = js("({})")
    msg.type = "user"

    val message = js("({})")
    message.role = "user"
    message.content = content

    msg.message = message
    msg.parent_tool_use_id = null
    return msg
}

/** IteratorResult 形式の JS オブジェクトを作る（done/value） */
private fun createIteratorResult(
    value: dynamic = undefined,
    done: Boolean,
): dynamic {
    val result = js("({})")
    result.value = value
    result.done = done
    return result
}

private fun buildUserContent(msg: dynamic): dynamic {
    val images = msg.images
    val documents = msg.documents

    val imageLen = lengthOrZero(images)
    val docLen = lengthOrZero(documents)

    val hasAttachments = imageLen > 0 || docLen > 0
    if (!hasAttachments) return msg.text

    val arr = js("[]")

    val textBlock = js("({})")
    textBlock.type = "text"
    textBlock.text = msg.text
    arr.push(textBlock)

    repeat(imageLen) { i -> arr.push(images[i]) }
    repeat(docLen) { i -> arr.push(documents[i]) }

    return arr
}

private fun lengthOrZero(value: dynamic): Int {
    if (value == null || value === undefined) return 0
    val len = value.length
    return (len as? Number)?.toInt() ?: 0
}

/**
 * Kotlin/JS から Symbol.asyncIterator を直接作れないため、最小限の JS で生成する。
 */
private fun createAsyncIterableJs(firstMessage: dynamic, nextProvider: () -> dynamic): dynamic = js(
    """
    (function(firstMsg, provider) {
        var first = true;
        var obj = {};
        obj[Symbol.asyncIterator] = function() {
            return {
                next: function() {
                    if (first) {
                        first = false;
                        return Promise.resolve({ value: firstMsg, done: false });
                    }
                    return provider();
                }
            };
        };
        return obj;
    })(firstMessage, nextProvider)
    """
)
