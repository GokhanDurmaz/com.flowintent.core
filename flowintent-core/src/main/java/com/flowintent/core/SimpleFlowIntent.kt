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

class SimpleFlowIntent(
    context: Context,
    target: Class<*>,
    viewModel: FlowIntentViewModel,
    cleanupPolicy: FlowCleanupPolicy = FlowCleanupPolicy.CLEANUP_PREVIOUS,
    scope: CoroutineScope
) : FlowIntent(context, target, viewModel, cleanupPolicy, scope) {

    fun withData(key: String, value: Any?): SimpleFlowIntent {
        putExtra(key, value)
        Log.d("SimpleFlowIntent", "Statik Veri Eklendi: $key -> $value")
        return this
    }

    fun <T> withDynamicData(key: String, value: T): SimpleFlowIntent {
        scope.launch {
            emitData(key, value)
        }
        return this
    }

    override fun start() {
        super.start()
        Log.d("SimpleFlowIntent", "Aktivite Başlatıldı, Flow ID: $flowId")
        instanceMap[flowId] = this // Instance’ı flowId ile kaydet
    }

    fun getDynamicData(): Flow<BundleData?>? {
        val intentFlowId = getFlowId((context as Activity).intent)
        Log.d("SimpleFlowIntent", "Getting Flow ID from Intent: $intentFlowId, Instance Flow ID: $flowId")
        // Intent’ten flowId almak yerine instance’ın flowId’sini kullan
        return viewModel.getFlow(flowId)
    }

    inline fun <reified T> getTypedDynamicData(key: String): Flow<T?>? {
        return getDynamicData()?.map { bundleData ->
            if (bundleData?.key == key && bundleData.value is T) {
                bundleData.value as T
            } else {
                null
            }
        }
    }

    fun fetchFlowId(): String {
        val intentFlowId = getFlowId((context as Activity).intent)
        Log.d("SimpleFlowIntent", "Fetching Flow ID - Intent: $intentFlowId, Instance: $flowId")
        return intentFlowId ?: flowId // Intent’ten yoksa instance’ın flowId’sini döndür
    }

    companion object {
        private val instanceMap = mutableMapOf<String, SimpleFlowIntent>()

        fun from(activity: AppCompatActivity, target: Class<*>): SimpleFlowIntent {
            val viewModel = getSharedViewModel(activity)
            val instance = SimpleFlowIntent(
                context = activity,
                target = target,
                viewModel = viewModel,
                cleanupPolicy = FlowCleanupPolicy.KEEP_PREVIOUS,
                scope = activity.lifecycleScope
            )
            Log.d("SimpleFlowIntent", "SimpleFlowIntent.from() ile oluşturuldu, Target: $target, Flow ID: ${instance.flowId}")
            return instance
        }

        fun current(activity: Activity): SimpleFlowIntent {
            val flowId = getFlowId(activity.intent)
            Log.d("SimpleFlowIntent", "SimpleFlowIntent.current() çağrıldı, Intent’ten Flow ID: $flowId")

            if (flowId != null && instanceMap.containsKey(flowId)) {
                val instance = instanceMap[flowId]!!
                Log.d("SimpleFlowIntent", "Mevcut instance döndürüldü, Flow ID: ${instance.flowId}")
                return instance
            } else {
                Log.e("SimpleFlowIntent", "Instance bulunamadı, Flow ID: $flowId. from() ile oluşturulmuş bir instance gerekli.")
                throw IllegalStateException("No SimpleFlowIntent instance found for flowId: $flowId. Ensure from() was called and start() was executed.")
            }
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