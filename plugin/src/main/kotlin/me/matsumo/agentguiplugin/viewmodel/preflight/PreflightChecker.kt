package me.matsumo.agentguiplugin.viewmodel.preflight

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.concurrent.TimeUnit

sealed interface PreflightResult {
    data class Ready(val version: String) : PreflightResult
    data class AuthRequired(
        val process: Process,
        val stdout: BufferedReader,
        val stdin: OutputStreamWriter,
        val initialOutput: List<String>,
    ) : PreflightResult
    data class Error(val message: String) : PreflightResult
}

class PreflightChecker {

    companion object {
        private val VERSION_REGEX = Regex("""(\d+\.\d+\.\d+)""")
        private const val TIMEOUT_MS = 10_000L
    }

    suspend fun check(cliPath: String?, environment: Map<String, String> = emptyMap()): PreflightResult = withContext(Dispatchers.IO) {
        if (cliPath == null) {
            return@withContext PreflightResult.Ready("auto-discovery")
        }

        val process: Process
        try {
            process = ProcessBuilder(cliPath, "-v")
                .redirectErrorStream(true)
                .also { pb ->
                    if (environment.isNotEmpty()) {
                        pb.environment().putAll(environment)
                    }
                }
                .start()
        } catch (e: IOException) {
            return@withContext PreflightResult.Error("Failed to start CLI: ${e.message}")
        }

        val stdout = BufferedReader(InputStreamReader(process.inputStream))
        val stdin = OutputStreamWriter(process.outputStream)
        val initialOutput = mutableListOf<String>()

        val firstLine = withTimeoutOrNull(TIMEOUT_MS) {
            withContext(Dispatchers.IO) {
                stdout.readLine()
            }
        }

        if (firstLine == null) {
            // Timeout — process is likely waiting for input (auth wrapper)
            // ただしプロセスが既に終了していたらエラーとして扱う
            if (!process.isAlive) {
                val exitCode = process.exitValue()
                process.destroyForcibly()
                return@withContext PreflightResult.Error(
                    "CLI exited unexpectedly (code $exitCode) with no output",
                )
            }
            return@withContext PreflightResult.AuthRequired(
                process = process,
                stdout = stdout,
                stdin = stdin,
                initialOutput = initialOutput,
            )
        }

        initialOutput.add(firstLine)

        if (VERSION_REGEX.containsMatchIn(firstLine)) {
            // Got a version string — CLI is ready
            process.destroyForcibly()
            val version = VERSION_REGEX.find(firstLine)!!.groupValues[1]
            return@withContext PreflightResult.Ready(version)
        }

        // Non-version output — likely an auth prompt
        // Drain any additional immediately-available lines
        while (stdout.ready()) {
            val line = stdout.readLine() ?: break
            initialOutput.add(line)
        }

        // プロセスが既に終了している場合はエラーとして扱う（認証プロンプトではない）
        if (!process.isAlive) {
            val exitCode = process.exitValue()
            val output = initialOutput.joinToString("\n")
            return@withContext PreflightResult.Error(
                "CLI exited with code $exitCode:\n$output",
            )
        }

        return@withContext PreflightResult.AuthRequired(
            process = process,
            stdout = stdout,
            stdin = stdin,
            initialOutput = initialOutput,
        )
    }

    /**
     * ユーザーのログインシェルから PATH を取得する。
     * IntelliJ を Dock から起動した場合、プロセスの PATH が不十分なことがあるため、
     * ラッパー CLI が内部で呼び出すコマンドを解決できるようにする。
     */
    suspend fun resolveShellPath(): Map<String, String> = withContext(Dispatchers.IO) {
        try {
            val shell = System.getenv("SHELL") ?: "/bin/sh"
            val p = ProcessBuilder(shell, "-lc", "echo \$PATH")
                .redirectErrorStream(true)
                .start()
            val path = p.inputStream.bufferedReader().readLine()
            p.waitFor(5, TimeUnit.SECONDS)
            p.destroyForcibly()
            if (!path.isNullOrBlank()) mapOf("PATH" to path) else emptyMap()
        } catch (_: Exception) {
            emptyMap()
        }
    }
}
