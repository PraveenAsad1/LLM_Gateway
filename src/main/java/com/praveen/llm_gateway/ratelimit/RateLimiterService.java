package com.praveen.llm_gateway.ratelimit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Spring-managed service that wraps a {@link TokenBucket} and exposes a single
 * {@link #tryConsume()} method for the rest of the application.
 *
 * <p>Capacity and refill rate are driven by {@code application.properties} so
 * they can be tuned without recompilation.</p>
 */
@Service
public class RateLimiterService {

    private final TokenBucket bucket;

    public RateLimiterService(
            @Value("${gateway.ratelimit.capacity:20}") long capacity,
            @Value("${gateway.ratelimit.refill-per-second:5}") double refillPerSecond) {
        this.bucket = new TokenBucket(capacity, refillPerSecond);
    }

    /**
     * Attempt to consume one token from the bucket.
     *
     * @throws RateLimitExceededException if no tokens are available
     */
    public void tryConsume() {
        if (!bucket.tryConsume()) {
            throw new RateLimitExceededException();
        }
    }
}
