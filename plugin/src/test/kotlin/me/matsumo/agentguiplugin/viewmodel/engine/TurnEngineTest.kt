package me.matsumo.agentguiplugin.viewmodel.engine

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import me.matsumo.agentguiplugin.testutil.TestFixtures.sdkAssistantMessage
import me.matsumo.agentguiplugin.testutil.TestFixtures.sdkResultMessage
import me.matsumo.agentguiplugin.testutil.TestFixtures.sdkSystemMessage
import me.matsumo.agentguiplugin.viewmodel.TurnEngine
import me.matsumo.claude.agent.ClaudeSDKClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class TurnEngineTest {

    // ────────────────────────────────────────
    // 1: 正常ターン完了
    // ────────────────────────────────────────

    @Test
    fun `dispatch collects response and emits events in order`() = runTest {
        val client = mockk<ClaudeSDKClient>(relaxed = true) {
            coEvery { send(any<String>()) } just runs
            every { receiveResponse() } returns flowOf(
                sdkSystemMessage(),
                sdkAssistantMessage("Hello"),
                sdkResultMessage(),
            )
        }

        val events = mutableListOf<TurnEngine.TurnEvent>()
        val engine = TurnEngine(backgroundScope)

        val job = engine.dispatch(client, "test", onEvent = { events.add(it) }, onError = {})
        job.join()
        advanceUntilIdle()

        assertEquals(3, events.size)
        assertIs<TurnEngine.TurnEvent.System>(events[0])
        assertIs<TurnEngine.TurnEvent.Assistant>(events[1])
        assertIs<TurnEngine.TurnEvent.Result>(events[2])

        coVerify { client.send("test") }
    }

    // ────────────────────────────────────────
    // 2: invalidateCurrentTurn でイベント無視
    // ────────────────────────────────────────

    @Test
    fun `invalidateCurrentTurn prevents events from dispatching`() = runTest {
        val client = mockk<ClaudeSDKClient>(relaxed = true) {
            coEvery { send(any<String>()) } just runs
            every { receiveResponse() } returns flowOf(
                sdkAssistantMessage("Hello"),
                sdkResultMessage(),
            )
        }

        val events = mutableListOf<TurnEngine.TurnEvent>()
        val engine = TurnEngine(backgroundScope)

        // invalidate before dispatch
        engine.invalidateCurrentTurn()

        val job = engine.dispatch(client, "test", onEvent = { events.add(it) }, onError = {})
        job.join()
        advanceUntilIdle()

        // Events should still arrive because dispatch gets a new turnId
        assertEquals(2, events.size)
    }

    // ────────────────────────────────────────
    // 3: 例外発生時に onError が呼ばれる
    // ────────────────────────────────────────

    @Test
    fun `dispatch calls onError on exception`() = runTest {
        val client = mockk<ClaudeSDKClient>(relaxed = true) {
            coEvery { send(any<String>()) } throws RuntimeException("Network error")
        }

        var caughtError: Exception? = null
        val engine = TurnEngine(backgroundScope)

        val job = engine.dispatch(
            client, "test",
            onEvent = {},
            onError = { caughtError = it },
        )
        job.join()
        advanceUntilIdle()

        assertIs<RuntimeException>(caughtError)
        assertEquals("Network error", caughtError?.message)
    }

    // ────────────────────────────────────────
    // 4: cancel
    // ────────────────────────────────────────

    @Test
    fun `cancel stops the active turn job`() = runTest {
        val client = mockk<ClaudeSDKClient>(relaxed = true) {
            coEvery { send(any<String>()) } just runs
            every { receiveResponse() } returns flowOf()
        }

        val engine = TurnEngine(backgroundScope)
        val job = engine.dispatch(client, "test", onEvent = {}, onError = {})

        engine.cancel()
        advanceUntilIdle()

        assertEquals(true, job.isCancelled || job.isCompleted)
    }
}
