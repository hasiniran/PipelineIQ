package com.hasini.pipelineiq.core.parse;

import com.hasini.pipelineiq.core.model.LogSnippet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SimpleLogParserTest {

    private SimpleLogParser parser;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        parser = new SimpleLogParser();
        ReflectionTestUtils.setField(parser, "errorPatternString", "\\[ERROR\\]|\\[Failure\\]");
        parser.init();
    }

    @Test
    void extractErrorSnippet_nullPath_returnsEmptyOptional() throws IOException {
        Optional<LogSnippet> result = parser.extractErrorSnippet(null);

        assertThat(result).isEmpty();
    }

    @Test
    void extractErrorSnippet_missingFile_throwsIOException() {
        Path missing = tempDir.resolve("missing.log");

        assertThatThrownBy(() -> parser.extractErrorSnippet(missing))
                .isInstanceOf(IOException.class);
    }

    @Test
    void extractErrorSnippet_emptyFile_returnsEmptyOptional() throws IOException {
        Path emptyFile = tempDir.resolve("empty.log");
        Files.write(emptyFile, List.of());

        Optional<LogSnippet> result = parser.extractErrorSnippet(emptyFile);

        assertThat(result).isEmpty();
    }

    @Test
    void extractErrorSnippet_whitespaceOnlyFile_returnsEmptyOptional() throws IOException {
        Path whitespaceFile = tempDir.resolve("whitespace.log");
        Files.write(whitespaceFile, List.of("   ", "\t"));

        Optional<LogSnippet> result = parser.extractErrorSnippet(whitespaceFile);

        assertThat(result).isEmpty();
    }

    @Test
    void extractErrorSnippet_noMatch_returnsFallbackSnippet() throws IOException {
        List<String> lines = List.of(
                "line one",
                "line two",
                "line three"
        );
        Path logFile = tempDir.resolve("no-error.log");
        Files.write(logFile, lines);

        LogSnippet result = parser.extractErrorSnippet(logFile)
                .orElseThrow(() -> new AssertionError("Expected snippet to be present"));

        assertThat(result.rawContent()).isEqualTo(String.join("\n", lines));
    }

    @Test
    void extractErrorSnippet_matchOnFirstLine_returnsErrorLineAndAfterContext() throws IOException {
        List<String> lines = List.of(
                "[ERROR] Something broke",
                "stack line 1",
                "stack line 2"
        );
        Path logFile = tempDir.resolve("error-first.log");
        Files.write(logFile, lines);

        LogSnippet result = parser.extractErrorSnippet(logFile)
                .orElseThrow(() -> new AssertionError("Expected snippet to be present"));

        assertThat(result.rawContent())
                .startsWith("[ERROR] Something broke")
                .contains("stack line 1", "stack line 2");
        assertThat(result.rawContent().split("\n")).hasSize(3);
    }

    @Test
    void extractErrorSnippet_matchInMiddle_includesBeforeAndAfterContext() throws IOException {
        List<String> before = List.of("line1", "line2", "line3");
        String errorLine = "[ERROR] compilation failed";
        List<String> after = List.of("at com.example.Main.main", "at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0");
        List<String> all = new ArrayList<>(before);
        all.add(errorLine);
        all.addAll(after);
        Path logFile = tempDir.resolve("error-middle.log");
        Files.write(logFile, all);

        LogSnippet result = parser.extractErrorSnippet(logFile)
                .orElseThrow(() -> new AssertionError("Expected snippet to be present"));

        assertThat(result.rawContent())
                .contains("line1", "line2", "line3")
                .contains("[ERROR] compilation failed")
                .contains("at com.example.Main.main")
                .contains("at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0");
    }

    @Test
    void extractErrorSnippet_matchCaseInsensitive_matchesLowercaseError() throws IOException {
        Path logFile = tempDir.resolve("lowercase.log");
        Files.write(logFile, List.of("info line", "[error] something wrong", "after"));

        LogSnippet result = parser.extractErrorSnippet(logFile)
                .orElseThrow(() -> new AssertionError("Expected snippet to be present"));

        assertThat(result.rawContent()).contains("[error] something wrong").contains("info line").contains("after");
    }

    @Test
    void extractErrorSnippet_matchFailurePattern_matchesBracketFailure() throws IOException {
        Path logFile = tempDir.resolve("failure.log");
        Files.write(logFile, List.of("pre", "[Failure] Assertion failed", "post"));

        LogSnippet result = parser.extractErrorSnippet(logFile)
                .orElseThrow(() -> new AssertionError("Expected snippet to be present"));

        assertThat(result.rawContent()).contains("[Failure] Assertion failed").contains("pre").contains("post");
    }

    @Test
    void extractErrorSnippet_returnsFirstMatchOnly_ignoresLaterErrors() throws IOException {
        List<String> lines = List.of(
                "first [ERROR] first error",
                "second [ERROR] second error",
                "third [ERROR] third error"
        );
        Path logFile = tempDir.resolve("multiple-errors.log");
        Files.write(logFile, lines);

        LogSnippet result = parser.extractErrorSnippet(logFile)
                .orElseThrow(() -> new AssertionError("Expected snippet to be present"));

        assertThat(result.rawContent())
                .contains("first [ERROR] first error")
                .contains("second [ERROR] second error")
                .contains("third [ERROR] third error");
        assertThat(result.rawContent().split("\n")).hasSize(3);
    }

    @Test
    void extractErrorSnippet_manyLinesBeforeError_truncatesToWindowSize() throws IOException {
        List<String> prefix = List.of("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m");
        List<String> rest = List.of("[ERROR] only this error", "tail");
        List<String> all = new ArrayList<>(prefix);
        all.addAll(rest);
        Path logFile = tempDir.resolve("many-before.log");
        Files.write(logFile, all);

        LogSnippet result = parser.extractErrorSnippet(logFile)
                .orElseThrow(() -> new AssertionError("Expected snippet to be present"));

        String content = result.rawContent();
        assertThat(content).contains("[ERROR] only this error").contains("tail");
        List<String> lines = List.of(content.split("\n"));
        assertThat(lines.size()).isLessThanOrEqualTo(11 + 1 + 20);
        assertThat(lines).doesNotContain("a").doesNotContain("b").doesNotContain("c");
        assertThat(lines).contains("d", "e", "f", "g", "h", "i", "j", "k", "l", "m");
    }
}
