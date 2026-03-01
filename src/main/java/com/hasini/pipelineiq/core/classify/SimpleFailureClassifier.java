package com.hasini.pipelineiq.core.classify;

import com.hasini.pipelineiq.core.model.FailureCategory;
import org.springframework.stereotype.Component;

@Component
public class SimpleFailureClassifier implements FailureClassifier{
    @Override
    public FailureCategory classify(String logSnippet) {
        return FailureCategory.TEST_FAILURE;
    }
}
