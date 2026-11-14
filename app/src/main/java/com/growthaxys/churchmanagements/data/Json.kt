package com.growthaxys.churchmanagements.data

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/** Builds a JSON payload; null values are sent as JSON null so fields can be cleared (same as supabase-js). */
fun jsonOf(vararg pairs: Pair<String, Any?>): JsonObject = JsonObject(pairs.associate { (k, v) -> k to v.toJson() })

private fun Any?.toJson(): JsonElement = when (this) {
    null -> JsonNull
    is JsonElement -> this
    is String -> JsonPrimitive(this)
    is Boolean -> JsonPrimitive(this)
    is Number -> JsonPrimitive(this)
    is Collection<*> -> JsonArray(map { it.toJson() })
    else -> JsonPrimitive(toString())
}

/** Empty string -> null for optional text columns. */
fun String?.orNull(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
