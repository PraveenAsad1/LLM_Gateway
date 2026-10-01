package com.praveen.llm_gateway.ratelimit;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple token bucket implementation for rate limiting.
 *
 * <p>It supports a fixed capacity and a refill rate expressed as tokens per second.
 * The bucket refills lazily on each {@code tryConsume()} call based on the elapsed time
 * since the last refill.</p>
 */
public class TokenBucket {
    private final long capacity;
    private final double refillTokensPerSecond;

    private final AtomicLong tokens;
    private volatile Instant lastRefill;

    /**
     * @param capacity maximum number of tokens the bucket can hold
     * @param refillTokensPerSecond how many tokens are added each second
     */
    public TokenBucket(long capacity, double refillTokensPerSecond) {
        this.capacity = capacity;
        this.refillTokensPerSecond = refillTokensPerSecond;
        this.tokens = new AtomicLong(capacity);
        this.lastRefill = Instant.now();
    }

    /**
     * Attempts to consume a single token.
     * @return {@code true} if a token was available and consumed, {@code false} otherwise
     */
    public synchronized boolean tryConsume() {
        refill();
        long current = tokens.get();
        if (current > 0) {
            tokens.decrementAndGet();
            return true;
        }
        return false;
    }

    /**
     * Refill the bucket based on elapsed time.
     */
    private void refill() {
        Instant now = Instant.now();
        long millisElapsed = Duration.between(lastRefill, now).toMillis();
        if (millisElapsed <= 0) {
            return;
        }
        double tokensToAdd = (millisElapsed / 1000.0) * refillTokensPerSecond;
        if (tokensToAdd >= 1) {
            long newTokenCount = Math.min(capacity, tokens.get() + (long) tokensToAdd);
            tokens.set(newTokenCount);
            lastRefill = now;
        }
    }
}
