# マルチタブ・セッション管理機能 実装プラン (v3)

## 概要

IntelliJ プラグインのチャット UI にマルチタブ機能を追加し、複数の会話を並行して管理できるようにする。
タブの切り替え、新規作成、削除、セッション履歴からの再開をサポートする。

---

## チーム体制

### ロール定義

| ロール | 担当者 | 責務 |
|--------|--------|------|
| **VM Engineer** | (未アサイン) | ViewModel / Service レイヤーの実装。ChatViewModel ライフサイクル整理、TabViewModel、SessionService 改修 |
| **UI Engineer** | (未アサイン) | Compose UI レイヤーの実装。TabBar、ChatPanel 改修、削除ダイアログ |
| **History Engineer** | (未アサイン) | セッション履歴機能の実装。SessionHistoryService、履歴 Popup、TabViewModel への履歴統合 |
| **Spec Reviewer** | (未アサイン) | 仕様整合性レビュー（後述） |
| **Code Reviewer** | (未アサイン) | コード品質レビュー（後述） |

> **注**: 少人数チームの場合、VM Engineer と History Engineer を兼任可能。UI Engineer は独立性が高いため専任が望ましい。

### ワークストリームと並列化

> **注意**: 同一担当者の複数タスクは直列実行。矢印 `→` は順序依存を示す。

```
Week 1 (基盤)
─────────────────────────────────────────────────────────
VM Engineer:    [0.1a: VM API契約/状態遷移確定] → [0.1b: VM実装] → [0.2] → [0.3] → [1.1]
History Eng:    [Phase 4.1: SessionHistoryService] ──────────────────────────────────────→
UI Engineer:    [TabBar デザインプロト] ──→ (0.1a + 1.1 完了後) [Phase 2.1: TabBar UI]

Week 2 (タブ管理 + UI)
─────────────────────────────────────────────────────────
VM Engineer:    [Phase 1.2: TabViewModel] → [Phase 1.3: Model/PermScope]
UI Engineer:    [Phase 2.1 続き] → [Phase 2.2: ChatPanel改修]
History Eng:    [Phase 4.1 続き] ─────────→ [Phase 4.3: 履歴Popup UI] (※ UI Eng と共同)

Week 3 (統合 + 履歴)
─────────────────────────────────────────────────────────
VM Engineer:    [Phase 2.3: ToolWindowFactory] → (UI Eng と結合テスト)
UI Engineer:    [Phase 3.1: 削除ダイアログ] → (結合テスト)
History Eng:    [Phase 4.4: 履歴統合] → (全体結合テスト)
```

**Phase 0 のクリティカルパス圧縮:**
- `Phase 0.1` を `0.1a (API 契約/状態遷移表の確定)` と `0.1b (実装)` に分割
- UI Engineer は **`0.1a` + `1.1` 完了時点** で `Phase 2.1 (TabBar UI)` に着手可能
  - 理由: `TabBar` は `ChatTab` モデルと `TabViewModel` の public API シグネチャにのみ依存。内部実装の完了は不要
- これにより UI の遊休期間を最小化する

### アサイン表（Phase × 担当者）

| Phase | 内容 | 担当 | レビュー | 備考 |
|-------|------|------|---------|------|
| 0.1a | ChatViewModel API 契約/状態遷移表確定 | VM Engineer | Spec Reviewer | ドキュメントレビュー。UI Engineer の着手ゲート |
| 0.1b | ChatViewModel ライフサイクル実装 | VM Engineer | Code Reviewer → Spec Reviewer | 最重要。Code Reviewer 2回レビュー推奨 |
| 0.2 | PermissionHandler cancel 対応 | VM Engineer | Code Reviewer | 小規模変更 |
| 0.3 | SessionService lazy dispose 修正 | VM Engineer | Code Reviewer | 小規模変更 |
| 1.1 | ChatTab モデル定義 | VM Engineer | Code Reviewer | 小規模。Spec レビュー不要 |
| 1.2 | TabViewModel 作成 | VM Engineer | Code Reviewer → Spec Reviewer | |
| 1.3 | モデル/権限モードスコープ変更 | VM Engineer | Code Reviewer → Spec Reviewer | |
| 2.1 | TabBar UI コンポーネント | UI Engineer | Code Reviewer → Spec Reviewer | |
| 2.2 | ChatPanel 改修 | UI Engineer | Code Reviewer → Spec Reviewer | 1.2 と統合 Spec レビュー |
| 2.3 | AgentToolWindowFactory 改修 | VM Engineer | Code Reviewer → Spec Reviewer | |
| 3.1 | 削除確認ダイアログ + clearActiveTab | UI Engineer | Code Reviewer → Spec Reviewer | cancel/clear 含むため Code Reviewer 必須 |
| 4.1 | SessionHistoryService | History Engineer | Code Reviewer → Spec Reviewer | |
| 4.3 | 履歴 Popup UI | History Eng + UI Eng | Code Reviewer → Spec Reviewer | 共同実装（後述） |
| 4.4 | TabViewModel 履歴統合 | History Engineer | Code Reviewer → Spec Reviewer | 最終統合。最も厳密にレビュー |

### Phase 4.3 の共同実装ルール

履歴 Popup UI (`SessionHistoryPopup.kt`) は History Engineer と UI Engineer の共同実装とする:

- **History Engineer**: データ供給側の API 契約（`SessionSummary` のフィールド、ローディング状態、エラーハンドリング）を定義・実装
- **UI Engineer**: Compose UI のレイアウト・スタイリング・インタラクション（既存 UI コンポーネントとの一貫性確保）を実装
- **進め方**: History Engineer が API + データモデルを先に PR し、UI Engineer がそれを使って UI を実装。もしくはペアプログラミング

### レビュー体制

#### Spec Reviewer（仕様整合性レビュアー）

**レビュー観点:**

1. **仕様準拠**: 本プランに記載された仕様（状態遷移表、API 契約、破棄責務）を実装が完全に満たしているか
2. **クロスコンポーネント整合性**: VM Engineer / UI Engineer / History Engineer の成果物が矛盾なく結合できるか
   - `TabViewModel ↔ ChatViewModel` のライフサイクル契約
   - `TabViewModel ↔ ChatPanel` の state 購読・イベントコールバック
   - `SessionHistoryService ↔ TabViewModel` の resume フロー
   - `SessionService ↔ TabViewModel` の所有権・dispose 契約
   - `AgentToolWindowFactory ↔ TabViewModel` の初期化タイミング・再生成時挙動
3. **エッジケース網羅**: 状態遷移表にないパス（例: 接続中のタブ切替、resume 失敗後の再 start）が安全か
4. **データフロー**: Single Source of Truth (`ChatViewModel.uiState.sessionId`) が崩れていないか
5. **エラー/フォールバックの UX 一貫性**: resume 失敗、履歴読み込み失敗、接続エラー時の表示が統一されているか
6. **ユーザーシナリオ検証**: 以下のシナリオが仕様通りに動作するか
   - 新規タブ作成 → メッセージ送信 → タブタイトル自動更新
   - タブ切替 → 入力欄/スクロール位置の独立
   - 削除ボタン → 確認ダイアログ → クリア → 新セッション開始
   - 履歴選択 → 過去メッセージ表示 → resume 接続 → 新規ターン
   - 処理中タブの close → turn cancel + permission cancel + リソース解放

**レビュータイミング:**
- 各 Phase の PR マージ前（Code Reviewer の後に実施）
- Phase 1.2 (TabViewModel) と Phase 2.2 (ChatPanel) は **両 PR の Code Review 完了後に、Spec Reviewer が統合観点で連続レビュー** する（必要に応じて結合ブランチで動作確認）
- Phase 4.4 (履歴統合) は全体の最終整合性チェックとして **最も厳密に** レビュー

**チェックリスト (PR テンプレート):**
```markdown
## Spec Review Checklist
- [ ] 状態遷移表 (Phase 0.1) に準拠しているか
- [ ] 破棄責務 (VM/TabVM) が漏れなく実装されているか
- [ ] Single Source of Truth (sessionId, messages) が維持されているか
- [ ] 他の Phase との結合点で API シグネチャが一致しているか
- [ ] SessionService ↔ TabViewModel の所有権/dispose が明確か
- [ ] AgentToolWindowFactory ↔ TabViewModel の初期化が正しいか
- [ ] エッジケース（dispose 中の start、resume 失敗、同時操作）が安全か
- [ ] エラー/フォールバック時の UX が一貫しているか
- [ ] ユーザーシナリオ（上記5つ）が仕様通りに動作するか
```

#### Code Reviewer（コード品質レビュアー）

**レビュー観点:**

1. **Kotlin コード品質**:
   - Kotlin の慣用的な書き方（scope functions, extension functions, sealed class の使い方）に準拠しているか
   - 命名規則が既存コードベースと一致しているか（`_uiState` / `uiState` パターン、`internal` の使い分け等）
   - 不要な `public` 修飾子がないか（Kotlin はデフォルト public だが、`internal` / `private` を適切に使用）

2. **既存コードとの一貫性**:
   - 既存の `ChatViewModel` / `ChatPanel` / `SessionService` のパターンに合致しているか
   - `@Stable` / `@Immutable` アノテーションの付与ルール
   - `StateFlow` / `MutableStateFlow` / `collectAsState()` の使い方
   - Compose コンポーネントの命名・分割粒度
   - IntelliJ Platform API の使い方（`@Service`, `Disposable`, `Messages` 等）

3. **コルーチン・非同期処理**:
   - `CoroutineScope` の所有権と破棄責務が明確か
   - `SupervisorJob` の適切な使用（子の失敗が親に伝播しない）
   - `Mutex` / `@Volatile` / `ConcurrentHashMap` の使い分けが正しいか
   - `cancelAndJoin()` vs `cancel()` の使い分け（`clear()` では join、`dispose()` では即 cancel）
   - `withContext(Dispatchers.IO)` が I/O 操作に適用されているか
   - `Flow.collect` が適切なスコープで実行されているか（リーク防止）
   - `stateIn` の `SharingStarted` 戦略が適切か

4. **コメント・ドキュメント**:
   - public API には KDoc コメントがあるか
   - 複雑なロジック（race condition 防止、defensive check）に説明コメントがあるか
   - TODO / FIXME が残っていないか（残す場合は issue リンク付き）

5. **安全性**:
   - null 安全性（`?.` / `?:` / `!!` の使い方。`!!` は原則禁止）
   - リソースリーク（`Closeable.use {}`, `Files.list().use {}`, scope cancel）
   - スレッドセーフティ（共有 mutable state へのアクセスが保護されているか）
   - IntelliJ Platform のスレッドルール（EDT で Swing API、BGT で I/O）

**レビュータイミング:**
- 各 Phase の PR 作成時（Spec Reviewer の前に実施）
- Phase 0.1 (ChatViewModel lifecycle) は最も重要。**2回レビュー** を推奨（初回 + 修正後）

**チェックリスト (PR テンプレート):**
```markdown
## Code Review Checklist
### Kotlin コード品質
- [ ] Kotlin 慣用記法に準拠しているか
- [ ] 命名・構造が既存コードベースと一貫しているか
- [ ] null 安全性が確保されているか（!! 不使用）
- [ ] public API に KDoc コメントがあるか
- [ ] 複雑なロジックに説明コメントがあるか

### コルーチン・非同期処理
- [ ] CoroutineScope の所有権と破棄が明確か
- [ ] Mutex / Volatile / ConcurrentHashMap の使い方が正しいか
- [ ] Flow collect のスコープが適切か（リーク防止）
- [ ] 例外時に state が中途半端に残らないか（rollback / error state への遷移）
- [ ] MutableStateFlow.update {} の原子性が保たれているか
- [ ] 競合ケースのテストが存在するか（start/clear/dispose 同時呼び出し等）

### リソース・スレッド安全性
- [ ] リソースリークがないか（Closeable, scope, Stream）
- [ ] IntelliJ Platform のスレッドルールに準拠しているか（EDT で Swing API、BGT で I/O）
- [ ] EDT/BGT 境界での state mutation / UI callback の実行場所が正しいか
```

### レビューフロー

```
実装者が PR 作成
    ↓
Code Reviewer がコード品質レビュー（レビュー SLA: 24h 以内に一次反応）
    ↓ (Approve or Request Changes)
修正対応（必要な場合）
    ↓
Spec Reviewer が仕様整合性レビュー（レビュー SLA: 24h 以内に一次反応）
    ↓ (Approve or Request Changes)
修正対応（必要な場合）
    ↓
マージ
```

**レビュー必要度:**
- **Code + Spec 両方必須**: Phase 0.1b, 1.2, 1.3, 2.1, 2.2, 2.3, 3.1, 4.1, 4.3, 4.4
- **Code のみ**: Phase 0.2, 0.3, 1.1（小規模変更）
- **Spec のみ**: Phase 0.1a（API 契約ドキュメント）

**クリティカルパス優先キュー**: Phase 0.1b → 1.2 → 2.2 → 4.4 の順でレビューを優先する。これらが遅れると後続の全ストリームに波及するため、レビュアーはこれらの PR を最優先で処理する。

### コミュニケーション

- **日次**: 各エンジニアが進捗と blocker を共有（非同期 OK）
- **Phase 完了時**: VM Engineer + UI Engineer + History Engineer で結合点の動作確認
- **Week 3 開始時**: 全員で結合テスト計画の確認
- **ブロッカー発生時**: 即座に関連メンバーに通知。特に VM Engineer の Phase 0 遅延は全体に波及するため最優先解消
- **API 変更通知**: 結合点の API シグネチャを変更する場合、変更前に関連メンバーに通知し合意を得る（API freeze 後の breaking change 防止）

---

## Phase 0: ChatViewModel ライフサイクル整理

> **目的**: マルチタブ化の前提として、ChatViewModel の初期化/再開/クリア/破棄の契約を明確にし、race condition を排除する。

### 0.1 ライフサイクル API の再設計

**変更ファイル: `plugin/src/.../viewmodel/ChatViewModel.kt`**

現行の `fun initialize()` を以下の明示的ライフサイクル API に置き換える。
ChatViewModel は自前の `CoroutineScope` を所有し、`dispose()` で完全に cancel する。

```kotlin
class ChatViewModel(
    private val projectBasePath: String,
    private val claudeCodePath: String,
    private val initialModel: Model,
    private val initialPermissionMode: PermissionMode,
) {
    // VM が所有する CoroutineScope。dispose() で cancel される。
    private val vmScope = CoroutineScope(SupervisorJob())

    private val startMutex = Mutex()
    private var startJob: Job? = null
    @Volatile private var disposed = false

    /**
     * セッションを開始する。resumeSessionId が指定された場合はセッション再開。
     * Mutex により多重起動を防止。dispose 済みの場合は何もしない。
     *
     * Idempotent: Disconnected 以外の状態 or startJob がアクティブなら何もしない。
     */
    suspend fun start(resumeSessionId: String? = null) {
        startMutex.withLock {
            if (disposed) return
            if (startJob?.isActive == true) return
            if (_uiState.value.sessionState != SessionState.Disconnected) return

            // ロック内で先に Connecting に更新。これにより次の start() 呼び出しを確実にブロック。
            _uiState.update { it.copy(sessionState = SessionState.Connecting) }

            startJob = vmScope.launch {
                connectSession(resumeSessionId)
            }
        }
    }

    /**
     * 現在のセッションをクリアし、初期状態に戻す。
     * 進行中の turn、permission 待ち、sub-agent tailer も安全に中断する。
     * dispose() とは異なり、VM 自体は再利用可能。
     */
    suspend fun clear() {
        startMutex.withLock {
            startJob?.cancelAndJoin()
            startJob = null
            activeTurnJob?.cancelAndJoin()
            activeTurnJob = null
            stopAllTailers()
            permissionHandler.cancelPending()
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
     * 所有する CoroutineScope を cancel し、全リソースを解放する。
     */
    fun dispose() {
        disposed = true
        vmScope.cancel()  // startJob, activeTurnJob, tailer jobs もすべて cancel される
        permissionHandler.cancelPending()
        client?.close()
        client = null
    }

    /**
     * 履歴メッセージを UI に投入する（resume 時の過去メッセージ表示用）。
     * start() の前に呼び出すこと。
     */
    fun importHistory(messages: List<ChatMessage>) {
        _uiState.update { it.copy(messages = messages) }
    }

    private suspend fun connectSession(resumeSessionId: String?) {
        // 防御的チェック: dispose 済みなら何もしない
        if (disposed) return

        // ローカル変数で client を作成。代入前に disposed/cancel を再チェックし、
        // dispose() が connectSession() 実行中に入った場合のリークを防止する。
        val localClient = if (resumeSessionId != null) {
            ClaudeAgentSDK.resumeSession(resumeSessionId) { /* ... */ }
        } else {
            ClaudeAgentSDK.createSession { /* ... */ }
        }

        // SDK client 作成後、代入前に再チェック
        if (disposed || !currentCoroutineContext().isActive) {
            localClient.close()
            return
        }

        client = localClient
        // ... 以降の接続処理 (connect, receive flow 等)
    }
}
```

**状態遷移表:**

| 現在の状態 | start() | clear() | dispose() | sendMessage() |
|-----------|---------|---------|-----------|---------------|
| Disconnected | → Connecting | no-op | → 破棄 | no-op (client=null) |
| Connecting | no-op (idempotent) | → Disconnected | → 破棄 | no-op (client=null) |
| Ready | no-op | → Disconnected | → 破棄 | → Processing |
| Processing | no-op | → Disconnected (turn cancel) | → 破棄 | no-op (turn active) |
| WaitingForInput | no-op | → Disconnected | → 破棄 | → Processing |
| Error | no-op | → Disconnected | → 破棄 | no-op |

> **Ready vs WaitingForInput**: `Ready` は初回接続完了直後（まだ1度もメッセージを送っていない状態）。`WaitingForInput` はターン完了後、次のユーザー入力を待っている状態。どちらも `sendMessage()` で `Processing` に遷移する。

**破棄責務 (ChatViewModel が cancel するもの):**
- `vmScope` → `startJob`, `activeTurnJob`, 全 tailer jobs を含む
- `permissionHandler` → pending permission/question を cancel
- `client` → SDK クライアントを close

### 0.2 PermissionHandler の cancel 対応

**変更ファイル: `plugin/src/.../viewmodel/permission/PermissionHandler.kt`**

```kotlin
internal class PermissionHandler(...) {
    private var pendingDeferred: CompletableDeferred<*>? = null

    fun cancelPending() {
        pendingDeferred?.cancel()
        pendingDeferred = null
        updateState { it.copy(pendingPermission = null, pendingQuestion = null) }
    }
}
```

### 0.3 SessionService の lazy dispose 問題修正

**変更ファイル: `plugin/src/.../service/SessionService.kt`**

```kotlin
@Service(Service.Level.PROJECT)
class SessionService(private val project: Project) : Disposable {
    private val scope = CoroutineScope(SupervisorJob())

    private var tabViewModel: TabViewModel? = null

    fun getOrCreateTabViewModel(): TabViewModel {
        return tabViewModel ?: TabViewModel(
            projectBasePath = projectBasePath,
            claudeCodePath = claudeCodePath,
            settingsService = settingsService,
            scope = scope,
        ).also { tabViewModel = it }
    }

    override fun dispose() {
        tabViewModel?.dispose()
        tabViewModel = null
        scope.cancel()
    }
}
```

---

## Phase 1: タブ管理基盤

### 1.1 タブデータモデルの定義

**新規ファイル: `plugin/src/.../model/ChatTab.kt`**

```kotlin
data class ChatTab(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Empty conversation",
    val createdAt: Long = System.currentTimeMillis(),
)
```

> **設計判断**: `sessionId` は `ChatTab` に持たせない。`ChatViewModel.uiState.sessionId` が唯一の情報源 (Single Source of Truth)。

### 1.2 タブ管理 ViewModel の作成

**新規ファイル: `plugin/src/.../viewmodel/TabViewModel.kt`**

```kotlin
class TabViewModel(
    private val projectBasePath: String,
    private val claudeCodePath: String,
    private val settingsService: SettingsService,
    private val scope: CoroutineScope,
) {
    // --- State ---
    private val _tabs = MutableStateFlow<List<ChatTab>>(listOf(ChatTab()))
    val tabs: StateFlow<List<ChatTab>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow<String>(_tabs.value.first().id)
    val activeTabId: StateFlow<String> = _activeTabId.asStateFlow()

    // viewModels を Flow 化し、activeChatViewModel を combine で構成する
    private val _viewModels = MutableStateFlow<Map<String, ChatViewModel>>(emptyMap())

    val activeChatViewModel: StateFlow<ChatViewModel?> = combine(
        _activeTabId,
        _viewModels,
    ) { activeId, vms ->
        vms[activeId]
    }.stateIn(scope, SharingStarted.WhileSubscribed(), null)

    // タブタイトル監視 Job の管理（リーク防止）
    private val titleObserverJobs = ConcurrentHashMap<String, Job>()

    init {
        val firstTab = _tabs.value.first()
        val vm = createViewModel()
        _viewModels.update { it + (firstTab.id to vm) }
        observeTabTitles(firstTab.id, vm)
    }

    // --- Public API ---

    fun addTab(): ChatTab {
        val tab = ChatTab()
        val vm = createViewModel()
        _viewModels.update { it + (tab.id to vm) }
        _tabs.update { it + tab }
        _activeTabId.value = tab.id
        observeTabTitles(tab.id, vm)
        scope.launch { vm.start() }
        return tab
    }

    fun removeTab(tabId: String) {
        if (_tabs.value.size <= 1) return

        // タイトル監視 Job を cancel
        titleObserverJobs.remove(tabId)?.cancel()

        // ViewModel を dispose
        val vm = _viewModels.value[tabId]
        vm?.dispose()
        _viewModels.update { it - tabId }

        _tabs.update { it.filter { tab -> tab.id != tabId } }
        if (_activeTabId.value == tabId) {
            _activeTabId.value = _tabs.value.first().id
        }
    }

    fun selectTab(tabId: String) {
        _activeTabId.value = tabId
    }

    fun clearActiveTab() {
        val tabId = _activeTabId.value
        val vm = _viewModels.value[tabId] ?: return
        scope.launch {
            vm.clear()
            _tabs.update { tabs ->
                tabs.map { if (it.id == tabId) it.copy(title = "Empty conversation") else it }
            }
            vm.start()
        }
    }

    fun resumeSessionFromHistory(summary: SessionSummary, historyMessages: List<ChatMessage>) {
        val tab = ChatTab(
            title = summary.firstPrompt?.take(40) ?: "Resumed session",
        )
        val vm = createViewModel()
        vm.importHistory(historyMessages)
        _viewModels.update { it + (tab.id to vm) }
        _tabs.update { it + tab }
        _activeTabId.value = tab.id
        observeTabTitles(tab.id, vm)
        scope.launch { vm.start(resumeSessionId = summary.sessionId) }
    }

    fun dispose() {
        // 全タイトル監視 Job を cancel
        titleObserverJobs.values.forEach { it.cancel() }
        titleObserverJobs.clear()
        // 全 ViewModel を dispose
        _viewModels.value.values.forEach { it.dispose() }
        _viewModels.value = emptyMap()
    }

    // --- Private ---

    private fun createViewModel(): ChatViewModel {
        return ChatViewModel(
            projectBasePath = projectBasePath,
            claudeCodePath = claudeCodePath,
            initialModel = settingsService.model,
            initialPermissionMode = settingsService.permissionMode,
        )
    }

    /**
     * タブタイトルの自動更新。
     * ChatViewModel の uiState.messages を監視し、最初のユーザーメッセージをタイトルに設定する。
     * Job は titleObserverJobs に保持し、removeTab/dispose で cancel する。
     */
    private fun observeTabTitles(tabId: String, vm: ChatViewModel) {
        val job = scope.launch {
            vm.uiState
                .map { state -> state.messages.filterIsInstance<ChatMessage.User>().firstOrNull()?.text }
                .distinctUntilChanged()
                .collect { firstUserMessage ->
                    if (firstUserMessage != null) {
                        val title = firstUserMessage.take(40).let {
                            if (firstUserMessage.length > 40) "$it..." else it
                        }
                        _tabs.update { tabs ->
                            tabs.map { if (it.id == tabId) it.copy(title = title) else it }
                        }
                    }
                }
        }
        titleObserverJobs[tabId] = job
    }
}
```

**破棄責務 (TabViewModel が cancel するもの):**
- `titleObserverJobs` — 各タブのタイトル監視 Job
- 各 `ChatViewModel.dispose()` — VM 自身のリソース

### 1.3 モデル/権限モードのスコープ定義

**仕様決定:**

- **初期値**: タブ作成時に `SettingsService` からスナップショットを取得
- **タブ内変更**: そのタブの `ChatViewModel` にのみ適用（ローカルスコープ）
- **グローバル反映なし**: タブ内でモデル/権限モードを変更しても `SettingsService` は更新しない
- **新規タブ**: 常に `SettingsService` の現在値を使用

**変更**: `ChatViewModel.changeModel()` / `changePermissionMode()` から `settingsService` への書き込みを削除。

---

## Phase 2: タブバー UI

### 2.1 タブバーコンポーネント

**新規ファイル: `plugin/src/.../ui/component/TabBar.kt`**

```
┌──────────────────────────────────────────────────────────────────┐
│ [Tab1] [Tab2] [Tab3] ←横スクロール可能→  | + 新規チャット 📜 🗑️  │
└──────────────────────────────────────────────────────────────────┘
```

```kotlin
@Composable
fun TabBar(
    tabs: List<ChatTab>,
    activeTabId: String,
    onTabSelect: (String) -> Unit,
    onTabClose: (String) -> Unit,
    onNewChat: () -> Unit,
    onHistory: () -> Unit,
    onDeleteCurrent: () -> Unit,
)
```

**レイアウト構成:**
- **左側**: 横スクロール可能な `LazyRow` でタブを表示
  - 各タブ: タイトル + × ボタン（タブが2つ以上の場合のみ表示）
  - アクティブタブ: IntelliJ テーマ準拠のハイライト
- **右側**: 固定アクションボタン群
  - `+ 新規チャット`: テキストボタン
  - 履歴アイコン: `AllIcons.Actions.ListChanges` or 類似
  - 削除アイコン: `AllIcons.General.Remove` or 類似

### 2.2 ChatPanel の改修

**変更ファイル: `plugin/src/.../ui/ChatPanel.kt`**

```kotlin
@Composable
fun ChatPanel(tabViewModel: TabViewModel, project: Project) {
    val tabs by tabViewModel.tabs.collectAsState()
    val activeTabId by tabViewModel.activeTabId.collectAsState()
    val activeViewModel by tabViewModel.activeChatViewModel.collectAsState()

    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        TabBar(
            tabs = tabs,
            activeTabId = activeTabId,
            onTabSelect = { tabViewModel.selectTab(it) },
            onTabClose = { tabViewModel.removeTab(it) },
            onNewChat = { tabViewModel.addTab() },
            onHistory = { /* Phase 4 で実装 */ },
            onDeleteCurrent = { showDeleteConfirmation = true },
        )

        // key(activeTabId) でタブごとの Compose local state を分離
        // → 入力欄の下書き、スクロール位置がタブ切替時にリセットされる
        key(activeTabId) {
            activeViewModel?.let { vm ->
                ChatContent(vm, project)
            }
        }
    }

    // 削除確認ダイアログ (Phase 3)
    if (showDeleteConfirmation) {
        DeleteConfirmationDialog(
            onConfirm = {
                tabViewModel.clearActiveTab()
                showDeleteConfirmation = false
            },
            onDismiss = { showDeleteConfirmation = false },
        )
    }
}
```

> **設計判断**: `key(activeTabId)` でタブごとに Compose state を分離。`ChatInputArea` の `TextFieldValue` と `ChatMessageList` の `LazyListState` がタブ切替時に独立する。下書き保持は将来要件として後回し。

### 2.3 AgentToolWindowFactory の改修

**変更ファイル: `plugin/src/.../toolwindow/AgentToolWindowFactory.kt`**

```kotlin
class AgentToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        toolWindow.addComposeTab("Claude Code", focusOnClickInside = true) {
            val sessionService = remember { project.service<SessionService>() }
            val tabViewModel = remember { sessionService.getOrCreateTabViewModel() }

            // 初期タブの自動初期化
            val activeVm by tabViewModel.activeChatViewModel.collectAsState()
            LaunchedEffect(activeVm) {
                val vm = activeVm ?: return@LaunchedEffect
                if (vm.uiState.value.sessionState == SessionState.Disconnected) {
                    vm.start()
                }
            }

            ChatPanel(tabViewModel, project)
        }
    }
}
```

---

## Phase 3: 削除機能

### 3.1 削除確認ダイアログ

IntelliJ Platform の標準ダイアログ API を使用:

```kotlin
@Composable
private fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    LaunchedEffect(Unit) {
        withContext(Dispatchers.Main) {
            val result = Messages.showYesNoDialog(
                "現在のチャットをクリアしますか？\n（ローカルの履歴は削除されません）",
                "チャットのクリア",
                Messages.getQuestionIcon()
            )
            if (result == Messages.YES) onConfirm() else onDismiss()
        }
    }
}
```

**`clearActiveTab()` の動作:**
1. `ChatViewModel.clear()` で現在のセッションを安全に中断
   - 進行中の turn をキャンセル
   - pending permission/question を中断
   - SDK クライアントを close
2. UI 状態を初期化
3. タブタイトルを `"Empty conversation"` にリセット
4. `ChatViewModel.start()` で新しいセッションを開始

---

## Phase 4: セッション履歴機能

### 4.1 履歴読み取りサービス（Plugin 側）

**新規ファイル: `plugin/src/.../service/SessionHistoryService.kt`**

> **設計判断**: 履歴読み取りは SDK ではなく plugin 側の service に配置する。
> - 理由: `~/.claude/` のディレクトリ構造は Claude Code CLI の実装詳細であり、SDK の public API として安定させるコストが高い。

```kotlin
@Service(Service.Level.PROJECT)
class SessionHistoryService(private val project: Project) {

    data class SessionSummary(
        val sessionId: String,
        val projectPath: String,
        val firstPrompt: String?,
        val userMessageCount: Int,
        val assistantMessageCount: Int,
        val startTime: Instant?,
        val durationMinutes: Int?,
        val model: String?,
        val totalCostUsd: Double?,
    )

    companion object {
        private val claudeDir = Path.of(System.getProperty("user.home"), ".claude")

        /**
         * プロジェクトパスを正規化する（symlink 解決）。
         */
        fun normalizeProjectPath(path: String): String {
            return try {
                Path.of(path).toRealPath().toString()
            } catch (e: Exception) {
                path  // toRealPath() 失敗時はそのまま返す
            }
        }

        /**
         * Claude Code CLI と同じ方式でプロジェクトパスをエンコードする。
         * "/" → "-" に置換。
         */
        fun encodeClaudeProjectPath(normalizedPath: String): String {
            return normalizedPath.replace("/", "-")
        }
    }

    /**
     * 現在のプロジェクトのセッション一覧を取得する。
     * ~/.claude/usage-data/session-meta/ からメタデータを読み取り、プロジェクトパスでフィルタリング。
     */
    suspend fun listSessions(): List<SessionSummary> = withContext(Dispatchers.IO) {
        val metaDir = claudeDir.resolve("usage-data").resolve("session-meta")
        if (!Files.isDirectory(metaDir)) return@withContext emptyList()

        val projectPath = project.basePath ?: return@withContext emptyList()
        val normalizedProjectPath = normalizeProjectPath(projectPath)

        // Files.list() は Stream を返すため use{} で確実にクローズ
        Files.list(metaDir).use { stream ->
            stream.filter { it.extension == "json" }
                .mapNotNull { file -> parseSummary(file, normalizedProjectPath) }
                .sortedByDescending { it.startTime }
                .toList()
        }
    }

    /**
     * セッションの会話メッセージを読み取る（UI 表示用）。
     */
    suspend fun readSessionMessages(sessionId: String): List<ChatMessage> = withContext(Dispatchers.IO) {
        val projectPath = project.basePath ?: return@withContext emptyList()
        val normalizedPath = normalizeProjectPath(projectPath)
        val encodedPath = encodeClaudeProjectPath(normalizedPath)
        val sessionFile = claudeDir.resolve("projects").resolve(encodedPath).resolve("$sessionId.jsonl")
        if (!Files.isReadable(sessionFile)) return@withContext emptyList()

        parseSessionMessages(sessionFile)
    }

    // --- Private parsing methods ---
    // parseSummary: session-meta JSON → SessionSummary
    //   - project_path フィルタリングも normalizeProjectPath() で正規化して比較
    // parseSessionMessages: JSONL → List<ChatMessage>
    //   - type="user" + message.content is String → ChatMessage.User
    //   - type="assistant" + message.content is List → ChatMessage.Assistant
    //   - isSidechain=true → skip
    //   - 壊れた JSON 行 → skip（ログ出力のみ）
}
```

### 4.2 セッション再開の仕様

**仕様決定:**

- **履歴からの再開時**: 過去メッセージを UI に表示 **した上で** セッションを resume する
  1. `SessionHistoryService.readSessionMessages()` で JSONL からメッセージを構築
  2. `ChatViewModel.importHistory()` で UI に投入
  3. `ChatViewModel.start(resumeSessionId)` で CLI に `--resume` を渡す
- **fork policy**: `forkSession = true` をデフォルトにする
  - 理由: 元のセッション履歴を保全。同一セッションを複数タブで同時 resume しても安全
- **resume 失敗時**: 空セッションにフォールバック + エラーバナー表示

**ChatViewModel の resume 対応:**

```kotlin
private suspend fun connectSession(resumeSessionId: String?) {
    if (disposed) return

    val client = if (resumeSessionId != null) {
        ClaudeAgentSDK.resumeSession(resumeSessionId) {
            cliPath = this@ChatViewModel.claudeCodePath
            model = _uiState.value.model.id
            permissionMode = _uiState.value.permissionMode
            forkSession = true  // 元セッション保全
            // ... 他の設定
        }
    } else {
        ClaudeAgentSDK.createSession {
            cliPath = this@ChatViewModel.claudeCodePath
            model = _uiState.value.model.id
            permissionMode = _uiState.value.permissionMode
            // ... 他の設定
        }
    }
    // 以降の接続処理は共通
}
```

### 4.3 履歴 Popup UI

**新規ファイル: `plugin/src/.../ui/component/SessionHistoryPopup.kt`**

Compose `Popup` で実装する。フォーカス管理・キーボード操作で問題が生じた場合は `JBPopupFactory` ベースへの切り替えを検討。

```kotlin
@Composable
fun SessionHistoryPopup(
    sessions: List<SessionSummary>,
    isLoading: Boolean,
    onSessionSelect: (SessionSummary) -> Unit,
    onDismiss: () -> Unit,
)
```

**表示内容 (各セッション行):**
```
┌─────────────────────────────────────────────────────┐
│ 2026-02-25 14:30  |  claude-opus-4-6                │
│ "マルチタブ機能の実装について相談したい..."          │
│ 12 messages  |  45 min  |  $0.52                    │
├─────────────────────────────────────────────────────┤
│ 2026-02-24 10:15  |  claude-sonnet-4-6              │
│ "バグ修正: PermissionCard のレイアウト崩れ"         │
│ 5 messages  |  12 min  |  $0.08                     │
└─────────────────────────────────────────────────────┘
```

### 4.4 TabViewModel への履歴統合

```kotlin
// ユーザーがセッションを選択した時
fun onSessionSelected(summary: SessionSummary) {
    scope.launch {
        val messages = sessionHistoryService.readSessionMessages(summary.sessionId)
        tabViewModel.resumeSessionFromHistory(summary, messages)
    }
}
```

---

## Phase 5: タブのドラッグ＆ドロップ分割 (将来検討)

**Phase 5 は将来タスクとして延期する。** 理由:

1. Compose for IDE にネイティブなドラッグ＆ドロップ分割 API がない
2. IntelliJ の `ContentManager` はタブ管理には使えるが、エディタのような自由な分割配置は提供しない
3. 自前実装のコストが非常に大きい（ドラッグ判定、分割方向、パネルサイズ管理等）
4. Phase 1-4 で基本的なマルチタブ UX を検証してからの判断が適切

**将来の選択肢:**
- A: Compose で分割パネルを自前実装（`draggable` modifier + `SplitPane` 的レイアウト）
- B: IntelliJ の `JBSplitter` を Swing interop で利用
- C: 分割ではなく「タブのポップアウト（別ウィンドウ化）」で代替

---

## 実装順序

| 順序 | Phase | 内容 | 依存 |
|------|-------|------|------|
| 1 | 0.1 | ChatViewModel ライフサイクル整理 (start/clear/dispose + Mutex + scope) | なし |
| 2 | 0.2 | PermissionHandler cancel 対応 | 0.1 |
| 3 | 0.3 | SessionService lazy dispose 修正 | なし |
| 4 | 1.1 | ChatTab モデル定義 | なし |
| 5 | 1.2 | TabViewModel 作成 (viewModels Flow化 + titleObserverJobs) | 0.1, 1.1 |
| 6 | 1.3 | モデル/権限モードスコープ変更 | 0.1 |
| 7 | 2.1 | TabBar UI コンポーネント | 1.1 |
| 8 | 2.2 | ChatPanel 改修 (TabBar + key分離 + activeChatViewModel) | 1.2, 2.1 |
| 9 | 2.3 | AgentToolWindowFactory 改修 | 0.3, 1.2 |
| 10 | 3.1 | 削除確認ダイアログ + clearActiveTab | 1.2, 2.1 |
| 11 | 4.1 | SessionHistoryService (パス正規化共通化含む) | なし (独立) |
| 12 | 4.3 | 履歴 Popup UI | 4.1 |
| 13 | 4.4 | TabViewModel 履歴統合 | 4.1, 4.3, 1.2 |

**並列化可能な作業:**
- Phase 0.1 + 0.3 + 1.1 + 4.1（独立した作業）
- Phase 2.1 + 1.3（UI と ViewModel 改修は並行可能）

---

## 変更ファイル一覧

### 新規作成
| ファイル | 内容 |
|----------|------|
| `plugin/.../model/ChatTab.kt` | タブデータモデル |
| `plugin/.../viewmodel/TabViewModel.kt` | タブ管理 ViewModel |
| `plugin/.../ui/component/TabBar.kt` | タブバー UI |
| `plugin/.../ui/component/SessionHistoryPopup.kt` | 履歴ポップアップ |
| `plugin/.../service/SessionHistoryService.kt` | セッション履歴読み取りサービス |

### 変更
| ファイル | 変更内容 |
|----------|----------|
| `plugin/.../viewmodel/ChatViewModel.kt` | ライフサイクル API 再設計 (start/clear/dispose/importHistory、自前 scope、状態遷移) |
| `plugin/.../viewmodel/permission/PermissionHandler.kt` | cancelPending() 追加 |
| `plugin/.../service/SessionService.kt` | tabViewModel 管理、lazy dispose 修正 |
| `plugin/.../ui/ChatPanel.kt` | TabBar 追加 + ChatContent 抽出 + key(activeTabId) |
| `plugin/.../toolwindow/AgentToolWindowFactory.kt` | TabViewModel 使用に変更 |

---

## リスクと対策

| リスク | 影響度 | 対策 |
|--------|--------|------|
| 多重初期化 race による client リーク | 高 | `Mutex` + `disposed` フラグ + ロック内で `Connecting` 先行更新 |
| タブ削除/clear 時の in-flight permission/question 中断漏れ | 高 | `PermissionHandler.cancelPending()` + `vmScope.cancel()` |
| メモリ使用量（タブ数 × CLI サブプロセス） | 高 | 非アクティブタブの自動切断を将来検討。当面はタブ数に上限なし |
| タブタイトル監視 Job のリーク | 中 | `titleObserverJobs` Map で管理、removeTab/dispose で cancel |
| activeChatViewModel の初期化不全 | 中 | `combine(activeTabId, viewModelsFlow)` で確実に再計算 |
| セッション resume の失敗 | 中 | 空セッションにフォールバック + エラーバナー表示 |
| タブごとの UI ローカル state 混線 | 中 | `key(activeTabId)` で Compose state を分離 |
| `~/.claude/` ディレクトリ構造の変更 | 中 | plugin 側 service に局所化。パース失敗は graceful に skip |
| 履歴ロードの I/O 遅延 | 中 | `Dispatchers.IO` で非同期実行 + `Files.list().use{}` + loading indicator |
| project path 正規化差異（symlink, case） | 中 | `normalizeProjectPath()` / `encodeClaudeProjectPath()` を共通関数化 |
| 同一 session resume の競合 | 低 | `forkSession = true` デフォルトで回避 |
| ファイルウォッチャー（subagent tailer）数の増加 | 低 | `vmScope.cancel()` で確実に停止 |
| CoroutineScope リーク | 低 | ChatViewModel が自前 scope を所有、dispose で cancel |
| **[チーム]** Phase 0.1 レビュー滞留 | 高 | クリティカルパス優先キュー + レビュー SLA 24h |
| **[チーム]** Spec/Code Reviewer の同時ボトルネック | 中 | 小規模 Phase は Code のみ。クリティカルパス PR を最優先 |
| **[チーム]** 結合点 API の後方互換性破壊 | 中 | API freeze + 変更前の関連メンバー通知・合意 |
| **[チーム]** Phase 0.1 差し戻し多発 | 中 | 0.1a で API 契約を先にレビュー。実装着手前に合意 |

---

## テスト観点（最低限）

| テストケース | 検証内容 |
|-------------|---------|
| `start()` 同時呼び出し | 2回/多回呼んでも client が1個だけ |
| `removeTab()` 後のリソース解放 | title observer Job が停止、vmScope が cancel |
| `clearActiveTab()` 中の pending 解除 | permission/question がキャンセルされる |
| 履歴 resume | 過去メッセージが表示され、新規 turn が継続可能 |
| symlink project path | 履歴一覧/読み込みが正しくマッチ |
| タブ切替時の UI state 分離 | 入力欄・スクロール位置がタブごとに独立 |
| dispose 後の late callback | start/sendMessage が no-op |
