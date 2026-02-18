# Claude Code GUI のためのデータモデルリファレンス

Claude Code の GUI クライアントを構築する際に必要となるデータの持ち方を、言語非依存で網羅的にまとめたドキュメント。

> 本ドキュメントは [claude-code-viewer](https://github.com/d-kimuson/claude-code-viewer) の実装をリバースエンジニアリングして作成した。

---

## 目次

1. [全体アーキテクチャ概要](#1-全体アーキテクチャ概要)
2. [ファイルシステム上のデータ構造](#2-ファイルシステム上のデータ構造)
3. [プロジェクトモデル](#3-プロジェクトモデル)
4. [セッションモデル](#4-セッションモデル)
5. [会話エントリモデル（JSONL スキーマ）](#5-会話エントリモデルjsonl-スキーマ)
6. [メッセージコンテンツモデル](#6-メッセージコンテンツモデル)
7. [ツール使用・結果モデル](#7-ツール使用結果モデル)
8. [サブエージェントセッションモデル](#8-サブエージェントセッションモデル)
9. [セッションプロセスモデル（実行中のプロセス管理）](#9-セッションプロセスモデル実行中のプロセス管理)
10. [ターン（会話の1往復）モデル](#10-ターン会話の1往復モデル)
11. [仮想会話モデル（楽観的更新）](#11-仮想会話モデル楽観的更新)
12. [ツール許可モデル](#12-ツール許可モデル)
13. [イベントモデル（リアルタイム更新）](#13-イベントモデルリアルタイム更新)
14. [Claude Code 構成モデル（バージョン・機能フラグ）](#14-claude-code-構成モデルバージョン機能フラグ)
15. [MCP サーバーモデル](#15-mcp-サーバーモデル)
16. [スラッシュコマンド・スキルモデル](#16-スラッシュコマンドスキルモデル)
17. [スケジューラモデル](#17-スケジューラモデル)
18. [Git モデル](#18-git-モデル)
19. [ファイルコンテンツモデル](#19-ファイルコンテンツモデル)
20. [検索モデル](#20-検索モデル)
21. [キャッシュ戦略](#21-キャッシュ戦略)
22. [ユーザー設定モデル](#22-ユーザー設定モデル)
23. [トークンコスト計算モデル](#23-トークンコスト計算モデル)
24. [エンティティ間の関係図](#24-エンティティ間の関係図)

---

## 1. 全体アーキテクチャ概要

```
永続データ（ファイルシステム）
┌─────────────────────────────────────────────────┐
│  ~/.claude/projects/           ← Claude Code が書く（読み取り専用）  │
│  ~/.claude/commands/           ← スラッシュコマンド定義            │
│  ~/.claude/skills/             ← スキル定義                      │
│  ~/.claude-code-viewer/cache/  ← GUI アプリのキャッシュ            │
│  ~/.claude-code-viewer/scheduler/ ← スケジュール設定              │
└─────────────────────────────────────────────────┘
         ↓ 読み取り
┌─────────────────────────────────────────────────┐
│  サーバープロセス（インメモリ）                            │
│  ├ プロジェクトメタキャッシュ (Map)                       │
│  ├ セッションメタキャッシュ (Map)                         │
│  ├ セッションプロセス状態 (配列)                          │
│  ├ 仮想会話データベース (配列)                            │
│  ├ 許可リクエスト/レスポンス (Map)                       │
│  ├ 検索インデックス (MiniSearch)                        │
│  ├ イベントバス (pub/sub)                              │
│  └ ユーザー設定 (単一オブジェクト)                        │
└─────────────────────────────────────────────────┘
         ↓ HTTP API / SSE
┌─────────────────────────────────────────────────┐
│  フロントエンド                                        │
└─────────────────────────────────────────────────┘
```

**重要な原則**: Claude Code 自体は API を外部公開しない。全データは `~/.claude/projects/` 配下の JSONL ファイルとしてディスクに書き出される。GUI アプリはこのファイルを直接読む。

---

## 2. ファイルシステム上のデータ構造

```
~/.claude/projects/
  {エンコードされたプロジェクトパス}/          ← プロジェクト単位のディレクトリ
    {session-uuid}.jsonl                    ← 通常のセッションログ
    agent-{agent-id}.jsonl                  ← サブエージェント（旧形式）
    {session-uuid}/
      subagents/
        agent-{agent-id}.jsonl              ← サブエージェント（新形式、v2.0.28+）
```

### ディレクトリ名のエンコード規則

プロジェクトのディレクトリ名は、**実際のプロジェクトパスをスラッシュ→ハイフンに置換**したもの。

例: `/Users/john/myapp` → `-Users-john-myapp`

### ファイル名の規則

- `{uuid}.jsonl` — 通常セッション。UUID は Claude Code が生成
- `agent-{id}.jsonl` — サブエージェントセッション。`agent-` プレフィックスで判別

---

## 3. プロジェクトモデル

「プロジェクト」は `~/.claude/projects/` 内の1つのディレクトリに対応する。

### Project

| フィールド | 型 | 説明 |
|---|---|---|
| `id` | 文字列 | ディレクトリのフルパスを **base64url エンコード**したもの |
| `claudeProjectPath` | 文字列 | `~/.claude/projects/` 内のディレクトリの絶対パス |
| `lastModifiedAt` | 日時 | ディレクトリの mtime |
| `meta` | ProjectMeta | 以下参照 |

### ProjectMeta

| フィールド | 型 | 説明 |
|---|---|---|
| `projectName` | 文字列 or null | ユーザーの作業ディレクトリの basename（例: `"myapp"`） |
| `projectPath` | 文字列 or null | ユーザーの実際の作業ディレクトリパス（例: `/Users/john/myapp`） |
| `sessionCount` | 整数 | プロジェクトディレクトリ内の `.jsonl` ファイル数 |

### projectPath の導出方法

1. プロジェクトディレクトリ内の `.jsonl` ファイルを **mtime 昇順**（古い順）にソート
2. 各ファイルの行を順に読み、`type` が `summary`, `x-error`, `file-history-snapshot`, `queue-operation`, `custom-title`, `agent-name` **以外**の最初の行を見つける
3. その行の `cwd` フィールドが `projectPath`
4. `projectName` = `projectPath` の最後のパス要素（basename）

---

## 4. セッションモデル

「セッション」は1つの `.jsonl` ファイル（`agent-*.jsonl` を除く）に対応する。

### Session

| フィールド | 型 | 説明 |
|---|---|---|
| `id` | 文字列 | ファイル名から `.jsonl` を除いたもの（UUID） |
| `jsonlFilePath` | 文字列 | `.jsonl` ファイルの絶対パス |
| `lastModifiedAt` | 日時 | ファイルの mtime |
| `meta` | SessionMeta | 以下参照 |

### SessionMeta

| フィールド | 型 | 説明 |
|---|---|---|
| `messageCount` | 整数 | JSONL ファイルの総行数 |
| `firstUserMessage` | ParsedUserMessage or null | 最初の意味のあるユーザーメッセージ |
| `cost` | CostSummary | トークン使用量とコスト |
| `modelName` | 文字列 or null | 最後に使用されたモデル名（正規化済み） |

### ParsedUserMessage（判別共用体、`kind` で区別）

| kind | フィールド | 説明 |
|---|---|---|
| `"text"` | `content: 文字列` | 通常のテキスト入力 |
| `"command"` | `commandName: 文字列`, `commandArgs?: 文字列`, `commandMessage?: 文字列` | スラッシュコマンド入力（`/init` 等） |
| `"local-command"` | `stdout: 文字列` | ローカルコマンドの出力 |

#### 検出方法
生のテキスト内に XML 風タグが埋め込まれている:
- `<command-name>...</command-name>` → kind = "command"
- `<local-command-stdout>...</local-command-stdout>` → kind = "local-command"
- タグなし → kind = "text"

### CostSummary

```
CostSummary {
  totalUsd: 浮動小数点       // 合計USD
  breakdown: {
    inputTokensUsd: 浮動小数点
    outputTokensUsd: 浮動小数点
    cacheCreationUsd: 浮動小数点
    cacheReadUsd: 浮動小数点
  }
  tokenUsage: {
    inputTokens: 整数
    outputTokens: 整数
    cacheCreationTokens: 整数
    cacheReadTokens: 整数
  }
}
```

### SessionDetail（セッション詳細取得時）

Session の全フィールドに加えて:

| フィールド | 型 | 説明 |
|---|---|---|
| `conversations` | 会話エントリの配列 | JSONL の全行をパースした結果（エラー行含む） |

### セッション一覧のソート・ページネーション

- **ソート**: mtime 降順（最新が先頭）
- **ページネーション**: カーソルベース。1ページ20件。カーソルは前ページ最後のセッション ID
- **フィルタ**: `firstUserMessage` が null のセッションは非表示可（ユーザー設定）
- **重複排除**: 同一タイトルのセッションを1つに集約可（ユーザー設定）

---

## 5. 会話エントリモデル（JSONL スキーマ）

JSONL の各行は `type` フィールドで種別が決まる。パースに失敗した行は `x-error` になる。

### 全エントリタイプ一覧

| type | 説明 | BaseEntry を継承？ |
|---|---|---|
| `"user"` | ユーザーメッセージ | はい |
| `"assistant"` | アシスタント応答 | はい |
| `"system"` | システムイベント（5つのサブタイプあり） | はい |
| `"summary"` | コンテキスト圧縮サマリー | **いいえ（独自構造）** |
| `"file-history-snapshot"` | ファイルスナップショット | いいえ |
| `"queue-operation"` | メッセージキュー操作 | いいえ |
| `"progress"` | 進捗表示 | はい |
| `"custom-title"` | ユーザー設定タイトル | いいえ |
| `"agent-name"` | エージェント名 | いいえ |
| `"x-error"` | パース失敗行（アプリ独自） | いいえ |

### BaseEntry（共通フィールド）

ほとんどのエントリが持つ共通構造:

| フィールド | 型 | 必須？ | 説明 |
|---|---|---|---|
| `isSidechain` | 真偽値 | はい | サブエージェントチェーンに属するか |
| `userType` | `"external"` | はい | 常に `"external"` |
| `cwd` | 文字列 | はい | メッセージ時点の作業ディレクトリ |
| `sessionId` | 文字列 | はい | セッション UUID |
| `version` | 文字列 | はい | Claude Code のバージョン文字列 |
| `uuid` | UUID 文字列 | はい | このエントリの一意 ID |
| `timestamp` | ISO 8601 文字列 | はい | タイムスタンプ |
| `parentUuid` | UUID 文字列 or null | はい | 親エントリの UUID（スレッド/分岐用） |
| `isMeta` | 真偽値 | いいえ | メタデータエントリか |
| `toolUseResult` | 任意 | いいえ | ツール呼び出し結果（スキーマはツールごと） |
| `gitBranch` | 文字列 | いいえ | その時点の git ブランチ名 |
| `isCompactSummary` | 真偽値 | いいえ | コンパクトサマリーか |
| `agentId` | 文字列 | いいえ | サブエージェント ID |

### UserEntry

```
UserEntry extends BaseEntry {
  type: "user"
  message: {
    role: "user"
    content: 文字列 | コンテンツブロックの配列
  }
}
```

`content` は文字列（旧形式）または型付きコンテンツブロックの配列（新形式）。

### AssistantEntry

```
AssistantEntry extends BaseEntry {
  type: "assistant"
  message: {
    id: 文字列
    type: "message"
    role: "assistant"
    model: 文字列              // 生のモデルID（例: "claude-sonnet-4-5-20250929"）
    content: コンテンツブロックの配列
    stop_reason: 文字列 or null
    stop_sequence: 文字列 or null
    usage: TokenUsageBlock
  }
  requestId?: 文字列
  isApiErrorMessage?: 真偽値
}
```

### TokenUsageBlock

```
TokenUsageBlock {
  input_tokens: 整数
  output_tokens: 整数
  cache_creation_input_tokens?: 整数
  cache_read_input_tokens?: 整数
  cache_creation?: {
    ephemeral_5m_input_tokens: 整数
    ephemeral_1h_input_tokens: 整数
  }
  service_tier?: 文字列 or null
  server_tool_use?: {
    web_search_requests: 整数
  }
}
```

### SystemEntry（5つのサブタイプ）

`subtype` フィールドで区別:

**subtype なし（旧形式）**
```
{ content: 文字列, toolUseID: 文字列, level: "info" }
```

**subtype = "stop_hook_summary"**（v2.0.76+）
```
{
  toolUseID: 文字列, level: "info" | "suggestion",
  hookCount: 整数, hookInfos: [{ command: 文字列 }...],
  hookErrors: 配列, preventedContinuation: 真偽値,
  stopReason: 文字列, hasOutput: 真偽値
}
```

**subtype = "local_command"**
```
{ content: 文字列, level: "info" }
```

**subtype = "turn_duration"**（v2.1+）
```
{ durationMs: 整数, slug?: 文字列 }
```

**subtype = "compact_boundary"**
```
{
  content: 文字列, level: "info",
  logicalParentUuid?: 文字列,
  compactMetadata?: { trigger: 文字列, preTokens: 整数 }
}
```

**subtype = "api_error"**
```
{
  level: "error" | "warning" | "info",
  error: {
    status?: 整数, headers?: Map,
    requestID?: 文字列 or null,
    error?: { type: 文字列, message?: 文字列 }
  },
  retryInMs?: 整数, retryAttempt?: 整数, maxRetries?: 整数
}
```

### SummaryEntry（BaseEntry を継承しない）

```
SummaryEntry {
  type: "summary"
  summary: 文字列      // 圧縮されたテキスト要約
  leafUuid: UUID 文字列  // 要約対象の最後のメッセージの UUID
}
```

### FileHistorySnapshotEntry

```
FileHistorySnapshotEntry {
  type: "file-history-snapshot"
  messageId: 文字列
  snapshot: {
    messageId: 文字列
    trackedFileBackups: Map<文字列, 任意>  // ファイルパス → バックアップ内容
    timestamp: 文字列
  }
  isSnapshotUpdate: 真偽値
}
```

### QueueOperationEntry（`operation` で区別）

```
operation = "enqueue":  { content: 文字列 | コンテンツブロック配列 }
operation = "dequeue":  （追加フィールドなし）
operation = "remove":   （追加フィールドなし）
operation = "popAll":   { content?: 文字列 }

共通: { type: "queue-operation", sessionId: 文字列, timestamp: 文字列 }
```

### ProgressEntry

```
ProgressEntry extends BaseEntry {
  type: "progress"
  data: Map<文字列, 任意>
  toolUseID?: 文字列
  parentToolUseID?: 文字列
}
```

### CustomTitleEntry / AgentNameEntry

```
CustomTitleEntry { type: "custom-title", customTitle: 文字列, sessionId: 文字列 }
AgentNameEntry   { type: "agent-name",   agentName: 文字列,  sessionId: 文字列 }
```

### ErrorJsonl（パース失敗行、アプリ独自）

```
ErrorJsonl {
  type: "x-error"
  line: 文字列          // パース失敗した生の行テキスト
  lineNumber: 整数      // 1始まりの行番号
}
```

---

## 6. メッセージコンテンツモデル

`UserEntry.message.content` および `AssistantEntry.message.content` 内のブロック。

### テキストコンテンツ

```
{ type: "text", text: 文字列 }
```

### 思考コンテンツ（extended thinking）

```
{ type: "thinking", thinking: 文字列, signature?: 文字列 }
```

### 画像コンテンツ

```
{
  type: "image",
  source: {
    type: "base64",
    data: 文字列,           // base64 エンコードされた画像データ
    media_type: "image/png" | "image/jpeg" | "image/gif" | "image/webp"
  }
}
```

### ドキュメントコンテンツ

```
{
  type: "document",
  source:
    | { media_type: "text/plain",      type: "text",   data: 文字列 }
    | { media_type: "application/pdf", type: "base64", data: 文字列 }
}
```

---

## 7. ツール使用・結果モデル

### ToolUseContent（ツール呼び出し）

`AssistantEntry.message.content` 内に出現:

```
{
  type: "tool_use"
  id: 文字列                      // ツール呼び出しの一意 ID
  name: 文字列                    // ツール名（"Bash", "Read", "Write" 等）
  input: Map<文字列, 任意>         // ツール固有の入力パラメータ
}
```

### ToolResultContent（ツール結果）

次の `UserEntry.message.content` 内に出現（ToolUseContent と `tool_use_id` で紐づく）:

```
{
  type: "tool_result"
  tool_use_id: 文字列              // 対応する ToolUseContent の id
  content: 文字列 | (TextContent | ImageContent) の配列
  is_error?: 真偽値
}
```

### toolUseResult（BaseEntry のフィールド）

`BaseEntry.toolUseResult` にも型付きの結果が格納されることがある:

**Bash/コマンド実行結果:**
```
{ stdout: 文字列, stderr: 文字列, interrupted: 真偽値, isImage: 真偽値 }
```

**ファイル作成結果:**
```
{ type: "create", filePath: 文字列, content: 文字列, structuredPatch: StructuredPatch[] }
```

**ファイル更新結果:**
```
{
  filePath: 文字列, oldString: 文字列, newString: 文字列,
  originalFile: 文字列, userModified: 真偽値, replaceAll: 真偽値,
  structuredPatch: StructuredPatch[]
}
```

**検索結果:**
```
{ filenames: 文字列[], durationMs: 数値, numFiles: 整数, truncated: 真偽値 }
```

**ファイル読み取り結果:**
```
{
  type: "text",
  file: { filePath: 文字列, content: 文字列, numLines: 整数, startLine: 整数, totalLines: 整数 }
}
```

**Grep 結果:**
```
{ mode: "content", numFiles: 整数, filenames: 文字列[], content: 文字列, numLines: 整数 }
```

**StructuredPatch（差分情報）:**
```
{
  oldStart: 整数, oldLines: 整数, newStart: 整数, newLines: 整数,
  lines: 文字列[]    // unified diff 行（"+", "-", " " プレフィックス付き）
}
```

**Todo 結果:**
```
{
  oldTodos?: Todo[], newTodos?: Todo[]
}

Todo {
  content: 文字列
  status: "pending" | "in_progress" | "completed"
  priority: "low" | "medium" | "high"
  id: 文字列
}
```

---

## 8. サブエージェントセッションモデル

### サブエージェントとは

Claude Code が `Task` ツールでサブエージェントを起動すると、別の `.jsonl` ファイルに会話が記録される。通常のセッションと同じ JSONL 形式だが、エントリの `isSidechain` が `true`。

### ファイル配置（2つのパス）

| パス | 条件 | 親セッションとの紐づけ |
|---|---|---|
| `{project}/{sessionId}/subagents/agent-{agentId}.jsonl` | v2.0.28+ | ディレクトリ構造で暗黙的に紐づく |
| `{project}/agent-{agentId}.jsonl` | 旧形式 | 1行目の `sessionId` で親セッションを特定 |

### AgentSession のデータ

```
AgentSessionSummary {
  agentId: 文字列          // ファイル名から抽出（"agent-" と ".jsonl" の間）
  firstMessage: 文字列 or null  // サブエージェントセッション内の最初のユーザーテキスト
}

AgentSessionDetail {
  agentSessionId: 文字列 or null
  conversations: 会話エントリの配列
}
```

### コスト集計への影響

セッションのコスト計算時、**そのセッションに紐づく全サブエージェントファイルのトークン使用量も合算**される。

---

## 9. セッションプロセスモデル（実行中のプロセス管理）

Agent SDK を使ってメッセージを送信する際、長寿命のプロセスをメモリ上で管理する。

### 状態遷移図

```
pending ──→ not_initialized ──→ initialized ──→ file_created ──→ paused
                                                                   │
                                                    ←──── continue ┘
                                                    （再び pending から）

どの状態からも → completed（abort/エラー時）
```

### 各状態の意味

| 状態 | 意味 | sessionId | 備考 |
|---|---|---|---|
| `pending` | メッセージ定義のみ、SDK 未起動 | なし | 内部状態 |
| `not_initialized` | SDK 起動済み、init メッセージ待ち | なし | 内部状態 |
| `initialized` | init メッセージ受信、session_id 確定 | あり | 公開可能 |
| `file_created` | 最初の assistant メッセージ受信 | あり | 公開可能（JSONL ファイル存在確定） |
| `paused` | SDK の result メッセージ受信、待機中 | あり | 公開可能（次のメッセージ受付可能） |
| `completed` | 終了（正常/異常） | あり or なし | 終端状態 |

### 公開用プロセス情報

フロントエンドには `initialized`, `file_created`, `paused` の状態のみ以下の形式で公開:

```
PublicSessionProcess {
  id: 文字列              // プロセスの ULID
  projectId: 文字列
  sessionId: 文字列        // 確定済み
  status: "running" | "paused"   // initialized/file_created → "running", paused → "paused"
}
```

### プロセスの不変識別情報

```
SessionProcessDef {
  sessionProcessId: 文字列     // ULID
  projectId: 文字列
  cwd: 文字列                 // 作業ディレクトリ
  abortController: （中断制御）
  setNextMessage: （次のメッセージを供給するコールバック）
}
```

---

## 10. ターン（会話の1往復）モデル

各プロセスは**ターンの配列**を保持する。1ターン = 1回のユーザーメッセージ → Claude 応答のサイクル。

### ターン種別（`type` で区別）

| type | 意味 | sessionId | baseSessionId |
|---|---|---|---|
| `"new"` | 新規セッション開始 | なし | なし |
| `"continue"` | 同一プロセスで続行 | あり | あり（同じ値） |
| `"resume"` | 過去セッションの再開 | なし（新規に割り当てられる） | あり（元のセッション） |
| `"fork"` | セッションの分岐 | あり（元のセッション） | あり |

### ターンの状態

| 状態 | 意味 |
|---|---|
| `pending` | キュー済み、未開始 |
| `running` | SDK がストリーミング応答中 |
| `completed` | 正常完了。sessionId（最終値）を持つ |
| `failed` | エラー終了。error を持つ |

### ターンのオプション（CCOptions）

新規・再開・フォーク時に渡せるオプション:

| フィールド | 型 | 説明 |
|---|---|---|
| `disallowedTools` | 文字列の配列 | 無効化するツール名 |
| `settingSources` | `("user" \| "project" \| "local")[]` | 読み込む設定ソース |
| `systemPrompt` | 文字列 or プリセットオブジェクト | システムプロンプト |
| `model` | 文字列 | モデル指定 |
| `sandbox` | サンドボックス設定 | サンドボックスモード |
| `maxTurns` | 整数 | 最大ターン数 |
| `maxThinkingTokens` | 整数 | 最大思考トークン数 |
| `maxBudgetUsd` | 浮動小数点 | 最大予算（USD） |
| `env` | Map<文字列, 文字列?> | 環境変数オーバーライド |

---

## 11. 仮想会話モデル（楽観的更新）

### 目的

SDK 呼び出し直後〜JSONL ファイル作成までの空白期間に、UI にユーザーのメッセージを表示するためのインメモリストア。

### データ構造

```
VirtualConversationRecord {
  projectId: 文字列
  sessionId: 文字列
  conversations: 会話エントリの配列
}
```

### ライフサイクル

1. **プロセスが `initialized` 状態に遷移**: 合成 UserEntry を仮想 DB に挿入（UUID 形式: `vc__{sessionId}__{timestamp}`）
2. **プロセスが `file_created` 状態に遷移**: 仮想レコードを削除（実ファイルが存在するようになったため）
3. **セッション一覧取得時**: 仮想セッションをリストの先頭に挿入
4. **セッション詳細取得時**: 仮想会話をディスク上の会話とマージ

### 壊れたセッションの検出

`summary` エントリの `leafUuid` が、ファイル内でその summary より後に出現するメッセージを指している場合、セッションは「壊れている」と判定される。壊れたセッションでは仮想会話のマージをスキップする。

---

## 12. ツール許可モデル

Claude Code がツールを使う前に、ユーザーに許可を求める仕組み。

### PermissionRequest（サーバー → フロントエンド）

```
PermissionRequest {
  id: 文字列              // ULID
  turnId: 文字列           // どのターンが要求したか
  sessionId?: 文字列       // 未確定の場合あり
  toolName: 文字列         // 使おうとしているツール名
  toolInput: Map          // ツールの入力パラメータ
  timestamp: 整数（Unix ms）
}
```

### PermissionResponse（フロントエンド → サーバー）

```
PermissionResponse {
  permissionRequestId: 文字列  // PermissionRequest.id に対応
  decision: "allow" | "deny"
}
```

### 許可フロー

1. SDK の `canUseTool` コールバックが発火
2. PermissionRequest を作成、インメモリ Map に格納
3. SSE で `permissionRequested` イベントをフロントエンドに送信
4. サーバーが **1秒間隔でポーリング**（最大60秒）してレスポンスを待つ
5. フロントエンドが HTTP POST でレスポンスを返す
6. ポーリングがレスポンスを検出 → SDK に結果を返す

### 許可モード

| モード | 動作 |
|---|---|
| `"default"` | 対話的な許可/拒否フロー |
| `"bypassPermissions"` | 即座に全許可 |
| `"acceptEdits"` | 即座に全許可 |
| `"plan"` | 全拒否（"Tool execution is disabled in plan mode"） |

---

## 13. イベントモデル（リアルタイム更新）

### 内部イベント一覧

| イベント名 | ペイロード | 発火タイミング |
|---|---|---|
| `heartbeat` | （空） | 10秒ごと |
| `sessionListChanged` | `{ projectId }` | 新セッション出現、セッション削除 |
| `sessionChanged` | `{ projectId, sessionId }` | JSONL ファイル更新 |
| `agentSessionChanged` | `{ projectId, agentSessionId }` | サブエージェントファイル更新 |
| `sessionProcessChanged` | `{ processes, changed }` | プロセス状態遷移 |
| `permissionRequested` | `{ permissionRequest }` | ツール許可要求 |
| `virtualConversationUpdated` | `{ projectId, sessionId }` | 仮想会話の挿入/更新 |

### ファイル監視 → イベント変換

```
fs.watch（~/.claude/projects/ を再帰監視）
    ↓ ファイル名を正規表現でパース
    ↓ 100ms デバウンス（ファイルごと）
    ↓
EventBus.emit("sessionChanged" | "agentSessionChanged" | "sessionListChanged")
    ↓
キャッシュ無効化（SessionMetaService, ProjectMetaService）
    ↓
SSE → フロントエンド
```

### SSE 接続仕様

- エンドポイント: `/api/sse`
- タイムアウト: 5分（300,000ms）
- 接続時に `connect` イベントを送信
- 各イベントに `kind`（イベント種別）と `timestamp`（ISO 8601）が付与

---

## 14. Claude Code 構成モデル（バージョン・機能フラグ）

### ClaudeCodeVersion

```
ClaudeCodeVersion {
  major: 整数
  minor: 整数
  patch: 整数
}
```

`claude --version` の出力を正規表現でパース。パース失敗時は `null`。

### 実行ファイルの解決順序

1. 環境変数 `CCV_CC_EXECUTABLE_PATH` or CLI オプション `--executable`
2. `which -a claude` の結果から、npx-cache パス以外を優先

### 機能フラグ（バージョンゲート）

| フラグ名 | 最低バージョン | 説明 |
|---|---|---|
| `canUseTool` | 1.0.82 | 対話的ツール許可 |
| `uuidOnSDKMessage` | 1.0.86 | SDK メッセージに UUID |
| `agentSdk` | 1.0.125 | Agent SDK が使用可能 |
| `sidechainSeparation` | 2.0.28 | サブエージェントの新ディレクトリ構造 |
| `runSkillsDirectly` | 2.1.0 or 2.0.77 | スキルの直接実行 |

---

## 15. MCP サーバーモデル

`claude mcp list` コマンドの出力をパースして取得。永続化しない。

```
McpServer {
  name: 文字列              // サーバー名（例: "context7"）
  command: 文字列            // 起動コマンド
  status: "connected" | "failed" | "unknown"
}
```

検出方法: 出力行に `✓` or `connected` → connected、`✗` or `failed` → failed。

---

## 16. スラッシュコマンド・スキルモデル

### CommandInfo（コマンド/スキル共通）

```
CommandInfo {
  name: 文字列            // 例: "impl" or "frontend:impl"（サブディレクトリはコロン区切り）
  description: 文字列 or null   // YAML frontmatter の "description:" フィールド
  argumentHint: 文字列 or null  // YAML frontmatter の "argument-hint:" フィールド
}
```

### コマンドの読み込み元

| 種別 | パス | 形式 |
|---|---|---|
| グローバルコマンド | `~/.claude/commands/**/*.md` | Markdown + YAML frontmatter |
| プロジェクトコマンド | `{project}/.claude/commands/**/*.md` | 同上 |
| デフォルトコマンド | （ハードコード） | `init`, `compact`, `security-review`, `review` |

### スキルの読み込み元

| 種別 | パス | 形式 |
|---|---|---|
| グローバルスキル | `~/.claude/skills/*/SKILL.md` | ディレクトリ + SKILL.md |
| プロジェクトスキル | `{project}/.claude/skills/*/SKILL.md` | 同上 |

スキルは `runSkillsDirectly` フラグが true の場合のみ利用可能。

---

## 17. スケジューラモデル

定期実行やレート制限復帰のためのジョブスケジューラ。

### SchedulerJob（ディスク永続化）

保存先: `~/.claude-code-viewer/scheduler/schedules.json`

```
SchedulerJob {
  id: 文字列 (ULID)
  name: 文字列                // 表示名
  schedule: CronSchedule | ReservedSchedule
  message: MessageConfig
  enabled: 真偽値
  createdAt: ISO 日時文字列
  lastRunAt: ISO 日時文字列 or null
  lastRunStatus: "success" | "failed" | null
}
```

### CronSchedule

```
{ type: "cron", expression: 文字列, concurrencyPolicy: "skip" | "run" }
```

### ReservedSchedule（一回実行）

```
{ type: "reserved", reservedExecutionTime: ISO 日時文字列 }
```

実行後、設定ファイルから自動削除される。

### MessageConfig

```
MessageConfig {
  content: 文字列                           // 送信テキスト
  projectId: 文字列                         // 送信先プロジェクト
  baseSession: null                        // 新規セッション
            | { type: "fork", sessionId }   // 既存セッションを分岐
            | { type: "resume", sessionId } // 既存セッションを再開
}
```

### レート制限自動復帰

`autoScheduleContinueOnRateLimit` 有効時:
1. `sessionChanged` イベントを監視
2. 変更されたセッションの最終 JSONL 行を読む
3. レート制限エントリ（`type: "assistant"`, `isApiErrorMessage: true` で内容にリセット時刻が含まれる）を検出
4. リセット時刻に `reserved` ジョブを自動作成して resume する

---

## 18. Git モデル

全て `git` コマンドのサブプロセス実行結果から構築。永続化しない。

### GitBranch

```
{ name: 文字列, current: 真偽値, remote?: 文字列, commit: 文字列, ahead?: 整数, behind?: 整数 }
```

### GitCommit

```
{ sha: 文字列, message: 文字列, author: 文字列, date: ISO 文字列 }
```

### GitDiffFile

```
{ filePath: 文字列, status: "added"|"modified"|"deleted"|"renamed"|"copied", additions: 整数, deletions: 整数, oldPath?: 文字列 }
```

### GitDiffHunk / GitDiffLine

```
GitDiffHunk { oldStart, oldCount, newStart, newCount: 整数, header: 文字列, lines: GitDiffLine[] }
GitDiffLine { type: "context"|"added"|"deleted", content: 文字列, oldLineNumber?: 整数, newLineNumber?: 整数 }
```

### GitStatus

```
{ branch: 文字列, ahead: 整数, behind: 整数, staged: GitDiffFile[], unstaged: GitDiffFile[], untracked: 文字列[], conflicted: 文字列[] }
```

---

## 19. ファイルコンテンツモデル

プロジェクト内のファイルを閲覧する際のモデル。

### FileContentResult（判別共用体、`success` で区別）

**成功時:**
```
{ success: true, content: 文字列, filePath: 文字列, truncated: 真偽値, language: 文字列 }
```

- `content`: UTF-8 テキスト（1MB で切り詰め）
- `language`: 拡張子から自動検出（約60種類対応）
- セキュリティ: パストラバーサル(`..`)拒否、プロジェクトルート外拒否、バイナリファイル拒否（拡張子+先頭8KBのnullバイト検出）

**失敗時:**
```
{ success: false, error: "INVALID_PATH"|"NOT_FOUND"|"BINARY_FILE"|"READ_ERROR", message: 文字列, filePath: 文字列 }
```

### DirectoryEntry

```
{ name: 文字列, type: "file"|"directory", path: 文字列 }
```

ソート: ディレクトリ先、ファイル後。各グループ内はアルファベット順。`..` は常に先頭。

---

## 20. 検索モデル

MiniSearch による全文検索。インデックスはインメモリで TTL 60秒。

### SearchDocument（インデックス対象）

```
SearchDocument {
  id: 文字列                // "{sessionId}:{conversationIndex}" 形式
  projectId: 文字列
  projectName: 文字列
  sessionId: 文字列
  conversationIndex: 整数    // JSONL 内の0始まり位置
  type: "user" | "assistant"
  text: 文字列               // 抽出されたプレーンテキスト（user: 2000文字、assistant: 500文字上限）
  timestamp: ISO 文字列
}
```

### テキスト抽出ルール

| エントリタイプ | 抽出対象 |
|---|---|
| `user` | 全 string/text コンテンツブロックを結合 |
| `assistant` | `type: "text"` のコンテンツブロックのみ（thinking, tool_use は無視） |
| `custom-title` | `customTitle` 文字列 |
| その他 | インデックス対象外 |

### SearchResult（API レスポンス）

```
SearchResult {
  projectId: 文字列
  projectName: 文字列
  sessionId: 文字列
  conversationIndex: 整数
  type: "user" | "assistant"
  snippet: 文字列             // マッチ周辺150文字（前後に "..." 付与）
  timestamp: 文字列
  score: 浮動小数点           // user メッセージは 1.2 倍ブースト
}
```

---

## 21. キャッシュ戦略

### 2レベルキャッシュ

| レベル | 保存先 | 生存期間 | 無効化トリガー |
|---|---|---|---|
| インメモリ | プロセス内 Map | プロセス存続中 | ファイル監視イベント |
| ファイルキャッシュ | `~/.claude-code-viewer/cache/` | 永続 | サーバー起動時に再読み込み |

### キャッシュ対象

| 対象 | インメモリキー | ファイルキャッシュ内容 |
|---|---|---|
| ProjectMeta | projectId | `cwd` 文字列（プロジェクトパス） |
| SessionMeta | sessionId | `ParsedUserMessage or null`（最初のユーザーメッセージ） |
| 検索インデックス | （単一） | なし（TTL 60秒でリビルド） |

### キャッシュの陳腐化検出

特定の条件でキャッシュ値が「古い」と判定される:
- `local-command` で `stdout` が空
- テキスト値が `local-command-caveat` のプレースホルダー

---

## 22. ユーザー設定モデル

| フィールド | 型 | デフォルト | 説明 |
|---|---|---|---|
| `hideNoUserMessageSession` | 真偽値 | true | ユーザーメッセージのないセッションを非表示 |
| `unifySameTitleSession` | 真偽値 | false | 同一タイトルのセッションを集約 |
| `enterKeyBehavior` | 列挙型 | "shift-enter-send" | Enter キーの動作 |
| `permissionMode` | 列挙型 | "default" | ツール許可モード |
| `locale` | 文字列 | "en" | UI 言語 |
| `theme` | 列挙型 | "system" | テーマ |
| `searchHotkey` | 列挙型 | "command-k" | 検索ショートカット |
| `autoScheduleContinueOnRateLimit` | 真偽値 | false | レート制限後の自動再開 |

サーバー側にはディスク永続化しない（フロントエンドから毎リクエスト時に送信）。

---

## 23. トークンコスト計算モデル

### モデル別単価（USD/100万トークン）

| モデル名 | Input | Output | Cache Create | Cache Read |
|---|---|---|---|---|
| claude-opus-4.5 | 5.00 | 25.00 | 6.25 | 0.50 |
| claude-opus-4.1 | 15.00 | 75.00 | 18.75 | 1.50 |
| claude-sonnet-4.5 | 3.00 | 15.00 | 3.75 | 0.30 |
| claude-3.5-sonnet | 3.00 | 15.00 | 3.75 | 0.30 |
| claude-haiku-4.5 | 1.00 | 5.00 | 1.25 | 0.10 |
| claude-3-opus | 15.00 | 75.00 | 18.75 | 1.50 |
| claude-3-haiku | 0.25 | 1.25 | 0.30 | 0.03 |

### モデル名の正規化

API から返る生のモデル名（例: `claude-sonnet-4-5-20250929`）を部分文字列マッチで正規化:
- `sonnet-4-5` → `claude-sonnet-4.5`
- `opus-4-1` → `claude-opus-4.1`
- 等

### コスト集計方法

1. セッション内の全 `assistant` エントリを走査
2. 各エントリの `message.usage` からトークン数を抽出
3. `message.model` を正規化して単価テーブルを参照
4. サブエージェントファイルも含めて合算

---

## 24. エンティティ間の関係図

```
Project (1) ─────────── (N) Session
  │                          │
  │ id = base64url(dir)      │ id = filename (UUID)
  │ dir に JSONL が入る       │ 1つの .jsonl ファイル
  │                          │
  │                          ├── (N) AgentSession（サブエージェント）
  │                          │    agent-{id}.jsonl
  │                          │    親 sessionId で紐づけ
  │                          │
  │                          ├── (N) ConversationEntry（JSONL の各行）
  │                          │    type で種別判別
  │                          │    parentUuid でスレッド構造
  │                          │
  │                          ├── (0..1) SessionProcess（実行中のみ）
  │                          │    6状態のステートマシン
  │                          │    (N) Turn を保持
  │                          │
  │                          └── (0..N) VirtualConversation（一時的）
  │                               JSONL 未書き込み時の楽観的データ
  │
  └── (0..N) SchedulerJob
       プロジェクトに紐づくスケジュールジョブ

AssistantEntry.message.content
  └── (N) ToolUseContent { id, name, input }
                │
                │ tool_use_id で紐づけ
                ↓
次の UserEntry.message.content
  └── (1) ToolResultContent { tool_use_id, content, is_error }
```

---

## 付録: アプリケーション固有のパス一覧

| パス | 用途 | 管理者 |
|---|---|---|
| `~/.claude/projects/` | セッションログ（JSONL） | Claude Code CLI |
| `~/.claude/commands/` | グローバルスラッシュコマンド | ユーザー |
| `~/.claude/skills/` | グローバルスキル | ユーザー |
| `~/.claude/settings.json` | ユーザー設定 | Claude Code CLI |
| `{project}/.claude/commands/` | プロジェクトコマンド | ユーザー |
| `{project}/.claude/skills/` | プロジェクトスキル | ユーザー |
| `{project}/.claude/settings.json` | プロジェクト設定 | Claude Code CLI |
| `~/.claude-code-viewer/cache/` | GUI アプリのキャッシュ | GUI アプリ |
| `~/.claude-code-viewer/scheduler/` | スケジュール設定 | GUI アプリ |
