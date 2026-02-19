package me.matsumo.agentguiplugin.bridge.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import me.matsumo.agentguiplugin.bridge.model.BridgeCommand
import me.matsumo.agentguiplugin.bridge.model.BridgeEvent
import me.matsumo.agentguiplugin.bridge.model.PermissionResult
import me.matsumo.agentguiplugin.bridge.model.SessionOptions
import me.matsumo.agentguiplugin.bridge.process.NodeProcess

/**
 * Node.js ブリッジプロセスとの JSONL 通信クライアント。
 * コマンド送信とイベント受信を提供する。
 */
class BridgeClient(
    private val nodePath: String,
    private val bridgeScriptPath: String,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    private val _events = MutableSharedFlow<BridgeEvent>(extraBufferCapacity = 256)
    val events: SharedFlow<BridgeEvent> = _events.asSharedFlow()

    private var nodeProcess: NodeProcess? = null
    private var connected = false

    val isConnected: Boolean get() = connected && nodeProcess?.isAlive == true

    /** ブリッジプロセスを起動し、stdout の監視を開始する */
    suspend fun connect(scope: CoroutineScope) {
        val process = NodeProcess(nodePath, bridgeScriptPath)
        nodeProcess = process

        scope.launch {
            try {
                process.start().collect { line ->
                    parseLine(line)?.let { _events.emit(it) }
                }
            } catch (e: Exception) {
                _events.emit(
                    BridgeEvent.Error(
                        message = "Bridge process terminated: ${e.message}",
                        fatal = true,
                    )
                )
            } finally {
                connected = false
            }
        }

        // プロセス起動完了（ready イベントは events Flow で受信される）
        connected = true
    }

    /** ブリッジプロセスを切断・終了する */
    fun disconnect() {
        connected = false
        nodeProcess?.destroy()
        nodeProcess = null
    }

    suspend fun startSession(prompt: String, options: SessionOptions = SessionOptions()) {
        sendCommand(BridgeCommand.Start(prompt = prompt, options = options))
    }

    suspend fun sendUserMessage(text: String) {
        sendCommand(BridgeCommand.UserMessage(text = text))
    }

    suspend fun respondPermission(requestId: String, result: PermissionResult) {
        sendCommand(BridgeCommand.PermissionResponse(requestId = requestId, result = result))
    }

    suspend fun abort() {
        sendCommand(BridgeCommand.Abort)
    }

    private suspend fun sendCommand(command: BridgeCommand) {
        val jsonStr = json.encodeToString(command)
        nodeProcess?.writeLine(jsonStr)
    }

    private fun parseLine(line: String): BridgeEvent? = try {
        json.decodeFromString<BridgeEvent>(line)
    } catch (_: Exception) {
        null
    }
}
