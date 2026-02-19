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
import me.matsumo.agentguiplugin.bridge.model.BridgeCommandTypes
import kotlin.coroutines.resume

/**
 * stdin からの JSONL メッセージを受信し、型ごとに振り分けるリーダー。
 * 順序保証のため内部で 1 本の Channel + Mutex で直列化している。
 *
 * @param onAbort abort コマンド受信時に呼ばれるコールバック（AbortController 用）
 */
internal class StdinReader(
    private val readline: ReadlineInterface,
    private val onAbort: (() -> Unit)? = null,
    private val scope: kotlinx.coroutines.CoroutineScope =
        kotlinx.coroutines.CoroutineScope(SupervisorJob() + kotlinx.coroutines.Dispatchers.Default),
) {
    private val mutex = Mutex()
    private val messageQueue = ArrayDeque<dynamic>()
    private val waiters = ArrayDeque<(dynamic) -> Unit>()
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

    /** waiter がいればそれを優先、いなければキューへ。abort ならコールバックも呼ぶ */
    private suspend fun dispatchMessage(msg: dynamic) {
        // abort 検知: SDK の AbortController に通知（二重購読を避けるためここで一元化）
        if (dynamicStringOrNull(msg.type) == BridgeCommandTypes.ABORT) {
            onAbort?.invoke()
        }

        mutex.withLock {
            val waiter = waiters.removeFirstOrNull()
            if (waiter != null) {
                waiter(msg)
            } else {
                messageQueue.addLast(msg)
            }
        }
    }

    /** 指定した type のメッセージが来るまで待つ */
    suspend fun waitForMessage(vararg expectedTypes: String): dynamic =
        waitForMatchingMessage { msg ->
            dynamicStringOrNull(msg.type) in expectedTypes
        }

    /** 指定 requestId のパーミッション応答を待つ（タイムアウト時は deny を返す） */
    suspend fun waitForPermissionResponse(
        requestId: String,
        timeoutMs: Long = 60_000L,
    ): dynamic = withTimeoutOrNull(timeoutMs) {
        waitForMatchingMessage { msg ->
            dynamicStringOrNull(msg.type) == BridgeCommandTypes.PERMISSION_RESPONSE
                && dynamicStringOrNull(msg.requestId) == requestId
        }
    } ?: js("""({behavior: "deny", message: "Permission request timed out"})""")

    /**
     * 条件に合うメッセージが来るまで待つ。
     * 条件に合わないメッセージはキューへ戻す。
     */
    private suspend fun waitForMatchingMessage(
        predicate: (dynamic) -> Boolean,
    ): dynamic = suspendCancellableCoroutine { cont ->
        scope.launch {
            // まずキューに条件に合うメッセージがないか確認
            val immediate: dynamic? = mutex.withLock {
                val index = messageQueue.indexOfFirst(predicate)
                if (index >= 0) messageQueue.removeAt(index) else null
            }

            if (immediate != null) {
                cont.resume(immediate)
                return@launch
            }

            // waiter を登録: 一致すれば resume、違えばキューへ戻して waiter を再登録
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
                            if (cont.isActive) waiters.addLast(waiter)
                        }
                    }
                }
            }

            mutex.withLock {
                if (cont.isActive) waiters.addLast(waiter)
            }

            // キャンセル時に waiter を掃除（リーク防止）
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
 * 最初のプロンプトを即座に返し、以降は stdin からのメッセージを待つ。
 */
internal fun createMessageAsyncIterable(
    firstPrompt: String,
    stdinReader: StdinReader,
): dynamic {
    val firstMessage = createUserMessage(firstPrompt)

    val nextProvider: () -> dynamic = {
        GlobalScope.promise {
            val msg = stdinReader.waitForMessage(BridgeCommandTypes.USER_MESSAGE, BridgeCommandTypes.ABORT)
            when (dynamicStringOrNull(msg.type)) {
                BridgeCommandTypes.ABORT -> createIteratorResult(done = true)
                else -> createIteratorResult(
                    value = createUserMessage(buildUserContent(msg)),
                    done = false,
                )
            }
        }
    }

    return createAsyncIterableJs(firstMessage, nextProvider)
}

/**
 * SDK が期待する user メッセージ形式の JS オブジェクトを作る。
 * apply のレシーバ this は Kotlin/JS で型解決エラーになるため、ローカル変数経由で代入する。
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

/** IteratorResult 形式の JS オブジェクト（done/value） */
private fun createIteratorResult(
    value: dynamic = undefined,
    done: Boolean,
): dynamic {
    val result = js("({})")
    result.value = value
    result.done = done
    return result
}

/**
 * ユーザーメッセージのコンテンツを組み立てる。
 * 画像やドキュメントがあれば配列形式、テキストのみなら文字列を返す。
 */
private fun buildUserContent(msg: dynamic): dynamic {
    val images = msg.images
    val documents = msg.documents
    val imageLen = dynamicLength(images)
    val docLen = dynamicLength(documents)

    // 添付なしならテキストだけ返す
    if (imageLen == 0 && docLen == 0) return msg.text

    val arr = js("[]")

    val textBlock = js("({})")
    textBlock.type = "text"
    textBlock.text = msg.text
    arr.push(textBlock)

    repeat(imageLen) { i -> arr.push(images[i]) }
    repeat(docLen) { i -> arr.push(documents[i]) }

    return arr
}

/**
 * Kotlin/JS から Symbol.asyncIterator を直接作れないため、最小限の JS で生成する。
 * 最初のメッセージは同期的に返し、以降は nextProvider の Promise を返す。
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
