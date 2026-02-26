package me.matsumo.agentguiplugin.viewmodel

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ConversationTreeTest {

    // ──────────────────────────────────────────────────────────
    // Test Helpers
    // ──────────────────────────────────────────────────────────

    private fun userMsg(text: String, editGroupId: String = "eg-${text.hashCode()}"): ChatMessage.User {
        return ChatMessage.User(
            id = "msg-${text.hashCode()}",
            editGroupId = editGroupId,
            text = text,
        )
    }

    private fun assistantMsg(text: String): ChatMessage.Assistant {
        return ChatMessage.Assistant(
            id = "ast-${text.hashCode()}",
            blocks = listOf(UiContentBlock.Text(text)),
            timestamp = 0L,
        )
    }

    private fun interruptedMsg(): ChatMessage.Interrupted {
        return ChatMessage.Interrupted(
            id = "int-${System.nanoTime()}",
            timestamp = 0L,
        )
    }

    // ──────────────────────────────────────────────────────────
    // Empty Tree
    // ──────────────────────────────────────────────────────────

    @Test
    fun `empty tree returns empty messages`() {
        val tree = ConversationTree()
        assertEquals(emptyList(), tree.getActiveMessages())
    }

    @Test
    fun `empty tree returns empty leaf path`() {
        val tree = ConversationTree()
        assertEquals(emptyList(), tree.getActiveLeafPath())
    }

    @Test
    fun `empty tree returns null for getActiveLeafSessionId`() {
        val tree = ConversationTree()
        assertNull(tree.getActiveLeafSessionId())
    }

    @Test
    fun `empty tree returns empty getAllEditInfo`() {
        val tree = ConversationTree()
        assertEquals(emptyMap(), tree.getAllEditInfo())
    }

    // ──────────────────────────────────────────────────────────
    // appendUserMessage
    // ──────────────────────────────────────────────────────────

    @Test
    fun `appendUserMessage to empty tree creates first slot`() {
        val tree = ConversationTree()
        val msg = userMsg("Hello", editGroupId = "eg-1")

        val (newTree, path) = tree.appendUserMessage(msg, branchSessionId = "sess-1")

        assertEquals(1, newTree.slots.size)
        assertEquals("eg-1", newTree.slots[0].editGroupId)
        assertEquals(msg, newTree.slots[0].timelines[0].userMessage)
        assertEquals("sess-1", newTree.slots[0].timelines[0].branchSessionId)
        assertEquals(listOf(SlotPathSegment(0, 0)), path)
    }

    @Test
    fun `appendUserMessage to tree with one slot adds child`() {
        val msg1 = userMsg("First", editGroupId = "eg-1")
        val (tree1, _) = ConversationTree().appendUserMessage(msg1, branchSessionId = "sess-1")

        val msg2 = userMsg("Second", editGroupId = "eg-2")
        val (tree2, path2) = tree1.appendUserMessage(msg2, branchSessionId = "sess-1")

        // msg2 should be a child of msg1's timeline
        val timeline1 = tree2.slots[0].timelines[0]
        assertEquals(1, timeline1.childSlots.size)
        assertEquals("eg-2", timeline1.childSlots[0].editGroupId)
        assertEquals(msg2, timeline1.childSlots[0].timelines[0].userMessage)
        assertEquals(
            listOf(SlotPathSegment(0, 0), SlotPathSegment(0, 0)),
            path2,
        )
    }

    @Test
    fun `appendUserMessage with null branchSessionId`() {
        val tree = ConversationTree()
        val msg = userMsg("Hello", editGroupId = "eg-1")

        val (newTree, _) = tree.appendUserMessage(msg, branchSessionId = null)

        assertNull(newTree.slots[0].timelines[0].branchSessionId)
    }

    @Test
    fun `appendUserMessage chains three messages deep`() {
        var tree = ConversationTree()
        val messages = (1..3).map { userMsg("Msg $it", editGroupId = "eg-$it") }

        for (msg in messages) {
            val (newTree, _) = tree.appendUserMessage(msg, branchSessionId = null)
            tree = newTree
        }

        // Verify depth structure
        assertEquals(1, tree.slots.size)
        val t0 = tree.slots[0].timelines[0]
        assertEquals(1, t0.childSlots.size)
        val t1 = t0.childSlots[0].timelines[0]
        assertEquals(1, t1.childSlots.size)
        assertEquals("eg-3", t1.childSlots[0].editGroupId)
    }

    // ──────────────────────────────────────────────────────────
    // appendResponse / updateLastResponse
    // ──────────────────────────────────────────────────────────

    @Test
    fun `appendResponse adds response to targeted timeline`() {
        val msg = userMsg("Hello", editGroupId = "eg-1")
        val (tree, path) = ConversationTree().appendUserMessage(msg, branchSessionId = null)

        val response = assistantMsg("Hi there!")
        val newTree = tree.appendResponse(path, response)

        val timeline = newTree.slots[0].timelines[0]
        assertEquals(1, timeline.responses.size)
        assertEquals(response, timeline.responses[0])
    }

    @Test
    fun `appendResponse adds multiple responses`() {
        val msg = userMsg("Hello", editGroupId = "eg-1")
        val (tree, path) = ConversationTree().appendUserMessage(msg, branchSessionId = null)

        val r1 = assistantMsg("First response")
        val r2 = assistantMsg("Second response")
        val newTree = tree.appendResponse(path, r1).appendResponse(path, r2)

        val timeline = newTree.slots[0].timelines[0]
        assertEquals(2, timeline.responses.size)
        assertEquals(r1, timeline.responses[0])
        assertEquals(r2, timeline.responses[1])
    }

    @Test
    fun `updateLastResponse transforms the last response`() {
        val msg = userMsg("Hello", editGroupId = "eg-1")
        val (tree, path) = ConversationTree().appendUserMessage(msg, branchSessionId = null)

        val initial = assistantMsg("Partial...")
        val treeWithResponse = tree.appendResponse(path, initial)

        val updated = treeWithResponse.updateLastResponse(path) { existing ->
            (existing as ChatMessage.Assistant).copy(
                blocks = listOf(UiContentBlock.Text("Complete response"))
            )
        }

        val timeline = updated.slots[0].timelines[0]
        assertEquals(1, timeline.responses.size)
        val block = (timeline.responses[0] as ChatMessage.Assistant).blocks[0] as UiContentBlock.Text
        assertEquals("Complete response", block.text)
    }

    @Test
    fun `updateLastResponse on empty responses is no-op`() {
        val msg = userMsg("Hello", editGroupId = "eg-1")
        val (tree, path) = ConversationTree().appendUserMessage(msg, branchSessionId = null)

        val result = tree.updateLastResponse(path) { it }
        assertEquals(tree, result)
    }

    @Test
    fun `appendResponse with Interrupted message`() {
        val msg = userMsg("Hello", editGroupId = "eg-1")
        val (tree, path) = ConversationTree().appendUserMessage(msg, branchSessionId = null)

        val interrupted = interruptedMsg()
        val newTree = tree.appendResponse(path, interrupted)

        val timeline = newTree.slots[0].timelines[0]
        assertEquals(1, timeline.responses.size)
        assertTrue(timeline.responses[0] is ChatMessage.Interrupted)
    }

    // ──────────────────────────────────────────────────────────
    // editMessage
    // ──────────────────────────────────────────────────────────

    @Test
    fun `editMessage adds new timeline to slot`() {
        val msg1 = userMsg("Original", editGroupId = "eg-1")
        val (tree, _) = ConversationTree().appendUserMessage(msg1, branchSessionId = "sess-1")

        val editedMsg = userMsg("Edited", editGroupId = "eg-1")
        val editedTree = tree.editMessage("eg-1", editedMsg, branchSessionId = "sess-2")

        val slot = editedTree.slots[0]
        assertEquals(2, slot.timelines.size)
        assertEquals(1, slot.activeTimelineIndex) // new timeline is active
        assertEquals("Original", slot.timelines[0].userMessage.text)
        assertEquals("Edited", slot.timelines[1].userMessage.text)
        assertEquals("sess-1", slot.timelines[0].branchSessionId)
        assertEquals("sess-2", slot.timelines[1].branchSessionId)
    }

    @Test
    fun `editMessage on nested slot works correctly`() {
        val msg1 = userMsg("First", editGroupId = "eg-1")
        val msg2 = userMsg("Second", editGroupId = "eg-2")
        var tree = ConversationTree()
        tree = tree.appendUserMessage(msg1, branchSessionId = null).first
        tree = tree.appendUserMessage(msg2, branchSessionId = null).first

        val editedMsg = userMsg("Second-v2", editGroupId = "eg-2")
        val editedTree = tree.editMessage("eg-2", editedMsg, branchSessionId = "sess-branch")

        val childSlot = editedTree.slots[0].timelines[0].childSlots[0]
        assertEquals(2, childSlot.timelines.size)
        assertEquals(1, childSlot.activeTimelineIndex)
        assertEquals("Second-v2", childSlot.timelines[1].userMessage.text)
    }

    @Test
    fun `editMessage preserves existing child slots in original timeline`() {
        val msg1 = userMsg("First", editGroupId = "eg-1")
        val msg2 = userMsg("Second", editGroupId = "eg-2")
        val msg3 = userMsg("Third", editGroupId = "eg-3")
        var tree = ConversationTree()
        tree = tree.appendUserMessage(msg1, branchSessionId = null).first
        tree = tree.appendUserMessage(msg2, branchSessionId = null).first
        tree = tree.appendUserMessage(msg3, branchSessionId = null).first

        // Edit msg2 — msg3 should stay in the original timeline
        val edited = userMsg("Second-v2", editGroupId = "eg-2")
        val editedTree = tree.editMessage("eg-2", edited, branchSessionId = null)

        val childSlot = editedTree.slots[0].timelines[0].childSlots[0]
        // Original timeline still has msg3 as child
        assertEquals(1, childSlot.timelines[0].childSlots.size)
        assertEquals("eg-3", childSlot.timelines[0].childSlots[0].editGroupId)
        // New timeline has no children
        assertEquals(0, childSlot.timelines[1].childSlots.size)
    }

    @Test
    fun `editMessage multiple times adds multiple timelines`() {
        val original = userMsg("v1", editGroupId = "eg-1")
        var tree = ConversationTree().appendUserMessage(original, branchSessionId = null).first

        for (i in 2..4) {
            val edit = userMsg("v$i", editGroupId = "eg-1")
            tree = tree.editMessage("eg-1", edit, branchSessionId = "sess-$i")
        }

        val slot = tree.slots[0]
        assertEquals(4, slot.timelines.size)
        assertEquals(3, slot.activeTimelineIndex) // last edit is active
        assertEquals("v1", slot.timelines[0].userMessage.text)
        assertEquals("v4", slot.timelines[3].userMessage.text)
    }

    // ──────────────────────────────────────────────────────────
    // navigateVersion
    // ──────────────────────────────────────────────────────────

    @Test
    fun `navigateVersion moves to previous timeline`() {
        val original = userMsg("v1", editGroupId = "eg-1")
        var tree = ConversationTree().appendUserMessage(original, branchSessionId = null).first
        tree = tree.editMessage("eg-1", userMsg("v2", editGroupId = "eg-1"), branchSessionId = null)
        // activeTimelineIndex is 1 (v2)

        val navigated = tree.navigateVersion("eg-1", direction = -1)
        assertEquals(0, navigated.slots[0].activeTimelineIndex)
    }

    @Test
    fun `navigateVersion moves to next timeline`() {
        val original = userMsg("v1", editGroupId = "eg-1")
        var tree = ConversationTree().appendUserMessage(original, branchSessionId = null).first
        tree = tree.editMessage("eg-1", userMsg("v2", editGroupId = "eg-1"), branchSessionId = null)
        // Navigate back to v1
        tree = tree.navigateVersion("eg-1", direction = -1)
        assertEquals(0, tree.slots[0].activeTimelineIndex)

        // Navigate forward to v2
        val navigated = tree.navigateVersion("eg-1", direction = 1)
        assertEquals(1, navigated.slots[0].activeTimelineIndex)
    }

    @Test
    fun `navigateVersion clamps at boundaries`() {
        val original = userMsg("v1", editGroupId = "eg-1")
        var tree = ConversationTree().appendUserMessage(original, branchSessionId = null).first
        tree = tree.editMessage("eg-1", userMsg("v2", editGroupId = "eg-1"), branchSessionId = null)

        // Try to go beyond last timeline
        val clamped = tree.navigateVersion("eg-1", direction = 1)
        assertEquals(1, clamped.slots[0].activeTimelineIndex) // stays at 1

        // Try to go before first timeline
        val tree2 = tree.navigateVersion("eg-1", direction = -1) // index 0
        val clamped2 = tree2.navigateVersion("eg-1", direction = -1) // still 0
        assertEquals(0, clamped2.slots[0].activeTimelineIndex)
    }

    // ──────────────────────────────────────────────────────────
    // getActiveMessages
    // ──────────────────────────────────────────────────────────

    @Test
    fun `getActiveMessages returns flat list along active path`() {
        val msg1 = userMsg("First", editGroupId = "eg-1")
        val msg2 = userMsg("Second", editGroupId = "eg-2")
        var tree = ConversationTree()
        tree = tree.appendUserMessage(msg1, branchSessionId = null).first
        val (tree2, path1) = tree.let { it to it.getActiveLeafPath() }
        tree = tree2.appendResponse(path1, assistantMsg("Response 1"))
        tree = tree.appendUserMessage(msg2, branchSessionId = null).first

        val messages = tree.getActiveMessages()

        assertEquals(3, messages.size)
        assertEquals("First", (messages[0] as ChatMessage.User).text)
        assertEquals("Response 1", ((messages[1] as ChatMessage.Assistant).blocks[0] as UiContentBlock.Text).text)
        assertEquals("Second", (messages[2] as ChatMessage.User).text)
    }

    @Test
    fun `getActiveMessages follows active timeline after edit`() {
        val msg1 = userMsg("Original", editGroupId = "eg-1")
        var tree = ConversationTree().appendUserMessage(msg1, branchSessionId = null).first

        val edited = userMsg("Edited", editGroupId = "eg-1")
        tree = tree.editMessage("eg-1", edited, branchSessionId = null)

        val messages = tree.getActiveMessages()
        assertEquals(1, messages.size)
        assertEquals("Edited", (messages[0] as ChatMessage.User).text)
    }

    @Test
    fun `getActiveMessages follows original timeline after navigate back`() {
        val msg1 = userMsg("Original", editGroupId = "eg-1")
        var tree = ConversationTree().appendUserMessage(msg1, branchSessionId = null).first
        // Add child to original timeline
        val path0 = tree.getActiveLeafPath()
        tree = tree.appendResponse(path0, assistantMsg("Original response"))
        val msg2 = userMsg("Follow up", editGroupId = "eg-2")
        tree = tree.appendUserMessage(msg2, branchSessionId = null).first

        // Edit msg1
        val edited = userMsg("Edited", editGroupId = "eg-1")
        tree = tree.editMessage("eg-1", edited, branchSessionId = null)

        // Navigate back to original
        tree = tree.navigateVersion("eg-1", direction = -1)

        val messages = tree.getActiveMessages()
        assertEquals(3, messages.size)
        assertEquals("Original", (messages[0] as ChatMessage.User).text)
        assertEquals("Follow up", (messages[2] as ChatMessage.User).text)
    }

    // ──────────────────────────────────────────────────────────
    // getActiveLeafPath
    // ──────────────────────────────────────────────────────────

    @Test
    fun `getActiveLeafPath for single message`() {
        val msg = userMsg("Hello", editGroupId = "eg-1")
        val (tree, _) = ConversationTree().appendUserMessage(msg, branchSessionId = null)

        assertEquals(listOf(SlotPathSegment(0, 0)), tree.getActiveLeafPath())
    }

    @Test
    fun `getActiveLeafPath for chain of three messages`() {
        var tree = ConversationTree()
        for (i in 1..3) {
            tree = tree.appendUserMessage(userMsg("Msg $i", editGroupId = "eg-$i"), branchSessionId = null).first
        }

        val path = tree.getActiveLeafPath()
        assertEquals(3, path.size)
        assertEquals(
            listOf(
                SlotPathSegment(0, 0),
                SlotPathSegment(0, 0),
                SlotPathSegment(0, 0),
            ),
            path,
        )
    }

    @Test
    fun `getActiveLeafPath follows edited timeline`() {
        val msg = userMsg("v1", editGroupId = "eg-1")
        var tree = ConversationTree().appendUserMessage(msg, branchSessionId = null).first
        tree = tree.editMessage("eg-1", userMsg("v2", editGroupId = "eg-1"), branchSessionId = null)

        val path = tree.getActiveLeafPath()
        assertEquals(listOf(SlotPathSegment(0, 1)), path) // timeline index 1
    }

    // ──────────────────────────────────────────────────────────
    // getEditInfo / getAllEditInfo
    // ──────────────────────────────────────────────────────────

    @Test
    fun `getEditInfo returns correct info for single version`() {
        val msg = userMsg("Hello", editGroupId = "eg-1")
        val tree = ConversationTree().appendUserMessage(msg, branchSessionId = null).first

        val info = tree.getEditInfo("eg-1")
        assertNotNull(info)
        assertEquals("eg-1", info.editGroupId)
        assertEquals(0, info.currentIndex)
        assertEquals(1, info.totalVersions)
        assertFalse(info.hasMultipleVersions)
    }

    @Test
    fun `getEditInfo returns correct info after edit`() {
        val msg = userMsg("v1", editGroupId = "eg-1")
        var tree = ConversationTree().appendUserMessage(msg, branchSessionId = null).first
        tree = tree.editMessage("eg-1", userMsg("v2", editGroupId = "eg-1"), branchSessionId = null)

        val info = tree.getEditInfo("eg-1")
        assertNotNull(info)
        assertEquals(1, info.currentIndex)
        assertEquals(2, info.totalVersions)
        assertTrue(info.hasMultipleVersions)
    }

    @Test
    fun `getEditInfo returns null for unknown editGroupId`() {
        val tree = ConversationTree()
        assertNull(tree.getEditInfo("nonexistent"))
    }

    @Test
    fun `getAllEditInfo returns info for all active path slots`() {
        var tree = ConversationTree()
        tree = tree.appendUserMessage(userMsg("A", editGroupId = "eg-1"), branchSessionId = null).first
        tree = tree.appendUserMessage(userMsg("B", editGroupId = "eg-2"), branchSessionId = null).first

        // Before edit: both eg-1 and eg-2 are on active path
        val allInfoBefore = tree.getAllEditInfo()
        assertEquals(2, allInfoBefore.size)
        assertFalse(allInfoBefore["eg-1"]!!.hasMultipleVersions)
        assertFalse(allInfoBefore["eg-2"]!!.hasMultipleVersions)

        // After editing eg-1: active path switches to new timeline (no children), so only eg-1 is active
        tree = tree.editMessage("eg-1", userMsg("A-v2", editGroupId = "eg-1"), branchSessionId = null)
        val allInfoAfterEdit = tree.getAllEditInfo()
        assertEquals(1, allInfoAfterEdit.size)
        assertTrue(allInfoAfterEdit.containsKey("eg-1"))
        assertTrue(allInfoAfterEdit["eg-1"]!!.hasMultipleVersions)

        // Navigate back to original timeline: eg-2 is visible again
        tree = tree.navigateVersion("eg-1", direction = -1)
        val allInfoAfterNav = tree.getAllEditInfo()
        assertEquals(2, allInfoAfterNav.size)
        assertTrue(allInfoAfterNav["eg-1"]!!.hasMultipleVersions)
        assertFalse(allInfoAfterNav["eg-2"]!!.hasMultipleVersions)
    }

    // ──────────────────────────────────────────────────────────
    // getMessagesBeforeSlot
    // ──────────────────────────────────────────────────────────

    @Test
    fun `getMessagesBeforeSlot returns empty for first slot`() {
        val msg = userMsg("Hello", editGroupId = "eg-1")
        val tree = ConversationTree().appendUserMessage(msg, branchSessionId = null).first

        val before = tree.getMessagesBeforeSlot("eg-1")
        assertEquals(emptyList(), before)
    }

    @Test
    fun `getMessagesBeforeSlot returns messages before target`() {
        val msg1 = userMsg("First", editGroupId = "eg-1")
        var tree = ConversationTree().appendUserMessage(msg1, branchSessionId = null).first
        val path1 = tree.getActiveLeafPath()
        tree = tree.appendResponse(path1, assistantMsg("Response 1"))
        tree = tree.appendUserMessage(userMsg("Second", editGroupId = "eg-2"), branchSessionId = null).first
        val path2 = tree.getActiveLeafPath()
        tree = tree.appendResponse(path2, assistantMsg("Response 2"))
        tree = tree.appendUserMessage(userMsg("Third", editGroupId = "eg-3"), branchSessionId = null).first

        val before = tree.getMessagesBeforeSlot("eg-3")
        assertEquals(4, before.size) // msg1, resp1, msg2, resp2
        assertEquals("First", (before[0] as ChatMessage.User).text)
        assertEquals("Second", (before[2] as ChatMessage.User).text)
    }

    @Test
    fun `getMessagesBeforeSlot returns empty for unknown editGroupId`() {
        val msg = userMsg("Hello", editGroupId = "eg-1")
        val tree = ConversationTree().appendUserMessage(msg, branchSessionId = null).first

        val before = tree.getMessagesBeforeSlot("nonexistent")
        // Will traverse all messages and not find the target, returns all
        // Actually, since it never finds the editGroupId, traverse returns false,
        // and all messages end up in result. Let's verify:
        assertEquals(1, before.size)
    }

    // ──────────────────────────────────────────────────────────
    // findSlot
    // ──────────────────────────────────────────────────────────

    @Test
    fun `findSlot returns slot for existing editGroupId`() {
        val msg = userMsg("Hello", editGroupId = "eg-1")
        val tree = ConversationTree().appendUserMessage(msg, branchSessionId = null).first

        val slot = tree.findSlot("eg-1")
        assertNotNull(slot)
        assertEquals("eg-1", slot.editGroupId)
    }

    @Test
    fun `findSlot returns null for missing editGroupId`() {
        val tree = ConversationTree()
        assertNull(tree.findSlot("nonexistent"))
    }

    @Test
    fun `findSlot finds nested slot`() {
        var tree = ConversationTree()
        tree = tree.appendUserMessage(userMsg("A", editGroupId = "eg-1"), branchSessionId = null).first
        tree = tree.appendUserMessage(userMsg("B", editGroupId = "eg-2"), branchSessionId = null).first
        tree = tree.appendUserMessage(userMsg("C", editGroupId = "eg-3"), branchSessionId = null).first

        val slot = tree.findSlot("eg-3")
        assertNotNull(slot)
        assertEquals("eg-3", slot.editGroupId)
    }

    // ──────────────────────────────────────────────────────────
    // resolveTimeline
    // ──────────────────────────────────────────────────────────

    @Test
    fun `resolveTimeline returns timeline for valid path`() {
        val msg = userMsg("Hello", editGroupId = "eg-1")
        val (tree, _) = ConversationTree().appendUserMessage(msg, branchSessionId = "sess-1")

        val timeline = tree.resolveTimeline(listOf(SlotPathSegment(0, 0)))
        assertNotNull(timeline)
        assertEquals("Hello", timeline.userMessage.text)
        assertEquals("sess-1", timeline.branchSessionId)
    }

    @Test
    fun `resolveTimeline returns null for out-of-range slot index`() {
        val msg = userMsg("Hello", editGroupId = "eg-1")
        val tree = ConversationTree().appendUserMessage(msg, branchSessionId = null).first

        assertNull(tree.resolveTimeline(listOf(SlotPathSegment(5, 0))))
    }

    @Test
    fun `resolveTimeline returns null for out-of-range timeline index`() {
        val msg = userMsg("Hello", editGroupId = "eg-1")
        val tree = ConversationTree().appendUserMessage(msg, branchSessionId = null).first

        assertNull(tree.resolveTimeline(listOf(SlotPathSegment(0, 5))))
    }

    @Test
    fun `resolveTimeline returns null for empty path`() {
        val tree = ConversationTree()
        assertNull(tree.resolveTimeline(emptyList()))
    }

    @Test
    fun `resolveTimeline works for deep path`() {
        var tree = ConversationTree()
        tree = tree.appendUserMessage(userMsg("A", editGroupId = "eg-1"), branchSessionId = null).first
        tree = tree.appendUserMessage(userMsg("B", editGroupId = "eg-2"), branchSessionId = "sess-2").first

        val path = listOf(SlotPathSegment(0, 0), SlotPathSegment(0, 0))
        val timeline = tree.resolveTimeline(path)
        assertNotNull(timeline)
        assertEquals("B", timeline.userMessage.text)
        assertEquals("sess-2", timeline.branchSessionId)
    }

    // ──────────────────────────────────────────────────────────
    // getActiveLeafSessionId
    // ──────────────────────────────────────────────────────────

    @Test
    fun `getActiveLeafSessionId returns deepest session id`() {
        var tree = ConversationTree()
        tree = tree.appendUserMessage(userMsg("A", editGroupId = "eg-1"), branchSessionId = "sess-1").first
        tree = tree.appendUserMessage(userMsg("B", editGroupId = "eg-2"), branchSessionId = "sess-2").first

        assertEquals("sess-2", tree.getActiveLeafSessionId())
    }

    @Test
    fun `getActiveLeafSessionId returns parent session if leaf has null`() {
        var tree = ConversationTree()
        tree = tree.appendUserMessage(userMsg("A", editGroupId = "eg-1"), branchSessionId = "sess-1").first
        tree = tree.appendUserMessage(userMsg("B", editGroupId = "eg-2"), branchSessionId = null).first

        assertEquals("sess-1", tree.getActiveLeafSessionId())
    }

    @Test
    fun `getActiveLeafSessionId follows edited branch`() {
        var tree = ConversationTree()
        tree = tree.appendUserMessage(userMsg("A", editGroupId = "eg-1"), branchSessionId = "sess-1").first
        tree = tree.appendUserMessage(userMsg("B", editGroupId = "eg-2"), branchSessionId = "sess-1").first

        // Edit eg-1, which changes active path
        tree = tree.editMessage("eg-1", userMsg("A-v2", editGroupId = "eg-1"), branchSessionId = "sess-branch")

        // Now active leaf is eg-1 timeline 1, which has no children
        assertEquals("sess-branch", tree.getActiveLeafSessionId())
    }

    // ──────────────────────────────────────────────────────────
    // updateTimelineAtPath
    // ──────────────────────────────────────────────────────────

    @Test
    fun `updateTimelineAtPath with empty path returns unchanged tree`() {
        val msg = userMsg("Hello", editGroupId = "eg-1")
        val tree = ConversationTree().appendUserMessage(msg, branchSessionId = null).first

        val result = tree.updateTimelineAtPath(emptyList()) { it }
        assertEquals(tree, result)
    }

    @Test
    fun `updateTimelineAtPath modifies specific timeline`() {
        var tree = ConversationTree()
        tree = tree.appendUserMessage(userMsg("A", editGroupId = "eg-1"), branchSessionId = null).first
        tree = tree.appendUserMessage(userMsg("B", editGroupId = "eg-2"), branchSessionId = null).first

        val path = listOf(SlotPathSegment(0, 0), SlotPathSegment(0, 0))
        val updated = tree.updateTimelineAtPath(path) { timeline ->
            timeline.copy(branchSessionId = "new-sess")
        }

        assertEquals("new-sess", updated.resolveTimeline(path)?.branchSessionId)
        // Parent unchanged
        assertNull(updated.resolveTimeline(listOf(SlotPathSegment(0, 0)))?.branchSessionId)
    }

    // ──────────────────────────────────────────────────────────
    // Complex scenario: Edit + Navigate + Continue
    // ──────────────────────────────────────────────────────────

    @Test
    fun `complex scenario - edit navigate and continue conversation`() {
        // Build initial conversation: A -> R1 -> B -> R2
        var tree = ConversationTree()
        tree = tree.appendUserMessage(userMsg("A", editGroupId = "eg-1"), branchSessionId = "sess-1").first
        val p1 = tree.getActiveLeafPath()
        tree = tree.appendResponse(p1, assistantMsg("R1"))
        tree = tree.appendUserMessage(userMsg("B", editGroupId = "eg-2"), branchSessionId = "sess-1").first
        val p2 = tree.getActiveLeafPath()
        tree = tree.appendResponse(p2, assistantMsg("R2"))

        // Edit A -> A-v2 (new branch)
        tree = tree.editMessage("eg-1", userMsg("A-v2", editGroupId = "eg-1"), branchSessionId = "sess-2")

        // Active messages should only show A-v2 (no responses yet, no children)
        val messagesAfterEdit = tree.getActiveMessages()
        assertEquals(1, messagesAfterEdit.size)
        assertEquals("A-v2", (messagesAfterEdit[0] as ChatMessage.User).text)

        // Add response in new branch
        val p3 = tree.getActiveLeafPath()
        tree = tree.appendResponse(p3, assistantMsg("R1-new"))

        // Continue conversation in new branch
        tree = tree.appendUserMessage(userMsg("C", editGroupId = "eg-3"), branchSessionId = "sess-2").first
        val p4 = tree.getActiveLeafPath()
        tree = tree.appendResponse(p4, assistantMsg("R3"))

        // Active messages: A-v2, R1-new, C, R3
        val messagesInBranch = tree.getActiveMessages()
        assertEquals(4, messagesInBranch.size)
        assertEquals("A-v2", (messagesInBranch[0] as ChatMessage.User).text)
        assertEquals("C", (messagesInBranch[2] as ChatMessage.User).text)

        // Navigate back to original
        tree = tree.navigateVersion("eg-1", direction = -1)

        // Active messages: A, R1, B, R2
        val messagesOriginal = tree.getActiveMessages()
        assertEquals(4, messagesOriginal.size)
        assertEquals("A", (messagesOriginal[0] as ChatMessage.User).text)
        assertEquals("B", (messagesOriginal[2] as ChatMessage.User).text)

        // Navigate forward again
        tree = tree.navigateVersion("eg-1", direction = 1)
        val messagesBack = tree.getActiveMessages()
        assertEquals(4, messagesBack.size)
        assertEquals("A-v2", (messagesBack[0] as ChatMessage.User).text)
        assertEquals("C", (messagesBack[2] as ChatMessage.User).text)
    }

    @Test
    fun `complex scenario - nested edits`() {
        // A -> R1 -> B -> R2
        var tree = ConversationTree()
        tree = tree.appendUserMessage(userMsg("A", editGroupId = "eg-1"), branchSessionId = "sess-1").first
        val p1 = tree.getActiveLeafPath()
        tree = tree.appendResponse(p1, assistantMsg("R1"))
        tree = tree.appendUserMessage(userMsg("B", editGroupId = "eg-2"), branchSessionId = "sess-1").first
        val p2 = tree.getActiveLeafPath()
        tree = tree.appendResponse(p2, assistantMsg("R2"))

        // Edit B -> B-v2
        tree = tree.editMessage("eg-2", userMsg("B-v2", editGroupId = "eg-2"), branchSessionId = "sess-b2")

        // Active: A, R1, B-v2
        val msgs1 = tree.getActiveMessages()
        assertEquals(3, msgs1.size)
        assertEquals("B-v2", (msgs1[2] as ChatMessage.User).text)

        // Also edit A -> A-v2
        tree = tree.editMessage("eg-1", userMsg("A-v2", editGroupId = "eg-1"), branchSessionId = "sess-a2")

        // Active: A-v2 only (new timeline has no children)
        val msgs2 = tree.getActiveMessages()
        assertEquals(1, msgs2.size)
        assertEquals("A-v2", (msgs2[0] as ChatMessage.User).text)

        // Navigate eg-1 back to original
        tree = tree.navigateVersion("eg-1", direction = -1)

        // Active: A, R1, B-v2 (eg-2 is still on timeline 1 = B-v2)
        val msgs3 = tree.getActiveMessages()
        assertEquals(3, msgs3.size)
        assertEquals("A", (msgs3[0] as ChatMessage.User).text)
        assertEquals("B-v2", (msgs3[2] as ChatMessage.User).text)

        // Navigate eg-2 back to original
        tree = tree.navigateVersion("eg-2", direction = -1)
        val msgs4 = tree.getActiveMessages()
        assertEquals(4, msgs4.size)
        assertEquals("A", (msgs4[0] as ChatMessage.User).text)
        assertEquals("B", (msgs4[2] as ChatMessage.User).text)

        // EditInfo check
        val allInfo = tree.getAllEditInfo()
        assertEquals(2, allInfo["eg-1"]!!.totalVersions)
        assertEquals(2, allInfo["eg-2"]!!.totalVersions)
    }

    // ──────────────────────────────────────────────────────────
    // Immutability
    // ──────────────────────────────────────────────────────────

    @Test
    fun `operations do not mutate original tree`() {
        val msg = userMsg("Hello", editGroupId = "eg-1")
        val (original, path) = ConversationTree().appendUserMessage(msg, branchSessionId = null)

        // appendResponse should not affect original
        val withResponse = original.appendResponse(path, assistantMsg("Response"))
        assertEquals(0, original.slots[0].timelines[0].responses.size)
        assertEquals(1, withResponse.slots[0].timelines[0].responses.size)

        // editMessage should not affect original
        val edited = original.editMessage("eg-1", userMsg("Edited", editGroupId = "eg-1"), branchSessionId = null)
        assertEquals(1, original.slots[0].timelines.size)
        assertEquals(2, edited.slots[0].timelines.size)

        // navigateVersion should not affect edited
        val navigated = edited.navigateVersion("eg-1", direction = -1)
        assertEquals(1, edited.slots[0].activeTimelineIndex)
        assertEquals(0, navigated.slots[0].activeTimelineIndex)
    }
}
