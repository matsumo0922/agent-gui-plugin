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

### Tier 1: 基本設定 (必須)

ほとんどのユーザーが触る可能性がある設定。

| 設定 | 型 | デフォルト | SDK マッピング | 説明 |
|---|---|---|---|---|
| **Claude CLI パス** | `String?` | `null` (自動検出) | `cliPath` | `claude` バイナリのパス。null 時は `which claude` → 既知パスの順で自動検出 |
| **モデル** | `enum` | `SONNET` | `model` | `SONNET` / `OPUS` / `HAIKU` |
| **パーミッションモード** | `enum` | `DEFAULT` | `permissionMode` | `DEFAULT` / `ACCEPT_EDITS` / `PLAN` / `BYPASS_PERMISSIONS` |
| **Effort レベル** | `enum?` | `null` (未指定) | `effort` | `LOW` / `MEDIUM` / `HIGH` / `MAX` — 思考の深さ制御 |
| **最大予算 (USD)** | `Double?` | `null` (無制限) | `maxBudgetUsd` | セッション当たりのコスト上限 |

### Tier 2: 詳細設定 (パワーユーザー向け)

ある程度 Claude Code を理解しているユーザーが使う設定。

| 設定 | 型 | デフォルト | SDK マッピング | 説明 |
|---|---|---|---|---|
| **最大ターン数** | `Int?` | `null` (無制限) | `maxTurns` | エージェントの最大ターン数 |
| **追加システムプロンプト** | `String?` | `null` | `systemPrompt` (append) | デフォルトプロンプトに追加するカスタム指示 |
| **許可ツール** | `List<String>` | `[]` | `allowedTools` | 明示的に許可するツール名リスト |
| **禁止ツール** | `List<String>` | `[]` | `disallowedTools` | 明示的に禁止するツール名リスト |
| **追加ディレクトリ** | `List<String>` | `[]` | `addDirs` | セッションに追加するディレクトリパス (モノレポ対応) |
| **設定ソース** | `List<SettingSource>?` | `null` (全ソース) | `settingSources` | `USER` / `PROJECT` / `LOCAL` のどれを読み込むか |
| **カスタム設定ファイル** | `String?` | `null` | `settings` | 設定 JSON ファイルのパス |

### Tier 3: 環境変数・プロバイダ設定

API キーやクラウドプロバイダの切り替えなど。

| 設定 | 型 | デフォルト | SDK マッピング | 説明 |
|---|---|---|---|---|
| **カスタム環境変数** | `Map<String, String>` | `{}` | `env` | CLI プロセスに渡す追加環境変数 |
| **API プロバイダ** | `enum?` | `null` (Anthropic直接) | `env` 経由 | `ANTHROPIC` / `BEDROCK` / `VERTEX` — 環境変数で切り替え |
| **カスタム API Base URL** | `String?` | `null` | `env["ANTHROPIC_BASE_URL"]` | カスタム API エンドポイント |

#### API プロバイダと環境変数の対応

| プロバイダ | 設定される環境変数 |
|---|---|
| Anthropic (デフォルト) | なし |
| AWS Bedrock | `CLAUDE_CODE_USE_BEDROCK=1` |
| Google Vertex AI | `CLAUDE_CODE_USE_VERTEX=1` |

### Tier 4: 実験的・デバッグ設定

開発者やトラブルシューティング向け。

| 設定 | 型 | デフォルト | SDK マッピング | 説明 |
|---|---|---|---|---|
| **デバッグモード** | `Boolean` | `false` | `extraArgs["--debug"]` | CLI のデバッグ出力を有効化 |
| **デバッグログファイル** | `String?` | `null` | `extraArgs["--debug-file"]` | デバッグログの出力先ファイル |
| **バージョンチェック無効化** | `Boolean` | `false` | `env["CLAUDE_AGENT_SDK_SKIP_VERSION_CHECK"]` | SDK の最小バージョンチェックをスキップ |
| **Context 1M ベータ** | `Boolean` | `false` | `betas` | 1M コンテキストウィンドウベータを有効化 |

---

## SDK がハードコードしている値 (参考)

以下は SDK が内部で自動設定するため、ユーザーが変更すべきではない項目:

| 設定 | 値 | 理由 |
|---|---|---|
| `--output-format` | `stream-json` | SDK プロトコルに必須 |
| `--input-format` | `stream-json` | SDK プロトコルに必須 |
| `--verbose` | 常に付与 | ストリーミングに必須 |
| `CLAUDE_CODE_ENTRYPOINT` | `sdk-kt` | SDK 識別用 |
| `CLAUDE_AGENT_SDK_VERSION` | `0.1.0` | バージョン報告用 |
| `includePartialMessages` | `true` | プラグインのストリーミング UI に必須 |
| `DEFAULT_MAX_BUFFER_SIZE` | 1MB | メッセージバッファ上限 |
| `MINIMUM_CLAUDE_CODE_VERSION` | `2.0.0` | 互換性保証 |

---

## 設定のスコープ

| スコープ | 対象設定 | 理由 |
|---|---|---|
| **Application-level** | CLI パス、モデル、パーミッションモード、Effort、最大予算、API プロバイダ、環境変数、デバッグ設定 | ユーザー環境に依存し、全プロジェクト共通 |
| **Project-level** (将来) | 追加システムプロンプト、許可/禁止ツール、追加ディレクトリ、設定ソース | プロジェクト固有のカスタマイズ |

初期実装では **Application-level のみ** とし、需要に応じて Project-level を追加する。

---

## 実装方針

### 1. SettingsService の拡張

```kotlin
data class State(
    // --- Tier 1: 基本設定 ---
    var claudeCodePath: String? = null,
    var model: String = "sonnet",
    var permissionMode: String = "default",
    var effort: String? = null,
    var maxBudgetUsd: Double? = null,

    // --- Tier 2: 詳細設定 ---
    var maxTurns: Int? = null,
    var appendSystemPrompt: String? = null,
    var allowedTools: List<String> = emptyList(),
    var disallowedTools: List<String> = emptyList(),
    var addDirs: List<String> = emptyList(),
    var settingSources: List<String>? = null,
    var customSettingsFile: String? = null,

    // --- Tier 3: 環境変数・プロバイダ ---
    var customEnvVars: Map<String, String> = emptyMap(),
    var apiProvider: String? = null,
    var customApiBaseUrl: String? = null,

    // --- Tier 4: デバッグ ---
    var debugMode: Boolean = false,
    var debugLogFile: String? = null,
    var skipVersionCheck: Boolean = false,
    var enableContext1MBeta: Boolean = false,
)
```

### 2. Settings UI の登録 (`plugin.xml`)

```xml
<extensions defaultExtensionNs="com.intellij">
    <applicationConfigurable
        parentId="tools"
        instance="me.matsumo.agentguiplugin.settings.AgentGuiSettingsConfigurable"
        id="me.matsumo.agentguiplugin.settings"
        displayName="Agent GUI" />
</extensions>
```

### 3. Configurable 実装

IntelliJ の `Configurable` インターフェースを実装。UI は Swing (JPanel) で構築する。
Compose for IDE は Settings ダイアログ内では使えないため、標準の Swing UI を使用する。

```
Settings / Preferences
└── Tools
    └── Agent GUI
        ├── General (基本設定)
        │   ├── Claude CLI Path [TextField + Browse]
        │   ├── Model [ComboBox]
        │   ├── Permission Mode [ComboBox]
        │   ├── Effort Level [ComboBox]
        │   └── Max Budget (USD) [TextField]
        ├── Advanced (詳細設定)
        │   ├── Max Turns [TextField]
        │   ├── Append System Prompt [TextArea]
        │   ├── Allowed Tools [EditableList]
        │   ├── Disallowed Tools [EditableList]
        │   ├── Additional Directories [EditableList + Browse]
        │   ├── Setting Sources [CheckBox group]
        │   └── Custom Settings File [TextField + Browse]
        ├── Provider (プロバイダ設定)
        │   ├── API Provider [ComboBox]
        │   ├── Custom API Base URL [TextField]
        │   └── Custom Environment Variables [KeyValue Table]
        └── Debug (デバッグ設定)
            ├── Debug Mode [CheckBox]
            ├── Debug Log File [TextField + Browse]
            ├── Skip Version Check [CheckBox]
            └── Context 1M Beta [CheckBox]
```

### 4. ChatViewModel への反映

`SettingsService` の値を `connectSession()` 内の `createSession {}` / `resumeSession {}` DSL に反映する。
設定変更は次回のセッション作成時から反映される（既存セッションには影響しない）。

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

1. **Phase 1**: Tier 1 (基本設定) + Configurable 登録 + Swing UI
2. **Phase 2**: Tier 2 (詳細設定) の追加
3. **Phase 3**: Tier 3 (プロバイダ設定) + Tier 4 (デバッグ設定)
4. **Phase 4**: Project-level 設定の分離 (必要に応じて)
