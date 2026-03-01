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
- `ui/theme/IdeaTheme.kt` — テーマ定義 (IdeaColorScheme, IdeaTypography)
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

### Compose
- 全ての Composable の引数には `Modifier` を用意し、`fillMaxWidth` などの　Composable 自体の大きさやマージンは外から指定すること。
- `Modifier` は呼び出し時には第一引数に記述する。引数の定義時には必ずデフォルト引数で `Modifier` を渡しデフォルト引数が指定されている引数の中で先頭に定義する。
- `TextFieldValue(text)` を recomposition ごとに再生成するとカーソル位置がリセットされる
- `mutableStateOf(TextFieldValue)` でローカル管理し、外部変更は `LaunchedEffect` で同期する

### テーマ (`IdeaTheme`)

プロジェクト全体で **`IdeaTheme`** を唯一のテーマ参照先として使用する。`JewelTheme` や旧 `ChatTheme` を UI コンポーネントから直接参照してはならない。

#### 基本ルール
- **色** → `IdeaTheme.colorScheme.*` を使う（例: `IdeaTheme.colorScheme.onSurface`）
- **テキストスタイル** → `IdeaTheme.typography.*` を使う（例: `IdeaTheme.typography.bodyMedium`）
- **ダークモード判定** → `IdeaTheme.isDark`
- **テーマ変更検知キー** → `IdeaTheme.instanceUuid`（`remember` の key に使用）

#### Provider
全ての Compose エントリポイント（`JewelComposePanel` 内）で `IdeaTheme { ... }` を wrap する。
- `TabManager.createContent()` — メインチャット画面
- `AgentGuiSettingsConfigurable.createComponent()` — 設定画面

#### IdeaColorScheme トークン一覧
| カテゴリ | トークン | 用途 |
|---|---|---|
| Surface | `background` | パネル/ウィンドウ背景 |
| | `surface` | カード、コードブロック背景 |
| | `surfaceContainer` | ヘッダー、セカンダリパネル背景 |
| On Surface | `onSurface` | 主テキスト/アイコン |
| | `onSurfaceVariant` | 副テキスト/アイコン |
| | `onSurfaceDisabled` | 無効テキスト/プレースホルダー |
| Outline | `outline` | デフォルト枠線/区切り線 |
| | `outlineVariant` | 控えめな枠線/区切り線 |
| Primary | `primary` | アクセントカラー（ボタン、リンク） |
| | `primaryContainer` | アクセント由来のコンテナ色 |
| | `onPrimaryContainer` | primaryContainer 上のコンテンツ色 |
| Semantic | `error` / `warning` / `success` | 状態表示 |

#### IdeaTypography トークン一覧
| トークン | Jewel ベース | 用途 |
|---|---|---|
| `titleLarge` | regular + SemiBold 16sp | セクションヘッダー |
| `titleMedium` | regular + SemiBold | カードタイトル |
| `titleSmall` | medium + SemiBold | サブセクションヘッダー |
| `bodyLarge` | regular (≈ 13sp) | 本文テキスト |
| `bodyMedium` | medium (≈ 12sp) | 標準 UI テキスト |
| `bodySmall` | small (≈ 11sp) | 補助テキスト |
| `labelLarge` | medium + Medium | ボタンラベル |
| `labelMedium` | small | フォームラベル |
| `labelSmall` | small 10sp | キャプション、バッジ |

#### IdeaTheme にトークンが無い特殊カラー
以下の色は `IdeaTheme` のロールベーストークンに適合しないため、使用箇所のファイル内で **private トップレベル Composable プロパティ** として定義し、`JewelTheme.colorPalette` を直接参照する。

- **Diff 色** (`CodeBlock.kt`): `diffAddedBackground`, `diffRemovedBackground`, `diffAddedLabel`, `diffRemovedLabel`
- **コンテキスト使用量インジケーター** (`ChatInputArea.kt`): `contextWarningColor`, `contextDangerColor`

新たに特殊カラーが必要な場合も同じパターンに従うこと。`IdeaTheme` にコンポーネント固有のトークンを追加しない。

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
