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
import me.matsumo.agentguiplugin.model.RawContentBlock
import me.matsumo.agentguiplugin.model.RawConversationEntry
import me.matsumo.agentguiplugin.viewmodel.ChatMessage
import me.matsumo.agentguiplugin.viewmodel.UiContentBlock
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
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
        val projectPath = project.basePath ?: return@withContext emptyList()
        val normalizedPath = normalizeProjectPath(projectPath)
        val encodedPath = encodeClaudeProjectPath(normalizedPath)
        val projectDir = claudeDir.resolve("projects").resolve(encodedPath)

        if (!Files.isDirectory(projectDir)) return@withContext emptyList()

        Files.list(projectDir).use { stream ->
            stream.filter { it.extension == "jsonl" }
                .toList()
                .mapNotNull { file -> parseSummaryFromJsonl(file, normalizedPath) }
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

    private fun parseSummaryFromJsonl(file: Path, normalizedProjectPath: String): SessionSummary? {
        return try {
            val sessionId = file.fileName.toString().removeSuffix(".jsonl")
            val lines = Files.readAllLines(file)

            var firstPrompt: String? = null
            var startTime: Instant? = null
            var endTime: Instant? = null
            var model: String? = null
            var userCount = 0
            var assistantCount = 0

            for (line in lines) {
                if (line.isBlank()) continue

                val entry = try {
                    json.decodeFromString<RawConversationEntry>(line)
                } catch (_: Exception) {
                    continue
                }

                val ts = entry.timestamp?.let { parseInstant(it) }

                when (entry.type) {
                    "user" -> {
                        if (entry.isSidechain) continue
                        if (!entry.isMeta) {
                            userCount++
                            if (firstPrompt == null) {
                                firstPrompt = extractTextFromContent(entry)
                            }
                        }
                        if (startTime == null && ts != null) startTime = ts
                    }
                    "assistant" -> {
                        if (entry.isSidechain) continue
                        assistantCount++
                        if (model == null) {
                            model = entry.message?.model
                        }
                    }
                }

                if (ts != null) endTime = ts
            }

            // メッセージが1件もなければスキップ
            if (userCount == 0 && assistantCount == 0) return null

            val durationMinutes = if (startTime != null && endTime != null) {
                Duration.between(startTime, endTime).toMinutes().toInt()
            } else {
                null
            }

            SessionSummary(
                sessionId = sessionId,
                projectPath = normalizedProjectPath,
                firstPrompt = firstPrompt,
                userMessageCount = userCount,
                assistantMessageCount = assistantCount,
                startTime = startTime,
                durationMinutes = durationMinutes,
                model = model,
            )
        } catch (e: Exception) {
            println("Failed to parse session JSONL: $file, ${e.message}")
            null
        }
    }

    private fun extractTextFromContent(entry: RawConversationEntry): String? {
        val content = entry.message?.content ?: return null

        return when {
            // String 形式
            runCatching { content.jsonPrimitive }.isSuccess ->
                content.jsonPrimitive.contentOrNull

            // Array 形式: text ブロックを結合
            runCatching { content.jsonArray }.isSuccess ->
                content.jsonArray
                    .map { json.decodeFromString<RawContentBlock>(it.toString()) }
                    .filter { it.type == "text" }
                    .joinToString("\n") { it.text.orEmpty() }
                    .ifBlank { null }

            else -> null
        }
    }

    private fun parseInstant(value: String): Instant? {
        return try {
            Instant.parse(value)
        } catch (_: Exception) {
            // epoch millis as string
            value.toLongOrNull()?.let { Instant.ofEpochMilli(it) }
        }
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
        if (entry.isMeta) return null

        val text = extractTextFromContent(entry)
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
