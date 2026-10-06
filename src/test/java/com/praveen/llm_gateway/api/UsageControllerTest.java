package com.praveen.llm_gateway.api;

import com.praveen.llm_gateway.model.RequestLog;
import com.praveen.llm_gateway.repository.RequestLogRepository;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UsageControllerTest {

    @Test
    void testGetUsageByApiKey() {
        RequestLogRepository repo = mock(RequestLogRepository.class);
        UsageController controller = new UsageController(repo);
        
        when(repo.findByApiKey("empty-key")).thenReturn(Collections.emptyList());
        
        UsageController.UsageSummary emptySummary = controller.getUsageByApiKey("empty-key");
        assertEquals(0, emptySummary.totalRequests());
        assertEquals(0, emptySummary.totalInputTokens());
        assertEquals(0, emptySummary.totalOutputTokens());
        assertEquals(0.0, emptySummary.totalCostUsd(), 0.0001);
        
        RequestLog log1 = new RequestLog("key-1", "openai", 10, 20, 0.05, 100, 100, false);
        RequestLog log2 = new RequestLog("key-1", "openai", 5, 5, 0.01, 50, 50, false);
        when(repo.findByApiKey("key-1")).thenReturn(List.of(log1, log2));
        
        UsageController.UsageSummary summary = controller.getUsageByApiKey("key-1");
        assertEquals(2, summary.totalRequests());
        assertEquals(15, summary.totalInputTokens());
        assertEquals(25, summary.totalOutputTokens());
        assertEquals(0.06, summary.totalCostUsd(), 0.0001);
    }

    @Test
    void testGetGlobalSummary() {
        RequestLogRepository repo = mock(RequestLogRepository.class);
        UsageController controller = new UsageController(repo);
        
        RequestLog log1 = new RequestLog("key-1", "openai", 10, 20, 0.05, 100, 100, false);
        RequestLog log2 = new RequestLog("key-2", "anthropic", 5, 5, 0.01, 50, 50, false);
        
        when(repo.findAll()).thenReturn(List.of(log1, log2));
        
        UsageController.GlobalUsageSummary summary = controller.getGlobalSummary();
        assertEquals(2, summary.totalRequests());
        assertEquals(0.06, summary.totalCostUsd(), 0.0001);
        assertNotNull(summary.providerBreakdown());
        assertEquals(2, summary.providerBreakdown().size());
        assertEquals(1, summary.providerBreakdown().get("openai").count());
        assertEquals(0.05, summary.providerBreakdown().get("openai").costUsd(), 0.0001);
    }
}
