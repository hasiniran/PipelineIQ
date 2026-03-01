package com.hasini.pipelineiq.core.model;

import java.time.Instant;

/**
 * A data carrier representing the final interpreted result of a build failure.
 * @param category
 * @param tool
 * @param rawSnippet
 * @param explanation
 * @param suggestion
 */
public record AnalysisResult(
        FailureCategory category, // Failure type
        String tool,              // e.g., "Maven", "Gradle"
        String rawSnippet,        // The isolated "Hot Zone" from the logs
        String explanation,       // The "Why" from the LLM
        String suggestion,        // The "Action" from the LLM
        Instant processedAt       // Metadata for the report
) {
    /**
     * Factory method to create an initial result before AI enrichment.
     */
    public static AnalysisResult initial(FailureCategory category, String tool, String snippet) {
        return new AnalysisResult(category, tool, snippet, "Pending...", "Pending...", Instant.now());
    }
}