package com.praveen.llm_gateway.ratelimit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spring-managed service that wraps a {@link TokenBucket} and exposes a single
 * {@link #tryConsume(String)} method for the rest of the application.
 *
 * <p>Capacity and refill rate are driven by {@code application.properties} so
 * they can be tuned without recompilation.</p>
 */
@Service
public class RateLimiterService {

    // In-memory, per-instance only — a second gateway instance would not share
    // rate-limit state across instances. Redis-backed buckets would be required
    // for horizontal scaling; out of scope here.
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final long capacity;
    private final double refillPerSecond;

    public RateLimiterService(
            @Value("${gateway.ratelimit.capacity:20}") long capacity,
            @Value("${gateway.ratelimit.refill-per-second:5}") double refillPerSecond) {
        this.capacity = capacity;
        this.refillPerSecond = refillPerSecond;
    }

    /**
     * Attempt to consume one token from the bucket.
     *
     * @throws RateLimitExceededException if no tokens are available
     */
    public void tryConsume(String apiKey) {
        TokenBucket bucket = buckets.computeIfAbsent(apiKey, k -> new TokenBucket(capacity, refillPerSecond));
        if (!bucket.tryConsume()) {
            throw new RateLimitExceededException();
        }
    }
}
