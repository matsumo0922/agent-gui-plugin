package me.matsumo.agentguiplugin.bridge.js.external

external val process: NodeProcess

external interface NodeProcess {
    val stdin: dynamic
    val stdout: dynamic
    val stderr: dynamic
    fun exit(code: Int)
    fun on(event: String, listener: (dynamic) -> Unit)
}

@JsModule("node:readline")
@JsNonModule
external object Readline {
    fun createInterface(options: dynamic): ReadlineInterface
}

external interface ReadlineInterface {
    fun on(event: String, listener: (String) -> Unit): ReadlineInterface
    fun off(event: String, listener: (String) -> Unit): ReadlineInterface
    fun close()
}

// AbortController is available globally in Node.js >= 15
external class AbortController {
    val signal: dynamic
    fun abort()
}
