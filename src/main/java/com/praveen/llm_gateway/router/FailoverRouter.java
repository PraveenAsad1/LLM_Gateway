package com.praveen.llm_gateway.router;

import com.praveen.llm_gateway.model.GatewayRequest;
import com.praveen.llm_gateway.model.GatewayResponse;
import com.praveen.llm_gateway.provider.LlmProvider;
import com.praveen.llm_gateway.provider.ProviderUnavailableException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component

public class FailoverRouter {

    @Qualifier("primaryProvider")
    private final LlmProvider primary;

    @Qualifier("backupProvider")
    private final LlmProvider backup;



    @Autowired
    public FailoverRouter(@Qualifier("primaryProvider") LlmProvider primary,
                          @Qualifier("backupProvider") LlmProvider backup) {
        this.primary = primary;
        this.backup = backup;
    }
    public GatewayResponse route(GatewayRequest request) {
        try {
            return primary.call(request);
        } catch (ProviderUnavailableException primaryFailure) {
            System.out.println("[FailoverRouter] Primary failed: " + primaryFailure.getMessage()
                    + " — trying backup...");
            // If backup also throws, let it propagate — caller needs to know both are down
            return backup.call(request);
        }
    }
}
