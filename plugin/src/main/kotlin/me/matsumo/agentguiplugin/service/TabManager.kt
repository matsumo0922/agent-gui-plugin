package me.matsumo.agentguiplugin.service

import androidx.compose.runtime.LaunchedEffect
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.content.Content
import com.intellij.ui.content.Content.TEMPORARY_REMOVED_KEY
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.content.ContentManagerEvent
import com.intellij.ui.content.ContentManagerListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.matsumo.agentguiplugin.ui.ChatPanel
import me.matsumo.agentguiplugin.viewmodel.ChatMessage
import me.matsumo.agentguiplugin.viewmodel.ChatViewModel
import me.matsumo.agentguiplugin.viewmodel.SessionState
import me.matsumo.agentguiplugin.viewmodel.getActiveMessages
import org.jetbrains.jewel.bridge.JewelComposePanel
import org.jetbrains.jewel.foundation.enableNewSwingCompositing
import java.util.concurrent.ConcurrentHashMap

/**
 * IntelliJ ToolWindow の Content タブと ChatViewModel のライフサイクルを管理する。
 * 各タブ = 1 つの addComposeTab() 呼び出し + 1 つの ChatViewModel。
 */
class TabManager(
    private val toolWindow: ToolWindow,
    private val project: Project,
    private val settingsService: SettingsService,
    private val scope: CoroutineScope,
) {
    private val viewModels = ConcurrentHashMap<Content, ChatViewModel>()
    private val titleJobs = ConcurrentHashMap<Content, Job>()

    private val projectBasePath: String
        get() = project.basePath ?: System.getProperty("user.dir")

    private val claudeCodePath: String?
        get() = settingsService.claudeCodePath

    init {
        toolWindow.contentManager.addContentManagerListener(object : ContentManagerListener {
            override fun contentRemoved(event: ContentManagerEvent) {
                val content = event.content

                // Split/Unsplit 中の一時的な移動では ViewModel を破棄しない
                if (content.getUserData(TEMPORARY_REMOVED_KEY) == true) return

                titleJobs.remove(content)?.cancel()
                viewModels.remove(content)?.dispose()

                // 最後のタブが閉じられたら新しい空タブを作成
                if (toolWindow.contentManager.contentCount == 0) {
                    invokeLater { addTab() }
                }
            }
        })
    }

    /**
     * ChatViewModel と Compose UI を持つ Content を生成して返す。
     * ContentManager への追加は呼び出し側の責務。
     * Split API (ToolWindowSplitContentProvider) からも利用される。
     */
    fun createContent(
        title: String = "New chat",
        vm: ChatViewModel = createViewModel(),
        resumeSessionId: String? = null,
    ): Content {
        enableNewSwingCompositing()

        val component = JewelComposePanel(focusOnClickInside = true) {
            LaunchedEffect(vm) {
                if (vm.uiState.value.sessionState == SessionState.Disconnected) {
                    vm.start(resumeSessionId = resumeSessionId)
                }
            }
            ChatPanel(viewModel = vm, project = project)
        }

        val content = ContentFactory.getInstance().createContent(component, title, false)
        content.isCloseable = true

        viewModels[content] = vm
        observeTitle(content, vm)

        return content
    }

    /**
     * 新しいチャットタブを追加して選択する。
     */
    fun addTab(title: String = "New chat") {
        val content = createContent(title)
        toolWindow.contentManager.addContent(content)
        toolWindow.contentManager.setSelectedContent(content, true)
    }

    /**
     * セッション履歴からタブを作成して resume する。
     */
    fun resumeSession(summary: SessionHistoryService.SessionSummary, history: SessionHistoryService.SessionHistory) {
        val title = summary.firstPrompt?.take(40) ?: "Resumed session"
        val vm = createViewModel()
        vm.importHistory(history.messages, toolResults = history.toolResults)

        val content = createContent(title = title, vm = vm, resumeSessionId = summary.sessionId)
        toolWindow.contentManager.addContent(content)
        toolWindow.contentManager.setSelectedContent(content, true)
    }

    fun dispose() {
        titleJobs.values.forEach { it.cancel() }
        titleJobs.clear()
        viewModels.values.forEach { it.dispose() }
        viewModels.clear()
    }

    private fun createViewModel(): ChatViewModel {
        return ChatViewModel(
            projectBasePath = projectBasePath,
            claudeCodePath = claudeCodePath,
            initialModel = settingsService.model,
            initialPermissionMode = settingsService.permissionMode,
        )
    }

    /**
     * ChatViewModel の最初のユーザーメッセージを監視してタブタイトルを自動更新。
     */
    private fun observeTitle(content: Content, vm: ChatViewModel) {
        val job = scope.launch {
            vm.uiState
                .map { state -> state.conversationTree }
                .distinctUntilChanged()
                .map { tree -> tree.getActiveMessages().filterIsInstance<ChatMessage.User>().firstOrNull()?.text }
                .distinctUntilChanged()
                .collect { firstUserMessage ->
                    if (firstUserMessage != null) {
                        val title = firstUserMessage.take(40).let {
                            if (firstUserMessage.length > 40) "$it..." else it
                        }
                        invokeLater { content.displayName = title }
                    }
                }
        }
        titleJobs[content] = job
    }
}
