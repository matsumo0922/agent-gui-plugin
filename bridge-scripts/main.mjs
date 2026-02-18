import { query } from "@anthropic-ai/claude-agent-sdk";
import { createInterface } from "node:readline";

// --- stdout ---
function send(obj) {
  process.stdout.write(JSON.stringify(obj) + "\n");
}

// --- stdin (JSONL) ---
const rl = createInterface({ input: process.stdin });

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

// --- abort ---
let abortController = new AbortController();

rl.on("line", (line) => {
  try {
    const msg = JSON.parse(line);
    if (msg.type === "abort") {
      abortController.abort();
    }
  } catch { /* ignore */ }
});

// --- error handling ---
process.on("uncaughtException", (err) => {
  send({ type: "error", message: err.message, fatal: true });
  process.exit(1);
});

// --- streaming event conversion ---
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
          return;
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
      break;

    case "message_stop":
      send({ type: "stream_message_stop", sessionId });
      break;
  }
}

// --- main loop ---
send({ type: "ready" });

const startMsg = await waitForMessage("start");
const { prompt, options: userOptions = {} } = startMsg;

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

  return await waitForPermissionResponse(requestId);
};

async function* generateMessages() {
  yield {
    type: "user",
    message: { role: "user", content: prompt },
    parent_tool_use_id: null,
  };

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
  includePartialMessages: userOptions.includePartialMessages !== false,
};

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
        handleStreamEvent(message.session_id, message.event);
        break;

      case "assistant":
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
