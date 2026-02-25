# Settings Page 設計ドキュメント

## 概要

IntelliJ の Settings/Preferences ダイアログから Agent GUI Plugin の設定をカスタマイズできる機能を提供する。

## 現状

### 既存の SettingsService

`plugin/.../service/SettingsService.kt` で `PersistentStateComponent` を使い、以下 3 項目のみ永続化:

| 設定 | 型 | デフォルト | 用途 |
|---|---|---|---|
| `claudeCodePath` | `String?` | `null` (自動検出) | Claude CLI のパス |
| `permissionMode` | `String` | `"default"` | ツール許可モード |
| `model` | `String` | `"sonnet"` | 使用モデル |

### 設定 UI

**未実装** — Settings ダイアログへの登録なし。`plugin.xml` に `applicationConfigurable` / `projectConfigurable` がない。

---

## 設定項目の分類

### Tier 0: CLI パス設定 (最優先)

プラグインが動作するための前提条件。CLI が見つからないと何も始まらない。

| 設定 | 型 | デフォルト | SDK マッピング | 説明 |
|---|---|---|---|---|
| **Claude CLI パス** | `String?` | `null` (自動検出) | `cliPath` | `claude` バイナリのパス。null/空欄 時は SDK が自動検出 |

### Tier 1: 基本設定

ほとんどのユーザーが触る可能性がある設定。

| 設定 | 型 | デフォルト | SDK マッピング | 説明 |
|---|---|---|---|---|
| **デフォルトモデル** | `enum` | `SONNET` | `model` | `SONNET` / `OPUS` / `HAIKU` — セッション中は Chat 入力欄から変更可 (Settings は既定値) |
| **デフォルトパーミッションモード** | `enum` | `DEFAULT` | `permissionMode` | `DEFAULT` / `ACCEPT_EDITS` / `PLAN` / `BYPASS_PERMISSIONS` — セッション中は Chat 入力欄から変更可 (Settings は既定値) |

### Tier 2: 詳細設定 (パワーユーザー向け)

| 設定 | 型 | デフォルト | SDK マッピング | 説明 |
|---|---|---|---|---|
| **Effort レベル** | `enum?` | `null` (未指定) | `effort` | `LOW` / `MEDIUM` / `HIGH` / `MAX` — 思考の深さ制御 |
| **最大予算 (USD)** | `String?` (UI 検証) | `null` (無制限) | `maxBudgetUsd` | セッション当たりのコスト上限。誤設定の影響が大きいため Tier 2 |
| **最大ターン数** | `Int?` | `null` (無制限) | `maxTurns` | エージェントの最大ターン数 |
| **追加システムプロンプト** | `String?` | `null` | `systemPromptPreset.append` | デフォルトプロンプトに**追記**するカスタム指示。※ `systemPrompt` (置換) ではなく `SystemPromptPreset(append=...)` を使うこと |
| **許可ツール** | `List<String>` | `[]` | `allowedTools` | 明示的に許可するツール名リスト |
| **禁止ツール** | `List<String>` | `[]` | `disallowedTools` | 明示的に禁止するツール名リスト |
| **追加ディレクトリ** | `List<String>` | `[]` | `addDirs` | セッションに追加するディレクトリパス (モノレポ対応) |
| **設定ソース** | `List<SettingSource>?` | `null` (全ソース) | `settingSources` | `USER` / `PROJECT` / `LOCAL` のどれを読み込むか |
| **設定ファイルパス** | `String?` | `null` | `settings` | 設定 JSON ファイルのパス |

### Tier 3: 環境変数・プロバイダ設定

| 設定 | 型 | デフォルト | SDK マッピング | 説明 |
|---|---|---|---|---|
| **API プロバイダ** | `enum?` | `null` (Anthropic 直接) | `env` 経由 | `ANTHROPIC` / `BEDROCK` / `VERTEX` |
| **カスタム API Base URL** | `String?` | `null` | `env["ANTHROPIC_BASE_URL"]` | カスタム API エンドポイント |
| **カスタム環境変数** | `List<EnvVarEntry>` | `[]` | `env` | CLI プロセスに渡す非秘密環境変数。秘密値 (API キー等) は IntelliJ Password Safe に分離 |

### Tier 4: 実験的・デバッグ設定

| 設定 | 型 | デフォルト | SDK マッピング | 説明 |
|---|---|---|---|---|
| **デバッグモード** | `Boolean` | `false` | `extraArgs["debug"]` | CLI のデバッグ出力を有効化。**注:** `extraArgs` のキーは `--` なしで指定 (SDK が自動付与) |
| **Debug to stderr** | `Boolean` | `false` | `extraArgs["debug-to-stderr"]` | stderr へのデバッグ出力。stderr コールバック配線も必要 |
| **デバッグログファイル** | `String?` | `null` | `extraArgs["debug-file"]` | デバッグログの出力先ファイル |
| **Context 1M ベータ** | `Boolean` | `false` | `betas` | 1M コンテキストウィンドウベータを有効化 |

> **注:** `skipVersionCheck` は現 SDK 実装では `options.env` 経由での per-session 設定が不可 (SDK が `System.getenv()` で親プロセス環境のみを参照)。SDK 側の修正後に UI 公開する。

### 対象外 (複雑性が高いため)

以下は Settings UI には含めない:

- `sandbox` — Sandbox 設定 (構造が複雑)
- `mcpServers` — MCP サーバー設定 (JSON 構造)
- `plugins` — プラグイン設定
- `outputFormat` — 構造化出力スキーマ
- `hooks` — フック設定 (コールバック)

---

## SDK がハードコードしている値 (参考)

以下は SDK が内部で自動設定するため、ユーザーが変更すべきではない項目:

| 設定 | 値 | 設定元 |
|---|---|---|
| `--output-format` | `stream-json` | SDK (プロトコルに必須) |
| `--input-format` | `stream-json` | SDK (プロトコルに必須) |
| `--verbose` | 常に付与 | SDK (ストリーミングに必須) |
| `CLAUDE_CODE_ENTRYPOINT` | `sdk-kt` | SDK (識別用) |
| `CLAUDE_AGENT_SDK_VERSION` | `0.1.0` | SDK (バージョン報告用) |
| `DEFAULT_MAX_BUFFER_SIZE` | 1MB | SDK (メッセージバッファ上限) |
| `MINIMUM_CLAUDE_CODE_VERSION` | `2.0.0` | SDK (互換性保証) |
| `includePartialMessages` | `true` | **プラグイン** (ストリーミング UI に必須) |

---

## 設定のスコープ

初期実装では **Application-level のみ**。

| スコープ | 対象 | 理由 |
|---|---|---|
| **Application-level** | Tier 0〜4 全て | ユーザー環境に依存し、全プロジェクト共通 |
| **Project-level** (将来) | 追加システムプロンプト、許可/禁止ツール、追加ディレクトリ、設定ソース | プロジェクト固有のカスタマイズ |

---

## 実装方針

### UI 技術: Compose for IDE (Jewel)

Settings ダイアログでも **Compose for IDE** を使用する。

- `Configurable.createComponent()` から `JewelComposePanel { ... }` を返すことで Compose UI を Settings に埋め込める
- `JewelComposePanel` は `JComponent` を返すため、IntelliJ の `Configurable` API と互換性がある
- `SwingBridgeTheme` が自動適用され、IDE テーマに追従する
- 既存の `TabManager.kt` と同じパターン (`enableNewSwingCompositing()` + `JewelComposePanel`)
- **注:** `BoundConfigurable` (`panel {}` DSL) は使えない — `Configurable` インターフェースを直接実装する

### 状態管理: Configurable と Compose の橋渡し

`Configurable` の `isModified()` / `apply()` / `reset()` は命令的 API のため、Compose の Reactive State と橋渡しが必要:

- `Configurable` クラスのフィールドに `mutableStateOf(...)` を保持
- Compose UI はその State を参照・更新
- `isModified()` / `apply()` / `reset()` はフィールドを直接読み書き

---

## Tier 0 実装計画 (Claude CLI パス)

### 目的

Settings ダイアログから Claude CLI のパスを設定できるようにする。これにより:
- CLI が PATH に入っていない環境でもプラグインを使用可能に
- 複数バージョンの Claude CLI を切り替え可能に

### 変更対象ファイル

1. **`plugin/src/main/resources/META-INF/plugin.xml`** — `applicationConfigurable` 登録
2. **`plugin/src/main/kotlin/me/matsumo/agentguiplugin/settings/AgentGuiSettingsConfigurable.kt`** — 新規作成
3. **`plugin/src/main/kotlin/me/matsumo/agentguiplugin/settings/AgentGuiSettingsPanel.kt`** — 新規作成 (Compose UI)

`SettingsService` は既に `claudeCodePath` を持っているため変更不要。

### 1. plugin.xml への登録

```xml
<applicationConfigurable
    parentId="tools"
    instance="me.matsumo.agentguiplugin.settings.AgentGuiSettingsConfigurable"
    id="me.matsumo.agentguiplugin.settings"
    displayName="Claude Code GUI"/>
```

### 2. AgentGuiSettingsConfigurable

```kotlin
package me.matsumo.agentguiplugin.settings

class AgentGuiSettingsConfigurable : Configurable {
    private val settings = service<SettingsService>()

    // Compose UI の状態 — フィールドとして保持
    private val cliPathState = mutableStateOf(settings.claudeCodePath.orEmpty())
    private var originalCliPath = settings.claudeCodePath.orEmpty()

    override fun getDisplayName(): String = "Claude Code GUI"

    override fun createComponent(): JComponent {
        enableNewSwingCompositing()
        return JewelComposePanel {
            AgentGuiSettingsPanel(
                cliPath = cliPathState.value,
                onCliPathChange = { cliPathState.value = it },
            )
        }
    }

    override fun isModified(): Boolean =
        cliPathState.value != originalCliPath

    override fun apply() {
        settings.claudeCodePath = cliPathState.value.ifBlank { null }
        originalCliPath = cliPathState.value
    }

    override fun reset() {
        val current = settings.claudeCodePath.orEmpty()
        cliPathState.value = current
        originalCliPath = current
    }
}
```

### 3. AgentGuiSettingsPanel (Compose UI)

```kotlin
package me.matsumo.agentguiplugin.settings

@Composable
fun AgentGuiSettingsPanel(
    cliPath: String,
    onCliPathChange: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // セクションタイトル
        Text("General", style = ...)

        // Claude CLI Path
        Row {
            TextField(
                value = cliPath,
                onValueChange = onCliPathChange,
                placeholder = { Text("Auto detect (leave empty)") },
                modifier = Modifier.weight(1f),
            )
            // Browse ボタン (ファイル選択ダイアログ)
            // Auto Detect ボタン (SDK の findCli 相当の検出)
        }

        // ヘルプテキスト: 空欄時の自動検出パス一覧
    }
}
```

### UI レイアウト

```
Settings / Preferences
└── Tools
    └── Claude Code GUI
        └── General
            ├── "Claude CLI Path" ラベル
            ├── [TextField (placeholder: "Auto detect")] [Browse] [Auto Detect]
            └── ヘルプテキスト: 空欄の場合、以下の順で自動検出:
                  which claude → ~/.npm-global/bin/claude → /usr/local/bin/claude → ...
```

### 設定反映タイミング

- Settings で変更 → `Apply` → `SettingsService.claudeCodePath` が更新
- 既存セッションには影響しない (CLI パスはセッション開始時に参照)
- 次回タブ作成時に `TabManager.createViewModel()` 経由で `SettingsService.claudeCodePath` が参照される

---

## Claude CLI の自動検出パス (参考)

SDK (`SubprocessTransport.kt`) が `cliPath` 未指定時に試行する順序:

1. `which claude` / `where claude` (PATH 検索)
2. `$HOME/.npm-global/bin/claude`
3. `/usr/local/bin/claude`
4. `$HOME/.local/bin/claude`
5. `$HOME/node_modules/.bin/claude`
6. `$HOME/.yarn/bin/claude`
7. `$HOME/.claude/local/claude`

---

## Claude Code が読み込む設定ファイル (参考)

| ファイル | 場所 | 説明 |
|---|---|---|
| `settings.json` | `~/.claude/` | ユーザーグローバル設定 |
| `settings.json` | `.claude/` (プロジェクトルート) | 共有プロジェクト設定 (Git 管理) |
| `settings.local.json` | `.claude/` (プロジェクトルート) | ローカルプロジェクト設定 (gitignore) |
| `.credentials.json` | `~/.claude/` | 認証情報 |
| `CLAUDE.md` | プロジェクトルート / `.claude/` | プロジェクト指示 |

---

## 実装優先順

1. **Phase 1 (今回)**: Tier 0 — `Configurable` 登録 + Compose Settings UI + CLI パス設定のみ
2. **Phase 2**: Tier 1 — デフォルトモデル / パーミッションモード
3. **Phase 3**: Tier 2 — Effort / 予算 / ターン数 / プロンプト / ツール / ディレクトリ / 設定ソース
4. **Phase 4**: Tier 3 — プロバイダ / 環境変数 + Password Safe 分離
5. **Phase 5**: Tier 4 — デバッグ設定 (stderr 配線含む)
6. **Phase 6**: Project-level 設定の分離 (App defaults + Project overrides の merge ルール)

---

## Codex レビュー指摘事項 (反映済み)

| # | 指摘 | 対応 |
|---|---|---|
| 1 | `skipVersionCheck` は SDK が `System.getenv()` で判定するため `options.env` 経由では効かない | Tier 4 の注記に追記。SDK 修正後に UI 公開 |
| 2 | `extraArgs` のキーに `--` を付けると二重付与になる | Tier 4 の SDK マッピングを `--` なしに修正 |
| 3 | `appendSystemPrompt` は `systemPrompt` ではなく `SystemPromptPreset(append=...)` を使う | Tier 2 の SDK マッピングを修正 |
| 4 | `customEnvVars` に秘密値が平文保存されるリスク | Tier 3 で Password Safe 分離を明記 |
| 5 | `ChatViewModel` のコンストラクタ注入で設定が stale になる | 設定反映タイミングの説明を追加。stale 問題は上位 Tier 実装時に対応 |
| 6 | `Effort` / `maxBudgetUsd` は Tier 1 には重すぎる | Tier 2 へ移動 |
| 7 | `includePartialMessages` は SDK ハードコードではなくプラグイン側設定 | SDK ハードコード表で「設定元」列を追加し明確化 |
| 8 | Model/PermissionMode は「既定値」であることを明示すべき | Tier 1 の説明に「セッション中は Chat 入力欄から変更可 (Settings は既定値)」を追加 |
| 9 | IntelliJ UI DSL (`panel {}`) の方が保守性が高い | Compose for IDE を採用 (プロジェクトの技術スタック統一) |
| 10 | CLI Path に Auto Detect / Test ボタン追加推奨 | Tier 0 の UI レイアウトに反映 |
