package com.hasini.pipelineiq.core.classify;

import com.hasini.pipelineiq.core.model.FailureCategory;
import com.hasini.pipelineiq.core.model.LogSnippet;
import org.springframework.stereotype.Component;

@Component
public class SimpleFailureClassifier implements FailureClassifier{
    @Override
    public FailureCategory classify(LogSnippet logSnippet) {
        if (logSnippet == null) {
            return FailureCategory.UNKNOWN;
        }
        return FailureCategory.TEST_FAILURE;
    }
}
