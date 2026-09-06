package com.hasini.pipelineiq.core.parse;

import com.hasini.pipelineiq.core.model.LogSnippet;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;


public interface LogParser {

    /**
     * Scans the log source to extract the relevant failure context.
     *
     * @param logLocation The path to the log file (or a URI for remote logs)
     * @return the error "hot zone" as a {@link LogSnippet}, or {@link Optional#empty()} when
     *         {@code logLocation} is null or the extracted content cannot form a valid snippet
     * @throws IOException if the file is unreadable
     */
    Optional<LogSnippet> extractErrorSnippet(Path logLocation) throws IOException;

}
