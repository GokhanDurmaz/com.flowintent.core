package com.flowintent.core.deeplink

import org.json.JSONException
import org.json.JSONObject

data class ParamDefinition(
    val name: String,
    val isRequired: Boolean = false,
    val validator: ((Any?) -> Boolean)? = null,
    val errorMessage: String = "Invalid value for $name"
)

class DeepLinkValidator {
    private val definitions = mutableListOf<ParamDefinition>()

    fun param(
        name: String,
        isRequired: Boolean = false,
        validator: ((Any?) -> Boolean)? = null,
        errorMessage: String = "Invalid value for $name"
    ) {
        definitions.add(ParamDefinition(name, isRequired, validator, errorMessage))
    }

    fun regexParam(
        name: String,
        pattern: String,
        isRequired: Boolean = false,
        errorMessage: String = "Value for $name must match pattern $pattern"
    ) {
        param(name, isRequired, { it?.toString()?.matches(Regex(pattern)) == true}, errorMessage)
    }

    fun rangeParam(
        name: String,
        min: Int,
        max: Int,
        isRequired: Boolean = false,
        errorMessage: String = "Value for $name must be between $min and $max"
    ) {
        param(name, isRequired, { it?.toString()?.toIntOrNull().let { value -> value in min..max } }, errorMessage)
    }

    fun emailParam(
        name: String,
        isRequired: Boolean = false,
        errorMessage: String = "Value for $name must be a valid email"
    ) {
        regexParam(name, "^[A-Za-z0-9+_.-]+@(.+)$", isRequired, errorMessage)
    }

    fun jsonParam(
        name: String,
        isRequired: Boolean = false,
        errorMessage: String = "Invalid JSON for $name",
        validator: (JSONObject) -> Boolean
    ) {
        param(name, isRequired, { value ->
            try {
                value?.let { JSONObject(it.toString()).let(validator) } == true
            } catch (e: JSONException) {
                false
            }
        }, errorMessage)
    }

    fun validate(params: DeepLinkParams): Result<DeepLinkParams> {
        val errors = mutableListOf<String>()
        definitions.forEach { def ->
            val value = params.get<String>(def.name)
            if (def.isRequired && !params.containsKey(def.name)) {
                errors.add("$def.name is required")
            } else if (value != null && def.validator?.invoke(value) == false) {
                errors.add(def.errorMessage)
            }
        }
        return if (errors.isEmpty()) {
            Result.success(params)
        } else {
            Result.failure(IllegalArgumentException(errors.joinToString("; ")))
        }
    }
}