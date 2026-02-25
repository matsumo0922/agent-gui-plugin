package me.matsumo.agentguiplugin.toolwindow

import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import me.matsumo.agentguiplugin.service.SessionHistoryService
import me.matsumo.agentguiplugin.service.TabManager
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.swing.DefaultListCellRenderer
import javax.swing.DefaultListModel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.ListSelectionModel

object SessionHistoryAction {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        .withZone(ZoneId.systemDefault())

    fun show(project: Project, tabManager: TabManager) {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        scope.launch {
            val historyService = project.service<SessionHistoryService>()
            val sessions = historyService.listSessions()

            println(sessions.joinToString("\n") { it.toString() })

            invokeLater {
                val dialog = SessionHistoryDialog(project, sessions)
                if (dialog.showAndGet()) {
                    val selected = dialog.selectedSession ?: return@invokeLater
                    scope.launch {
                        val messages = historyService.readSessionMessages(selected.sessionId)
                        invokeLater {
                            tabManager.resumeSession(selected, messages)
                        }
                    }
                }
            }
        }
    }

    private class SessionHistoryDialog(
        project: Project,
        private val sessions: List<SessionHistoryService.SessionSummary>,
    ) : DialogWrapper(project, false) {

        private val listModel = DefaultListModel<SessionHistoryService.SessionSummary>()
        private val list = JBList(listModel)

        val selectedSession: SessionHistoryService.SessionSummary?
            get() = list.selectedValue

        init {
            title = "Session History"
            setOKButtonText("Resume")

            sessions.forEach { listModel.addElement(it) }

            list.selectionMode = ListSelectionModel.SINGLE_SELECTION
            list.cellRenderer = SessionCellRenderer()
            if (listModel.size > 0) list.selectedIndex = 0

            init()
        }

        override fun createCenterPanel(): JPanel {
            val panel = JPanel(BorderLayout())
            panel.preferredSize = Dimension(500, 400)

            if (sessions.isEmpty()) {
                panel.add(JBLabel("No session history found for this project."), BorderLayout.CENTER)
            } else {
                panel.add(JBScrollPane(list), BorderLayout.CENTER)
            }

            return panel
        }

        override fun isOKActionEnabled(): Boolean {
            return list.selectedValue != null
        }

        private inner class SessionCellRenderer : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>,
                value: Any?,
                index: Int,
                isSelected: Boolean,
                cellHasFocus: Boolean,
            ): Component {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
                val summary = value as? SessionHistoryService.SessionSummary ?: return this

                val dateStr = summary.lastModifiedAt?.let { dateFormatter.format(it) }
                    ?: summary.startTime?.let { dateFormatter.format(it) }
                    ?: "Unknown"
                val promptStr = summary.firstPrompt?.take(60) ?: "(no prompt)"
                val metaStr = buildString {
                    summary.model?.let { append(it) }
                }

                text = "<html><b>$dateStr</b><br>$promptStr<br><font color='gray'>$metaStr</font></html>"
                return this
            }
        }
    }
}
