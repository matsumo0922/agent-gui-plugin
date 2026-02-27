package me.matsumo.agentguiplugin.viewmodel

import me.matsumo.agentguiplugin.model.AttachedFile
import me.matsumo.claude.agent.types.Model
import me.matsumo.claude.agent.types.PermissionMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ChatStateReducerTest {

    private val defaultState = ChatUiState()

    // ──────────────────────────────────────────
    // SessionState 遷移表ガード
    // ──────────────────────────────────────────

    @Test
    fun `Disconnected can transition to Connecting`() {
        assertTrue(SessionState.Disconnected.canTransitionTo(SessionState.Connecting))
    }

    @Test
    fun `Disconnected cannot transition to Ready`() {
        assertTrue(!SessionState.Disconnected.canTransitionTo(SessionState.Ready))
    }

    @Test
    fun `Connecting can transition to Ready, AuthRequired, Error`() {
        assertTrue(SessionState.Connecting.canTransitionTo(SessionState.Ready))
        assertTrue(SessionState.Connecting.canTransitionTo(SessionState.AuthRequired))
        assertTrue(SessionState.Connecting.canTransitionTo(SessionState.Error))
    }

    @Test
    fun `Ready can transition to Processing, Disconnected, Error`() {
        assertTrue(SessionState.Ready.canTransitionTo(SessionState.Processing))
        assertTrue(SessionState.Ready.canTransitionTo(SessionState.Disconnected))
        assertTrue(SessionState.Ready.canTransitionTo(SessionState.Error))
    }

    @Test
    fun `Processing can transition to WaitingForInput, Error`() {
        assertTrue(SessionState.Processing.canTransitionTo(SessionState.WaitingForInput))
        assertTrue(SessionState.Processing.canTransitionTo(SessionState.Error))
        assertTrue(!SessionState.Processing.canTransitionTo(SessionState.Ready))
    }

    @Test
    fun `WaitingForInput can transition to Processing, Connecting, Disconnected, Error`() {
        assertTrue(SessionState.WaitingForInput.canTransitionTo(SessionState.Processing))
        assertTrue(SessionState.WaitingForInput.canTransitionTo(SessionState.Connecting))
        assertTrue(SessionState.WaitingForInput.canTransitionTo(SessionState.Disconnected))
        assertTrue(SessionState.WaitingForInput.canTransitionTo(SessionState.Error))
    }

    @Test
    fun `Error can only transition to Disconnected`() {
        assertTrue(SessionState.Error.canTransitionTo(SessionState.Disconnected))
        assertTrue(!SessionState.Error.canTransitionTo(SessionState.Ready))
        assertTrue(!SessionState.Error.canTransitionTo(SessionState.Processing))
    }

    @Test
    fun `AuthRequired can only transition to Disconnected`() {
        assertTrue(SessionState.AuthRequired.canTransitionTo(SessionState.Disconnected))
        assertTrue(!SessionState.AuthRequired.canTransitionTo(SessionState.Connecting))
    }

    // ──────────────────────────────────────────
    // Session lifecycle actions
    // ──────────────────────────────────────────

    @Test
    fun `StartConnecting sets state to Connecting`() {
        val result = reduce(defaultState, StateAction.StartConnecting)
        assertEquals(SessionState.Connecting, result.sessionState)
    }

    @Test
    fun `SessionReady sets state to Ready with sessionId`() {
        val connecting = defaultState.copy(session = SessionStatus(state = SessionState.Connecting))
        val result = reduce(connecting, StateAction.SessionReady(sessionId = "s1"))
        assertEquals(SessionState.Ready, result.sessionState)
        assertEquals("s1", result.sessionId)
    }

    @Test
    fun `SessionAuthRequired sets state to AuthRequired`() {
        val connecting = defaultState.copy(session = SessionStatus(state = SessionState.Connecting))
        val result = reduce(connecting, StateAction.SessionAuthRequired)
        assertEquals(SessionState.AuthRequired, result.sessionState)
    }

    @Test
    fun `SessionError sets state and errorMessage`() {
        val connecting = defaultState.copy(session = SessionStatus(state = SessionState.Connecting))
        val result = reduce(connecting, StateAction.SessionError("oops"))
        assertEquals(SessionState.Error, result.sessionState)
        assertEquals("oops", result.errorMessage)
    }

    @Test
    fun `SessionDisconnected clears authOutputLines`() {
        val state = defaultState.copy(
            session = SessionStatus(state = SessionState.AuthRequired),
            authOutputLines = listOf("line1"),
        )
        val result = reduce(state, StateAction.SessionDisconnected)
        assertEquals(SessionState.Disconnected, result.sessionState)
        assertTrue(result.authOutputLines.isEmpty())
    }

    @Test
    fun `SessionIdUpdated only updates sessionId`() {
        val state = defaultState.copy(session = SessionStatus(state = SessionState.Ready, sessionId = "old"))
        val result = reduce(state, StateAction.SessionIdUpdated("new"))
        assertEquals("new", result.sessionId)
        assertEquals(SessionState.Ready, result.sessionState)
    }

    // ──────────────────────────────────────────
    // Turn lifecycle actions
    // ──────────────────────────────────────────

    @Test
    fun `TurnStarted sets tree, cursor, clears files, sets Processing`() {
        val tree = ConversationTree()
        val path = listOf(SlotPathSegment(0, 0))
        val file = AttachedFile(id = "f1", name = "test.kt", path = "/test.kt", icon = null)
        val state = defaultState.copy(
            session = SessionStatus(state = SessionState.Ready),
            attachedFiles = listOf(file),
        )

        val result = reduce(state, StateAction.TurnStarted(newTree = tree, newPath = path))
        assertEquals(tree, result.conversationTree)
        assertEquals(path, result.conversationCursor.activeLeafPath)
        assertTrue(result.attachedFiles.isEmpty())
        assertEquals(SessionState.Processing, result.sessionState)
    }

    @Test
    fun `AssistantMessageReceived appends new message when no streaming id`() {
        val userMsg = ChatMessage.User(id = "u1", text = "hello")
        val tree = ConversationTree(
            slots = listOf(
                MessageSlot(
                    editGroupId = "u1",
                    timelines = listOf(Timeline(userMessage = userMsg)),
                ),
            ),
        )
        val path = listOf(SlotPathSegment(0, 0))
        val state = defaultState.copy(
            conversationTree = tree,
            conversationCursor = ConversationCursor(activeLeafPath = path),
        )

        val assistantMsg = ChatMessage.Assistant(id = "a1", blocks = emptyList())
        val result = reduce(state, StateAction.AssistantMessageReceived(assistantMsg))

        assertEquals("a1", result.conversationCursor.activeStreamingMessageId)
        val timeline = result.conversationTree.slots[0].timelines[0]
        assertEquals(1, timeline.responses.size)
        assertEquals("a1", timeline.responses[0].id)
    }

    @Test
    fun `AssistantMessageReceived updates existing message when same streaming id`() {
        val userMsg = ChatMessage.User(id = "u1", text = "hello")
        val existingAssistant = ChatMessage.Assistant(id = "a1", blocks = emptyList())
        val tree = ConversationTree(
            slots = listOf(
                MessageSlot(
                    editGroupId = "u1",
                    timelines = listOf(
                        Timeline(userMessage = userMsg, responses = listOf(existingAssistant)),
                    ),
                ),
            ),
        )
        val path = listOf(SlotPathSegment(0, 0))
        val state = defaultState.copy(
            conversationTree = tree,
            conversationCursor = ConversationCursor(
                activeLeafPath = path,
                activeStreamingMessageId = "a1",
            ),
        )

        val updatedMsg = ChatMessage.Assistant(
            id = "a1",
            blocks = listOf(UiContentBlock.Text("updated")),
        )
        val result = reduce(state, StateAction.AssistantMessageReceived(updatedMsg))

        val timeline = result.conversationTree.slots[0].timelines[0]
        assertEquals(1, timeline.responses.size)
        val assistant = timeline.responses[0] as ChatMessage.Assistant
        assertEquals(1, assistant.blocks.size)
    }

    @Test
    fun `TurnCompleted transitions to WaitingForInput and clears streaming id`() {
        val state = defaultState.copy(
            session = SessionStatus(state = SessionState.Processing),
            conversationCursor = ConversationCursor(activeStreamingMessageId = "a1"),
        )
        val result = reduce(state, StateAction.TurnCompleted(isError = false, errorMessage = null))

        assertEquals(SessionState.WaitingForInput, result.sessionState)
        assertNull(result.conversationCursor.activeStreamingMessageId)
        assertNull(result.errorMessage)
    }

    @Test
    fun `TurnCompleted with error sets errorMessage`() {
        val state = defaultState.copy(
            session = SessionStatus(state = SessionState.Processing),
        )
        val result = reduce(state, StateAction.TurnCompleted(isError = true, errorMessage = "timeout"))

        assertEquals(SessionState.WaitingForInput, result.sessionState)
        assertNotNull(result.errorMessage)
        assertTrue(result.errorMessage!!.contains("timeout"))
    }

    @Test
    fun `TurnError sets error state`() {
        val state = defaultState.copy(
            session = SessionStatus(state = SessionState.Processing),
        )
        val result = reduce(state, StateAction.TurnError("bad request"))

        assertEquals(SessionState.Error, result.sessionState)
        assertEquals("bad request", result.errorMessage)
    }

    @Test
    fun `TurnError with custom toState`() {
        val state = defaultState.copy(
            session = SessionStatus(state = SessionState.Processing),
        )
        val result = reduce(
            state,
            StateAction.TurnError("edit failed", toState = SessionState.WaitingForInput),
        )

        assertEquals(SessionState.WaitingForInput, result.sessionState)
        assertEquals("edit failed", result.errorMessage)
    }

    @Test
    fun `Aborted sets WaitingForInput and clears streaming`() {
        val newTree = ConversationTree()
        val state = defaultState.copy(
            session = SessionStatus(state = SessionState.Processing),
            conversationCursor = ConversationCursor(activeStreamingMessageId = "a1"),
        )
        val result = reduce(state, StateAction.Aborted(newTree))

        assertEquals(SessionState.WaitingForInput, result.sessionState)
        assertNull(result.conversationCursor.activeStreamingMessageId)
        assertEquals(newTree, result.conversationTree)
    }

    // ──────────────────────────────────────────
    // Edit / Navigate actions
    // ──────────────────────────────────────────

    @Test
    fun `EditBranchCreated updates tree and cursor`() {
        val tree = ConversationTree()
        val path = listOf(SlotPathSegment(0, 1))
        val result = reduce(defaultState, StateAction.EditBranchCreated(tree, path))

        assertEquals(tree, result.conversationTree)
        assertEquals(path, result.conversationCursor.activeLeafPath)
    }

    @Test
    fun `EditSessionSynced updates sessionId only`() {
        val state = defaultState.copy(session = SessionStatus(state = SessionState.Processing))
        val result = reduce(state, StateAction.EditSessionSynced("new-session"))

        assertEquals("new-session", result.sessionId)
        assertEquals(SessionState.Processing, result.sessionState)
    }

    @Test
    fun `VersionNavigated with session switch sets Connecting`() {
        val tree = ConversationTree()
        val path = listOf(SlotPathSegment(0, 0))
        val state = defaultState.copy(
            session = SessionStatus(state = SessionState.WaitingForInput, sessionId = "s1"),
        )

        val result = reduce(
            state,
            StateAction.VersionNavigated(
                newTree = tree,
                newPath = path,
                newSessionId = "s2",
                needsSessionSwitch = true,
            ),
        )

        assertEquals(SessionState.Connecting, result.sessionState)
        assertEquals("s2", result.sessionId)
    }

    @Test
    fun `VersionNavigated without session switch preserves state`() {
        val tree = ConversationTree()
        val path = listOf(SlotPathSegment(0, 0))
        val state = defaultState.copy(
            session = SessionStatus(state = SessionState.WaitingForInput, sessionId = "s1"),
        )

        val result = reduce(
            state,
            StateAction.VersionNavigated(
                newTree = tree,
                newPath = path,
                newSessionId = "s1",
                needsSessionSwitch = false,
            ),
        )

        assertEquals(SessionState.WaitingForInput, result.sessionState)
    }

    @Test
    fun `BranchSwitchCompleted sets WaitingForInput`() {
        val state = defaultState.copy(
            session = SessionStatus(state = SessionState.Connecting),
        )
        val result = reduce(state, StateAction.BranchSwitchCompleted("s2"))

        assertEquals(SessionState.WaitingForInput, result.sessionState)
        assertEquals("s2", result.sessionId)
    }

    @Test
    fun `BranchSwitchFailed sets Error`() {
        val state = defaultState.copy(
            session = SessionStatus(state = SessionState.Connecting),
        )
        val result = reduce(state, StateAction.BranchSwitchFailed("network error"))

        assertEquals(SessionState.Error, result.sessionState)
        assertEquals("network error", result.errorMessage)
    }

    // ──────────────────────────────────────────
    // File management
    // ──────────────────────────────────────────

    @Test
    fun `FileAttached adds file`() {
        val file = AttachedFile(id = "f1", name = "test.kt", path = "/test.kt", icon = null)
        val result = reduce(defaultState, StateAction.FileAttached(file))
        assertEquals(1, result.attachedFiles.size)
        assertEquals("f1", result.attachedFiles[0].id)
    }

    @Test
    fun `FileAttached ignores duplicate`() {
        val file = AttachedFile(id = "f1", name = "test.kt", path = "/test.kt", icon = null)
        val state = defaultState.copy(attachedFiles = listOf(file))
        val result = reduce(state, StateAction.FileAttached(file))
        assertEquals(1, result.attachedFiles.size)
    }

    @Test
    fun `FileDetached removes file`() {
        val file = AttachedFile(id = "f1", name = "test.kt", path = "/test.kt", icon = null)
        val state = defaultState.copy(attachedFiles = listOf(file))
        val result = reduce(state, StateAction.FileDetached(file))
        assertTrue(result.attachedFiles.isEmpty())
    }

    // ──────────────────────────────────────────
    // Config
    // ──────────────────────────────────────────

    @Test
    fun `ModelChanged updates model`() {
        val result = reduce(defaultState, StateAction.ModelChanged(Model.OPUS))
        assertEquals(Model.OPUS, result.model)
    }

    @Test
    fun `PermissionModeChanged updates permissionMode`() {
        val result = reduce(defaultState, StateAction.PermissionModeChanged(PermissionMode.PLAN))
        assertEquals(PermissionMode.PLAN, result.permissionMode)
    }

    // ──────────────────────────────────────────
    // External sync
    // ──────────────────────────────────────────

    @Test
    fun `SubAgentTasksUpdated replaces tasks`() {
        val tasks = mapOf("t1" to SubAgentTask(id = "t1"))
        val result = reduce(defaultState, StateAction.SubAgentTasksUpdated(tasks))
        assertEquals(1, result.subAgentTasks.size)
        assertTrue(result.subAgentTasks.containsKey("t1"))
    }

    @Test
    fun `UsageUpdated replaces usage`() {
        val usage = UsageInfo(contextUsage = 0.5f, totalInputTokens = 1000L, totalCostUsd = 0.01)
        val result = reduce(defaultState, StateAction.UsageUpdated(usage))
        assertEquals(0.5f, result.usage.contextUsage)
        assertEquals(1000L, result.usage.totalInputTokens)
        assertEquals(0.01, result.usage.totalCostUsd)
    }

    @Test
    fun `AuthOutputUpdated replaces lines`() {
        val result = reduce(defaultState, StateAction.AuthOutputUpdated(listOf("line1", "line2")))
        assertEquals(2, result.authOutputLines.size)
    }

    // ──────────────────────────────────────────
    // History
    // ──────────────────────────────────────────

    @Test
    fun `HistoryImported sets tree and cursor`() {
        val tree = ConversationTree()
        val cursor = ConversationCursor(activeLeafPath = listOf(SlotPathSegment(0, 0)))
        val result = reduce(defaultState, StateAction.HistoryImported(tree, cursor))
        assertEquals(tree, result.conversationTree)
        assertEquals(cursor, result.conversationCursor)
    }

    // ──────────────────────────────────────────
    // Reset
    // ──────────────────────────────────────────

    @Test
    fun `Reset creates fresh state with model and permissionMode`() {
        val state = defaultState.copy(
            session = SessionStatus(state = SessionState.Processing, sessionId = "s1"),
            attachedFiles = listOf(AttachedFile(id = "f1", name = "test.kt", path = "/test.kt", icon = null)),
            authOutputLines = listOf("line"),
        )
        val result = reduce(state, StateAction.Reset(model = Model.HAIKU, permissionMode = PermissionMode.PLAN))

        assertEquals(Model.HAIKU, result.model)
        assertEquals(PermissionMode.PLAN, result.permissionMode)
        assertEquals(SessionState.Disconnected, result.sessionState)
        assertTrue(result.attachedFiles.isEmpty())
        assertTrue(result.authOutputLines.isEmpty())
        assertNull(result.sessionId)
    }
}
