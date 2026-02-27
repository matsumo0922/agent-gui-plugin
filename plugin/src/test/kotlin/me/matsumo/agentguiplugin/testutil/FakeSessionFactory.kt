package me.matsumo.agentguiplugin.testutil

import me.matsumo.claude.agent.ClaudeSDKClient
import me.matsumo.claude.agent.createSession
import me.matsumo.claude.agent.resumeSession
import me.matsumo.claude.agent.types.SessionOptionsBuilder

/**
 * SDK クライアントの生成を抽象化するインターフェース。
 * テスト時に FakeSessionFactory に差し替えることで、CLI プロセスなしでテスト可能にする。
 *
 * NOTE: Phase B（リファクタリング後）で BranchSessionManager / SessionCoordinator に注入される。
 * Phase A では FakeSessionFactory を直接テスト内で使用する。
 */
interface SessionFactory {
    suspend fun create(config: SessionOptionsBuilder.() -> Unit): ClaudeSDKClient
    suspend fun resume(sessionId: String, config: SessionOptionsBuilder.() -> Unit): ClaudeSDKClient
}

/** プロダクションコード用: 実際の SDK を呼ぶ */
internal class DefaultSessionFactory : SessionFactory {
    override suspend fun create(config: SessionOptionsBuilder.() -> Unit): ClaudeSDKClient =
        createSession(config)

    override suspend fun resume(sessionId: String, config: SessionOptionsBuilder.() -> Unit): ClaudeSDKClient =
        resumeSession(sessionId, config)
}

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
