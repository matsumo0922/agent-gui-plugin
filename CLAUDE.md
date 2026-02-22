# Claude Code GUI IntelliJ Plugin

IntelliJ IDE 上で Claude Code (CLI) を GUI で操作するためのプラグイン。
`claude-agent-sdk-kotlin` (純 Kotlin/JVM の SDK) を介して Claude Code CLI と通信し、Compose for IDE (Jewel) で UI を描画する。

## プロジェクト構成

```
agent-gui-plugin/
├── claude-agent-sdk-kotlin/  # includeBuild: Claude Agent SDK の Kotlin 実装 (純 JVM)
│   ├── agent/                #   SDK 本体 (ClaudeAgentSDK, ClaudeSDKClient, types, transport 等)
│   ├── demo/                 #   デモアプリ
│   └── CLAUDE.md             #   SDK 固有のドキュメント
├── plugin/                   # IntelliJ プラグイン本体 (Compose UI + Services)
├── docs/design/              # 設計ドキュメント
└── gradle/libs.versions.toml # バージョンカタログ
```

### claude-agent-sdk-kotlin (`claude-agent-sdk-kotlin/`)
Claude Agent SDK の Kotlin/JVM 移植版。`includeBuild()` でプロジェクトに統合。
Claude Code CLI をサブプロセスとして起動し、stdin/stdout の stream-json で通信する。

- 詳細は `claude-agent-sdk-kotlin/CLAUDE.md` を参照
- `settings.gradle.kts` の `dependencySubstitution` で `me.matsumo.claude.agent:agent` → `:agent` にマッピング

主要 API:
- `ClaudeAgentSDK.query()` — ワンショットクエリ (Flow<SDKMessage>)
- `ClaudeAgentSDK.createSession()` / `ClaudeSDKClient` — マルチターンセッション
- `types/` — SDKMessage, ContentBlock, ClaudeAgentOptions 等の型定義

### plugin モジュール (`plugin/`)
IntelliJ プラグイン本体。Compose for IDE (Jewel) で UI を描画。

- `service/SessionService.kt` — プロジェクトレベルのセッション管理
- `service/SettingsService.kt` — アプリケーションレベルの設定 (永続化)
- `viewmodel/ChatViewModel.kt` — チャット UI の状態管理
- `viewmodel/ChatUiState.kt` — UI 状態データクラス
- `viewmodel/mapper/ContentBlockMapper.kt` — SDK ContentBlock → UI モデルのマッピング
- `viewmodel/permission/PermissionHandler.kt` — パーミッション要求の処理
- `viewmodel/permission/ToolNames.kt` — ツール名の表示用マッピング
- `viewmodel/util/JsonUtils.kt` — JSON ユーティリティ
- `toolwindow/AgentToolWindowFactory.kt` — ToolWindow エントリポイント
- `ui/ChatPanel.kt` — メインチャット画面
- `ui/chat/ChatMessageList.kt` — メッセージリスト
- `ui/chat/AssistantMessageBlock.kt` — アシスタントメッセージ表示
- `ui/chat/UserMessageBubble.kt` — ユーザーメッセージ表示
- `ui/chat/ThinkingBlock.kt` — thinking ブロック表示
- `ui/chat/ToolUseBlock.kt` — ツール使用ブロック表示
- `ui/chat/SubAgentTaskCard.kt` — サブエージェントタスクカード
- `ui/component/ChatInputArea.kt` — メッセージ入力エリア
- `ui/component/MarkdownText.kt` — Markdown レンダリング
- `ui/component/CodeBlock.kt` — コードブロック表示
- `ui/component/Button.kt` — 共通ボタンコンポーネント
- `ui/component/PermissionCard.kt` — パーミッション要求カード
- `ui/component/AskUserQuestionCard.kt` — ユーザー質問カード
- `ui/component/AnimatedNullableVisibility.kt` — Nullable 値のアニメーション表示切替
- `ui/component/ErrorBanner.kt` — エラーバナー
- `ui/component/FileAttachPopup.kt` — ファイル添付ポップアップ
- `ui/component/AttachedFileChip.kt` — 添付ファイルチップ
- `ui/theme/ChatTheme.kt` — チャットテーマ定義
- `model/AttachedFile.kt` — 添付ファイルモデル
- `util/FilePickerUtil.kt` — ファイルピッカーユーティリティ

## ビルド手順

```bash
# プラグインのビルド
./gradlew :plugin:build

# 開発用 IDE で実行
./gradlew :plugin:runIde
```

## 重要な技術的制約

### 依存関係スコープ (SDK + IntelliJ Platform)
- `claude-agent-sdk-kotlin` は `kotlinx-serialization-json` と `kotlinx-coroutines-core` を `implementation` で宣言
- IntelliJ Platform が `bundledPlugin("org.jetbrains.kotlin")` 経由でこれらを提供するため、`plugin/build.gradle.kts` で `exclude` を使って ClassLoader 衝突を回避
- exclude 対象: `kotlinx-serialization-json`, `kotlinx-serialization-json-jvm`, `kotlinx-serialization-core`, `kotlinx-serialization-core-jvm`, `kotlinx-coroutines-core`, `kotlinx-coroutines-core-jvm`

### Compose TextFieldValue
- `TextFieldValue(text)` を recomposition ごとに再生成するとカーソル位置がリセットされる
- `mutableStateOf(TextFieldValue)` でローカル管理し、外部変更は `LaunchedEffect` で同期する

## アーキテクチャ

```
Plugin (Compose UI + ViewModel)
    ↓
claude-agent-sdk-kotlin (Pure JVM SDK)
    ↓
Claude Code CLI (subprocess: stdin/stdout stream-json)
```

- Plugin は SDK の `ClaudeSDKClient` を使ってセッション管理
- SDK が Claude Code CLI をサブプロセスとして起動・通信
- ストリーミングレスポンスは `Flow<SDKMessage>` として受信
- パーミッション要求は SDK のコールバック経由で UI に通知

## 技術スタック

- Kotlin 2.1.20 / JVM 17
- IntelliJ Platform 2025.2.4 (sinceBuild: 252.25557)
- IntelliJ Platform Gradle Plugin 2.10.2
- Compose for IDE (Jewel) — `composeUI()`
- claude-agent-sdk-kotlin (includeBuild, 純 JVM)
- kotlinx.serialization / kotlinx.coroutines (IntelliJ Platform 同梱版を使用)
