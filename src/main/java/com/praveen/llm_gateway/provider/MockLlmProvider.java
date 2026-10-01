package com.praveen.llm_gateway.provider;

import com.praveen.llm_gateway.model.GatewayRequest;
import com.praveen.llm_gateway.model.GatewayResponse;

public class MockLlmProvider implements LlmProvider {

    private final String name;
    private final boolean shouldFail;

    public MockLlmProvider(String name, boolean shouldFail) {
        this.name = name;
        this.shouldFail = shouldFail;
    }

    @Override
    public GatewayResponse call(GatewayRequest request) {
        if (shouldFail) {
            throw new ProviderUnavailableException("Provider " + name + " is down");
        }
        return new GatewayResponse("mock response", name, 10, 20);
    }
}
