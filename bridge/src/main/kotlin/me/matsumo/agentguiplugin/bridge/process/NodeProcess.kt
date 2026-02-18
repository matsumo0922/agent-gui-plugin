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

            // IntelliJ spawns processes without the user's shell PATH,
            // so the SDK can't find the `claude` executable.
            // Resolve the full PATH from the user's default shell.
            val shellPath = resolveShellPath()
            if (shellPath != null) {
                environment()["PATH"] = shellPath
            }
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

    private fun resolveShellPath(): String? {
        // Try to get full PATH from user's login shell
        val shellPath = try {
            val shell = System.getenv("SHELL") ?: "/bin/zsh"
            val proc = ProcessBuilder(shell, "-l", "-i", "-c", "printf '%s' \"\$PATH\"")
                .redirectErrorStream(false)
                .start()
            val output = proc.inputStream.bufferedReader().readText().trim()
            val exitCode = proc.waitFor()
            if (exitCode == 0 && output.isNotEmpty()) output else null
        } catch (_: Exception) {
            null
        }

        if (shellPath != null) return shellPath

        // Fallback: extend current PATH with common directories
        val currentPath = System.getenv("PATH") ?: "/usr/bin:/bin"
        val extraDirs = listOf(
            "/opt/homebrew/bin",
            "/usr/local/bin",
            System.getProperty("user.home") + "/.nvm/current/bin",
            System.getProperty("user.home") + "/.volta/bin",
            System.getProperty("user.home") + "/.local/bin",
        )
        val pathSet = currentPath.split(":").toMutableSet()
        val additions = extraDirs.filter { dir ->
            dir !in pathSet && java.io.File(dir).isDirectory
        }
        return if (additions.isNotEmpty()) {
            currentPath + ":" + additions.joinToString(":")
        } else {
            currentPath
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
