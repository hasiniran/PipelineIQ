package com.hasini.pipelineiq.core.classify;

import com.hasini.pipelineiq.core.model.FailureCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegexFailureClassifierTest {

    private RegexFailureClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new RegexFailureClassifier();
    }

    @Test
    void classify_MavenCompilationError_ShouldReturnCompilationError() {
        var logSnippet = """
                [INFO] ------------------------------------------------------------------------
                [INFO] BUILD FAILURE
                [INFO] ------------------------------------------------------------------------
                [ERROR] COMPILATION ERROR :
                [ERROR] /E:/java/pipelineiq/src/main/java/com/hasini/pipelineiq/core/classify/Something.java:[5,24] error: cannot find symbol
                """;
        assertThat(classifier.classify(logSnippet)).isEqualTo(FailureCategory.COMPILATION_ERROR);
    }

    @Test
    void classify_MavenTestFailure_ShouldReturnTestFailure() {
        var logSnippet = """
                [ERROR] Tests run: 2, Failures: 1, Errors: 0, Skipped: 0
                [INFO] ------------------------------------------------------------------------
                [ERROR] There are test failures.
                """;
        assertThat(classifier.classify(logSnippet)).isEqualTo(FailureCategory.TEST_FAILURE);
    }

    @Test
    void classify_MavenDependencyResolutionFailure_ShouldReturnDependencyResolution() {
        var logSnippet = """
                [ERROR] Failed to execute goal on project pipelineiq: Could not resolve dependencies for project com.hasini:pipelineiq:jar:0.0.1-SNAPSHOT
                [ERROR] Failed to execute goal org.apache.maven.plugins:maven-dependency-plugin:3.1.2:resolve (default-cli)
                """;
        assertThat(classifier.classify(logSnippet)).isEqualTo(FailureCategory.DEPENDENCY_RESOLUTION);
    }

    @Test
    void classify_GradleCompilationError_ShouldReturnCompilationError() {
        var logSnippet = """
                FAILURE: Build failed with an exception.
                * What went wrong:
                Compilation failed; see the compiler error output for details.
                """;
        assertThat(classifier.classify(logSnippet)).isEqualTo(FailureCategory.COMPILATION_ERROR);
    }

    @Test
    void classify_GradleTestFailure_ShouldReturnTestFailure() {
        var logSnippet = """
                > Task :app:test FAILED
                BUILD FAILED in 1s
                """;
        assertThat(classifier.classify(logSnippet)).isEqualTo(FailureCategory.TEST_FAILURE);
    }

    @Test
    void classify_GradleDependencyResolutionFailure_ShouldReturnDependencyResolution() {
        var logSnippet = """
                Could not resolve all files for configuration ':classpath'
                Failed to resolve dependencies
                """;
        assertThat(classifier.classify(logSnippet)).isEqualTo(FailureCategory.DEPENDENCY_RESOLUTION);
    }

    @Test
    void classify_GenericBuildFailure_ShouldReturnBuildFailure() {
        var logSnippet = """
                [INFO] ------------------------------------------------------------------------
                [INFO] BUILD FAILURE
                [INFO] ------------------------------------------------------------------------
                """;
        assertThat(classifier.classify(logSnippet)).isEqualTo(FailureCategory.BUILD_FAILURE);
    }

    @Test
    void classify_UnknownLogSnippet_ShouldReturnUnknown() {
        var logSnippet = "This is a random log message without failure signatures.";
        assertThat(classifier.classify(logSnippet)).isEqualTo(FailureCategory.UNKNOWN);
    }
}
