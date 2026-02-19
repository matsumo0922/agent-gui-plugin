# Claude Code GUI IntelliJ Plugin

IntelliJ IDE 上で Claude Code (CLI) を GUI で操作するためのプラグイン。
`@anthropic-ai/claude-agent-sdk` を Node.js ブリッジ経由で呼び出し、Compose for IDE (Jewel) で UI を描画する。

## プロジェクト構成

```
agent-gui-plugin/
├── bridge/              # Kotlin モジュール: SDK通信の抽象化層
├── bridge-scripts/      # Node.js: Claude Agent SDK を呼ぶブリッジスクリプト
├── plugin/              # IntelliJ プラグイン本体 (Compose UI + Services)
└── docs/design/         # 設計ドキュメント
```

### bridge モジュール (`bridge/`)
SDK との通信プロトコルを担当。IntelliJ Platform への依存なし。

- `model/` — データモデル (`BridgeCommand`, `BridgeEvent`, `SessionOptions`, `ContentBlock` 等)
- `client/BridgeClient.kt` — Node.js プロセスとの JSONL 通信クライアント
- `process/NodeProcess.kt` — Node.js 子プロセスの管理 (stdin/stdout)
- `process/NodeResolver.kt` — Node.js 実行ファイルの自動検出
- `process/ClaudeCodeResolver.kt` — Claude Code CLI の自動検出 (`which claude` をログインシェルで実行)
- `process/BridgeScriptExtractor.kt` — JAR 内の bridge スクリプトを一時ディレクトリに展開

### bridge-scripts モジュール (`bridge-scripts/`)
Node.js で動作するブリッジスクリプト。`@anthropic-ai/claude-agent-sdk` の `query()` を呼び出す。

- `main.mjs` — ブリッジスクリプト本体 (stdin/stdout で JSONL 通信)
- `esbuild.config.mjs` — esbuild でバンドルして `plugin/src/main/resources/bridge/main.mjs` に出力
- ビルド: `cd bridge-scripts && npm install && npm run build`

### plugin モジュール (`plugin/`)
IntelliJ プラグイン本体。Compose for IDE (Jewel) で UI を描画。

- `service/SessionService.kt` — プロジェクトレベルのセッション管理
- `service/SettingsService.kt` — アプリケーションレベルの設定 (永続化)
- `viewmodel/ChatViewModel.kt` — チャット UI の状態管理
- `viewmodel/ChatUiState.kt` — UI 状態データクラス
- `toolwindow/AgentToolWindowFactory.kt` — ToolWindow エントリポイント
- `ui/ChatPanel.kt` — メインチャット画面
- `ui/chat/` — メッセージ表示 (Assistant, User, Thinking, ToolUse)
- `ui/input/ChatInputArea.kt` — メッセージ入力エリア
- `ui/component/` — 共通コンポーネント (MarkdownText, CodeBlock)
- `ui/dialog/` — ダイアログ (Permission, AskUserQuestion)

## ビルド手順

```bash
# 1. ブリッジスクリプトのビルド (初回 or bridge-scripts 変更時)
cd bridge-scripts && npm install && npm run build

# 2. プラグインのビルド
./gradlew :plugin:build

# 3. 開発用 IDE で実行
./gradlew :plugin:runIde
```

## 重要な技術的制約

### 依存関係スコープ
- `bridge` モジュールの `kotlinx-serialization-json` と `kotlinx-coroutines-core` は **`compileOnly`** でなければならない
- IntelliJ Platform が `bundledPlugin("org.jetbrains.kotlin")` 経由でこれらを提供するため、`implementation` にすると ClassLoader 衝突が発生する

### ブリッジスクリプトのリソース配置
- `bridge-scripts/main.mjs` を esbuild でバンドルし `plugin/src/main/resources/bridge/main.mjs` に出力
- `BridgeScriptExtractor` が JAR 内からこのリソースを一時ディレクトリに展開して Node.js で実行
- ブリッジスクリプト変更時は `npm run build` を忘れずに実行すること

### IntelliJ プロセス環境の制約
- IntelliJ から起動される子プロセスはユーザーのログインシェル PATH を継承しない
- `NodeProcess` がユーザーのシェルから PATH を解決する仕組みを持つ
- `ClaudeCodeResolver` がログインシェル内で `which claude` を実行して Claude Code CLI を自動検出
- 自動検出に失敗した場合は `SettingsService.claudeCodePath` で手動設定が可能

### シリアライゼーション
- `BridgeCommand` sealed interface のサブクラスに `val type: String` プロパティを定義してはならない
- kotlinx.serialization のデフォルト class discriminator `"type"` と衝突する
- `@SerialName("start")` 等のアノテーションが自動で `{"type": "start", ...}` を生成する

### Compose TextFieldValue
- `TextFieldValue(text)` を recomposition ごとに再生成するとカーソル位置がリセットされる
- `mutableStateOf(TextFieldValue)` でローカル管理し、外部変更は `LaunchedEffect` で同期する

## 通信プロトコル

Kotlin ↔ Node.js 間は stdin/stdout の JSONL (1行1JSON) で通信。

### コマンド (Kotlin → Node.js)
- `start` — セッション開始 (`prompt`, `options`)
- `user_message` — ユーザーメッセージ送信
- `permission_response` — パーミッション応答
- `abort` — セッション中断

### イベント (Node.js → Kotlin)
- `ready` — ブリッジ準備完了
- `session_init` — セッション初期化完了
- `stream_message_start/stop` — ストリーミング開始/終了
- `stream_content_start/delta/stop` — コンテンツブロックのストリーミング
- `assistant_message` — アシスタントメッセージ完了
- `permission_request` — パーミッション要求
- `turn_result` — ターン結果
- `error` — エラー

## SDK オプション

`SessionOptions.claudeCodePath` → ブリッジスクリプトの `pathToClaudeCodeExecutable` に渡される。
Claude Agent SDK が内部で Claude Code CLI を呼び出す際のパスを指定する。

## 技術スタック

- Kotlin 2.1.20 / JVM 17
- IntelliJ Platform 2025.2.4 (sinceBuild: 252.25557)
- IntelliJ Platform Gradle Plugin 2.10.2
- Compose for IDE (Jewel) — `composeUI()`
- kotlinx.serialization 1.8.1 / kotlinx.coroutines 1.10.1
- Node.js + @anthropic-ai/claude-agent-sdk ^0.2.42
- esbuild ^0.25.0
