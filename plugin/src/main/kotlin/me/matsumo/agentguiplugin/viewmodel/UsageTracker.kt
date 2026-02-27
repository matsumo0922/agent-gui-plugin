package me.matsumo.agentguiplugin.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import me.matsumo.claude.agent.types.StreamEvent

/**
 * トークン使用量・コスト追跡。
 * handleStreamEvent の message_start usage 解析と handleResultMessage のコスト/コンテキスト計算を集約。
 */
class UsageTracker(
    private val contextWindow: Long = DEFAULT_CONTEXT_WINDOW,
) {
    data class Usage(
        val contextUsage: Float = 0f,
        val totalInputTokens: Long = 0L,
        val totalCostUsd: Double = 0.0,
    )

    private val _usage = MutableStateFlow(Usage())
    val usage: StateFlow<Usage> = _usage.asStateFlow()

    /** 最後の message_start イベントから取得した per-turn input tokens */
    private var lastTurnInputTokens = 0L

    /**
     * message_start StreamEvent から usage を抽出して記録する。
     * サブエージェントの StreamEvent (parentToolUseId != null) は無視すること。
     */
    fun onMessageStart(inputTokens: Long, cacheCreation: Long, cacheRead: Long) {
        lastTurnInputTokens = inputTokens + cacheCreation + cacheRead
    }

    /**
     * StreamEvent を解析して message_start の usage を記録する。
     * サブエージェントのイベント (parentToolUseId != null) は無視する。
     * @return true if this was a message_start event that was processed
     */
    fun processStreamEvent(event: StreamEvent): Boolean {
        if (event.parentToolUseId != null) return false
        val type = event.event["type"]?.jsonPrimitive?.contentOrNull ?: return false
        if (type != "message_start") return false

        val usage = event.event["message"]?.jsonObject?.get("usage")?.jsonObject ?: return false
        val inputTokens = usage["input_tokens"]?.jsonPrimitive?.longOrNull ?: 0L
        val cacheCreation = usage["cache_creation_input_tokens"]?.jsonPrimitive?.longOrNull ?: 0L
        val cacheRead = usage["cache_read_input_tokens"]?.jsonPrimitive?.longOrNull ?: 0L
        onMessageStart(inputTokens, cacheCreation, cacheRead)
        return true
    }

    /**
     * ResultMessage 受信時に呼び出し、コスト・コンテキスト使用率を更新する。
     */
    fun onResult(totalCostUsd: Double?) {
        val contextUsage = (lastTurnInputTokens.toFloat() / contextWindow).coerceIn(0f, 1f)
        _usage.update { prev ->
            prev.copy(
                contextUsage = contextUsage,
                totalInputTokens = lastTurnInputTokens,
                totalCostUsd = totalCostUsd ?: prev.totalCostUsd,
            )
        }
    }

    fun reset() {
        lastTurnInputTokens = 0L
        _usage.value = Usage()
    }

    companion object {
        const val DEFAULT_CONTEXT_WINDOW = 200_000L
    }
}

private inline fun <T> MutableStateFlow<T>.update(transform: (T) -> T) {
    while (true) {
        val cur = value
        val new = transform(cur)
        if (compareAndSet(cur, new)) return
    }
}
