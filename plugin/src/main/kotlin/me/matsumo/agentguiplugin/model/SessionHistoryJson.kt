package me.matsumo.agentguiplugin.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * ~/.claude/usage-data/session-meta/{id}.json の構造。
 */
@Serializable
data class RawSessionMeta(
    @SerialName("session_id")
    val sessionId: String? = null,
    @SerialName("project_path")
    val projectPath: String? = null,
    @SerialName("first_prompt")
    val firstPrompt: String? = null,
    @SerialName("user_message_count")
    val userMessageCount: Int = 0,
    @SerialName("assistant_message_count")
    val assistantMessageCount: Int = 0,
    @SerialName("start_time")
    val startTime: JsonElement? = null,
    @SerialName("duration_minutes")
    val durationMinutes: Int? = null,
    @SerialName("model")
    val model: String? = null,
    @SerialName("total_cost_usd")
    val totalCostUsd: Double? = null,
)

/**
 * JSONL の各行（会話エントリ）のトップレベル構造。
 */
@Serializable
data class RawConversationEntry(
    val type: String? = null,
    val isSidechain: Boolean = false,
    val message: RawMessage? = null,
)

/**
 * 会話エントリ内の message フィールド。
 * content は String | Array なので JsonElement で受ける。
 */
@Serializable
data class RawMessage(
    val role: String? = null,
    val content: JsonElement? = null,
)

/**
 * content 配列内の各コンテンツブロック。
 * 全フィールドを nullable で持ち、type で判別する。
 */
@Serializable
data class RawContentBlock(
    val type: String? = null,
    val text: String? = null,
    val thinking: String? = null,
    val name: String? = null,
    val input: JsonObject? = null,
)
