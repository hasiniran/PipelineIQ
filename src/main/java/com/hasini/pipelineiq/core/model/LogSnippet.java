
package com.hasini.pipelineiq.core.model;

import java.util.Objects;

public record LogSnippet(
    String rawContent
) {
    public static final LogSnippet EMPTY=new LogSnippet("EMPTY_LOG_SIGNAL");
    public LogSnippet {

        Objects.requireNonNull(rawContent, "rawContent cannot be null");


    }

    public String getNormalizedString(){
        return rawContent.trim().toLowerCase().replaceAll("[\\s\\t]+", " ");
    }
}
