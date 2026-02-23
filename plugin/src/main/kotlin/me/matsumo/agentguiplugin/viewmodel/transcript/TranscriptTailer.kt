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
            if (!awaitDirExists(dir)) return@launch

            dir.fileSystem.newWatchService().use { watcher ->
                dir.register(watcher, ENTRY_CREATE, ENTRY_MODIFY)

                var pos = if (Files.exists(path)) readNewLines(path, 0L, onMessage) else 0L

                while (isActive) {
                    val key = watcher.poll(1, TimeUnit.SECONDS) ?: continue
                    val touched = key.pollEvents().any { (it.context() as? Path) == fileName }

                    if (touched && Files.exists(path)) {
                        pos = readNewLines(path, pos, onMessage)
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
        path: Path,
        fromPosition: Long,
        onMessage: (ChatMessage) -> Unit,
    ): Long {
        val size = Files.size(path)
        if (size <= fromPosition) return fromPosition

        return runCatching {
            RandomAccessFile(path.toFile(), "r").use { raf ->
                raf.seek(fromPosition)
                val buffer = ByteArray((size - fromPosition).toInt())
                raf.readFully(buffer)

                buffer.toString(Charsets.UTF_8).split('\n').forEach { line ->
                    TranscriptParser.parseLine(line)?.let(onMessage)
                }
            }
        }.fold(
            onSuccess = { size },
            onFailure = { fromPosition }
        )
    }
}
