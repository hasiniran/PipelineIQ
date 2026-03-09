package com.hasini.pipelineiq.core.classify;

import com.hasini.pipelineiq.core.model.FailureCategory;
import com.hasini.pipelineiq.core.model.LogSnippet;

public interface FailureClassifier {

    FailureCategory classify(LogSnippet logSnippet);
}
