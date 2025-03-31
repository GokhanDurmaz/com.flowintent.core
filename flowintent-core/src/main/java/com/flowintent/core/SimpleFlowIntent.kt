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

/**
 * A subclass of FlowIntent that provides a simpler interface for managing static and dynamic data
 * between activities, with instance tracking and lifecycle-aware operations.
 */
class SimpleFlowIntent(
    context: Context,
    target: Class<*>,
    viewModel: FlowIntentViewModel,
    cleanupPolicy: FlowCleanupPolicy = FlowCleanupPolicy.CLEANUP_PREVIOUS,
    scope: CoroutineScope
) : FlowIntent(context, target, viewModel, cleanupPolicy, scope) {

    /**
     * Adds static data to the intent as an extra.
     * @param key The key for the data
     * @param value The value to be added (converted to String)
     * @return This instance for method chaining
     */
    fun withData(key: String, value: Any?): SimpleFlowIntent {
        putExtra(key, value)
        return this
    }

    /**
     * Emits dynamic data asynchronously through the FlowIntent's data flow.
     * @param key The key for the dynamic data
     * @param value The value to emit
     * @return This instance for method chaining
     */
    fun <T> withDynamicData(key: String, value: T): SimpleFlowIntent {
        scope.launch {
            emitData(key, value)
        }
        return this
    }

    /**
     * Overrides the start method to launch the target activity and register the instance.
     */
    override fun start() {
        super.start()
        Log.d("SimpleFlowIntent", "Started activity, Flow ID: $flowId")
        instanceMap[flowId] = this
    }

    /**
     * Retrieves the dynamic data flow associated with this instance’s flowId.
     * @return A Flow of BundleData, or null if not available
     */
    fun getDynamicData(): Flow<BundleData?>? {
        val intentFlowId = getFlowId((context as Activity).intent)
        Log.d("SimpleFlowIntent", "Getting Flow ID from Intent: $intentFlowId, Instance Flow ID: $flowId")
        return viewModel.getFlow(flowId)
    }

    /**
     * Retrieves a typed flow of dynamic data filtered by the specified key.
     * @param key The key to filter the data by
     * @return A Flow of the specified type T, or null if not available
     */
    inline fun <reified T> getTypedDynamicData(key: String): Flow<T?>? {
        return getDynamicData()?.map { bundleData ->
            if (bundleData?.key == key && bundleData.value is T) {
                bundleData.value as T
            } else {
                null
            }
        }
    }

    /**
     * Fetches the flowId, prioritizing the intent’s flowId if available.
     * @return The flowId from the intent or the instance’s flowId as a fallback
     */
    fun fetchFlowId(): String {
        val intentFlowId = getFlowId((context as Activity).intent)
        Log.d("SimpleFlowIntent", "Fetching Flow ID - Intent: $intentFlowId, Instance: $flowId")
        return intentFlowId ?: flowId
    }

    companion object {
        private val instanceMap = mutableMapOf<String, SimpleFlowIntent>()

        /**
         * Creates a new SimpleFlowIntent instance from an activity.
         * @param activity The source activity
         * @param target The target activity class
         * @return A new SimpleFlowIntent instance
         */
        fun from(activity: AppCompatActivity, target: Class<*>): SimpleFlowIntent {
            val viewModel = getSharedViewModel(activity)
            val instance = SimpleFlowIntent(
                context = activity,
                target = target,
                viewModel = viewModel,
                cleanupPolicy = FlowCleanupPolicy.KEEP_PREVIOUS,
                scope = activity.lifecycleScope
            )
            Log.d("SimpleFlowIntent", "Created with SimpleFlowIntent.from(), Target: " +
                    "$target, Flow ID: ${instance.flowId}")
            return instance
        }

        /**
         * Retrieves an existing SimpleFlowIntent instance based on the intent’s flowId.
         * @param activity The activity containing the intent
         * @return The existing SimpleFlowIntent instance
         * @throws IllegalStateException if no instance is found for the flowId
         */
        fun current(activity: Activity): SimpleFlowIntent {
            val flowId = getFlowId(activity.intent)

            if (flowId != null && instanceMap.containsKey(flowId)) {
                return instanceMap[flowId]!!
            } else {
                throw IllegalStateException("No SimpleFlowIntent instance found for flowId: " +
                        "$flowId. Ensure from() was called and start() was executed.")
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