package me.matsumo.agentguiplugin.bridge.process

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.BufferedWriter

class NodeProcess(
    private val nodePath: String,
    private val scriptPath: String,
    private val workingDir: String? = null,
) {
    private var process: Process? = null
    private var stdinWriter: BufferedWriter? = null

    val isAlive: Boolean get() = process?.isAlive == true

    fun start(): Flow<String> {
        val pb = ProcessBuilder(nodePath, scriptPath).apply {
            redirectErrorStream(false)
            workingDir?.let { directory(java.io.File(it)) }
        }

        val proc = pb.start()
        process = proc
        stdinWriter = proc.outputStream.bufferedWriter()

        // Log stderr in background
        Thread({
            proc.errorStream.bufferedReader().forEachLine { line ->
                System.err.println("[bridge-stderr] $line")
            }
        }, "bridge-stderr-reader").apply { isDaemon = true }.start()

        return flow {
            proc.inputStream.bufferedReader().use { reader ->
                while (true) {
                    val line = reader.readLine() ?: break
                    emit(line)
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun writeLine(json: String) = withContext(Dispatchers.IO) {
        stdinWriter?.let {
            it.write(json)
            it.newLine()
            it.flush()
        }
    }

    fun destroy() {
        process?.let { proc ->
            if (proc.isAlive) {
                proc.descendants().forEach { it.destroyForcibly() }
                proc.destroyForcibly()
            }
        }
        process = null
        stdinWriter = null
    }
}
