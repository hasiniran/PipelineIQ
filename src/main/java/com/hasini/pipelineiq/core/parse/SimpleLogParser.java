package com.hasini.pipelineiq.core.parse;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class SimpleLogParser implements LogParser {
    @Override
    public String extractErrorSnippet(Path logLocation) throws IOException {

        try(Stream<String> lines=Files.lines(logLocation)){
            return lines.skip(Math.max(0,countLines(logLocation)-50)).collect(Collectors.joining("/n"));
        }

    }

    private long countLines(Path logLocation) throws IOException {
        try(Stream<String> lines=Files.lines(logLocation)){
            return lines.count();
        }
    }
}
