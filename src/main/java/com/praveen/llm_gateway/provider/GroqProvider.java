package com.praveen.llm_gateway.provider;

import com.praveen.llm_gateway.model.GatewayRequest;
import com.praveen.llm_gateway.model.GatewayResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * LlmProvider implementation that calls the real Groq API.
 *
 * <p>Groq is fully OpenAI-compatible, so this uses the exact same request/response
 * structure as OpenAI, just pointed to the Groq base URL.</p>
 */
public class GroqProvider implements LlmProvider {

    private static final Logger log = LoggerFactory.getLogger(GroqProvider.class);

    private final String apiKey;
    private final String model;
    private final RestClient restClient;

    public GroqProvider(String baseUrl, String apiKey, String model, Duration timeout) {
        this.apiKey = apiKey;
        this.model  = model;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public GatewayResponse call(GatewayRequest request) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ProviderUnavailableException("Groq API key not configured");
        }

        log.info("[Groq] Sending request for model={}", model);

        // Build the request payload (identical to OpenAI)
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(Map.of("role", "user", "content", request.prompt())),
                "max_tokens", 900
        );

        GroqResponse response;
        try {
            response = restClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .body(body)
                    .retrieve()
                    .body(GroqResponse.class);
        } catch (Exception ex) {
            log.error("[Groq] Request failed: {}", ex.getMessage());
            throw new ProviderUnavailableException("Groq request failed: " + ex.getMessage());
        }

        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new ProviderUnavailableException("Groq returned an empty response");
        }

        String content = response.choices().get(0).message().content();
        int promptTokens     = response.usage() != null ? response.usage().promptTokens()     : 0;
        int completionTokens = response.usage() != null ? response.usage().completionTokens() : 0;

        log.info("[Groq] Response received, promptTokens={}, completionTokens={}", promptTokens, completionTokens);
        return new GatewayResponse(content, "groq/" + model, promptTokens, completionTokens);
    }

    // ── Internal response records (Identical to OpenAI) ──────────────────────

    record GroqResponse(List<Choice> choices, Usage usage) {}

    record Choice(Message message) {}

    record Message(String role, String content) {}

    record Usage(
            @com.fasterxml.jackson.annotation.JsonProperty("prompt_tokens") int promptTokens,
            @com.fasterxml.jackson.annotation.JsonProperty("completion_tokens") int completionTokens,
            @com.fasterxml.jackson.annotation.JsonProperty("total_tokens") int totalTokens
    ) {}
}
