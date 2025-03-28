package com.flowintent.core

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.UUID

class FlowIntentViewModel : ViewModel() {
    private val flows = mutableMapOf<String, MutableSharedFlow<BundleData>>()

    fun createFlow(): Pair<String, MutableSharedFlow<BundleData>> {
        val id = UUID.randomUUID().toString()
        val flow = MutableSharedFlow<BundleData>()
        flows[id] = flow
        return id to flow
    }

    fun getFlow(id: String): Flow<BundleData>? = flows[id]

    override fun onCleared() {
        flows.clear()
        super.onCleared()
    }
}