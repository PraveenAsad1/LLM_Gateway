package com.praveen.llm_gateway.api;

import com.praveen.llm_gateway.model.Role;
import com.praveen.llm_gateway.model.User;
import com.praveen.llm_gateway.repository.UserRepository;
import com.praveen.llm_gateway.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private AuthController authController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void testLoginSuccess() {
        userRepository.save(new User("dev", passwordEncoder.encode("dev123"), Role.DEVELOPER, "dev-key"));

        AuthController.LoginRequest request = new AuthController.LoginRequest("dev", "dev123");
        ResponseEntity<?> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testLoginFailure() {
        AuthController.LoginRequest request = new AuthController.LoginRequest("wrong", "dev123");
        ResponseEntity<?> response = authController.login(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
