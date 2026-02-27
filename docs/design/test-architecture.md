# テストベース設計書 — ChatViewModel リファクタリング

> リファクタリング後の各コンポーネントに対する単体テスト設計。
> CLI 実行依存・UI テストは対象外。
> 2026-02-27 (Rev.2: Codex レビュー反映)

---

## 1. 設計方針

### 1.1 本書の位置づけ — テストファースト設計 (TDD)

本書は `chatviewmodel-refactoring-report.md` で提案されたリファクタリング（TurnEngine, SessionCoordinator, SubAgentCoordinator 等の切り出し）を **テストから先に設計する** ためのドキュメントである。

- 本書で言及するクラス（TurnEngine, SubAgentCoordinator 等）は **まだ実装されていない**。テストケースとインターフェースを先に設計し、実装を後追いで行う TDD アプローチを取る
- 既存クラス（PermissionHandler, TranscriptTailer, BranchSessionManager 等）については、現行 API に対するテスト（Phase A）とリファクタリング後 API に対するテスト（Phase B）を区別して記載する

### 1.2 実装移行フェーズ

| フェーズ | 内容 | テスト対象 |
|---------|------|-----------|
| **Phase A: 現行コードへの即時テスト** | 既存クラスの公開 API をそのままテスト。インターフェース導入（SessionFactory 等）も含む | PermissionHandler, ConversationTree, buildContextSystemPrompt, TranscriptParser, PreflightChecker (VERSION_REGEX) |
| **Phase B: リファクタリング後テスト** | 新クラスを切り出した後のテスト | TurnEngine, SubAgentCoordinator, SessionCoordinator, AuthFlowHandler, UsageTracker, BranchSessionManager (SessionFactory 注入後), TranscriptTailer (FileLineReader 注入後) |

Phase A のテストは **リファクタリング着手前に書く**。これにより既存の振る舞いを回帰テストとして固定し、リファクタリング中の壊れを即座に検知できる。

### 1.3 テスト可能性の原則

- **依存の注入**: 外部システム（CLI プロセス、ファイル I/O、SDK クライアント）との境界にインターフェースを導入し、テスト時に Fake/Mock に差し替え可能にする
- **純粋ロジックの分離**: 状態遷移・データ変換・バリデーションは副作用のない純粋関数として切り出し、直接テスト可能にする
- **Coroutines テスト**: `kotlinx-coroutines-test` の `runTest` / `TestScope` / `TestDispatcher` を使い、時間制御・並行制御を決定的にテストする
- **テスト粒度**: コンポーネント単位の単体テストを基本とし、コンポーネント間の結合テストは Fake を組み合わせて実施

### 1.4 テスト対象外

| 対象外 | 理由 |
|--------|------|
| CLI プロセス実行を伴うテスト | 環境依存、CI での実行が困難 |
| Compose UI テスト | IntelliJ Platform Sandbox 必須、UI ロジックはテスト対象外 |
| `PreflightChecker` の実 Process テスト | 外部プロセス依存 |
| SDK の `SubprocessTransport` | CLI 起動が必要 |

### 1.5 追加するテスト依存

```kotlin
// plugin/build.gradle.kts
testImplementation("io.mockk:mockk:1.13.16")
testImplementation("app.cash.turbine:turbine:1.2.0")
```

| ライブラリ | 用途 |
|-----------|------|
| MockK | SDK クライアント等の振る舞いモック |
| Turbine | `StateFlow` / `Flow` のイベント検証 |

---

## 2. テスト境界のインターフェース設計

リファクタリング後のコンポーネントがテスト可能になるために導入するインターフェースと Fake 実装。

### 2.1 `SessionFactory` — SDK クライアント生成の抽象化

**動機**: `BranchSessionManager` と `SessionCoordinator` が直接 `createSession()` / `resumeSession()` を呼んでおり、テスト時に実 CLI プロセスが起動してしまう。

```kotlin
/**
 * SDK クライアントの生成を抽象化するインターフェース。
 * テスト時に FakeSessionFactory に差し替えることで、CLI プロセスなしでテスト可能にする。
 */
interface SessionFactory {
    suspend fun create(config: SessionOptionsBuilder.() -> Unit): ClaudeSDKClient
    suspend fun resume(sessionId: String, config: SessionOptionsBuilder.() -> Unit): ClaudeSDKClient
}

/** プロダクションコード用: 実際の SDK を呼ぶ */
internal class DefaultSessionFactory : SessionFactory {
    override suspend fun create(config: SessionOptionsBuilder.() -> Unit) = createSession(config)
    override suspend fun resume(sessionId: String, config: SessionOptionsBuilder.() -> Unit) = resumeSession(sessionId, config)
}
```

**テスト用 Fake**:

```kotlin
class FakeSessionFactory : SessionFactory {
    /** create/resume が順番に消費するクライアントキュー */
    private val clientQueue = ArrayDeque<ClaudeSDKClient>()

    /** 呼び出し記録 */
    data class CreateCall(val config: SessionOptionsBuilder)
    data class ResumeCall(val sessionId: String, val config: SessionOptionsBuilder)

    val createCalls = mutableListOf<CreateCall>()
    val resumeCalls = mutableListOf<ResumeCall>()

    fun enqueueClient(vararg clients: ClaudeSDKClient) {
        clients.forEach { clientQueue.addLast(it) }
    }

    private fun dequeue(): ClaudeSDKClient =
        clientQueue.removeFirstOrNull() ?: error("FakeSessionFactory: no more clients in queue")

    override suspend fun create(config: SessionOptionsBuilder.() -> Unit): ClaudeSDKClient {
        val builder = SessionOptionsBuilder().apply(config)
        createCalls.add(CreateCall(builder))
        return dequeue()
    }

    override suspend fun resume(sessionId: String, config: SessionOptionsBuilder.() -> Unit): ClaudeSDKClient {
        val builder = SessionOptionsBuilder().apply(config)
        resumeCalls.add(ResumeCall(sessionId, builder))
        return dequeue()
    }
}
```

**注意**: `SessionFactory` は `BranchSessionManager` だけでなく、`SessionCoordinator`（現行の `ChatViewModel.connectSession` 相当）でも使用する。これにより `ChatViewModel` が直接呼んでいた `createSession()` / `resumeSession()` も全てテスト時に差し替え可能になる。

### 2.2 `FileLineReader` — TranscriptTailer のファイル I/O 抽象化

**動機**: `TranscriptTailer` が `RandomAccessFile` / `WatchService` に直接依存しており、単体テストでファイルシステムが必要。

```kotlin
/**
 * ファイルから指定位置以降のバイトを読み取る。
 * TranscriptTailer のファイル I/O を抽象化し、テスト時にインメモリ実装で差し替え可能にする。
 */
interface FileLineReader {
    /** ファイルが存在するか */
    fun exists(filePath: String): Boolean

    /** ファイルサイズを返す */
    fun size(filePath: String): Long

    /** fromPosition から末尾までのバイト列を読み取る */
    fun readBytes(filePath: String, fromPosition: Long): ByteArray
}

internal class DefaultFileLineReader : FileLineReader {
    override fun exists(filePath: String) = Files.exists(Path.of(filePath))
    override fun size(filePath: String) = Files.size(Path.of(filePath))
    override fun readBytes(filePath: String, fromPosition: Long): ByteArray {
        RandomAccessFile(Path.of(filePath).toFile(), "r").use { raf ->
            val fileSize = raf.length()
            if (fileSize <= fromPosition) return ByteArray(0)
            raf.seek(fromPosition)
            val buffer = ByteArray((fileSize - fromPosition).toInt())
            raf.readFully(buffer)
            return buffer
        }
    }
}
```

**テスト用 Fake**:

```kotlin
class FakeFileLineReader : FileLineReader {
    /** filePath → content bytes (追記シミュレーション可能) */
    private val files = mutableMapOf<String, ByteArray>()

    fun setContent(filePath: String, content: String) {
        files[filePath] = content.toByteArray(Charsets.UTF_8)
    }

    fun appendContent(filePath: String, content: String) {
        val existing = files[filePath] ?: ByteArray(0)
        files[filePath] = existing + content.toByteArray(Charsets.UTF_8)
    }

    override fun exists(filePath: String) = filePath in files
    override fun size(filePath: String) = files[filePath]?.size?.toLong() ?: 0L
    override fun readBytes(filePath: String, fromPosition: Long): ByteArray {
        val bytes = files[filePath] ?: return ByteArray(0)
        if (fromPosition >= bytes.size) return ByteArray(0)  // DefaultFileLineReader と同じ挙動
        return bytes.copyOfRange(fromPosition.toInt(), bytes.size)
    }
}
```

**注意**: `WatchService` によるファイル監視イベントのトリガーは抽象化しない。代わりに `TranscriptTailer` の `readNewLines` ロジック（バイト読み取り → 行分割 → パース）を独立したテスト可能なメソッドとして切り出し、ファイル監視ループとは分離する。

### 2.3 `Clock` — 時刻取得の抽象化

**動機**: `SubAgentTask` の `startedAt` / `completedAt` が `System.currentTimeMillis()` に直接依存。テストで時刻を制御できない。

```kotlin
fun interface Clock {
    fun currentTimeMillis(): Long

    companion object {
        val System: Clock = Clock { java.lang.System.currentTimeMillis() }
    }
}

/** テスト用: 明示的に時刻を制御 */
class FakeClock(var now: Long = 0L) : Clock {
    override fun currentTimeMillis() = now
    fun advance(millis: Long) { now += millis }
}
```

---

## 3. コンポーネント別テスト設計

### 3.1 `PermissionHandler`

**テスト可能性**: 既に lambda injection で依存注入済み。テスト容易。

#### Phase A: 現行 API のテスト（リファクタリング前に書く）

| # | テストケース | 検証内容 |
|---|------------|---------|
| A1 | Permission 要求→許可応答 | `request()` が `PermissionResultAllow` を返す |
| A2 | Permission 要求→拒否応答 | `PermissionResultDeny(message)` を返す |
| A3 | Question 要求→回答応答 | `PermissionResultAllow(updatedInput=...)` を返す |
| A4 | `cancelPending` で deferred がキャンセル | `request()` が `CancellationException` を throw |
| A5 | 直列化 (Semaphore) | 2つの同時 `request()` が直列に処理される |
| A6 | 応答後の UI 状態クリア | `pendingPermission` / `pendingQuestion` が null に戻る |
| A7 | 空文字 denyMessage のフォールバック | `denyMessage` が空なら `"Denied by user"` になる |
| A8 | Question 中に `respondPermission(false)` | 現行では Deny として完了する（UI の質問キャンセル動作） |

#### Phase B: リファクタリング後のテスト（型ガード + cancelActiveRequest 追加後）

| # | テストケース | 検証内容 |
|---|------------|---------|
| B1 | `respondPermission` で Question 型ガード | `active.type == Question` の場合、`respondPermission` は何もしない |
| B2 | `respondQuestion` で Permission 型ガード | `active.type == Permission` の場合、`respondQuestion` は何もしない |
| B3 | `cancelActiveRequest` | 型を問わず Deny で完了する |
| B4 | Phase A の A1-A7 が全て引き続きパスする | リファクタリングの回帰テスト |

**テストコード例**:

```kotlin
class PermissionHandlerTest {
    private var state = ChatUiState()
    private val handler = PermissionHandler(
        currentState = { state },
        updateState = { transform -> state = transform(state) },
    )

    @Test
    fun `respondPermission ignores when active type is Question`() = runTest {
        // Question リクエストを発行（別コルーチンで await 中）
        val job = launch {
            handler.request(ToolNames.ASK_USER_QUESTION, mapOf("questions" to listOf<Any>()))
        }
        advanceUntilIdle()

        // Permission として応答を試みる → 無視される
        handler.respondPermission(allow = true)

        // deferred はまだ未完了
        assertTrue(job.isActive)

        // cancelActiveRequest で正しくキャンセルできる
        handler.cancelActiveRequest()
        advanceUntilIdle()
        assertTrue(job.isCompleted)
    }
}
```

### 3.2 `TranscriptTailer` (readNewLines ロジック)

**リファクタリング方針**: `readNewLines` を `FileLineReader` ベースの純粋ロジックに切り出す。ファイル監視ループ（`WatchService`）は別途薄いラッパーとして残す。

**テストケース**:

| # | テストケース | 検証内容 |
|---|------------|---------|
| 1 | 完全な JSONL 行を正しくパース | `onMessage` が各行に対して呼ばれる |
| 2 | 空行をスキップ | `parseLine` が null を返す行は無視 |
| 3 | partial line の保護 | 末尾が `\n` で終わらない場合、不完全行を読み飛ばさず pos を巻き戻す |
| 4 | 追記読み取り | `fromPosition` 以降のみ読み取り、既読部分は再処理しない |
| 5 | ファイル未存在 | `exists() == false` なら pos = 0 を返す |
| 6 | 読み取りエラー時のフォールバック | 例外時に `fromPosition` を返す（位置を進めない） |
| 7 | ファイル truncate/rotate | `size < fromPosition` の場合、pos を 0 にリセットして先頭から再読み取り |
| 8 | 全行がパース失敗 | `parseLine` が全て null → `onMessage` 未呼び出し、pos は正しく進む |

**テストコード例**:

```kotlin
class TranscriptTailerReadTest {
    private val fakeReader = FakeFileLineReader()

    @Test
    fun `partial line is not consumed`() {
        // 1行目は完全、2行目は途中で切れている
        val content = """{"type":"assistant","message":{"content":[{"type":"text","text":"hello"}]}}
{"type":"assistant","messa"""
        fakeReader.setContent("/tmp/test.jsonl", content)

        val messages = mutableListOf<ChatMessage>()
        val newPos = readNewLines(fakeReader, "/tmp/test.jsonl", 0L) { messages.add(it) }

        // 1行目のみパースされる
        assertEquals(1, messages.size)
        // pos は1行目の末尾 + \n まで（2行目の不完全部分は含まない）
        val expectedPos = content.indexOf('\n').toLong() + 1
        assertEquals(expectedPos, newPos)
    }
}
```

### 3.3 `UsageTracker`

**テスト可能性**: 純粋なステートマシン。外部依存なし。

**テストケース**:

| # | テストケース | 検証内容 |
|---|------------|---------|
| 1 | 初期状態 | `usage` が全てゼロ |
| 2 | `onMessageStart` でコンテキスト使用率計算 | `inputTokens / contextWindow` が正しい |
| 3 | cache トークンの加算 | `inputTokens + cacheCreation + cacheRead` が合算される |
| 4 | `onResult` でコスト更新 | `totalCostUsd` が更新される |
| 5 | `onResult(null)` で既存値維持 | `totalCostUsd` が変わらない |
| 6 | 使用率の上限クランプ | `contextWindow` を超える入力で `1.0f` にクランプ |

**テストコード例**:

```kotlin
class UsageTrackerTest {
    @Test
    fun `context usage is calculated correctly`() {
        val tracker = UsageTracker()
        tracker.onMessageStart(inputTokens = 100_000L, cacheCreation = 0L, cacheRead = 0L)

        assertEquals(0.5f, tracker.usage.value.contextUsage, 0.001f)
        assertEquals(100_000L, tracker.usage.value.totalInputTokens)
    }

    @Test
    fun `context usage clamps to 1`() {
        val tracker = UsageTracker()
        tracker.onMessageStart(inputTokens = 300_000L, cacheCreation = 0L, cacheRead = 0L)

        assertEquals(1.0f, tracker.usage.value.contextUsage)
    }
}
```

### 3.4 `SubAgentCoordinator`

**テスト可能性**: `Mutex` / actor で内部状態を保護。`Clock` を注入して時刻制御。`StateFlow<Map<String, SubAgentTask>>` を Turbine で検証。

**テストケース**:

| # | テストケース | 検証内容 |
|---|------------|---------|
| 1 | `onSubAgentStart` でタスク作成 | `tasks` に hookId キーでエントリが追加される |
| 2 | `resolveParentToolUseId` で re-key | hookId → realId にキーが変わる |
| 3 | re-key 時の既存メッセージ保持 | hookId 時代のメッセージが realId に引き継がれる |
| 4 | `onSubAgentStop` で完了時刻設定 | `completedAt` が設定される |
| 5 | `stopAll` で全 tailer 停止 | `activeTailers` が空になる |
| 6 | 並行アクセスの安全性 | 複数コルーチンから同時に start/stop しても例外なし |
| 7 | `updateSpawnedByToolName` | toolName が正しく設定される |
| 8 | セッション ID によるフィルタリング | `timelineSessionId` が正しく設定される |

**テストコード例**:

```kotlin
class SubAgentCoordinatorTest {
    @Test
    fun `resolveParentToolUseId re-keys task from hookId to realId`() = runTest {
        val clock = FakeClock(1000L)
        val coordinator = SubAgentCoordinator(backgroundScope, clock)

        coordinator.onSubAgentStart(
            agentId = "agent-1",
            transcriptPath = "/tmp/transcript",
            hookToolUseId = "hook-uuid-1",
            sessionId = "session-1",
        )

        // hookId で登録されている
        assertEquals(1, coordinator.tasks.value.size)
        assertNotNull(coordinator.tasks.value["hook-uuid-1"])

        // realId で解決
        coordinator.resolveParentToolUseId("hook-uuid-1", "toolu_abc123")

        // re-key されている
        assertNull(coordinator.tasks.value["hook-uuid-1"])
        val task = coordinator.tasks.value["toolu_abc123"]
        assertNotNull(task)
        assertEquals("toolu_abc123", task.id)
        assertEquals("session-1", task.timelineSessionId)
    }
}
```

### 3.5 `TurnEngine`

**テスト可能性**: `ClaudeSDKClient` を MockK でモック。`receiveResponse()` が返す `Flow<SDKMessage>` をテストデータで制御。

**テストケース**:

| # | テストケース | 検証内容 |
|---|------------|---------|
| 1 | 正常ターン完了 | `onEvent` に System → Assistant → Result の順でイベントが届く |
| 2 | テキスト送信 | `client.send(text)` が呼ばれる |
| 3 | ファイル付き送信 | `client.send(contentBlocks)` が呼ばれる |
| 4 | `invalidateCurrentTurn` | 新しい turnId が返り、旧ターンのイベントが無視される |
| 5 | `cancel` | `activeTurnJob` がキャンセルされる |
| 6 | 例外発生時 | `onError` が呼ばれる |
| 7 | ターン中のキャンセル | `CancellationException` が正しく伝播される |
| 8 | sub-agent メッセージのフィルタリング | `parentToolUseId != null` の StreamEvent は `onEvent` に届かない |

**テストコード例**:

```kotlin
class TurnEngineTest {
    @Test
    fun `dispatch collects response and emits events`() = runTest {
        val client = mockk<ClaudeSDKClient> {
            coEvery { send(any<String>()) } just runs
            every { receiveResponse() } returns flowOf(
                SystemMessage(sessionId = "s1", isInit = true),
                AssistantMessage(
                    content = listOf(ContentBlock.Text("Hello")),
                    uuid = "msg-1",
                ),
                ResultMessage(
                    totalCostUsd = 0.01,
                    isError = false,
                    subtype = null,
                ),
            )
        }

        val events = mutableListOf<TurnEngine.TurnEvent>()
        val engine = TurnEngine(backgroundScope)

        val job = engine.dispatch(client, "test") { events.add(it) }
        job.join()

        assertEquals(3, events.size)
        assertIs<TurnEngine.TurnEvent.System>(events[0])
        assertIs<TurnEngine.TurnEvent.Assistant>(events[1])
        assertIs<TurnEngine.TurnEvent.Result>(events[2])

        coVerify { client.send("test") }
    }
}
```

### 3.6 `BranchSessionManager`

**テスト可能性**: `SessionFactory` を注入。`ClaudeSDKClient` を MockK でモック。

**テストケース**:

| # | テストケース | 検証内容 |
|---|------------|---------|
| 1 | `createEditBranchSession` | `sessionFactory.create` が呼ばれ、`activeSessions` に登録 |
| 2 | `getOrResumeSession` (キャッシュヒット) | 既存 client がそのまま返る |
| 3 | `getOrResumeSession` (キャッシュミス) | `sessionFactory.resume` が呼ばれ、新 client が返る |
| 4 | `removeSession` | `activeSessions` から削除され、client が close される |
| 5 | `closeAll` | 全 client が close され、map が空になる |
| 6 | `closeAll` と `createEditBranchSession` の並行安全性 | mutex で保護されて競合しない (**前提**: リファクタリングレポート §3.6 に従い `createEditBranchSession` も `sessionMutex` で保護する修正を適用後) |
| 7 | コンテキストプロンプトの内容検証 | `systemPrompt` にメッセージ履歴が含まれる |
| 8 | `applyCommonConfig` が正しく適用される | model, permissionMode が config に反映される |

**テストコード例**:

```kotlin
class BranchSessionManagerTest {
    @Test
    fun `getOrResumeSession returns cached client`() = runTest {
        val factory = FakeSessionFactory()
        val mockClient = mockk<ClaudeSDKClient>(relaxed = true) {
            every { sessionId } returns "branch-1"
        }
        factory.enqueueClient(mockClient)

        val manager = BranchSessionManager(factory) { model, mode ->
            this.model = model
            this.permissionMode = mode
        }

        // createEditBranchSession で activeSessions に登録
        val created = manager.createEditBranchSession(
            messagesBeforeEdit = emptyList(),
            originalAttachedFiles = emptyList(),
            model = Model.SONNET,
            permissionMode = PermissionMode.DEFAULT,
        )

        // 同じ sessionId で get → キャッシュから返る（factory.resume は呼ばれない）
        val cached = manager.getOrResumeSession("branch-1", Model.SONNET, PermissionMode.DEFAULT)
        assertSame(created, cached)

        // create は1回、resume は0回
        assertEquals(1, factory.createCalls.size)
        assertEquals(0, factory.resumeCalls.size)
    }
}
```

### 3.7 `SessionCoordinator`

**テスト可能性**: `SessionFactory` と `PreflightChecker` を注入。

**テストケース**:

| # | テストケース | 検証内容 |
|---|------------|---------|
| 1 | `connect` 成功 | `sessionState` が `Disconnected → Connecting → Ready` に遷移 |
| 2 | `connect` エラー | `sessionState` が `Error` に遷移、`errorMessage` が設定される |
| 3 | `switchClient` で旧 client が close | 旧 client の `close()` が呼ばれる |
| 4 | `switchToBranch` | `branchSessionManager.getOrResumeSession` が呼ばれる |
| 5 | `closeAll` | client + branchSessionManager 両方が close |
| 6 | 多重起動防止 | 同時に2回 `connect` しても1回しか実行されない |
| 7 | dispose 後の connect は no-op | `disposed` フラグが立っていたら何もしない |

### 3.8 `AuthFlowHandler`

**テスト可能性**: `Process` / `BufferedReader` / `OutputStreamWriter` はコンストラクタで渡されるのでモック可能。

**テストケース**:

| # | テストケース | 検証内容 |
|---|------------|---------|
| 1 | `startAuth` で状態が `isActive = true` | `state.value.isActive` が true |
| 2 | `sendInput` でテキスト送信 | stdin に書き込まれる |
| 3 | stdout 読み取りで `outputLines` 更新 | `state.value.outputLines` にリアルタイム反映 |
| 4 | `confirmComplete` で cleanup | isActive = false、Process が destroy される |
| 5 | プロセス終了の自動検知 | `readLine()` が null → `confirmComplete` が呼ばれる |

### 3.9 `PreflightChecker` — VERSION_REGEX の純粋ロジックテスト (Phase A)

**テスト可能性**: `check()` メソッド全体は外部プロセス依存だが、VERSION_REGEX の判定ロジックは純粋関数として切り出しテスト可能。`Companion.VERSION_REGEX` を公開するか、バージョン判定メソッドを `internal` で公開する。

**テストケース**:

| # | テストケース | 検証内容 |
|---|------------|---------|
| 1 | `"1.0.50"` → バージョン検出 | `VERSION_REGEX` がマッチし `"1.0.50"` を抽出 |
| 2 | `"claude-code 1.2.3-beta"` → バージョン検出 | 前後テキストがあっても `"1.2.3"` を抽出 |
| 3 | `"Please login..."` → 非バージョン | `VERSION_REGEX` がマッチしない |
| 4 | 空文字列 → 非バージョン | マッチしない |
| 5 | `cliPath == null` のハンドリング | `Ready("auto-discovery")` を返す（Process 不要なのでテスト可能） |

**リファクタリング提案**: バージョン判定を `internal` メソッドとして切り出す:

```kotlin
internal fun parseVersion(line: String): String? =
    VERSION_REGEX.find(line)?.groupValues?.get(1)
```

### 3.10 `buildContextSystemPrompt` (純粋関数)

**テスト可能性**: 完全に純粋関数。外部依存ゼロ。

**テストケース**:

| # | テストケース | 検証内容 |
|---|------------|---------|
| 1 | User メッセージのみ | `[User]: text` 形式で出力 |
| 2 | Assistant メッセージ (テキスト) | `[Assistant]: text` 形式で出力 |
| 3 | Assistant メッセージ (ツール使用) | `[Tool used: name]` + target/command の要約 |
| 4 | 添付ファイル付き User | `(Attached files: ...)` が出力 |
| 5 | Interrupted メッセージ | `[System: Response was interrupted]` |
| 6 | 空リスト | ヘッダーとフッターのみ |
| 7 | originalAttachedFiles 付き | `Previously attached files:` セクションが出力 |

### 3.11 `ConversationTree` (既存テスト拡張)

**テスト可能性**: 既に 50+ テストあり。追加テストケースのみ。

**追加テストケース**:

| # | テストケース | 検証内容 |
|---|------------|---------|
| 1 | `getMessagesBeforeSlot` | 指定 editGroupId より前のメッセージのみ返す |
| 2 | `getActiveLeafSessionId` | 最深タイムラインの `branchSessionId` を返す |
| 3 | `editMessage` で同一テキストの場合 | ツリーが変化しない（呼び出し元で検証するが、前提条件の確認） |
| 4 | 複数ブランチの navigate | `activeTimelineIndex` が正しく切り替わる |

### 3.12 `TranscriptParser` (純粋関数)

**テスト可能性**: `parseTranscriptLine` (SDK internal) への依存があるが、入力は文字列、出力は `ChatMessage?` の純粋変換。

**テストケース**:

| # | テストケース | 検証内容 |
|---|------------|---------|
| 1 | Assistant メッセージのパース | テキストブロックが `UiContentBlock.Text` に変換 |
| 2 | User メッセージのパース | テキストが `ChatMessage.User.text` に設定 |
| 3 | 不正 JSON | `null` を返す |
| 4 | 空行 | `null` を返す |
| 5 | ToolUse ブロック付き Assistant | `UiContentBlock.ToolUse` に変換 |
| 6 | サポート外メッセージ型 | `null` を返す |
| 7 | JsonArray 内に非 object 要素 | `extractUserText` が例外せず null or テキスト部分のみ返す |
| 8 | content が null の UserMessage | `null` を返す |

---

## 4. テストヘルパーとフィクスチャ

### 4.1 共通テストフィクスチャ

```kotlin
/** テスト用メッセージビルダー（ConversationTreeTest の既存パターンに合わせる） */
object TestFixtures {
    fun userMsg(
        text: String,
        editGroupId: String = "eg-${text.hashCode()}",
        attachedFiles: List<AttachedFile> = emptyList(),
    ) = ChatMessage.User(
        id = UUID.randomUUID().toString(),
        editGroupId = editGroupId,
        text = text,
        attachedFiles = attachedFiles,
    )

    fun assistantMsg(
        text: String,
        id: String = UUID.randomUUID().toString(),
    ) = ChatMessage.Assistant(
        id = id,
        blocks = listOf(UiContentBlock.Text(text)),
    )

    fun interruptedMsg() = ChatMessage.Interrupted(
        id = UUID.randomUUID().toString(),
    )

    /** SDK AssistantMessage を生成 */
    fun sdkAssistantMessage(
        text: String,
        uuid: String = UUID.randomUUID().toString(),
        parentToolUseId: String? = null,
    ) = AssistantMessage(
        content = listOf(ContentBlock.Text(text)),
        uuid = uuid,
        parentToolUseId = parentToolUseId,
    )

    /** SDK ResultMessage を生成 */
    fun sdkResultMessage(
        totalCostUsd: Double? = 0.01,
        isError: Boolean = false,
    ) = ResultMessage(
        totalCostUsd = totalCostUsd,
        isError = isError,
        subtype = if (isError) "error" else null,
    )
}
```

### 4.2 MockK ヘルパー

```kotlin
/** テスト用の relaxed ClaudeSDKClient モック */
fun createMockClient(
    sessionId: String = "test-session",
    responses: Flow<SDKMessage> = emptyFlow(),
): ClaudeSDKClient = mockk(relaxed = true) {
    every { this@mockk.sessionId } returns sessionId
    every { receiveResponse() } returns responses
}
```

---

## 5. テストファイル構成

```
plugin/src/test/kotlin/me/matsumo/agentguiplugin/
├── viewmodel/
│   ├── ConversationTreeTest.kt              # 既存（§3.11 で拡張）
│   ├── BranchSessionManagerTest.kt          # §3.6 [Phase B]
│   ├── BuildContextSystemPromptTest.kt      # §3.10 [Phase A]
│   ├── permission/
│   │   └── PermissionHandlerTest.kt         # §3.1 [Phase A + B]
│   ├── transcript/
│   │   ├── TranscriptTailerReadTest.kt      # §3.2 [Phase B]
│   │   └── TranscriptParserTest.kt          # §3.12 [Phase A]
│   ├── preflight/
│   │   └── PreflightCheckerTest.kt          # §3.9 [Phase A]
│   ├── coordinator/
│   │   ├── SubAgentCoordinatorTest.kt       # §3.4 [Phase B]
│   │   ├── SessionCoordinatorTest.kt        # §3.7 [Phase B]
│   │   └── AuthFlowHandlerTest.kt           # §3.8 [Phase B]
│   ├── engine/
│   │   └── TurnEngineTest.kt               # §3.5 [Phase B]
│   └── UsageTrackerTest.kt                  # §3.3 [Phase B]
└── testutil/
    ├── TestFixtures.kt                      # §4.1
    ├── FakeSessionFactory.kt                # §2.1
    ├── FakeFileLineReader.kt                # §2.2
    ├── FakeClock.kt                         # §2.3
    └── MockClientHelpers.kt                 # §4.2
```

---

## 6. テスト実行方針

### 6.1 実行コマンド

```bash
# 純粋単体テスト（IntelliJ Platform Sandbox 不要）
./gradlew :plugin:unitTest

# 全テスト（IntelliJ Platform Sandbox 含む）
./gradlew :plugin:test
```

全テストは `unitTest` タスクで実行可能とする。IntelliJ Platform 依存のテストは不要（Compose UI テスト対象外のため）。

### 6.2 テストカバレッジ目標

| コンポーネント | フェーズ | 目標カバレッジ | 理由 |
|--------------|---------|-------------|------|
| PermissionHandler | A+B | 90%+ | バグ修正の回帰防止（リファクタリングレポート §3.3） |
| TranscriptTailer readNewLines | B | 90%+ | バグ修正の回帰防止（リファクタリングレポート §3.4） |
| PreflightChecker (VERSION_REGEX) | A | 80%+ | 純粋ロジック部分のみ |
| UsageTracker | B | 95%+ | 純粋ロジック、完全テスト可能 |
| SubAgentCoordinator | B | 85%+ | 並行制御のコーナーケースが多い |
| TurnEngine | B | 80%+ | MockK 依存だが主要パスをカバー |
| BranchSessionManager | B | 80%+ | SessionFactory 注入後は容易 |
| SessionCoordinator | B | 75%+ | 状態遷移パスが多い |
| AuthFlowHandler | B | 70%+ | Process I/O モックの制約 |
| ConversationTree | A (既存) | 90%+ | 既にカバレッジ高い |
| buildContextSystemPrompt | A | 95%+ | 純粋関数、完全テスト可能 |
| TranscriptParser | A | 85%+ | SDK internal 依存の制約 |

---

## 7. リファクタリング対象のコンストラクタ変更まとめ

テスト可能性のために各コンポーネントのコンストラクタに追加するパラメータ。プロダクションコードではデフォルト値を使い、テストコードでは Fake/Mock を注入する。

```kotlin
// BranchSessionManager — SessionFactory 注入
class BranchSessionManager(
    private val sessionFactory: SessionFactory = DefaultSessionFactory(),  // NEW
    private val applyCommonConfig: SessionOptionsBuilder.(Model, PermissionMode) -> Unit,
)

// TranscriptTailer — FileLineReader 注入
internal class TranscriptTailer(
    private val scope: CoroutineScope,
    private val fileReader: FileLineReader = DefaultFileLineReader(),  // NEW
)

// SubAgentCoordinator — Clock 注入
class SubAgentCoordinator(
    private val scope: CoroutineScope,
    private val clock: Clock = Clock.System,  // NEW
)

// SessionCoordinator — SessionFactory + PreflightChecker 注入
class SessionCoordinator(
    private val projectBasePath: String,
    private val claudeCodePath: String?,
    private val sessionFactory: SessionFactory = DefaultSessionFactory(),  // NEW
    private val preflightChecker: PreflightChecker = PreflightChecker(),   // NEW
    private val configApplier: SessionOptionsBuilder.(Model, PermissionMode) -> Unit,
)

// UsageTracker — contextWindow を定数 or パラメータ化
class UsageTracker(
    private val contextWindow: Long = DEFAULT_CONTEXT_WINDOW,  // NEW
)
```

---

## 付録: Codex レビュー対応

| # | Codex 指摘 | 判断 | 対応 |
|---|-----------|------|------|
| 1 | 設計書が未実装クラス前提 | 却下 | TDD アプローチを §1.1 で明記。Phase A/B の分離を §1.2 で追加 |
| 2 | PermissionHandler の cancelActiveRequest が未実装 | 一部受入 | Phase A（現行 API）と Phase B（リファクタ後）に分離。§3.1 |
| 3 | SessionFactory を connectSession にも適用すべき | 受入 | §2.1 に注記追加。SessionCoordinator 経由で全経路をカバー |
| 4 | BranchSessionManager mutex 前提が現実装と不一致 | 却下 | リファクタリングレポート §3.6 の修正後が前提。TDD 設計書の趣旨 |
| 5 | FakeFileLineReader の fromPosition > size で例外 | 受入 | §2.2 の Fake に境界チェック追加 |
| 6 | TranscriptTailer truncate/rotate ケース不足 | 受入 | §3.2 にケース 7, 8 を追加 |
| 7 | FakeSessionFactory が検証用途として弱い | 受入 | §2.1 を queue 化 + createCalls/resumeCalls 記録に改善 |
| 8 | PreflightChecker のテスト設計が欠落 | 一部受入 | §3.9 を新設。VERSION_REGEX 純粋ロジック + cliPath==null ケース |
| 9 | TranscriptParser の JsonArray 異常系不足 | 受入 | §3.12 にケース 7, 8 を追加 |
| 10 | 節番号ズレ・移行順序の欠如 | 一部受入 | 節番号修正 + §1.2 で実装移行フェーズを追加 |

---

*テストベース設計書作成: Claude / 2026-02-27*
*Rev.2: Codex レビュー反映 / 2026-02-27*
