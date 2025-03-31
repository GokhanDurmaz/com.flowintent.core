package com.flowintent.core

import android.content.Context
import android.content.Intent
import android.util.Log
import com.flowintent.core.model.FlowCleanupPolicy
import com.flowintent.core.vm.FlowIntentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

/**
 * A data class representing a key-value pair for dynamic data emission.
 * @param key The identifier for the data
 * @param value The value associated with the key, can be any type or null
 */
data class BundleData(val key: String, val value: Any?)

/**
 * Base class for managing intents with static and dynamic data flows between activities.
 * Provides lifecycle-aware data emission and cleanup policies.
 */
open class FlowIntent(
    protected val context: Context,
    private val target: Class<*>,
    protected val viewModel: FlowIntentViewModel,
    private val cleanupPolicy: FlowCleanupPolicy = FlowCleanupPolicy.CLEANUP_PREVIOUS,
    protected val scope: CoroutineScope
) {
    private val intent = Intent(context, target)
    protected val flowId: String
    private val dataFlow: MutableSharedFlow<BundleData?>
    private var emitJob: Job? = null
    private val emitJobs = mutableListOf<Job>()

    /**
     * Initializes the FlowIntent by creating a flow in the ViewModel and setting up the flowId and dataFlow.
     */
    init {
        val flowPair = viewModel.createFlow(cleanupPolicy)
        flowId = flowPair.first
        dataFlow = flowPair.second
        Log.d("FlowIntent", "Initialized - Flow ID: $flowId")
    }

    /**
     * Adds static data to the intent as an extra.
     * @param key The key for the extra
     * @param value The value to add, converted to a String
     * @return This instance for method chaining
     */
    fun putExtra(key: String, value: Any?): FlowIntent {
        intent.putExtra(key, value.toString())
        return this
    }

    /**
     * Emits dynamic data through the dataFlow asynchronously.
     * @param key The key for the data
     * @param value The value to emit
     */
    suspend fun emitData(key: String, value: Any?) {
        dataFlow.emit(BundleData(key, value))
        Log.d("FlowIntent", "Emitted data: $key -> $value, Flow ID: $flowId")
    }

    /**
     * Schedules a coroutine job for emitting data based on the cleanup policy.
     * - CLEANUP_PREVIOUS: Cancels the previous job and starts a new one.
     * - KEEP_PREVIOUS: Adds the job to a list without canceling previous ones.
     * @param emitAction The suspend function defining the emission action
     */
    fun scheduleJob(emitAction: suspend (FlowIntent) -> Unit) {
        if (cleanupPolicy == FlowCleanupPolicy.CLEANUP_PREVIOUS) {
            emitJob?.cancel()
            emitJob = scope.launch {
                emitAction(this@FlowIntent)
            }
        } else { // KEEP_PREVIOUS
            val job = scope.launch {
                emitAction(this@FlowIntent)
            }
            emitJobs.add(job)
        }
    }

    /**
     * Starts the target activity with the intent, including the flowId as an extra.
     * This method is open for subclasses to override.
     */
    open fun start() {
        intent.putExtra("flowId", flowId)
        context.startActivity(intent)
        Log.d("FlowIntent", "Started activity, Flow ID: $flowId")
    }

    /**
     * Stops any ongoing emission jobs based on the cleanup policy.
     * - CLEANUP_PREVIOUS: Cancels the single active job.
     * - KEEP_PREVIOUS: Cancels all jobs in the list.
     */
    fun stop() {
        if (cleanupPolicy == FlowCleanupPolicy.CLEANUP_PREVIOUS) {
            emitJob?.cancel()
            emitJob = null
        } else {
            emitJobs.forEach { it.cancel() }
            emitJobs.clear()
        }
    }

    companion object {
        /**
         * Retrieves the flowId from an intent’s extras.
         * @param intent The intent to extract the flowId from
         * @return The flowId if present, null otherwise
         */
        fun getFlowId(intent: Intent): String? = intent.getStringExtra("flowId")
    }
}