package com.flowintent.core.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import com.flowintent.core.BundleData
import com.flowintent.core.model.FlowCleanupPolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.UUID

class FlowIntentViewModel : ViewModel() {
    private val flows = mutableMapOf<String, MutableSharedFlow<BundleData?>>()
    private var currentFlowId: String? = null

    fun createFlow(cleanupPolicy: FlowCleanupPolicy = FlowCleanupPolicy.CLEANUP_PREVIOUS): Pair<String, MutableSharedFlow<BundleData?>> {
        if (cleanupPolicy == FlowCleanupPolicy.CLEANUP_PREVIOUS) {
            currentFlowId?.let { flows.remove(it) }
        }
        val id = UUID.randomUUID().toString()
        val flow = MutableStateFlow<BundleData?>(null)
        flows[id] = flow
        Log.d("FlowIntentViewModel", "Flow Created - ID: $id, Flows: ${flows.keys}")
        if (cleanupPolicy == FlowCleanupPolicy.CLEANUP_PREVIOUS) {
            currentFlowId = id
        }
        return id to flow
    }

    fun getFlow(id: String): Flow<BundleData?>? {
        val flow = flows[id]
        Log.d("FlowIntentViewModel", "Getting Flow - ID: $id, Found: $flow, Flows: ${flows.keys}")
        return flow
    }

    override fun onCleared() {
        flows.clear()
        currentFlowId = null
        Log.d("FlowIntentViewModel", "Flows Cleared")
    }
}