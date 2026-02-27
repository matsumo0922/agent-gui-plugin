package me.matsumo.agentguiplugin.viewmodel

import com.intellij.openapi.diagnostic.Logger

private val logger = Logger.getInstance("ChatViewModel")

/**
 * SessionState の遷移を検証するユーティリティ。
 * 不正遷移時は警告ログを出力するが、状態は更新する（fail-open）。
 */
internal fun SessionStatus.transitionTo(
    next: SessionState,
    sessionId: String? = this.sessionId,
    errorMessage: String? = null,
): SessionStatus {
    if (!state.canTransitionTo(next)) {
        logger.warn("Invalid SessionState transition: $state → $next")
    }
    return SessionStatus(state = next, sessionId = sessionId, errorMessage = errorMessage)
}
