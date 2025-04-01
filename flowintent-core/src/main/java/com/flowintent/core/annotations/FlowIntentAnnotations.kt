package com.flowintent.core.annotations

import kotlin.reflect.KClass

/**
 * Annotation to mark a function as a step in the FlowIntentChain that starts an activity.
 * The function must return an Intent, which will be used to launch the activity.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class StartActivity(
    /**
     * A unique name identifying this step in the chain.
     * Used to reference this step in the FlowIntentChain’s annotation-based configuration.
     */
    val stepName: String,

    /**
     * The intent action to be used when building the Intent for this step.
     * This can correspond to standard Android actions (e.g., Intent.ACTION_VIEW) or custom actions.
     */
    val action: String = "",
    val parent: String = ""
)

/**
 * Annotation to mark a function as a result handler for a step in the FlowIntentChain.
 * The function will be invoked with the ActivityResult when the launched activity returns.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class OnResult(
    /**
     * The name of the step this result handler corresponds to.
     * Links this handler to a specific StartActivity step in the chain.
     */
    val stepName: String,

    /**
     * The name of the next step to execute after handling the result.
     * If empty, no further step is chained automatically.
     * Default value is an empty string, indicating the chain ends unless specified otherwise.
     */
    val nextStep: String = ""
)

/**
 * Annotation to mark a function as the initial step in an annotation-based FlowIntentChain.
 * Must be used in conjunction with StartActivity to define the starting point of the chain.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class InitialStep

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class DeepLinkParam(
    val name: String,
    val isRequired: Boolean = false,
    val validator: KClass<*> = Any::class,
    val errorMessage: String = ""
)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class OnDeepLinkError



