package me.matsumo.agentguiplugin.viewmodel

import kotlin.test.Test
import kotlin.test.assertEquals

class UsageTrackerTest {

    // ────────────────────────────────────────
    // 1: 初期状態
    // ────────────────────────────────────────

    @Test
    fun `initial usage is all zero`() {
        val tracker = UsageTracker()
        val usage = tracker.usage.value
        assertEquals(0f, usage.contextUsage)
        assertEquals(0L, usage.totalInputTokens)
        assertEquals(0.0, usage.totalCostUsd)
    }

    // ────────────────────────────────────────
    // 2: onMessageStart + onResult でコンテキスト使用率
    // ────────────────────────────────────────

    @Test
    fun `context usage is calculated correctly`() {
        val tracker = UsageTracker()
        tracker.onMessageStart(inputTokens = 100_000L, cacheCreation = 0L, cacheRead = 0L)
        tracker.onResult(totalCostUsd = null)

        assertEquals(0.5f, tracker.usage.value.contextUsage, 0.001f)
        assertEquals(100_000L, tracker.usage.value.totalInputTokens)
    }

    // ────────────────────────────────────────
    // 3: cache トークンの加算
    // ────────────────────────────────────────

    @Test
    fun `cache tokens are added to input tokens`() {
        val tracker = UsageTracker()
        tracker.onMessageStart(inputTokens = 50_000L, cacheCreation = 20_000L, cacheRead = 30_000L)
        tracker.onResult(totalCostUsd = null)

        assertEquals(100_000L, tracker.usage.value.totalInputTokens)
        assertEquals(0.5f, tracker.usage.value.contextUsage, 0.001f)
    }

    // ────────────────────────────────────────
    // 4: onResult でコスト更新
    // ────────────────────────────────────────

    @Test
    fun `onResult updates total cost`() {
        val tracker = UsageTracker()
        tracker.onMessageStart(inputTokens = 10_000L, cacheCreation = 0L, cacheRead = 0L)
        tracker.onResult(totalCostUsd = 0.05)

        assertEquals(0.05, tracker.usage.value.totalCostUsd)
    }

    // ────────────────────────────────────────
    // 5: onResult(null) で既存値維持
    // ────────────────────────────────────────

    @Test
    fun `onResult with null cost preserves existing cost`() {
        val tracker = UsageTracker()
        tracker.onMessageStart(inputTokens = 10_000L, cacheCreation = 0L, cacheRead = 0L)
        tracker.onResult(totalCostUsd = 0.05)
        tracker.onResult(totalCostUsd = null)

        assertEquals(0.05, tracker.usage.value.totalCostUsd)
    }

    // ────────────────────────────────────────
    // 6: 使用率の上限クランプ
    // ────────────────────────────────────────

    @Test
    fun `context usage clamps to 1`() {
        val tracker = UsageTracker()
        tracker.onMessageStart(inputTokens = 300_000L, cacheCreation = 0L, cacheRead = 0L)
        tracker.onResult(totalCostUsd = null)

        assertEquals(1.0f, tracker.usage.value.contextUsage)
    }

    // ────────────────────────────────────────
    // 7: カスタム contextWindow
    // ────────────────────────────────────────

    @Test
    fun `custom context window affects calculation`() {
        val tracker = UsageTracker(contextWindow = 100_000L)
        tracker.onMessageStart(inputTokens = 50_000L, cacheCreation = 0L, cacheRead = 0L)
        tracker.onResult(totalCostUsd = null)

        assertEquals(0.5f, tracker.usage.value.contextUsage, 0.001f)
    }

    // ────────────────────────────────────────
    // 8: reset
    // ────────────────────────────────────────

    @Test
    fun `reset clears all state`() {
        val tracker = UsageTracker()
        tracker.onMessageStart(inputTokens = 100_000L, cacheCreation = 0L, cacheRead = 0L)
        tracker.onResult(totalCostUsd = 0.1)

        tracker.reset()

        val usage = tracker.usage.value
        assertEquals(0f, usage.contextUsage)
        assertEquals(0L, usage.totalInputTokens)
        assertEquals(0.0, usage.totalCostUsd)
    }
}
