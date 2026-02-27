package me.matsumo.agentguiplugin.testutil

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import me.matsumo.claude.agent.ClaudeSDKClient
import me.matsumo.claude.agent.types.SDKMessage

/**
 * テスト用の relaxed ClaudeSDKClient モック生成ヘルパー。
 */
fun createMockClient(
    sessionId: String = "test-session",
    responses: Flow<SDKMessage> = emptyFlow(),
): ClaudeSDKClient = mockk(relaxed = true) {
    every { this@mockk.sessionId } returns sessionId
    every { receiveResponse() } returns responses
}
