package com.flowintent.example

class FlowIntentActionValidator {
    fun invoke(value: Any?): Boolean {
        return value == "view" || value == "edit" || value == "fetch"
    }
}