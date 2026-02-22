package me.matsumo.agentguiplugin.viewmodel

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.matsumo.agentguiplugin.viewmodel.mapper.toUiBlock
import me.matsumo.agentguiplugin.viewmodel.permission.PermissionHandler
import me.matsumo.claude.agent.ClaudeSDKClient
import me.matsumo.claude.agent.createSession
import me.matsumo.claude.agent.types.AssistantMessage
import me.matsumo.claude.agent.types.ResultMessage
import me.matsumo.claude.agent.types.StreamEvent
import me.matsumo.claude.agent.types.SystemMessage
import me.matsumo.claude.agent.types.UserMessage
import java.util.*

class ChatViewModel(
    private val projectBasePath: String,
    private val claudeCodePath: String?,
    private val scope: CoroutineScope,
) {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val permissionHandler = PermissionHandler(
        currentState = { _uiState.value },
        updateState = { transform -> _uiState.update(transform) },
    )

    private var client: ClaudeSDKClient? = null
    private var activeTurnJob: Job? = null
    private var activeTurnId = 0L

    // toolUseId → toolName の O(1) インデックス
    private val toolUseNameIndex = mutableMapOf<String, String>()

    fun initialize() {
        scope.launch {
            try {
                _uiState.update { it.copy(sessionState = SessionState.Connecting) }

                createSession {
                    cwd = projectBasePath
                    cliPath = claudeCodePath
                    includePartialMessages = true
                    canUseTool { toolName, input, _ ->
                        permissionHandler.request(toolName, input)
                    }
                }.also {
                    it.connect()
                    client = it
                }

                _uiState.update { it.copy(sessionState = SessionState.Ready) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        sessionState = SessionState.Error,
                        errorMessage = e.message,
                    )
                }
            }
        }
    }

    fun sendMessage(text: String) {
        val session = client ?: return

        if (text.isEmpty()) return

        val userMsg = ChatMessage.User(
            id = UUID.randomUUID().toString(),
            text = text,
        )

        _uiState.update {
            it.copy(
                messages = it.messages + userMsg,
                sessionState = SessionState.Processing,
            )
        }

        activeTurnJob?.cancel()
        val turnId = ++activeTurnId
        toolUseNameIndex.clear()

        activeTurnJob = scope.launch {
            try {
                session.send(text)
                session.receiveResponse().collect { msg ->
                    if (turnId != activeTurnId) return@collect

                    when (msg) {
                        is SystemMessage -> {
                            if (msg.isInit) {
                                _uiState.update { it.copy(sessionId = msg.sessionId) }
                            }
                        }

                        is StreamEvent -> { /* wait for completion */ }
                        is AssistantMessage -> handleAssistantMessage(msg)
                        is ResultMessage -> handleResultMessage(msg)
                        is UserMessage -> { /* tool results - ignore */ }
                    }
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
    }

    private fun handleAssistantMessage(msg: AssistantMessage) {
        val pid = msg.parentToolUseId
        if (pid != null) {
            // サブエージェントのメッセージ → SubAgentTask に追加
            ensureSubAgentTask(pid)
            val blocks = msg.content.map { it.toUiBlock() }
            val assistantMsg = ChatMessage.Assistant(
                id = UUID.randomUUID().toString(),
                blocks = blocks,
            )
            updateSubAgentTask(pid) { task ->
                task.copy(messages = task.messages + assistantMsg)
            }
        } else {
            // メインのアシスタントメッセージ → messages に直接追加
            val blocks = msg.content.map { block ->
                val ui = block.toUiBlock()
                if (ui is UiContentBlock.ToolUse && ui.toolUseId != null && ui.toolName.isNotBlank()) {
                    toolUseNameIndex[ui.toolUseId] = ui.toolName
                }
                ui
            }
            val assistantMsg = ChatMessage.Assistant(
                id = UUID.randomUUID().toString(),
                blocks = blocks,
            )
            _uiState.update { it.copy(messages = it.messages + assistantMsg) }
        }
    }

    private fun handleResultMessage(msg: ResultMessage) {
        toolUseNameIndex.clear()

        _uiState.update {
            it.copy(
                sessionState = SessionState.WaitingForInput,
                totalCostUsd = msg.totalCostUsd ?: it.totalCostUsd,
                errorMessage = if (msg.isError) "Turn ended with error: ${msg.subtype}" else null,
            )
        }
    }

    // region SubAgentTask helpers — ToolUse ブロック内に埋め込む

    /**
     * parentToolUseId に対応する SubAgentTask が既に ToolUse ブロックに存在しなければ作成して埋め込む。
     */
    private fun ensureSubAgentTask(parentToolUseId: String) {
        // 既に存在するか確認
        if (findSubAgentTask(parentToolUseId) != null) return

        val toolName = toolUseNameIndex[parentToolUseId]
        val task = SubAgentTask(
            id = parentToolUseId,
            spawnedByToolName = toolName,
        )
        embedSubAgentTaskInToolUse(parentToolUseId, task)
    }

    /**
     * messages 内の全 Assistant ブロックを走査し、対応する ToolUse を見つけて SubAgentTask を埋め込む。
     */
    private fun embedSubAgentTaskInToolUse(parentToolUseId: String, task: SubAgentTask) {
        _uiState.update { state ->
            state.copy(
                messages = state.messages.map { msg ->
                    if (msg is ChatMessage.Assistant) {
                        val updated = msg.blocks.map { block ->
                            if (block is UiContentBlock.ToolUse &&
                                block.toolUseId == parentToolUseId &&
                                block.subAgentTask == null
                            ) {
                                block.copy(subAgentTask = task)
                            } else {
                                block
                            }
                        }
                        if (updated != msg.blocks) msg.copy(blocks = updated) else msg
                    } else {
                        msg
                    }
                },
            )
        }
    }

    /**
     * parentToolUseId に一致する SubAgentTask を ToolUse ブロック内から見つける。
     */
    private fun findSubAgentTask(parentToolUseId: String): SubAgentTask? {
        for (msg in _uiState.value.messages) {
            if (msg is ChatMessage.Assistant) {
                for (block in msg.blocks) {
                    if (block is UiContentBlock.ToolUse &&
                        block.toolUseId == parentToolUseId &&
                        block.subAgentTask != null
                    ) {
                        return block.subAgentTask
                    }
                }
            }
        }
        return null
    }

    /**
     * parentToolUseId に対応する SubAgentTask を transform で更新する。
     * ToolUse ブロック内に埋め込まれた SubAgentTask を直接更新する。
     */
    private fun updateSubAgentTask(
        parentToolUseId: String,
        transform: (SubAgentTask) -> SubAgentTask,
    ) {
        _uiState.update { state ->
            state.copy(
                messages = state.messages.map { msg ->
                    if (msg is ChatMessage.Assistant) {
                        val updated = msg.blocks.map { block ->
                            if (block is UiContentBlock.ToolUse &&
                                block.toolUseId == parentToolUseId &&
                                block.subAgentTask != null
                            ) {
                                block.copy(subAgentTask = transform(block.subAgentTask))
                            } else {
                                block
                            }
                        }
                        if (updated != msg.blocks) msg.copy(blocks = updated) else msg
                    } else {
                        msg
                    }
                },
            )
        }
    }

    // endregion

    fun respondPermission(allow: Boolean, denyMessage: String = "Denied by user") {
        permissionHandler.respondPermission(allow, denyMessage)
    }

    fun respondQuestion(answers: Map<String, String>) {
        permissionHandler.respondQuestion(answers)
    }

    fun abortSession() {
        activeTurnJob?.cancel()
        activeTurnJob = null
        toolUseNameIndex.clear()
        scope.launch { client?.interrupt() }
        _uiState.update {
            it.copy(sessionState = SessionState.WaitingForInput)
        }
    }

    fun reconnect() {
        activeTurnJob?.cancel()
        activeTurnJob = null
        toolUseNameIndex.clear()
        client?.close()
        client = null
        _uiState.update { ChatUiState(sessionState = SessionState.Connecting) }
        initialize()
    }

    fun dispose() {
        activeTurnJob?.cancel()
        client?.close()
        client = null
    }
}
