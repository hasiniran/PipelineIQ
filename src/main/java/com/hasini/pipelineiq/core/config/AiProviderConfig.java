package com.hasini.pipelineiq.core.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AiProviderConfig {

    @Bean
    @Primary
    @ConditionalOnProperty(name = "spring.ai.provider", havingValue = "openai", matchIfMissing = true)
    public ChatClient.Builder openAiChatClientBuilder(OpenAiChatModel openAiChatModel) {
        return ChatClient.builder(openAiChatModel);
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "spring.ai.provider", havingValue = "ollama")
    public ChatClient.Builder ollamaChatClientBuilder(OllamaChatModel ollamaChatModel) {
        return ChatClient.builder(ollamaChatModel);
    }
}