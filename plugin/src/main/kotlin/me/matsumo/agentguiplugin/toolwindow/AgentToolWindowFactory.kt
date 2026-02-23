package me.matsumo.agentguiplugin.toolwindow

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import me.matsumo.agentguiplugin.service.SessionService
import me.matsumo.agentguiplugin.ui.ChatPanel
import me.matsumo.agentguiplugin.viewmodel.SessionState
import org.jetbrains.jewel.bridge.addComposeTab

class AgentToolWindowFactory : ToolWindowFactory {
    override fun shouldBeAvailable(project: Project) = true

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        toolWindow.addComposeTab("Claude Code", focusOnClickInside = true) {
            val sessionService = remember { project.service<SessionService>() }
            val viewModel = remember { sessionService.chatViewModel }

            LaunchedEffect(viewModel) {
                if (viewModel.uiState.value.sessionState == SessionState.Disconnected) {
                    viewModel.initialize()
                }
            }

            ChatPanel(viewModel = viewModel, project = project)
        }
    }
}
