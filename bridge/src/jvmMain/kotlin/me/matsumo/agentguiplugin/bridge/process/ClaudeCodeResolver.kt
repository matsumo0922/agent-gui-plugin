package me.matsumo.agentguiplugin.bridge.process

import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Claude Code CLI を自動検出するリゾルバ。
 * ユーザーのログインシェル内で `which claude` を実行して探索する。
 */
object ClaudeCodeResolver {

    private const val DEFAULT_SHELL = "/bin/zsh"
    private const val SHELL_TIMEOUT_SECONDS = 5L

    /** Claude Code CLI パスを解決する。見つからなければ null */
    fun resolve(customPath: String? = null): String? {
        // 1. カスタムパス（設定画面で指定）
        if (!customPath.isNullOrBlank() && isValid(customPath)) return customPath

        // 2. ログインシェルで which claude を実行
        val shellWhich = runInLoginShell("which claude")
        if (shellWhich != null && isValid(shellWhich)) return shellWhich

        // 3. 一般的なインストール先をフォールバック
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

    /** パスが存在し、実行可能か確認 */
    private fun isValid(path: String): Boolean = try {
        val file = File(path)
        file.exists() && file.canExecute()
    } catch (_: Exception) {
        false
    }

    /** ユーザーのログインシェルでコマンドを実行し、最初の絶対パス行を返す */
    private fun runInLoginShell(command: String): String? = try {
        val shell = System.getenv("SHELL") ?: DEFAULT_SHELL
        val proc = ProcessBuilder(shell, "-l", "-c", command)
            .redirectErrorStream(false)
            .start()

        val output = proc.inputStream.bufferedReader().readText().trim()
        val completed = proc.waitFor(SHELL_TIMEOUT_SECONDS, TimeUnit.SECONDS)

        if (completed && proc.exitValue() == 0 && output.isNotEmpty()) {
            // which が複数行返す場合があるので、最初の絶対パス行を取る
            output.lines().firstOrNull { it.isNotBlank() && it.startsWith("/") }
        } else {
            null
        }
    } catch (_: Exception) {
        null
    }
}
