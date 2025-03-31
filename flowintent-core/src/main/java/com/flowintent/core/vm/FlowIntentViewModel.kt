package com.flowintent.core.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import com.flowintent.core.BundleData
import com.flowintent.core.model.FlowCleanupPolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.UUID

/**
 * A ViewModel responsible for managing flows of dynamic data for FlowIntent instances.
 * It maintains a collection of flows identified by unique IDs and handles cleanup policies.
 */
class FlowIntentViewModel : ViewModel() {
    private val flows = mutableMapOf<String, MutableSharedFlow<BundleData?>>()
    private var currentFlowId: String? = null

    /**
     * Creates a new flow with a unique ID and associates it with the specified cleanup policy.
     * @param cleanupPolicy Determines how previous flows are handled (CLEANUP_PREVIOUS or KEEP_PREVIOUS)
     * @return A pair containing the flow ID and the newly created MutableSharedFlow
     */
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

    /**
     * Retrieves the flow associated with the given ID.
     * @param id The unique ID of the flow to retrieve
     * @return The Flow of BundleData if found, null otherwise
     */
    fun getFlow(id: String): Flow<BundleData?>? {
        val flow = flows[id]
        Log.d("FlowIntentViewModel", "Getting Flow - ID: $id, Found: $flow, Flows: ${flows.keys}")
        return flow
    }

    /**
     * Called when the ViewModel is cleared (e.g., when the associated activity/process is destroyed).
     * Cleans up all flows and resets the currentFlowId.
     */
    override fun onCleared() {
        flows.clear()
        currentFlowId = null
        Log.d("FlowIntentViewModel", "Flows Cleared")
    }
}