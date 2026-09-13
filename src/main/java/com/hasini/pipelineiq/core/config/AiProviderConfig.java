package com.hasini.pipelineiq.core.config;

                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.InvocationTargetException;

@Configuration
public class AiProviderConfig {

    @Bean
    @ConditionalOnProperty(name = "pipelineiq.provider", havingValue = "openai", matchIfMissing = true)
    public ChatClient openAiChatClient(OpenAiChatModel openAiChatModel, ChatClient.Builder builder) {
        return buildChatClient(builder, openAiChatModel);
    }

    @Bean
    @ConditionalOnProperty(name = "pipelineiq.provider", havingValue = "ollama")
    public ChatClient ollamaChatClient(OllamaChatModel ollamaChatModel, ChatClient.Builder builder) {
        return buildChatClient(builder, ollamaChatModel);
    }

    private ChatClient buildChatClient(ChatClient.Builder builder, Object model) {
        try {
            var configuredBuilder = builder.getClass()
                    .getMethod("chatModel", model.getClass())
                    .invoke(builder, model);
            return (ChatClient) configuredBuilder.getClass().getMethod("build").invoke(configuredBuilder);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            if (model instanceof OpenAiChatModel openAiChatModel) {
                return ChatClient.builder(openAiChatModel).build();
            }
            if (model instanceof OllamaChatModel ollamaChatModel) {
                return ChatClient.builder(ollamaChatModel).build();
            }
            throw new IllegalStateException("Unsupported chat model type: " + model.getClass().getName(), ex);
        }
    }
}
