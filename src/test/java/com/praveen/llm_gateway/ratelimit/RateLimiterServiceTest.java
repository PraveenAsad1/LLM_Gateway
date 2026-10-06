package com.praveen.llm_gateway.ratelimit;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RateLimiterServiceTest {

    @Test
    void testIndependentRateLimitsPerApiKey() {
        // Set capacity to 2 so we can easily exhaust it
        RateLimiterService service = new RateLimiterService(2, 0); // 0 refill for predictable tests

        // Key A uses all its tokens
        service.tryConsume("key-A");
        service.tryConsume("key-A");
        
        // Key A is now exhausted
        assertThrows(RateLimitExceededException.class, () -> service.tryConsume("key-A"));

        // Key B should still have its full bucket (2 tokens)
        assertDoesNotThrow(() -> service.tryConsume("key-B"));
        assertDoesNotThrow(() -> service.tryConsume("key-B"));
        
        // Key B is now exhausted
        assertThrows(RateLimitExceededException.class, () -> service.tryConsume("key-B"));
    }
}
