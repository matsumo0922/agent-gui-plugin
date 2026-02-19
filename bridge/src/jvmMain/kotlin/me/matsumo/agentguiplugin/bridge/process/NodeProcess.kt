package me.matsumo.agentguiplugin.bridge.process

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.File

/**
 * Node.js 子プロセスの起動と stdin/stdout 通信を管理する。
 * IntelliJ のプロセス環境はユーザーの PATH を継承しないため、
 * ログインシェルから PATH を解決する仕組みを持つ。
 */
class NodeProcess(
    private val nodePath: String,
    private val scriptPath: String,
    private val workingDir: String? = null,
) {
    private var process: Process? = null
    private var stdinWriter: BufferedWriter? = null

    val isAlive: Boolean get() = process?.isAlive == true

    /** プロセスを起動し、stdout の行を Flow で返す */
    fun start(): Flow<String> {
        val pb = ProcessBuilder(nodePath, scriptPath).apply {
            redirectErrorStream(false)
            workingDir?.let { directory(File(it)) }

            // ユーザーのログインシェルから PATH を解決して設定
            resolveShellPath()?.let { environment()["PATH"] = it }
        }

        val proc = pb.start()
        process = proc
        stdinWriter = proc.outputStream.bufferedWriter()

        // stderr をログに転送（デーモンスレッド）
        Thread({
            proc.errorStream.bufferedReader().forEachLine { line ->
                System.err.println("$STDERR_LOG_PREFIX $line")
            }
        }, STDERR_THREAD_NAME).apply { isDaemon = true }.start()

        return flow {
            proc.inputStream.bufferedReader().use { reader ->
                while (true) {
                    val line = reader.readLine() ?: break
                    emit(line)
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    /** stdin に 1 行書き込む */
    suspend fun writeLine(json: String) = withContext(Dispatchers.IO) {
        stdinWriter?.let {
            it.write(json)
            it.newLine()
            it.flush()
        }
    }

    /** プロセスと子孫を強制終了 */
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

    /**
     * ユーザーのログインシェルから完全な PATH を取得する。
     * 取得できない場合は、現在の PATH に一般的なディレクトリを追加する。
     */
    private fun resolveShellPath(): String? {
        // ログインシェルから PATH を取得
        val shellPath = try {
            val shell = System.getenv("SHELL") ?: DEFAULT_SHELL
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

        // フォールバック: 現在の PATH に一般的なディレクトリを追加
        val currentPath = System.getenv("PATH") ?: DEFAULT_PATH
        val home = System.getProperty("user.home")
        val extraDirs = listOf(
            "/opt/homebrew/bin",
            "/usr/local/bin",
            "$home/.nvm/current/bin",
            "$home/.volta/bin",
            "$home/.local/bin",
        )
        val pathSet = currentPath.split(":").toSet()
        val additions = extraDirs.filter { it !in pathSet && File(it).isDirectory }

        return if (additions.isNotEmpty()) {
            currentPath + ":" + additions.joinToString(":")
        } else {
            currentPath
        }
    }

    companion object {
        private const val DEFAULT_SHELL = "/bin/zsh"
        private const val DEFAULT_PATH = "/usr/bin:/bin"
        private const val STDERR_LOG_PREFIX = "[bridge-stderr]"
        private const val STDERR_THREAD_NAME = "bridge-stderr-reader"
    }
}
