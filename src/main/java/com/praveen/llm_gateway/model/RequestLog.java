package com.praveen.llm_gateway.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;

import java.time.Instant;
import java.util.UUID;

@Entity
public class RequestLog {
    @Id
    @GeneratedValue
    private UUID id;

    private String apiKey;
    private String providerUsed;
    private int inputTokens;
    private int outputTokens;
    private double costUsd;
    private long successfulCallLatencyMs;
    private long totalLatencyMs;
    private boolean wasFailover;
    private Instant createdAt;

    public RequestLog() {
    }

    public RequestLog(String apiKey, String providerUsed, int inputTokens, int outputTokens, double costUsd, long successfulCallLatencyMs, long totalLatencyMs, boolean wasFailover) {
        this.apiKey = apiKey;
        this.providerUsed = providerUsed;
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
        this.costUsd = costUsd;
        this.successfulCallLatencyMs = successfulCallLatencyMs;
        this.totalLatencyMs = totalLatencyMs;
        this.wasFailover = wasFailover;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getApiKey() { return apiKey; }
    public String getProviderUsed() { return providerUsed; }
    public int getInputTokens() { return inputTokens; }
    public int getOutputTokens() { return outputTokens; }
    public double getCostUsd() { return costUsd; }
    public long getSuccessfulCallLatencyMs() { return successfulCallLatencyMs; }
    public long getTotalLatencyMs() { return totalLatencyMs; }
    public boolean isWasFailover() { return wasFailover; }
    public Instant getCreatedAt() { return createdAt; }
    
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public void setProviderUsed(String providerUsed) { this.providerUsed = providerUsed; }
    public void setInputTokens(int inputTokens) { this.inputTokens = inputTokens; }
    public void setOutputTokens(int outputTokens) { this.outputTokens = outputTokens; }
    public void setCostUsd(double costUsd) { this.costUsd = costUsd; }
    public void setSuccessfulCallLatencyMs(long successfulCallLatencyMs) { this.successfulCallLatencyMs = successfulCallLatencyMs; }
    public void setTotalLatencyMs(long totalLatencyMs) { this.totalLatencyMs = totalLatencyMs; }
    public void setWasFailover(boolean wasFailover) { this.wasFailover = wasFailover; }
}
