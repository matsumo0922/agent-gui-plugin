package me.matsumo.agentguiplugin.service

import androidx.compose.runtime.Immutable
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import me.matsumo.agentguiplugin.model.RawContentBlock
import me.matsumo.agentguiplugin.model.RawConversationEntry
import me.matsumo.agentguiplugin.model.RawSessionMeta
import me.matsumo.agentguiplugin.viewmodel.ChatMessage
import me.matsumo.agentguiplugin.viewmodel.UiContentBlock
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.util.*
import kotlin.io.path.extension

@Service(Service.Level.PROJECT)
class SessionHistoryService(private val project: Project) {

    @Immutable
    data class SessionSummary(
        val sessionId: String,
        val projectPath: String,
        val firstPrompt: String?,
        val userMessageCount: Int,
        val assistantMessageCount: Int,
        val startTime: Instant?,
        val durationMinutes: Int?,
        val model: String?,
        val totalCostUsd: Double?,
    )

    companion object {
        private val claudeDir: Path = Path.of(System.getProperty("user.home"), ".claude")
        private val json = Json { ignoreUnknownKeys = true }

        fun normalizeProjectPath(path: String): String {
            return try {
                Path.of(path).toRealPath().toString()
            } catch (_: Exception) {
                path
            }
        }

        fun encodeClaudeProjectPath(normalizedPath: String): String {
            return normalizedPath.replace("/", "-")
        }
    }

    suspend fun listSessions(): List<SessionSummary> = withContext(Dispatchers.IO) {
        val metaDir = claudeDir.resolve("usage-data").resolve("session-meta")
        if (!Files.isDirectory(metaDir)) return@withContext emptyList()

        val projectPath = project.basePath ?: return@withContext emptyList()
        val normalizedProjectPath = normalizeProjectPath(projectPath)

        Files.list(metaDir).use { stream ->
            stream.filter { it.extension == "json" }
                .toList()
                .mapNotNull { file -> parseSummary(file, normalizedProjectPath) }
                .sortedByDescending { it.startTime }
        }
    }

    suspend fun readSessionMessages(sessionId: String): List<ChatMessage> = withContext(Dispatchers.IO) {
        val projectPath = project.basePath ?: return@withContext emptyList()
        val normalizedPath = normalizeProjectPath(projectPath)
        val encodedPath = encodeClaudeProjectPath(normalizedPath)
        val sessionFile = claudeDir.resolve("projects").resolve(encodedPath).resolve("$sessionId.jsonl")
        if (!Files.isReadable(sessionFile)) return@withContext emptyList()

        parseSessionMessages(sessionFile)
    }

    // --- Private parsing methods ---

    private fun parseSummary(file: Path, normalizedProjectPath: String): SessionSummary? {
        return try {
            val text = Files.readString(file)
            val meta = json.decodeFromString<RawSessionMeta>(text)

            val sessionProjectPath = meta.projectPath ?: return null
            val normalizedProjectPath = normalizeProjectPath(normalizedProjectPath)

            if (normalizedProjectPath != normalizeProjectPath(sessionProjectPath)) return null

            val sessionId = meta.sessionId ?: file.fileName.toString().removeSuffix(".json")

            SessionSummary(
                sessionId = sessionId,
                projectPath = normalizedProjectPath,
                firstPrompt = meta.firstPrompt,
                userMessageCount = meta.userMessageCount,
                assistantMessageCount = meta.assistantMessageCount,
                startTime = parseStartTime(meta.startTime),
                durationMinutes = meta.durationMinutes,
                model = meta.model,
                totalCostUsd = meta.totalCostUsd,
            )
        } catch (e: Exception) {
            println("Failed to parse session meta: $file, ${e.message}")
            null
        }
    }

    private fun parseStartTime(element: kotlinx.serialization.json.JsonElement?): Instant? {
        if (element == null) return null
        val primitive = element.jsonPrimitive

        // Long (epochMillis) → Instant
        primitive.longOrNull?.let { return Instant.ofEpochMilli(it) }

        // ISO 8601 string → Instant
        primitive.contentOrNull?.let {
            return try {
                Instant.parse(it)
            } catch (_: Exception) {
                null
            }
        }

        return null
    }

    private fun parseSessionMessages(sessionFile: Path): List<ChatMessage> {
        val messages = mutableListOf<ChatMessage>()

        try {
            Files.readAllLines(sessionFile).forEach { line ->
                if (line.isBlank()) return@forEach
                try {
                    val entry = json.decodeFromString<RawConversationEntry>(line)
                    if (entry.isSidechain) return@forEach

                    when (entry.type) {
                        "human", "user" -> parseUserMessage(entry)?.let(messages::add)
                        "assistant" -> parseAssistantMessage(entry)?.let(messages::add)
                    }
                } catch (e: Exception) {
                    println("Failed to parse session message line: $line, ${e.message}")
                }
            }
        } catch (e: Exception) {
            println("Failed to read session file: $sessionFile, ${e.message}")
        }

        return messages
    }

    private fun parseUserMessage(entry: RawConversationEntry): ChatMessage.User? {
        val content = entry.message?.content ?: return null

        val text = when {
            // String 形式
            runCatching { content.jsonPrimitive }.isSuccess ->
                content.jsonPrimitive.contentOrNull

            // Array 形式: text ブロックを結合
            runCatching { content.jsonArray }.isSuccess ->
                content.jsonArray
                    .map { json.decodeFromString<RawContentBlock>(it.toString()) }
                    .filter { it.type == "text" }
                    .joinToString("\n") { it.text.orEmpty() }

            else -> null
        }

        if (text.isNullOrBlank()) return null

        return ChatMessage.User(
            id = UUID.randomUUID().toString(),
            text = text,
        )
    }

    private fun parseAssistantMessage(entry: RawConversationEntry): ChatMessage.Assistant? {
        val contentArray = runCatching {
            entry.message?.content?.jsonArray
        }.getOrNull() ?: return null

        val blocks = contentArray
            .map { json.decodeFromString<RawContentBlock>(it.toString()) }
            .mapNotNull { block ->
                when (block.type) {
                    "text" -> block.text?.let(UiContentBlock::Text)
                    "thinking" -> block.thinking?.let(UiContentBlock::Thinking)
                    "tool_use" -> UiContentBlock.ToolUse(
                        toolName = block.name ?: "unknown",
                        inputJson = block.input ?: JsonObject(emptyMap()),
                    )
                    else -> null
                }
            }

        if (blocks.isEmpty()) return null

        return ChatMessage.Assistant(
            id = UUID.randomUUID().toString(),
            blocks = blocks.toImmutableList(),
        )
    }
}
