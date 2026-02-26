package com.hasini.pipelineiq.core.model;

import lombok.Getter;

@Getter
public enum FailureCategory {
    COMPILATION_ERROR("The code failed to compile."),
    TEST_FAILURE("Unit or integration tests failed."),
    DEPENDENCY_RESOLUTION("Could not download or find required libraries."),
    INFRASTRUCTURE_TIMEOUT("The CI runner timed out or lost connection."),
    LINT_CHECK_FAILURE("Code style or static analysis rules were violated."),
    UNKNOWN("The cause of failure could not be determined deterministically.");

    private final String description;
    FailureCategory(String description) {
        this.description = description;
    }

}
