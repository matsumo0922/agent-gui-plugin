package me.matsumo.agentguiplugin.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * JSONL の各行（会話エントリ）のトップレベル構造。
 */
@Serializable
data class RawConversationEntry(
    val type: String? = null,
    val isSidechain: Boolean = false,
    val isMeta: Boolean = false,
    val timestamp: String? = null,
    val sessionId: String? = null,
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
    val model: String? = null,
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
