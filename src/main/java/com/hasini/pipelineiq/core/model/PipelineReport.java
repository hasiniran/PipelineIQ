package com.hasini.pipelineiq.core.model;

import java.util.Objects;

public record PipelineReport(FailureCategory category, FailureExplanation explanation) {

        public PipelineReport {
            Objects.requireNonNull(category, "FailureCategory cannot be null");
            Objects.requireNonNull(explanation, "FailureExplanation cannot be null");
        }

        public static PipelineReport of(FailureCategory category, FailureExplanation explanation) {
            return new PipelineReport(category, explanation);
        }

        public static PipelineReport notFound() {
            return of(FailureCategory.UNKNOWN, FailureExplanation.EMPTY);
        }

}
