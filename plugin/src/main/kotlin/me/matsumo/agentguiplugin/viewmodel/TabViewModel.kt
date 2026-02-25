package me.matsumo.agentguiplugin.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.matsumo.agentguiplugin.model.ChatTab
import me.matsumo.agentguiplugin.service.SessionHistoryService
import me.matsumo.agentguiplugin.service.SettingsService
import java.util.concurrent.ConcurrentHashMap

class TabViewModel(
    private val projectBasePath: String,
    private val claudeCodePath: String?,
    private val settingsService: SettingsService,
    private val scope: CoroutineScope,
) {
    // --- State ---
    private val _tabs = MutableStateFlow<List<ChatTab>>(listOf(ChatTab()))
    val tabs: StateFlow<List<ChatTab>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow<String>(_tabs.value.first().id)
    val activeTabId: StateFlow<String> = _activeTabId.asStateFlow()

    // viewModels を Flow 化し、activeChatViewModel を combine で構成する
    private val _viewModels = MutableStateFlow<Map<String, ChatViewModel>>(emptyMap())

    val activeChatViewModel: StateFlow<ChatViewModel?> = combine(
        _activeTabId,
        _viewModels,
    ) { activeId, vms ->
        vms[activeId]
    }.stateIn(scope, SharingStarted.WhileSubscribed(), null)

    // タブタイトル監視 Job の管理（リーク防止）
    private val titleObserverJobs = ConcurrentHashMap<String, Job>()

    init {
        val firstTab = _tabs.value.first()
        val vm = createViewModel()
        _viewModels.update { it + (firstTab.id to vm) }
        observeTabTitles(firstTab.id, vm)
    }

    // --- Public API ---

    fun addTab(): ChatTab {
        val tab = ChatTab()
        val vm = createViewModel()
        _viewModels.update { it + (tab.id to vm) }
        _tabs.update { it + tab }
        _activeTabId.value = tab.id
        observeTabTitles(tab.id, vm)
        scope.launch { vm.start() }
        return tab
    }

    fun removeTab(tabId: String) {
        if (_tabs.value.size <= 1) return

        // タイトル監視 Job を cancel
        titleObserverJobs.remove(tabId)?.cancel()

        // ViewModel を dispose
        val vm = _viewModels.value[tabId]
        vm?.dispose()
        _viewModels.update { it - tabId }

        _tabs.update { it.filter { tab -> tab.id != tabId } }
        if (_activeTabId.value == tabId) {
            _activeTabId.value = _tabs.value.first().id
        }
    }

    fun selectTab(tabId: String) {
        _activeTabId.value = tabId
    }

    fun clearActiveTab() {
        val tabId = _activeTabId.value
        val vm = _viewModels.value[tabId] ?: return
        scope.launch {
            vm.clear()
            _tabs.update { tabs ->
                tabs.map { if (it.id == tabId) it.copy(title = "Empty conversation") else it }
            }
            vm.start()
        }
    }

    fun resumeSessionFromHistory(summary: SessionHistoryService.SessionSummary, historyMessages: List<ChatMessage>) {
        val tab = ChatTab(
            title = summary.firstPrompt?.take(40) ?: "Resumed session",
        )
        val vm = createViewModel()
        vm.importHistory(historyMessages)
        _viewModels.update { it + (tab.id to vm) }
        _tabs.update { it + tab }
        _activeTabId.value = tab.id
        observeTabTitles(tab.id, vm)
        scope.launch { vm.start(resumeSessionId = summary.sessionId) }
    }

    fun dispose() {
        // 全タイトル監視 Job を cancel
        titleObserverJobs.values.forEach { it.cancel() }
        titleObserverJobs.clear()
        // 全 ViewModel を dispose
        _viewModels.value.values.forEach { it.dispose() }
        _viewModels.value = emptyMap()
    }

    // --- Private ---

    private fun createViewModel(): ChatViewModel {
        return ChatViewModel(
            projectBasePath = projectBasePath,
            claudeCodePath = claudeCodePath,
            initialModel = settingsService.model,
            initialPermissionMode = settingsService.permissionMode,
        )
    }

    /**
     * タブタイトルの自動更新。
     * ChatViewModel の uiState.messages を監視し、最初のユーザーメッセージをタイトルに設定する。
     * Job は titleObserverJobs に保持し、removeTab/dispose で cancel する。
     */
    private fun observeTabTitles(tabId: String, vm: ChatViewModel) {
        val job = scope.launch {
            vm.uiState
                .map { state -> state.messages.filterIsInstance<ChatMessage.User>().firstOrNull()?.text }
                .distinctUntilChanged()
                .collect { firstUserMessage ->
                    if (firstUserMessage != null) {
                        val title = firstUserMessage.take(40).let {
                            if (firstUserMessage.length > 40) "$it..." else it
                        }
                        _tabs.update { tabs ->
                            tabs.map { if (it.id == tabId) it.copy(title = title) else it }
                        }
                    }
                }
        }
        titleObserverJobs[tabId] = job
    }
}
