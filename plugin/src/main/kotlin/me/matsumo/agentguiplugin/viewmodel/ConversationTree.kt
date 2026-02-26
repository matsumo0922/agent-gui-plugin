package me.matsumo.agentguiplugin.viewmodel

import androidx.compose.runtime.Immutable

/**
 * 会話ツリー全体を表すルートデータ構造。
 * slots は会話のトップレベルのユーザーメッセージスロットのリスト。
 */
@Immutable
data class ConversationTree(
    val slots: List<MessageSlot> = emptyList(),
)

/**
 * ユーザーメッセージの「位置」を表すノード。
 * 1つのスロットに複数のタイムライン（編集バージョン）が存在可能。
 */
@Immutable
data class MessageSlot(
    val editGroupId: String,
    val timelines: List<Timeline>,
    val activeTimelineIndex: Int = 0,
)

/**
 * 1つの編集バージョンとその後の会話の流れ。
 * userMessage + responses が1ターン分、childSlots がその先の会話。
 */
@Immutable
data class Timeline(
    val userMessage: ChatMessage.User,
    val responses: List<ChatMessage> = emptyList(),
    val childSlots: List<MessageSlot> = emptyList(),
    val branchSessionId: String? = null,
)

/**
 * ツリー内の特定タイムラインへのパスセグメント。
 */
@Immutable
data class SlotPathSegment(
    val slotIndex: Int,
    val timelineIndex: Int,
)

typealias SlotPath = List<SlotPathSegment>

/**
 * 現在の送信先・streaming 書込先を示すカーソル。
 *
 * Streaming 更新ルール:
 * - activeStreamingMessageId == null: 次の AssistantMessage は appendResponse() で新規追加
 * - activeStreamingMessageId == messageId: 同一 ID なら updateLastResponse() で差し替え
 * - activeStreamingMessageId != messageId: 新しいメッセージなので appendResponse() で追加し、ID を更新
 */
@Immutable
data class ConversationCursor(
    val activeLeafPath: SlotPath = emptyList(),
    val activeStreamingMessageId: String? = null,
)

/**
 * 各ユーザーメッセージの編集情報を UI に提供するデータクラス。
 */
@Immutable
data class EditInfo(
    val editGroupId: String,
    val currentIndex: Int,
    val totalVersions: Int,
    val hasMultipleVersions: Boolean,
)

// ──────────────────────────────────────────────────────────
// ConversationTree Operations
// ──────────────────────────────────────────────────────────

/**
 * path で指定されたタイムラインを transform する。
 * editGroupId ベースの全木探索より高速（O(depth)）。
 */
fun ConversationTree.updateTimelineAtPath(
    path: SlotPath,
    transform: (Timeline) -> Timeline,
): ConversationTree {
    if (path.isEmpty()) return this

    fun updateSlots(slots: List<MessageSlot>, remaining: SlotPath): List<MessageSlot> {
        val segment = remaining.first()
        return slots.mapIndexed { index, slot ->
            if (index != segment.slotIndex) slot
            else slot.copy(
                timelines = slot.timelines.mapIndexed { tIndex, timeline ->
                    if (tIndex != segment.timelineIndex) timeline
                    else if (remaining.size == 1) transform(timeline)
                    else timeline.copy(
                        childSlots = updateSlots(timeline.childSlots, remaining.drop(1))
                    )
                }
            )
        }
    }

    return copy(slots = updateSlots(slots, path))
}

/**
 * アクティブパスのフラットなメッセージリストを取得。
 */
fun ConversationTree.getActiveMessages(): List<ChatMessage> {
    val result = mutableListOf<ChatMessage>()
    fun traverse(slots: List<MessageSlot>) {
        for (slot in slots) {
            val timeline = slot.timelines[slot.activeTimelineIndex]
            result.add(timeline.userMessage)
            result.addAll(timeline.responses)
            traverse(timeline.childSlots)
        }
    }
    traverse(slots)
    return result
}

/**
 * アクティブパスの末端タイムラインへの SlotPath を返す。
 */
fun ConversationTree.getActiveLeafPath(): SlotPath {
    val path = mutableListOf<SlotPathSegment>()
    fun traverse(slots: List<MessageSlot>) {
        if (slots.isEmpty()) return
        val lastSlot = slots.last()
        path.add(SlotPathSegment(slots.lastIndex, lastSlot.activeTimelineIndex))
        traverse(lastSlot.timelines[lastSlot.activeTimelineIndex].childSlots)
    }
    traverse(slots)
    return path
}

/**
 * editGroupId に一致するスロットの EditInfo を返す。
 */
fun ConversationTree.getEditInfo(editGroupId: String): EditInfo? {
    val slot = findSlot(editGroupId) ?: return null
    return EditInfo(
        editGroupId = slot.editGroupId,
        currentIndex = slot.activeTimelineIndex,
        totalVersions = slot.timelines.size,
        hasMultipleVersions = slot.timelines.size > 1,
    )
}

/**
 * アクティブパス上の全ユーザーメッセージの EditInfo を一括取得。
 */
fun ConversationTree.getAllEditInfo(): Map<String, EditInfo> {
    val result = mutableMapOf<String, EditInfo>()
    fun traverse(slots: List<MessageSlot>) {
        for (slot in slots) {
            result[slot.editGroupId] = EditInfo(
                editGroupId = slot.editGroupId,
                currentIndex = slot.activeTimelineIndex,
                totalVersions = slot.timelines.size,
                hasMultipleVersions = slot.timelines.size > 1,
            )
            traverse(slot.timelines[slot.activeTimelineIndex].childSlots)
        }
    }
    traverse(slots)
    return result
}

/**
 * アクティブパスの末尾に新しいユーザーメッセージスロットを追加。
 * 新しいツリーとその新スロットへのパスを返す。
 */
fun ConversationTree.appendUserMessage(
    userMessage: ChatMessage.User,
    branchSessionId: String?,
): Pair<ConversationTree, SlotPath> {
    val newSlot = MessageSlot(
        editGroupId = userMessage.editGroupId,
        timelines = listOf(
            Timeline(
                userMessage = userMessage,
                branchSessionId = branchSessionId,
            )
        ),
    )
    val leafPath = getActiveLeafPath()
    if (leafPath.isEmpty()) {
        val newTree = copy(slots = slots + newSlot)
        val newPath = listOf(SlotPathSegment(0, 0))
        return newTree to newPath
    }

    val newTree = updateTimelineAtPath(leafPath) { timeline ->
        timeline.copy(childSlots = timeline.childSlots + newSlot)
    }
    val newPath = leafPath + SlotPathSegment(
        slotIndex = newTree.resolveTimeline(leafPath)!!.childSlots.lastIndex,
        timelineIndex = 0,
    )
    return newTree to newPath
}

/**
 * 指定パスのタイムラインの responses に応答を追加。
 */
fun ConversationTree.appendResponse(
    path: SlotPath,
    response: ChatMessage,
): ConversationTree {
    return updateTimelineAtPath(path) { timeline ->
        timeline.copy(responses = timeline.responses + response)
    }
}

/**
 * 指定パスのタイムラインの最後の応答を更新。
 * streaming partial update 用。
 */
fun ConversationTree.updateLastResponse(
    path: SlotPath,
    transform: (ChatMessage) -> ChatMessage,
): ConversationTree {
    return updateTimelineAtPath(path) { timeline ->
        if (timeline.responses.isEmpty()) timeline
        else timeline.copy(
            responses = timeline.responses.dropLast(1) + transform(timeline.responses.last())
        )
    }
}

/**
 * editGroupId に一致するスロットに新しいタイムラインを追加。
 * activeTimelineIndex を新タイムラインに設定。
 */
fun ConversationTree.editMessage(
    editGroupId: String,
    newUserMessage: ChatMessage.User,
    branchSessionId: String?,
): ConversationTree {
    return copy(
        slots = slots.updateSlot(editGroupId) { slot ->
            val newTimeline = Timeline(
                userMessage = newUserMessage,
                branchSessionId = branchSessionId,
            )
            slot.copy(
                timelines = slot.timelines + newTimeline,
                activeTimelineIndex = slot.timelines.size,
            )
        }
    )
}

/**
 * editGroupId のスロットの activeTimelineIndex を変更。
 */
fun ConversationTree.navigateVersion(
    editGroupId: String,
    direction: Int,
): ConversationTree {
    return copy(
        slots = slots.updateSlot(editGroupId) { slot ->
            val newIndex = (slot.activeTimelineIndex + direction)
                .coerceIn(0, slot.timelines.lastIndex)
            slot.copy(activeTimelineIndex = newIndex)
        }
    )
}

/**
 * editGroupId のスロットより前の、アクティブパス上のメッセージをフラットリストで返す。
 * コンテキスト復元のシステムプロンプト構築に使用。
 */
fun ConversationTree.getMessagesBeforeSlot(editGroupId: String): List<ChatMessage> {
    val result = mutableListOf<ChatMessage>()
    fun traverse(slots: List<MessageSlot>): Boolean {
        for (slot in slots) {
            if (slot.editGroupId == editGroupId) return true
            val timeline = slot.timelines[slot.activeTimelineIndex]
            result.add(timeline.userMessage)
            result.addAll(timeline.responses)
            if (traverse(timeline.childSlots)) return true
        }
        return false
    }
    traverse(slots)
    return result
}

/**
 * editGroupId に一致するスロットをツリーから検索。
 */
fun ConversationTree.findSlot(editGroupId: String): MessageSlot? {
    fun search(slots: List<MessageSlot>): MessageSlot? {
        for (slot in slots) {
            if (slot.editGroupId == editGroupId) return slot
            for (timeline in slot.timelines) {
                search(timeline.childSlots)?.let { return it }
            }
        }
        return null
    }
    return search(slots)
}

/**
 * SlotPath で指定されたタイムラインを取得。
 */
fun ConversationTree.resolveTimeline(path: SlotPath): Timeline? {
    var currentSlots = slots
    for ((i, segment) in path.withIndex()) {
        val slot = currentSlots.getOrNull(segment.slotIndex) ?: return null
        val timeline = slot.timelines.getOrNull(segment.timelineIndex) ?: return null
        if (i == path.lastIndex) return timeline
        currentSlots = timeline.childSlots
    }
    return null
}

/**
 * アクティブパスの末端タイムラインの branchSessionId を返す。
 */
fun ConversationTree.getActiveLeafSessionId(): String? {
    fun traverse(slots: List<MessageSlot>): String? {
        if (slots.isEmpty()) return null
        val lastSlot = slots.last()
        val timeline = lastSlot.timelines[lastSlot.activeTimelineIndex]
        return traverse(timeline.childSlots) ?: timeline.branchSessionId
    }
    return traverse(slots)
}

// ──────────────────────────────────────────────────────────
// Internal helpers
// ──────────────────────────────────────────────────────────

/**
 * editGroupId ベースの再帰的スロット更新。UI 操作向け。
 */
private fun List<MessageSlot>.updateSlot(
    editGroupId: String,
    transform: (MessageSlot) -> MessageSlot,
): List<MessageSlot> {
    return map { slot ->
        if (slot.editGroupId == editGroupId) {
            transform(slot)
        } else {
            slot.copy(
                timelines = slot.timelines.map { timeline ->
                    timeline.copy(
                        childSlots = timeline.childSlots.updateSlot(editGroupId, transform)
                    )
                }
            )
        }
    }
}
