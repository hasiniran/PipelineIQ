package com.hasini.pipelineiq.core.classify;

import com.hasini.pipelineiq.core.model.FailureCategory;

public interface FailureClassifier {

    FailureCategory classify(String errorSnippet);
}
