package me.matsumo.agentguiplugin.viewmodel.transcript

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.matsumo.agentguiplugin.viewmodel.ChatMessage
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY
import java.util.concurrent.TimeUnit

internal class TranscriptTailer(
    private val scope: CoroutineScope,
    private val fileReader: FileLineReader = DefaultFileLineReader(),
) {
    private var job: Job? = null

    fun start(filePath: String, onMessage: (ChatMessage) -> Unit) {
        stop()

        val path = Path.of(filePath)
        val dir = path.parent
        val fileName = path.fileName

        job = scope.launch(Dispatchers.IO) {
            if (!awaitDirExists(dir)) return@launch

            dir.fileSystem.newWatchService().use { watcher ->
                dir.register(watcher, ENTRY_CREATE, ENTRY_MODIFY)

                var pos = if (fileReader.exists(filePath)) readNewLines(filePath, 0L, onMessage) else 0L

                while (isActive) {
                    val key = watcher.poll(1, TimeUnit.SECONDS) ?: continue
                    val touched = key.pollEvents().any { (it.context() as? Path) == fileName }

                    if (touched && fileReader.exists(filePath)) {
                        pos = readNewLines(filePath, pos, onMessage)
                    }
                    if (!key.reset()) return@use
                }
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    private suspend fun CoroutineScope.awaitDirExists(dir: Path): Boolean {
        while (isActive && !Files.exists(dir)) delay(500)
        return isActive
    }

    private fun readNewLines(
        filePath: String,
        fromPosition: Long,
        onMessage: (ChatMessage) -> Unit,
    ): Long {
        val size = fileReader.size(filePath)
        if (size <= fromPosition) return fromPosition

        return runCatching {
            val buffer = fileReader.readBytes(filePath, fromPosition)

            val raw = buffer.toString(Charsets.UTF_8)
            val lines = raw.split('\n')
            // 末尾が改行で終わっていない場合、最後の行は不完全（書込中）なので消費しない
            val endsWithNewline = raw.endsWith('\n')
            val completeLines = if (endsWithNewline) lines else lines.dropLast(1)

            completeLines.forEach { line ->
                TranscriptParser.parseLine(line)?.let(onMessage)
            }

            // 完全行分のバイト数だけ pos を進める
            val bytesConsumed = completeLines.sumOf { it.toByteArray(Charsets.UTF_8).size + 1 }
            fromPosition + bytesConsumed
        }.fold(
            onSuccess = { it },
            onFailure = { fromPosition }
        )
    }
}
