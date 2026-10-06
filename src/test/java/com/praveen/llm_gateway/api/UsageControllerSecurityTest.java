package com.praveen.llm_gateway.api;

import com.praveen.llm_gateway.repository.RequestLogRepository;
import com.praveen.llm_gateway.security.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class UsageControllerSecurityTest {

    @Autowired
    private UsageController controller;

    @MockitoBean
    private RequestLogRepository repository;

    private void setSecurityContext(String username, String role, String apiKey) {
        AuthenticatedUser user = new AuthenticatedUser(username, role, apiKey);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void devCanAccessOwnKey() {
        when(repository.findByApiKey(anyString())).thenReturn(Collections.emptyList());
        setSecurityContext("dev", "DEVELOPER", "my-key");
        assertDoesNotThrow(() -> controller.getUsageByApiKey("my-key"));
    }

    @Test
    void devCannotAccessOtherKey() {
        setSecurityContext("dev", "DEVELOPER", "my-key");
        assertThrows(AccessDeniedException.class, () -> controller.getUsageByApiKey("other-key"));
    }

    @Test
    void adminCanAccessAnyKey() {
        when(repository.findByApiKey(anyString())).thenReturn(Collections.emptyList());
        setSecurityContext("admin", "ADMIN", "admin-key");
        assertDoesNotThrow(() -> controller.getUsageByApiKey("other-key"));
    }

    @Test
    void devCannotAccessSummary() {
        setSecurityContext("dev", "DEVELOPER", "my-key");
        assertThrows(AccessDeniedException.class, () -> controller.getGlobalSummary());
    }
}
