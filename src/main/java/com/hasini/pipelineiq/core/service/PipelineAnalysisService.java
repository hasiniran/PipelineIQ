package com.hasini.pipelineiq.core.service;

import com.hasini.pipelineiq.core.classify.FailureClassifier;
import com.hasini.pipelineiq.core.explain.FailureExplainer;
import com.hasini.pipelineiq.core.model.FailureCategory;
import com.hasini.pipelineiq.core.model.FailureExplanation;
import com.hasini.pipelineiq.core.model.LogSnippet;
import com.hasini.pipelineiq.core.model.PipelineReport;
import com.hasini.pipelineiq.core.parse.LogParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

@Service
public class PipelineAnalysisService {

    private final LogParser logParser;
    private final FailureClassifier failureClassifier;
    private final FailureExplainer failureExplainer;

    public PipelineAnalysisService(LogParser logParser, FailureClassifier failureClassifier, FailureExplainer failureExplainer) {
        this.logParser = logParser;
        this.failureClassifier = failureClassifier;
        this.failureExplainer = failureExplainer;
    }

    /**
     * Analyzes the log file at the specified location and returns a PipelineReport.
     *
     * @param logLocation The path to the log file (or a URI for remote logs)
     * @return a PipelineReport indicating whether the analysis was successful
     */
    public PipelineReport analyze(Path logLocation) {
        try {
            Optional<LogSnippet> logSnippet = logParser.extractErrorSnippet(logLocation);
            if (logSnippet.isPresent()) {
                FailureCategory category = failureClassifier.classify(logSnippet.get());
                FailureExplanation explanation = failureExplainer.explain(logSnippet.get(), category);
                return PipelineReport.of(category, explanation);
            } else {
                return PipelineReport.notFound();
            }
        } catch (IOException e) {
            // Log the error with SLF4J
            Logger logger = LoggerFactory.getLogger(PipelineAnalysisService.class);
            logger.error("Failed to read log file at location: {}", logLocation, e);
            return PipelineReport.notFound();
        }
    }
}
