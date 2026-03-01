package com.hasini.pipelineiq.core.classify;

import com.hasini.pipelineiq.core.model.FailureCategory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@Component
@Primary
public class RegexFailureClassifier implements FailureClassifier {

    private record FailureSignature(FailureCategory category, List<Pattern> patterns) {
        private FailureSignature {
            Objects.requireNonNull(category, "FailureCategory cannot be null");
            Objects.requireNonNull(patterns, "Patterns list cannot be null");
        }

        boolean matches(String logSnippet) {
            return patterns.stream().anyMatch(pattern -> pattern.matcher(logSnippet).find());
        }
    }

    private static final List<FailureSignature> SIGNATURES = List.of(
            new FailureSignature(FailureCategory.INFRASTRUCTURE_TIMEOUT, List.of(
                    Pattern.compile("The build step-up timed out"),
                    Pattern.compile("Exit code 137")
            )),
            new FailureSignature(FailureCategory.COMPILATION_ERROR, List.of(
                    Pattern.compile("\\[ERROR\\] COMPILATION ERROR :", Pattern.DOTALL),
                    Pattern.compile("ERROR\\] Failed to execute goal org\\.apache\\.maven\\.plugins:maven-compiler-plugin", Pattern.DOTALL),
                    Pattern.compile("FAILURE: Build failed with an exception\\.", Pattern.DOTALL),
                    Pattern.compile("Compilation failed", Pattern.DOTALL)
            )),
            new FailureSignature(FailureCategory.TEST_FAILURE, List.of(
                    Pattern.compile("\\[ERROR\\] Tests run:.*, Failures:.*, Errors:.*, Skipped:.*", Pattern.DOTALL),
                    Pattern.compile("> Task :.* FAILED", Pattern.DOTALL),
                    Pattern.compile("\\\\\\\\\\[ERROR\\\\\\\\] Failures:.*", Pattern.DOTALL),
                    Pattern.compile("Tests run:.*FAILURE", Pattern.DOTALL)
            )),
            new FailureSignature(FailureCategory.DEPENDENCY_RESOLUTION, List.of(
                    Pattern.compile("Could not resolve dependencies", Pattern.DOTALL),
                    Pattern.compile("Could not find artifact", Pattern.DOTALL),
                    Pattern.compile("\\[ERROR\\] Failed to execute goal org\\.apache\\.maven\\.plugins:maven-dependency-plugin", Pattern.DOTALL),
                    Pattern.compile("Could not resolve all files for configuration", Pattern.DOTALL),
                    Pattern.compile("Failed to resolve dependencies", Pattern.DOTALL)
            )),
            new FailureSignature(FailureCategory.BUILD_FAILURE, List.of(
                    Pattern.compile("BUILD FAILURE", Pattern.DOTALL),
                    Pattern.compile("BUILD FAILED", Pattern.DOTALL)
            ))
    );

    @Override
    public FailureCategory classify(String logSnippet) {
        if (logSnippet == null || logSnippet.isEmpty()) {
            return FailureCategory.UNKNOWN;
        }

        return SIGNATURES.stream()
                .filter(signature -> signature.matches(logSnippet))
                .map(signature -> signature.category)
                .findFirst()
                .orElse(FailureCategory.UNKNOWN);
    }
}
