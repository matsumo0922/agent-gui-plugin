package me.matsumo.agentguiplugin.ui.component.chat

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.testFramework.LightVirtualFile
import kotlinx.coroutines.delay
import me.matsumo.agentguiplugin.ui.component.CodeBlock
import me.matsumo.agentguiplugin.ui.component.DiffLine
import me.matsumo.agentguiplugin.ui.component.MarkdownText
import me.matsumo.agentguiplugin.ui.component.computeDiffLines
import me.matsumo.agentguiplugin.ui.theme.IdeaTheme
import me.matsumo.agentguiplugin.ui.util.openDiffInIde
import me.matsumo.agentguiplugin.viewmodel.EditDiffInfo
import me.matsumo.agentguiplugin.viewmodel.SubAgentTask
import me.matsumo.agentguiplugin.viewmodel.ToolResultInfo
import me.matsumo.agentguiplugin.viewmodel.UiContentBlock
import me.matsumo.agentguiplugin.viewmodel.permission.ToolNames
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.ui.component.IconActionButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import java.awt.datatransfer.StringSelection
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil

private sealed interface DiffPreviewState {
    object Loading : DiffPreviewState
    data class Ready(val lines: List<DiffLine>) : DiffPreviewState
    data class Error(val message: String) : DiffPreviewState
}

@Composable
fun AssistantMessageBlock(
    blocks: List<UiContentBlock>,
    subAgentTasks: Map<String, SubAgentTask>,
    toolResults: Map<String, ToolResultInfo>,
    project: Project,
    modifier: Modifier = Modifier,
    timestamp: Long = 0L,
    isStreaming: Boolean = false,
) {
    val now = rememberTickingNow(isActive = isStreaming)

    // 現セッション由来（startedAt != null）かどうか → 経過時間を表示するかの判定に使う
    val hasTimingInfo = remember(blocks) {
        blocks.any { block ->
            when (block) {
                is UiContentBlock.Thinking -> block.startedAt != null
                is UiContentBlock.ToolUse -> block.startedAt != null
                is UiContentBlock.Text -> false
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        blocks.forEachIndexed { index, block ->
            when (block) {
                is UiContentBlock.Text -> {
                    MarkdownText(
                        modifier = Modifier.fillMaxWidth(),
                        text = block.text,
                    )

                    AssistantMessageFooter(
                        modifier = Modifier.fillMaxWidth(),
                        timestamp = timestamp,
                        blocks = blocks,
                        project = project,
                        showElapsed = hasTimingInfo,
                        now = now,
                    )
                }

                is UiContentBlock.Thinking -> {
                    val elapsed = computeBlockElapsed(block.startedAt, blocks, index, now)
                    ThinkingBlock(
                        modifier = Modifier.fillMaxWidth(),
                        text = block.text,
                        elapsedText = elapsed,
                    )
                }

                is UiContentBlock.ToolUse -> {
                    val elapsed = computeBlockElapsed(block.startedAt, blocks, index, now)
                    val toolResult = if (block.toolName in ToolNames.RESULT_IGNORED_TOOL_NAMES) {
                        null
                    } else {
                        block.toolUseId?.let { toolResults[it] }
                    }

                    ToolUseBlock(
                        modifier = Modifier.fillMaxWidth(),
                        name = block.toolName,
                        inputJson = block.inputJson,
                        resultContent = toolResult?.content,
                        isResultError = toolResult?.isError ?: false,
                        elapsedText = elapsed,
                    )

                    val task = block.toolUseId?.let { subAgentTasks[it] }
                    if (task != null) {
                        SubAgentTaskCard(
                            task = task,
                            subAgentTasks = subAgentTasks,
                            project = project,
                        )
                    }

                    // Edit Tool の場合、diff を表示する
                    val diffInfo = block.diffInfo
                    if (diffInfo != null) {
                        EditDiffBlock(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            diffInfo = diffInfo,
                            project = project,
                        )
                    }
                }
            }
        }

        if (isStreaming) {
            StreamingIndicator()
        }
    }
}

@Composable
private fun AssistantMessageFooter(
    timestamp: Long,
    blocks: List<UiContentBlock>,
    project: Project,
    modifier: Modifier = Modifier,
    showElapsed: Boolean = false,
    now: Long = System.currentTimeMillis(),
) {
    val formattedTime = remember(timestamp) {
        if (timestamp > 0L) {
            SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
        } else {
            ""
        }
    }

    val displayText = if (showElapsed && timestamp > 0L) {
        val elapsedSeconds = ((now - timestamp) / 1000).toInt().coerceAtLeast(0)
        "$formattedTime (${formatElapsed(elapsedSeconds)})"
    } else {
        formattedTime
    }

    val plainText = remember(blocks) {
        blocks.filterIsInstance<UiContentBlock.Text>().joinToString("\n") { it.text }
    }

    Row(
        modifier = modifier.padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = displayText,
            fontSize = 11.sp,
            color = IdeaTheme.colorScheme.onSurfaceVariant,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (plainText.isNotEmpty()) {
                IconActionButton(
                    key = AllIconsKeys.General.OpenInToolWindow,
                    onClick = {
                        openTextInEditor(project, plainText)
                    },
                    contentDescription = "エディターで開く",
                )

                IconActionButton(
                    key = AllIconsKeys.Actions.Copy,
                    onClick = {
                        CopyPasteManager.getInstance()
                            .setContents(StringSelection(plainText))
                    },
                    contentDescription = "出力をコピー",
                )
            }
        }
    }
}

private fun openTextInEditor(project: Project, text: String) {
    ApplicationManager.getApplication().invokeLater {
        val virtualFile = LightVirtualFile("Claude Output.md", text)
        FileEditorManager.getInstance(project).openFile(virtualFile, true)
    }
}

@OptIn(ExperimentalJewelApi::class)
@Composable
private fun EditDiffBlock(
    diffInfo: EditDiffInfo,
    project: Project,
    modifier: Modifier = Modifier,
) {
    val previewState by produceState<DiffPreviewState>(
        initialValue = DiffPreviewState.Loading,
        key1 = diffInfo,
    ) {
        value = runCatching {
            computeDiffLines(diffInfo.oldString, diffInfo.newString)
        }.fold(
            onSuccess = { DiffPreviewState.Ready(it) },
            onFailure = { DiffPreviewState.Error(it.message ?: "diff の計算に失敗しました") },
        )
    }

    val fileName = diffInfo.filePath.substringAfterLast('/')

    when (val state = previewState) {
        is DiffPreviewState.Loading -> {
            // ロード中は何も表示しない（ちらつき防止）
        }

        is DiffPreviewState.Error -> {
            CodeBlock(
                content = "// ${state.message}",
                language = fileName,
                modifier = modifier,
            )
        }

        is DiffPreviewState.Ready -> {
            CodeBlock(
                content = diffInfo.oldString,
                language = fileName,
                modifier = modifier,
                diffLines = state.lines,
                showLineNumbers = true,
                onOpenDiff = {
                    openDiffInIde(
                        project = project,
                        diffInfo = diffInfo,
                        fileName = fileName
                    )
                },
            )
        }
    }
}

@Composable
private fun StreamingIndicator(
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "streaming-dots")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "dots-phase",
    )

    val dots = ".".repeat(ceil(phase).toInt())

    Text(
        modifier = modifier,
        text = dots,
        style = IdeaTheme.typography.bodyLarge,
        color = IdeaTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun rememberTickingNow(isActive: Boolean): Long {
    val now = remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(isActive) {
        if (isActive) {
            while (true) {
                now.longValue = System.currentTimeMillis()
                delay(1000L)
            }
        }
    }

    return now.longValue
}

private fun formatElapsed(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return if (mins > 0) "${mins}m${secs}s" else "${secs}s"
}

/**
 * ブロックの経過時間テキストを計算する。
 * completedAt は次のブロックの startedAt。次がなく streaming 中なら now を使用。
 * startedAt が null（transcript 復元時）なら空文字を返す。
 */
private fun computeBlockElapsed(
    startedAt: Long?,
    blocks: List<UiContentBlock>,
    index: Int,
    now: Long,
): String {
    if (startedAt == null) return ""

    val nextStartedAt = blocks.drop(index + 1).firstNotNullOfOrNull { block ->
        when (block) {
            is UiContentBlock.Thinking -> block.startedAt
            is UiContentBlock.ToolUse -> block.startedAt
            is UiContentBlock.Text -> null
        }
    }

    val endTime = nextStartedAt ?: now
    val elapsedSeconds = ((endTime - startedAt) / 1000).toInt().coerceAtLeast(0)
    return formatElapsed(elapsedSeconds)
}
