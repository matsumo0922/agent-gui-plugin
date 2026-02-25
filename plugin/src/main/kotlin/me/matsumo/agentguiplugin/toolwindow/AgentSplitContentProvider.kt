package me.matsumo.agentguiplugin.toolwindow

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.toolWindow.ToolWindowSplitContentProvider
import com.intellij.ui.content.Content
import me.matsumo.agentguiplugin.service.SessionService

class AgentSplitContentProvider : ToolWindowSplitContentProvider {
    override fun createContentCopy(project: Project, content: Content): Content {
        val tabManager = project.service<SessionService>().tabManager
            ?: error("TabManager not initialized")

        return tabManager.createContent()
    }
}
