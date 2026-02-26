package me.matsumo.agentguiplugin.viewmodel.preflight

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter

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

    suspend fun check(cliPath: String?): PreflightResult = withContext(Dispatchers.IO) {
        if (cliPath == null) {
            return@withContext PreflightResult.Ready("auto-discovery")
        }

        val process: Process
        try {
            process = ProcessBuilder(cliPath, "-v")
                .redirectErrorStream(true)
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

        return@withContext PreflightResult.AuthRequired(
            process = process,
            stdout = stdout,
            stdin = stdin,
            initialOutput = initialOutput,
        )
    }
}
