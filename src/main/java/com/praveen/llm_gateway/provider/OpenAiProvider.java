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
 * LlmProvider implementation that calls the real OpenAI Chat Completions API.
 *
 * <p>Uses Spring's {@link RestClient} (available since Spring 6.1 / Boot 3.2).
 * If the API key is blank the provider immediately throws
 * {@link ProviderUnavailableException} so the failover router can try backup.</p>
 */
public class OpenAiProvider implements LlmProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenAiProvider.class);

    private final String apiKey;
    private final String model;
    private final RestClient restClient;

    public OpenAiProvider(String baseUrl, String apiKey, String model, Duration timeout) {
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
            throw new ProviderUnavailableException("OpenAI API key not configured");
        }

        log.info("[OpenAI] Sending request for model={}", model);

        // Build the request payload
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(Map.of("role", "user", "content", request.prompt()))
        );

        OpenAiResponse response;
        try {
            response = restClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .body(body)
                    .retrieve()
                    .body(OpenAiResponse.class);
        } catch (Exception ex) {
            log.error("[OpenAI] Request failed: {}", ex.getMessage());
            throw new ProviderUnavailableException("OpenAI request failed: " + ex.getMessage());
        }

        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new ProviderUnavailableException("OpenAI returned an empty response");
        }

        String content = response.choices().get(0).message().content();
        int promptTokens     = response.usage() != null ? response.usage().promptTokens()     : 0;
        int completionTokens = response.usage() != null ? response.usage().completionTokens() : 0;

        log.info("[OpenAI] Response received, promptTokens={}, completionTokens={}", promptTokens, completionTokens);
        return new GatewayResponse(content, "openai/" + model, promptTokens, completionTokens);
    }

    // ── Internal response records ─────────────────────────────────────────────

    record OpenAiResponse(List<Choice> choices, Usage usage) {}

    record Choice(Message message) {}

    record Message(String role, String content) {}

    record Usage(
            int promptTokens,
            int completionTokens,
            int totalTokens
    ) {}
}
