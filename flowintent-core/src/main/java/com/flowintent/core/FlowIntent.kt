package com.flowintent.core

import android.content.Context
import android.content.Intent
import com.flowintent.core.model.FlowCleanupPolicy
import com.flowintent.core.vm.FlowIntentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

data class BundleData(val key: String, val value: Any?)

class FlowIntent(
    private val context: Context,
    private val target: Class<*>,
    private val viewModel: FlowIntentViewModel,
    private val cleanupPolicy: FlowCleanupPolicy = FlowCleanupPolicy.CLEANUP_PREVIOUS,
    private val scope: CoroutineScope
) {
    private val intent = Intent(context, target)
    private val flowId: String
    private val dataFlow: MutableSharedFlow<BundleData>
    private var emitJob: Job? = null
    private val emitJobs = mutableListOf<Job>()

    init {
        val flowPair = viewModel.createFlow(cleanupPolicy)
        flowId = flowPair.first
        dataFlow = flowPair.second
    }

    fun putExtra(key: String, value: Any?): FlowIntent {
        intent.putExtra(key, value.toString())
        return this
    }

    suspend fun emitData(key: String, value: Any?) {
        dataFlow.emit(BundleData(key, value))
    }

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

    fun start() {
        intent.putExtra("flowId", flowId)
        context.startActivity(intent)
    }

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
        fun getFlowId(intent: Intent): String? = intent.getStringExtra("flowId")
    }
}