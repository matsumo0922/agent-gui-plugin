package me.matsumo.agentguiplugin.viewmodel.permission

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Semaphore
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import me.matsumo.agentguiplugin.viewmodel.ChatUiState
import me.matsumo.agentguiplugin.viewmodel.PendingPermission
import me.matsumo.agentguiplugin.viewmodel.PendingQuestion
import me.matsumo.agentguiplugin.viewmodel.util.toJsonElement
import me.matsumo.claude.agent.types.PermissionResult
import me.matsumo.claude.agent.types.PermissionResultAllow
import me.matsumo.claude.agent.types.PermissionResultDeny

internal class PermissionHandler(
    private val currentState: () -> ChatUiState,
    private val updateState: ((ChatUiState) -> ChatUiState) -> Unit,
) {
    private enum class RequestType { Permission, Question }

    private data class ActiveRequest(
        val type: RequestType,
        val deferred: CompletableDeferred<PermissionResult>,
    )

    private val gate = Semaphore(1)

    @Volatile
    private var active: ActiveRequest? = null

    suspend fun request(toolName: String, input: Map<String, Any?>): PermissionResult {
        gate.acquire()
        val type = if (toolName == ToolNames.ASK_USER_QUESTION) RequestType.Question else RequestType.Permission
        val deferred = CompletableDeferred<PermissionResult>()
        active = ActiveRequest(type, deferred)

        updateState {
            it.copy(
                pendingPermission = if (type == RequestType.Permission) PendingPermission(toolName, input) else null,
                pendingQuestion = if (type == RequestType.Question) PendingQuestion(toolName, input) else null,
            )
        }

        return try {
            deferred.await()
        } finally {
            active = null
            updateState { it.copy(pendingPermission = null, pendingQuestion = null) }
            gate.release()
        }
    }

    /**
     * 保留中の permission/question リクエストをキャンセルする。
     * clear() / dispose() 時に呼び出される。
     */
    fun cancelPending() {
        active?.deferred?.cancel()
        active = null
        updateState { it.copy(pendingPermission = null, pendingQuestion = null) }
    }

    fun respondPermission(allow: Boolean, denyMessage: String) {
        val req = active ?: return
        val message = denyMessage.ifBlank { "Denied by user" }
        val result: PermissionResult = if (allow) PermissionResultAllow() else PermissionResultDeny(message = message)
        req.deferred.complete(result)
    }

    fun respondQuestion(answers: Map<String, String>) {
        val req = active ?: return
        val pendingQ = currentState().pendingQuestion ?: return

        val originalQuestionsJson = pendingQ.toolInput.toJsonElement()
            .let { it as? JsonObject }
            ?.get("questions")

        val updatedInput = buildJsonObject {
            originalQuestionsJson?.let { put("questions", it) }
            put("answers", JsonObject(answers.mapValues { (_, v) -> JsonPrimitive(v) }))
        }

        req.deferred.complete(PermissionResultAllow(updatedInput = updatedInput))
    }
}
