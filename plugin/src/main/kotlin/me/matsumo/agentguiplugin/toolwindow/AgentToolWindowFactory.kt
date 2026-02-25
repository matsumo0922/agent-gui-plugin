package me.matsumo.agentguiplugin.toolwindow

import com.intellij.execution.ui.RunnerLayoutUi
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
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

        // RunnerLayoutUi を生成
        val layoutUi = RunnerLayoutUi.Factory.getInstance(project).create(
            "AgentGUI",
            "Claude Code",
            "Claude Code Chat",
            project,
        )

        val tabManager = sessionService.getOrCreateTabManager(layoutUi)

        // RunnerLayoutUi.component を ToolWindow の単一ルート Content として追加
        // ※ setLeftToolbar より先に component を attach しないと NPE になる
        val rootContent = toolWindow.contentManager.factory.createContent(
            layoutUi.component,
            null,
            false,
        )
        toolWindow.contentManager.addContent(rootContent)

        // アクションを RunnerLayoutUi のツールバーに配置
        val actionGroup = DefaultActionGroup().apply {
            add(object : DumbAwareAction("New Chat", "Start a new chat", AllIcons.General.Add) {
                override fun actionPerformed(e: AnActionEvent) {
                    tabManager.addTab()
                }
            })
            add(object : DumbAwareAction("Session History", "Browse session history", AllIcons.Vcs.History) {
                override fun actionPerformed(e: AnActionEvent) {
                    SessionHistoryAction.show(project, tabManager)
                }
            })
        }
        layoutUi.options.setLeftToolbar(actionGroup, "AgentGUI.LeftToolbar")

        // 初期タブを作成
        tabManager.addTab()
    }
}
