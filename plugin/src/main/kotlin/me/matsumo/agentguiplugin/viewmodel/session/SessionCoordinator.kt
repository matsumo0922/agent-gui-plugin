package me.matsumo.agentguiplugin.viewmodel.session

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import me.matsumo.agentguiplugin.viewmodel.BranchSessionManager
import me.matsumo.claude.agent.ClaudeSDKClient
import me.matsumo.claude.agent.types.Model
import me.matsumo.claude.agent.types.PermissionMode
import me.matsumo.claude.agent.types.SessionOptionsBuilder

/**
 * セッションのライフサイクルを一元管理するコーディネーター。
 * activeClient の保持・切り替え・クリーンアップ、BranchSessionManager の統合を担当。
 */
class SessionCoordinator(
    private val sessionFactory: SessionFactory = DefaultSessionFactory(),
    private val applyCommonConfig: SessionOptionsBuilder.(Model, PermissionMode) -> Unit,
) {
    val branchSessionManager = BranchSessionManager(sessionFactory, applyCommonConfig)

    @Volatile
    var activeClient: ClaudeSDKClient? = null
        private set

    val activeSessionId: String? get() = activeClient?.sessionId

    /**
     * 新規セッションを作成またはセッションを再開し、activeClient に設定する。
     * disposed チェックは呼び出し元で行うこと。
     */
    suspend fun connect(
        model: Model,
        permissionMode: PermissionMode,
        resumeSessionId: String? = null,
    ): ClaudeSDKClient {
        val localClient = if (resumeSessionId != null) {
            sessionFactory.resume(resumeSessionId) {
                applyCommonConfig(model, permissionMode)
                forkSession = true
            }
        } else {
            sessionFactory.create {
                applyCommonConfig(model, permissionMode)
            }
        }

        if (!currentCoroutineContext().isActive) {
            localClient.close()
            throw CancellationException("Cancelled before connect")
        }

        localClient.connect()
        activeClient = localClient
        return localClient
    }

    /**
     * activeClient を新しいクライアントに切り替える。
     * 旧クライアントが BranchSessionManager 管理外の場合は close する。
     */
    fun switchClient(newClient: ClaudeSDKClient) {
        val oldClient = activeClient
        activeClient = newClient

        if (oldClient != null && oldClient !== newClient) {
            val oldSessionId = oldClient.sessionId
            if (oldSessionId == null || !branchSessionManager.hasSession(oldSessionId)) {
                oldClient.close()
            }
        }
    }

    /**
     * ナビゲーション時に activeClient の参照だけ外す（close はしない）。
     */
    fun clearActiveClient() {
        activeClient = null
    }

    /**
     * 全セッションを閉じる。dispose / clear 時に使用。
     */
    fun closeAll() {
        branchSessionManager.closeAll()
        activeClient?.close()
        activeClient = null
    }
}
