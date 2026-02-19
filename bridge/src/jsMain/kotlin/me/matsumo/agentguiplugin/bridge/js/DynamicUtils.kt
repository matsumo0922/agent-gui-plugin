package me.matsumo.agentguiplugin.bridge.js

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

/**
 * Kotlin/JS の dynamic 値を安全に扱うためのユーティリティ。
 * null と JS の undefined を統一的にハンドリングする。
 */

/** null または JS undefined かを判定 */
internal fun isNullOrUndefined(value: dynamic): Boolean =
    value == null || value === undefined

/** dynamic → String（null/undefined なら default） */
internal fun dynamicString(value: dynamic, default: String = ""): String =
    value as? String ?: default

/** dynamic → String?（null/undefined なら null） */
internal fun dynamicStringOrNull(value: dynamic): String? =
    value as? String

/** dynamic → Int（null/undefined なら default） */
internal fun dynamicInt(value: dynamic, default: Int = 0): Int =
    (value as? Number)?.toInt() ?: default

/** dynamic → Double（null/undefined なら default） */
internal fun dynamicDouble(value: dynamic, default: Double = 0.0): Double =
    (value as? Number)?.toDouble() ?: default

/** dynamic → Boolean（null/undefined なら default） */
internal fun dynamicBool(value: dynamic, default: Boolean = false): Boolean =
    value as? Boolean ?: default

/** JS 配列の length を取得（null/undefined/非配列は 0） */
internal fun dynamicLength(value: dynamic): Int {
    if (isNullOrUndefined(value)) return 0
    return (value.length as? Number)?.toInt() ?: 0
}

/** JS 配列を Kotlin List に変換 */
internal inline fun <T> dynamicToList(arr: dynamic, mapper: (dynamic) -> T): List<T> {
    val length = dynamicLength(arr)
    if (length == 0) return emptyList()
    return List(length) { i -> mapper(arr[i]) }
}

/** dynamic オブジェクトを kotlinx.serialization の JsonObject に変換 */
internal fun dynamicToJsonObject(obj: dynamic): JsonObject {
    if (isNullOrUndefined(obj)) return buildJsonObject {}
    return try {
        val jsonStr = js("JSON.stringify(obj)") as String
        Json.parseToJsonElement(jsonStr) as? JsonObject ?: buildJsonObject {}
    } catch (_: Throwable) {
        buildJsonObject {}
    }
}
