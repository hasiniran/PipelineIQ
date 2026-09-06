package com.hasini.pipelineiq.core.parse;

import com.hasini.pipelineiq.core.model.LogSnippet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Implementation of {@link LogParser} that scans log files for error patterns.
 *
 * Extracts a context window around the first detected error, including
 * lines before and after the match. Uses efficient O(1) deque operations.
 * Returns raw (non-normalized) snippets for downstream processing.
 *
 * Performance: O(n) where n is the number of lines in the log file.
 * Memory: O(w) where w is the window size (lines before + lines after).
 */
@Component
public class SimpleLogParser implements LogParser {

    private static final Logger logger = LoggerFactory.getLogger(SimpleLogParser.class);
    private static final int LINES_BEFORE = 10;
    private static final int LINES_AFTER = 20;

    @Value("${logparser.error.pattern:\\[ERROR\\]|\\[Failure\\]}")
    private String errorPatternString;

    private Pattern errorPattern;

    @PostConstruct
    public void init() {
        this.errorPattern = Pattern.compile(errorPatternString, Pattern.CASE_INSENSITIVE);
        logger.debug("Initialized SimpleLogParser with pattern: {}", errorPatternString);
    }

    @Override
    public Optional<LogSnippet> extractErrorSnippet(Path logLocation) throws IOException {
        if (logLocation == null) {
            logger.warn("logLocation is null, returning empty");
            return Optional.empty();
        }

        try (Stream<String> lines = Files.lines(logLocation)) {
            Deque<String> beforeWindow = new ArrayDeque<>();
            var iterator = lines.iterator();

            while (iterator.hasNext()) {
                var line = iterator.next();
                beforeWindow.addLast(line);

                // Maintain window size for lines before match using O(1) removeFirst
                if (beforeWindow.size() > LINES_BEFORE + 1) {
                    beforeWindow.removeFirst();
                }

                if (errorPattern.matcher(line).find()) {
                    logger.debug("Error pattern matched in line: {}", line);
                    // Start constructing the snippet with all lines from beforeWindow (full context)
                    var snippetLines = new ArrayDeque<>(beforeWindow);
                    // Collect subsequent lines after error (without truncation)
                    for (int i = 0; i < LINES_AFTER && iterator.hasNext(); i++) {
                        snippetLines.addLast(iterator.next());
                    }
                    return toSnippet(String.join("\n", snippetLines));
                }
            }

            // If no error found, return the last lines captured in the window (fallback)
            return toSnippet(String.join("\n", beforeWindow));
        } catch (IOException e) {
            logger.error("Failed to read log file: {}", logLocation, e);
            throw e;
        }
    }

    private Optional<LogSnippet> toSnippet(String snippet) {
        if (snippet == null || snippet.isBlank()) {
            logger.debug("Extracted content cannot form a valid snippet");
            return Optional.empty();
        }
        logger.debug("Extracted error snippet of {} characters", snippet.length());
        return Optional.of(new LogSnippet(snippet));
    }
}
