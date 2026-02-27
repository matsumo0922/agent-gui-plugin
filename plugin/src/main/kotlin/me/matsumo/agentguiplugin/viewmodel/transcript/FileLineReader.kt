package me.matsumo.agentguiplugin.viewmodel.transcript

import java.io.RandomAccessFile
import java.nio.file.Files
import java.nio.file.Path

/**
 * ファイルから指定位置以降のバイトを読み取る抽象インターフェース。
 * TranscriptTailer のファイル I/O を抽象化し、テスト時にインメモリ実装で差し替え可能にする。
 */
interface FileLineReader {
    fun exists(filePath: String): Boolean
    fun size(filePath: String): Long
    fun readBytes(filePath: String, fromPosition: Long): ByteArray
}

/** プロダクションコード用: 実際のファイルシステムを読む */
internal class DefaultFileLineReader : FileLineReader {
    override fun exists(filePath: String): Boolean =
        Files.exists(Path.of(filePath))

    override fun size(filePath: String): Long =
        Files.size(Path.of(filePath))

    override fun readBytes(filePath: String, fromPosition: Long): ByteArray {
        return RandomAccessFile(filePath, "r").use { raf ->
            val fileSize = raf.length()
            if (fromPosition >= fileSize) return ByteArray(0)
            raf.seek(fromPosition)
            val buffer = ByteArray((fileSize - fromPosition).toInt())
            raf.readFully(buffer)
            buffer
        }
    }
}
