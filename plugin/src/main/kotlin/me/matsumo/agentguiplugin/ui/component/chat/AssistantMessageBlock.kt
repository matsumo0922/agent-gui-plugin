package me.matsumo.agentguiplugin.ui.component.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intellij.diff.DiffContentFactory
import com.intellij.diff.DiffManager
import com.intellij.diff.requests.SimpleDiffRequest
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.testFramework.LightVirtualFile
import me.matsumo.agentguiplugin.ui.component.CodeBlock
import me.matsumo.agentguiplugin.ui.component.DiffLine
import me.matsumo.agentguiplugin.ui.component.MarkdownText
import me.matsumo.agentguiplugin.ui.component.computeDiffLines
import me.matsumo.agentguiplugin.ui.theme.ChatTheme
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
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        blocks.forEach { block ->
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
                    )
                }
                is UiContentBlock.Thinking -> {
                    ThinkingBlock(
                        modifier = Modifier.fillMaxWidth(),
                        text = block.text,
                    )
                }
                is UiContentBlock.ToolUse -> {
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
    }
}

@Composable
private fun AssistantMessageFooter(
    timestamp: Long,
    blocks: List<UiContentBlock>,
    project: Project,
    modifier: Modifier = Modifier,
) {
    val formattedTime = remember(timestamp) {
        if (timestamp > 0L) {
            SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
        } else {
            ""
        }
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
            text = formattedTime,
            fontSize = 11.sp,
            color = ChatTheme.Text.secondary,
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
                    openDiffInIde(project = project, diffInfo = diffInfo, fileName = fileName)
                },
            )
        }
    }
}

private fun openDiffInIde(project: Project, diffInfo: EditDiffInfo, fileName: String) {
    val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(diffInfo.filePath)
    val currentText = virtualFile?.let {
        ApplicationManager.getApplication().runReadAction<String> { VfsUtil.loadText(it) }
    } ?: ""

    val factory = DiffContentFactory.getInstance()
    val left: com.intellij.diff.contents.DiffContent
    val right: com.intellij.diff.contents.DiffContent

    if (diffInfo.oldString.isNotEmpty() && currentText.contains(diffInfo.oldString)) {
        // ファイルがまだ編集前 → 実ファイルと適用後の差分を表示
        val newText = currentText.replaceFirst(diffInfo.oldString, diffInfo.newString)
        left = if (virtualFile != null) factory.create(project, virtualFile)
               else factory.create(currentText)
        right = if (virtualFile != null) factory.create(project, newText, virtualFile.fileType)
                else factory.create(newText)
    } else {
        // ファイルが既に編集済み or ファイルなし → oldString vs newString を直接比較
        left = if (virtualFile != null) factory.create(project, diffInfo.oldString, virtualFile.fileType)
               else factory.create(diffInfo.oldString)
        right = if (virtualFile != null) factory.create(project, diffInfo.newString, virtualFile.fileType)
                else factory.create(diffInfo.newString)
    }

    val request = SimpleDiffRequest("Edit: $fileName", left, right, "変更前", "変更後")
    ApplicationManager.getApplication().invokeLater {
        DiffManager.getInstance().showDiff(project, request)
    }
}
