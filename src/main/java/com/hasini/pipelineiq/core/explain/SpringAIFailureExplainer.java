package com.hasini.pipelineiq.core.explain;

import com.hasini.pipelineiq.core.model.FailureCategory;
import com.hasini.pipelineiq.core.model.FailureExplanation;
import com.hasini.pipelineiq.core.model.LogSnippet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class SpringAIFailureExplainer implements FailureExplainer {

    private static final Logger logger = LoggerFactory.getLogger(SpringAIFailureExplainer.class);

    private final ChatClient chatClient;

    public SpringAIFailureExplainer(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }


    @Override
    public FailureExplanation explain(LogSnippet snippet, FailureCategory category) {
        // avoid unnecessary AI API costs
        if (snippet == null || category == null || category == FailureCategory.UNKNOWN) {
            logger.debug("Skipping AI analysis: insufficient or unknown failure context");
            return FailureExplanation.EMPTY;
        }

        String systemPrompt = """
            You are a CI/CD build failure analyzer.
            Analyze the following build log failure snippet categorized under '%s'.
            Provide a root cause (maximum 2 sentences) and a concrete recommended fix (maximum 2 sentences).
            """.formatted(category.name());

        return chatClient.prompt()
                .system(systemPrompt)
                .user(snippet.rawContent())
                .call()
                .entity(FailureExplanation.class);
    }
}