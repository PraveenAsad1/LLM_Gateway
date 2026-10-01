package com.praveen.llm_gateway.provider;

import com.praveen.llm_gateway.model.GatewayRequest;
import com.praveen.llm_gateway.model.GatewayResponse;

public interface LlmProvider {
    GatewayResponse call(GatewayRequest request);
}
