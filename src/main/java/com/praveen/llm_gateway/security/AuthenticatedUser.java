package com.praveen.llm_gateway.security;

public record AuthenticatedUser(String username, String role, String apiKey) {}
