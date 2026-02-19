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

    suspend fun connect(scope: CoroutineScope) {
        val process = NodeProcess(nodePath, bridgeScriptPath)
        nodeProcess = process

        scope.launch {
            try {
                process.start().collect { line ->
                    val event = parseLine(line)
                    if (event != null) {
                        _events.emit(event)
                    }
                }
            } catch (e: Exception) {
                _events.emit(BridgeEvent.Error(
                    message = "Bridge process terminated: ${e.message}",
                    fatal = true,
                ))
            } finally {
                connected = false
            }
        }

        // Wait for ready event
        // The first event should be Ready
        connected = true
    }

    fun disconnect() {
        connected = false
        nodeProcess?.destroy()
        nodeProcess = null
    }

    suspend fun startSession(prompt: String, options: SessionOptions = SessionOptions()) {
        val command = BridgeCommand.Start(prompt = prompt, options = options)
        sendCommand(command)
    }

    suspend fun sendUserMessage(text: String) {
        val command = BridgeCommand.UserMessage(text = text)
        sendCommand(command)
    }

    suspend fun respondPermission(requestId: String, result: PermissionResult) {
        val command = BridgeCommand.PermissionResponse(requestId = requestId, result = result)
        sendCommand(command)
    }

    suspend fun abort() {
        sendCommand(BridgeCommand.Abort)
    }

    private suspend fun sendCommand(command: BridgeCommand) {
        val jsonStr = json.encodeToString(command)
        nodeProcess?.writeLine(jsonStr)
    }

    private fun parseLine(line: String): BridgeEvent? {
        return try {
            json.decodeFromString<BridgeEvent>(line)
        } catch (_: Exception) {
            null
        }
    }
}
