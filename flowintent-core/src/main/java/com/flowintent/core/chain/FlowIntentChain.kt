package com.flowintent.core.chain

import android.app.TaskStackBuilder
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.flowintent.core.annotations.InitialStep
import com.flowintent.core.annotations.OnResult
import com.flowintent.core.annotations.StartActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * A class for chaining activity intents with support for DSL and annotation-based configurations.
 * Manages the sequence of activity launches and their results using a coroutine-based flow.
 */
class FlowIntentChain(
    private val activity: AppCompatActivity,
    private val scope: CoroutineScope
) {
    private val dslSteps = mutableListOf<Step>()
    private val annotationSteps = mutableMapOf<String, Step>()
    private val resultChannel = Channel<ActivityResult>(capacity = Channel.UNLIMITED)
    private val launcher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        scope.launch {
            resultChannel.send(result)
        }
    }

    /**
     * Data class representing a single step in the intent chain.
     * @param intentBuilder Function to build the intent for this step, optionally using the previous result
     * @param onResult Optional callback to handle the result of this step
     * @param nextStep The name of the next step (used in annotation-based chaining)
     */
    data class Step(
        val intentBuilder: (ActivityResult?) -> Intent,
        val onResult: ((ActivityResult) -> Unit)?,
        val nextStep: String = "",
        val parent: Class<*>? = null
    )

    fun startActivityWithParent(
        parent: Class<*>?,
        intentBuilder: (ActivityResult?) -> Intent
    ) {
        dslSteps.add(Step(intentBuilder, null, parent = parent))
    }

    // DSL Methods

    /**
     * Adds a step to start an activity using the provided intent builder (DSL).
     * @param intentBuilder Function to create the intent for this step
     */
    fun startActivity(intentBuilder: (ActivityResult?) -> Intent) {
        dslSteps.add(Step(intentBuilder, null))
    }

    /**
     * Adds a result handler to the last step defined via DSL.
     * @param onResult Callback to handle the activity result
     * @throws IllegalStateException if no prior startActivity step exists
     */
    fun onResult(onResult: (ActivityResult) -> Unit) {
        if (dslSteps.isEmpty()) throw IllegalStateException("Register startActivity first.")
        val lastStep = dslSteps.removeLast()
        dslSteps.add(Step(lastStep.intentBuilder, onResult))
    }

    /**
     * Builds and executes a flow of activity results using the DSL-defined steps.
     * @return A Flow emitting ActivityResult objects as each step completes
     * @throws IllegalStateException if no DSL steps are defined
     */
    fun build(): Flow<ActivityResult> {
        if (dslSteps.isEmpty()) throw IllegalStateException("Any DSL Step not specified.")

        var stepIndex = 0
        var previousResult: ActivityResult? = null

        scope.launch {
            val firstStep = dslSteps[0]
            val firstIntent = firstStep.intentBuilder(null)
            if (firstStep.parent != null) {
                TaskStackBuilder.create(activity)
                    .addParentStack(firstStep.parent)
                    .addNextIntent(firstIntent)
                    .startActivities()
            } else {
                launcher.launch(firstIntent)
            }

            for (result in resultChannel) {
                val currentStep = dslSteps.getOrNull(stepIndex)
                currentStep?.onResult?.invoke(result)
                previousResult = result
                stepIndex++
                val nextStep = dslSteps.getOrNull(stepIndex)
                if (nextStep != null) {
                    val nextIntent = nextStep.intentBuilder(previousResult)
                    if (nextStep.parent != null) {
                        TaskStackBuilder.create(activity)
                            .addParentStack(nextStep.parent)
                            .addNextIntent(nextIntent)
                            .startActivities()
                    } else {
                        launcher.launch(nextIntent)
                    }
                }
            }
        }

        return resultChannel.receiveAsFlow()
    }

    /**
     * Builds and executes a flow of activity results using annotation-defined steps.
     * @return A Flow emitting ActivityResult objects as each step completes
     * @throws IllegalStateException if the initial step or next steps are not found
     */
    fun buildFlowFromAnnotations(): Flow<ActivityResult> {
        val methods = activity::class.java.declaredMethods
        var initialStepName: String? = null

        methods.forEach { method ->
            if (method.isAnnotationPresent(InitialStep::class.java)) {
                val startAnnotation = method.getAnnotation(StartActivity::class.java)
                if (startAnnotation != null) {
                    initialStepName = startAnnotation.stepName
                } else {
                    throw IllegalStateException("Both InitialStep and StartActivity must be used together.")
                }
            }

            if (method.isAnnotationPresent(StartActivity::class.java)) {
                val startAnnotation = method.getAnnotation(StartActivity::class.java)
                val stepName = startAnnotation.stepName
                val parentClass = if (startAnnotation.parent.isNotEmpty()) {
                    try {
                        Class.forName(startAnnotation.parent) as Class<*>
                    } catch (e: ClassNotFoundException) {
                        null
                    }
                } else null
                val intentBuilder: (ActivityResult?) -> Intent = { param ->
                    method.invoke(activity, param) as Intent
                }
                val onResultAnnotation = method.getAnnotation(OnResult::class.java)
                val onResult: ((ActivityResult) -> Unit)? = if (onResultAnnotation != null) {
                    { result -> method.invoke(activity, result) }
                } else {
                    null
                }
                val nextStep: String = onResultAnnotation?.nextStep ?: ""
                annotationSteps[stepName] = Step(intentBuilder, onResult, nextStep, parentClass)
            }
        }

        val initialStep = annotationSteps[initialStepName ?: annotationSteps.keys.first()]
            ?: throw IllegalStateException("Start step not found.")
        val initialIntent = initialStep.intentBuilder(null)
        if (initialStep.parent != null) {
            TaskStackBuilder.create(activity)
                .addParentStack(initialStep.parent)
                .addNextIntent(initialIntent)
                .startActivities()
        } else {
            launcher.launch(initialIntent)
        }

        scope.launch {
            for (result in resultChannel) {
                val currentStep = annotationSteps.values.find { step ->
                    step.onResult != null && step.onResult.hashCode() == result.hashCode()
                }
                if (currentStep != null) {
                    currentStep.onResult?.invoke(result)
                    val nextStepName = currentStep.nextStep
                    if (nextStepName.isNotEmpty()) {
                        val nextStep = annotationSteps[nextStepName]
                            ?: throw IllegalStateException("Next step '$nextStepName' not found.")
                        val nextIntent = nextStep.intentBuilder(result)
                        if (nextStep.parent != null) {
                            TaskStackBuilder.create(activity)
                                .addParentStack(nextStep.parent)
                                .addNextIntent(nextIntent)
                                .startActivities()
                        } else {
                            launcher.launch(nextIntent)
                        }
                    }
                }
            }
        }

        return resultChannel.receiveAsFlow()
    }
}

/**
 * Utility function to create a DSL-based FlowIntentChain and execute it.
 * @param activity The hosting activity
 * @param block DSL block to define the chain steps
 * @return A Flow emitting ActivityResult objects
 * @throws IllegalStateException if called after the activity reaches STARTED state
 */
fun flowIntentChain(
    activity: AppCompatActivity,
    block: FlowIntentChain.() -> Unit
): Flow<ActivityResult> {
    if (activity.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) {
        throw IllegalStateException("flowIntentChain must be called before the Activity is STARTED")
    }
    val chain = FlowIntentChain(activity, activity.lifecycleScope)
    chain.block()
    return chain.build()
}

/**
 * Extension function to create an annotation-based FlowIntentChain and execute it.
 * @receiver The hosting AppCompatActivity
 * @return A Flow emitting ActivityResult objects
 * @throws IllegalStateException if called after the activity reaches STARTED state
 */
fun AppCompatActivity.flowIntentChainFromAnnotations(): Flow<ActivityResult> {
    if (lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) {
        throw IllegalStateException("flowIntentChainFromAnnotations must be called before the Activity is STARTED")
    }
    val chain = FlowIntentChain(this, lifecycleScope)
    return chain.buildFlowFromAnnotations()
}