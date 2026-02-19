@file:OptIn(DelicateCoroutinesApi::class)

package me.matsumo.agentguiplugin.bridge.js

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import kotlinx.serialization.json.Json
import me.matsumo.agentguiplugin.bridge.js.external.AbortController
import me.matsumo.agentguiplugin.bridge.js.external.ClaudeAgentSdk
import me.matsumo.agentguiplugin.bridge.js.external.Readline
import me.matsumo.agentguiplugin.bridge.js.external.process
import me.matsumo.agentguiplugin.bridge.model.BridgeEvent
import me.matsumo.agentguiplugin.bridge.model.BridgeEventSerializer

private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    explicitNulls = false
}

private var requestCounter = 0

private fun send(event: BridgeEvent) {
    val jsonStr = json.encodeToString(BridgeEventSerializer, event)
    process.stdout.write(jsonStr + "\n")
}

fun main() {
    // Setup readline
    val rl = Readline.createInterface(js("({input: process.stdin})"))
    val stdinReader = StdinReader(rl)
    stdinReader.setup()

    // Setup abort handling
    val abortController = AbortController()
    rl.on("line") { line ->
        try {
            val msg = js("JSON.parse(line)")
            if ((msg.type as? String) == "abort") {
                abortController.abort()
            }
        } catch (_: Throwable) {
            // ignore
        }
    }

    // Global error handler
    process.on("uncaughtException") { err ->
        send(BridgeEvent.Error(message = err.message as? String ?: "Unknown error", fatal = true))
        process.exit(1)
    }

    // Send ready event
    send(BridgeEvent.Ready)

    // Main flow as a coroutine
    GlobalScope.promise {
        try {
            // Wait for start command
            val startMsg = stdinReader.waitForMessage("start")
            val prompt = startMsg.prompt as? String ?: ""
            val userOptions = startMsg.options ?: js("{}")

            // Build canUseTool callback
            val canUseTool: dynamic = if ((userOptions.permissionMode as? String) == "default") {
                { toolName: String, toolInput: dynamic, sdkOptions: dynamic ->
                    GlobalScope.promise {
                        val requestId = "req_${++requestCounter}"
                        val toolUseId = sdkOptions.toolUseID as? String

                        send(
                            BridgeEvent.PermissionRequest(
                                requestId = requestId,
                                toolName = toolName,
                                toolInput = dynamicToJsonObject(toolInput),
                                toolUseId = toolUseId,
                            )
                        )

                        val result = stdinReader.waitForPermissionResponse(requestId)
                        result
                    }
                }
            } else {
                undefined
            }

            // Build SDK options
            val sdkOptions = buildSdkOptions(userOptions, abortController, canUseTool)

            // Create async iterable for messages
            val messageIterable = createMessageAsyncIterable(prompt, stdinReader)

            // Call SDK query()
            val queryParams = js("{}")
            queryParams.prompt = messageIterable
            queryParams.options = sdkOptions

            val messageIter = ClaudeAgentSdk.query(queryParams)

            // Iterate over async iterable using for-await pattern via JS interop
            iterateAsyncIterable(messageIter) { message ->
                val events = mapSdkMessageToBridgeEvents(message)
                for (event in events) {
                    send(event)
                }
            }
        } catch (e: Throwable) {
            send(BridgeEvent.Error(message = e.message ?: "Unknown error", fatal = true))
        }

        process.exit(0)
    }
}

private fun buildSdkOptions(userOptions: dynamic, abortController: AbortController, canUseTool: dynamic): dynamic {
    val opts = js("{}")

    val cwd = userOptions.cwd as? String
    if (cwd != null) opts.cwd = cwd

    val resume = userOptions.resume as? String
    if (resume != null) opts.resume = resume

    val model = userOptions.model as? String
    if (model != null) opts.model = model

    val systemPrompt = userOptions.systemPrompt as? String
    if (systemPrompt != null) opts.systemPrompt = systemPrompt

    opts.settingSources = userOptions.settingSources ?: js("['user', 'project', 'local']")

    val disallowedTools = userOptions.disallowedTools
    if (disallowedTools != null && disallowedTools != undefined) opts.disallowedTools = disallowedTools

    val maxTurns = userOptions.maxTurns
    if (maxTurns != null && maxTurns != undefined) opts.maxTurns = maxTurns

    val maxThinkingTokens = userOptions.maxThinkingTokens
    if (maxThinkingTokens != null && maxThinkingTokens != undefined) opts.maxThinkingTokens = maxThinkingTokens

    val maxBudgetUsd = userOptions.maxBudgetUsd
    if (maxBudgetUsd != null && maxBudgetUsd != undefined) opts.maxBudgetUsd = maxBudgetUsd

    val env = userOptions.env
    if (env != null && env != undefined) opts.env = env

    opts.abortController = abortController

    if (canUseTool != null && canUseTool != undefined) {
        opts.canUseTool = canUseTool
    }

    val permissionMode = userOptions.permissionMode as? String
    opts.permissionMode = permissionMode ?: "default"

    val includePartial = userOptions.includePartialMessages
    opts.includePartialMessages = if (includePartial == false) false else true

    val claudeCodePath = userOptions.claudeCodePath as? String
    if (!claudeCodePath.isNullOrEmpty()) opts.pathToClaudeCodeExecutable = claudeCodePath

    return opts
}

/**
 * Iterates over a JS async iterable using the Symbol.asyncIterator protocol.
 * Kotlin/JS does not support `for await` natively, so we manually call the iterator.
 */
private suspend fun iterateAsyncIterable(asyncIterable: dynamic, handler: (dynamic) -> Unit) {
    val iterator = js("asyncIterable[Symbol.asyncIterator]()")
    while (true) {
        val result: dynamic = (iterator.next() as kotlin.js.Promise<dynamic>).await()
        if (result.done == true) break
        handler(result.value)
    }
}
