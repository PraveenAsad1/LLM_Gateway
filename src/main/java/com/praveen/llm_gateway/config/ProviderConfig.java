package com.praveen.llm_gateway.config;

import com.praveen.llm_gateway.provider.AnthropicProvider;
import com.praveen.llm_gateway.provider.LlmProvider;
import com.praveen.llm_gateway.provider.MockLlmProvider;
import com.praveen.llm_gateway.provider.OpenAiProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Wires up the primary and backup {@link LlmProvider} beans.
 *
 * <p>If an API key is present the real HTTP provider is used; otherwise a safe
 * {@link MockLlmProvider} is registered and a warning is logged, so the application
 * starts correctly even without credentials configured.</p>
 */
@Configuration
public class ProviderConfig {

    private static final Logger log = LoggerFactory.getLogger(ProviderConfig.class);

    // ── OpenAI settings ────────────────────────────────────────────────────────
    @Value("${gateway.openai.api-key:}") private String openAiKey;
    @Value("${gateway.openai.base-url:https://api.openai.com/v1}") private String openAiBaseUrl;
    @Value("${gateway.openai.model:gpt-4o-mini}") private String openAiModel;
    @Value("${gateway.openai.timeout-seconds:30}") private int openAiTimeout;

    // ── Groq settings ──────────────────────────────────────────────────────────
    @Value("${gateway.groq.api-key:}") private String groqKey;
    @Value("${gateway.groq.base-url:https://api.groq.com/openai/v1}") private String groqBaseUrl;
    @Value("${gateway.groq.model:qwen/qwen3.8-27b}") private String groqModel;
    @Value("${gateway.groq.timeout-seconds:30}") private int groqTimeout;

    // ── Anthropic settings ─────────────────────────────────────────────────────
    @Value("${gateway.anthropic.api-key:}") private String anthropicKey;
    @Value("${gateway.anthropic.base-url:https://api.anthropic.com/v1}") private String anthropicBaseUrl;
    @Value("${gateway.anthropic.model:claude-3-haiku-20240307}") private String anthropicModel;
    @Value("${gateway.anthropic.timeout-seconds:30}") private int anthropicTimeout;
    @Value("${gateway.anthropic.version:2023-06-01}") private String anthropicVersion;

    @Bean
    public LlmProvider primaryProvider() {
        if (groqKey != null && !groqKey.isBlank()) {
            log.info("[ProviderConfig] Primary provider: Groq (model={})", groqModel);
            return new com.praveen.llm_gateway.provider.GroqProvider(groqBaseUrl, groqKey, groqModel,
                    Duration.ofSeconds(groqTimeout));
        }
        log.warn("[ProviderConfig] GROQ_API_KEY not set — primary provider uses mock");
        return new MockLlmProvider("groq-mock", false);
    }

    @Bean
    public LlmProvider backupProvider() {
        if (anthropicKey != null && !anthropicKey.isBlank()) {
            log.info("[ProviderConfig] Backup provider: Anthropic (model={})", anthropicModel);
            return new AnthropicProvider(anthropicBaseUrl, anthropicKey, anthropicModel,
                    anthropicVersion, Duration.ofSeconds(anthropicTimeout));
        }
        log.warn("[ProviderConfig] ANTHROPIC_API_KEY not set — backup provider uses mock");
        return new MockLlmProvider("anthropic-mock", false);
    }
}
