package me.matsumo.agentguiplugin.viewmodel

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.matsumo.agentguiplugin.viewmodel.transcript.DefaultFileLineReader
import me.matsumo.agentguiplugin.viewmodel.transcript.FileLineReader
import me.matsumo.agentguiplugin.viewmodel.transcript.TranscriptParser
import me.matsumo.agentguiplugin.viewmodel.transcript.TranscriptTailer
import me.matsumo.claude.agent.types.SubAgentIdResolver
import me.matsumo.claude.agent.types.SubAgentPaths

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

    // hookToolUseId <-> parentToolUseId resolution (FIFO)
    private val idResolver = SubAgentIdResolver()

    // agentId -> keyRef
    private val agentKeyRefs = mutableMapOf<String, SubAgentIdResolver.KeyRef>()

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
            val jsonlPath = SubAgentPaths.subAgentTranscriptPath(transcriptPath, agentId)

            val keyRef = idResolver.register(hookToolUseId)

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

            tailer.start(jsonlPath) { parsed ->
                val currentKey = keyRef.currentKey
                when (parsed) {
                    is TranscriptParser.ParsedLine.Msg -> {
                        val message = parsed.message
                        _tasks.update { tasks ->
                            val existing = tasks[currentKey]
                            val oldMessages = existing?.messages ?: persistentListOf()

                            val existingIndex = oldMessages.indexOfFirst { it.id == message.id }
                            val newMessages = if (existingIndex >= 0) {
                                oldMessages.toMutableList().apply { set(existingIndex, message) }.toImmutableList()
                            } else {
                                (oldMessages + message).toImmutableList()
                            }

                            val task = (existing ?: SubAgentTask(id = currentKey, timelineSessionId = sessionId))
                                .copy(messages = newMessages)
                            tasks + (currentKey to task)
                        }
                    }
                    is TranscriptParser.ParsedLine.ToolResults -> {
                        _tasks.update { tasks ->
                            val existing = tasks[currentKey] ?: SubAgentTask(id = currentKey, timelineSessionId = sessionId)
                            val merged = (existing.toolResults + parsed.results).toImmutableMap()
                            tasks + (currentKey to existing.copy(toolResults = merged))
                        }
                    }
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
            val taskKey = keyRef.currentKey
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
            val result = idResolver.resolve(realParentToolUseId) ?: return

            // Re-key the SubAgentTask from hookId to realParentToolUseId
            _tasks.update { tasks ->
                val task = tasks[result.hookToolUseId] ?: return@update tasks
                val newTasks = tasks.toMutableMap()
                newTasks.remove(result.hookToolUseId)
                val existingAtPid = newTasks[result.parentToolUseId]
                val mergedMessages = (task.messages + (existingAtPid?.messages ?: persistentListOf())).toImmutableList()
                newTasks[result.parentToolUseId] = task.copy(id = result.parentToolUseId, messages = mergedMessages)
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
            agentKeyRefs.clear()
            idResolver.clear()
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
