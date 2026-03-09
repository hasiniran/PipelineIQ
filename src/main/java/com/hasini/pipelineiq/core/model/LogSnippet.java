
package com.hasini.pipelineiq.core.model;

import java.util.Objects;

public record LogSnippet(
    String rawContent
) {
    public LogSnippet {
        Objects.requireNonNull(rawContent, "rawContent cannot be null");

        if (rawContent.isBlank()) {
            throw new IllegalArgumentException("rawContent cannot be blank");
        }

    }

    public String getNormalizedString(){
        return rawContent.trim().toLowerCase().replaceAll("[\\s\\t]+", " ");
    }
}
