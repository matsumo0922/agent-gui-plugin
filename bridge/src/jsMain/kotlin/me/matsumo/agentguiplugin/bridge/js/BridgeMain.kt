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
import me.matsumo.agentguiplugin.bridge.model.BridgeCommandTypes
import me.matsumo.agentguiplugin.bridge.model.BridgeEvent
import me.matsumo.agentguiplugin.bridge.model.BridgeEventSerializer

private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    explicitNulls = false
}

private var requestCounter = 0

/** BridgeEvent を JSONL で stdout に送出 */
private fun send(event: BridgeEvent) {
    val jsonStr = json.encodeToString(BridgeEventSerializer, event)
    process.stdout.write(jsonStr + "\n")
}

fun main() {
    val rl = Readline.createInterface(js("({input: process.stdin})"))
    val abortController = AbortController()

    // StdinReader に abort 検知コールバックを渡し、二重購読を回避
    val stdinReader = StdinReader(rl, onAbort = { abortController.abort() })
    stdinReader.setup()

    // グローバルエラーハンドラ
    process.on("uncaughtException") { err ->
        send(BridgeEvent.Error(message = dynamicString(err.message, "Unknown error"), fatal = true))
        process.exit(1)
    }

    // ready イベントを送出してコマンド待ちへ
    send(BridgeEvent.Ready)

    GlobalScope.promise {
        try {
            // start コマンドを待機
            val startMsg = stdinReader.waitForMessage(BridgeCommandTypes.START)
            val prompt = dynamicString(startMsg.prompt)
            val userOptions = startMsg.options ?: js("{}")

            val canUseTool = buildCanUseToolCallback(userOptions, stdinReader)
            val sdkOptions = buildSdkOptions(userOptions, abortController, canUseTool)
            val messageIterable = createMessageAsyncIterable(prompt, stdinReader)

            // SDK query() を呼び出しイベントをストリーミング
            val queryParams = js("{}")
            queryParams.prompt = messageIterable
            queryParams.options = sdkOptions

            iterateAsyncIterable(ClaudeAgentSdk.query(queryParams)) { message ->
                mapSdkMessageToBridgeEvents(message).forEach(::send)
            }
        } catch (e: Throwable) {
            send(BridgeEvent.Error(message = e.message ?: "Unknown error", fatal = true))
        }

        process.exit(0)
    }
}

/**
 * permissionMode が "default" の場合、パーミッション要求→応答のコールバックを生成。
 * それ以外は undefined を返す（SDK にコールバックを渡さない）。
 */
private fun buildCanUseToolCallback(userOptions: dynamic, stdinReader: StdinReader): dynamic {
    val permissionMode = dynamicStringOrNull(userOptions.permissionMode)
    if (permissionMode != PermissionModes.DEFAULT) return undefined

    return { toolName: String, toolInput: dynamic, sdkOptions: dynamic ->
        GlobalScope.promise {
            val requestId = "req_${++requestCounter}"

            send(
                BridgeEvent.PermissionRequest(
                    requestId = requestId,
                    toolName = toolName,
                    toolInput = dynamicToJsonObject(toolInput),
                    toolUseId = dynamicStringOrNull(sdkOptions.toolUseID),
                )
            )

            stdinReader.waitForPermissionResponse(requestId)
        }
    }
}

/** SDK に渡すオプションオブジェクトを組み立てる */
private fun buildSdkOptions(
    userOptions: dynamic,
    abortController: AbortController,
    canUseTool: dynamic,
): dynamic {
    val opts = js("{}")

    // 文字列オプション（null/undefined なら省略）
    dynamicStringOrNull(userOptions.cwd)?.let { opts.cwd = it }
    dynamicStringOrNull(userOptions.resume)?.let { opts.resume = it }
    dynamicStringOrNull(userOptions.model)?.let { opts.model = it }
    dynamicStringOrNull(userOptions.systemPrompt)?.let { opts.systemPrompt = it }

    // 配列・数値オプション（null/undefined でなければそのままコピー）
    opts.settingSources = userOptions.settingSources ?: js("['user', 'project', 'local']")
    if (!isNullOrUndefined(userOptions.disallowedTools)) opts.disallowedTools = userOptions.disallowedTools
    if (!isNullOrUndefined(userOptions.maxTurns)) opts.maxTurns = userOptions.maxTurns
    if (!isNullOrUndefined(userOptions.maxThinkingTokens)) opts.maxThinkingTokens = userOptions.maxThinkingTokens
    if (!isNullOrUndefined(userOptions.maxBudgetUsd)) opts.maxBudgetUsd = userOptions.maxBudgetUsd
    if (!isNullOrUndefined(userOptions.env)) opts.env = userOptions.env

    // 固定オプション
    opts.abortController = abortController
    if (!isNullOrUndefined(canUseTool)) opts.canUseTool = canUseTool
    opts.permissionMode = dynamicStringOrNull(userOptions.permissionMode) ?: PermissionModes.DEFAULT
    opts.includePartialMessages = dynamicBool(userOptions.includePartialMessages, default = true)

    // Claude Code CLI パス
    dynamicStringOrNull(userOptions.claudeCodePath)
        ?.takeIf { it.isNotEmpty() }
        ?.let { opts.pathToClaudeCodeExecutable = it }

    return opts
}

/**
 * JS の async iterable を手動で走査する。
 * Kotlin/JS は for-await をサポートしないため、Symbol.asyncIterator プロトコルを直接呼ぶ。
 */
private suspend fun iterateAsyncIterable(asyncIterable: dynamic, handler: (dynamic) -> Unit) {
    val iterator = js("asyncIterable[Symbol.asyncIterator]()")
    while (true) {
        val result: dynamic = (iterator.next() as kotlin.js.Promise<dynamic>).await()
        if (result.done == true) break
        handler(result.value)
    }
}
