package com.hasini.pipelineiq.core.classify;

import com.hasini.pipelineiq.core.model.FailureCategory;
import com.hasini.pipelineiq.core.model.LogSnippet;
import org.springframework.stereotype.Component;

@Component
public class SimpleFailureClassifier implements FailureClassifier{
    @Override
    public FailureCategory classify(LogSnippet logSnippet) {
        return FailureCategory.TEST_FAILURE;
    }
}
