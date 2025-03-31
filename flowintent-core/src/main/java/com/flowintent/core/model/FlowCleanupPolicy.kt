package com.flowintent.core.model

/**
 * An enumeration defining policies for managing previous flow emissions in FlowIntent.
 * Determines whether to clean up or retain previous flows when a new one is created or started.
 */
enum class FlowCleanupPolicy {
    /**
     * Indicates that any previous flow should be cleaned up (e.g., canceled or removed)
     * when a new flow is created or started. This ensures only one active flow exists at a time,
     * useful for scenarios where only the latest data or task is relevant.
     */
    CLEANUP_PREVIOUS,

    /**
     * Indicates that previous flows should be kept active alongside new flows.
     * This allows multiple flows to coexist, useful for scenarios where historical or concurrent
     * data emissions need to be preserved and processed.
     */
    KEEP_PREVIOUS
}