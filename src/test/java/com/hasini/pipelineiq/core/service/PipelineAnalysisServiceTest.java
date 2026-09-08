package com.hasini.pipelineiq.core.service;

import com.hasini.pipelineiq.core.model.FailureCategory;
import com.hasini.pipelineiq.core.model.FailureExplanation;
import com.hasini.pipelineiq.core.model.LogSnippet;
import com.hasini.pipelineiq.core.model.PipelineReport;
import com.hasini.pipelineiq.core.parse.LogParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PipelineAnalysisServiceTest {

    @Mock
    private LogParser logParser;

    @Mock
    private com.hasini.pipelineiq.core.classify.FailureClassifier failureClassifier;

    @Mock
    private com.hasini.pipelineiq.core.explain.FailureExplainer failureExplainer;

    @InjectMocks
    private PipelineAnalysisService service;

    @Test
    void analyze_nullLogLocation_returnsNotFoundReportAnd_skipsDownstreamCalls() throws Exception {
        // Arrange
        Path logLocation = null;
        when(logParser.extractErrorSnippet(null)).thenReturn(Optional.empty());

        // Act
        PipelineReport result = service.analyze(logLocation);

        // Assert
        assertThat(result).isEqualTo(PipelineReport.notFound());

        verify(logParser).extractErrorSnippet(null);
        verifyNoInteractions(failureClassifier, failureExplainer);
        verifyNoMoreInteractions(logParser);
    }

    @Test
    void analyze_emptyLogSnippet_returnsNotFoundReport() throws Exception {
        // Arrange
        Path logLocation = Paths.get("/tmp/some.log");
        when(logParser.extractErrorSnippet(logLocation)).thenReturn(Optional.empty());

        // Act
        PipelineReport result = service.analyze(logLocation);

        // Assert
        assertThat(result).isEqualTo(PipelineReport.notFound());

        verify(logParser).extractErrorSnippet(logLocation);
        verifyNoInteractions(failureClassifier, failureExplainer);
        verifyNoMoreInteractions(logParser);
    }

    @Test
    void analyze_parserThrowsIOException_catchesExceptionAndReturnsNotFoundReport() throws Exception {
        // Arrange
        Path logLocation = Paths.get("/tmp/error.log");
        when(logParser.extractErrorSnippet(logLocation)).thenThrow(new IOException("read error"));

        // Act
        PipelineReport result = service.analyze(logLocation);

        // Assert
        assertThat(result).isEqualTo(PipelineReport.notFound());

        verify(logParser).extractErrorSnippet(logLocation);
        verifyNoInteractions(failureClassifier, failureExplainer);
        verifyNoMoreInteractions(logParser);
    }

    @Test
    void analyze_validLog_orchestratesFlowAndReturnsPopulatedReport() throws Exception {
        // Arrange
        Path logLocation = Paths.get("/tmp/valid.log");
        LogSnippet snippet = new LogSnippet("error: something failed\nstacktrace");
        when(logParser.extractErrorSnippet(logLocation)).thenReturn(Optional.of(snippet));

        FailureCategory category = FailureCategory.BUILD_FAILURE;
        FailureExplanation explanation = new FailureExplanation("root cause", "recommended fix");

        when(failureClassifier.classify(snippet)).thenReturn(category);
        when(failureExplainer.explain(snippet, category)).thenReturn(explanation);

        // Act
        PipelineReport result = service.analyze(logLocation);

        // Assert
        assertThat(result).isEqualTo(PipelineReport.of(category, explanation));

        verify(logParser).extractErrorSnippet(logLocation);
        verify(failureClassifier).classify(snippet);
        verify(failureExplainer).explain(snippet, category);

        verifyNoMoreInteractions(logParser, failureClassifier, failureExplainer);
    }
}
