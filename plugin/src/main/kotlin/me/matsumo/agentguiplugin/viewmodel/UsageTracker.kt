package me.matsumo.agentguiplugin.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.matsumo.claude.agent.types.ApiStreamEvent
import me.matsumo.claude.agent.types.StreamEvent
import me.matsumo.claude.agent.types.parsed

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
        val parsed = event.parsed()
        if (parsed !is ApiStreamEvent.MessageStart) return false
        val usage = parsed.usage ?: return false
        onMessageStart(usage.inputTokens, usage.cacheCreationInputTokens, usage.cacheReadInputTokens)
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
