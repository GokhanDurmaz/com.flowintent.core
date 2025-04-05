package com.flowintent.core.annotations

import kotlin.reflect.KClass

/**
 * Annotation to mark a function as a step in the FlowIntentChain that starts an activity.
 * The function must return an [Intent], which will be used to launch the activity as part of the chain.
 *
 * This annotation is processed at runtime to configure the navigation flow in a declarative way.
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

    /**
     * The name of the parent step or activity in the chain, used to define a hierarchical navigation structure.
     * If specified, it influences the back stack behavior when launching this step’s activity.
     * If empty, no parent is assumed.
     *
     * @property parent The identifier of the parent step.
     */
    val parent: String = ""
)

/**
 * Annotation to mark a function as a result handler for a step in the FlowIntentChain.
 * The annotated function will be invoked with the [ActivityResult] when the launched activity returns a result.
 *
 * This allows chaining subsequent steps based on the result of a previous activity.
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

/**
 * Annotation to define a deep link parameter for a function handling deep link navigation in FlowIntentChain.
 * Can be repeated to specify multiple parameters for validation and processing.
 *
 * Used to enforce parameter requirements and validation rules for deep link handling.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class DeepLinkParam(
    /**
     * The name of the deep link parameter (e.g., "id" in "myapp://details?id=123").
     *
     * @property name The parameter key to extract from the deep link URI.
     */
    val name: String,

    /**
     * Indicates whether this parameter is required for the deep link to be valid.
     * If true and the parameter is missing, validation fails.
     *
     * @property isRequired Whether the parameter is mandatory (default is false).
     */
    val isRequired: Boolean = false,

    /**
     * The class of a custom validator to check the parameter’s value.
     * Must be a Kotlin class implementing validation logic; defaults to [Any] if no specific validation is needed.
     *
     * @property validator The validator class (default is [Any]).
     */
    val validator: KClass<*> = Any::class,

    /**
     * The error message to display or log if the parameter validation fails.
     * Useful for debugging or user feedback; defaults to an empty string.
     *
     * @property errorMessage The custom error message (default is empty).
     */
    val errorMessage: String = ""
)

/**
 * Annotation to mark a function as the error handler for deep link processing in FlowIntentChain.
 * The annotated function will be invoked if an error occurs during deep link validation or navigation.
 *
 * Only one function should bear this annotation per chain to ensure a single error-handling entry point.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class OnDeepLinkError



