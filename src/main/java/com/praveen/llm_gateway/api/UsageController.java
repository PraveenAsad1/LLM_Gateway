package com.praveen.llm_gateway.api;

import com.praveen.llm_gateway.model.RequestLog;
import com.praveen.llm_gateway.repository.RequestLogRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usage")
public class UsageController {

    private final RequestLogRepository repository;

    public UsageController(RequestLogRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/{apiKey}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DEVELOPER') and principal.apiKey() == #apiKey)")
    public UsageSummary getUsageByApiKey(@PathVariable String apiKey) {
        List<RequestLog> logs = repository.findByApiKey(apiKey);
        
        long totalRequests = logs.size();
        long totalInputTokens = logs.stream().mapToLong(RequestLog::getInputTokens).sum();
        long totalOutputTokens = logs.stream().mapToLong(RequestLog::getOutputTokens).sum();
        double totalCostUsd = logs.stream().mapToDouble(RequestLog::getCostUsd).sum();
        
        return new UsageSummary(totalRequests, totalInputTokens, totalOutputTokens, totalCostUsd);
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public GlobalUsageSummary getGlobalSummary() {
        // NOTE: In-memory aggregation won't scale. A real production version would push this aggregation into SQL.
        List<RequestLog> allLogs = repository.findAll();
        
        long totalRequests = allLogs.size();
        double totalCostUsd = allLogs.stream().mapToDouble(RequestLog::getCostUsd).sum();
        
        Map<String, ProviderStats> providerBreakdown = allLogs.stream()
            .collect(Collectors.groupingBy(
                RequestLog::getProviderUsed,
                Collectors.collectingAndThen(Collectors.toList(), list -> {
                    long count = list.size();
                    double cost = list.stream().mapToDouble(RequestLog::getCostUsd).sum();
                    return new ProviderStats(count, cost);
                })
            ));
            
        return new GlobalUsageSummary(totalRequests, totalCostUsd, providerBreakdown);
    }

    public record UsageSummary(long totalRequests, long totalInputTokens, long totalOutputTokens, double totalCostUsd) {}
    public record ProviderStats(long count, double costUsd) {}
    public record GlobalUsageSummary(long totalRequests, double totalCostUsd, Map<String, ProviderStats> providerBreakdown) {}
}
