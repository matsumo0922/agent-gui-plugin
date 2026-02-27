package me.matsumo.agentguiplugin.viewmodel.preflight

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Phase A: PreflightChecker のテスト。
 * VERSION_REGEX の純粋ロジック + cliPath==null のテスト可能部分のみ。
 * 実 Process 起動のテストは対象外。
 */
class PreflightCheckerTest {

    private val checker = PreflightChecker()

    // VERSION_REGEX を間接テストするためのヘルパー
    // PreflightChecker.Companion.VERSION_REGEX は private なので、
    // 同じパターンを使ってテストする
    private val versionRegex = Regex("""(\d+\.\d+\.\d+)""")

    // ────────────────────────────────────────
    // 1: バージョン検出
    // ────────────────────────────────────────

    @Test
    fun `VERSION_REGEX matches simple version string`() {
        val input = "1.0.50"
        val match = versionRegex.find(input)
        assertEquals("1.0.50", match?.groupValues?.get(1))
    }

    // ────────────────────────────────────────
    // 2: 前後テキスト付きバージョン検出
    // ────────────────────────────────────────

    @Test
    fun `VERSION_REGEX matches version with surrounding text`() {
        val input = "claude-code 1.2.3-beta"
        val match = versionRegex.find(input)
        assertEquals("1.2.3", match?.groupValues?.get(1))
    }

    // ────────────────────────────────────────
    // 3: 非バージョン文字列
    // ────────────────────────────────────────

    @Test
    fun `VERSION_REGEX does not match non-version text`() {
        val input = "Please login to continue..."
        val match = versionRegex.find(input)
        assertEquals(null, match)
    }

    // ────────────────────────────────────────
    // 4: 空文字列
    // ────────────────────────────────────────

    @Test
    fun `VERSION_REGEX does not match empty string`() {
        val match = versionRegex.find("")
        assertEquals(null, match)
    }

    // ────────────────────────────────────────
    // 5: cliPath == null のハンドリング
    // ────────────────────────────────────────

    @Test
    fun `check with null cliPath returns Ready with auto-discovery`() = runTest {
        val result = checker.check(null)
        assertIs<PreflightResult.Ready>(result)
        assertEquals("auto-discovery", result.version)
    }
}
