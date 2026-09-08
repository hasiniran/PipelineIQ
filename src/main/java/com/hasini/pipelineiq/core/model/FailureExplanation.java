package com.hasini.pipelineiq.core.model;

import java.util.Objects;

/**
 * Domain model representing AI-driven root cause analysis and suggested fix.
 */
public record FailureExplanation(String rootCause, String recommendedFix) {
    public static final FailureExplanation EMPTY = 
        new FailureExplanation("No failure identified.", "Inspect raw CI logs manually.");

    public FailureExplanation {
        Objects.requireNonNull(rootCause, "rootCause cannot be null");
        Objects.requireNonNull(recommendedFix, "recommendedFix cannot be null");
    }
}