package me.matsumo.agentguiplugin.viewmodel.transcript

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.matsumo.agentguiplugin.viewmodel.ChatMessage
import java.io.File
import java.io.RandomAccessFile

/**
 * Tails a JSONL transcript file in real-time, emitting parsed [ChatMessage.Assistant]
 * objects as new lines are appended by the sub-agent process.
 *
 * Uses [RandomAccessFile] with position tracking and periodic polling.
 */
internal class TranscriptTailer(
    private val scope: CoroutineScope,
    private val pollIntervalMs: Long = 300L,
) {
    private var job: Job? = null

    /**
     * Start tailing the given JSONL file. Calls [onMessage] for each new
     * assistant message parsed from newly appended lines.
     *
     * If the file does not exist yet, the tailer will wait for it to appear.
     *
     * @param filePath Absolute path to the JSONL file.
     * @param onMessage Callback invoked on the coroutine's dispatcher for each new message.
     */
    fun start(filePath: String, onMessage: (ChatMessage.Assistant) -> Unit) {
        stop()

        job = scope.launch {
            val file = File(filePath)

            // Wait for the file to appear (sub-agent may not have written yet)
            while (isActive && !file.exists()) {
                delay(pollIntervalMs)
            }

            if (!isActive) return@launch

            var position = 0L

            while (isActive) {
                val currentLength = file.length()

                if (currentLength > position) {
                    try {
                        RandomAccessFile(file, "r").use { raf ->
                            raf.seek(position)
                            val buffer = ByteArray((currentLength - position).toInt())
                            raf.readFully(buffer)
                            position = currentLength

                            val chunk = buffer.toString(Charsets.UTF_8)
                            val lines = chunk.split('\n')

                            for (line in lines) {
                                val message = TranscriptParser.parseLine(line) ?: continue
                                onMessage(message)
                            }
                        }
                    } catch (_: Exception) {
                        // File might be in the middle of a write; retry next poll
                    }
                }

                delay(pollIntervalMs)
            }
        }
    }

    /**
     * Stop tailing and cancel the polling coroutine.
     */
    fun stop() {
        job?.cancel()
        job = null
    }

    val isRunning: Boolean get() = job?.isActive == true
}
