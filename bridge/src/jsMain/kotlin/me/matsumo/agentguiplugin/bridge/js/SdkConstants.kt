package me.matsumo.agentguiplugin.bridge.js

/** SDK query() が返すメッセージの type */
internal object SdkMessageTypes {
    const val SYSTEM = "system"
    const val STREAM_EVENT = "stream_event"
    const val ASSISTANT = "assistant"
    const val TOOL_PROGRESS = "tool_progress"
    const val RESULT = "result"
}

/** SDK system メッセージの subtype */
internal object SdkSystemSubtypes {
    const val INIT = "init"
    const val STATUS = "status"
}

/** SDK ストリームイベントの type */
internal object SdkStreamEventTypes {
    const val MESSAGE_START = "message_start"
    const val CONTENT_BLOCK_START = "content_block_start"
    const val CONTENT_BLOCK_DELTA = "content_block_delta"
    const val CONTENT_BLOCK_STOP = "content_block_stop"
    const val MESSAGE_STOP = "message_stop"
    const val MESSAGE_DELTA = "message_delta"
}

/** SDK コンテンツデルタの type */
internal object SdkDeltaTypes {
    const val TEXT_DELTA = "text_delta"
    const val THINKING_DELTA = "thinking_delta"
    const val INPUT_JSON_DELTA = "input_json_delta"
}

/** パーミッションモード */
internal object PermissionModes {
    const val DEFAULT = "default"
}
