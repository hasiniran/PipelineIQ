package com.hasini.pipelineiq.cli;

import com.hasini.pipelineiq.core.model.PipelineReport;
import com.hasini.pipelineiq.core.service.PipelineAnalysisService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

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
        System.out.println("🔍 Analyzing log file: " + logPath.toAbsolutePath());

        // Delegate the entire workflow to the service
        PipelineReport report = pipelineAnalysisService.analyze(logPath);

        // Print a clean, human-readable summary
        System.out.println("\n=== 🎯 PipelineIQ Analysis ===");
        System.out.println("Category:   " + report.category());
        System.out.println("Root Cause: " + report.explanation().rootCause());
        System.out.println("Fix:        " + report.explanation().recommendedFix());
        System.out.println("================================\n");
    }
}