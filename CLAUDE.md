# Claude Code GUI IntelliJ Plugin

IntelliJ IDE 上で Claude Code (CLI) を GUI で操作するためのプラグイン。
`@anthropic-ai/claude-agent-sdk` を Node.js ブリッジ経由で呼び出し、Compose for IDE (Jewel) で UI を描画する。

## プロジェクト構成

```
agent-gui-plugin/
├── bridge/              # Kotlin Multiplatform モジュール: SDK通信の抽象化層
│   ├── commonMain/      #   共有モデル (BridgeCommand, BridgeEvent 等)
│   ├── jvmMain/         #   JVM固有 (BridgeClient, NodeProcess, Resolvers)
│   └── jsMain/          #   Kotlin/JS ブリッジスクリプト (旧 bridge-scripts/main.mjs の置き換え)
├── bridge-scripts/      # esbuild 設定 (Kotlin/JS 出力をバンドル)
├── plugin/              # IntelliJ プラグイン本体 (Compose UI + Services)
└── docs/design/         # 設計ドキュメント
```

### bridge モジュール (`bridge/`)
Kotlin Multiplatform。SDK との通信プロトコルを担当。

#### commonMain — 共有モデル
- `model/` — データモデル (`BridgeCommand`, `BridgeEvent`, `SessionOptions`, `ContentBlock` 等)

#### jvmMain — JVM 固有コード (IntelliJ Platform への依存なし)
- `client/BridgeClient.kt` — Node.js プロセスとの JSONL 通信クライアント
- `process/NodeProcess.kt` — Node.js 子プロセスの管理 (stdin/stdout)
- `process/NodeResolver.kt` — Node.js 実行ファイルの自動検出
- `process/ClaudeCodeResolver.kt` — Claude Code CLI の自動検出 (`which claude` をログインシェルで実行)
- `process/BridgeScriptExtractor.kt` — JAR 内の bridge スクリプトを一時ディレクトリに展開

#### jsMain — ブリッジスクリプト (Kotlin/JS)
`@anthropic-ai/claude-agent-sdk` の `query()` を呼び出す Node.js スクリプト。Kotlin/JS (IR backend) でコンパイルされ、esbuild でバンドルされる。

- `js/external/ClaudeAgentSdk.kt` — SDK の external 宣言
- `js/external/NodeApis.kt` — Node.js API の external 宣言 (process, readline, AbortController)
- `js/BridgeMain.kt` — エントリポイント (main関数)。ready → start → query → event dispatch の全フロー
- `js/MessageGenerator.kt` — AsyncIterable 生成 (SDK の prompt パラメータ用) + stdin 読み取り
- `js/StreamEventMapper.kt` — SDK メッセージ → BridgeEvent 変換

### bridge-scripts モジュール (`bridge-scripts/`)
Kotlin/JS 出力を esbuild でバンドルする設定のみ。

- `esbuild.config.mjs` — Kotlin/JS 出力 + SDK をバンドルして `plugin/src/main/resources/bridge/main.mjs` に出力
- ビルド: `./gradlew :bridge:jsProductionExecutableCompileSync` の後に `cd bridge-scripts && npm run build`
  (または `./gradlew :plugin:bundleBridgeScript` で自動実行)

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
# 1. bridge-scripts の npm 依存をインストール (初回のみ)
cd bridge-scripts && npm install

# 2. プラグインのビルド (Kotlin/JS コンパイル + esbuild バンドルも自動実行)
./gradlew :plugin:build

# 3. 開発用 IDE で実行
./gradlew :plugin:runIde
```

### 手動ビルド (デバッグ時)
```bash
# Kotlin/JS のみコンパイル
./gradlew :bridge:jsProductionExecutableCompileSync

# esbuild バンドルのみ
cd bridge-scripts && npm run build

# JVM ビルドのみ
./gradlew :bridge:jvmJar
```

## 重要な技術的制約

### 依存関係スコープ (KMP + IntelliJ Platform)
- `bridge` の `commonMain` では `kotlinx-serialization-json` と `kotlinx-coroutines-core` を `implementation` で宣言
- IntelliJ Platform が `bundledPlugin("org.jetbrains.kotlin")` 経由でこれらを提供するため、`plugin/build.gradle.kts` で `exclude` を使って ClassLoader 衝突を回避
- 旧方式の `compileOnly` は commonMain では使えないため、この exclude パターンを採用

### ブリッジスクリプトのリソース配置
- Kotlin/JS (IR) が `bridge/build/compileSync/js/main/productionExecutable/kotlin/` に `.js` ファイルを出力
- esbuild がこれを SDK (`@anthropic-ai/claude-agent-sdk`) と共にバンドルし `plugin/src/main/resources/bridge/main.mjs` に出力
- `BridgeScriptExtractor` が JAR 内からこのリソースを一時ディレクトリに展開して Node.js で実行
- `plugin/build.gradle.kts` の `bundleBridgeScript` タスクが `processResources` 前に自動実行される

### Kotlin/JS と AsyncIterable
- SDK の `query()` は `AsyncIterable<SDKUserMessage>` を `prompt` パラメータとして受け取る
- Kotlin/JS は `Symbol.asyncIterator` を直接サポートしないため、`js()` ブロックで最小限の JS ヘルパーを埋め込んで生成
- `MessageGenerator.kt` の `createAsyncIterableJs()` を参照

### IntelliJ プロセス環境の制約
- IntelliJ から起動される子プロセスはユーザーのログインシェル PATH を継承しない
- `NodeProcess` がユーザーのシェルから PATH を解決する仕組みを持つ
- `ClaudeCodeResolver` がログインシェル内で `which claude` を実行して Claude Code CLI を自動検出
- 自動検出に失敗した場合は `SettingsService.claudeCodePath` で手動設定が可能

### シリアライゼーション
- `BridgeCommand` sealed interface のサブクラスに `val type: String` プロパティを定義してはならない
- kotlinx.serialization のデフォルト class discriminator `"type"` と衝突する
- `@SerialName("start")` 等のアノテーションが自動で `{"type": "start", ...}` を生成する
- `BridgeEventSerializer` は serialize/deserialize の両方をサポート (JS側で送出、JVM側で受信)

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

- Kotlin 2.1.20 / JVM 17 / Kotlin Multiplatform (JVM + JS)
- Kotlin/JS IR backend (Node.js target)
- IntelliJ Platform 2025.2.4 (sinceBuild: 252.25557)
- IntelliJ Platform Gradle Plugin 2.10.2
- Compose for IDE (Jewel) — `composeUI()`
- kotlinx.serialization 1.8.1 / kotlinx.coroutines 1.10.1
- Node.js + @anthropic-ai/claude-agent-sdk ^0.2.42
- esbuild ^0.25.0
