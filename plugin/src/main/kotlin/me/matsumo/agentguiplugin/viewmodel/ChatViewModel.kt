package me.matsumo.agentguiplugin.viewmodel

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.matsumo.agentguiplugin.model.AttachedFile
import me.matsumo.agentguiplugin.viewmodel.mapper.toUiBlock
import me.matsumo.agentguiplugin.viewmodel.permission.PermissionHandler
import me.matsumo.agentguiplugin.viewmodel.preflight.PreflightChecker
import me.matsumo.agentguiplugin.viewmodel.preflight.PreflightResult
import me.matsumo.claude.agent.ClaudeSDKClient
import me.matsumo.claude.agent.createSession
import me.matsumo.claude.agent.resumeSession
import me.matsumo.claude.agent.types.AssistantMessage
import me.matsumo.claude.agent.types.HookEvent
import me.matsumo.claude.agent.types.HookOutput
import me.matsumo.claude.agent.types.Model
import me.matsumo.claude.agent.types.PermissionMode
import me.matsumo.claude.agent.types.ResultMessage
import me.matsumo.claude.agent.types.SessionOptionsBuilder
import me.matsumo.claude.agent.types.SubagentStartHookInput
import me.matsumo.claude.agent.types.SubagentStopHookInput
import me.matsumo.claude.agent.types.SystemMessage
import java.util.*

class ChatViewModel(
    private val projectBasePath: String,
    private val claudeCodePath: String?,
    private val initialModel: Model,
    private val initialPermissionMode: PermissionMode,
) {
    /** VM が所有する CoroutineScope。dispose() で cancel される。 */
    private val vmScope = CoroutineScope(SupervisorJob())

    private val _uiState = MutableStateFlow(
        ChatUiState(
            model = initialModel,
            permissionMode = initialPermissionMode,
        ),
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    // --- Delegates ---
    private val preflightChecker = PreflightChecker()
    private val usageTracker = UsageTracker()
    private val authFlowHandler = AuthFlowHandler(vmScope)
    private val subAgentCoordinator = SubAgentCoordinator(vmScope)
    private val turnEngine = TurnEngine(vmScope)

    private val permissionHandler = PermissionHandler(
        currentState = { _uiState.value },
        updateState = { transform -> _uiState.update(transform) },
    )

    val branchSessionManager = BranchSessionManager(
        applyCommonConfig = { model, permissionMode -> applyCommonConfig(model, permissionMode) },
    )

    private val startMutex = Mutex()
    private var startJob: Job? = null

    @Volatile
    private var disposed = false

    @Volatile
    private var client: ClaudeSDKClient? = null
    private var branchSwitchJob: Job? = null

    init {
        // AuthFlowHandler の認証完了コールバック
        authFlowHandler.onAuthComplete = {
            vmScope.launch {
                _uiState.update {
                    it.copy(
                        sessionState = SessionState.Disconnected,
                        authOutputLines = emptyList(),
                    )
                }
                start()
            }
        }

        // SubAgentCoordinator の tasks を ChatUiState に同期
        vmScope.launch {
            subAgentCoordinator.tasks.collect { tasks ->
                _uiState.update { it.copy(subAgentTasks = tasks) }
            }
        }

        // UsageTracker の usage を ChatUiState に同期（onResult 時のみ更新されるので頻度は低い）
        vmScope.launch {
            usageTracker.usage.collect { usage ->
                _uiState.update {
                    it.copy(
                        contextUsage = usage.contextUsage,
                        totalInputTokens = usage.totalInputTokens,
                        totalCostUsd = usage.totalCostUsd,
                    )
                }
            }
        }

        // AuthFlowHandler の state を ChatUiState に同期
        vmScope.launch {
            authFlowHandler.state.collect { authState ->
                _uiState.update {
                    it.copy(authOutputLines = authState.outputLines)
                }
            }
        }
    }

    /**
     * セッションを開始する。resumeSessionId が指定された場合はセッション再開。
     * Mutex により多重起動を防止。dispose 済みの場合は何もしない。
     */
    suspend fun start(resumeSessionId: String? = null) {
        startMutex.withLock {
            if (disposed) return
            if (startJob?.isActive == true) return
            if (_uiState.value.sessionState != SessionState.Disconnected) return

            _uiState.update { it.copy(sessionState = SessionState.Connecting) }

            startJob = vmScope.launch {
                val result = preflightChecker.check(claudeCodePath)
                when (result) {
                    is PreflightResult.Ready -> connectSession(resumeSessionId)
                    is PreflightResult.AuthRequired -> {
                        authFlowHandler.startAuth(
                            process = result.process,
                            stdout = result.stdout,
                            stdin = result.stdin,
                            initialOutput = result.initialOutput,
                        )
                        _uiState.update {
                            it.copy(sessionState = SessionState.AuthRequired)
                        }
                    }
                    is PreflightResult.Error -> {
                        _uiState.update {
                            it.copy(
                                sessionState = SessionState.Error,
                                errorMessage = result.message,
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * 現在のセッションをクリアし、初期状態に戻す。
     */
    suspend fun clear() {
        startMutex.withLock {
            startJob?.cancel()
            startJob = null
            turnEngine.cancel()
            branchSwitchJob?.cancel()
            branchSwitchJob = null
            authFlowHandler.cleanup()
            subAgentCoordinator.reset()
            usageTracker.reset()
            permissionHandler.cancelPending()
            branchSessionManager.closeAll()
            client?.close()
            client = null
            _uiState.value = ChatUiState(
                model = _uiState.value.model,
                permissionMode = _uiState.value.permissionMode,
            )
        }
    }

    /**
     * VM を完全に破棄する。再利用不可。
     */
    fun dispose() {
        disposed = true
        vmScope.cancel()
        authFlowHandler.cleanup()
        permissionHandler.cancelPending()
        branchSessionManager.closeAll()
        client?.close()
        client = null
    }

    /**
     * 履歴メッセージを UI に投入する（resume 時の過去メッセージ表示用）。
     */
    fun importHistory(messages: List<ChatMessage>, branchSessionId: String? = null) {
        val tree = buildConversationTreeFromFlatList(messages, branchSessionId)
        _uiState.update {
            it.copy(
                conversationTree = tree,
                conversationCursor = ConversationCursor(activeLeafPath = tree.getActiveLeafPath()),
            )
        }
    }

    private suspend fun connectSession(resumeSessionId: String?) {
        if (disposed) return

        try {
            val state = _uiState.value
            val localClient = if (resumeSessionId != null) {
                resumeSession(resumeSessionId) {
                    applyCommonConfig(state.model, state.permissionMode)
                    forkSession = true
                }
            } else {
                createSession {
                    applyCommonConfig(state.model, state.permissionMode)
                }
            }

            if (disposed || !currentCoroutineContext().isActive) {
                localClient.close()
                return
            }

            localClient.connect()
            client = localClient

            _uiState.update {
                it.copy(
                    sessionState = SessionState.Ready,
                    sessionId = localClient.sessionId,
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    sessionState = SessionState.Error,
                    errorMessage = e.message,
                )
            }
        }
    }

    /**
     * 全セッションで共通の設定を適用。
     */
    private fun SessionOptionsBuilder.applyCommonConfig(
        model: Model,
        permissionMode: PermissionMode,
    ) {
        this.model = model
        this.permissionMode = permissionMode
        this.cwd = projectBasePath
        this.cliPath = claudeCodePath
        this.includePartialMessages = true

        if (claudeCodePath != null) {
            env { put("CLAUDE_AGENT_SDK_SKIP_VERSION_CHECK", "1") }
        }

        canUseTool { toolName, input, _ ->
            permissionHandler.request(toolName, input)
        }

        hooks {
            on(HookEvent.SUBAGENT_START) { input, toolUseId, _ ->
                val si = input as SubagentStartHookInput
                subAgentCoordinator.onSubAgentStart(
                    agentId = si.agentId,
                    transcriptPath = si.transcriptPath,
                    hookToolUseId = toolUseId,
                    sessionId = _uiState.value.sessionId,
                )
                HookOutput.proceed()
            }
            on(HookEvent.SUBAGENT_STOP) { input, _, _ ->
                val si = input as SubagentStopHookInput
                subAgentCoordinator.onSubAgentStop(si.agentId)
                HookOutput.proceed()
            }
        }
    }

    fun attachFile(file: AttachedFile) {
        _uiState.update { state ->
            if (state.attachedFiles.any { it.id == file.id }) state
            else state.copy(attachedFiles = state.attachedFiles + file)
        }
    }

    fun detachFile(file: AttachedFile) {
        _uiState.update { state ->
            state.copy(attachedFiles = state.attachedFiles.filter { it.id != file.id })
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        if (disposed) return

        val session = client ?: run {
            _uiState.update {
                it.copy(
                    sessionState = SessionState.Error,
                    errorMessage = "No active session for this branch. Please start a new conversation.",
                )
            }
            return
        }
        val files = _uiState.value.attachedFiles

        val editGroupId = UUID.randomUUID().toString()
        val userMsg = ChatMessage.User(
            id = UUID.randomUUID().toString(),
            editGroupId = editGroupId,
            text = text,
            attachedFiles = files,
        )

        _uiState.update { state ->
            val (newTree, newPath) = state.conversationTree.appendUserMessage(
                userMessage = userMsg,
                branchSessionId = state.sessionId,
            )
            state.copy(
                conversationTree = newTree,
                conversationCursor = ConversationCursor(activeLeafPath = newPath),
                attachedFiles = emptyList(),
                sessionState = SessionState.Processing,
                errorMessage = null,
            )
        }

        turnEngine.dispatch(
            client = session,
            text = text,
            files = files,
            onEvent = { event -> handleTurnEvent(event) },
            onError = { e ->
                _uiState.update {
                    it.copy(
                        sessionState = SessionState.Error,
                        errorMessage = e.message,
                    )
                }
            },
        )
    }

    /**
     * ユーザーメッセージを編集し、新しいブランチで応答を取得する。
     */
    fun editMessage(editGroupId: String, newText: String) {
        if (newText.isBlank()) return
        if (disposed) return
        val state = _uiState.value
        if (!canEditOrNavigate(state)) return

        val tree = state.conversationTree

        val currentSlot = tree.findSlot(editGroupId) ?: return
        val currentTimeline = currentSlot.timelines[currentSlot.activeTimelineIndex]
        if (currentTimeline.userMessage.text == newText) return

        turnEngine.cancel()

        // 旧セッションの sub-agent tailer を停止
        vmScope.launch { subAgentCoordinator.stopAll() }

        _uiState.update { it.copy(sessionState = SessionState.Processing, errorMessage = null) }

        vmScope.launch {
            try {
                val messagesBeforeEdit = tree.getMessagesBeforeSlot(editGroupId)
                val originalAttachedFiles = currentTimeline.userMessage.attachedFiles

                // 旧 client を保持して後で close する (§3.2 client lifecycle leak fix)
                val oldClient = client

                val newClient = branchSessionManager.createEditBranchSession(
                    messagesBeforeEdit = messagesBeforeEdit,
                    originalAttachedFiles = originalAttachedFiles,
                    model = state.model,
                    permissionMode = state.permissionMode,
                )

                val newUserMessage = ChatMessage.User(
                    id = UUID.randomUUID().toString(),
                    editGroupId = editGroupId,
                    text = newText,
                    attachedFiles = originalAttachedFiles,
                )

                val newTree = tree.editMessage(
                    editGroupId = editGroupId,
                    newUserMessage = newUserMessage,
                    branchSessionId = newClient.sessionId,
                )
                val newPath = newTree.getActiveLeafPath()

                _uiState.update {
                    it.copy(
                        conversationTree = newTree,
                        conversationCursor = ConversationCursor(activeLeafPath = newPath),
                    )
                }

                // activeClient を新ブランチに切り替え
                client = newClient
                _uiState.update { it.copy(sessionId = newClient.sessionId) }

                // 初期セッション client が BranchSessionManager 管理外の場合は close (§3.2)
                if (oldClient != null && oldClient !== newClient) {
                    val oldSessionId = oldClient.sessionId
                    if (oldSessionId == null || !branchSessionManager.hasSession(oldSessionId)) {
                        oldClient.close()
                    }
                }

                // TurnEngine で応答収集
                turnEngine.dispatch(
                    client = newClient,
                    text = newText,
                    files = originalAttachedFiles,
                    onEvent = { event -> handleTurnEvent(event) },
                    onError = { e ->
                        _uiState.update {
                            it.copy(
                                sessionState = SessionState.WaitingForInput,
                                errorMessage = "Failed to edit message: ${e.message}",
                            )
                        }
                    },
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        sessionState = SessionState.WaitingForInput,
                        errorMessage = "Failed to edit message: ${e.message}",
                    )
                }
            }
        }
    }

    /**
     * TurnEngine からのイベントを処理する統一ハンドラー。
     */
    private fun handleTurnEvent(event: TurnEngine.TurnEvent) {
        when (event) {
            is TurnEngine.TurnEvent.System -> handleSystemMessage(event.message)
            is TurnEngine.TurnEvent.Stream -> usageTracker.processStreamEvent(event.event)
            is TurnEngine.TurnEvent.Assistant -> handleAssistantMessage(event.message)
            is TurnEngine.TurnEvent.Result -> handleResultMessage(event.message)
        }
    }

    private fun handleSystemMessage(message: SystemMessage) {
        if (message.isInit) {
            _uiState.update { it.copy(sessionId = message.sessionId) }
        }
    }

    private fun handleAssistantMessage(message: AssistantMessage) {
        val pid = message.parentToolUseId
        if (pid != null) {
            // Sub-agent message: delegate to coordinator
            vmScope.launch { subAgentCoordinator.resolveParentToolUseId(pid) }
            message.parentToolName?.let { toolName ->
                subAgentCoordinator.updateSpawnedByToolName(pid, toolName)
            }
        } else {
            val blocks = message.content.map { it.toUiBlock() }
            val messageId = message.uuid ?: UUID.randomUUID().toString()
            val assistantMsg = ChatMessage.Assistant(
                id = messageId,
                blocks = blocks,
            )

            _uiState.update { state ->
                val cursor = state.conversationCursor
                val targetPath = cursor.activeLeafPath
                val isSameStreaming = cursor.activeStreamingMessageId == messageId

                val newTree = if (isSameStreaming) {
                    state.conversationTree.updateLastResponse(targetPath) { last ->
                        if (last is ChatMessage.Assistant && last.id == messageId) assistantMsg else last
                    }
                } else {
                    state.conversationTree.appendResponse(targetPath, assistantMsg)
                }

                state.copy(
                    conversationTree = newTree,
                    conversationCursor = cursor.copy(activeStreamingMessageId = messageId),
                )
            }
        }
    }

    private fun handleResultMessage(message: ResultMessage) {
        usageTracker.onResult(message.totalCostUsd)
        _uiState.update {
            it.copy(
                sessionState = SessionState.WaitingForInput,
                conversationCursor = it.conversationCursor.copy(activeStreamingMessageId = null),
                errorMessage = if (message.isError) "Turn ended with error: ${message.subtype}" else null,
            )
        }
    }

    /**
     * 編集/ナビゲーションが可能な状態かチェック。
     */
    private fun canEditOrNavigate(state: ChatUiState): Boolean {
        return state.sessionState != SessionState.Processing &&
            state.sessionState != SessionState.Connecting &&
            state.pendingPermission == null &&
            state.pendingQuestion == null
    }

    /**
     * 編集バージョンをナビゲーションする（左右矢印）。
     */
    fun navigateEditVersion(editGroupId: String, direction: Int) {
        if (disposed) return
        val state = _uiState.value
        if (!canEditOrNavigate(state)) return

        val newTree = state.conversationTree.navigateVersion(editGroupId, direction)
        val newPath = newTree.getActiveLeafPath()
        val newSessionId = newTree.getActiveLeafSessionId()
        val currentSessionId = state.sessionId
        val needsSessionSwitch = newSessionId != null && newSessionId != currentSessionId
        val isNullSession = newSessionId == null

        _uiState.update {
            it.copy(
                conversationTree = newTree,
                conversationCursor = ConversationCursor(activeLeafPath = newPath),
                sessionState = if (needsSessionSwitch) SessionState.Connecting
                    else it.sessionState,
                sessionId = newSessionId,
                errorMessage = null,
            )
        }

        if (isNullSession) {
            client = null
            return
        }

        if (needsSessionSwitch) {
            client = null
            branchSwitchJob?.cancel()
            branchSwitchJob = vmScope.launch {
                try {
                    val newClient = branchSessionManager.getOrResumeSession(
                        branchSessionId = newSessionId,
                        model = state.model,
                        permissionMode = state.permissionMode,
                    )
                    if (_uiState.value.sessionId != newSessionId) {
                        newClient.close()
                        return@launch
                    }
                    client = newClient
                    _uiState.update {
                        it.copy(
                            sessionId = newSessionId,
                            sessionState = SessionState.WaitingForInput,
                        )
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    client = null
                    _uiState.update {
                        it.copy(
                            sessionState = SessionState.Error,
                            errorMessage = "Failed to switch branch session: ${e.message}",
                        )
                    }
                }
            }
        }
    }

    fun abortSession() {
        turnEngine.invalidateCurrentTurn()
        vmScope.launch { subAgentCoordinator.stopAll() }
        vmScope.launch { client?.interrupt() }

        val interruptedMsg = ChatMessage.Interrupted(
            id = UUID.randomUUID().toString(),
        )
        _uiState.update { state ->
            val path = state.conversationCursor.activeLeafPath
            state.copy(
                sessionState = SessionState.WaitingForInput,
                conversationTree = state.conversationTree.appendResponse(path, interruptedMsg),
                conversationCursor = state.conversationCursor.copy(activeStreamingMessageId = null),
            )
        }
    }

    fun reconnect() {
        vmScope.launch { clear(); start() }
    }

    // --- Public API ---

    fun changeModel(model: Model) {
        vmScope.launch { client?.setModel(model.modelId) }
        _uiState.update { it.copy(model = model) }
    }

    fun changePermissionMode(mode: PermissionMode) {
        vmScope.launch { client?.setPermissionMode(mode) }
        _uiState.update { it.copy(permissionMode = mode) }
    }

    fun respondPermission(allow: Boolean, denyMessage: String = "Denied by user") {
        permissionHandler.respondPermission(allow, denyMessage)
    }

    fun respondQuestion(answers: Map<String, String>) {
        permissionHandler.respondQuestion(answers)
    }

    fun cancelActiveRequest() {
        permissionHandler.cancelActiveRequest()
    }

    // --- Auth delegation ---

    fun sendAuthInput(text: String) {
        authFlowHandler.sendInput(text)
    }

    fun confirmAuthComplete() {
        authFlowHandler.confirmComplete()
    }
}
