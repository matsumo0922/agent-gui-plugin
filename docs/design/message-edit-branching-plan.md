# Message Edit & Conversation Branching Design

## 1. Overview

ユーザーメッセージの編集機能を実装する。編集されたメッセージは、そのメッセージより前の会話コンテキストと共に Claude に送信され、新しい応答を得る。会話履歴は Git のブランチのようにツリー構造で管理し、過去の編集バージョンへのナビゲーションと、任意のブランチでの会話継続を可能にする。

## 2. Goals

1. ユーザーメッセージを編集し、新しい応答を取得できる
2. 編集履歴をツリー構造で保持し、左右矢印で任意のバージョンに切り替え可能
3. どのブランチでも会話を継続可能（古いブランチのセッションも維持）
4. 編集バージョンが存在しないメッセージではナビゲーション UI を非表示

## 3. Current State Analysis

### 3.1 Message Model (ChatUiState.kt)

```kotlin
// 現在: フラットなリスト
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    // ...
)

sealed interface ChatMessage {
    val id: String
    data class User(val id: String, val text: String, val attachedFiles: List<AttachedFile>) : ChatMessage
    data class Assistant(val id: String, val blocks: List<UiContentBlock>, val timestamp: Long) : ChatMessage
    data class Interrupted(val id: String, val timestamp: Long) : ChatMessage
}
```

### 3.2 SDK Session Capabilities

| Feature | Support | Notes |
|---------|---------|-------|
| マルチターン会話 | OK | `ClaudeSDKClient` で複数ターン |
| セッション分岐 (最新から) | OK | `forkSession = true` |
| セッション再開 | OK | `resumeSession(id)` |
| **特定メッセージからの分岐** | **NG** | SDK 未対応 (Feature Request: #16276) |
| **プログラム的 rewind** | **NG** | `/rewind` はインタラクティブのみ (#16976) |
| ファイルチェックポイント | OK | `rewindFiles(checkpointId)` (ファイルのみ、会話は巻き戻せない) |

### 3.3 Current ViewModel Architecture (単一セッション前提)

現行の `ChatViewModel` は以下が全てグローバル単位であり、ブランチ化に際して根本的な見直しが必要:

- `client: ClaudeSDKClient?` — 単一セッションクライアント
- `activeTurnJob: Job?` / `activeTurnId: Long` — ターン管理 (branch-aware でない)
- `subAgentTasks: Map<String, SubAgentTask>` — グローバルマップ (ブランチスコープなし)
- `pendingPermission` / `pendingQuestion` — グローバル state (どのセッション由来か不明)

### 3.4 Current Session Config Requirements

`ChatViewModel.connectSession()` で設定される必須項目（ブランチセッションでも同等の設定が必要）:

- `cwd = projectBasePath`
- `cliPath = claudeCodePath`
- `includePartialMessages = true`
- `env { CLAUDE_AGENT_SDK_SKIP_VERSION_CHECK = "1" }` (cliPath 設定時)
- `canUseTool { ... }` — permission コールバック
- `hooks { SUBAGENT_START / SUBAGENT_STOP }` — sub-agent lifecycle コールバック

### 3.5 UI (UserMessageBubble.kt)

既に以下が存在:
- 編集ボタン → `UserMessageEditBubble` を展開
- 左右矢印ボタン (未実装 / no-op)
- バージョン番号テキスト (ハードコード "1")
- `onEdit: (String) -> Unit` コールバック (未接続)

## 4. Data Model Design

### 4.1 ConversationTree: ツリー構造の会話管理

Git のコミットツリーに倣い、会話をツリー構造で管理する。各ユーザーメッセージの位置を「スロット」、各スロットの編集バージョンを「タイムライン」と呼ぶ。

```
Slot 0 ─── Timeline 0 (original) ─── Slot 1 ─── Timeline 0 (original)
                                              └── Timeline 1 (edit v2) ─── Slot 2 ─── Timeline 0
         └── Timeline 1 (edit v2) ─── Slot 1' ─── Timeline 0
```

#### Core Types

```kotlin
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
    val editGroupId: String,                // 同一位置の全タイムラインで共有する ID
    val timelines: List<Timeline>,          // 編集バージョンのリスト (1-indexed で表示)
    val activeTimelineIndex: Int = 0,       // 現在アクティブなタイムライン
)

/**
 * 1つの編集バージョンとその後の会話の流れ。
 * userMessage + responses が1ターン分、childSlots がその先の会話。
 */
@Immutable
data class Timeline(
    val userMessage: ChatMessage.User,
    val responses: List<ChatMessage> = emptyList(),  // Assistant / Interrupted
    val childSlots: List<MessageSlot> = emptyList(), // この先の会話
    val branchSessionId: String? = null,             // このブランチ用のセッション ID
)
```

### 4.2 SlotPath & Cursor: Streaming 応答の書込先特定

ツリーの immutable update を効率的に行うため、path-based のアドレッシングを導入する。
`editGroupId` ベースの探索は UI 操作（ナビゲーション等）に使い、
streaming 更新のような高頻度操作には path-based を使う。

```kotlin
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
 * ChatUiState に保持し、handleAssistantMessage() の書込先を明確にする。
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
```

#### Path-based Update

```kotlin
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
```

### 4.3 Active Path の取得

ツリーからフラットなメッセージリストを取得するユーティリティ:

```kotlin
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
 * 新しいメッセージ追加や streaming 書込先の特定に使用。
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
```

### 4.4 Edit Metadata の取得

各ユーザーメッセージの編集情報を UI に提供:

```kotlin
@Immutable
data class EditInfo(
    val editGroupId: String,
    val currentIndex: Int,      // 0-based
    val totalVersions: Int,     // timelines.size
    val hasMultipleVersions: Boolean,
)

fun ConversationTree.getEditInfo(editGroupId: String): EditInfo? {
    fun findSlot(slots: List<MessageSlot>): MessageSlot? {
        for (slot in slots) {
            if (slot.editGroupId == editGroupId) return slot
            for (timeline in slot.timelines) {
                findSlot(timeline.childSlots)?.let { return it }
            }
        }
        return null
    }
    val slot = findSlot(slots) ?: return null
    return EditInfo(
        editGroupId = slot.editGroupId,
        currentIndex = slot.activeTimelineIndex,
        totalVersions = slot.timelines.size,
        hasMultipleVersions = slot.timelines.size > 1,
    )
}

/**
 * アクティブパス上の全ユーザーメッセージの EditInfo を一括取得。
 * UI のレンダリングで使用。
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
```

### 4.5 ChatMessage.User への editGroupId 追加

```kotlin
data class User(
    override val id: String,
    val editGroupId: String,          // NEW: スロット識別用 (同一スロットの全バージョンで共有)
    val text: String,
    val attachedFiles: List<AttachedFile> = emptyList(),
) : ChatMessage
```

### 4.6 ChatUiState の変更 (過渡期互換を含む)

```kotlin
data class ChatUiState(
    val conversationTree: ConversationTree = ConversationTree(),
    val conversationCursor: ConversationCursor = ConversationCursor(),

    // --- 移行期間中のみ併存 (Step 11 で削除) ---
    val messages: List<ChatMessage> = emptyList(),

    val subAgentTasks: Map<String, SubAgentTask> = emptyMap(),
    val attachedFiles: List<AttachedFile> = emptyList(),
    val sessionState: SessionState = SessionState.Disconnected,
    val sessionId: String? = null,
    val model: Model = Model.SONNET,
    val permissionMode: PermissionMode = PermissionMode.DEFAULT,
    val contextUsage: Float = 0f,
    val totalInputTokens: Long = 0L,
    val totalCostUsd: Double = 0.0,
    val pendingPermission: PendingPermission? = null,
    val pendingQuestion: PendingQuestion? = null,
    val errorMessage: String? = null,
    val authOutputLines: List<String> = emptyList(),
) {
    /** アクティブパスのフラットメッセージリスト（UI 互換用） */
    val activeMessages: List<ChatMessage>
        get() = conversationTree.getActiveMessages()
}
```

### 4.7 SubAgentTask のブランチ紐付け

```kotlin
@Immutable
data class SubAgentTask(
    val id: String,
    val timelineSessionId: String? = null,   // NEW: どのブランチのタスクか
    val spawnedByToolName: String? = null,
    val messages: List<ChatMessage> = emptyList(),
    val startedAt: Long? = null,
    val completedAt: Long? = null,
)
```

## 5. Session Management Strategy

### 5.1 問題

SDK は特定メッセージ地点からのセッション分岐をサポートしていない。`forkSession` は最新状態からの分岐のみ。`/rewind` はインタラクティブ専用で SDK API として公開されていない。

### 5.2 採用するアプローチ: New Session + Context Reconstruction

編集時に新しいセッションを作成し、編集地点までの会話コンテキストをシステムプロンプトで復元する。

**Phase 1 (Current SDK):**

```
Edit at message N:
1. Create new session (createSession)
2. Set system prompt with conversation context up to message N-1
3. Send the edited user message as the first message
4. Receive new response
5. Store new session ID in the new Timeline
```

### 5.3 Context Reconstruction (強化版)

テキストのみではなく、ツール使用履歴・添付情報・ワークスペース差分を含めた構造化コンテキストを構築する。

```kotlin
fun buildContextSystemPrompt(
    messagesBeforeEdit: List<ChatMessage>,
    originalAttachedFiles: List<AttachedFile>,
): String {
    val sb = StringBuilder()
    sb.appendLine("""
        You are continuing a branched conversation after the user edited an earlier message.

        Known limitations:
        - Tool execution history/results may be incomplete.
        - Workspace file state may have diverged from the original branch.
        - If any missing context is required, ask a short clarifying question or re-run tools.

        Conversation history up to branch point:
    """.trimIndent())
    sb.appendLine()

    for (msg in messagesBeforeEdit) {
        when (msg) {
            is ChatMessage.User -> {
                sb.appendLine("[User]: ${msg.text}")
                if (msg.attachedFiles.isNotEmpty()) {
                    sb.appendLine("  (Attached files: ${msg.attachedFiles.joinToString { it.name }})")
                }
            }
            is ChatMessage.Assistant -> {
                val textParts = msg.blocks.filterIsInstance<UiContentBlock.Text>()
                    .joinToString("\n") { it.text }
                if (textParts.isNotBlank()) {
                    sb.appendLine("[Assistant]: $textParts")
                }

                // ツール使用の要約を含める
                val toolUses = msg.blocks.filterIsInstance<UiContentBlock.ToolUse>()
                for (tool in toolUses) {
                    sb.appendLine("  [Tool used: ${tool.toolName}]")
                    // ファイルパス等の主要パラメータを抽出
                    tool.inputJson["file_path"]?.let { path ->
                        sb.appendLine("    target: $path")
                    }
                    tool.inputJson["command"]?.let { cmd ->
                        sb.appendLine("    command: $cmd")
                    }
                }
            }
            is ChatMessage.Interrupted -> {
                sb.appendLine("[System: Response was interrupted]")
            }
        }
    }

    if (originalAttachedFiles.isNotEmpty()) {
        sb.appendLine()
        sb.appendLine("Previously attached files: ${originalAttachedFiles.joinToString { it.name }}")
    }

    sb.appendLine()
    sb.appendLine("Continue the conversation from here. The user will send their next message.")
    return sb.toString()
}
```

**Trade-offs:**
- **Pros**: SDK 変更不要、即座に実装可能、全ブランチで独立したセッション
- **Cons**: ツール使用結果の詳細が失われる（要約のみ）、トークン消費増加、ファイル変更状態の不整合

### 5.4 Phase 2 (将来の SDK 拡張時)

SDK に `rewind` API が公開された場合 (Issue #16976):

```
Edit at message N:
1. resumeSession(originalSessionId) with forkSession = true
2. Use rewind API to rewind to message N checkpoint
3. Send edited message
4. Full tool use history preserved
```

### 5.5 共通セッション設定の抽出

**重要**: ブランチセッションでも初期セッションと同等の設定が必要。
回帰を防ぐため、セッション設定を共通関数に抽出する。

```kotlin
/**
 * 全セッション（初期・ブランチ・再接続）で共通の設定を適用。
 * ChatViewModel.connectSession() の設定要件を一元化し、設定漏れによる回帰を防ぐ。
 */
private fun SessionOptionsBuilder.applyCommonConfig(
    model: Model,
    permissionMode: PermissionMode,
    projectBasePath: String,
    claudeCodePath: String?,
    permissionHandler: PermissionHandler,
    onSubAgentStart: (HookInput, String?, String?) -> Unit,
    onSubAgentStop: (HookInput, String?, String?) -> Unit,
) {
    this.model = model
    this.permissionMode = permissionMode
    this.cwd = projectBasePath
    claudeCodePath?.let { this.cliPath = it }
    this.includePartialMessages = true

    if (claudeCodePath != null) {
        env { put("CLAUDE_AGENT_SDK_SKIP_VERSION_CHECK", "1") }
    }

    canUseTool { toolName, input, _ ->
        permissionHandler.request(toolName, input)
    }

    hooks {
        on(HookEvent.SUBAGENT_START) { input, toolUseId, _ ->
            onSubAgentStart(input, toolUseId, null)
        }
        on(HookEvent.SUBAGENT_STOP) { input, _, _ ->
            onSubAgentStop(input, null, null)
        }
    }
}
```

### 5.6 BranchSessionManager

各ブランチのセッションを管理するコンポーネント:

```kotlin
class BranchSessionManager(
    private val projectBasePath: String,
    private val claudeCodePath: String?,
    private val permissionHandler: PermissionHandler,
    private val onSubAgentStart: (HookInput, String?, String?) -> Unit,
    private val onSubAgentStop: (HookInput, String?, String?) -> Unit,
) {
    // branchSessionId -> ClaudeSDKClient
    private val activeSessions = ConcurrentHashMap<String, ClaudeSDKClient>()

    /**
     * 編集による新しいブランチのセッションを作成。
     * messagesBeforeEdit: 編集対象メッセージより前の全メッセージ (active path)
     * originalAttachedFiles: 編集元メッセージの添付ファイル
     */
    suspend fun createEditBranchSession(
        messagesBeforeEdit: List<ChatMessage>,
        originalAttachedFiles: List<AttachedFile>,
        model: Model,
        permissionMode: PermissionMode,
    ): ClaudeSDKClient {
        val contextPrompt = buildContextSystemPrompt(messagesBeforeEdit, originalAttachedFiles)
        val client = ClaudeAgentSDK.createSession {
            applyCommonConfig(
                model = model,
                permissionMode = permissionMode,
                projectBasePath = projectBasePath,
                claudeCodePath = claudeCodePath,
                permissionHandler = permissionHandler,
                onSubAgentStart = onSubAgentStart,
                onSubAgentStop = onSubAgentStop,
            )
            this.systemPrompt = contextPrompt
        }
        client.connect()
        activeSessions[client.sessionId] = client
        return client
    }

    /**
     * 既存ブランチのセッションを取得（会話継続用）。
     * セッションが閉じている場合は resumeSession で再接続。
     */
    suspend fun getOrResumeSession(
        branchSessionId: String,
        model: Model,
        permissionMode: PermissionMode,
    ): ClaudeSDKClient {
        activeSessions[branchSessionId]?.let { return it }
        val client = ClaudeAgentSDK.resumeSession(branchSessionId) {
            applyCommonConfig(
                model = model,
                permissionMode = permissionMode,
                projectBasePath = projectBasePath,
                claudeCodePath = claudeCodePath,
                permissionHandler = permissionHandler,
                onSubAgentStart = onSubAgentStart,
                onSubAgentStop = onSubAgentStop,
            )
            forkSession = false  // 同じセッションを継続
        }
        client.connect()
        activeSessions[branchSessionId] = client
        return client
    }

    fun removeSession(sessionId: String) {
        activeSessions.remove(sessionId)?.close()
    }

    fun closeAll() {
        activeSessions.values.forEach { it.close() }
        activeSessions.clear()
    }
}
```

## 6. ConversationTree Operations

### 6.1 メッセージ追加 (通常の送信)

```kotlin
/**
 * アクティブパスの末尾に新しいユーザーメッセージスロットを追加。
 * cursor の activeLeafPath を更新して返す。
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
    // アクティブパスの末端タイムラインの childSlots に追加
    val leafPath = getActiveLeafPath()
    if (leafPath.isEmpty()) {
        // 最初のメッセージ
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
 * streaming 中の高頻度呼び出しに使用。
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
 * 指定パスのタイムラインの最後の応答 (Assistant) を更新。
 * streaming partial update 用: 同一 Assistant メッセージのブロック差し替え。
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
```

### 6.2 メッセージ編集

```kotlin
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
                activeTimelineIndex = slot.timelines.size, // 新タイムラインをアクティブに
            )
        }
    )
}
```

### 6.3 バージョンナビゲーション

```kotlin
fun ConversationTree.navigateVersion(
    editGroupId: String,
    direction: Int,  // -1 = left (older), +1 = right (newer)
): ConversationTree {
    return copy(
        slots = slots.updateSlot(editGroupId) { slot ->
            val newIndex = (slot.activeTimelineIndex + direction)
                .coerceIn(0, slot.timelines.lastIndex)
            slot.copy(activeTimelineIndex = newIndex)
        }
    )
}
```

### 6.4 編集対象より前のメッセージ取得

```kotlin
/**
 * editGroupId のスロットより前の、アクティブパス上のメッセージをフラットリストで返す。
 * コンテキスト復元のシステムプロンプト構築に使用。
 */
fun ConversationTree.getMessagesBeforeSlot(editGroupId: String): List<ChatMessage> {
    val result = mutableListOf<ChatMessage>()
    fun traverse(slots: List<MessageSlot>): Boolean {
        for (slot in slots) {
            if (slot.editGroupId == editGroupId) return true // 見つかった、ここで停止
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
```

### 6.5 ツリー操作の不変性

全ての操作は新しい `ConversationTree` を返す（immutable update）。Compose の状態管理と互換。

```kotlin
// editGroupId ベースの探索（UI 操作向け）
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
```

### 6.6 補助関数

```kotlin
/**
 * editGroupId に一致するスロットをツリーから検索。
 * 見つからない場合は null を返す (no-op 扱い)。
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
 * パスが不正（インデックス範囲外等）の場合は null を返す。
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
```

### 6.7 末端ブランチのセッション ID 取得

```kotlin
/**
 * アクティブパスの末端タイムラインの branchSessionId を返す。
 * ブランチ切替時のアクティブセッション特定に使用。
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
```

## 7. ViewModel Changes

### 7.1 Response Collection の抽出

現行 `sendMessage()` に内包されている応答収集ロジックを共通関数に抽出。
`sendMessage` と `editMessage` の両方から使用。

```kotlin
/**
 * session.receiveResponse() を収集し、ConversationTree を更新する。
 * turnId で stale 応答を無効化。path で書込先を指定。
 */
private suspend fun collectResponses(
    session: ClaudeSDKClient,
    turnId: Long,
    targetPath: SlotPath,
) {
    session.receiveResponse().collect { message ->
        if (turnId != activeTurnId) return@collect  // stale turn

        when (message) {
            is SystemMessage -> {
                updateState { it.copy(sessionId = message.sessionId) }
            }
            is StreamEvent -> {
                // token tracking
            }
            is AssistantMessage -> {
                handleAssistantMessage(message, targetPath)
            }
            is ResultMessage -> {
                handleResultMessage(message, turnId)
            }
            else -> { /* ignore */ }
        }
    }
}

/**
 * AssistantMessage を ConversationTree の指定パスに反映。
 *
 * Streaming 更新ルール:
 * - cursor.activeStreamingMessageId == messageId → updateLastResponse() で差し替え
 * - それ以外 → appendResponse() で新規追加し、cursor を更新
 */
private fun handleAssistantMessage(message: AssistantMessage, targetPath: SlotPath) {
    if (message.parentToolUseId != null) {
        handleSubAgentMessage(message)
        return
    }

    val blocks = message.content.mapNotNull { it.toUiBlockOrNull() }
    if (blocks.isEmpty()) return

    val messageId = message.uuid ?: UUID.randomUUID().toString()
    val assistantMessage = ChatMessage.Assistant(
        id = messageId,
        blocks = blocks,
    )

    updateState { state ->
        val cursor = state.conversationCursor
        val isSameStreaming = cursor.activeStreamingMessageId == messageId

        val newTree = if (isSameStreaming) {
            // 同一メッセージの partial update: 最後の応答を差し替え
            state.conversationTree.updateLastResponse(targetPath) { last ->
                if (last is ChatMessage.Assistant && last.id == messageId) assistantMessage else last
            }
        } else {
            // 新しいメッセージ: 追加
            state.conversationTree.appendResponse(targetPath, assistantMessage)
        }

        state.copy(
            conversationTree = newTree,
            conversationCursor = cursor.copy(activeStreamingMessageId = messageId),
        )
    }
}
```

### 7.2 ChatViewModel の主要変更点

```kotlin
class ChatViewModel(
    private val projectBasePath: String,
    private val claudeCodePath: String?,
    private val initialModel: Model,
    private val initialPermissionMode: PermissionMode,
) {
    private val branchSessionManager = BranchSessionManager(
        projectBasePath = projectBasePath,
        claudeCodePath = claudeCodePath,
        permissionHandler = permissionHandler,
        onSubAgentStart = ::handleSubAgentStart,
        onSubAgentStop = ::handleSubAgentStop,
    )

    // 現在アクティブなブランチのクライアント
    private var activeClient: ClaudeSDKClient? = null
    private var activeSessionId: String? = null

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val state = _uiState.value
        if (state.sessionState != SessionState.Ready &&
            state.sessionState != SessionState.WaitingForInput) return

        val editGroupId = UUID.randomUUID().toString()
        val userMessage = ChatMessage.User(
            id = UUID.randomUUID().toString(),
            editGroupId = editGroupId,
            text = text,
            attachedFiles = state.attachedFiles,
        )

        activeTurnJob?.cancel()
        val turnId = ++activeTurnId

        // 先に Processing に遷移し、ensureActiveClient() の suspend 中に
        // 他の操作 (edit/navigate) が入るのを canEditOrNavigate() でブロック
        updateState { it.copy(sessionState = SessionState.Processing) }

        activeTurnJob = vmScope.launch {
            try {
                // Lazy reconstruction: activeClient がない場合 (import 履歴からのナビゲーション等) は新規作成
                val client = ensureActiveClient(state)

                val (newTree, newPath) = state.conversationTree.appendUserMessage(
                    userMessage = userMessage,
                    branchSessionId = activeSessionId,
                )

                updateState {
                    it.copy(
                        conversationTree = newTree,
                        conversationCursor = ConversationCursor(activeLeafPath = newPath),
                        attachedFiles = emptyList(),
                    )
                }

                client.send(text)
                collectResponses(client, turnId, newPath)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Processing 先行遷移後のエラー復帰: WaitingForInput に戻す
                updateState {
                    it.copy(
                        sessionState = SessionState.WaitingForInput,
                        errorMessage = "Failed to send message: ${e.message}",
                    )
                }
            }
        }
    }

    /**
     * 現在のアクティブパス末端に適切なセッションを確保する。
     * Edge 10.10: branchSessionId == null な timeline からの送信をサポート。
     *
     * 重要: activeClient が存在しても、leafSessionId と activeSessionId が
     * 一致しない場合は再取得する（ブランチ切替後の旧セッション送信を防止）。
     */
    private suspend fun ensureActiveClient(state: ChatUiState): ClaudeSDKClient {
        val leafSessionId = state.conversationTree.getActiveLeafSessionId()

        // 既存の activeClient が現在のブランチと一致する場合はそのまま返す
        if (activeClient != null && activeSessionId == leafSessionId && leafSessionId != null) {
            return activeClient!!
        }

        return if (leafSessionId != null) {
            branchSessionManager.getOrResumeSession(
                branchSessionId = leafSessionId,
                model = state.model,
                permissionMode = state.permissionMode,
            ).also {
                activeClient = it
                activeSessionId = leafSessionId
                updateState { s -> s.copy(sessionId = leafSessionId) }
            }
        } else {
            // セッションが存在しない場合は新規作成 (初回接続と同等)
            val client = connectNewSession(state.model, state.permissionMode)
            activeClient = client
            activeSessionId = client.sessionId
            updateState { s -> s.copy(sessionId = client.sessionId) }
            client
        }
    }

    fun editMessage(editGroupId: String, newText: String) {
        if (newText.isBlank()) return
        val state = _uiState.value
        // Phase 1 安全制約: Processing / pending 中は編集不可
        if (!canEditOrNavigate(state)) return

        val tree = state.conversationTree

        // 編集内容が元と同一の場合は no-op
        val currentSlot = tree.findSlot(editGroupId) ?: return
        val currentTimeline = currentSlot.timelines[currentSlot.activeTimelineIndex]
        if (currentTimeline.userMessage.text == newText) return

        activeTurnJob?.cancel()
        val turnId = ++activeTurnId

        // 先に Processing に遷移し、createEditBranchSession() の suspend 中に
        // 他の操作 (edit/navigate/send) が入るのを canEditOrNavigate() でブロック
        updateState { it.copy(sessionState = SessionState.Processing) }

        activeTurnJob = vmScope.launch {
            try {
                // 1. 編集対象より前のメッセージを取得
                val messagesBeforeEdit = tree.getMessagesBeforeSlot(editGroupId)
                val originalAttachedFiles = currentTimeline.userMessage.attachedFiles

                // 2. 新しいブランチセッションを作成
                val newClient = branchSessionManager.createEditBranchSession(
                    messagesBeforeEdit = messagesBeforeEdit,
                    originalAttachedFiles = originalAttachedFiles,
                    model = state.model,
                    permissionMode = state.permissionMode,
                )

                // 3. 新しいユーザーメッセージを作成 (添付ファイルも引き継ぐ)
                val newUserMessage = ChatMessage.User(
                    id = UUID.randomUUID().toString(),
                    editGroupId = editGroupId,  // 同じ editGroupId を維持
                    text = newText,
                    attachedFiles = originalAttachedFiles,
                )

                // 4. ConversationTree を更新 (新タイムライン追加)
                val newTree = tree.editMessage(
                    editGroupId = editGroupId,
                    newUserMessage = newUserMessage,
                    branchSessionId = newClient.sessionId,
                )
                val newPath = newTree.getActiveLeafPath()

                updateState {
                    it.copy(
                        conversationTree = newTree,
                        conversationCursor = ConversationCursor(activeLeafPath = newPath),
                    )
                }

                // 5. activeClient を新ブランチに切り替え + UI sessionId 同期
                activeClient = newClient
                activeSessionId = newClient.sessionId
                updateState { it.copy(sessionId = newClient.sessionId) }

                // 6. 編集メッセージを送信
                newClient.send(newText)

                // 7. 応答を収集
                collectResponses(newClient, turnId, newPath)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Processing 先行遷移後のエラー復帰: WaitingForInput に戻す
                updateState {
                    it.copy(
                        sessionState = SessionState.WaitingForInput,
                        errorMessage = "Failed to edit message: ${e.message}",
                    )
                }
            }
        }
    }

    fun navigateEditVersion(editGroupId: String, direction: Int) {
        val state = _uiState.value
        // Phase 1 安全制約: Processing / pending 中はナビゲーション不可
        if (!canEditOrNavigate(state)) return

        val newTree = state.conversationTree.navigateVersion(editGroupId, direction)
        val newPath = newTree.getActiveLeafPath()
        val newSessionId = newTree.getActiveLeafSessionId()
        val needsSessionSwitch = newSessionId != null && newSessionId != activeSessionId
        val isNullSession = newSessionId == null

        updateState {
            it.copy(
                conversationTree = newTree,
                conversationCursor = ConversationCursor(activeLeafPath = newPath),
                // セッション切替が必要な場合は Connecting に遷移し、
                // sendMessage() を canEditOrNavigate() でブロック
                sessionState = if (needsSessionSwitch) SessionState.Connecting
                               else it.sessionState,
                // sessionId を常にアクティブブランチに同期 (null 含む)
                sessionId = newSessionId,
            )
        }

        // null session ブランチへの移動: activeClient をクリアし、
        // 次の sendMessage() で ensureActiveClient() が新規作成する (lazy reconstruction)
        if (isNullSession) {
            activeClient = null
            activeSessionId = null
            return
        }

        // アクティブセッションの非同期切り替え
        if (needsSessionSwitch) {
            vmScope.launch {
                try {
                    val client = branchSessionManager.getOrResumeSession(
                        branchSessionId = newSessionId!!,
                        model = state.model,
                        permissionMode = state.permissionMode,
                    )
                    activeClient = client
                    activeSessionId = newSessionId
                    updateState {
                        it.copy(
                            sessionId = newSessionId,  // UI の sessionId も同期
                            sessionState = SessionState.WaitingForInput,
                        )
                    }
                } catch (e: Exception) {
                    // 失敗時: activeClient/activeSessionId/sessionId を null 化し、
                    // WaitingForInput に戻す。次の sendMessage() で ensureActiveClient() がリカバリ。
                    // Error ではなく WaitingForInput にするのは、sendMessage() が
                    // Ready/WaitingForInput のみ許可するため、Error だとリカバリ不能になるため。
                    activeClient = null
                    activeSessionId = null
                    updateState {
                        it.copy(
                            sessionId = null,
                            sessionState = SessionState.WaitingForInput,
                            errorMessage = "Failed to switch branch session: ${e.message}",
                        )
                    }
                }
            }
        }
    }

    /**
     * Phase 1 安全制約: 編集/ナビゲーションが可能な状態かチェック。
     */
    private fun canEditOrNavigate(state: ChatUiState): Boolean {
        return state.sessionState != SessionState.Processing &&
               state.sessionState != SessionState.Connecting &&
               state.pendingPermission == null &&
               state.pendingQuestion == null
    }

    fun abortSession() {
        val currentTurnId = activeTurnId
        activeTurnId++

        vmScope.launch {
            activeClient?.interrupt()

            val interruptedMessage = ChatMessage.Interrupted(
                id = UUID.randomUUID().toString(),
            )

            updateState { state ->
                val path = state.conversationCursor.activeLeafPath
                state.copy(
                    conversationTree = state.conversationTree.appendResponse(path, interruptedMessage),
                    sessionState = SessionState.WaitingForInput,
                )
            }
        }
    }

    fun clear() {
        startJob?.cancel()
        activeTurnJob?.cancel()
        branchSessionManager.closeAll()
        activeClient = null
        activeSessionId = null

        _uiState.update {
            ChatUiState(
                model = it.model,
                permissionMode = it.permissionMode,
            )
        }
    }

    override fun dispose() {
        branchSessionManager.closeAll()
        vmScope.cancel()
    }
}
```

### 7.3 importHistory との整合

```kotlin
/**
 * フラットなメッセージリストから ConversationTree を構築。
 * セッション再開時の履歴インポート用。
 *
 * 重要: User メッセージ間は parent-child (childSlots) で連結する。
 * root siblings にしてはならない（getActiveLeafPath() や navigateVersion() が壊れるため）。
 */
fun buildConversationTreeFromFlatList(messages: List<ChatMessage>): ConversationTree {
    if (messages.isEmpty()) return ConversationTree()

    // メッセージを (User, [responses...]) のペアに分割
    data class Turn(val user: ChatMessage.User, val responses: MutableList<ChatMessage> = mutableListOf())
    val turns = mutableListOf<Turn>()

    for (msg in messages) {
        when (msg) {
            is ChatMessage.User -> turns.add(Turn(msg))
            else -> turns.lastOrNull()?.responses?.add(msg)
        }
    }

    if (turns.isEmpty()) return ConversationTree()

    // 末尾から再帰的にチェーン構造を構築
    fun buildChain(index: Int): List<MessageSlot> {
        if (index >= turns.size) return emptyList()
        val turn = turns[index]
        val childSlots = buildChain(index + 1)
        val timeline = Timeline(
            userMessage = turn.user,
            responses = turn.responses.toList(),
            childSlots = childSlots,
        )
        return listOf(
            MessageSlot(
                editGroupId = turn.user.editGroupId,
                timelines = listOf(timeline),
            )
        )
    }

    return ConversationTree(slots = buildChain(0))
}
```

## 8. UI Changes

### 8.1 ChatPanel

```kotlin
// ChatPanel.kt (変更箇所)
ChatMessageList(
    modifier = Modifier.fillMaxWidth().weight(1f),
    conversationTree = uiState.conversationTree,
    subAgentTasks = uiState.subAgentTasks,
    activeSessionId = uiState.sessionId,  // SubAgentTask フィルタ用
    canInteract = viewModel.canEditOrNavigate(uiState),
    project = project,
    onEdit = viewModel::editMessage,
    onNavigateVersion = viewModel::navigateEditVersion,
)
```

### 8.2 ChatMessageList

```kotlin
@Composable
fun ChatMessageList(
    conversationTree: ConversationTree,
    subAgentTasks: Map<String, SubAgentTask>,
    activeSessionId: String?,
    canInteract: Boolean,
    project: Project,
    onEdit: (editGroupId: String, newText: String) -> Unit,
    onNavigateVersion: (editGroupId: String, direction: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val messages = remember(conversationTree) {
        conversationTree.getActiveMessages()
    }
    val editInfoMap = remember(conversationTree) {
        conversationTree.getAllEditInfo()
    }

    // SubAgentTask をアクティブセッションでフィルタ
    val filteredTasks = remember(subAgentTasks, activeSessionId) {
        subAgentTasks.filter { it.value.timelineSessionId == activeSessionId || it.value.timelineSessionId == null }
    }

    LazyColumn(modifier = modifier) {
        items(messages, key = { it.id }) { message ->
            when (message) {
                is ChatMessage.User -> {
                    val editInfo = editInfoMap[message.editGroupId]
                    UserMessageBubble(
                        text = message.text,
                        attachedFiles = message.attachedFiles,
                        editInfo = editInfo,
                        canInteract = canInteract,
                        onEdit = { newText -> onEdit(message.editGroupId, newText) },
                        onNavigatePrev = { onNavigateVersion(message.editGroupId, -1) },
                        onNavigateNext = { onNavigateVersion(message.editGroupId, +1) },
                    )
                }
                is ChatMessage.Assistant -> {
                    AssistantMessageBlock(
                        blocks = message.blocks,
                        subAgentTasks = filteredTasks,
                        project = project,
                        timestamp = message.timestamp,
                    )
                }
                is ChatMessage.Interrupted -> {
                    DividerWithText("Response interrupted")
                }
            }
        }
    }
}
```

### 8.3 UserMessageBubble

```kotlin
@Composable
fun UserMessageBubble(
    text: String,
    attachedFiles: List<AttachedFile>,
    editInfo: EditInfo?,               // null = 単一バージョン (ナビゲーション非表示)
    canInteract: Boolean,              // false = 編集/ナビゲーション無効化
    onEdit: (String) -> Unit,
    onNavigatePrev: () -> Unit,
    onNavigateNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // ...
    UserMessageFooter(
        editInfo = editInfo,
        canInteract = canInteract,
        onEdit = { showEditor = true },
        onNavigatePrev = onNavigatePrev,
        onNavigateNext = onNavigateNext,
    )
}

@Composable
private fun UserMessageFooter(
    editInfo: EditInfo?,
    canInteract: Boolean,
    onEdit: () -> Unit,
    onNavigatePrev: () -> Unit,
    onNavigateNext: () -> Unit,
) {
    Row {
        // バージョンナビゲーション: 複数バージョンが存在する時のみ表示
        if (editInfo != null && editInfo.hasMultipleVersions) {
            IconButton(
                onClick = onNavigatePrev,
                enabled = canInteract && editInfo.currentIndex > 0,
            )
            Text("${editInfo.currentIndex + 1} / ${editInfo.totalVersions}")
            IconButton(
                onClick = onNavigateNext,
                enabled = canInteract && editInfo.currentIndex < editInfo.totalVersions - 1,
            )
        }

        // 編集ボタン (常に表示、Processing 中は無効化)
        IconButton(onClick = onEdit, enabled = canInteract)
        // コピーボタン (常に有効)
        IconButton(onClick = { /* copy */ })
    }
}
```

## 9. Migration Strategy

### 9.1 段階的な実装順序 (安全な移行)

```
Step 1:  ConversationTree データモデルの実装
         - ConversationTree, MessageSlot, Timeline, SlotPath, ConversationCursor の定義
         - getActiveMessages(), getActiveLeafPath(), getAllEditInfo() の実装
         - appendUserMessage(), appendResponse(), updateLastResponse() の実装
         - editMessage(), navigateVersion(), getMessagesBeforeSlot() の実装
         - updateTimelineAtPath(), updateSlot() の実装
         - buildConversationTreeFromFlatList() の実装
         - ユニットテスト

Step 2:  ChatMessage.User に editGroupId 追加
         - ChatUiState.kt: User data class に editGroupId フィールド追加
         - ChatViewModel.kt: sendMessage() 内の User 生成箇所に editGroupId を設定
         - TranscriptParser.kt: パース時の User 生成に editGroupId を設定
         - コンパイル確認

Step 3:  ChatUiState に conversationTree を追加（messages は一時併存）
         - conversationTree + conversationCursor フィールド追加
         - activeMessages computed property 追加
         - messages フィールドは残す（移行中）
         - コンパイル確認

Step 3.5: importHistory() の ConversationTree 対応（Step 4 の前提）
         - buildConversationTreeFromFlatList() の実装（チェーン構造）
         - importHistory() で conversationTree も同時更新するように修正
         - TabManager.resumeSession() の動作確認

Step 4:  ChatViewModel の送信/応答ロジックを tree 更新へ切替
         - collectResponses() の抽出・リファクタ
         - sendMessage() で conversationTree を更新
         - handleAssistantMessage() で conversationTree を更新
         - abortSession() で conversationTree を更新
         - UI は activeMessages を参照するように変更
         - 既存の messages 更新は残す（並行書込み）
         - 動作確認 (既存機能のリグレッションなし)

Step 5:  共通セッション設定の抽出
         - applyCommonConfig() の実装
         - connectSession() のリファクタ
         - コンパイル確認

Step 6:  BranchSessionManager の実装
         - createEditBranchSession()
         - getOrResumeSession()
         - closeAll(), removeSession()
         - buildContextSystemPrompt() (ツール要約含む強化版)
         - ChatViewModel に統合

Step 7:  editMessage の実装
         - ChatViewModel.editMessage() の実装
         - canEditOrNavigate() ガード条件
         - no-op チェック（同一テキスト）
         - 空文字チェック
         - 添付ファイル引き継ぎ
         - 動作確認

Step 8:  navigateEditVersion の実装
         - ChatViewModel.navigateEditVersion() の実装
         - アクティブセッションの切り替え
         - 動作確認

Step 8.5: sessionId 責務の統一（Step 9 の SubAgentTask フィルタの前提）
         - uiState.sessionId を「現在アクティブなブランチのセッション ID」として統一
         - activeSessionId (ViewModel 内部) と uiState.sessionId (UI 公開) を同期するルールを確立:
           editMessage / navigateEditVersion / sendMessage / ensureActiveClient で
           必ず uiState.sessionId も更新 (null 含む)
         - null session ブランチ移動時の activeClient/activeSessionId/sessionId クリア確認
         - テスト: importHistory → null session branch → sendMessage で新規セッション生成

Step 9:  SubAgentTask のブランチ紐付け
         - SubAgentTask に timelineSessionId 追加
         - startTailing / handleSubAgentMessage で設定
         - ChatMessageList でフィルタリング

Step 10: UI の接続
         - ChatPanel の修正 (conversationTree, callbacks を渡す)
         - ChatMessageList の修正 (editInfo, canInteract 連携)
         - UserMessageBubble / UserMessageFooter の修正 (ナビゲーション接続)

Step 11: messages フィールド完全削除
         - ChatUiState から messages を削除
         - 全参照箇所を activeMessages に切り替え
         - 最終動作確認

Step 12: Edge Cases & Polish
         - エラーハンドリング
         - セッションのクリーンアップ
         - UI の provenance 表示（Reconstructed Context バッジ）
```

## 10. Edge Cases

### 10.1 Processing 中の編集・ナビゲーション

```
状況: Claude が応答生成中にユーザーが過去のメッセージを編集 or バージョン切替しようとする
対策: canEditOrNavigate() でガード。sessionState == Processing / Connecting の間、
      または pendingPermission / pendingQuestion がある間は、
      編集ボタン・ナビゲーションボタンを無効化。
```

### 10.2 ブランチ切り替え時のセッション

```
状況: ブランチ A で会話中、ブランチ B に切り替え後、ブランチ B で会話を続行したい
対策: BranchSessionManager が全ブランチのセッションを保持。
      切り替え時に activeClient を対応するブランチのセッションに変更。
      セッションが閉じていた場合は resumeSession で再接続。
```

### 10.3 ブランチの末端タイムラインの特定

```
状況: ブランチ B の途中のメッセージを編集し、さらに新しいブランチ C が作成された場合、
      activeClient はどのセッションか？
対策: getActiveLeafSessionId() で末端タイムラインのセッションを取得。
      navigateVersion 時に末端セッションを再計算。
```

### 10.4 SubAgentTask のブランチ間スコープ

```
状況: ブランチ A の SubAgentTask はブランチ B では無関係
対策: SubAgentTask に timelineSessionId を追加。
      ChatMessageList 表示時に activeSessionId でフィルタリング。
      timelineSessionId == null のタスクは全ブランチで表示（レガシー互換）。
```

### 10.5 Permission/Question 待ち中のブランチ切替

```
状況: pendingPermission がグローバル state で、どのセッション由来か不明
対策: Phase 1 では canEditOrNavigate() で pending 中のナビゲーションを禁止。
      将来的には pendingPermission に sessionId を付与して branch-aware にする。
```

### 10.6 abortSession() の Interrupted 追加先

```
状況: 現行は messages に直接 append だが、ツリー化後はどこに入れるか
対策: conversationCursor.activeLeafPath を使って
      現在のアクティブタイムラインの responses に追加。
```

### 10.7 添付ファイル付きユーザーメッセージの編集

```
状況: 元メッセージに attachedFiles があるが、編集で失われる
対策: editMessage() で元メッセージの attachedFiles を新 User に引き継ぐ。
      Context Reconstruction のシステムプロンプトにも添付情報を含める。
      将来的には UserMessageEditBubble で添付ファイルの再編集も可能にする。
```

### 10.8 編集内容が元と同一 (no-op)

```
状況: ユーザーが編集したが、テキストを変更しなかった
対策: editMessage() の冒頭で元テキストと比較し、同一なら即 return。
```

### 10.9 空文字編集

```
状況: ユーザーが編集で空文字を送信しようとする
対策: editMessage() の冒頭で isBlank() チェック。
      UI 側でも UserMessageEditBubble の Send ボタンを無効化。
```

### 10.10 branchSessionId == null な timeline へのナビゲーション

```
状況: importHistory() で復元された履歴のタイムラインにはセッションがない
対策: navigateEditVersion() で新 sessionId が null の場合、
      activeClient / activeSessionId / uiState.sessionId を全て null にクリアする。
      次の sendMessage() 時に ensureActiveClient() が新セッションを作成する。
      (Lazy reconstruction パターン)
```

### 10.11 clear() / dispose() 時のクリーンアップ

```
対策: branchSessionManager.closeAll() で全セッションを閉じる。
      conversationTree を空にリセット。activeClient, activeSessionId を null に。
```

### 10.12 importHistory() との整合

```
状況: 既存の importHistory(messages: List<ChatMessage>) はフラットリスト前提
対策: buildConversationTreeFromFlatList() でフラットリストから ConversationTree を構築。
      importHistory() を内部で変換処理を呼ぶように修正。
```

## 11. Limitations & Future Work

### 11.1 Phase 1 の制限

- **ツール使用履歴の不完全さ**: 新セッションではツール結果 (Read, Grep 等) のコンテキストが失われる。ツール名・対象パス等の要約のみ復元される。
- **トークンコスト増加**: コンテキスト復元のためのシステムプロンプトが長くなる。
- **ファイル変更の不整合**: 編集前のブランチでファイルが変更されていた場合、新ブランチのセッションはその変更を認識しない。

### 11.2 Phase 1 の緩和策

1. **構造化 Context Reconstruction**: テキストのみでなく、ツール名・対象パス・添付ファイル情報を含める (Section 5.3)
2. **Reconstructed Context バッジ**: 編集ブランチの UI に「コンテキスト復元済み」バッジを表示し、ユーザーの期待値を調整
3. **再調査促進プロンプト**: システムプロンプトに「不足コンテキストがあれば再調査せよ」の指示を含める
4. **Lazy Session Creation**: セッションを編集時ではなく、該当ブランチで最初に送信する時に作成することで、不要なセッション生成を抑制（Phase 1.5 として検討）

### 11.3 Phase 2 で解決予定

SDK に以下が公開された場合に対応:
- **Programmatic rewind** (Issue #16976): セッションを特定メッセージ地点まで巻き戻し、フルコンテキストを保持した状態で分岐可能に。
- **Fork from specific message** (Issue #16276): 特定メッセージからのフォークが可能になれば、コンテキスト復元が不要に。

### 11.4 Session File Manipulation (検討中の代替案)

Claude Code セッションは `~/.claude/projects/{hash}/sessions/{sessionId}/` に JSONL 形式で保存されている。セッションファイルを直接操作（truncate して resume）することで、ツール使用履歴を保持した分岐が可能かもしれない。ただし:
- ファイル形式が undocumented
- CLI のバージョンアップで壊れるリスク
- 現時点では Phase 2 の代替として検討のみ
