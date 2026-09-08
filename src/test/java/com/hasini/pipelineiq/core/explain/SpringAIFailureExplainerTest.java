package com.hasini.pipelineiq.core.explain;

import com.hasini.pipelineiq.core.model.FailureCategory;
import com.hasini.pipelineiq.core.model.FailureExplanation;
import com.hasini.pipelineiq.core.model.LogSnippet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.ai.chat.client.ChatClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

@ExtendWith(MockitoExtension.class)
class SpringAIFailureExplainerTest {

    @Mock
    ChatClient.Builder mockBuilder;

    ChatClient mockChat;
    SpringAIFailureExplainer explainer;

    @BeforeEach
    void setUp() {
        // create a deep-stub chat client and have the builder return it when build() is called
        mockChat = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        when(mockBuilder.build()).thenReturn(mockChat);
        explainer = new SpringAIFailureExplainer(mockBuilder);
    }

    @Test
    void nullSnippetShortCircuitsWithoutCallingApi() {
        FailureExplanation result = explainer.explain(null, FailureCategory.COMPILATION_ERROR);

        assertEquals(FailureExplanation.EMPTY, result);
        // Builder should not be used when short-circuiting
        verify(mockBuilder, never()).build();
        verifyNoInteractions(mockChat);
    }

    @Test
    void unknownCategoryShortCircuitsWithoutCallingApi() {
        LogSnippet snippet = new LogSnippet("Some failure details\nline2");
        FailureExplanation result = explainer.explain(snippet, FailureCategory.UNKNOWN);

        assertEquals(FailureExplanation.EMPTY, result);
        verify(mockBuilder, never()).build();
        verifyNoInteractions(mockChat);
    }

    @Test
    void validSnippetInvokesChatClientAndMapsToFailureExplanation() {
        LogSnippet snippet = new LogSnippet("Compilation error: cannot find symbol Foo");
        FailureExplanation expected = new FailureExplanation("Missing dependency: Foo class not found.", "Add the library that provides Foo to the build configuration.");

        when(mockChat.prompt().system(anyString()).user(anyString()).call().entity(FailureExplanation.class))
                .thenReturn(expected);

        FailureExplanation result = explainer.explain(snippet, FailureCategory.DEPENDENCY_RESOLUTION);

        assertEquals(expected, result);
        // Builder was used to create the chat client and the prompt was invoked
        verify(mockBuilder, times(1)).build();
        verify(mockChat, times(1)).prompt();
    }


}
