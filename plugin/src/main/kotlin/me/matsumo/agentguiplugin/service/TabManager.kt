package me.matsumo.agentguiplugin.service

import com.intellij.execution.ui.RunnerLayoutUi
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.project.Project
import com.intellij.ui.content.Content
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
import org.jetbrains.jewel.bridge.JewelComposePanel
import java.awt.BorderLayout
import java.awt.Graphics
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * RunnerLayoutUi の Content タブと ChatViewModel のライフサイクルを管理する。
 * 各タブ = 1 つの JewelComposePanel + 1 つの ChatViewModel。
 */
class TabManager(
    private val layoutUi: RunnerLayoutUi,
    private val project: Project,
    private val settingsService: SettingsService,
    private val scope: CoroutineScope,
) {
    private val viewModels = ConcurrentHashMap<Content, ChatViewModel>()
    private val titleJobs = ConcurrentHashMap<Content, Job>()
    private val tabCounter = AtomicInteger(0)

    private val projectBasePath: String
        get() = project.basePath ?: System.getProperty("user.dir")

    private val claudeCodePath: String?
        get() = settingsService.claudeCodePath

    init {
        layoutUi.contentManager.addContentManagerListener(object : ContentManagerListener {
            override fun contentRemoved(event: ContentManagerEvent) {
                val content = event.content
                titleJobs.remove(content)?.cancel()
                viewModels.remove(content)?.dispose()

                // 最後のタブが閉じられたら新しい空タブを作成
                if (layoutUi.contentManager.contentCount == 0) {
                    invokeLater { addTab() }
                }
            }
        })
    }

    /**
     * 新しいチャットタブを追加して選択する。
     */
    fun addTab(title: String = "New chat") {
        val vm = createViewModel()
        val contentId = "chat-tab-${tabCounter.incrementAndGet()}"

        val component = createComposeComponent(vm)
        val content = layoutUi.createContent(
            contentId,
            component,
            title,
            AllIcons.Actions.Execute,
            null,
        )
        content.isCloseable = true

        layoutUi.addContent(content)
        viewModels[content] = vm

        layoutUi.contentManager.setSelectedContent(content, true)
        observeTitle(content, vm)
    }

    /**
     * セッション履歴からタブを作成して resume する。
     */
    fun resumeSession(summary: SessionHistoryService.SessionSummary, historyMessages: List<ChatMessage>) {
        val title = summary.firstPrompt?.take(40) ?: "Resumed session"
        val vm = createViewModel()
        vm.importHistory(historyMessages)

        val contentId = "chat-tab-${tabCounter.incrementAndGet()}"
        val component = createComposeComponent(vm, resumeSessionId = summary.sessionId)
        val content = layoutUi.createContent(
            contentId,
            component,
            title,
            AllIcons.Actions.Execute,
            null,
        )
        content.isCloseable = true

        layoutUi.addContent(content)
        viewModels[content] = vm

        layoutUi.contentManager.setSelectedContent(content, true)
        observeTitle(content, vm)
    }

    fun dispose() {
        titleJobs.values.forEach { it.cancel() }
        titleJobs.clear()
        viewModels.values.forEach { it.dispose() }
        viewModels.clear()
    }

    private fun createComposeComponent(vm: ChatViewModel, resumeSessionId: String? = null): JComponent {
        val composeComponent = JewelComposePanel {
            androidx.compose.runtime.LaunchedEffect(vm) {
                if (vm.uiState.value.sessionState == SessionState.Disconnected) {
                    vm.start(resumeSessionId = resumeSessionId)
                }
            }
            ChatPanel(viewModel = vm, project = project)
        }

        // Skiko (Metal/GPU) は BufferedImage への描画に非対応のため、
        // RunnerLayoutUi のドラッグ時にタブ画像キャプチャで UnsupportedOperationException が発生する。
        // ラッパー JPanel で paint 例外をキャッチし、フォールバック描画を返すことで回避する。
        return object : JPanel(BorderLayout()) {
            init {
                add(composeComponent, BorderLayout.CENTER)
            }

            override fun paint(g: Graphics) {
                try {
                    super.paint(g)
                } catch (_: UnsupportedOperationException) {
                    g.color = background
                    g.fillRect(0, 0, width, height)
                }
            }
        }
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
                .map { state -> state.messages.filterIsInstance<ChatMessage.User>().firstOrNull()?.text }
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
