package com.flowintent.core.chain

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

    // Present a step in intent chain
    data class Step(
        val intentBuilder: (ActivityResult?) -> Intent,
        val onResult: ((ActivityResult) -> Unit)?,
        val nextStep: String = ""
    )

    // For DSL methods
    fun startActivity(intentBuilder: (ActivityResult?) -> Intent) {
        dslSteps.add(Step(intentBuilder, null))
    }

    fun onResult(onResult: (ActivityResult) -> Unit) {
        if (dslSteps.isEmpty()) throw IllegalStateException("Register startActivity first.")
        val lastStep = dslSteps.removeLast()
        dslSteps.add(Step(lastStep.intentBuilder, onResult))
    }

    // Create a flow with DSL
    fun build(): Flow<ActivityResult> {
        if (dslSteps.isEmpty()) throw IllegalStateException("Hiçbir DSL adımı tanımlanmadı")

        var stepIndex = 0
        var previousResult: ActivityResult? = null

        scope.launch {
            launcher.launch(dslSteps[0].intentBuilder(null))
            for (result in resultChannel) {
                val currentStep = dslSteps.getOrNull(stepIndex)
                currentStep?.onResult?.invoke(result)
                previousResult = result
                stepIndex++
                val nextStep = dslSteps.getOrNull(stepIndex)
                if (nextStep != null) {
                    launcher.launch(nextStep.intentBuilder(previousResult))
                }
            }
        }

        return resultChannel.receiveAsFlow()
    }

    fun buildFlowFromAnnotations(): Flow<ActivityResult> {
        val methods = activity::class.java.declaredMethods
        var initialStepName: String? = null

        methods.forEach { method ->
            if (method.isAnnotationPresent(InitialStep::class.java)) {
                val startAnnotation = method.getAnnotation(StartActivity::class.java)
                if (startAnnotation != null) {
                    initialStepName = startAnnotation.stepName // Safe access
                } else {
                    throw IllegalStateException("InitialStep ile StartActivity birlikte kullanılmalı")
                }
            }

            if (method.isAnnotationPresent(StartActivity::class.java)) {
                val startAnnotation = method.getAnnotation(StartActivity::class.java)
                val stepName = startAnnotation.stepName
                val intentBuilder: (ActivityResult?) -> Intent = { param ->
                    method.invoke(activity, param) as Intent
                }
                val onResultAnnotation = method.getAnnotation(OnResult::class.java)
                val onResult: ((ActivityResult) -> Unit)? = if (onResultAnnotation != null) {
                    { result -> method.invoke(activity, result) }
                } else {
                    null
                }
                val nextStep: String = if (onResultAnnotation != null) {
                    onResultAnnotation.nextStep
                } else {
                    ""
                }
                annotationSteps[stepName] = Step(intentBuilder, onResult, nextStep)
            }
        }

        val initialStep = annotationSteps[initialStepName ?: annotationSteps.keys.first()]
            ?: throw IllegalStateException("Başlangıç adımı bulunamadı")
        launcher.launch(initialStep.intentBuilder(null))

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
                            ?: throw IllegalStateException("Sonraki adım '$nextStepName' bulunamadı")
                        launcher.launch(nextStep.intentBuilder(result))
                    }
                }
            }
        }

        return resultChannel.receiveAsFlow()
    }
}

// Utility function for DSL
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

// Utility function for annotation
fun AppCompatActivity.flowIntentChainFromAnnotations(): Flow<ActivityResult> {
    if (lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) {
        throw IllegalStateException("flowIntentChainFromAnnotations must be called before the Activity is STARTED")
    }
    val chain = FlowIntentChain(this, lifecycleScope)
    return chain.buildFlowFromAnnotations()
}