package com.praveen.llm_gateway.api.exception;

/**
 * Simple DTO that will be serialized as JSON for error responses.
 */
public record ErrorResponse(String error, String message) {}
