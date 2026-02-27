# ChatViewModel 責務分離・問題点分析レポート (Codex)

## 1. エグゼクティブサマリー

`ChatViewModel` は 881 行に対して責務が過密で、状態遷移・I/O・SDK 依存・UI 更新が同居しています。
特に `sub-agent tailing` と `branch/session 切替` は競合しやすく、`hookToolUseId` 解決は順序依存で壊れやすい実装です（`ChatViewModel.kt:445`, `ChatViewModel.kt:518`）。
改善の主軸は「VM を orchestration のみに縮小」「状態更新の単一化（Reducer）」「可変共有状態を actor/Mutex へ集約」「SDK 内部知識の SDK 側移管」です。

## 2. 現状分析（責務の一覧と問題点）

1. セッション管理は `start/clear/dispose/reconnect` が VM 直下にあり、`auth` と混在（`ChatViewModel.kt:119`, `ChatViewModel.kt:162`, `ChatViewModel.kt:820`）。
2. 送受信処理が `sendMessage` と `editMessage` で重複（`ChatViewModel.kt:305`, `ChatViewModel.kt:617`）。
3. `hookToolUseId -> parentToolUseId` 解決が FIFO 依存（`unresolvedHookIds.removeFirst()`）で、複数 sub-agent 並列時に誤マッピングのリスク（`ChatViewModel.kt:448`）。
4. `activeTailers/tailerKeyRefs/agentKeyRefs/unresolvedHookIds` が `mutableMap/mutableList` で保護なし（`ChatViewModel.kt:101`）。
5. `BranchSessionManager` は分離済みだが、`client` 切替ロジックが VM 側に残存（`ChatViewModel.kt:759`, `BranchSessionManager.kt:18`）。
6. `PermissionHandler` は request type を検証せず応答可能（誤 UI 操作で不整合）（`PermissionHandler.kt:64`, `PermissionHandler.kt:71`）。
7. `TranscriptTailer` は partial line を破棄し得る（読取位置を進めるため JSON 断片を再試行しない）（`TranscriptTailer.kt:60`）。
8. `ChatUiState` の `activeMessages` は getter で毎回ツリー走査し、再コンポーズ時コスト増（`ChatUiState.kt:51`, `ConversationTree.kt:109`）。

## 3. 責務分離の提案（具体的なクラス/インターフェース設計）

提案構成:
1. **`ChatOrchestrator`**（現 VM）: UI event を受けて UseCase 呼び出しのみ。
2. **`SessionLifecycleService`**: `start/clear/reconnect/dispose` と preflight/auth。
3. **`TurnEngine`**: `send/edit/abort` と stream collect 共通化。
4. **`SubAgentTaskService`**: tailing 開始停止・task 更新・ID 解決。
5. **`BranchCoordinator`**: 分岐セッション選択と `client` 切替一元化。
6. **`ChatStateStore` + `Reducer`**: `_uiState.update` を一箇所に限定。
7. **`AttachmentStore`** / **`TokenUsageTracker`**: 細粒度な小責務分離。

最小インターフェース例:
```kotlin
interface TurnEngine {
    suspend fun runTurn(input: TurnInput): Flow<TurnEvent>
    suspend fun interrupt()
}

sealed interface TurnEvent {
    data class AssistantDelta(val msg: ChatMessage.Assistant): TurnEvent
    data class Usage(val inputTokens: Long, val totalCostUsd: Double?): TurnEvent
    data class Error(val reason: String): TurnEvent
}
```

## 4. コードリーダビリティの改善提案

1. `sendMessage` と `editMessage` の collect 分岐を共通関数化。
2. `_uiState.update` の分散をやめ、`dispatch(Action)` -> `reduce` へ統一。
3. `SessionState` 遷移表を明示（不正遷移をガード）。
4. `magic number` 抽出: `contextWindow=200_000`, tail poll interval 等（`ChatViewModel.kt:501`, `TranscriptTailer.kt:38`）。
5. `applyCommonConfig` の hook 処理は別クラスへ（VM の認知負荷を削減）。

## 5. Kotlin/Coroutines の活用提案

1. 可変共有状態（tailers/key map）を `actor` で直列化。
2. `CoroutineScope(SupervisorJob())` に dispatcher 明示（`Dispatchers.Main.immediate` か専用 single-thread dispatcher）。
3. `startJob/activeTurnJob/branchSwitchJob` の job 管理を `JobController` に集約。
4. `PermissionHandler` は `active.type` 検証を追加。

```kotlin
fun respondPermission(allow: Boolean, deny: String) {
    val req = active ?: return
    if (req.type != RequestType.Permission) return
    req.deferred.complete(if (allow) PermissionResultAllow() else PermissionResultDeny(deny))
}
```

## 6. Compose ReComposition への配慮

1. `ChatUiState` を分割: `sessionUiState`, `conversationUiState`, `authUiState`, `subAgentUiState`。
2. `activeMessages` は getter 計算を避け、Reducer 内で更新済み値を保持。
3. `authOutputLines` と `subAgentTasks` の高頻度更新は別 `StateFlow` に分離。
4. `@Stable` より `@Immutable` 前提設計に寄せる（不変コレクション採用）。

## 7. スレッドセーフティの問題と改善

1. `mutableMap/mutableList`（tailer 管理）は Mutex/actor で保護必須（`ChatViewModel.kt:101`）。
2. `client` は `@Volatile` でも複合操作は非原子的。`client` と `sessionId` 更新を同一クリティカルセクションへ。
3. `BranchSessionManager.createEditBranchSession` も `sessionMutex` 保護対象へ（`BranchSessionManager.kt:30`）。
4. `TranscriptTailer` は partial line バッファを保持し、未完成行を次回へ繰越。

## 8. SDK への移行候補

1. `hookToolUseId -> parentToolUseId` 解決ロジック（VM で持つべきでない）。
2. sub-agent transcript path 解決（現在は文字列組立）（`ChatViewModel.kt:521`）。
3. `parseTranscriptLine` が SDK internal 依存。公開 API 化が妥当（`TranscriptParser.kt:11`）。
4. stream-json の usage 集計方針（単発/累積）を SDK で正規化。
5. tool-use/event correlation ID を SDK event model に追加。

## 9. 優先度付き改善ロードマップ

1. **P0（即時）**: `hookId` FIFO 解決の廃止、tailer 管理の排他、`PermissionHandler` type チェック。
2. **P1（短期）**: `send/edit` の TurnEngine 共通化、`client/sessionId` 切替責務を `BranchCoordinator` へ移動。
3. **P1（短期）**: `TranscriptTailer` の partial line 保持実装。
4. **P2（中期）**: `Reducer + Action` 導入、`ChatUiState` 分割で Compose 再描画コストを低減。
5. **P2（中期）**: `BranchSessionManager` の同期戦略統一（全 write を mutex 下へ）。
6. **P3（中長期）**: SDK 側に sub-agent correlation/transcript API を追加し、VM から内部知識を撤去。

---

*レポート作成: Codex (OpenAI) / 2026-02-27*
