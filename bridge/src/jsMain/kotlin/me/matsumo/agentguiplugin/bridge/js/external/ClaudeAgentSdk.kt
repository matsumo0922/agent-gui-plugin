package me.matsumo.agentguiplugin.bridge.js.external

@JsModule("@anthropic-ai/claude-agent-sdk")
@JsNonModule
external object ClaudeAgentSdk {
    fun query(params: dynamic): dynamic
}
