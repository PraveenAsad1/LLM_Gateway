package com.praveen.llm_gateway.service;

import com.praveen.llm_gateway.model.RequestLog;
import com.praveen.llm_gateway.repository.RequestLogRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CostTrackingServiceTest {
    @Test
    void testLogRequestAsyncHandlesExceptionWithoutThrowing() {
        RequestLogRepository repo = mock(RequestLogRepository.class);
        doThrow(new RuntimeException("DB down")).when(repo).save(any(RequestLog.class));
        
        CostTrackingService service = new CostTrackingService(repo);
        
        RequestLog log = new RequestLog("test-key", "provider", 10, 10, 0.01, 100, 100, false);
        
        assertDoesNotThrow(() -> service.logRequestAsync(log));
        verify(repo).save(log);
    }
}
