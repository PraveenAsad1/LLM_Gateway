package com.praveen.llm_gateway.service;

import com.praveen.llm_gateway.model.RequestLog;
import com.praveen.llm_gateway.repository.RequestLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class CostTrackingService {

    private static final Logger log = LoggerFactory.getLogger(CostTrackingService.class);
    private final RequestLogRepository repository;

    public CostTrackingService(RequestLogRepository repository) {
        this.repository = repository;
    }

    // Cost logging is best-effort and must never block or fail the
    // primary chat response. Failures here are logged, not propagated.
    @Async
    public void logRequestAsync(RequestLog requestLog) {
        try {
            repository.save(requestLog);
        } catch (Exception e) {
            log.error("Failed to save request log for apiKey: {} - {}", requestLog.getApiKey(), e.getMessage());
        }
    }
}
