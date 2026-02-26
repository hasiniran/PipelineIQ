package com.hasini.pipelineiq.core.parse;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;


public interface LogParser {

    /**
     * Scans the log source to extract the relevant failure context.
     * @param logLocation The path to the log file (or a URI for remote logs)
     * @return A String containing the "hot zone" (the error and surrounding lines)
     * @throws LogParsingException if the file is unreadable or context can't be found
     */
    String extractErrorSnippet(Path logLocation) throws IOException;

}
