package me.matsumo.agentguiplugin.bridge.model

/** ブリッジイベントの type 識別子（Node.js → Kotlin） */
object BridgeEventTypes {
    const val READY = "ready"
    const val SESSION_INIT = "session_init"
    const val ASSISTANT_MESSAGE = "assistant_message"
    const val STREAM_MESSAGE_START = "stream_message_start"
    const val STREAM_CONTENT_START = "stream_content_start"
    const val STREAM_CONTENT_DELTA = "stream_content_delta"
    const val STREAM_CONTENT_STOP = "stream_content_stop"
    const val STREAM_MESSAGE_STOP = "stream_message_stop"
    const val TURN_RESULT = "turn_result"
    const val PERMISSION_REQUEST = "permission_request"
    const val TOOL_PROGRESS = "tool_progress"
    const val STATUS = "status"
    const val ERROR = "error"
}

/** コンテンツブロックの type 識別子 */
object ContentBlockTypes {
    const val TEXT = "text"
    const val THINKING = "thinking"
    const val TOOL_USE = "tool_use"
}

/** ブリッジコマンドの type 識別子（Kotlin → Node.js） */
object BridgeCommandTypes {
    const val START = "start"
    const val USER_MESSAGE = "user_message"
    const val PERMISSION_RESPONSE = "permission_response"
    const val ABORT = "abort"
}
