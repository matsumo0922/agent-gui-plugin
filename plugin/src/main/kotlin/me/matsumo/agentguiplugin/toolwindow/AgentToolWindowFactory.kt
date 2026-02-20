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
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import me.matsumo.agentguiplugin.service.SessionService
import me.matsumo.agentguiplugin.ui.ChatPanel
import me.matsumo.agentguiplugin.ui.dialog.AskUserQuestionDialog
import me.matsumo.agentguiplugin.ui.dialog.PermissionDialog
import me.matsumo.agentguiplugin.viewmodel.ChatViewModel
import me.matsumo.agentguiplugin.viewmodel.PendingPermission
import me.matsumo.agentguiplugin.viewmodel.PendingQuestion
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
    request: PendingPermission,
    viewModel: ChatViewModel,
) {
    invokeLater {
        val dialog = PermissionDialog(
            toolName = request.toolName,
            toolInput = request.toolInput.toJsonObject(),
        )
        dialog.show()
        viewModel.respondPermission(dialog.isAllowed)
    }
}

private fun showQuestionDialog(
    request: PendingQuestion,
    viewModel: ChatViewModel,
) {
    invokeLater {
        val dialog = AskUserQuestionDialog(toolInput = request.toolInput.toJsonObject())
        if (dialog.showAndGet()) {
            viewModel.respondQuestion(dialog.answers)
        } else {
            viewModel.respondPermission(false)
        }
    }
}

private fun Map<String, Any?>.toJsonObject(): JsonObject {
    return JsonObject(this.mapValues { (_, v) -> v.toJsonElement() })
}

private fun Any?.toJsonElement(): kotlinx.serialization.json.JsonElement = when (this) {
    null -> JsonNull
    is String -> JsonPrimitive(this)
    is Number -> JsonPrimitive(this)
    is Boolean -> JsonPrimitive(this)
    is Map<*, *> -> JsonObject(
        this.entries.associate { (k, v) -> k.toString() to v.toJsonElement() }
    )
    is List<*> -> JsonArray(this.map { it.toJsonElement() })
    is kotlinx.serialization.json.JsonElement -> this
    else -> JsonPrimitive(this.toString())
}
