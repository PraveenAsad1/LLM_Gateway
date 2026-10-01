package com.praveen.llm_gateway.model;

public record GatewayRequest(String apiKey, String prompt, String model) {
}
