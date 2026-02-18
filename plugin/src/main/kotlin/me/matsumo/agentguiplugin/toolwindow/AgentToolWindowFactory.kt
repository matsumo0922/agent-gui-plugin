package me.matsumo.agentguiplugin.toolwindow

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import me.matsumo.agentguiplugin.bridge.model.BridgeEvent
import me.matsumo.agentguiplugin.service.SessionService
import me.matsumo.agentguiplugin.service.SettingsService
import me.matsumo.agentguiplugin.ui.ChatPanel
import me.matsumo.agentguiplugin.ui.dialog.AskUserQuestionDialog
import me.matsumo.agentguiplugin.ui.dialog.PermissionDialog
import me.matsumo.agentguiplugin.viewmodel.ChatViewModel
import org.jetbrains.jewel.bridge.addComposeTab

class AgentToolWindowFactory : ToolWindowFactory {
    override fun shouldBeAvailable(project: Project) = true

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        toolWindow.addComposeTab("Claude Code", focusOnClickInside = true) {
            val scope = rememberCoroutineScope()
            val sessionService = remember { project.service<SessionService>() }
            val settings = remember { service<SettingsService>() }
            var viewModel by remember { mutableStateOf<ChatViewModel?>(null) }

            LaunchedEffect(Unit) {
                sessionService.connect()
                val client = sessionService.client ?: return@LaunchedEffect
                val vm = ChatViewModel(
                    client = client,
                    projectBasePath = sessionService.projectBasePath,
                    claudeCodePath = settings.claudeCodePath,
                    scope = scope,
                )
                viewModel = vm
                vm.initialize()
            }

            val vm = viewModel
            if (vm != null) {
                val uiState by vm.uiState.collectAsState()

                val permission = uiState.pendingPermission
                LaunchedEffect(permission) {
                    if (permission != null) {
                        showPermissionDialog(permission, vm)
                    }
                }

                val question = uiState.pendingQuestion
                LaunchedEffect(question) {
                    if (question != null) {
                        showQuestionDialog(question, vm)
                    }
                }

                ChatPanel(viewModel = vm)
            }
        }
    }
}

private fun showPermissionDialog(
    request: BridgeEvent.PermissionRequest,
    viewModel: ChatViewModel,
) {
    invokeLater {
        val dialog = PermissionDialog(
            toolName = request.toolName,
            toolInput = request.toolInput,
        )
        dialog.show()
        viewModel.respondPermission(dialog.isAllowed)
    }
}

private fun showQuestionDialog(
    request: BridgeEvent.PermissionRequest,
    viewModel: ChatViewModel,
) {
    invokeLater {
        val dialog = AskUserQuestionDialog(toolInput = request.toolInput)
        if (dialog.showAndGet()) {
            viewModel.respondQuestion(dialog.answers)
        } else {
            viewModel.respondPermission(false)
        }
    }
}
