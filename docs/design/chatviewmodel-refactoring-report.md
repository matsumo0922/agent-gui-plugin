# ChatViewModel リファクタリング分析 — 統合レポート

> Claude と Codex (OpenAI) による並列調査 + 相互レビュー + 最終レビューの結果を統合。
> 2026-02-27 (Rev.2: Codex 最終レビュー反映)

---

## 1. エグゼクティブサマリー

`ChatViewModel`（881行）は **少なくとも10の独立した責務** を単一クラスに集約しており、SRP 違反、テスト困難性、変更波及リスク、スレッドセーフティの脆弱性を引き起こしている。

特に深刻な問題:
- **バグ直結**: hookToolUseId の FIFO 解決による誤マッピングリスク、`client` ライフサイクル漏れ、PermissionHandler の型検証不足、TranscriptTailer の partial line 欠落
- **設計負債**: sendMessage/editMessage の応答収集ロジック完全重複、30箇所以上に散在する `_uiState.update`、非スレッドセーフなコレクション
- **Compose 非最適化**: 18フィールドの巨大な `ChatUiState` による不要な ReComposition

改善の主軸: **バグ修正を最優先** → VM を薄いオーケストレーターに縮小 → 状態管理の最適化

---

## 2. 現状分析

### 2.1 責務の一覧と問題点

| # | 責務 | 行数(概算) | 深刻度 | 問題 |
|---|------|-----------|--------|------|
| 1 | セッションライフサイクル | ~100行 | 中 | Mutex + disposed + 複数 Job の手動管理が複雑 |
| 2 | メッセージ送受信 | ~130行 | 高 | sendMessage/editMessage で応答収集ロジックが **完全重複** |
| 3 | 認証フロー | ~60行 | 中 | VM の責務と無関係な Process I/O 管理 |
| 4 | サブエージェントテイリング | ~75行 | 高 | 非スレッドセーフなコレクション + hookId FIFO 解決のバグリスク |
| 5 | hookId → parentToolUseId 解決 | ~40行 | 高 | SDK 内部知識への依存 + FIFO 前提の脆弱性 |
| 6 | ConversationTree 操作 | ~散在 | 低 | 拡張関数で分離済みだが state.copy パターンが冗長 |
| 7 | ブランチ切替・再接続 | ~70行 | 高 | client = null → 再接続フローが分散、旧 client 未 close |
| 8 | ファイル添付 | ~30行 | 低 | 他責務と混在するが軽微 |
| 9 | トークン計算 | ~15行 | 低 | マジックナンバー `200_000L` がハードコード |
| 10 | メッセージ編集 | ~75行 | 中 | sendMessage との重複が主な問題 |

### 2.2 重複コードの特定

**最も深刻な重複: 応答収集ループ**

`sendMessage()` (L353-369) と `editMessage()` (L687-697) に **全く同じ** `when (message)` ブロックが存在:

```kotlin
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
// editMessage 内 — 同一パターン
if (originalAttachedFiles.isEmpty()) newClient.send(newText) else newClient.send(...)
```

---

## 3. バグ・脆弱性の指摘（P0: 即時修正対象）

### 3.1 hookToolUseId の FIFO 解決によるマッピング誤り `Confirmed`

**箇所**: `ChatViewModel.kt:448-449`

```kotlin
if (!_uiState.value.subAgentTasks.containsKey(pid) && unresolvedHookIds.isNotEmpty()) {
    val hookId = unresolvedHookIds.removeFirst()  // ← FIFO 前提
```

**問題**: 複数のサブエージェントが並列起動した場合、FIFO 順に `removeFirst()` するだけでは、hookToolUseId と parentToolUseId の対応が保証されない。順序が入れ替わると誤ったタスクにメッセージが紐づく。

**改善案**: SDK 側で hookToolUseId と parentToolUseId の対応を提供するか、hook コールバックに parentToolUseId を含める。Plugin 側の暫定対策として、agentId をキーにした Map での解決を検討。

### 3.2 `client` ライフサイクル漏れ（条件付き） `Partial`

**箇所**: `ChatViewModel.kt:676` (editMessage), `ChatViewModel.kt:761` (navigateEditVersion)

**問題**: `editMessage()` で `client = newClient` に上書きする際、初期セッションの旧 `client` が `BranchSessionManager` 管理外のまま残り、close されない経路がある。

**注意**: `navigateEditVersion()` の `client = null` は誤送信防止目的であり、ブランチ client は `BranchSessionManager.activeSessions` に保持されて再利用される設計。従って `navigateEditVersion` 自体は常にリークするわけではなく、**リークが発生するのは初期セッション client が editMessage で上書きされるケースに限定される**。

**改善案**: `SessionCoordinator` に `client` 管理を集約し、切替時に初期セッション client を `BranchSessionManager` に返却するか、明示的に close する。

### 3.3 PermissionHandler の型検証不足 `Confirmed`

**箇所**: `PermissionHandler.kt:64-68`

**問題**: `respondPermission()` が `active.type == RequestType.Question` の場合でもそのまま complete してしまう。UI の実装ミスで不整合が発生しうる。

**注意（Codex 最終レビュー指摘）**: 現行 UI では質問カードのキャンセル時に `respondPermission(allow = false)` を呼んでいる（`ChatPanel.kt:234`）。そのまま型ガードを入れると **質問キャンセルが無効化される**。修正時は UI 側の呼び出し経路も同時に変更する必要がある。

**改善案**:
```kotlin
// 1. PermissionHandler に型ガード + キャンセル用メソッドを追加
fun respondPermission(allow: Boolean, denyMessage: String) {
    val req = active ?: return
    if (req.type != RequestType.Permission) return  // ← ガード追加
    req.deferred.complete(if (allow) PermissionResultAllow() else PermissionResultDeny(denyMessage))
}

fun respondQuestion(answers: Map<String, String>) {
    val req = active ?: return
    if (req.type != RequestType.Question) return  // ← ガード追加
    // ...
}

/** 質問カードのキャンセル（型を問わず deny で完了） */
fun cancelActiveRequest() {
    val req = active ?: return
    req.deferred.complete(PermissionResultDeny("Cancelled by user"))
}

// 2. ChatPanel.kt の質問カード onCancel を修正
//    Before: onCancel = { viewModel.respondPermission(allow = false) }
//    After:  onCancel = { viewModel.cancelActiveRequest() }
```

### 3.4 TranscriptTailer の partial line 欠落 `Confirmed`

**箇所**: `TranscriptTailer.kt:60-81`

**問題**: `readNewLines()` が `buffer.toString().split('\n')` で分割後、pos を `size` まで進める。ファイル末尾が改行で終わっていない場合（書込中）、不完全な JSON 行を `parseLine` に渡して失敗し、かつ pos が進むため再試行されない。

**改善案**: 最後の行が改行で終わっていない場合は pos を巻き戻す:
```kotlin
val lines = buffer.toString(Charsets.UTF_8).split('\n')
val completeLines = if (buffer.last() == '\n'.code.toByte()) lines else lines.dropLast(1)
val bytesConsumed = completeLines.sumOf { it.toByteArray(Charsets.UTF_8).size + 1 }
// pos = fromPosition + bytesConsumed
```

### 3.5 非スレッドセーフなコレクション + 複合整合性 `Confirmed`

**箇所**: `ChatViewModel.kt:101-111`

```kotlin
private val activeTailers = mutableMapOf<String, TranscriptTailer>()       // NOT thread-safe
private val tailerKeyRefs = mutableMapOf<String, AtomicReference<String>>() // NOT thread-safe
private val agentKeyRefs = mutableMapOf<String, AtomicReference<String>>()  // NOT thread-safe
private val unresolvedHookIds = mutableListOf<String>()                     // NOT thread-safe
```

`startTailing` は SDK スレッド（hook コールバック）から呼ばれ、`stopAllTailing` は VM スレッドから呼ばれるため、同時アクセスの可能性がある。

**注意（Codex 最終レビュー指摘）**: 問題は Map 単体のスレッド安全性だけでなく、`unresolvedHookIds`（MutableList）と複数 Map の **複合整合性** にある。`ConcurrentHashMap` 化だけでは不十分で、複数コレクション間の操作の原子性が保証されない。

**改善案**: `SubAgentCoordinator` に集約し、内部で Mutex/actor による一貫した排他制御を行う。単純な `ConcurrentHashMap` 化は暫定措置としても不十分であり、Phase 1 の `SubAgentCoordinator` 導入で根本対応する。

### 3.6 BranchSessionManager.createEditBranchSession の mutex 保護不足 `Partial`

**箇所**: `BranchSessionManager.kt:30`

**問題**: `getOrResumeSession` は `sessionMutex` で保護されているが、`createEditBranchSession` は保護されていない。

**注意（Codex 最終レビュー指摘）**: `activeSessions` は `ConcurrentHashMap` であるため、単純な put 操作の競合でクラッシュすることはない。真の論点は `closeAll()` と `createEditBranchSession`/`getOrResumeSession` の **並行実行時の整合性** にある（closeAll で全セッションを閉じている最中に新規セッションが作成される可能性）。

**改善案**: `closeAll()` と create/resume の並行実行をガードするため、`createEditBranchSession` も `sessionMutex` で保護すべき。

### 3.7 tailer 停止時の join 不足 `Hypothesis`

**箇所**: `ChatViewModel.kt:584` (stopAllTailing)

**問題（Codex 最終レビュー指摘）**: `stopAllTailing()` が tailer の coroutine を `cancel()` するのみで `join()` しないため、停止直後に遅延更新が混入するリスクがある。

**改善案**: `SubAgentCoordinator.stopAll()` で cancel + join を行い、全 tailer の完全停止を保証する。

---

## 4. 責務分離の提案

### 4.1 アーキテクチャ概要

```
ChatViewModel (薄いオーケストレーター ~200行)
    ├── SessionCoordinator       ← セッションライフサイクル + client 管理
    │     ├── BranchSessionManager (既存)
    │     └── AuthFlowHandler    ← 認証フロー (Process I/O)
    ├── TurnEngine               ← メッセージ送受信 + 応答収集の統一
    ├── SubAgentCoordinator      ← テイリング管理 + hookId 解決 + task 状態
    └── UsageTracker             ← トークン計算・コスト管理
```

### 4.2 `SessionCoordinator` — セッション + client 管理の一元化

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

    /** client 切替時に旧 client を安全に close */
    suspend fun switchClient(newClient: ClaudeSDKClient) { ... }

    suspend fun connect(model: Model, permissionMode: PermissionMode, resumeSessionId: String? = null): Result<Unit>
    suspend fun switchToBranch(branchSessionId: String, model: Model, permissionMode: PermissionMode): Result<Unit>
    fun clearActiveClient()
    suspend fun closeAll()
}
```

**解決する問題**: client ライフサイクル漏れ（§3.2）、client/sessionId の非原子的更新

### 4.3 `AuthFlowHandler` — 認証フローの分離

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

**解決する問題**: VM から Process I/O 管理を完全に分離

### 4.4 `TurnEngine` — メッセージ送受信の統一

```kotlin
class TurnEngine(private val scope: CoroutineScope) {
    private val activeTurnId = AtomicLong(0L)
    private var activeTurnJob: Job? = null

    sealed interface TurnEvent {
        data class System(val message: SystemMessage) : TurnEvent
        data class Stream(val event: StreamEvent) : TurnEvent
        data class Assistant(val message: AssistantMessage) : TurnEvent
        data class Result(val message: ResultMessage) : TurnEvent
    }

    /**
     * メッセージを送信し、応答をイベントとして通知する。
     * sendMessage と editMessage の共通ロジック。
     */
    fun dispatch(
        client: ClaudeSDKClient,
        text: String,
        files: List<AttachedFile> = emptyList(),
        onEvent: (TurnEvent) -> Unit,
    ): Job

    fun invalidateCurrentTurn(): Long
    fun cancel()
}
```

**解決する問題**: sendMessage/editMessage の重複除去、activeTurnId 管理の局所化

### 4.5 `SubAgentCoordinator` — サブエージェント管理

```kotlin
class SubAgentCoordinator(private val scope: CoroutineScope) {
    private val mutex = Mutex()  // スレッドセーフティ保証

    private val _tasks = MutableStateFlow<Map<String, SubAgentTask>>(emptyMap())
    val tasks: StateFlow<Map<String, SubAgentTask>> = _tasks.asStateFlow()

    suspend fun onSubAgentStart(agentId: String, transcriptPath: String, hookToolUseId: String, sessionId: String?)
    suspend fun onSubAgentStop(agentId: String)
    fun resolveParentToolUseId(hookId: String, realId: String)
    fun updateSpawnedByToolName(parentToolUseId: String, toolName: String)
    suspend fun stopAll()
}
```

**解決する問題**: 非スレッドセーフなコレクション（§3.5）、hookId 管理の局所化

### 4.6 `UsageTracker` — トークン・コスト追跡

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
        const val DEFAULT_CONTEXT_WINDOW = 200_000L
    }
}
```

**解決する問題**: マジックナンバーの定数化、将来のモデル別 context window 対応

### 4.7 リファクタリング後の ChatViewModel イメージ

```kotlin
class ChatViewModel(
    private val projectBasePath: String,
    private val claudeCodePath: String?,
    private val initialModel: Model,
    private val initialPermissionMode: PermissionMode,
) {
    private val vmScope = CoroutineScope(SupervisorJob())

    // --- Delegates ---
    private val sessionCoordinator = SessionCoordinator(...)
    private val authFlowHandler = AuthFlowHandler(vmScope)
    private val turnEngine = TurnEngine(vmScope)
    private val subAgentCoordinator = SubAgentCoordinator(vmScope)
    private val usageTracker = UsageTracker()
    private val permissionHandler = PermissionHandler(...)

    // --- Conversation State ---
    private val _conversationState = MutableStateFlow(ConversationState())
    private val _attachedFiles = MutableStateFlow<List<AttachedFile>>(emptyList())

    // --- Public API (thin delegation) ---
    fun sendMessage(text: String) { /* ~20行 */ }
    fun editMessage(editGroupId: String, newText: String) { /* ~30行 */ }
    fun navigateEditVersion(editGroupId: String, direction: Int) { /* ~20行 */ }
    suspend fun start(resumeSessionId: String? = null) { /* delegate to sessionCoordinator */ }
    suspend fun clear() { /* delegate cleanup to all coordinators */ }
    fun dispose() { vmScope.cancel() }
}
```

---

## 5. コードリーダビリティの改善提案

### 5.1 状態遷移の宣言的管理

30箇所以上に散在する `_uiState.update { it.copy(...) }` を Reducer パターンで統一:

```kotlin
private sealed interface StateAction {
    data class SessionConnected(val sessionId: String) : StateAction
    data class TurnStarted(val userMessage: ChatMessage.User) : StateAction
    data class MessageReceived(val message: ChatMessage.Assistant, val path: SlotPath) : StateAction
    data class TurnCompleted(val usage: UsageTracker.Usage, val errorMessage: String?) : StateAction
    // ...
}

private fun reduce(state: ChatUiState, action: StateAction): ChatUiState = when (action) {
    is StateAction.SessionConnected -> state.copy(sessionState = SessionState.Ready, sessionId = action.sessionId)
    // ...
}
```

**注意**: Reducer 導入は Phase 3 とし、まずバグ修正と責務分離を優先すべき（Codex レビュー指摘）。

### 5.2 SessionState 遷移表の明示

不正な状態遷移をガードする仕組み:

```kotlin
enum class SessionState {
    Disconnected, Connecting, AuthRequired, Ready, Processing, WaitingForInput, Error;

    fun canTransitionTo(next: SessionState): Boolean = when (this) {
        Disconnected -> next in setOf(Connecting)
        Connecting -> next in setOf(Ready, AuthRequired, Error)
        AuthRequired -> next in setOf(Disconnected)
        Ready -> next in setOf(Processing, Disconnected, Error)
        Processing -> next in setOf(WaitingForInput, Error)
        WaitingForInput -> next in setOf(Processing, Connecting, Disconnected, Error)
        Error -> next in setOf(Disconnected)
    }
}
```

### 5.3 ConversationTree の upsert ヘルパー

`handleAssistantMessage` の複雑な条件分岐を簡素化:

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

---

## 6. Kotlin/Coroutines の活用提案

### 6.1 `collectLatest` による Job 自動キャンセル

```kotlin
private val sendRequests = MutableSharedFlow<SendRequest>(extraBufferCapacity = 1)

init {
    vmScope.launch {
        sendRequests.collectLatest { request ->
            // 前の collect が自動的にキャンセルされる — activeTurnJob 手動管理不要
            executeTurn(request)
        }
    }
}
```

### 6.2 structured concurrency による `disposed` フラグ不要化

```kotlin
// 現状: disposed フラグ + 手動チェックが散在
if (disposed) return
// ...
if (disposed || !currentCoroutineContext().isActive) { localClient.close(); return }

// 改善: vmScope.isActive に統一
private inline fun ifActive(block: () -> Unit) {
    if (vmScope.isActive) block()
}
```

### 6.3 actor ベースのサブエージェント状態管理

```kotlin
// SubAgentCoordinator 内
private val commands = Channel<SubAgentCommand>(Channel.UNLIMITED)

init {
    scope.launch {
        for (cmd in commands) {
            when (cmd) {
                is SubAgentCommand.Start -> handleStart(cmd)
                is SubAgentCommand.Stop -> handleStop(cmd)
                is SubAgentCommand.Resolve -> handleResolve(cmd)
            }
        }
    }
}
```

### 6.4 PermissionHandler の型安全化

型ガード追加に加え、質問キャンセルの専用メソッドを導入（§3.3 参照）:

```kotlin
fun respondPermission(allow: Boolean, denyMessage: String) {
    val req = active ?: return
    if (req.type != RequestType.Permission) return  // ← ガード
    req.deferred.complete(if (allow) PermissionResultAllow() else PermissionResultDeny(denyMessage))
}

/** 型を問わず現在のリクエストをキャンセル */
fun cancelActiveRequest() {
    val req = active ?: return
    req.deferred.complete(PermissionResultDeny("Cancelled by user"))
}
```

**UI 側の修正も必須**: `ChatPanel.kt:234` の `onCancel` を `viewModel.cancelActiveRequest()` に変更。

---

## 7. Compose ReComposition への配慮

### 7.1 ChatUiState の分割

現在の18フィールドを持つ `ChatUiState` を機能別に分割:

```kotlin
// 頻繁に変わる（セッション状態遷移）
data class SessionStatus(val state: SessionState, val sessionId: String?, val errorMessage: String?)

// ターン完了時に変わる
data class UsageInfo(val contextUsage: Float, val totalInputTokens: Long, val totalCostUsd: Double)

// 会話操作で変わる
data class ConversationState(val tree: ConversationTree, val cursor: ConversationCursor)

// 独立した更新頻度
// subAgentTasks → SubAgentCoordinator.tasks (別 StateFlow)
// authOutputLines → AuthFlowHandler.state (別 StateFlow)
// pendingPermission/Question → PermissionHandler.state (別 StateFlow)
```

### 7.2 `activeMessages` の計算コスト

```kotlin
// 現状: ChatUiState の任意のフィールド変更で再計算
val activeMessages: List<ChatMessage>
    get() = conversationTree.getActiveMessages()  // ← 毎回ツリー走査

// 改善: UI 側で derivedStateOf でキャッシュ
val messages by remember { derivedStateOf { uiState.conversationTree.getActiveMessages() } }
```

### 7.3 SubAgentTask のストリーミング更新

ストリーミング中に1タスクの messages が頻繁に更新されると、`Map<String, SubAgentTask>` 全体のコピーが発生。`SubAgentCoordinator` を導入し、タスク単位の `StateFlow` を UI 側で個別に collect することで影響を局所化。

### 7.4 `@Immutable` 前提設計

`@Stable` より `@Immutable` 前提に寄せ、不変コレクション（`kotlinx.collections.immutable` の `PersistentMap` 等）を採用することで、Compose の同一性チェックを最適化。

---

## 8. SDK への移行候補

| # | 項目 | 理由 | 優先度 |
|---|------|------|--------|
| 1 | hookToolUseId → parentToolUseId 解決 | SDK の stream-json プロトコル内部知識。Plugin が知るべきでない | 高 |
| 2 | sub-agent transcript path の構築 | `"${transcriptPath}/subagents/agent-$agentId.jsonl"` は SDK 規約 | 高 |
| 3 | `parseTranscriptLine` の公開 API 化 | 現在 SDK internal に依存 (`TranscriptParser.kt:11`) | 高 |
| 4 | stream-json の usage 集計の正規化 | 単発/累積の判断を Plugin に委ねるべきでない | 中 |
| 5 | ContentBlockBuilder DSL | JSON プロトコル仕様への直接依存を排除 | 中 |
| 6 | 型付き StreamEvent | `message_start` の手動解析を不要にする | 中 |
| 7 | tool-use/event correlation ID | hookId 解決問題の根本解決 | 中 |

---

## 9. 優先度付き改善ロードマップ

### Phase 0: 即時バグ修正（2-3日）

| 項目 | 深刻度 | 工数 | 備考 |
|------|--------|------|------|
| TranscriptTailer の partial line 修正 | バグ `Confirmed` | XS | §3.4 |
| PermissionHandler の型チェック追加 + UI 修正 | バグ `Confirmed` | S | §3.3 — `cancelActiveRequest()` 追加 + ChatPanel 修正が必要 |
| サブエージェント管理の暫定排他制御（Mutex） | バグ `Confirmed` | S | §3.5 — 単純な ConcurrentHashMap 化では複合整合性が不十分。暫定 Mutex で保護し、Phase 1 で SubAgentCoordinator に移行 |
| client 切替時の旧 client close 追加 | リーク `Partial` | S | §3.2 — editMessage での初期セッション client リークに限定 |

### Phase 1: 重複除去・共通化（3-5日）

| 項目 | 効果 | 工数 |
|------|------|------|
| 応答収集ループの共通化 (`TurnEngine`) | 重複除去 | M |
| `200_000L` 等のマジックナンバー定数化 | 可読性 | XS |
| `AuthFlowHandler` の切り出し | 責務分離 | M |
| `SubAgentCoordinator` の導入（Mutex 保護含む） | 責務分離 + スレッド安全 | M |

### Phase 2: 構造改善（1-2週間）

| 項目 | 効果 | 工数 |
|------|------|------|
| `SessionCoordinator` の導入 | client 管理一元化 | L |
| `UsageTracker` の切り出し | 責務分離 | S |
| `ChatUiState` の分割 | ReComposition 最適化 | M |
| `activeMessages` の derivedStateOf 化 | パフォーマンス | S |
| BranchSessionManager の mutex 保護統一 | スレッド安全 | S |

### Phase 3: アーキテクチャ刷新（2-4週間）

| 項目 | 効果 | 工数 |
|------|------|------|
| Reducer + Action パターンの導入 | 状態遷移の宣言的管理 | L |
| `collectLatest` ベースのイベント駆動化 | Job 管理の簡素化 | M |
| SessionState 遷移表ガードの導入 | 不正遷移防止 | S |
| `@Immutable` + persistent collection の採用 | Compose 最適化 | M |

### Phase 4: SDK 改善（並行作業）

| 項目 | 効果 | 工数 |
|------|------|------|
| hookId → parentToolUseId 解決の SDK 移管 | バグの根本解決 | L (SDK) |
| `parseTranscriptLine` の公開 API 化 | internal 依存の排除 | S (SDK) |
| ContentBlockBuilder DSL | 型安全性 | M (SDK) |
| 型付き StreamEvent | 型安全性 | M (SDK) |
| transcript path の SDK 提供 | 規約依存の排除 | S (SDK) |

---

## 付録: レビュー経緯

本レポートは以下のプロセスで作成:

1. **並列調査**: Claude と Codex がそれぞれ独立に ChatViewModel を分析
2. **相互レビュー**: 各レポートを相手がレビューし、見落とし・優先度のズレを指摘
3. **統合**: 両者の強みを統合
4. **最終レビュー (Rev.2)**: 統合レポートを Codex がレビューし、以下を反映:
   - PermissionHandler 型ガードと UI 呼び出し経路の衝突（`cancelActiveRequest()` 追加）
   - `navigateEditVersion` client リーク指摘の条件限定
   - `createEditBranchSession` mutex 指摘の根拠精緻化（`closeAll()` との並行整合性）
   - 非スレッドセーフコレクションの工数再見積もり（XS → S、複合整合性の明記）
   - tailer 停止時の join 不足の追加指摘
   - 各指摘への `Confirmed` / `Partial` / `Hypothesis` ラベル付与

| 観点 | Claude の強み | Codex の強み |
|------|--------------|-------------|
| 設計提案 | 具体的なクラススケッチとコード例 | — |
| バグ検出 | — | FIFO 誤マップ、型検証不足、partial line、client リーク |
| Kotlin 活用 | collectLatest、structured concurrency | actor ベースの並行制御 |
| Compose | derivedStateOf、SubAgentTask の StateFlow 分離 | @Immutable 前提設計 |
| 優先度 | Phase 分けと工数見積もり | バグ修正を最優先とする実務的判断 |
| 最終レビュー | — | UI 呼び出し経路との整合性検証、指摘の信頼度ラベリング |

---

*統合レポート作成: Claude + Codex / 2026-02-27*
*Rev.2: Codex 最終レビュー反映 / 2026-02-27*
