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
 * LlmProvider implementation that calls the real Anthropic Messages API.
 *
 * <p>Uses Spring's {@link RestClient}. Falls back via {@link ProviderUnavailableException}
 * when the API key is absent so the {@link com.praveen.llm_gateway.router.FailoverRouter}
 * can automatically try the next provider.</p>
 */
public class AnthropicProvider implements LlmProvider {

    private static final Logger log = LoggerFactory.getLogger(AnthropicProvider.class);

    private final String apiKey;
    private final String model;
    private final String anthropicVersion;
    private final RestClient restClient;

    public AnthropicProvider(String baseUrl, String apiKey, String model,
                             String anthropicVersion, Duration timeout) {
        this.apiKey           = apiKey;
        this.model            = model;
        this.anthropicVersion = anthropicVersion;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public GatewayResponse call(GatewayRequest request) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ProviderUnavailableException("Anthropic API key not configured");
        }

        log.info("[Anthropic] Sending request for model={}", model);

        Map<String, Object> body = Map.of(
                "model", model,
                "max_tokens", 1024,
                "messages", List.of(Map.of("role", "user", "content", request.prompt()))
        );

        AnthropicResponse response;
        try {
            response = restClient.post()
                    .uri("/messages")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", anthropicVersion)
                    .body(body)
                    .retrieve()
                    .body(AnthropicResponse.class);
        } catch (Exception ex) {
            log.error("[Anthropic] Request failed: {}", ex.getMessage());
            throw new ProviderUnavailableException("Anthropic request failed: " + ex.getMessage());
        }

        if (response == null || response.content() == null || response.content().isEmpty()) {
            throw new ProviderUnavailableException("Anthropic returned an empty response");
        }

        String content       = response.content().get(0).text();
        int inputTokens      = response.usage() != null ? response.usage().inputTokens()  : 0;
        int outputTokens     = response.usage() != null ? response.usage().outputTokens() : 0;

        log.info("[Anthropic] Response received, inputTokens={}, outputTokens={}", inputTokens, outputTokens);
        return new GatewayResponse(content, "anthropic/" + model, inputTokens, outputTokens);
    }

    // ── Internal response records ─────────────────────────────────────────────

    record AnthropicResponse(List<ContentBlock> content, Usage usage) {}

    record ContentBlock(String type, String text) {}

    record Usage(int inputTokens, int outputTokens) {}
}
