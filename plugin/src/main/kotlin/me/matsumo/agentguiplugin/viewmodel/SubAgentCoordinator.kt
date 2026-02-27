package me.matsumo.agentguiplugin.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.matsumo.agentguiplugin.viewmodel.transcript.DefaultFileLineReader
import me.matsumo.agentguiplugin.viewmodel.transcript.FileLineReader
import me.matsumo.agentguiplugin.viewmodel.transcript.TranscriptTailer
import java.util.concurrent.atomic.AtomicReference

/**
 * サブエージェントのライフサイクル・テイリング・hookId解決を一元管理。
 * 全操作を Mutex で保護し、複合整合性を保証する。
 */
class SubAgentCoordinator(
    private val scope: CoroutineScope,
    private val clock: Clock = Clock.System,
    private val fileLineReader: FileLineReader = DefaultFileLineReader(),
) {
    private val mutex = Mutex()

    private val _tasks = MutableStateFlow<Map<String, SubAgentTask>>(emptyMap())
    val tasks: StateFlow<Map<String, SubAgentTask>> = _tasks.asStateFlow()

    // agentId -> tailer
    private val activeTailers = mutableMapOf<String, TranscriptTailer>()

    // hookToolUseId -> mutable key reference
    private val tailerKeyRefs = mutableMapOf<String, AtomicReference<String>>()

    // agentId -> keyRef
    private val agentKeyRefs = mutableMapOf<String, AtomicReference<String>>()

    // Ordered list of hookToolUseIds not yet mapped to real parentToolUseId
    private val unresolvedHookIds = mutableListOf<String>()

    /**
     * サブエージェント開始時に呼ばれる。
     * テイリングを開始し、初期タスクエントリを作成する。
     */
    suspend fun onSubAgentStart(
        agentId: String,
        transcriptPath: String,
        hookToolUseId: String?,
        sessionId: String?,
    ) {
        if (hookToolUseId == null) return

        mutex.withLock {
            val jsonlPath = "${transcriptPath.removeSuffix(".jsonl")}/subagents/agent-$agentId.jsonl"

            val keyRef = AtomicReference(hookToolUseId)
            tailerKeyRefs[hookToolUseId] = keyRef
            unresolvedHookIds.add(hookToolUseId)

            // Create initial SubAgentTask under hookToolUseId
            _tasks.update { tasks ->
                if (tasks.containsKey(hookToolUseId)) tasks
                else tasks + (hookToolUseId to SubAgentTask(
                    id = hookToolUseId,
                    timelineSessionId = sessionId,
                    startedAt = clock.currentTimeMillis(),
                ))
            }

            agentKeyRefs[agentId] = keyRef

            val tailer = TranscriptTailer(scope, fileLineReader)
            activeTailers[agentId] = tailer

            tailer.start(jsonlPath) { message ->
                val currentKey = keyRef.get()
                _tasks.update { tasks ->
                    val existing = tasks[currentKey]
                    val oldMessages = existing?.messages ?: emptyList()

                    val existingIndex = oldMessages.indexOfFirst { it.id == message.id }
                    val newMessages = if (existingIndex >= 0) {
                        oldMessages.toMutableList().apply { set(existingIndex, message) }
                    } else {
                        oldMessages + message
                    }

                    val task = (existing ?: SubAgentTask(id = currentKey, timelineSessionId = sessionId))
                        .copy(messages = newMessages)
                    tasks + (currentKey to task)
                }
            }
        }
    }

    /**
     * サブエージェント停止時に呼ばれる。
     * テイリングを停止し、完了時刻を設定する。
     */
    suspend fun onSubAgentStop(agentId: String) {
        mutex.withLock {
            activeTailers.remove(agentId)?.stop()

            val keyRef = agentKeyRefs.remove(agentId) ?: return
            val taskKey = keyRef.get()
            val now = clock.currentTimeMillis()

            _tasks.update { tasks ->
                val task = tasks[taskKey] ?: return@update tasks
                tasks + (taskKey to task.copy(completedAt = now))
            }
        }
    }

    /**
     * hookToolUseId → real parentToolUseId の解決。
     * stream-json から AssistantMessage を受信した際に、まだ未解決の hookId があれば
     * FIFO で解決し、タスクエントリのキーを変更する。
     */
    suspend fun resolveParentToolUseId(realParentToolUseId: String) {
        mutex.withLock {
            if (_tasks.value.containsKey(realParentToolUseId)) return
            if (unresolvedHookIds.isEmpty()) return

            val hookId = unresolvedHookIds.removeFirst()

            // Update the tailer's key so new messages go to the correct key
            tailerKeyRefs[hookId]?.set(realParentToolUseId)

            // Re-key the SubAgentTask from hookId to realParentToolUseId
            _tasks.update { tasks ->
                val task = tasks[hookId] ?: return@update tasks
                val newTasks = tasks.toMutableMap()
                newTasks.remove(hookId)
                val existingAtPid = newTasks[realParentToolUseId]
                val mergedMessages = task.messages + (existingAtPid?.messages ?: emptyList())
                newTasks[realParentToolUseId] = task.copy(id = realParentToolUseId, messages = mergedMessages)
                newTasks
            }
        }
    }

    /**
     * spawnedByToolName を更新する。
     */
    fun updateSpawnedByToolName(parentToolUseId: String, toolName: String) {
        _tasks.update { tasks ->
            val existing = tasks[parentToolUseId] ?: return@update tasks
            if (existing.spawnedByToolName != null) tasks
            else tasks + (parentToolUseId to existing.copy(spawnedByToolName = toolName))
        }
    }

    /**
     * 全テイラーを停止し、内部状態をクリアする。
     * タスク一覧はクリアしない（UI 表示のため保持）。
     */
    suspend fun stopAll() {
        mutex.withLock {
            activeTailers.values.forEach { it.stop() }
            activeTailers.clear()
            tailerKeyRefs.clear()
            agentKeyRefs.clear()
            unresolvedHookIds.clear()
        }
    }

    /**
     * タスク一覧も含めて全てリセットする。
     */
    suspend fun reset() {
        stopAll()
        _tasks.value = emptyMap()
    }
}

/**
 * 時刻取得の抽象化。テスト時に FakeClock で差し替え可能。
 */
fun interface Clock {
    fun currentTimeMillis(): Long

    companion object {
        val System: Clock = Clock { java.lang.System.currentTimeMillis() }
    }
}
