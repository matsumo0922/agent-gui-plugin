package me.matsumo.agentguiplugin.toolwindow

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import me.matsumo.agentguiplugin.service.SessionService

class AgentToolWindowFactory : ToolWindowFactory {
    override fun shouldBeAvailable(project: Project) = true

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val sessionService = project.service<SessionService>()
        val tabManager = sessionService.getOrCreateTabManager(toolWindow)

        // IDE タイトルバーにアクションボタンを配置
        toolWindow.title = ""
        toolWindow.setTitleActions(
            listOf(
                object : DumbAwareAction("New Chat", "Start a new chat", AllIcons.General.Add) {
                    override fun actionPerformed(e: AnActionEvent) {
                        tabManager.addTab()
                    }
                },
                object : DumbAwareAction("Session History", "Browse session history", AllIcons.Vcs.History) {
                    override fun actionPerformed(e: AnActionEvent) {
                        SessionHistoryAction.show(project, tabManager)
                    }
                },
                object : DumbAwareAction("Clear Chat", "Clear the current chat", AllIcons.General.Remove) {
                    override fun actionPerformed(e: AnActionEvent) {
                        tabManager.clearCurrentTab()
                    }
                },
            ),
        )

        // 初期タブを作成
        tabManager.addTab()
    }
}
