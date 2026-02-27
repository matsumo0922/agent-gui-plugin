package me.matsumo.agentguiplugin.testutil

/**
 * 時刻取得の抽象化。テストで時刻を決定的に制御するために使用。
 *
 * NOTE: Phase B（リファクタリング後）で SubAgentCoordinator に注入される。
 */
fun interface Clock {
    fun currentTimeMillis(): Long

    companion object {
        val System: Clock = Clock { java.lang.System.currentTimeMillis() }
    }
}

/**
 * テスト用: 明示的に時刻を制御。
 */
class FakeClock(var now: Long = 0L) : Clock {
    override fun currentTimeMillis() = now
    fun advance(millis: Long) { now += millis }
}
