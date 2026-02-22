package me.matsumo.agentguiplugin.util

import com.intellij.openapi.application.readAction
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileEditor.impl.EditorHistoryManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import me.matsumo.agentguiplugin.model.AttachedFile
import me.matsumo.agentguiplugin.model.toAttachedFile

object FilePickerUtil {

    fun getRecentFiles(project: Project, limit: Int = 20): List<AttachedFile> {
        return EditorHistoryManager.getInstance(project)
            .fileList
            .filter { it.isValid }
            .take(limit)
            .map { it.toAttachedFile() }
    }

    fun chooseFilesFromOS(project: Project, imageOnly: Boolean, onChosen: (List<VirtualFile>) -> Unit) {
        val descriptor = if (imageOnly) {
            FileChooserDescriptorFactory.singleFile().apply {
                title = "画像を選択"
                withFileFilter { file ->
                    file.extension?.lowercase() in setOf("png", "jpg", "jpeg", "gif", "bmp", "svg", "webp")
                }
            }
        } else {
            FileChooserDescriptorFactory.singleFile().apply {
                title = "ファイルを選択"
            }
        }

        FileChooser.chooseFiles(descriptor, project, null) { files ->
            onChosen(files.filter { it.isValid && !it.isDirectory })
        }
    }

    suspend fun loadAllProjectFilenames(project: Project): List<String> {
        return readAction {
            FilenameIndex.getAllFilenames(project).toList()
        }
    }

    suspend fun resolveFiles(project: Project, names: List<String>): List<AttachedFile> {
        return readAction {
            val scope = GlobalSearchScope.projectScope(project)
            names.flatMap { name ->
                FilenameIndex.getVirtualFilesByName(name, scope)
                    .filter { it.isValid && !it.isDirectory }
                    .map { it.toAttachedFile() }
            }
        }
    }
}
