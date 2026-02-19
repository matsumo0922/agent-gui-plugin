package me.matsumo.agentguiplugin.bridge.process

import java.io.File

object NodeResolver {

    fun resolve(customPath: String? = null): String? {
        // 1. Custom path from settings
        if (customPath != null && isValidNode(customPath)) {
            return customPath
        }

        // 2. Try `which node`
        val whichResult = runCommand("which", "node")
        if (whichResult != null && isValidNode(whichResult)) {
            return whichResult
        }

        // 3. Common fallback paths
        val candidates = listOf(
            "/usr/local/bin/node",
            "/opt/homebrew/bin/node",
            "/usr/bin/node",
            System.getProperty("user.home") + "/.nvm/current/bin/node",
            System.getProperty("user.home") + "/.volta/bin/node",
            System.getProperty("user.home") + "/.fnm/current/bin/node",
        )

        return candidates.firstOrNull { isValidNode(it) }
    }

    private fun isValidNode(path: String): Boolean {
        return try {
            val file = File(path)
            if (!file.exists() || !file.canExecute()) return false
            val proc = ProcessBuilder(path, "--version")
                .redirectErrorStream(true)
                .start()
            val exitCode = proc.waitFor()
            exitCode == 0
        } catch (_: Exception) {
            false
        }
    }

    private fun runCommand(vararg command: String): String? {
        return try {
            val proc = ProcessBuilder(*command)
                .redirectErrorStream(true)
                .start()
            val output = proc.inputStream.bufferedReader().readText().trim()
            if (proc.waitFor() == 0 && output.isNotEmpty()) output else null
        } catch (_: Exception) {
            null
        }
    }
}
