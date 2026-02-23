package me.matsumo.agentguiplugin.viewmodel.transcript

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.matsumo.agentguiplugin.viewmodel.ChatMessage
import java.io.RandomAccessFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY
import java.util.concurrent.TimeUnit

/**
 * Tails a JSONL transcript file using [java.nio.file.WatchService].
 *
 * Watches the parent directory for file creation/modification events
 * and reads newly appended lines when the target file changes.
 */
internal class TranscriptTailer(
    private val scope: CoroutineScope,
) {
    private var job: Job? = null

    fun start(filePath: String, onMessage: (ChatMessage) -> Unit) {
        stop()

        val path = Path.of(filePath)
        val dir = path.parent
        val fileName = path.fileName

        job = scope.launch(Dispatchers.IO) {
            // Wait for parent directory to exist
            while (isActive && !Files.exists(dir)) {
                delay(500)
            }
            if (!isActive) return@launch

            val watcher = dir.fileSystem.newWatchService()

            try {
                dir.register(watcher, ENTRY_CREATE, ENTRY_MODIFY)

                var position = 0L
                if (Files.exists(path)) {
                    position = readNewLines(path, position, onMessage)
                }

                while (isActive) {
                    val key = watcher.poll(1, TimeUnit.SECONDS) ?: continue
                    val relevant = key.pollEvents().any { event ->
                        (event.context() as? Path) == fileName
                    }

                    if (relevant && Files.exists(path)) {
                        position = readNewLines(path, position, onMessage)
                    }

                    if (!key.reset()) break
                }
            } finally {
                watcher.close()
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    private fun readNewLines(
        path: Path,
        fromPosition: Long,
        onMessage: (ChatMessage) -> Unit,
    ): Long {
        val size = Files.size(path)
        if (size <= fromPosition) return fromPosition

        try {
            RandomAccessFile(path.toFile(), "r").use { raf ->
                raf.seek(fromPosition)
                val buffer = ByteArray((size - fromPosition).toInt())
                raf.readFully(buffer)

                buffer.toString(Charsets.UTF_8).split('\n').forEach { line ->
                    TranscriptParser.parseLine(line)?.let(onMessage)
                }
            }
        } catch (_: Exception) {
            return fromPosition
        }

        return size
    }
}
