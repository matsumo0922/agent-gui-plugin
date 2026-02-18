# Kotlin/JVM から Node.js ブリッジ経由で Claude Agent SDK を呼ぶ

Anthropic の Claude Agent SDK は TypeScript/Python 向けにのみ提供されている。
Kotlin/JVM から SDK の全機能（マルチターン会話・ツール許可コールバック・ストリーミング・中断）を利用するために、
Node.js 子プロセスを**双方向 JSONL**で制御するブリッジパターンを定義する。

---

## アーキテクチャ

```
┌──────────────────────────────────────────────┐
│            Kotlin/JVM アプリケーション          │
│                                              │
│  UI ←→ SessionManager ←→ BridgeClient       │
│                              │               │
└──────────────────────────────┼───────────────┘
                               │ ProcessBuilder
                 stdin (JSONL) ↓↑ stdout (JSONL)
┌──────────────────────────────┼───────────────┐
│            Node.js ブリッジプロセス             │
│                              │               │
│  StdinRouter ←→ SessionRunner ←→ SDK query() │
│                                    ↕         │
│                              Claude CLI      │
└──────────────────────────────────────────────┘
```

**設計原則**:
- stdin / stdout ともに **1行 = 1 JSON オブジェクト**（JSONL）
- すべてのメッセージは `type` フィールドで判別する（判別共用体）
- Node.js プロセスは**セッション単位で1プロセス**起動し、マルチターンを同一プロセス内で処理する
- stderr は Node.js のデバッグログ専用（Kotlin 側はロギングのみに利用）
- ストリーミングイベントは SDK の生形式を使わず、**Kotlin から扱いやすい形に変換**して送出する

---

## プロトコル定義

### Kotlin → Node.js（stdin）

| type | 用途 | 追加フィールド |
|---|---|---|
| `start` | セッション開始 | `prompt`, `options` |
| `user_message` | フォローアップメッセージ送信 | `text`, `images?`, `documents?` |
| `permission_response` | ツール許可コールバックへの応答 | `requestId`, `result` |
| `abort` | セッション中断 | _(なし)_ |

### Node.js → Kotlin（stdout）

#### 制御系メッセージ

| type | 用途 | 追加フィールド |
|---|---|---|
| `ready` | プロセス起動完了（start 受付可能） | _(なし)_ |
| `session_init` | セッション初期化完了 | `sessionId`, `model`, `tools`, `mcpServers`, `claudeCodeVersion` |
| `turn_result` | 1ターン完了 | `sessionId`, `subtype`, `totalCostUsd`, `usage`, `numTurns`, `isError` |
| `permission_request` | ツール使用許可の問い合わせ | `requestId`, `toolName`, `toolInput`, `toolUseId` |
| `tool_progress` | ツール実行中の進捗 | `sessionId`, `toolName`, `toolUseId`, `elapsedSeconds` |
| `status` | 状態変化通知（compacting 等） | `sessionId`, `status` |
| `error` | エラー | `message`, `fatal` |

#### 完全メッセージ（非ストリーミング）

| type | 用途 | 追加フィールド |
|---|---|---|
| `assistant_message` | Claudeの応答（完全なメッセージ） | `sessionId`, `parentToolUseId`, `content` |

#### ストリーミング出力

| type | 用途 | 追加フィールド |
|---|---|---|
| `stream_message_start` | assistant メッセージ生成開始 | `sessionId` |
| `stream_content_start` | content block 生成開始 | `sessionId`, `index`, `blockType`, `blockId?`, `toolName?` |
| `stream_content_delta` | テキスト差分（逐次送出） | `sessionId`, `index`, `deltaType`, `text` |
| `stream_content_stop` | content block 生成完了 | `sessionId`, `index` |
| `stream_message_stop` | assistant メッセージ生成完了 | `sessionId` |

---

## メッセージ詳細スキーマ

### Kotlin → Node.js

#### `start` — セッション開始

```json
{
  "type": "start",
  "prompt": "Refactor this function to use coroutines.",
  "options": {
    "cwd": "/path/to/project",
    "resume": "existing-session-id-or-null",
    "model": "sonnet",
    "systemPrompt": "You are a Kotlin expert.",
    "permissionMode": "default",
    "disallowedTools": [],
    "maxTurns": 10,
    "maxThinkingTokens": 16000,
    "maxBudgetUsd": 5.0,
    "settingSources": ["user", "project", "local"],
    "env": {}
  }
}
```

`options` の各フィールドは省略可能。省略時は SDK のデフォルトが適用される。
`resume` にセッション ID を渡すと既存セッションを継続する。

#### `user_message` — フォローアップ

```json
{
  "type": "user_message",
  "text": "Now add error handling.",
  "images": [],
  "documents": []
}
```

`start` 後、ターン完了（`turn_result` 受信）のたびに送信可能。
`images` / `documents` は Anthropic API の ContentBlock 形式（省略可）。

#### `permission_response` — ツール許可への応答

```json
{
  "type": "permission_response",
  "requestId": "req_abc123",
  "result": {
    "behavior": "allow",
    "updatedInput": { "command": "ls -la" }
  }
}
```

または拒否する場合:

```json
{
  "type": "permission_response",
  "requestId": "req_abc123",
  "result": {
    "behavior": "deny",
    "message": "User denied Bash execution"
  }
}
```

`requestId` は `permission_request` で受け取った値をそのまま返す。
60秒以内に応答しなければ Node.js 側で自動 deny する。

#### `abort` — 中断

```json
{
  "type": "abort"
}
```

進行中の query() を AbortController 経由で中断し、プロセスを終了する。

---

### Node.js → Kotlin

#### `ready` — 起動完了

```json
{
  "type": "ready"
}
```

Node.js プロセスが stdin の読み取りを開始した合図。
Kotlin 側はこれを受信してから `start` を送信すること。

#### `session_init` — セッション初期化

SDK の `SDKSystemMessage`（`type: "system"`, `subtype: "init"`）を受信したときに送出:

```json
{
  "type": "session_init",
  "sessionId": "abc-def-123",
  "model": "claude-sonnet-4-6",
  "claudeCodeVersion": "2.1.0",
  "tools": ["Bash", "Read", "Write", "Edit", "Glob", "Grep"],
  "mcpServers": [
    { "name": "context7", "status": "connected" }
  ],
  "permissionMode": "default"
}
```

#### `assistant_message` — Claude の応答（完全版）

SDK の `SDKAssistantMessage`（`type: "assistant"`）を受信したときに送出。
**ストリーミングが有効でも、完全なメッセージとして常に送出される**（ストリーミングイベントの後に到着する）:

```json
{
  "type": "assistant_message",
  "sessionId": "abc-def-123",
  "parentToolUseId": null,
  "content": [
    {
      "type": "thinking",
      "thinking": "Let me analyze this code..."
    },
    {
      "type": "text",
      "text": "Here is the refactored version:"
    },
    {
      "type": "tool_use",
      "id": "tool_01",
      "name": "Edit",
      "input": { "file_path": "/src/main.kt", "old_string": "...", "new_string": "..." }
    }
  ]
}
```

`content` は Anthropic API の ContentBlock 配列そのもの。
テキスト・thinking・ツール使用が混在する。

---

### ストリーミング出力メッセージ

SDK の `SDKPartialAssistantMessage`（`type: "stream_event"`）から、Kotlin が扱いやすい形に変換して送出する。

> ストリーミングが不要な場合は `options.includePartialMessages: false` を指定すれば
> 以下のメッセージはすべて抑制され、`assistant_message` のみが届く。

#### `stream_message_start` — メッセージ生成開始

Claude がメッセージの生成を開始したタイミングで1回送出:

```json
{
  "type": "stream_message_start",
  "sessionId": "abc-def-123"
}
```

#### `stream_content_start` — content block 生成開始

content block（テキスト・thinking・ツール使用）の生成が始まるたびに送出:

```json
{
  "type": "stream_content_start",
  "sessionId": "abc-def-123",
  "index": 0,
  "blockType": "thinking"
}
```

```json
{
  "type": "stream_content_start",
  "sessionId": "abc-def-123",
  "index": 1,
  "blockType": "text"
}
```

```json
{
  "type": "stream_content_start",
  "sessionId": "abc-def-123",
  "index": 2,
  "blockType": "tool_use",
  "blockId": "tool_01",
  "toolName": "Edit"
}
```

| フィールド | 説明 |
|---|---|
| `index` | メッセージ内での content block の位置（0始まり） |
| `blockType` | `"text"` / `"thinking"` / `"tool_use"` |
| `blockId` | tool_use の場合のみ。ツール使用のID |
| `toolName` | tool_use の場合のみ。ツール名 |

#### `stream_content_delta` — テキスト差分

content block の生成中に逐次送出される。**GUI でリアルタイム表示するための主要メッセージ**:

```json
{
  "type": "stream_content_delta",
  "sessionId": "abc-def-123",
  "index": 0,
  "deltaType": "thinking_delta",
  "text": "Let me analyze"
}
```

```json
{
  "type": "stream_content_delta",
  "sessionId": "abc-def-123",
  "index": 1,
  "deltaType": "text_delta",
  "text": "Here is the"
}
```

```json
{
  "type": "stream_content_delta",
  "sessionId": "abc-def-123",
  "index": 2,
  "deltaType": "input_json_delta",
  "text": "{\"file_path\":\"/src"
}
```

| deltaType | 説明 | 対応する blockType |
|---|---|---|
| `text_delta` | 通常テキストの差分 | `text` |
| `thinking_delta` | thinking（内部推論）の差分 | `thinking` |
| `input_json_delta` | ツール入力 JSON の差分 | `tool_use` |

#### `stream_content_stop` — content block 生成完了

content block の生成が完了したときに送出:

```json
{
  "type": "stream_content_stop",
  "sessionId": "abc-def-123",
  "index": 1
}
```

#### `stream_message_stop` — メッセージ生成完了

全 content block の生成が完了したときに送出:

```json
{
  "type": "stream_message_stop",
  "sessionId": "abc-def-123"
}
```

この直後に完全な `assistant_message` が届く。

---

### ストリーミングの時系列

1つの assistant メッセージが thinking + text + tool_use を含む場合の送出順序:

```
stream_message_start

  stream_content_start  (index=0, blockType="thinking")
  stream_content_delta  (index=0, deltaType="thinking_delta", text="Let me...")
  stream_content_delta  (index=0, deltaType="thinking_delta", text=" analyze...")
  stream_content_stop   (index=0)

  stream_content_start  (index=1, blockType="text")
  stream_content_delta  (index=1, deltaType="text_delta", text="Here")
  stream_content_delta  (index=1, deltaType="text_delta", text=" is the")
  stream_content_delta  (index=1, deltaType="text_delta", text=" refactored")
  stream_content_stop   (index=1)

  stream_content_start  (index=2, blockType="tool_use", blockId="tool_01", toolName="Edit")
  stream_content_delta  (index=2, deltaType="input_json_delta", text="{\"file")
  stream_content_delta  (index=2, deltaType="input_json_delta", text="_path\":...")
  stream_content_stop   (index=2)

stream_message_stop

assistant_message       (完全な content 配列を含む)
```

---

### その他の制御メッセージ

#### `permission_request` — ツール許可の問い合わせ

SDK の `canUseTool` コールバックが呼ばれたときに送出:

```json
{
  "type": "permission_request",
  "requestId": "req_abc123",
  "toolName": "Bash",
  "toolInput": { "command": "rm -rf /tmp/cache" },
  "toolUseId": "tool_01"
}
```

Kotlin 側は UI でユーザーに許可/拒否を求め、`permission_response` で応答する。
**応答があるまで SDK の query() は一時停止する**。

#### `turn_result` — ターン完了

SDK の `SDKResultMessage`（`type: "result"`）を受信したときに送出:

```json
{
  "type": "turn_result",
  "sessionId": "abc-def-123",
  "subtype": "success",
  "totalCostUsd": 0.042,
  "numTurns": 3,
  "isError": false,
  "usage": {
    "input_tokens": 1500,
    "output_tokens": 800,
    "cache_creation_input_tokens": 0,
    "cache_read_input_tokens": 200
  },
  "result": "Refactoring complete."
}
```

`subtype` は `"success"` / `"error_during_execution"` / `"error_max_turns"` / `"error_max_budget_usd"` のいずれか。

この受信後、次の `user_message` を送れる状態になる。
**プロセスは終了しない** — 次のメッセージを待ち続ける。

#### `error` — エラー

```json
{
  "type": "error",
  "message": "Claude Code CLI not found",
  "fatal": true
}
```

`fatal: true` のときはプロセスが終了する。`false` のときは続行可能。

---

## Node.js ブリッジの実装

### ディレクトリ構成

```
bridge/
├── package.json
└── main.mjs
```

### `package.json`

```json
{
  "name": "claude-agent-bridge",
  "type": "module",
  "dependencies": {
    "@anthropic-ai/claude-agent-sdk": "^0.2.42"
  }
}
```

### `main.mjs`

```javascript
import { query } from "@anthropic-ai/claude-agent-sdk";
import { createInterface } from "node:readline";

// --- stdout 送出 ---

function send(obj) {
  process.stdout.write(JSON.stringify(obj) + "\n");
}

// --- stdin 受信（JSONL 行単位） ---

const rl = createInterface({ input: process.stdin });

/** type を指定して次のメッセージを待つ */
function waitForMessage(...expectedTypes) {
  return new Promise((resolve) => {
    const handler = (line) => {
      try {
        const msg = JSON.parse(line);
        if (expectedTypes.includes(msg.type)) {
          rl.off("line", handler);
          resolve(msg);
        }
      } catch { /* ignore non-JSON */ }
    };
    rl.on("line", handler);
  });
}

/** permission_response を待つ（タイムアウト付き） */
function waitForPermissionResponse(requestId, timeoutMs = 60000) {
  return new Promise((resolve) => {
    const timer = setTimeout(() => {
      rl.off("line", handler);
      resolve({ behavior: "deny", message: "Permission request timed out" });
    }, timeoutMs);

    const handler = (line) => {
      try {
        const msg = JSON.parse(line);
        if (msg.type === "permission_response" && msg.requestId === requestId) {
          clearTimeout(timer);
          rl.off("line", handler);
          resolve(msg.result);
        }
      } catch { /* ignore */ }
    };
    rl.on("line", handler);
  });
}

// --- abort 監視 ---

let abortController = new AbortController();

rl.on("line", (line) => {
  try {
    const msg = JSON.parse(line);
    if (msg.type === "abort") {
      abortController.abort();
    }
  } catch { /* ignore */ }
});

// --- エラーハンドリング ---

process.on("uncaughtException", (err) => {
  send({ type: "error", message: err.message, fatal: true });
  process.exit(1);
});

// --- ストリーミングイベント変換 ---

/** SDK の stream_event を Kotlin 向けメッセージに変換して送出 */
function handleStreamEvent(sessionId, event) {
  switch (event.type) {
    case "message_start":
      send({ type: "stream_message_start", sessionId });
      break;

    case "content_block_start": {
      const block = event.content_block;
      const msg = {
        type: "stream_content_start",
        sessionId,
        index: event.index,
        blockType: block.type,
      };
      // tool_use の場合はツール情報を付与
      if (block.type === "tool_use") {
        msg.blockId = block.id;
        msg.toolName = block.name;
      }
      send(msg);
      break;
    }

    case "content_block_delta": {
      const delta = event.delta;
      let deltaType;
      let text;

      switch (delta.type) {
        case "text_delta":
          deltaType = "text_delta";
          text = delta.text;
          break;
        case "thinking_delta":
          deltaType = "thinking_delta";
          text = delta.thinking;
          break;
        case "input_json_delta":
          deltaType = "input_json_delta";
          text = delta.partial_json;
          break;
        default:
          return; // 未知の delta は無視
      }

      send({
        type: "stream_content_delta",
        sessionId,
        index: event.index,
        deltaType,
        text,
      });
      break;
    }

    case "content_block_stop":
      send({ type: "stream_content_stop", sessionId, index: event.index });
      break;

    case "message_delta":
      // message_delta は stop_reason や usage を含むが、
      // これらは turn_result で取得できるため、ここでは省略
      break;

    case "message_stop":
      send({ type: "stream_message_stop", sessionId });
      break;
  }
}

// --- メインループ ---

send({ type: "ready" });

const startMsg = await waitForMessage("start");
const { prompt, options: userOptions = {} } = startMsg;

// canUseTool コールバック: Node.js → Kotlin → Node.js の双方向通信
let requestCounter = 0;

const canUseTool = async (toolName, toolInput, sdkOptions) => {
  const requestId = `req_${++requestCounter}`;

  send({
    type: "permission_request",
    requestId,
    toolName,
    toolInput,
    toolUseId: sdkOptions.toolUseID,
  });

  // Kotlin 側の応答を待つ（最大60秒）
  return await waitForPermissionResponse(requestId);
};

// AsyncGenerator: マルチターン入力
async function* generateMessages() {
  // 最初のプロンプト
  yield {
    type: "user",
    message: { role: "user", content: prompt },
    parent_tool_use_id: null,
  };

  // 以降のターン: turn_result 後に user_message を待つ
  while (true) {
    const msg = await waitForMessage("user_message", "abort");
    if (msg.type === "abort") return;

    const content = (msg.images?.length || msg.documents?.length)
      ? [{ type: "text", text: msg.text }, ...(msg.images ?? []), ...(msg.documents ?? [])]
      : msg.text;

    yield {
      type: "user",
      message: { role: "user", content },
      parent_tool_use_id: null,
    };
  }
}

// SDK query() 呼び出し
const sdkOptions = {
  cwd: userOptions.cwd,
  resume: userOptions.resume,
  model: userOptions.model,
  systemPrompt: userOptions.systemPrompt,
  settingSources: userOptions.settingSources ?? ["user", "project", "local"],
  disallowedTools: userOptions.disallowedTools,
  maxTurns: userOptions.maxTurns,
  maxThinkingTokens: userOptions.maxThinkingTokens,
  maxBudgetUsd: userOptions.maxBudgetUsd,
  env: userOptions.env,
  abortController,
  canUseTool: userOptions.permissionMode === "default" ? canUseTool : undefined,
  permissionMode: userOptions.permissionMode ?? "default",
  includePartialMessages: userOptions.includePartialMessages !== false, // デフォルト有効
};

// pathToClaudeCodeExecutable は省略可（SDK がデフォルトで解決する）

let currentSessionId = null;

try {
  const messageIter = query({
    prompt: generateMessages(),
    options: sdkOptions,
  });

  for await (const message of messageIter) {
    switch (message.type) {
      case "system":
        if (message.subtype === "init") {
          currentSessionId = message.session_id;
          send({
            type: "session_init",
            sessionId: message.session_id,
            model: message.model,
            claudeCodeVersion: message.claude_code_version,
            tools: message.tools,
            mcpServers: message.mcp_servers,
            permissionMode: message.permissionMode,
          });
        } else if (message.subtype === "status") {
          send({
            type: "status",
            sessionId: message.session_id,
            status: message.status,
          });
        }
        break;

      case "stream_event":
        // ストリーミングイベントを Kotlin 向けに変換
        handleStreamEvent(message.session_id, message.event);
        break;

      case "assistant":
        // ストリーミング完了後の完全メッセージ
        send({
          type: "assistant_message",
          sessionId: message.session_id,
          parentToolUseId: message.parent_tool_use_id,
          content: message.message.content,
        });
        break;

      case "tool_progress":
        send({
          type: "tool_progress",
          sessionId: message.session_id,
          toolName: message.tool_name,
          toolUseId: message.tool_use_id,
          elapsedSeconds: message.elapsed_time_seconds,
        });
        break;

      case "result":
        send({
          type: "turn_result",
          sessionId: message.session_id,
          subtype: message.subtype,
          totalCostUsd: message.total_cost_usd,
          numTurns: message.num_turns,
          isError: message.is_error,
          usage: message.usage,
          result: message.subtype === "success" ? message.result : undefined,
          errors: message.subtype !== "success" ? message.errors : undefined,
        });
        break;
    }
  }
} catch (err) {
  send({ type: "error", message: err.message, fatal: true });
}

process.exit(0);
```

---

## Kotlin 側の実装

### BridgeClient — プロセス管理と JSONL 双方向通信

```kotlin
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.util.concurrent.CountDownLatch

class BridgeClient(
    private val nodeExecutable: String,
    private val bridgeScript: String,
) {
    private val gson = Gson()
    private var process: Process? = null
    private var stdinWriter: BufferedWriter? = null
    private var stdoutReader: BufferedReader? = null

    /** コールバックインターフェース */
    interface Listener {
        // --- 制御系 ---
        fun onReady() {}
        fun onSessionInit(sessionId: String, model: String, tools: List<String>) {}
        fun onTurnResult(sessionId: String, subtype: String, totalCostUsd: Double) {}
        fun onPermissionRequest(requestId: String, toolName: String, toolInput: JsonObject) {}
        fun onToolProgress(sessionId: String, toolName: String, elapsedSeconds: Double) {}
        fun onError(message: String, fatal: Boolean) {}

        // --- 完全メッセージ ---
        fun onAssistantMessage(sessionId: String, content: List<JsonObject>) {}

        // --- ストリーミング ---
        /** メッセージ生成開始 */
        fun onStreamMessageStart(sessionId: String) {}
        /** content block 生成開始 (blockType: "text" / "thinking" / "tool_use") */
        fun onStreamContentStart(sessionId: String, index: Int, blockType: String,
                                 blockId: String?, toolName: String?) {}
        /** テキスト差分 (deltaType: "text_delta" / "thinking_delta" / "input_json_delta") */
        fun onStreamContentDelta(sessionId: String, index: Int, deltaType: String, text: String) {}
        /** content block 生成完了 */
        fun onStreamContentStop(sessionId: String, index: Int) {}
        /** メッセージ生成完了（この直後に onAssistantMessage が届く） */
        fun onStreamMessageStop(sessionId: String) {}
    }

    /**
     * ブリッジプロセスを起動し、ready を待つ。
     * 戻り値の Sender でメッセージを送信する。
     */
    fun start(listener: Listener): Sender {
        val pb = ProcessBuilder(nodeExecutable, bridgeScript)
        // stderr はログ用に分離（stdout と混ぜない）
        pb.redirectErrorStream(false)

        val proc = pb.start()
        process = proc
        stdinWriter = proc.outputStream.bufferedWriter()
        stdoutReader = proc.inputStream.bufferedReader()

        val readyLatch = CountDownLatch(1)

        // stdout 読み取りスレッド
        Thread({
            stdoutReader?.forEachLine { line ->
                val msg = runCatching { gson.fromJson(line, JsonObject::class.java) }
                    .getOrNull() ?: return@forEachLine

                when (msg.get("type")?.asString) {
                    // 制御系
                    "ready" -> {
                        listener.onReady()
                        readyLatch.countDown()
                    }
                    "session_init" -> listener.onSessionInit(
                        msg.get("sessionId").asString,
                        msg.get("model").asString,
                        msg.getAsJsonArray("tools").map { it.asString },
                    )
                    "turn_result" -> listener.onTurnResult(
                        msg.get("sessionId").asString,
                        msg.get("subtype").asString,
                        msg.get("totalCostUsd").asDouble,
                    )
                    "permission_request" -> listener.onPermissionRequest(
                        msg.get("requestId").asString,
                        msg.get("toolName").asString,
                        msg.getAsJsonObject("toolInput"),
                    )
                    "tool_progress" -> listener.onToolProgress(
                        msg.get("sessionId").asString,
                        msg.get("toolName").asString,
                        msg.get("elapsedSeconds").asDouble,
                    )
                    "error" -> listener.onError(
                        msg.get("message").asString,
                        msg.get("fatal")?.asBoolean ?: true,
                    )

                    // 完全メッセージ
                    "assistant_message" -> listener.onAssistantMessage(
                        msg.get("sessionId").asString,
                        msg.getAsJsonArray("content").map { it.asJsonObject },
                    )

                    // ストリーミング
                    "stream_message_start" -> listener.onStreamMessageStart(
                        msg.get("sessionId").asString,
                    )
                    "stream_content_start" -> listener.onStreamContentStart(
                        msg.get("sessionId").asString,
                        msg.get("index").asInt,
                        msg.get("blockType").asString,
                        msg.get("blockId")?.asString,
                        msg.get("toolName")?.asString,
                    )
                    "stream_content_delta" -> listener.onStreamContentDelta(
                        msg.get("sessionId").asString,
                        msg.get("index").asInt,
                        msg.get("deltaType").asString,
                        msg.get("text").asString,
                    )
                    "stream_content_stop" -> listener.onStreamContentStop(
                        msg.get("sessionId").asString,
                        msg.get("index").asInt,
                    )
                    "stream_message_stop" -> listener.onStreamMessageStop(
                        msg.get("sessionId").asString,
                    )
                }
            }
        }, "bridge-stdout-reader").apply { isDaemon = true }.start()

        // stderr 読み取り（ログ用）
        Thread({
            proc.errorStream.bufferedReader().forEachLine { line ->
                System.err.println("[bridge-stderr] $line")
            }
        }, "bridge-stderr-reader").apply { isDaemon = true }.start()

        readyLatch.await()
        return Sender()
    }

    /** stdin にメッセージを送信するオブジェクト */
    inner class Sender {
        fun startSession(prompt: String, options: Map<String, Any?> = emptyMap()) {
            sendJson(mapOf("type" to "start", "prompt" to prompt, "options" to options))
        }

        fun sendUserMessage(text: String) {
            sendJson(mapOf("type" to "user_message", "text" to text))
        }

        fun respondPermission(requestId: String, allow: Boolean, updatedInput: Any? = null) {
            val result = if (allow) {
                mapOf("behavior" to "allow", "updatedInput" to updatedInput)
            } else {
                mapOf("behavior" to "deny", "message" to "Denied by user")
            }
            sendJson(mapOf("type" to "permission_response", "requestId" to requestId, "result" to result))
        }

        fun abort() {
            sendJson(mapOf("type" to "abort"))
        }
    }

    private fun sendJson(obj: Any) {
        val json = gson.toJson(obj)
        stdinWriter?.let {
            it.write(json)
            it.newLine()
            it.flush()
        }
    }

    fun destroy() {
        process?.let { proc ->
            if (proc.isAlive) {
                proc.descendants().forEach { it.destroyForcibly() }
                proc.destroyForcibly()
            }
        }
        process = null
    }
}
```

### 使用例 — ストリーミング表示 + ツール許可

```kotlin
val client = BridgeClient(
    nodeExecutable = "/usr/local/bin/node",
    bridgeScript = "/path/to/bridge/main.mjs",
)

val sender = client.start(object : BridgeClient.Listener {
    override fun onSessionInit(sessionId: String, model: String, tools: List<String>) {
        println("Session started: $sessionId (model: $model)")
    }

    // --- ストリーミング: テキストをリアルタイム表示 ---

    override fun onStreamContentStart(sessionId: String, index: Int, blockType: String,
                                       blockId: String?, toolName: String?) {
        when (blockType) {
            "thinking" -> print("[Thinking] ")
            "text" -> { /* テキスト開始、特に何もしない */ }
            "tool_use" -> print("\n[Tool: $toolName] ")
        }
    }

    override fun onStreamContentDelta(sessionId: String, index: Int, deltaType: String, text: String) {
        when (deltaType) {
            "text_delta" -> print(text)          // テキストを逐次表示
            "thinking_delta" -> print(text)      // thinking を逐次表示
            "input_json_delta" -> { /* ツール入力JSONは表示しない */ }
        }
    }

    override fun onStreamContentStop(sessionId: String, index: Int) {
        println()  // ブロック終了で改行
    }

    // --- 完全メッセージ: ストリーミング後の確定データ ---

    override fun onAssistantMessage(sessionId: String, content: List<JsonObject>) {
        // ストリーミングで表示済みなので、ここではデータ保存のみ
        // GUI であれば、ストリーミング表示を確定版で置き換える
    }

    // --- ツール許可 ---

    override fun onPermissionRequest(requestId: String, toolName: String, toolInput: JsonObject) {
        println("\n--- Permission Request ---")
        println("Tool: $toolName")
        println("Input: $toolInput")
        print("Allow? (y/n): ")

        val allow = readLine()?.trim()?.lowercase() == "y"
        sender.respondPermission(requestId, allow)
    }

    // --- ターン完了 ---

    override fun onTurnResult(sessionId: String, subtype: String, totalCostUsd: Double) {
        println("\n--- Turn complete ($subtype, cost: $$totalCostUsd) ---")
    }

    override fun onError(message: String, fatal: Boolean) {
        System.err.println("[Error] $message (fatal=$fatal)")
        if (fatal) client.destroy()
    }
})

// セッション開始
sender.startSession(
    prompt = "Analyze the project structure.",
    options = mapOf(
        "cwd" to "/path/to/project",
        "permissionMode" to "default",
    ),
)
```

---

## シーケンス図

### ストリーミング付きの会話フロー

```
Kotlin                        Node.js                     Claude CLI
  │                             │                            │
  │──ProcessBuilder.start()───→ │                            │
  │←─{type:"ready"}──────────  │                            │
  │                             │                            │
  │──{type:"start",prompt}───→  │                            │
  │                             │──query({prompt:gen()})───→ │
  │                             │                            │── API call ──→
  │                             │←─SDKSystemMessage(init)──  │
  │←─{type:"session_init"}───  │                            │
  │                             │                            │
  │                             │←─stream_event(msg_start)─  │
  │←─{stream_message_start}──  │                            │
  │                             │←─stream_event(blk_start)─  │
  │←─{stream_content_start}──  │  (index=0, "thinking")     │
  │                             │←─stream_event(blk_delta)─  │
  │←─{stream_content_delta}──  │  ("Let me...")             │
  │←─{stream_content_delta}──  │  (" analyze...")           │
  │←─{stream_content_stop}───  │                            │
  │                             │                            │
  │←─{stream_content_start}──  │  (index=1, "text")         │
  │←─{stream_content_delta}──  │  ("Here is")              │
  │←─{stream_content_delta}──  │  (" the refactored")      │
  │←─{stream_content_stop}───  │                            │
  │                             │                            │
  │←─{stream_message_stop}───  │                            │
  │                             │←─SDKAssistantMessage─────  │
  │←─{assistant_message}──────  │  (完全な content 配列)      │
  │                             │                            │
  │                             │←─SDKResultMessage────────  │
  │←─{type:"turn_result"}────  │                            │
  │                             │    (generator awaits...)   │
  │──{type:"user_message"}──→   │                            │
  │                             │──generator yields────────  │
  │  ... (同様のストリーミング)  │                            │
```

### ツール許可フロー

```
Kotlin                       Node.js                     Claude CLI
  │                            │                            │
  │                            │←─"Bash使いたい"─────────────│
  │                            │  (canUseTool callback)     │
  │←─{type:"permission_request"}│                           │
  │                            │    (callback awaits...)    │
  │  ┌───────────┐             │                            │
  │  │ UI で     │             │                            │
  │  │ 許可/拒否 │             │                            │
  │  │ ダイアログ│             │                            │
  │  └─────┬─────┘             │                            │
  │──{type:"permission_response"}→                          │
  │                            │──{behavior:"allow"}───────→│
  │                            │                            │──Bash実行
  │                            │←──結果─────────────────────│
```

---

## セッション管理戦略

### 1プロセス = 1セッション

```
セッションA → Node.js プロセス A (PID 1234)
セッションB → Node.js プロセス B (PID 1235)
```

各プロセスは独立しており、相互に影響しない。
Kotlin 側で `Map<SessionId, BridgeClient>` として管理する。

### セッション継続（resume）

既存セッションを継続するには、新しいプロセスを起動して `options.resume` に
セッション ID を渡す:

```kotlin
sender.startSession(
    prompt = "Continue working on the feature.",
    options = mapOf("resume" to "previous-session-id"),
)
```

SDK が `~/.claude/projects/` の JSONL を読み込み、会話コンテキストを復元する。

### プロセスの終了条件

| 条件 | 動作 |
|---|---|
| Kotlin から `abort` 受信 | AbortController.abort() → query() 中断 → process.exit(0) |
| Kotlin プロセス終了（stdin EOF） | readline が close → generator が return → query() 終了 |
| Claude CLI がクラッシュ | query() が例外 → `error`(fatal) 送出 → process.exit(1) |
| 正常にすべてのターン完了 | query() の generator を return → `turn_result` 後に待機継続 |

---

## 配布と依存関係

### Node.js の必要性

このパターンでは Node.js ランタイムがユーザーの環境に必要。
Claude Code CLI 自体が Node.js に依存しているため、Claude Code がインストールされている
環境には Node.js も存在する前提で問題ない。

### ブリッジスクリプトのバンドル

```
my-app/
├── app.jar
└── bridge/
    ├── node_modules/
    │   └── @anthropic-ai/claude-agent-sdk/
    ├── package.json
    └── main.mjs
```

JAR にバンドルして起動時に展開するか、インストーラで配置する。
`npm install` は事前に実行しておく（ユーザー環境での npm 依存を避ける）。

### Node.js 実行ファイルの検索

```kotlin
fun findNode(): String {
    // Claude Code と同じ Node.js を使うのが最も安全
    val claudePath = ProcessBuilder("which", "claude")
        .start().inputStream.bufferedReader().readText().trim()
    // claude は Node.js スクリプトなので、shebang から node パスを取得可能

    // フォールバック
    val candidates = listOf("node", "/usr/local/bin/node", "/opt/homebrew/bin/node")
    return candidates.first { path ->
        runCatching {
            ProcessBuilder(path, "--version").start().waitFor() == 0
        }.getOrDefault(false)
    }
}
```

---

## まとめ

| 項目 | 内容 |
|---|---|
| 通信方式 | stdin/stdout 双方向 JSONL |
| プロセスモデル | 1セッション = 1 Node.js プロセス |
| マルチターン | AsyncGenerator で 1 query() 内に複数ターン |
| ツール許可 | `permission_request` / `permission_response` の往復 |
| 中断 | `abort` メッセージ → AbortController |
| セッション継続 | `options.resume` にセッション ID |
| ストリーミング | `stream_content_delta` で逐次テキスト差分を受信 |
| 非ストリーミング | `assistant_message` で完全な content 配列を受信 |
| ストリーミング抑制 | `options.includePartialMessages: false` で差分なし |
