@file:OptIn(DelicateCoroutinesApi::class)

package me.matsumo.agentguiplugin.bridge.js

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import me.matsumo.agentguiplugin.bridge.js.external.ReadlineInterface
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class StdinReader(private val rl: ReadlineInterface) {
    private val messageQueue = mutableListOf<dynamic>()
    private val waiters = mutableListOf<(dynamic) -> Unit>()

    fun setup() {
        rl.on("line") { line ->
            try {
                val msg = js("JSON.parse(line)")
                val waiterIndex = waiters.indexOfFirst { true }
                if (waiterIndex >= 0) {
                    val waiter = waiters.removeAt(waiterIndex)
                    waiter(msg)
                } else {
                    messageQueue.add(msg)
                }
            } catch (_: Throwable) {
                // ignore non-JSON lines
            }
        }
    }

    suspend fun waitForMessage(vararg expectedTypes: String): dynamic = suspendCoroutine { cont ->
        // Check queue first
        val queueIndex = messageQueue.indexOfFirst { msg ->
            val msgType = msg.type as? String
            msgType != null && msgType in expectedTypes
        }
        if (queueIndex >= 0) {
            val msg = messageQueue.removeAt(queueIndex)
            cont.resume(msg)
            return@suspendCoroutine
        }

        // Register a waiter that filters by type
        var resolved = false
        lateinit var waiterFn: (dynamic) -> Unit
        waiterFn = { msg: dynamic ->
            val msgType = msg.type as? String
            if (!resolved && msgType != null && msgType in expectedTypes) {
                resolved = true
                cont.resume(msg)
            } else {
                // Not the type we want, re-queue and re-register
                messageQueue.add(msg)
                waiters.add(waiterFn)
            }
        }
        waiters.add(waiterFn)
    }

    suspend fun waitForPermissionResponse(requestId: String, timeoutMs: Long = 60_000L): dynamic {
        return kotlinx.coroutines.withTimeoutOrNull(timeoutMs) {
            waitForMatchingMessage { msg ->
                val msgType = msg.type as? String
                val msgRequestId = msg.requestId as? String
                msgType == "permission_response" && msgRequestId == requestId
            }
        } ?: js("({behavior: 'deny', message: 'Permission request timed out'})")
    }

    private suspend fun waitForMatchingMessage(predicate: (dynamic) -> Boolean): dynamic = suspendCoroutine { cont ->
        // Check queue first
        val queueIndex = messageQueue.indexOfFirst { predicate(it) }
        if (queueIndex >= 0) {
            val msg = messageQueue.removeAt(queueIndex)
            cont.resume(msg)
            return@suspendCoroutine
        }

        var resolved = false
        lateinit var waiterFn: (dynamic) -> Unit
        waiterFn = { msg: dynamic ->
            if (!resolved && predicate(msg)) {
                resolved = true
                cont.resume(msg)
            } else {
                messageQueue.add(msg)
                waiters.add(waiterFn)
            }
        }
        waiters.add(waiterFn)
    }
}

/**
 * Creates an AsyncIterable that yields user messages for the SDK's query() function.
 * This is the key interop piece: SDK expects `prompt` to be an AsyncIterable<SDKUserMessage>.
 */
internal fun createMessageAsyncIterable(
    firstPrompt: String,
    stdinReader: StdinReader,
): dynamic {
    // Build the first message
    val firstMessage = js("{}")
    firstMessage.type = "user"
    firstMessage.message = js("{}")
    firstMessage.message.role = "user"
    firstMessage.message.content = firstPrompt
    firstMessage.parent_tool_use_id = null

    // Provider function that returns a Promise<IteratorResult>
    val nextMessageProvider: () -> dynamic = {
        GlobalScope.promise {
            val msg = stdinReader.waitForMessage("user_message", "abort")
            val msgType = msg.type as? String

            if (msgType == "abort") {
                val result = js("{}")
                result.done = true
                result.value = undefined
                result
            } else {
                val content = buildUserContent(msg)
                val userMessage = js("{}")
                userMessage.type = "user"
                userMessage.message = js("{}")
                userMessage.message.role = "user"
                userMessage.message.content = content
                userMessage.parent_tool_use_id = null

                val result = js("{}")
                result.done = false
                result.value = userMessage
                result
            }
        }
    }

    return createAsyncIterableJs(firstMessage, nextMessageProvider)
}

private fun buildUserContent(msg: dynamic): dynamic {
    val images = msg.images
    val documents = msg.documents
    val imageLen = if (images != null && images != undefined) (images.length as? Number)?.toInt() ?: 0 else 0
    val docLen = if (documents != null && documents != undefined) (documents.length as? Number)?.toInt() ?: 0 else 0
    val hasAttachments = imageLen > 0 || docLen > 0

    return if (hasAttachments) {
        val arr = js("[]")
        val textBlock = js("{}")
        textBlock.type = "text"
        textBlock.text = msg.text
        arr.push(textBlock)
        for (i in 0 until imageLen) {
            arr.push(images[i])
        }
        for (i in 0 until docLen) {
            arr.push(documents[i])
        }
        arr
    } else {
        msg.text
    }
}

// JS helper: creates an object with Symbol.asyncIterator
// Kotlin/JS cannot directly create Symbol.asyncIterator, so we use js() for this minimal interop
private fun createAsyncIterableJs(firstMessage: dynamic, nextProvider: () -> dynamic): dynamic = js("""
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
""")
