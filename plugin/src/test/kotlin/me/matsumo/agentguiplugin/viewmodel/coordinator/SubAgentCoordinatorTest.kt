package me.matsumo.agentguiplugin.viewmodel.coordinator

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import me.matsumo.agentguiplugin.testutil.FakeClock
import me.matsumo.agentguiplugin.viewmodel.SubAgentCoordinator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SubAgentCoordinatorTest {

    // ────────────────────────────────────────
    // 1: onSubAgentStart でタスク作成
    // ────────────────────────────────────────

    @Test
    fun `onSubAgentStart creates task under hookToolUseId`() = runTest {
        val clock = FakeClock(1000L)
        val coordinator = SubAgentCoordinator(backgroundScope, clock)

        coordinator.onSubAgentStart(
            agentId = "agent-1",
            transcriptPath = "/tmp/transcript",
            hookToolUseId = "hook-uuid-1",
            sessionId = "session-1",
        )

        val tasks = coordinator.tasks.value
        assertEquals(1, tasks.size)
        val task = tasks["hook-uuid-1"]
        assertNotNull(task)
        assertEquals("hook-uuid-1", task.id)
        assertEquals("session-1", task.timelineSessionId)
        assertEquals(1000L, task.startedAt)
    }

    // ────────────────────────────────────────
    // 2: resolveParentToolUseId で re-key
    // ────────────────────────────────────────

    @Test
    fun `resolveParentToolUseId re-keys task from hookId to realId`() = runTest {
        val clock = FakeClock(1000L)
        val coordinator = SubAgentCoordinator(backgroundScope, clock)

        coordinator.onSubAgentStart(
            agentId = "agent-1",
            transcriptPath = "/tmp/transcript",
            hookToolUseId = "hook-uuid-1",
            sessionId = "session-1",
        )

        coordinator.resolveParentToolUseId("toolu_abc123")

        val tasks = coordinator.tasks.value
        assertNull(tasks["hook-uuid-1"])
        val task = tasks["toolu_abc123"]
        assertNotNull(task)
        assertEquals("toolu_abc123", task.id)
        assertEquals("session-1", task.timelineSessionId)
    }

    // ────────────────────────────────────────
    // 3: resolve 済みの ID に対する重複 resolve は no-op
    // ────────────────────────────────────────

    @Test
    fun `resolveParentToolUseId is no-op when id already exists`() = runTest {
        val coordinator = SubAgentCoordinator(backgroundScope, FakeClock())

        coordinator.onSubAgentStart("agent-1", "/tmp/t", "hook-1", "s1")
        coordinator.resolveParentToolUseId("toolu_1")

        // 同じ realId で再度 resolve → tasks は変わらない
        coordinator.resolveParentToolUseId("toolu_1")

        assertEquals(1, coordinator.tasks.value.size)
        assertNotNull(coordinator.tasks.value["toolu_1"])
    }

    // ────────────────────────────────────────
    // 4: onSubAgentStop で完了時刻設定
    // ────────────────────────────────────────

    @Test
    fun `onSubAgentStop sets completedAt`() = runTest {
        val clock = FakeClock(1000L)
        val coordinator = SubAgentCoordinator(backgroundScope, clock)

        coordinator.onSubAgentStart("agent-1", "/tmp/t", "hook-1", "s1")
        clock.advance(5000L)
        coordinator.onSubAgentStop("agent-1")

        val task = coordinator.tasks.value["hook-1"]
        assertNotNull(task)
        assertEquals(6000L, task.completedAt)
    }

    // ────────────────────────────────────────
    // 5: stopAll でテイラー停止
    // ────────────────────────────────────────

    @Test
    fun `stopAll clears internal state but preserves tasks`() = runTest {
        val coordinator = SubAgentCoordinator(backgroundScope, FakeClock())

        coordinator.onSubAgentStart("agent-1", "/tmp/t", "hook-1", "s1")
        coordinator.onSubAgentStart("agent-2", "/tmp/t", "hook-2", "s1")

        coordinator.stopAll()

        // Tasks are preserved for UI display
        assertEquals(2, coordinator.tasks.value.size)
    }

    // ────────────────────────────────────────
    // 6: 並行アクセスの安全性
    // ────────────────────────────────────────

    @Test
    fun `concurrent access does not throw`() = runTest {
        val coordinator = SubAgentCoordinator(backgroundScope, FakeClock())

        val jobs = (1..10).map { i ->
            launch {
                coordinator.onSubAgentStart("agent-$i", "/tmp/t", "hook-$i", "s1")
            }
        }
        jobs.forEach { it.join() }

        assertEquals(10, coordinator.tasks.value.size)

        val stopJobs = (1..10).map { i ->
            launch {
                coordinator.onSubAgentStop("agent-$i")
            }
        }
        stopJobs.forEach { it.join() }
    }

    // ────────────────────────────────────────
    // 7: updateSpawnedByToolName
    // ────────────────────────────────────────

    @Test
    fun `updateSpawnedByToolName sets tool name`() = runTest {
        val coordinator = SubAgentCoordinator(backgroundScope, FakeClock())

        coordinator.onSubAgentStart("agent-1", "/tmp/t", "hook-1", "s1")
        coordinator.updateSpawnedByToolName("hook-1", "Task")

        val task = coordinator.tasks.value["hook-1"]
        assertNotNull(task)
        assertEquals("Task", task.spawnedByToolName)
    }

    @Test
    fun `updateSpawnedByToolName does not overwrite existing name`() = runTest {
        val coordinator = SubAgentCoordinator(backgroundScope, FakeClock())

        coordinator.onSubAgentStart("agent-1", "/tmp/t", "hook-1", "s1")
        coordinator.updateSpawnedByToolName("hook-1", "Task")
        coordinator.updateSpawnedByToolName("hook-1", "Write")

        assertEquals("Task", coordinator.tasks.value["hook-1"]?.spawnedByToolName)
    }

    // ────────────────────────────────────────
    // 8: hookToolUseId == null は無視
    // ────────────────────────────────────────

    @Test
    fun `onSubAgentStart ignores null hookToolUseId`() = runTest {
        val coordinator = SubAgentCoordinator(backgroundScope, FakeClock())
        coordinator.onSubAgentStart("agent-1", "/tmp/t", null, "s1")
        assertEquals(0, coordinator.tasks.value.size)
    }

    // ────────────────────────────────────────
    // 9: reset で全クリア
    // ────────────────────────────────────────

    @Test
    fun `reset clears everything including tasks`() = runTest {
        val coordinator = SubAgentCoordinator(backgroundScope, FakeClock())
        coordinator.onSubAgentStart("agent-1", "/tmp/t", "hook-1", "s1")
        coordinator.reset()
        assertEquals(0, coordinator.tasks.value.size)
    }
}
