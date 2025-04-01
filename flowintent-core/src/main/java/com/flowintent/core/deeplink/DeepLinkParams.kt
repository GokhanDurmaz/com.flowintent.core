package com.flowintent.core.deeplink

import org.json.JSONObject

data class DeepLinkParams(
    private val params: MutableMap<String, Any?> = mutableMapOf()
) {
    fun getRawValue(key: String): Any? = params[key]

    inline fun <reified T> get(key: String): T? {
        val value = getRawValue(key)
        return when {
            T::class == String::class -> value as? T
            T::class == Int::class -> (value as? String)?.toIntOrNull() as? T
            T::class == Boolean::class -> (value as? String)?.toBoolean() as? T
            T::class == JSONObject::class -> try {
                value?.let { JSONObject(it.toString()) } as? T
            } catch (e: Exception) {
                null
            }
            else -> throw IllegalArgumentException("Unsupported type for key: $key")
        }
    }

    fun put(key: String, value: Any?) {
        params[key] = value
    }

    fun containsKey(key: String): Boolean = params.containsKey(key)
}