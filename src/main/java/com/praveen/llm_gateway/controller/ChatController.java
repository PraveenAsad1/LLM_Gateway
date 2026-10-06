package com.praveen.llm_gateway.controller;

import com.praveen.llm_gateway.model.GatewayRequest;
import com.praveen.llm_gateway.model.GatewayResponse;
import com.praveen.llm_gateway.router.FailoverRouter;
import com.praveen.llm_gateway.ratelimit.RateLimiterService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final FailoverRouter failoverRouter;
    private final RateLimiterService rateLimiter;

    public ChatController(FailoverRouter failoverRouter, RateLimiterService rateLimiter) {
        this.failoverRouter = failoverRouter;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/chat")
    public GatewayResponse chat(@RequestBody GatewayRequest request) {
        rateLimiter.tryConsume(request.apiKey());
        return failoverRouter.route(request);
    }
}
