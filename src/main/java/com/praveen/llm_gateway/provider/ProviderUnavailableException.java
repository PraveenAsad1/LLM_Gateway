package com.praveen.llm_gateway.provider;

public class ProviderUnavailableException extends RuntimeException {
    public ProviderUnavailableException(String message) {
        super(message);
    }
}
