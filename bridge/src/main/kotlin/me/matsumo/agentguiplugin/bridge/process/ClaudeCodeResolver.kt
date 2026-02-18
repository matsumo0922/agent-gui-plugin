package me.matsumo.agentguiplugin.bridge.process

import java.io.File
import java.util.concurrent.TimeUnit

object ClaudeCodeResolver {

    fun resolve(customPath: String? = null): String? {
        // 1. Custom path from settings
        if (!customPath.isNullOrBlank() && isValid(customPath)) {
            return customPath
        }

        // 2. Run `which claude` inside the user's login shell to get the full PATH
        val shellWhich = runInLoginShell("which claude")
        if (shellWhich != null && isValid(shellWhich)) {
            return shellWhich
        }

        // 3. Common fallback paths
        val home = System.getProperty("user.home")
        val candidates = listOf(
            "/opt/homebrew/bin/claude",
            "/usr/local/bin/claude",
            "$home/.npm-global/bin/claude",
            "$home/.volta/bin/claude",
            "$home/.local/bin/claude",
        )

        return candidates.firstOrNull { isValid(it) }
    }

    private fun isValid(path: String): Boolean {
        return try {
            val file = File(path)
            file.exists() && file.canExecute()
        } catch (_: Exception) {
            false
        }
    }

    private fun runInLoginShell(command: String): String? {
        return try {
            val shell = System.getenv("SHELL") ?: "/bin/zsh"
            val proc = ProcessBuilder(shell, "-l", "-c", command)
                .redirectErrorStream(false)
                .start()

            val output = proc.inputStream.bufferedReader().readText().trim()
            val completed = proc.waitFor(5, TimeUnit.SECONDS)

            if (completed && proc.exitValue() == 0 && output.isNotEmpty()) {
                // which may return multiple lines; take the first non-empty line
                output.lines().firstOrNull { it.isNotBlank() && it.startsWith("/") }
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}
