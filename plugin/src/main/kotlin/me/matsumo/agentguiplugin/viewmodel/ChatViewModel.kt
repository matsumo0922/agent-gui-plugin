package me.matsumo.agentguiplugin.viewmodel

import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.matsumo.agentguiplugin.model.AttachedFile
import me.matsumo.agentguiplugin.viewmodel.mapper.extractToolResults
import me.matsumo.agentguiplugin.viewmodel.mapper.toUiBlock
import me.matsumo.agentguiplugin.viewmodel.permission.PermissionHandler
import me.matsumo.agentguiplugin.viewmodel.permission.ToolNames
import me.matsumo.agentguiplugin.viewmodel.preflight.PreflightChecker
import me.matsumo.agentguiplugin.viewmodel.preflight.PreflightResult
import me.matsumo.agentguiplugin.viewmodel.session.SessionCoordinator
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
import me.matsumo.claude.agent.types.ToolUseBlock
import me.matsumo.claude.agent.types.UserMessage
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

    private val sessionCoordinator = SessionCoordinator(
        applyCommonConfig = { model, permissionMode -> applyCommonConfig(model, permissionMode) },
    )

    val branchSessionManager get() = sessionCoordinator.branchSessionManager

    private val startMutex = Mutex()
    private var startJob: Job? = null

    private var branchSwitchJob: Job? = null

    // --- Reducer dispatch ---

    private fun dispatch(action: StateAction) {
        _uiState.update { reduce(it, action) }
    }

    /** ラッパー CLI のために解決したシェル環境（遅延初期化） */
    private var resolvedShellEnv: Map<String, String>? = null

    init {
        // AuthFlowHandler の認証完了コールバック
        authFlowHandler.onAuthComplete = {
            vmScope.launch {
                dispatch(StateAction.SessionDisconnected)
                start()
            }
        }

        // AuthFlowHandler の認証エラーコールバック（プロセス異常終了時）
        authFlowHandler.onAuthError = { errorMessage ->
            vmScope.launch {
                dispatch(StateAction.SessionError(errorMessage))
            }
        }

        // SubAgentCoordinator の tasks を ChatUiState に同期
        vmScope.launch {
            subAgentCoordinator.tasks.collectLatest { tasks ->
                dispatch(StateAction.SubAgentTasksUpdated(tasks))
            }
        }

        // UsageTracker の usage を ChatUiState に同期（onResult 時のみ更新されるので頻度は低い）
        vmScope.launch {
            usageTracker.usage.collectLatest { usage ->
                dispatch(
                    StateAction.UsageUpdated(
                        UsageInfo(
                            contextUsage = usage.contextUsage,
                            totalInputTokens = usage.totalInputTokens,
                            totalCostUsd = usage.totalCostUsd,
                        ),
                    ),
                )
            }
        }

        // AuthFlowHandler の state を ChatUiState に同期
        vmScope.launch {
            authFlowHandler.state.collectLatest { authState ->
                dispatch(StateAction.AuthOutputUpdated(authState.outputLines))
            }
        }
    }

    /**
     * セッションを開始する。resumeSessionId が指定された場合はセッション再開。
     * Mutex により多重起動を防止。dispose 済みの場合は何もしない。
     */
    suspend fun start(resumeSessionId: String? = null) {
        startMutex.withLock {
            if (!vmScope.isActive) return
            if (startJob?.isActive == true) return
            if (_uiState.value.sessionState != SessionState.Disconnected) return

            dispatch(StateAction.StartConnecting)

            startJob = vmScope.launch {
                // ラッパー CLI が内部コマンドを解決できるようシェル環境を取得
                if (claudeCodePath != null && resolvedShellEnv == null) {
                    resolvedShellEnv = preflightChecker.resolveShellPath()
                }

                when (val result = preflightChecker.check(claudeCodePath, resolvedShellEnv ?: emptyMap())) {
                    is PreflightResult.Ready -> connectSession(resumeSessionId)
                    is PreflightResult.AuthRequired -> {
                        authFlowHandler.startAuth(
                            process = result.process,
                            stdout = result.stdout,
                            stdin = result.stdin,
                            initialOutput = result.initialOutput,
                        )
                        dispatch(StateAction.SessionAuthRequired)
                    }
                    is PreflightResult.Error -> {
                        dispatch(StateAction.SessionError(result.message))
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
            sessionCoordinator.closeAll()
            dispatch(StateAction.Reset(model = _uiState.value.model, permissionMode = _uiState.value.permissionMode))
        }
    }

    /**
     * VM を完全に破棄する。再利用不可。
     */
    fun dispose() {
        vmScope.cancel()
        authFlowHandler.cleanup()
        permissionHandler.cancelPending()
        sessionCoordinator.closeAll()
    }

    /**
     * 履歴メッセージを UI に投入する（resume 時の過去メッセージ表示用）。
     */
    fun importHistory(
        messages: List<ChatMessage>,
        branchSessionId: String? = null,
        toolResults: Map<String, ToolResultInfo> = emptyMap(),
    ) {
        val tree = buildConversationTreeFromFlatList(messages, branchSessionId)
        dispatch(
            StateAction.HistoryImported(
                tree = tree,
                cursor = ConversationCursor(activeLeafPath = tree.getActiveLeafPath()),
                toolResults = toolResults,
            ),
        )
    }

    private suspend fun connectSession(resumeSessionId: String?) {
        if (!vmScope.isActive) return

        try {
            val state = _uiState.value
            val localClient = sessionCoordinator.connect(
                model = state.model,
                permissionMode = state.permissionMode,
                resumeSessionId = resumeSessionId,
            )

            if (!vmScope.isActive) {
                localClient.close()
                return
            }

            dispatch(StateAction.SessionReady(sessionId = localClient.sessionId))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            dispatch(StateAction.SessionError(e.message))
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
            env {
                put("CLAUDE_AGENT_SDK_SKIP_VERSION_CHECK", "1")
                // ラッパー CLI が内部コマンドを解決できるようシェルの PATH を注入
                resolvedShellEnv?.get("PATH")?.let { put("PATH", it) }
            }
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
        dispatch(StateAction.FileAttached(file))
    }

    fun detachFile(file: AttachedFile) {
        dispatch(StateAction.FileDetached(file))
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        if (!vmScope.isActive) return

        val session = sessionCoordinator.activeClient ?: run {
            dispatch(StateAction.SessionError("No active session for this branch. Please start a new conversation."))
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

        // appendUserMessage は現在の state に依存するため、_uiState.update 内で計算する
        _uiState.update { state ->
            val (newTree, newPath) = state.conversationTree.appendUserMessage(
                userMessage = userMsg,
                branchSessionId = state.sessionId,
            )
            reduce(
                state,
                StateAction.TurnStarted(newTree = newTree, newPath = newPath),
            )
        }

        turnEngine.dispatch(
            client = session,
            text = text,
            files = files,
            onEvent = { event -> handleTurnEvent(event) },
            onError = { e ->
                dispatch(StateAction.TurnError(e.message))
            },
        )
    }

    /**
     * ユーザーメッセージを編集し、新しいブランチで応答を取得する。
     */
    fun editMessage(editGroupId: String, newText: String) {
        if (newText.isBlank()) return
        if (!vmScope.isActive) return
        val state = _uiState.value
        if (!canEditOrNavigate(state)) return

        val tree = state.conversationTree

        val currentSlot = tree.findSlot(editGroupId) ?: return
        val currentTimeline = currentSlot.timelines[currentSlot.activeTimelineIndex]
        if (currentTimeline.userMessage.text == newText) return

        turnEngine.cancel()

        // 旧セッションの sub-agent tailer を停止
        vmScope.launch { subAgentCoordinator.stopAll() }

        dispatch(StateAction.TurnError(message = null, toState = SessionState.Processing))

        vmScope.launch {
            try {
                val messagesBeforeEdit = tree.getMessagesBeforeSlot(editGroupId)
                val originalAttachedFiles = currentTimeline.userMessage.attachedFiles

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

                dispatch(StateAction.EditBranchCreated(newTree = newTree, newPath = newPath))

                // activeClient を新ブランチに切り替え（旧 client の安全な close を含む）
                sessionCoordinator.switchClient(newClient)
                dispatch(StateAction.EditSessionSynced(sessionId = newClient.sessionId))

                // TurnEngine で応答収集
                turnEngine.dispatch(
                    client = newClient,
                    text = newText,
                    files = originalAttachedFiles,
                    onEvent = { event -> handleTurnEvent(event) },
                    onError = { e ->
                        dispatch(
                            StateAction.TurnError(
                                message = "Failed to edit message: ${e.message}",
                                toState = SessionState.WaitingForInput,
                            ),
                        )
                    },
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                dispatch(
                    StateAction.TurnError(
                        message = "Failed to edit message: ${e.message}",
                        toState = SessionState.WaitingForInput,
                    ),
                )
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
            is TurnEngine.TurnEvent.User -> handleUserMessage(event.message)
            is TurnEngine.TurnEvent.Result -> handleResultMessage(event.message)
        }
    }

    private fun handleSystemMessage(message: SystemMessage) {
        if (message.isInit) {
            dispatch(StateAction.SessionIdUpdated(message.sessionId))
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
            val blocks = message.content.map { it.toUiBlock() }.toImmutableList()
            val messageId = message.uuid ?: UUID.randomUUID().toString()
            val assistantMsg = ChatMessage.Assistant(
                id = messageId,
                blocks = blocks,
            )

            detectPlanModeChange(message)
            dispatch(StateAction.AssistantMessageReceived(assistantMsg))
        }
    }

    private fun handleUserMessage(message: UserMessage) {
        if (message.parentToolUseId != null) return // sub-agent の tool result は無視
        val results = extractToolResults(message)
        if (results.isNotEmpty()) {
            dispatch(StateAction.ToolResultReceived(results))
        }
    }

    /**
     * アシスタントメッセージ内の EnterPlanMode ツール使用を検知し、
     * UI のパーミッションモード表示を Plan に切り替える。
     *
     * ExitPlanMode はここでは処理しない。
     * ExitPlanMode のモード切替は respondPermission() でユーザーの承認/拒否に応じて行う。
     */
    private fun detectPlanModeChange(message: AssistantMessage) {
        for (block in message.content) {
            if (block !is ToolUseBlock) continue
            if (block.name == ToolNames.ENTER_PLAN_MODE) {
                dispatch(StateAction.PermissionModeChanged(PermissionMode.PLAN))
                return
            }
        }
    }

    private fun handleResultMessage(message: ResultMessage) {
        usageTracker.onResult(message.totalCostUsd)
        dispatch(
            StateAction.TurnCompleted(
                isError = message.isError,
                errorMessage = message.subtype,
            ),
        )
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
        if (!vmScope.isActive) return
        val state = _uiState.value
        if (!canEditOrNavigate(state)) return

        val newTree = state.conversationTree.navigateVersion(editGroupId, direction)
        val newPath = newTree.getActiveLeafPath()
        val newSessionId = newTree.getActiveLeafSessionId()
        val currentSessionId = state.sessionId
        val needsSessionSwitch = newSessionId != null && newSessionId != currentSessionId
        val isNullSession = newSessionId == null

        dispatch(
            StateAction.VersionNavigated(
                newTree = newTree,
                newPath = newPath,
                newSessionId = newSessionId,
                needsSessionSwitch = needsSessionSwitch,
            ),
        )

        if (isNullSession) {
            sessionCoordinator.clearActiveClient()
            return
        }

        if (needsSessionSwitch) {
            sessionCoordinator.clearActiveClient()
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
                    sessionCoordinator.switchClient(newClient)
                    dispatch(StateAction.BranchSwitchCompleted(sessionId = newSessionId))
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    sessionCoordinator.clearActiveClient()
                    dispatch(StateAction.BranchSwitchFailed("Failed to switch branch session: ${e.message}"))
                }
            }
        }
    }

    fun abortSession() {
        turnEngine.invalidateCurrentTurn()
        vmScope.launch { subAgentCoordinator.stopAll() }
        vmScope.launch { sessionCoordinator.activeClient?.interrupt() }

        val interruptedMsg = ChatMessage.Interrupted(
            id = UUID.randomUUID().toString(),
        )
        _uiState.update { state ->
            val path = state.conversationCursor.activeLeafPath
            reduce(
                state,
                StateAction.Aborted(
                    newTree = state.conversationTree.appendResponse(path, interruptedMsg),
                ),
            )
        }
    }

    fun reconnect() {
        vmScope.launch {
            clear()
            start()
        }
    }

    // --- Public API ---

    fun changeModel(model: Model) {
        vmScope.launch { sessionCoordinator.activeClient?.setModel(model.modelId) }
        dispatch(StateAction.ModelChanged(model))
    }

    fun changePermissionMode(mode: PermissionMode) {
        vmScope.launch { sessionCoordinator.activeClient?.setPermissionMode(mode) }
        dispatch(StateAction.PermissionModeChanged(mode))
    }

    fun respondPermission(allow: Boolean, denyMessage: String = "Denied by user") {
        permissionHandler.respondPermission(allow, denyMessage)
    }

    fun respondExitPlan(allow: Boolean, targetMode: PermissionMode?, denyMessage: String = "Denied by user") {
        permissionHandler.respondPermission(allow, denyMessage)
        if (allow && targetMode != null) {
            dispatch(StateAction.PermissionModeChanged(targetMode))
        }
        // 拒否の場合は Plan モード維持（何もしない）
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
