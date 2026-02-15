package com.errorfreetext.entity.enums;

/**
 * Represents the possible states of a text correction task
 * throughout its lifecycle in the system.
 */
public enum TaskStatus {

    /** Task created but not yet picked up for processing */
    PENDING,

    /** Task is currently being processed by the scheduler */
    PROCESSING,

    /** Text correction completed successfully */
    COMPLETED,

    /** Correction failed due to an error */
    FAILED
}