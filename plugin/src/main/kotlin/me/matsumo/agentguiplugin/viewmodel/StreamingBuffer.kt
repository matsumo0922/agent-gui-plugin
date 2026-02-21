package me.matsumo.agentguiplugin.viewmodel

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import me.matsumo.agentguiplugin.viewmodel.mapper.BlockType
import me.matsumo.agentguiplugin.viewmodel.mapper.DeltaType

class StreamingBuffer {

    private val blocks = mutableMapOf<Int, BlockBuffer>()

    fun startBlock(index: Int, blockType: BlockType, toolName: String? = null, toolUseId: String? = null) {
        blocks[index] = BlockBuffer(
            blockType = blockType,
            toolName = toolName,
            toolUseId = toolUseId,
        )
    }

    fun appendDelta(index: Int, deltaType: DeltaType, text: String) {
        val buffer = blocks.getOrPut(index) {
            BlockBuffer(blockType = deltaType.blockType)
        }
        buffer.content.append(text)
    }

    fun stopBlock(index: Int) {
        val buffer = blocks[index] ?: return
        if (buffer.blockType == BlockType.ToolUse) {
            buffer.parsedInput = runCatching {
                Json.decodeFromString<JsonObject>(buffer.content.toString())
            }.getOrDefault(EMPTY_JSON)
        }
    }

    fun toUiBlocks(): List<UiContentBlock> =
        blocks.entries.sortedBy { it.key }.map { (_, buffer) ->
            when (buffer.blockType) {
                BlockType.Thinking -> UiContentBlock.Thinking(
                    text = buffer.content.toString(),
                )

                BlockType.ToolUse -> UiContentBlock.ToolUse(
                    toolName = buffer.toolName ?: DEFAULT_TOOL_NAME,
                    inputJson = buffer.parsedInput ?: EMPTY_JSON,
                    toolUseId = buffer.toolUseId,
                    isStreaming = buffer.parsedInput == null,
                )

                BlockType.Text -> UiContentBlock.Text(
                    text = buffer.content.toString(),
                )
            }
        }

    fun hasContent(): Boolean = blocks.isNotEmpty()

    private class BlockBuffer(
        val blockType: BlockType,
        val toolName: String? = null,
        val toolUseId: String? = null,
        val content: StringBuilder = StringBuilder(),
        var parsedInput: JsonObject? = null,
    )

    companion object {
        private const val DEFAULT_TOOL_NAME = "unknown"
        private val EMPTY_JSON = JsonObject(emptyMap())
    }
}
