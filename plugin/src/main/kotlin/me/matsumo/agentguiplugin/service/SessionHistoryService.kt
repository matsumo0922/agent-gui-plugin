package me.matsumo.agentguiplugin.service

import androidx.compose.runtime.Immutable
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
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
import java.time.Instant
import java.util.*
import kotlin.io.path.extension
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.name

@Service(Service.Level.PROJECT)
class SessionHistoryService(private val project: Project) {

    @Immutable
    data class SessionSummary(
        val sessionId: String,
        val firstPrompt: String?,
        val startTime: Instant?,
        val lastModifiedAt: Instant?,
        val model: String?,
    )

    companion object {
        private val claudeDir: Path = Path.of(System.getProperty("user.home"), ".claude")
        private val json = Json { ignoreUnknownKeys = true }

        /** セッション一覧抽出時に先頭から読む最大行数 */
        private const val HEAD_LINE_LIMIT = 50

        /** セッション一覧構築時にスキップするエントリタイプ */
        private val SKIP_TYPES = setOf(
            "summary", "x-error", "file-history-snapshot",
            "queue-operation", "custom-title", "agent-name", "progress",
        )

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

    /**
     * プロジェクトに紐づくセッション一覧を JSONL ファイルから直接取得する。
     * 各 JSONL の先頭数行だけ読み、firstPrompt / startTime / model を抽出する。
     */
    suspend fun listSessions(): List<SessionSummary> = withContext(Dispatchers.IO) {
        val projectPath = project.basePath ?: return@withContext emptyList()
        val normalizedPath = normalizeProjectPath(projectPath)
        val encodedPath = encodeClaudeProjectPath(normalizedPath)
        val projectDir = claudeDir.resolve("projects").resolve(encodedPath)

        if (!Files.isDirectory(projectDir)) return@withContext emptyList()

        Files.list(projectDir).use { stream ->
            stream
                .filter { Files.isRegularFile(it) }
                .filter { it.extension == "jsonl" }
                .filter { !it.name.startsWith("agent-") }
                .toList()
                .mapNotNull { file -> extractSummaryFromHead(file) }
                .sortedByDescending { it.lastModifiedAt }
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

    // --- Private: セッション一覧用の軽量パース ---

    private fun extractSummaryFromHead(file: Path): SessionSummary? {
        val sessionId = file.name.removeSuffix(".jsonl")
        val lastModifiedAt = try {
            file.getLastModifiedTime().toInstant()
        } catch (_: Exception) {
            null
        }

        var firstPrompt: String? = null
        var startTime: Instant? = null
        var model: String? = null

        try {
            Files.newBufferedReader(file).use { reader ->
                var linesRead = 0

                while (linesRead < HEAD_LINE_LIMIT) {
                    val line = reader.readLine() ?: break
                    linesRead++

                    if (line.isBlank()) continue

                    val entry = try {
                        json.decodeFromString<RawConversationEntry>(line)
                    } catch (_: Exception) {
                        continue
                    }

                    // スキップ対象の type
                    if (entry.type in SKIP_TYPES) continue
                    // サイドチェーン（サブエージェント）はスキップ
                    if (entry.isSidechain) continue

                    // startTime: 最初の user/assistant エントリの timestamp を採用
                    if (startTime == null && entry.timestamp != null) {
                        startTime = parseTimestamp(entry.timestamp)
                    }

                    // firstPrompt: 最初の意味のあるユーザーメッセージ
                    if (firstPrompt == null && entry.type in setOf("user", "human") && !entry.isMeta) {
                        firstPrompt = extractFirstPrompt(entry)
                    }

                    // model: 最初の assistant エントリの message.model
                    if (model == null && entry.type == "assistant") {
                        model = entry.message?.model
                    }

                    // 必要な情報が全部揃ったら早期終了
                    if (firstPrompt != null && startTime != null && model != null) break
                }
            }
        } catch (e: Exception) {
            println("Failed to read session head: $file, ${e.message}")
        }

        // firstPrompt が取得できないセッションはスキップ
        if (firstPrompt == null) return null

        return SessionSummary(
            sessionId = sessionId,
            firstPrompt = firstPrompt,
            startTime = startTime,
            lastModifiedAt = lastModifiedAt,
            model = model,
        )
    }

    /**
     * ユーザーエントリからプロンプトテキストを抽出する。
     * local-command タグを含む場合はスキップ（null を返す）。
     */
    private fun extractFirstPrompt(entry: RawConversationEntry): String? {
        val text = extractUserText(entry) ?: return null

        // ローカルコマンド出力はプロンプトとして扱わない
        if ("<local-command-caveat>" in text) return null

        // コマンド入力の場合はコマンド名を抽出
        val commandMatch = Regex("<command-name>(.*?)</command-name>").find(text)
        if (commandMatch != null) {
            return "/${commandMatch.groupValues[1]}"
        }

        val cleaned = text
            .replace(Regex("<system-reminder>.*?</system-reminder>", RegexOption.DOT_MATCHES_ALL), "")
            .trim()

        if (cleaned.isBlank()) return null

        return if (cleaned.length > 200) cleaned.take(200).trim() + "..." else cleaned
    }

    private fun parseTimestamp(value: String): Instant? {
        return try {
            Instant.parse(value)
        } catch (_: Exception) {
            null
        }
    }

    // --- Private: セッション詳細読み込み ---

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
        val text = extractUserText(entry)
        if (text.isNullOrBlank()) return null

        return ChatMessage.User(
            id = UUID.randomUUID().toString(),
            text = text,
        )
    }

    private fun extractUserText(entry: RawConversationEntry): String? {
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

            else -> null
        }
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
            blocks = blocks,
        )
    }
}
