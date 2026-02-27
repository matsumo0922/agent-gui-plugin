package me.matsumo.agentguiplugin.testutil

/**
 * ファイルから指定位置以降のバイトを読み取る抽象インターフェース。
 * TranscriptTailer のファイル I/O を抽象化し、テスト時にインメモリ実装で差し替え可能にする。
 *
 * NOTE: Phase B（リファクタリング後）で TranscriptTailer に注入される。
 */
interface FileLineReader {
    fun exists(filePath: String): Boolean
    fun size(filePath: String): Long
    fun readBytes(filePath: String, fromPosition: Long): ByteArray
}

/**
 * テスト用 Fake: インメモリでファイル内容を保持し、追記シミュレーションが可能。
 */
class FakeFileLineReader : FileLineReader {
    private val files = mutableMapOf<String, ByteArray>()

    fun setContent(filePath: String, content: String) {
        files[filePath] = content.toByteArray(Charsets.UTF_8)
    }

    fun appendContent(filePath: String, content: String) {
        val existing = files[filePath] ?: ByteArray(0)
        files[filePath] = existing + content.toByteArray(Charsets.UTF_8)
    }

    override fun exists(filePath: String) = filePath in files

    override fun size(filePath: String) = files[filePath]?.size?.toLong() ?: 0L

    override fun readBytes(filePath: String, fromPosition: Long): ByteArray {
        val bytes = files[filePath] ?: return ByteArray(0)
        if (fromPosition >= bytes.size) return ByteArray(0)
        return bytes.copyOfRange(fromPosition.toInt(), bytes.size)
    }
}
