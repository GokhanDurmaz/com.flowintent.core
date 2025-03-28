package com.flowintent.core

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.MutableSharedFlow

data class BundleData(val key: String, val value: Any?)

class FlowIntent(
    private val context: Context,
    private val target: Class<*>,
    private val viewModel: FlowIntentViewModel
) {
    private val intent = Intent(context, target)
    private val flowId: String
    private val dataFlow: MutableSharedFlow<BundleData>

    init {
        val flowPair = viewModel.createFlow()
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

    fun start() {
        intent.putExtra("flowId", flowId)
        context.startActivity(intent)
    }

    companion object {
        fun getFlowId(intent: Intent): String? = intent.getStringExtra("flowId")
    }
}