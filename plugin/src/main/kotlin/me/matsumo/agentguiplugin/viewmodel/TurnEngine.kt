package me.matsumo.agentguiplugin.viewmodel

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import me.matsumo.agentguiplugin.model.AttachedFile
import me.matsumo.claude.agent.ClaudeSDKClient
import me.matsumo.claude.agent.types.AssistantMessage
import me.matsumo.claude.agent.types.ResultMessage
import me.matsumo.claude.agent.types.StreamEvent
import me.matsumo.claude.agent.types.SystemMessage
import me.matsumo.claude.agent.types.UserMessage
import java.util.concurrent.atomic.AtomicLong

/**
 * メッセージ送受信と応答収集の統一エンジン。
 * sendMessage と editMessage の重複する when(message) ブロックを共通化。
 */
class TurnEngine(private val scope: CoroutineScope) {

    sealed interface TurnEvent {
        data class System(val message: SystemMessage) : TurnEvent
        data class Stream(val event: StreamEvent) : TurnEvent
        data class Assistant(val message: AssistantMessage) : TurnEvent
        data class Result(val message: ResultMessage) : TurnEvent
    }

    private val activeTurnId = AtomicLong(0L)
    private var activeTurnJob: Job? = null

    /**
     * メッセージを送信し、応答をイベントとして通知する。
     * sendMessage と editMessage の共通ロジック。
     */
    fun dispatch(
        client: ClaudeSDKClient,
        text: String,
        files: List<AttachedFile> = emptyList(),
        onEvent: (TurnEvent) -> Unit,
        onError: (Exception) -> Unit,
    ): Job {
        activeTurnJob?.cancel()
        val turnId = activeTurnId.incrementAndGet()

        val job = scope.launch {
            try {
                if (files.isEmpty()) {
                    client.send(text)
                } else {
                    client.send(buildContentBlocks(text, files))
                }

                client.receiveResponse().collect { message ->
                    if (turnId != activeTurnId.get()) return@collect

                    when (message) {
                        is SystemMessage -> onEvent(TurnEvent.System(message))
                        is StreamEvent -> onEvent(TurnEvent.Stream(message))
                        is AssistantMessage -> onEvent(TurnEvent.Assistant(message))
                        is ResultMessage -> onEvent(TurnEvent.Result(message))
                        is UserMessage -> { /* tool results - ignore */ }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                onError(e)
            }
        }
        activeTurnJob = job
        return job
    }

    /**
     * 現在の turn を無効化して新しい turnId を返す。
     * abort 時に、ResultMessage を既存の collect で消費させつつ
     * イベントハンドリングをスキップするために使用。
     */
    fun invalidateCurrentTurn(): Long {
        return activeTurnId.incrementAndGet()
    }

    fun cancel() {
        activeTurnJob?.cancel()
        activeTurnJob = null
    }

    companion object {
        fun buildContentBlocks(text: String, files: List<AttachedFile>): List<JsonObject> {
            val blocks = mutableListOf<JsonObject>()
            blocks.add(
                buildJsonObject {
                    put("type", "text")
                    put("text", text)
                },
            )
            for (file in files) {
                if (file.isImage) {
                    blocks.add(file.toImageBlock())
                } else {
                    blocks.add(file.toDocumentBlock())
                }
            }
            return blocks
        }
    }
}
