# ChatViewModel 責務分離・問題点分析レポート (Claude)

## 1. エグゼクティブサマリー

`ChatViewModel`（881行）は、セッションライフサイクル、メッセージング、認証フロー、サブエージェント管理、ブランチナビゲーション、ファイル添付、トークン計算といった **少なくとも10の独立した責務** を単一クラスに集約している。これは Single Responsibility Principle (SRP) への明確な違反であり、以下の問題を引き起こしている:

- **テスト困難性**: 認証フローのテストにメッセージ送信の依存が必要
- **変更波及リスク**: サブエージェント管理の修正がセッション管理に影響しうる
- **認知的負荷**: 開発者が881行を通して全体像を把握する必要がある
- **スレッドセーフティの脆弱性**: 非スレッドセーフな `mutableMapOf` を複数スレッドから操作
- **Compose 非最適化**: 巨大な `ChatUiState` の部分更新が不要な ReComposition を誘発

## 2. 現状分析

### 2.1 責務の一覧と問題点

| # | 責務 | 行数(概算) | 問題 |
|---|------|-----------|------|
| 1 | セッションライフサイクル (start/clear/dispose/reconnect) | ~100行 | Mutex + disposed フラグ + 複数 Job の手動管理が複雑 |
| 2 | メッセージ送信・受信 (sendMessage + 4つの handle*) | ~130行 | `sendMessage` と `editMessage` で応答収集ロジックが重複 |
| 3 | 認証フロー (auth*系メソッド4つ + プロパティ4つ) | ~60行 | VM の責務と無関係な Process I/O 管理 |
| 4 | サブエージェントテイリング (startTailing/stopTailing/stopAllTailing) | ~75行 | hookId 解決は SDK 内部知識に依存 |
| 5 | hookToolUseId → parentToolUseId 解決 | ~40行 | SDK の stream-json プロトコル詳細を VM が知りすぎ |
| 6 | ConversationTree 操作 | ~散在 | ツリー操作自体は拡張関数で分離済みだが、VM 側の state.copy 呼び出しパターンが冗長 |
| 7 | ブランチ切替・セッション再接続 (navigateEditVersion) | ~70行 | client = null → 非同期再接続 → state 更新のフローが3箇所に分散 |
| 8 | ファイル添付 (attach/detach/buildContentBlocks) | ~30行 | 軽微だが他責務と混在 |
| 9 | コンテキストトークン計算 (handleStreamEvent) | ~15行 | マジックナンバー `200_000L` がハードコード |
| 10 | メッセージ編集・ブランチ作成 (editMessage) | ~75行 | sendMessage と応答収集ロジックが完全に重複 |

### 2.2 重複コードの特定

**最も深刻な重複: 応答収集ループ**

`sendMessage()` (L353-369) と `editMessage()` (L687-697) に全く同じ `when (message)` ブロックが存在する:

```kotlin
// sendMessage 内 (L356-368)
when (message) {
    is SystemMessage -> handleSystemMessage(message)
    is StreamEvent -> handleStreamEvent(message)
    is AssistantMessage -> handleAssistantMessage(message)
    is ResultMessage -> handleResultMessage(message)
    is UserMessage -> { /* ignore */ }
}

// editMessage 内 (L690-696) — 完全に同一
when (message) {
    is SystemMessage -> handleSystemMessage(message)
    is StreamEvent -> handleStreamEvent(message)
    is AssistantMessage -> handleAssistantMessage(message)
    is ResultMessage -> handleResultMessage(message)
    is UserMessage -> { /* ignore */ }
}
```

**重複パターン2: メッセージ送信**

```kotlin
// sendMessage 内
if (files.isEmpty()) session.send(text) else session.send(buildContentBlocks(text, files))

// editMessage 内
if (originalAttachedFiles.isEmpty()) newClient.send(newText) else newClient.send(buildContentBlocks(newText, originalAttachedFiles))
```

## 3. 責務分離の提案

### 3.1 アーキテクチャ概要

```
ChatViewModel (薄いオーケストレーター)
    ├── SessionCoordinator       ← セッションライフサイクル + client 管理
    │     ├── BranchSessionManager (既存)
    │     └── AuthFlowHandler    ← 認証フロー
    ├── MessageDispatcher        ← メッセージ送受信 + 応答収集
    ├── SubAgentCoordinator      ← テイリング管理 + hookId 解決
    └── UsageTracker             ← トークン計算・コスト管理
```

### 3.2 各コンポーネントの設計

#### A. `SessionCoordinator` — セッションライフサイクルの一元管理

```kotlin
class SessionCoordinator(
    private val projectBasePath: String,
    private val claudeCodePath: String?,
    private val configApplier: SessionOptionsBuilder.(Model, PermissionMode) -> Unit,
) {
    private val _activeClient = MutableStateFlow<ClaudeSDKClient?>(null)
    val activeClient: StateFlow<ClaudeSDKClient?> = _activeClient.asStateFlow()

    private val _sessionState = MutableStateFlow(SessionState.Disconnected)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private val _sessionId = MutableStateFlow<String?>(null)
    val sessionId: StateFlow<String?> = _sessionId.asStateFlow()

    val branchSessionManager = BranchSessionManager(configApplier)

    suspend fun connect(
        model: Model,
        permissionMode: PermissionMode,
        resumeSessionId: String? = null,
    ): Result<ClaudeSDKClient>

    suspend fun switchToBranch(
        branchSessionId: String,
        model: Model,
        permissionMode: PermissionMode,
    ): Result<ClaudeSDKClient>

    fun clearActiveClient()

    suspend fun closeAll()
}
```

**メリット**: `client` の nullable 管理と `SessionState` 遷移が1箇所に集約される。

#### B. `AuthFlowHandler` — 認証フロー

```kotlin
class AuthFlowHandler(private val scope: CoroutineScope) {
    data class AuthState(
        val isActive: Boolean = false,
        val outputLines: List<String> = emptyList(),
    )

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    suspend fun startAuth(process: Process, stdout: BufferedReader, stdin: OutputStreamWriter, initialOutput: List<String>)
    fun sendInput(text: String)
    suspend fun confirmComplete()
    fun cleanup()
}
```

**メリット**: Process I/O 管理が VM から完全に分離。テスト時に Process のモックが容易。

#### C. `MessageDispatcher` — メッセージ送受信の統一

```kotlin
class MessageDispatcher(
    private val scope: CoroutineScope,
    private val onSystemMessage: (SystemMessage) -> Unit,
    private val onAssistantMessage: (AssistantMessage) -> Unit,
    private val onStreamEvent: (StreamEvent) -> Unit,
    private val onResult: (ResultMessage) -> Unit,
) {
    private val activeTurnId = AtomicLong(0L)
    private var activeTurnJob: Job? = null

    /**
     * メッセージを送信し、応答を収集する統一メソッド。
     * sendMessage と editMessage の共通ロジック。
     */
    fun dispatch(
        client: ClaudeSDKClient,
        text: String,
        files: List<AttachedFile> = emptyList(),
    ): Job

    fun invalidateCurrentTurn(): Long  // abortSession 用
    fun cancel()
}
```

**メリット**: 応答収集の `when` ブロック重複が完全に解消。`activeTurnId` 管理が局所化。

#### D. `SubAgentCoordinator` — サブエージェント管理

```kotlin
class SubAgentCoordinator(private val scope: CoroutineScope) {
    // hookToolUseId → parentToolUseId のマッピングを内部管理
    private val keyResolver = HookIdResolver()

    private val _tasks = MutableStateFlow<Map<String, SubAgentTask>>(emptyMap())
    val tasks: StateFlow<Map<String, SubAgentTask>> = _tasks.asStateFlow()

    fun onSubAgentStart(agentId: String, transcriptPath: String, hookToolUseId: String, sessionId: String?)
    fun onSubAgentStop(agentId: String)
    fun resolveParentToolUseId(hookId: String, realId: String)
    fun updateSpawnedByToolName(parentToolUseId: String, toolName: String)
    fun stopAll()
}
```

**メリット**: `activeTailers`, `tailerKeyRefs`, `agentKeyRefs`, `unresolvedHookIds` の4つの mutable コレクションが VM から消える。

#### E. `UsageTracker` — トークン・コスト追跡

```kotlin
class UsageTracker {
    data class Usage(
        val contextUsage: Float = 0f,
        val totalInputTokens: Long = 0L,
        val totalCostUsd: Double = 0.0,
    )

    private val _usage = MutableStateFlow(Usage())
    val usage: StateFlow<Usage> = _usage.asStateFlow()

    fun onMessageStart(inputTokens: Long, cacheCreation: Long, cacheRead: Long)
    fun onResult(totalCostUsd: Double?)

    companion object {
        // モデルごとの context window をここで管理
        const val DEFAULT_CONTEXT_WINDOW = 200_000L
    }
}
```

**メリット**: マジックナンバー `200_000L` が定数化。将来的にモデル別の context window 対応が容易。

### 3.3 リファクタリング後の ChatViewModel

```kotlin
class ChatViewModel(
    private val projectBasePath: String,
    private val claudeCodePath: String?,
    private val initialModel: Model,
    private val initialPermissionMode: PermissionMode,
) {
    private val vmScope = CoroutineScope(SupervisorJob())

    // --- Delegates ---
    private val sessionCoordinator = SessionCoordinator(projectBasePath, claudeCodePath, ::applyCommonConfig)
    private val authFlowHandler = AuthFlowHandler(vmScope)
    private val messageDispatcher = MessageDispatcher(vmScope, ...)
    private val subAgentCoordinator = SubAgentCoordinator(vmScope)
    private val usageTracker = UsageTracker()
    private val permissionHandler = PermissionHandler(...)

    // --- UI State (Combine で合成) ---
    val uiState: StateFlow<ChatUiState> = combine(
        conversationState,
        sessionCoordinator.sessionState,
        sessionCoordinator.sessionId,
        subAgentCoordinator.tasks,
        usageTracker.usage,
        authFlowHandler.state,
        permissionHandler.state,
        attachedFilesFlow,
    ) { ... }.stateIn(vmScope, SharingStarted.Eagerly, ChatUiState())

    // --- Public API (thin delegation) ---
    fun sendMessage(text: String) { ... }
    fun editMessage(editGroupId: String, newText: String) { ... }
    fun navigateEditVersion(editGroupId: String, direction: Int) { ... }
    suspend fun start(resumeSessionId: String? = null) { ... }
    suspend fun clear() { ... }
    fun dispose() { ... }
}
```

## 4. コードリーダビリティの改善提案

### 4.1 `_uiState.update` の散在

現状、`_uiState.update { it.copy(...) }` が **30箇所以上** に散在しており、状態遷移の全体像が把握困難。

**改善**: 状態遷移を sealed interface で宣言的に管理

```kotlin
private sealed interface StateAction {
    data class SessionConnected(val sessionId: String) : StateAction
    data class MessageReceived(val message: ChatMessage.Assistant) : StateAction
    data class TurnCompleted(val usage: UsageTracker.Usage, val errorMessage: String?) : StateAction
    data object Processing : StateAction
    // ...
}

private fun reduce(state: ChatUiState, action: StateAction): ChatUiState = when (action) {
    is StateAction.SessionConnected -> state.copy(
        sessionState = SessionState.Ready,
        sessionId = action.sessionId,
    )
    // ...
}
```

### 4.2 `handleAssistantMessage` の複雑な条件分岐

```kotlin
// 現状: 「同一メッセージかどうか」の判定が分かりにくい
val isSameStreaming = cursor.activeStreamingMessageId == messageId
val newTree = if (isSameStreaming) { ... } else { ... }
```

**改善**: 意図を明確にするヘルパー

```kotlin
private fun ConversationTree.upsertAssistantMessage(
    path: SlotPath,
    message: ChatMessage.Assistant,
    streamingId: String?,
): Pair<ConversationTree, String> {
    return if (streamingId == message.id) {
        updateLastResponse(path) { last ->
            if (last is ChatMessage.Assistant && last.id == message.id) message else last
        } to message.id
    } else {
        appendResponse(path, message) to message.id
    }
}
```

### 4.3 メソッド長の問題

- `editMessage()`: 73行 — 7つのステップがコメントで区切られている。各ステップが独立しているため、ヘルパーに切り出し可能
- `navigateEditVersion()`: 67行 — session switch の分岐が深い
- `startTailing()`: 50行 — keyRef セットアップ + state 初期化 + tailer 起動が混在

## 5. Kotlin/Coroutines の活用提案

### 5.1 `activeTurnJob` の手動管理 → `MutableSharedFlow` ベースのイベント駆動

現状の `activeTurnJob?.cancel()` + `activeTurnJob = vmScope.launch { ... }` パターンは Job のライフサイクル管理が手動的。

```kotlin
// 改善案: collectLatest で自動キャンセル
private val sendRequests = MutableSharedFlow<SendRequest>(extraBufferCapacity = 1)

init {
    vmScope.launch {
        sendRequests.collectLatest { request ->
            // 前の collect が自動的にキャンセルされる
            executeMessageTurn(request)
        }
    }
}

fun sendMessage(text: String) {
    sendRequests.tryEmit(SendRequest.Normal(text, files))
}

fun editMessage(editGroupId: String, newText: String) {
    sendRequests.tryEmit(SendRequest.Edit(editGroupId, newText))
}
```

### 5.2 `disposed` フラグ + `currentCoroutineContext().isActive` → structured concurrency

```kotlin
// 現状: 防御的チェックが散在
if (disposed) return
// ...
if (disposed || !currentCoroutineContext().isActive) {
    localClient.close()
    return
}
```

**改善**: `vmScope.cancel()` に頼る structured concurrency。`disposed` フラグは `vmScope.isActive` で代替可能。

```kotlin
private inline fun ifActive(block: () -> Unit) {
    if (vmScope.isActive) block()
}
```

### 5.3 `runCatching` の活用

```kotlin
// 現状 (cleanupAuthProcess)
runCatching { authStdin?.close() }
runCatching { authStdout?.close() }

// 既に使われているが、connectSession の try-catch はより Kotlin 的に書ける:
suspend fun connectSession(resumeSessionId: String?) = runCatching {
    // ...
}.onFailure { e ->
    if (e is CancellationException) throw e
    _uiState.update { it.copy(sessionState = SessionState.Error, errorMessage = e.message) }
}
```

### 5.4 `Mutex` + `startJob` パターン → `Channel` ベース

```kotlin
// 現状の start() は Mutex + disposed チェック + startJob?.isActive チェック + SessionState チェックの4重ガード
// Channel(CONFLATED) + select でシンプル化可能
```

## 6. Compose ReComposition への配慮

### 6.1 巨大な `ChatUiState` の問題

現在の `ChatUiState` は **18フィールド** を持つ。`sessionState` の変更（頻繁）が `conversationTree`（巨大）を含む全体の再評価を引き起こす。

**改善**: State の分割

```kotlin
// 1. 頻繁に変わるもの
data class SessionStatus(
    val state: SessionState,
    val sessionId: String?,
    val errorMessage: String?,
)

// 2. ターン完了時に変わるもの
data class UsageInfo(
    val contextUsage: Float,
    val totalInputTokens: Long,
    val totalCostUsd: Double,
)

// 3. ユーザー操作で変わるもの
data class ConversationState(
    val tree: ConversationTree,
    val cursor: ConversationCursor,
)

// 4. 独立したもの
// subAgentTasks, pendingPermission, pendingQuestion, authOutputLines
```

### 6.2 `activeMessages` computed property の問題

```kotlin
val activeMessages: List<ChatMessage>
    get() = conversationTree.getActiveMessages()
```

これは `ChatUiState` が変更されるたびに（sessionState の変更でも）再計算される。`remember` + `derivedStateOf` で UI 側でキャッシュすべき。

```kotlin
// UI 側
val messages by remember { derivedStateOf { uiState.conversationTree.getActiveMessages() } }
```

### 6.3 `SubAgentTask.messages` のリスト更新

ストリーミング中、`subAgentTasks` の1タスクの `messages` が頻繁に更新される。これが `Map` 全体のコピーを引き起こす。

**改善**: `SubAgentTask` 単位の `StateFlow` を持ち、UI 側で個別に collect する。

## 7. スレッドセーフティの問題と改善

### 7.1 非スレッドセーフなコレクション

```kotlin
// 問題: mutableMapOf は ConcurrentHashMap ではない
private val activeTailers = mutableMapOf<String, TranscriptTailer>()
private val tailerKeyRefs = mutableMapOf<String, AtomicReference<String>>()
private val agentKeyRefs = mutableMapOf<String, AtomicReference<String>>()
private val unresolvedHookIds = mutableListOf<String>()
```

`startTailing` は `applyCommonConfig` のフック（SDK スレッド）から呼ばれ、`stopAllTailing` は VM のメインスレッドから呼ばれる。これらは同時アクセスの可能性がある。

**改善**:
```kotlin
private val activeTailers = ConcurrentHashMap<String, TranscriptTailer>()
// または SubAgentCoordinator に移して内部で同期
```

### 7.2 `client` の volatile だけでは不十分

```kotlin
@Volatile
private var client: ClaudeSDKClient? = null
```

`client` の読み書きは volatile で可視性が保証されるが、`navigateEditVersion` での `client = null` → 非同期再接続 → `client = newClient` のシーケンスでは race condition が起きうる。

**改善**: `AtomicReference<ClaudeSDKClient?>` + `compareAndSet`、または `SessionCoordinator` に集約して内部で `Mutex` 管理。

### 7.3 `lastTurnInputTokens` の非 volatile

```kotlin
private var lastTurnInputTokens = 0L
```

`handleStreamEvent` と `handleResultMessage` は同一コルーチンから呼ばれるので実際には問題ないが、意図が不明確。`UsageTracker` に移して明示的に thread-confined にすべき。

## 8. SDK への移行候補

### 8.1 hookToolUseId → parentToolUseId 解決（最優先）

`handleSubAgentAssistantMessage` 内の以下のロジックは SDK の stream-json プロトコルの内部詳細に依存:

```kotlin
if (!_uiState.value.subAgentTasks.containsKey(pid) && unresolvedHookIds.isNotEmpty()) {
    val hookId = unresolvedHookIds.removeFirst()
    tailerKeyRefs[hookId]?.set(pid)
    // Re-key the SubAgentTask from hookId to pid
    ...
}
```

**提案**: SDK が `AssistantMessage` に `resolvedHookToolUseId: String?` フィールドを追加するか、`SubAgentMessage` のような専用型を提供する。

### 8.2 コンテンツブロック構築

`buildContentBlocks` は SDK の JSON プロトコル仕様に依存:

```kotlin
buildJsonObject {
    put("type", "text")
    put("text", text)
}
```

**提案**: SDK に `ContentBlockBuilder` DSL を追加。

### 8.3 token usage の解析

`handleStreamEvent` での `message_start` イベント解析は SDK が型安全に提供すべき情報:

```kotlin
val usage = event.event["message"]?.jsonObject?.get("usage")?.jsonObject ?: return
```

**提案**: SDK が `StreamEvent.MessageStart(inputTokens, cacheCreation, cacheRead)` のような型付きイベントを提供。

## 9. 優先度付き改善ロードマップ

### Phase 1: 即座に対応（低リスク・高効果）
| 項目 | 効果 | 工数 |
|------|------|------|
| スレッドセーフでないコレクションの修正 | バグ防止 | S |
| 応答収集ループの共通化 (`collectResponses`) | 重複除去 | S |
| `200_000L` の定数化 | 可読性 | XS |
| `AuthFlowHandler` の切り出し | 責務分離 | M |

### Phase 2: 構造改善（中リスク・高効果）
| 項目 | 効果 | 工数 |
|------|------|------|
| `SubAgentCoordinator` の導入 | 責務分離 + スレッド安全 | M |
| `MessageDispatcher` の導入 | 重複除去 + activeTurnId 局所化 | M |
| `UsageTracker` の切り出し | 責務分離 | S |
| `ChatUiState` の分割 | ReComposition 最適化 | M |

### Phase 3: アーキテクチャ刷新（高リスク・高効果）
| 項目 | 効果 | 工数 |
|------|------|------|
| `SessionCoordinator` の導入 | client 管理の一元化 | L |
| SDK への hookId 解決移行 | 責務の正しい配置 | L (SDK 変更必要) |
| `collectLatest` ベースのイベント駆動化 | Job 管理の簡素化 | M |
| State reducer パターンの導入 | 状態遷移の宣言的管理 | L |

### Phase 4: 長期的改善
| 項目 | 効果 | 工数 |
|------|------|------|
| SDK に ContentBlockBuilder DSL を追加 | 型安全性 | M (SDK 変更) |
| SDK に型付き StreamEvent を追加 | 型安全性 | M (SDK 変更) |
| ConversationTree を persistent data structure 化 | パフォーマンス | L |

---

*レポート作成: Claude / 2026-02-27*
