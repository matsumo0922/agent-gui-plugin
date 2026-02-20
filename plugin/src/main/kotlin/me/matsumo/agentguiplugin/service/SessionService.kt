package me.matsumo.agentguiplugin.service

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

@Service(Service.Level.PROJECT)
class SessionService(
    private val project: Project,
) : Disposable {

    private val scope = CoroutineScope(SupervisorJob())

    val projectBasePath: String
        get() = project.basePath ?: System.getProperty("user.dir")

    val claudeCodePath: String?
        get() = service<SettingsService>().claudeCodePath

    override fun dispose() {
        scope.cancel()
    }
}
