package me.matsumo.agentguiplugin.model

import com.intellij.openapi.vfs.VirtualFile
import javax.swing.Icon

data class AttachedFile(
    val id: String,
    val name: String,
    val path: String,
    val icon: Icon?,
    val isImage: Boolean = false,
)

private val imageExtensions = setOf("png", "jpg", "jpeg", "gif", "bmp", "svg", "webp")

fun VirtualFile.toAttachedFile(): AttachedFile {
    return AttachedFile(
        id = url,
        name = name,
        path = path,
        icon = fileType.icon,
        isImage = extension?.lowercase() in imageExtensions,
    )
}
