package me.matsumo.agentguiplugin.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.OutputStreamWriter

/**
 * 認証フローの管理。
 * Process I/O (stdin/stdout) を ChatViewModel から分離。
 */
class AuthFlowHandler(private val scope: CoroutineScope) {

    data class AuthState(
        val isActive: Boolean = false,
        val outputLines: List<String> = emptyList(),
    )

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private var process: Process? = null
    private var stdout: BufferedReader? = null
    private var stdin: OutputStreamWriter? = null
    private var readerJob: Job? = null

    /** 認証完了時のコールバック */
    var onAuthComplete: (() -> Unit)? = null

    fun startAuth(
        process: Process,
        stdout: BufferedReader,
        stdin: OutputStreamWriter,
        initialOutput: List<String>,
    ) {
        this.process = process
        this.stdout = stdout
        this.stdin = stdin

        _state.update {
            AuthState(
                isActive = true,
                outputLines = initialOutput,
            )
        }

        startOutputReader()
    }

    fun sendInput(text: String) {
        val writer = stdin ?: return
        scope.launch(Dispatchers.IO) {
            try {
                writer.write(text + "\n")
                writer.flush()
            } catch (_: IOException) {
                // Process may have exited
            }
        }
    }

    fun confirmComplete() {
        cleanup()
        _state.update { AuthState() }
        onAuthComplete?.invoke()
    }

    fun cleanup() {
        // プロセス破棄 → ストリーム close を先に行い、readLine() のブロッキングを解除してから
        // ジョブを cancel する。逆順だと readLine() が永久に suspend する。
        process?.destroyForcibly()
        process = null
        runCatching { stdin?.close() }
        runCatching { stdout?.close() }
        stdin = null
        stdout = null
        readerJob?.cancel()
        readerJob = null
    }

    private fun startOutputReader() {
        val reader = stdout ?: return
        readerJob = scope.launch(Dispatchers.IO) {
            try {
                while (isActive) {
                    val line = reader.readLine() ?: break
                    _state.update { it.copy(outputLines = it.outputLines + line) }
                }
            } catch (_: IOException) {
                // Stream closed
            }
            // Process exited — auto-proceed to re-preflight
            confirmComplete()
        }
    }
}
