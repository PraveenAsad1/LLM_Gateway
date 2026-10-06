package scratch;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class TestDeserialization {
    record GroqResponse(List<Choice> choices, Usage usage) {}
    record Choice(Message message) {}
    record Message(String role, String content) {}
    record Usage(
            int promptTokens,
            int completionTokens,
            int totalTokens
    ) {}

    public static void main(String[] args) throws Exception {
        String json = """
        {"id":"chatcmpl-a79f596a-cdc3-4b0b-9e02-0cd4dda5506e","object":"chat.completion","created":1791297846,"model":"qwen/qwen3.8-27b","choices":[{"index":0,"message":{"role":"assistant","content":"Why don't scientists trust atoms?\\n\\nBecause they make up everything! 🧪✨"},"logprobs":null,"finish_reason":"stop"}],"usage":{"queue_time":0.0521354,"prompt_tokens":16,"prompt_time":0.00080814,"completion_tokens":19,"completion_time":0.036813737,"total_tokens":35,"total_time":0.037621877},"usage_breakdown":null,"system_fingerprint":"fp_21e59ac2de","x_groq":{"id":"req_01m48tqzdve5v8d97j7jgm40as","seed":1321871292},"service_tier":"on_demand"}
        """;
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        try {
            GroqResponse response = mapper.readValue(json, GroqResponse.class);
            System.out.println("Success! Choices: " + response.choices().size());
            System.out.println("Usage: " + response.usage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
