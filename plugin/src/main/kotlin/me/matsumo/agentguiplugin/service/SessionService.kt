package me.matsumo.agentguiplugin.service

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import me.matsumo.agentguiplugin.bridge.client.BridgeClient
import me.matsumo.agentguiplugin.bridge.process.BridgeScriptExtractor
import me.matsumo.agentguiplugin.bridge.process.NodeResolver

@Service(Service.Level.PROJECT)
class SessionService(
    private val project: Project,
) : Disposable {

    private val scope = CoroutineScope(SupervisorJob())
    private var bridgeClient: BridgeClient? = null

    val client: BridgeClient? get() = bridgeClient

    fun connect() {
        val settings = service<SettingsService>()
        val nodePath = NodeResolver.resolve(settings.nodePath)
            ?: error("Node.js not found. Please install Node.js or set the path in settings.")

        val scriptPath = BridgeScriptExtractor.extract()

        val client = BridgeClient(nodePath, scriptPath)
        bridgeClient = client

        scope.launch {
            client.connect(this)
        }
    }

    fun disconnect() {
        scope.launch {
            bridgeClient?.abort()
        }
        bridgeClient?.disconnect()
        bridgeClient = null
    }

    val projectBasePath: String
        get() = project.basePath ?: System.getProperty("user.dir")

    override fun dispose() {
        disconnect()
        scope.cancel()
    }
}
