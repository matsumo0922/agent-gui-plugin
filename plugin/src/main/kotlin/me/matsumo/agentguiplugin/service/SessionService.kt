package me.matsumo.agentguiplugin.service

import com.intellij.execution.ui.RunnerLayoutUi
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

    private var tabManager: TabManager? = null

    fun getOrCreateTabManager(layoutUi: RunnerLayoutUi): TabManager {
        return tabManager ?: TabManager(
            layoutUi = layoutUi,
            project = project,
            settingsService = service<SettingsService>(),
            scope = scope,
        ).also { tabManager = it }
    }

    override fun dispose() {
        tabManager?.dispose()
        tabManager = null
        scope.cancel()
    }
}
