package me.matsumo.agentguiplugin.model

import androidx.compose.runtime.Stable
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File
import java.util.*
import javax.swing.Icon

@Stable
data class AttachedFile(
    val id: String,
    val name: String,
    val path: String,
    val icon: Icon?,
    val isImage: Boolean = false,
) {
    fun toImageBlock(): JsonObject = buildJsonObject {
        put("type", "image")
        put("source", buildJsonObject {
            put("type", "base64")
            put("data", Base64.getEncoder().encodeToString(File(path).readBytes()))
            put("media_type", inferMediaType(path))
        })
    }

    fun toDocumentBlock(): JsonObject = buildJsonObject {
        put("type", "document")
        put("source", buildJsonObject {
            put("type", "text")
            put("media_type", "text/plain")
            put("data", File(path).readText())
        })
    }

    private fun inferMediaType(path: String): String {
        val ext = path.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "gif" -> "image/gif"
            "bmp" -> "image/bmp"
            "webp" -> "image/webp"
            "svg" -> "image/svg+xml"
            else -> "application/octet-stream"
        }
    }
}

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
