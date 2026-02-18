package me.matsumo.agentguiplugin.bridge.process

import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object BridgeScriptExtractor {

    private const val RESOURCE_PATH = "/bridge/main.mjs"
    private const val EXTRACT_DIR_NAME = "agent-gui-plugin-bridge"

    fun extract(baseDir: String? = null): String {
        val targetDir = File(baseDir ?: System.getProperty("java.io.tmpdir"), EXTRACT_DIR_NAME)
        targetDir.mkdirs()

        val targetFile = File(targetDir, "main.mjs")

        val resourceStream = BridgeScriptExtractor::class.java.getResourceAsStream(RESOURCE_PATH)
            ?: error("Bridge script resource not found at $RESOURCE_PATH")

        resourceStream.use { input ->
            Files.copy(input, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }

        return targetFile.absolutePath
    }
}
