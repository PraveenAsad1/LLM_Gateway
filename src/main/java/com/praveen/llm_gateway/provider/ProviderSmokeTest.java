package com.praveen.llm_gateway.provider;

import com.praveen.llm_gateway.model.GatewayRequest;
import com.praveen.llm_gateway.model.GatewayResponse;
import com.praveen.llm_gateway.router.FailoverRouter;
import com.praveen.llm_gateway.service.CostTrackingService;

public class ProviderSmokeTest {

    public static void main(String[] args) {
        GatewayRequest request = new GatewayRequest("test-key", "Hello!", "gpt-4");
        CostTrackingService dummyCostTrackingService = new CostTrackingService(null) {
            @Override
            public void logRequestAsync(com.praveen.llm_gateway.model.RequestLog log) {}
        };

        // --- Case 1: primary healthy, should return immediately ---
        System.out.println("=== Case 1: primary healthy ===");
        FailoverRouter router1 = new FailoverRouter(
                new MockLlmProvider("openai-mock", false),
                new MockLlmProvider("anthropic-mock", false),
                dummyCostTrackingService,
                50.0f, 30L, 10, 3
        );
        GatewayResponse r1 = router1.route(request);
        System.out.println("Response from: " + r1.providerUsed() + " | content: " + r1.content());

        System.out.println();

        // --- Case 2: primary fails, backup healthy — failover should kick in ---
        System.out.println("=== Case 2: primary fails, backup healthy ===");
        FailoverRouter router2 = new FailoverRouter(
                new MockLlmProvider("openai-mock", true),
                new MockLlmProvider("anthropic-mock", false),
                dummyCostTrackingService,
                50.0f, 30L, 10, 3
        );
        GatewayResponse r2 = router2.route(request);
        System.out.println("Response from: " + r2.providerUsed() + " | content: " + r2.content());

        System.out.println();

        // --- Case 3: both fail — exception must propagate ---
        System.out.println("=== Case 3: both fail ===");
        FailoverRouter router3 = new FailoverRouter(
                new MockLlmProvider("openai-mock", true),
                new MockLlmProvider("anthropic-mock", true),
                dummyCostTrackingService,
                50.0f, 30L, 10, 3
        );
        try {
            router3.route(request);
            System.out.println("BUG: expected exception but got a response!");
        } catch (RuntimeException e) {
            System.out.println("Correctly propagated: " + e.getMessage());
        }
    }
}
