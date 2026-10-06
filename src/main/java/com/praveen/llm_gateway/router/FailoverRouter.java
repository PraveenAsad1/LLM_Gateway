package com.praveen.llm_gateway.router;

import com.praveen.llm_gateway.model.GatewayRequest;
import com.praveen.llm_gateway.model.GatewayResponse;
import com.praveen.llm_gateway.model.RequestLog;
import com.praveen.llm_gateway.provider.LlmProvider;
import com.praveen.llm_gateway.provider.ProviderUnavailableException;
import com.praveen.llm_gateway.service.CostTrackingService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class FailoverRouter {

    private final LlmProvider primary;
    private final LlmProvider backup;
    private final CircuitBreaker primaryCircuitBreaker;
    private final CircuitBreaker backupCircuitBreaker;
    private final CostTrackingService costTrackingService;

    public FailoverRouter(@Qualifier("primaryProvider") LlmProvider primary,
                          @Qualifier("backupProvider") LlmProvider backup,
                          CostTrackingService costTrackingService,
                          @Value("${gateway.circuitbreaker.failure-rate-threshold:50}") float failureRateThreshold,
                          @Value("${gateway.circuitbreaker.wait-duration-in-open-state-seconds:30}") long waitDuration,
                          @Value("${gateway.circuitbreaker.sliding-window-size:10}") int slidingWindowSize,
                          @Value("${gateway.circuitbreaker.permitted-calls-in-half-open-state:3}") int permittedCallsInHalfOpenState) {
        this.primary = primary;
        this.backup = backup;
        this.costTrackingService = costTrackingService;

        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(failureRateThreshold)
                .waitDurationInOpenState(Duration.ofSeconds(waitDuration))
                .slidingWindowSize(slidingWindowSize)
                .permittedNumberOfCallsInHalfOpenState(permittedCallsInHalfOpenState)
                .recordExceptions(ProviderUnavailableException.class)
                .build();

        this.primaryCircuitBreaker = CircuitBreaker.of("primaryProvider", config);
        this.backupCircuitBreaker = CircuitBreaker.of("backupProvider", config);
    }

    public GatewayResponse route(GatewayRequest request) {
        long requestStartTime = System.currentTimeMillis();
        long primaryStartTime = System.currentTimeMillis();
        try {
            GatewayResponse response = primaryCircuitBreaker.executeSupplier(() -> primary.call(request));
            long successfulCallLatencyMs = System.currentTimeMillis() - primaryStartTime;
            long totalLatencyMs = System.currentTimeMillis() - requestStartTime;
            double costUsd = calculateCost(response.promptTokens(), response.completionTokens());
            
            costTrackingService.logRequestAsync(new RequestLog(
                    request.apiKey(), response.providerUsed(), response.promptTokens(), response.completionTokens(), costUsd, successfulCallLatencyMs, totalLatencyMs, false
            ));
            
            return response;
        } catch (Exception primaryFailure) {
            System.out.println("[FailoverRouter] Primary failed or circuit open: " + primaryFailure.getMessage()
                    + " — trying backup...");
            long backupStartTime = System.currentTimeMillis();
            try {
                GatewayResponse backupResponse = backupCircuitBreaker.executeSupplier(() -> backup.call(request));
                long successfulCallLatencyMs = System.currentTimeMillis() - backupStartTime;
                long totalLatencyMs = System.currentTimeMillis() - requestStartTime;
                double costUsd = calculateCost(backupResponse.promptTokens(), backupResponse.completionTokens());
                
                costTrackingService.logRequestAsync(new RequestLog(
                        request.apiKey(), backupResponse.providerUsed(), backupResponse.promptTokens(), backupResponse.completionTokens(), costUsd, successfulCallLatencyMs, totalLatencyMs, true
                ));
                
                return backupResponse;
            } catch (Exception backupFailure) {
                if (backupFailure instanceof ProviderUnavailableException) {
                    throw (ProviderUnavailableException) backupFailure;
                }
                throw new ProviderUnavailableException("All providers unavailable: " + backupFailure.getMessage());
            }
        }
    }
    
    private double calculateCost(int inputTokens, int outputTokens) {
        // Simple dummy cost calculation for now
        return (inputTokens * 0.0001) + (outputTokens * 0.0002);
    }
}
