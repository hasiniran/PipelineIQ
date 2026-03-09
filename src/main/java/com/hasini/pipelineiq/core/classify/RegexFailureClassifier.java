package com.hasini.pipelineiq.core.classify;

import com.hasini.pipelineiq.core.model.FailureCategory;
import com.hasini.pipelineiq.core.model.LogSnippet;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Classifies pipeline failures based on regex pattern matching.
 * Uses the pre-normalized content from LogSnippet for consistent pattern matching.
 */
@Component
@Primary
public class RegexFailureClassifier implements FailureClassifier {

    // compile helper applies CASE_INSENSITIVE and DOTALL per request
    private static Pattern compile(String regex) {
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    }

    private record FailureSignature(FailureCategory category, List<Pattern> patterns) {
        private FailureSignature {
            Objects.requireNonNull(category, "FailureCategory cannot be null");
            Objects.requireNonNull(patterns, "Patterns list cannot be null");
        }

        boolean matches(String normalizedSnippet) {
            return patterns.stream().anyMatch(pattern -> pattern.matcher(normalizedSnippet).find());
        }
    }

    private static final List<FailureSignature> SIGNATURES = List.of(
            new FailureSignature(FailureCategory.INFRASTRUCTURE_TIMEOUT, List.of(
                    // use word boundaries to avoid partial matches
                    compile("\\b(the\\s+)?build\\b.*\\btimed\\s*out\\b"),
                    compile("\\bexit\\s*code\\s*137\\b")
            )),
            new FailureSignature(FailureCategory.COMPILATION_ERROR, List.of(
                    compile("\\bcompilation\\s*error\\b"),
                    compile("\\berror\\]\\s*failed\\s*to\\s*execute\\s*goal\\b.*maven-compiler-plugin"),
                    compile("\\bfailure\\s*:\\s*build\\s*failed\\s*with\\s*an\\s*exception\\.?"),
                    compile("\\bcompilation\\s*failed\\b"),
                    compile(".+?\\.java:\\d+:\\s*error:")
            )),
            new FailureSignature(FailureCategory.TEST_FAILURE, List.of(
                    // Maven/TestNG/JUnit summary lines
                    compile("\\btests\\s*run\\s*[:]?\\s*\\d+\\s*,\\s*failures\\s*[:]?\\s*\\d+\\b"),
                    // Gradle style: "> Task :app:test FAILED" or similar
                    compile("\\btask\\b.*\\btest\\b.*\\bfailed\\b"),
                    compile("\\bfailures\\s*[:]?\\s*\\d+\\b")
            )),
            new FailureSignature(FailureCategory.DEPENDENCY_RESOLUTION, List.of(
                    compile("\\bcould\\s*not\\s*resolve\\b"),
                    compile("\\bcould\\s*not\\s*find\\s*artifact\\b"),
                    compile("\\bfailed\\s*to\\s*execute\\s*goal\\b.*maven-dependency-plugin"),
                    compile("\\bcould\\s*not\\s*resolve\\s*all\\s*files\\s*for\\s*configuration\\b"),
                    compile("\\bfailed\\s*to\\s*resolve\\s*dependencies\\b"),
                    compile("\\bfailed\\s*to\\s*collect\\s*dependencies\\b")
            )),
            new FailureSignature(FailureCategory.BUILD_FAILURE, List.of(
                    compile("\\bbuild\\s*failure\\b"),
                    compile("\\bbuild\\s*failed\\b")
            ))
    );

    @Override
    public FailureCategory classify(LogSnippet logSnippet) {
        if (logSnippet == null) {
            return FailureCategory.UNKNOWN;
        }

        String normalizedSnippet = logSnippet.getNormalizedString();
        if (normalizedSnippet == null || normalizedSnippet.isBlank()) {
            return FailureCategory.UNKNOWN;
        }

        return SIGNATURES.stream()
                .filter(signature -> signature.matches(normalizedSnippet))
                .map(signature -> signature.category)
                .findFirst()
                .orElse(FailureCategory.UNKNOWN);
    }
}