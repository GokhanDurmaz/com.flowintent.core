package com.flowintent.core

import android.app.Activity
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

class SimpleFlowIntent private constructor(
    private val context: Context,
    private val target: Class<*>? = null,
    private val scope: CoroutineScope
) {
    internal val flowIntent: FlowIntent
    private val viewModel: FlowIntentViewModel = getSharedViewModel(context)
    private val extras = mutableMapOf<String, String>()

    init {
        flowIntent = FlowIntent(
            context = context,
            target = target ?: context::class.java, // Null ise mevcut aktiviteyi kullan
            viewModel = viewModel,
            cleanupPolicy = FlowCleanupPolicy.KEEP_PREVIOUS,
            scope = scope
        )
    }

    fun withData(key: String, value: String): SimpleFlowIntent {
        extras[key] = value
        flowIntent.putExtra(key, value)
        return this
    }

    fun <T> withDynamicData(key: String, value: T): SimpleFlowIntent {
        scope.launch {
            flowIntent.emitData(key, value)
        }
        return this
    }

    fun start() {
        flowIntent.start()
    }

    fun getDynamicData(): Flow<BundleData>? {
        val flowId = FlowIntent.getFlowId((context as Activity).intent)
        Log.d("SimpleFlowIntent", "Getting Flow ID: $flowId")
        return flowId?.let { viewModel.getFlow(it) }
    }

    inline fun <reified T> getTypedDynamicData(key: String): Flow<T?>? {
        return getDynamicData()?.let { flow ->
            flow.map { bundleData ->
                if (bundleData.key == key && bundleData.value is T) {
                    bundleData.value as T
                } else {
                    null
                }
            }
        }
    }

    companion object {
        fun to(activity: AppCompatActivity, target: Class<*>): SimpleFlowIntent {
            return SimpleFlowIntent(activity, target, activity.lifecycleScope)
        }

        fun current(activity: AppCompatActivity): SimpleFlowIntent {
            return SimpleFlowIntent(activity, null, activity.lifecycleScope)
        }

        private fun getSharedViewModel(context: Context): FlowIntentViewModel {
            val application = context.applicationContext as Application
            return ViewModelProvider(
                ApplicationViewModelStoreOwner(application),
                ViewModelProvider.NewInstanceFactory()
            ).get(FlowIntentViewModel::class.java)
        }
    }
}

private class ApplicationViewModelStoreOwner(val application: Application) : ViewModelStoreOwner {
    override val viewModelStore: ViewModelStore = ViewModelStore()
}