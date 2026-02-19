package me.matsumo.agentguiplugin.bridge.process

import java.io.File

/**
 * Node.js 実行ファイルを自動検出するリゾルバ。
 * カスタムパス → which → フォールバック候補の順で探索する。
 */
object NodeResolver {

    /** Node.js パスを解決する。見つからなければ null */
    fun resolve(customPath: String? = null): String? {
        // 1. カスタムパス（設定画面で指定）
        if (customPath != null && isValidNode(customPath)) return customPath

        // 2. which node
        val whichResult = runCommand("which", "node")
        if (whichResult != null && isValidNode(whichResult)) return whichResult

        // 3. 一般的なインストール先をフォールバック
        val home = System.getProperty("user.home")
        val candidates = listOf(
            "/usr/local/bin/node",
            "/opt/homebrew/bin/node",
            "/usr/bin/node",
            "$home/.nvm/current/bin/node",
            "$home/.volta/bin/node",
            "$home/.fnm/current/bin/node",
        )
        return candidates.firstOrNull { isValidNode(it) }
    }

    /** パスが存在し、実行可能で、正常にバージョンを返すか確認 */
    private fun isValidNode(path: String): Boolean = try {
        val file = File(path)
        file.exists() && file.canExecute() && ProcessBuilder(path, "--version")
            .redirectErrorStream(true)
            .start()
            .waitFor() == 0
    } catch (_: Exception) {
        false
    }

    /** コマンドを実行し、成功時に stdout を返す */
    private fun runCommand(vararg command: String): String? = try {
        val proc = ProcessBuilder(*command)
            .redirectErrorStream(true)
            .start()
        val output = proc.inputStream.bufferedReader().readText().trim()
        if (proc.waitFor() == 0 && output.isNotEmpty()) output else null
    } catch (_: Exception) {
        null
    }
}
