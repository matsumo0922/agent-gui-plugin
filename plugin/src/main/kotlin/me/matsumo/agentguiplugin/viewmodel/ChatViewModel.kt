package me.matsumo.agentguiplugin.viewmodel

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.matsumo.agentguiplugin.viewmodel.mapper.ParsedStreamEvent
import me.matsumo.agentguiplugin.viewmodel.mapper.parseStreamEvent
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
    private var streamingBuffer: StreamingBuffer? = null
    private var lastUiUpdateTime = 0L
    private var activeTurnJob: Job? = null
    private var activeTurnId = 0L
    private var streamingMessageId: String? = null

    // サブエージェント用ストリーミング状態
    private data class SubAgentStreamState(
        val buffer: StreamingBuffer = StreamingBuffer(),
        var streamingMessageId: String? = null,
        var lastUiUpdateTime: Long = 0L,
    )

    private val subAgentStates = mutableMapOf<String, SubAgentStreamState>()
    private val subStateMutex = Mutex()

    // toolUseId → toolName の O(1) インデックス
    private val toolUseNameIndex = mutableMapOf<String, String>()

    fun initialize() {
        scope.launch {
            try {
                _uiState.update { it.copy(sessionState = SessionState.Connecting) }

                error("Sample")

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
                sessionState = SessionState.Streaming,
                isStreaming = true,
            )
        }

        activeTurnJob?.cancel()
        val turnId = ++activeTurnId
        resetStreamingState()

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

                        is StreamEvent -> handleStreamEvent(msg)
                        is AssistantMessage -> handleAssistantMessage(msg)
                        is ResultMessage -> handleResultMessage(msg)
                        is UserMessage -> { /* tool results - ignore */ }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                resetStreamingState()
                _uiState.update {
                    it.copy(
                        sessionState = SessionState.Error,
                        isStreaming = false,
                        errorMessage = e.message,
                    )
                }
            }
        }
    }

    private fun handleStreamEvent(event: StreamEvent) {
        val pid = event.parentToolUseId
        if (pid != null) {
            handleSubAgentStreamEvent(pid, event)
        } else {
            handleMainStreamEvent(event)
        }
    }

    private fun handleMainStreamEvent(event: StreamEvent) {
        val parsed = parseStreamEvent(event.event) ?: return

        when (parsed) {
            is ParsedStreamEvent.MessageStart -> {
                streamingBuffer = StreamingBuffer()
                streamingMessageId = UUID.randomUUID().toString()
                val assistantMsg = ChatMessage.Assistant(
                    id = streamingMessageId!!,
                    blocks = emptyList(),
                    isComplete = false,
                )
                _uiState.update {
                    it.copy(messages = it.messages + assistantMsg)
                }
            }

            is ParsedStreamEvent.ContentBlockStart -> {
                val buffer = streamingBuffer ?: return
                buffer.startBlock(parsed.index, parsed.blockType, parsed.toolName, parsed.toolUseId)
                if (parsed.toolUseId != null && parsed.toolName != null) {
                    toolUseNameIndex[parsed.toolUseId] = parsed.toolName
                }
                updateStreamingMessage()
            }

            is ParsedStreamEvent.ContentBlockDelta -> {
                val buffer = streamingBuffer ?: return
                buffer.appendDelta(parsed.index, parsed.deltaType, parsed.text)
                throttledUpdateStreamingMessage()
            }

            is ParsedStreamEvent.ContentBlockStop -> {
                val buffer = streamingBuffer ?: return
                buffer.stopBlock(parsed.index)
                updateStreamingMessage()
            }

            is ParsedStreamEvent.MessageStop -> {
                // AssistantMessage will follow to finalize
            }
        }
    }

    private fun handleSubAgentStreamEvent(parentToolUseId: String, event: StreamEvent) {
        val parsed = parseStreamEvent(event.event) ?: return

        scope.launch {
            subStateMutex.withLock {
                val state = subAgentStates.getOrPut(parentToolUseId) { SubAgentStreamState() }

                when (parsed) {
                    is ParsedStreamEvent.MessageStart -> {
                        val msgId = UUID.randomUUID().toString()
                        state.streamingMessageId = msgId

                        ensureSubAgentTask(parentToolUseId)

                        val assistantMsg = ChatMessage.Assistant(
                            id = msgId,
                            blocks = emptyList(),
                            isComplete = false,
                        )
                        updateSubAgentTask(parentToolUseId) { task ->
                            task.copy(messages = task.messages + assistantMsg)
                        }
                    }

                    is ParsedStreamEvent.ContentBlockStart -> {
                        state.buffer.startBlock(parsed.index, parsed.blockType, parsed.toolName, parsed.toolUseId)
                        if (parsed.toolUseId != null && parsed.toolName != null) {
                            toolUseNameIndex[parsed.toolUseId] = parsed.toolName
                        }
                        updateSubAgentStreamingMessage(parentToolUseId, state)
                    }

                    is ParsedStreamEvent.ContentBlockDelta -> {
                        state.buffer.appendDelta(parsed.index, parsed.deltaType, parsed.text)
                        val now = System.currentTimeMillis()
                        if (now - state.lastUiUpdateTime >= UI_UPDATE_THROTTLE_MS) {
                            state.lastUiUpdateTime = now
                            updateSubAgentStreamingMessage(parentToolUseId, state)
                        }
                    }

                    is ParsedStreamEvent.ContentBlockStop -> {
                        state.buffer.stopBlock(parsed.index)
                        updateSubAgentStreamingMessage(parentToolUseId, state)
                    }

                    is ParsedStreamEvent.MessageStop -> {
                        // AssistantMessage will follow
                    }
                }
            }
        }
    }

    private fun handleAssistantMessage(msg: AssistantMessage) {
        val pid = msg.parentToolUseId
        if (pid != null) {
            finalizeSubAgentAssistantMessage(pid, msg)
        } else {
            val blocks = msg.content.map { block ->
                val ui = block.toUiBlock()
                if (ui is UiContentBlock.ToolUse && ui.toolUseId != null && ui.toolName.isNotBlank()) {
                    toolUseNameIndex[ui.toolUseId] = ui.toolName
                }
                ui
            }
            val msgId = streamingMessageId

            if (msgId != null) {
                replaceStreamingMessage(msgId, blocks, isComplete = true)
                resetMainStreamingState()
            } else {
                val assistantMsg = ChatMessage.Assistant(
                    id = UUID.randomUUID().toString(),
                    blocks = blocks,
                    isComplete = true,
                )
                _uiState.update { it.copy(messages = it.messages + assistantMsg) }
            }
        }
    }

    private fun finalizeSubAgentAssistantMessage(parentToolUseId: String, msg: AssistantMessage) {
        scope.launch {
            subStateMutex.withLock {
                val state = subAgentStates[parentToolUseId]
                val blocks = msg.content.map { it.toUiBlock() }
                val msgId = state?.streamingMessageId

                if (msgId != null) {
                    replaceSubAgentStreamingMessage(parentToolUseId, msgId, blocks, isComplete = true)
                    state.streamingMessageId = null
                } else {
                    ensureSubAgentTask(parentToolUseId)
                    val assistantMsg = ChatMessage.Assistant(
                        id = UUID.randomUUID().toString(),
                        blocks = blocks,
                        isComplete = true,
                    )
                    updateSubAgentTask(parentToolUseId) { task ->
                        task.copy(messages = task.messages + assistantMsg)
                    }
                }
            }
        }
    }

    private fun handleResultMessage(msg: ResultMessage) {
        // メインバッファの残りを flush
        val mainBuffer = streamingBuffer
        val mainMsgId = streamingMessageId
        if (mainBuffer != null && mainMsgId != null && mainBuffer.hasContent()) {
            replaceStreamingMessage(mainMsgId, mainBuffer.toUiBlocks(), isComplete = true)
        }

        // 全サブエージェントバッファを flush
        subAgentStates.forEach { (parentToolUseId, state) ->
            val subMsgId = state.streamingMessageId
            if (subMsgId != null && state.buffer.hasContent()) {
                replaceSubAgentStreamingMessage(
                    parentToolUseId, subMsgId, state.buffer.toUiBlocks(), isComplete = true,
                )
            }
            updateSubAgentTask(parentToolUseId) { it.copy(isComplete = true) }
        }

        resetStreamingState()

        _uiState.update {
            it.copy(
                sessionState = SessionState.WaitingForInput,
                isStreaming = false,
                totalCostUsd = msg.totalCostUsd ?: it.totalCostUsd,
                errorMessage = if (msg.isError) "Turn ended with error: ${msg.subtype}" else null,
            )
        }
    }

    // region streaming helpers

    private fun throttledUpdateStreamingMessage() {
        val now = System.currentTimeMillis()
        if (now - lastUiUpdateTime < UI_UPDATE_THROTTLE_MS) return
        lastUiUpdateTime = now
        updateStreamingMessage()
    }

    private fun updateStreamingMessage() {
        val buffer = streamingBuffer ?: return
        val msgId = streamingMessageId ?: return

        lastUiUpdateTime = System.currentTimeMillis()
        replaceStreamingMessage(msgId, buffer.toUiBlocks(), isComplete = false)
    }

    private fun updateSubAgentStreamingMessage(parentToolUseId: String, state: SubAgentStreamState) {
        val msgId = state.streamingMessageId ?: return
        replaceSubAgentStreamingMessage(parentToolUseId, msgId, state.buffer.toUiBlocks(), isComplete = false)
    }

    private fun replaceStreamingMessage(
        messageId: String,
        blocks: List<UiContentBlock>,
        isComplete: Boolean,
    ) {
        _uiState.update { state ->
            state.copy(
                messages = state.messages.map { existing ->
                    if (existing.id == messageId) {
                        ChatMessage.Assistant(
                            id = messageId,
                            blocks = blocks,
                            isComplete = isComplete,
                        )
                    } else {
                        existing
                    }
                },
            )
        }
    }

    private fun replaceSubAgentStreamingMessage(
        parentToolUseId: String,
        messageId: String,
        blocks: List<UiContentBlock>,
        isComplete: Boolean,
    ) {
        updateSubAgentTask(parentToolUseId) { task ->
            task.copy(
                messages = task.messages.map { msg ->
                    if (msg.id == messageId) {
                        ChatMessage.Assistant(
                            id = messageId,
                            blocks = blocks,
                            isComplete = isComplete,
                        )
                    } else {
                        msg
                    }
                },
            )
        }
    }

    // endregion

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

    private fun resetMainStreamingState() {
        streamingBuffer = null
        streamingMessageId = null
    }

    private fun resetStreamingState() {
        streamingBuffer = null
        streamingMessageId = null
        subAgentStates.clear()
        toolUseNameIndex.clear()
    }

    fun respondPermission(allow: Boolean, denyMessage: String = "Denied by user") {
        permissionHandler.respondPermission(allow, denyMessage)
    }

    fun respondQuestion(answers: Map<String, String>) {
        permissionHandler.respondQuestion(answers)
    }

    fun abortSession() {
        activeTurnJob?.cancel()
        activeTurnJob = null
        resetStreamingState()
        scope.launch { client?.interrupt() }
        _uiState.update {
            it.copy(sessionState = SessionState.WaitingForInput, isStreaming = false)
        }
    }

    fun reconnect() {
        activeTurnJob?.cancel()
        activeTurnJob = null
        resetStreamingState()
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

    companion object {
        private const val UI_UPDATE_THROTTLE_MS = 50L
    }
}
