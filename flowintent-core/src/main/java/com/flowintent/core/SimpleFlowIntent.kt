package com.flowintent.core

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import com.flowintent.core.model.FlowCleanupPolicy
import com.flowintent.core.vm.FlowIntentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SimpleFlowIntent(
    context: Context,
    target: Class<*>,
    viewModel: FlowIntentViewModel,
    cleanupPolicy: FlowCleanupPolicy = FlowCleanupPolicy.CLEANUP_PREVIOUS,
    scope: CoroutineScope
) : FlowIntent(context, target, viewModel, cleanupPolicy, scope) {

    fun withData(key: String, value: Any?): SimpleFlowIntent {
        putExtra(key, value)
        return this
    }

    // Dinamik veri ekleme
    fun <T> withDynamicData(key: String, value: T): SimpleFlowIntent {
        scope.launch {
            emitData(key, value)
        }
        return this
    }

    override fun start() {
        super.start()
        Log.d("SimpleFlowIntent", "Aktivite Başlatıldı, Flow ID: ${getFlowId(intent)}")
    }

    // Dinamik verileri alma (FlowIntent’in dataFlow’unu döndürür)
    fun getDynamicData(): Flow<BundleData>? {
        val flowId = getFlowId((context as AppCompatActivity).intent)
        Log.d("SimpleFlowIntent", "Getting Flow ID: $flowId")
        return flowId?.let { viewModel.getFlow(it) }
    }

    inline fun <reified T> getTypedDynamicData(key: String): Flow<T?>? {
        return getDynamicData()?.map { bundleData ->
            if (bundleData.key == key && bundleData.value is T) {
                bundleData.value as T
            } else {
                null
            }
        }
    }

    fun getFlowId(): String? {
        val flowId = getFlowId((context as AppCompatActivity).intent)
        if (flowId == null) {
            Log.w("SimpleFlowIntent", "getFlowId() start() öncesi null dönecek")
        }
        return flowId
    }

    companion object {
        fun from(activity: AppCompatActivity, target: Class<*>): SimpleFlowIntent {
            val viewModel = getSharedViewModel(activity)
            return SimpleFlowIntent(
                context = activity,
                target = target,
                viewModel = viewModel,
                cleanupPolicy = FlowCleanupPolicy.KEEP_PREVIOUS,
                scope = activity.lifecycleScope
            )
        }

        fun current(activity: AppCompatActivity): SimpleFlowIntent {
            val viewModel = getSharedViewModel(activity)
            return SimpleFlowIntent(
                context = activity,
                target = AppCompatActivity::class.java,
                viewModel = viewModel,
                cleanupPolicy = FlowCleanupPolicy.KEEP_PREVIOUS,
                scope = activity.lifecycleScope
            )
        }

        private fun getSharedViewModel(context: Context): FlowIntentViewModel {
            val application = context.applicationContext as Application
            return ViewModelProvider(
                ApplicationViewModelStoreOwner(application),
                ViewModelProvider.NewInstanceFactory()
            )[FlowIntentViewModel::class.java]
        }
    }
}

private class ApplicationViewModelStoreOwner(val application: Application) : ViewModelStoreOwner {
    override val viewModelStore: ViewModelStore = ViewModelStore()
}