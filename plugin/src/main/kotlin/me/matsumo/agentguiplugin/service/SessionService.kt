package me.matsumo.agentguiplugin.service

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import me.matsumo.agentguiplugin.viewmodel.ChatViewModel

@Service(Service.Level.PROJECT)
class SessionService(
    private val project: Project,
) : Disposable {

    private val scope = CoroutineScope(SupervisorJob())

    val projectBasePath: String
        get() = project.basePath ?: System.getProperty("user.dir")

    val claudeCodePath: String?
        get() = service<SettingsService>().claudeCodePath

    val chatViewModel: ChatViewModel by lazy {
        ChatViewModel(
            projectBasePath = projectBasePath,
            claudeCodePath = claudeCodePath,
            scope = scope,
        )
    }

    override fun dispose() {
        chatViewModel.dispose()
        scope.cancel()
    }
}
