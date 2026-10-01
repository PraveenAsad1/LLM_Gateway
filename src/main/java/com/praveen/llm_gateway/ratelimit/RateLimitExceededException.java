package com.praveen.llm_gateway.ratelimit;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Exception thrown when a request exceeds the allowed rate limit.
 * It is handled globally to produce a 429 Too Many Requests response.
 */
public class RateLimitExceededException extends ResponseStatusException {
    public RateLimitExceededException() {
        super(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded");
    }
}
