package com.praveen.llm_gateway.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = com.praveen.llm_gateway.LlmGatewayApplication.class)
public class GroqProviderTest {
    
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testDeserialization() throws Exception {
        String json = "{\"id\":\"chatcmpl-a79f596a-cdc3-4b0b-9e02-0cd4dda5506e\",\"object\":\"chat.completion\",\"created\":1791297846,\"model\":\"qwen/qwen3.8-27b\",\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\",\"content\":\"Why don't scientists trust atoms?\\n\\nBecause they make up everything! 🧪✨\"},\"logprobs\":null,\"finish_reason\":\"stop\"}],\"usage\":{\"queue_time\":0.0521354,\"prompt_tokens\":16,\"prompt_time\":0.00080814,\"completion_tokens\":19,\"completion_time\":0.036813737,\"total_tokens\":35,\"total_time\":0.037621877},\"usage_breakdown\":null,\"system_fingerprint\":\"fp_21e59ac2de\",\"x_groq\":{\"id\":\"req_01m48tqzdve5v8d97j7jgm40as\",\"seed\":1321871292},\"service_tier\":\"on_demand\"}";
        try {
            GroqProvider.GroqResponse response = objectMapper.readValue(json, GroqProvider.GroqResponse.class);
            System.out.println("SUCCESS! choices: " + response.choices().size());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
