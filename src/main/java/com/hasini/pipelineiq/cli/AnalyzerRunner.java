package com.hasini.pipelineiq.cli;

import com.hasini.pipelineiq.core.model.PipelineReport;
import com.hasini.pipelineiq.core.service.PipelineAnalysisService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * CLI entry point for executing log analysis from the terminal.
 */
@Component
public class AnalyzerRunner implements CommandLineRunner {

    private final PipelineAnalysisService pipelineAnalysisService;

    public AnalyzerRunner(PipelineAnalysisService pipelineAnalysisService) {
        this.pipelineAnalysisService = pipelineAnalysisService;
    }

    @Override
    public void run(String... args) {
        if (args.length == 0) {
            System.out.println("⚠️ Please provide a log file path as a command-line argument.");
            return;
        }

        Path logPath = Paths.get(args[0]);
        if (!Files.exists(logPath)) {
            System.out.println("⚠️ Log file does not exist at path: " + logPath.toAbsolutePath());
            return;
        }

        System.out.println("🔍 Analyzing log file: " + logPath.toAbsolutePath());

        // Delegate the entire workflow to the service
        PipelineReport report = pipelineAnalysisService.analyze(logPath);
        writeSummary(report);
    }

    private void writeSummary(PipelineReport report) {
        String markdownSummary = buildMarkdownSummary(report);
        String stepSummaryPath = System.getenv("GITHUB_STEP_SUMMARY");

        if (stepSummaryPath != null && !stepSummaryPath.isBlank()) {
            try {
                Path summaryFile = Path.of(stepSummaryPath);
                Path parent = summaryFile.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                Files.writeString(
                    summaryFile,
                    markdownSummary,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
                );
                return;
            } catch (IOException e) {
                System.out.println("⚠️ Unable to write GitHub step summary. Falling back to stdout.");
            }
        }

        System.out.println(markdownSummary);
    }

    private String buildMarkdownSummary(PipelineReport report) {
        return "## PipelineIQ Analysis\n\n"
            + "-** Category:** " + report.category() + "\n"
            + "-** Root Cause:** " + report.explanation().rootCause() + "\n"
            + "-** Fix:** " + report.explanation().recommendedFix() + "\n";
    }
}