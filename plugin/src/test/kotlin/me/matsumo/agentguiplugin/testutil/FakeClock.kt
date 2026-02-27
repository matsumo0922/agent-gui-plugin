package me.matsumo.agentguiplugin.testutil

import me.matsumo.agentguiplugin.viewmodel.Clock

/**
 * テスト用: 明示的に時刻を制御。
 * SubAgentCoordinator 等に注入して使用。
 */
class FakeClock(var now: Long = 0L) : Clock {
    override fun currentTimeMillis() = now
    fun advance(millis: Long) { now += millis }
}
