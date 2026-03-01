package me.matsumo.agentguiplugin.ui.util

import com.intellij.diff.DiffContentFactory
import com.intellij.diff.DiffManager
import com.intellij.diff.requests.SimpleDiffRequest
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.testFramework.LightVirtualFile
import me.matsumo.agentguiplugin.viewmodel.EditDiffInfo

fun openDiffInIde(project: Project, diffInfo: EditDiffInfo, fileName: String) {
    val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(diffInfo.filePath)
    val currentText = virtualFile?.let {
        ApplicationManager.getApplication().runReadAction<String> { VfsUtil.loadText(it) }
    } ?: ""

    val factory = DiffContentFactory.getInstance()
    val left: com.intellij.diff.contents.DiffContent
    val right: com.intellij.diff.contents.DiffContent

    if (diffInfo.oldString.isNotEmpty() && currentText.contains(diffInfo.oldString)) {
        val newText = currentText.replaceFirst(diffInfo.oldString, diffInfo.newString)
        left = if (virtualFile != null) factory.create(project, virtualFile)
        else factory.create(currentText)
        right = if (virtualFile != null) factory.create(project, newText, virtualFile.fileType)
        else factory.create(newText)
    } else {
        left = if (virtualFile != null) factory.create(project, diffInfo.oldString, virtualFile.fileType)
        else factory.create(diffInfo.oldString)
        right = if (virtualFile != null) factory.create(project, diffInfo.newString, virtualFile.fileType)
        else factory.create(diffInfo.newString)
    }

    val request = SimpleDiffRequest("Edit: $fileName", left, right, "Before", "After")
    ApplicationManager.getApplication().invokeLater {
        DiffManager.getInstance().showDiff(project, request)
    }
}

fun openFilePreviewInIde(project: Project, filePath: String, content: String) {
    ApplicationManager.getApplication().invokeLater {
        val fileName = filePath.substringAfterLast('/')
        val virtualFile = LightVirtualFile(fileName, content)
        FileEditorManager.getInstance(project).openFile(virtualFile, true)
    }
}
