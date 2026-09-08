package com.hasini.pipelineiq.core.explain;

import com.hasini.pipelineiq.core.model.FailureCategory;
import com.hasini.pipelineiq.core.model.FailureExplanation;
import com.hasini.pipelineiq.core.model.LogSnippet;

public interface FailureExplainer {
    FailureExplanation explain(LogSnippet snippet, FailureCategory category);
}