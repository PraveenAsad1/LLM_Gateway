package com.praveen.llm_gateway.router;

import com.praveen.llm_gateway.model.GatewayRequest;
import com.praveen.llm_gateway.model.GatewayResponse;
import com.praveen.llm_gateway.provider.MockLlmProvider;
import com.praveen.llm_gateway.provider.ProviderUnavailableException;
import com.praveen.llm_gateway.service.CostTrackingService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FailoverRouterTest {

    private FailoverRouter createRouter(boolean primaryFails, boolean backupFails) {
        MockLlmProvider primary = new MockLlmProvider("primary", primaryFails);
        MockLlmProvider backup = new MockLlmProvider("backup", backupFails);
        CostTrackingService costTrackingService = Mockito.mock(CostTrackingService.class);
        
        return new FailoverRouter(primary, backup, costTrackingService, 50f, 30L, 10, 3);
    }

    @Test
    void primarySucceeds_returnsImmediately() {
        FailoverRouter router = createRouter(false, false);
        GatewayRequest request = new GatewayRequest("test-key", "hello", "gpt-4");
        
        GatewayResponse response = router.route(request);
        
        assertEquals("primary", response.providerUsed());
    }

    @Test
    void primaryFails_fallsBackToBackup() {
        FailoverRouter router = createRouter(true, false);
        GatewayRequest request = new GatewayRequest("test-key", "hello", "gpt-4");
        
        GatewayResponse response = router.route(request);
        
        assertEquals("backup", response.providerUsed());
    }

    @Test
    void bothFail_exceptionPropagates() {
        FailoverRouter router = createRouter(true, true);
        GatewayRequest request = new GatewayRequest("test-key", "hello", "gpt-4");
        
        assertThrows(ProviderUnavailableException.class, () -> router.route(request));
    }
}
