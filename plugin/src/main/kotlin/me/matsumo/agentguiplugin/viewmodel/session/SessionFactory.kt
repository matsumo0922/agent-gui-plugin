package me.matsumo.agentguiplugin.viewmodel.session

import me.matsumo.claude.agent.ClaudeSDKClient
import me.matsumo.claude.agent.createSession
import me.matsumo.claude.agent.resumeSession
import me.matsumo.claude.agent.types.SessionOptionsBuilder

/**
 * SDK クライアントの生成を抽象化するインターフェース。
 * テスト時に FakeSessionFactory に差し替えることで、CLI プロセスなしでテスト可能にする。
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
