package com.praveen.llm_gateway.model;

public record GatewayResponse(String content, String providerUsed, int promptTokens, int completionTokens) {
    
}
