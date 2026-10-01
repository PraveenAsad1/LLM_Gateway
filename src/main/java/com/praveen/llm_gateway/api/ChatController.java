package com.praveen.llm_gateway.api;

import com.praveen.llm_gateway.model.GatewayRequest;
import com.praveen.llm_gateway.model.GatewayResponse;
import com.praveen.llm_gateway.ratelimit.RateLimiterService;
import com.praveen.llm_gateway.router.FailoverRouter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing the chat routing functionality.
 * Thin wrapper that enforces rate limiting and then delegates to {@link FailoverRouter}.
 */
@RestController
@RequestMapping("/api")
public class ChatController {

    private final FailoverRouter failoverRouter;
    private final RateLimiterService rateLimiter;

    @Autowired
    public ChatController(FailoverRouter failoverRouter, RateLimiterService rateLimiter) {
        this.failoverRouter = failoverRouter;
        this.rateLimiter    = rateLimiter;
    }

    @PostMapping("/chat")
    public GatewayResponse chat(@RequestBody GatewayRequest request) {
        // Throws RateLimitExceededException (→ 429) if bucket is empty
        rateLimiter.tryConsume();
        return failoverRouter.route(request);
    }
}
