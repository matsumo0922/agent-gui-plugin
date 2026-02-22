package me.matsumo.agentguiplugin.toolwindow

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import me.matsumo.agentguiplugin.service.SessionService
import me.matsumo.agentguiplugin.ui.ChatPanel
import me.matsumo.agentguiplugin.viewmodel.ChatViewModel
import org.jetbrains.jewel.bridge.addComposeTab

class AgentToolWindowFactory : ToolWindowFactory {
    override fun shouldBeAvailable(project: Project) = true

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        toolWindow.addComposeTab("Claude Code", focusOnClickInside = true) {
            val scope = rememberCoroutineScope()
            val sessionService = remember { project.service<SessionService>() }
            var viewModel by remember { mutableStateOf<ChatViewModel?>(null) }

            LaunchedEffect(Unit) {
                val vm = ChatViewModel(
                    projectBasePath = sessionService.projectBasePath,
                    claudeCodePath = sessionService.claudeCodePath,
                    scope = scope,
                )
                viewModel = vm
                vm.initialize()
            }

            viewModel?.let {
                ChatPanel(viewModel = it, project = project)
            }
        }
    }
}
