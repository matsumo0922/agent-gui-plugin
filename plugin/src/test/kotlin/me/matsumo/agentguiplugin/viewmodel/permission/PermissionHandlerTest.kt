package me.matsumo.agentguiplugin.viewmodel.permission

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import me.matsumo.agentguiplugin.viewmodel.ChatUiState
import me.matsumo.claude.agent.types.PermissionResultAllow
import me.matsumo.claude.agent.types.PermissionResultDeny
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Phase A: 現行 PermissionHandler API のテスト。
 * リファクタリング前に振る舞いを固定するための回帰テスト。
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PermissionHandlerTest {

    private var state = ChatUiState()
    private val handler = PermissionHandler(
        currentState = { state },
        updateState = { transform -> state = transform(state) },
    )

    // ────────────────────────────────────────
    // A1: Permission 要求 → 許可応答
    // ────────────────────────────────────────

    @Test
    fun `permission request returns Allow on approve`() = runTest {
        val result = async {
            handler.request("Write", mapOf("file_path" to "/tmp/test.txt"))
        }
        advanceUntilIdle()

        // pendingPermission が設定されていること
        assertNotNull(state.pendingPermission)
        assertEquals("Write", state.pendingPermission?.toolName)

        handler.respondPermission(allow = true, denyMessage = "")
        advanceUntilIdle()

        assertIs<PermissionResultAllow>(result.await())

        // 応答後は UI 状態がクリアされること
        assertNull(state.pendingPermission)
    }

    // ────────────────────────────────────────
    // A2: Permission 要求 → 拒否応答
    // ────────────────────────────────────────

    @Test
    fun `permission request returns Deny on reject`() = runTest {
        val result = async {
            handler.request("Write", mapOf("file_path" to "/tmp/test.txt"))
        }
        advanceUntilIdle()

        handler.respondPermission(allow = false, denyMessage = "Not allowed")
        advanceUntilIdle()

        val deny = result.await()
        assertIs<PermissionResultDeny>(deny)
        assertEquals("Not allowed", deny.message)
    }

    // ────────────────────────────────────────
    // A3: Question 要求 → 回答応答
    // ────────────────────────────────────────

    @Test
    fun `question request returns Allow with answers`() = runTest {
        val result = async {
            handler.request(
                ToolNames.ASK_USER_QUESTION,
                mapOf("questions" to listOf(mapOf("question" to "Pick one", "options" to listOf("A", "B")))),
            )
        }
        advanceUntilIdle()

        // pendingQuestion が設定されていること
        assertNotNull(state.pendingQuestion)
        assertNull(state.pendingPermission)

        handler.respondQuestion(mapOf("Pick one" to "A"))
        advanceUntilIdle()

        val allow = result.await()
        assertIs<PermissionResultAllow>(allow)
        assertNotNull(allow.updatedInput)
    }

    // ────────────────────────────────────────
    // A4: cancelPending で deferred がキャンセル
    // ────────────────────────────────────────

    @Test
    fun `cancelPending cancels the pending deferred`() = runTest {
        var caught: Throwable? = null
        val job = launch {
            try {
                handler.request("Write", mapOf("file_path" to "/tmp/test.txt"))
            } catch (e: CancellationException) {
                caught = e
            }
        }
        advanceUntilIdle()

        handler.cancelPending()
        advanceUntilIdle()

        job.join()
        assertNotNull(caught)
        assertNull(state.pendingPermission)
    }

    // ────────────────────────────────────────
    // A5: 直列化 (Semaphore)
    // ────────────────────────────────────────

    @Test
    fun `two concurrent requests are serialized by semaphore`() = runTest {
        val results = mutableListOf<String>()

        // 1つ目のリクエスト
        val job1 = launch {
            handler.request("Write", mapOf("file_path" to "/tmp/1.txt"))
            results.add("first")
        }
        advanceUntilIdle()

        // 2つ目は1つ目が完了するまでブロックされる
        val job2 = launch {
            handler.request("Read", mapOf("file_path" to "/tmp/2.txt"))
            results.add("second")
        }
        advanceUntilIdle()

        // 1つ目の状態
        assertEquals("Write", state.pendingPermission?.toolName)

        // 1つ目を完了
        handler.respondPermission(allow = true, denyMessage = "")
        advanceUntilIdle()

        // 2つ目が処理される
        assertEquals("Read", state.pendingPermission?.toolName)

        handler.respondPermission(allow = true, denyMessage = "")
        advanceUntilIdle()

        job1.join()
        job2.join()

        assertEquals(listOf("first", "second"), results)
    }

    // ────────────────────────────────────────
    // A6: 応答後の UI 状態クリア
    // ────────────────────────────────────────

    @Test
    fun `UI state is cleared after permission response`() = runTest {
        val result = async {
            handler.request("Write", mapOf("file_path" to "/tmp/test.txt"))
        }
        advanceUntilIdle()

        assertNotNull(state.pendingPermission)

        handler.respondPermission(allow = true, denyMessage = "")
        advanceUntilIdle()
        result.await()

        assertNull(state.pendingPermission)
        assertNull(state.pendingQuestion)
    }

    // ────────────────────────────────────────
    // A7: 空文字 denyMessage のフォールバック
    // ────────────────────────────────────────

    @Test
    fun `empty deny message falls back to default`() = runTest {
        val result = async {
            handler.request("Write", mapOf("file_path" to "/tmp/test.txt"))
        }
        advanceUntilIdle()

        handler.respondPermission(allow = false, denyMessage = "")
        advanceUntilIdle()

        val deny = result.await()
        assertIs<PermissionResultDeny>(deny)
        assertEquals("Denied by user", deny.message)
    }

    // ────────────────────────────────────────
    // B1: respondPermission で Question 型ガード
    // ────────────────────────────────────────

    @Test
    fun `respondPermission ignores when active type is Question`() = runTest {
        val job = launch {
            handler.request(
                ToolNames.ASK_USER_QUESTION,
                mapOf("questions" to listOf(mapOf("question" to "Pick", "options" to listOf("A")))),
            )
        }
        advanceUntilIdle()

        assertNotNull(state.pendingQuestion)

        // Permission として応答を試みる → 型ガードで無視される
        handler.respondPermission(allow = true, denyMessage = "")
        advanceUntilIdle()

        // deferred はまだ未完了
        assertTrue(job.isActive)

        // cancelActiveRequest で正しくキャンセルできる
        handler.cancelActiveRequest()
        advanceUntilIdle()
        assertTrue(job.isCompleted)
    }

    // ────────────────────────────────────────
    // B2: respondQuestion で Permission 型ガード
    // ────────────────────────────────────────

    @Test
    fun `respondQuestion ignores when active type is Permission`() = runTest {
        val job = launch {
            handler.request("Write", mapOf("file_path" to "/tmp/test.txt"))
        }
        advanceUntilIdle()

        assertNotNull(state.pendingPermission)

        // Question として応答を試みる → 型ガードで無視される
        handler.respondQuestion(mapOf("q" to "a"))
        advanceUntilIdle()

        // deferred はまだ未完了
        assertTrue(job.isActive)

        // respondPermission で正しく完了できる
        handler.respondPermission(allow = true, denyMessage = "")
        advanceUntilIdle()
        assertTrue(job.isCompleted)
    }

    // ────────────────────────────────────────
    // B3: cancelActiveRequest で型を問わず Deny
    // ────────────────────────────────────────

    @Test
    fun `cancelActiveRequest cancels any active request type`() = runTest {
        // Permission リクエスト
        val result1 = async {
            handler.request("Write", mapOf("file_path" to "/tmp/test.txt"))
        }
        advanceUntilIdle()
        handler.cancelActiveRequest()
        advanceUntilIdle()
        val deny1 = result1.await()
        assertIs<PermissionResultDeny>(deny1)
        assertEquals("Cancelled by user", deny1.message)

        // Question リクエスト
        val result2 = async {
            handler.request(
                ToolNames.ASK_USER_QUESTION,
                mapOf("questions" to listOf(mapOf("question" to "Pick", "options" to listOf("A")))),
            )
        }
        advanceUntilIdle()
        handler.cancelActiveRequest("User cancelled question")
        advanceUntilIdle()
        val deny2 = result2.await()
        assertIs<PermissionResultDeny>(deny2)
        assertEquals("User cancelled question", deny2.message)
    }
}
