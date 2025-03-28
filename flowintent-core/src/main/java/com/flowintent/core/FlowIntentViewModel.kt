package com.flowintent.core

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.UUID

class FlowIntentViewModel : ViewModel() {
    private val flows = mutableMapOf<String, MutableSharedFlow<BundleData>>()
    private var currentFlowId: String? = null

    fun createFlow(cleanupPolicy: FlowCleanupPolicy = FlowCleanupPolicy.CLEANUP_PREVIOUS): Pair<String, MutableSharedFlow<BundleData>> {
        if (cleanupPolicy == FlowCleanupPolicy.CLEANUP_PREVIOUS) {
            currentFlowId?.let { flows.remove(it) }
        }
        val id = UUID.randomUUID().toString()
        val flow = MutableSharedFlow<BundleData>()
        flows[id] = flow
        if (cleanupPolicy == FlowCleanupPolicy.CLEANUP_PREVIOUS) {
            currentFlowId = id
        }
        return id to flow
    }

    fun getFlow(id: String): Flow<BundleData>? = flows[id]

    override fun onCleared() {
        flows.clear()
        currentFlowId = null
        super.onCleared()
    }
}