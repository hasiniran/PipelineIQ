package com.hasini.pipelineiq;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


@SpringBootTest(properties = "spring.ai.chat.client.enabled=false")
class PipelineiqApplicationTests {

	@MockitoBean
	private ChatClient.Builder chatClientBuilder;

	@Test
	void contextLoads() {
	}

}
