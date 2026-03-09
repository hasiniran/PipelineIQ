package com.hasini.pipelineiq.core.classify;

import com.hasini.pipelineiq.core.model.FailureCategory;
import com.hasini.pipelineiq.core.model.LogSnippet;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class RegexFailureClassifierTest {

    private final RegexFailureClassifier classifier = new RegexFailureClassifier();

    @ParameterizedTest(name = "{index} => {1}")
    @MethodSource("provideLogSnippets")
    void classify_ShouldReturnCorrectCategory(String logContent, FailureCategory expectedCategory) {
        LogSnippet snippet = null;
        if (logContent != null && !logContent.isBlank()) {
            snippet = new LogSnippet(logContent);
        }
        
        FailureCategory result = classifier.classify(snippet);
        assertThat(result)
                .as("Checking category for log: %s", logContent)
                .isEqualTo(expectedCategory);
    }

    private static Stream<Arguments> provideLogSnippets() {
        return Stream.of(
                // Maven Compilation
                Arguments.of("""
                        [INFO] BUILD FAILURE
                        [ERROR] COMPILATION ERROR :
                        [ERROR] /path/to/File.java:[5,24] error: cannot find symbol
                        """, FailureCategory.COMPILATION_ERROR),

                // Gradle Compilation
                Arguments.of("""
                        FAILURE: Build failed with an exception.
                        * What went wrong:
                        Compilation failed; see the compiler error output for details.
                        """, FailureCategory.COMPILATION_ERROR),

                // Maven Test Failure
                Arguments.of("""
                        [ERROR] Tests run: 2, Failures: 1, Errors: 0, Skipped: 0
                        """, FailureCategory.TEST_FAILURE),

                // Gradle Test Failure
                Arguments.of("""
                        > Task :app:test FAILED
                        """, FailureCategory.TEST_FAILURE),

                // Maven Dependency
                Arguments.of("""
                        [ERROR] Failed to execute goal on project: Could not resolve dependencies
                        """, FailureCategory.DEPENDENCY_RESOLUTION),

                // Gradle Dependency
                Arguments.of("""
                        Could not resolve all files for configuration ':classpath'
                        """, FailureCategory.DEPENDENCY_RESOLUTION),

                // Infrastructure
                Arguments.of("The build step-up timed out", FailureCategory.INFRASTRUCTURE_TIMEOUT),
                Arguments.of("Exit code 137", FailureCategory.INFRASTRUCTURE_TIMEOUT),

                // Build Failure (Generic)
                Arguments.of("BUILD FAILURE", FailureCategory.BUILD_FAILURE),
                Arguments.of("BUILD FAILED", FailureCategory.BUILD_FAILURE),

                // Unknown
                Arguments.of("Random log message", FailureCategory.UNKNOWN),
                Arguments.of("", FailureCategory.UNKNOWN),
                Arguments.of(null, FailureCategory.UNKNOWN)
        );
    }
}
