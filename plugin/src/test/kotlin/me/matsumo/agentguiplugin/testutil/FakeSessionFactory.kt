package me.matsumo.agentguiplugin.testutil

import me.matsumo.agentguiplugin.viewmodel.session.SessionFactory
import me.matsumo.claude.agent.ClaudeSDKClient
import me.matsumo.claude.agent.types.SessionOptionsBuilder

/**
 * テスト用 Fake: キューベースでクライアントを返し、呼び出しを記録する。
 */
class FakeSessionFactory : SessionFactory {
    private val clientQueue = ArrayDeque<ClaudeSDKClient>()

    data class CreateCall(val config: SessionOptionsBuilder)
    data class ResumeCall(val sessionId: String, val config: SessionOptionsBuilder)

    val createCalls = mutableListOf<CreateCall>()
    val resumeCalls = mutableListOf<ResumeCall>()

    fun enqueueClient(vararg clients: ClaudeSDKClient) {
        clients.forEach { clientQueue.addLast(it) }
    }

    private fun dequeue(): ClaudeSDKClient =
        clientQueue.removeFirstOrNull() ?: error("FakeSessionFactory: no more clients in queue")

    override suspend fun create(config: SessionOptionsBuilder.() -> Unit): ClaudeSDKClient {
        val builder = SessionOptionsBuilder().apply(config)
        createCalls.add(CreateCall(builder))
        return dequeue()
    }

    override suspend fun resume(sessionId: String, config: SessionOptionsBuilder.() -> Unit): ClaudeSDKClient {
        val builder = SessionOptionsBuilder().apply(config)
        resumeCalls.add(ResumeCall(sessionId, builder))
        return dequeue()
    }
}
