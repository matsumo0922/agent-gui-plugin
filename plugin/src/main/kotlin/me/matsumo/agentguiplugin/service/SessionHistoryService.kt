package me.matsumo.agentguiplugin.service

import androidx.compose.runtime.Immutable
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
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
        private val logger = Logger.getInstance(SessionHistoryService::class.java)
        private val claudeDir: Path = Path.of(System.getProperty("user.home"), ".claude")
        private val json = Json { ignoreUnknownKeys = true }

        /**
         * プロジェクトパスを正規化する（symlink 解決）。
         */
        fun normalizeProjectPath(path: String): String {
            return try {
                Path.of(path).toRealPath().toString()
            } catch (_: Exception) {
                path
            }
        }

        /**
         * Claude Code CLI と同じ方式でプロジェクトパスをエンコードする。
         * "/" → "-" に置換。
         */
        fun encodeClaudeProjectPath(normalizedPath: String): String {
            return normalizedPath.replace("/", "-")
        }
    }

    /**
     * 現在のプロジェクトのセッション一覧を取得する。
     * ~/.claude/usage-data/session-meta/ からメタデータを読み取り、プロジェクトパスでフィルタリング。
     */
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

    /**
     * セッションの会話メッセージを読み取る（UI 表示用）。
     */
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
            val obj = json.parseToJsonElement(text).jsonObject

            val sessionProjectPath = obj["project_path"]?.jsonPrimitive?.contentOrNull ?: return null
            val normalizedSessionPath = normalizeProjectPath(sessionProjectPath)

            if (normalizedSessionPath != normalizedProjectPath) return null

            val sessionId = obj["session_id"]?.jsonPrimitive?.contentOrNull
                ?: file.fileName.toString().removeSuffix(".json")

            SessionSummary(
                sessionId = sessionId,
                projectPath = sessionProjectPath,
                firstPrompt = obj["first_prompt"]?.jsonPrimitive?.contentOrNull,
                userMessageCount = obj["user_message_count"]?.jsonPrimitive?.intOrNull ?: 0,
                assistantMessageCount = obj["assistant_message_count"]?.jsonPrimitive?.intOrNull ?: 0,
                startTime = obj["start_time"]?.jsonPrimitive?.longOrNull?.let { Instant.ofEpochMilli(it) }
                    ?: obj["start_time"]?.jsonPrimitive?.contentOrNull?.let {
                        try {
                            Instant.parse(it)
                        } catch (_: Exception) {
                            null
                        }
                    },
                durationMinutes = obj["duration_minutes"]?.jsonPrimitive?.intOrNull,
                model = obj["model"]?.jsonPrimitive?.contentOrNull,
                totalCostUsd = obj["total_cost_usd"]?.jsonPrimitive?.doubleOrNull,
            )
        } catch (e: Exception) {
            logger.debug("Failed to parse session meta: $file", e)
            null
        }
    }

    private fun parseSessionMessages(sessionFile: Path): List<ChatMessage> {
        val messages = mutableListOf<ChatMessage>()

        try {
            Files.readAllLines(sessionFile).forEach { line ->
                if (line.isBlank()) return@forEach
                try {
                    val obj = json.parseToJsonElement(line).jsonObject

                    // Skip sidechain messages
                    if (obj["isSidechain"]?.jsonPrimitive?.contentOrNull == "true") return@forEach

                    val type = obj["type"]?.jsonPrimitive?.contentOrNull ?: return@forEach

                    when (type) {
                        "human", "user" -> {
                            val messageObj = obj["message"]?.jsonObject ?: return@forEach
                            val content = messageObj["content"]
                            val text = when {
                                content?.jsonPrimitive != null -> content.jsonPrimitive.contentOrNull
                                content?.jsonArray != null -> {
                                    content.jsonArray
                                        .filter {
                                            it.jsonObject["type"]?.jsonPrimitive?.contentOrNull == "text"
                                        }
                                        .joinToString("\n") {
                                            it.jsonObject["text"]?.jsonPrimitive?.contentOrNull ?: ""
                                        }
                                }
                                else -> null
                            }
                            if (!text.isNullOrBlank()) {
                                messages.add(
                                    ChatMessage.User(
                                        id = UUID.randomUUID().toString(),
                                        text = text,
                                    ),
                                )
                            }
                        }

                        "assistant" -> {
                            val messageObj = obj["message"]?.jsonObject ?: return@forEach
                            val contentArray = messageObj["content"]?.jsonArray ?: return@forEach
                            val blocks = contentArray.mapNotNull { element ->
                                val blockObj = element.jsonObject
                                when (blockObj["type"]?.jsonPrimitive?.contentOrNull) {
                                    "text" -> {
                                        val text = blockObj["text"]?.jsonPrimitive?.contentOrNull
                                        text?.let { UiContentBlock.Text(it) }
                                    }
                                    "thinking" -> {
                                        val text = blockObj["thinking"]?.jsonPrimitive?.contentOrNull
                                        text?.let { UiContentBlock.Thinking(it) }
                                    }
                                    "tool_use" -> {
                                        val toolName = blockObj["name"]?.jsonPrimitive?.contentOrNull ?: "unknown"
                                        val inputJson = blockObj["input"]?.jsonObject ?: JsonObject(emptyMap())
                                        UiContentBlock.ToolUse(toolName = toolName, inputJson = inputJson)
                                    }
                                    else -> null
                                }
                            }
                            if (blocks.isNotEmpty()) {
                                messages.add(
                                    ChatMessage.Assistant(
                                        id = UUID.randomUUID().toString(),
                                        blocks = blocks,
                                    ),
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    logger.debug("Failed to parse session message line", e)
                }
            }
        } catch (e: Exception) {
            logger.warn("Failed to read session file: $sessionFile", e)
        }

        return messages
    }
}
