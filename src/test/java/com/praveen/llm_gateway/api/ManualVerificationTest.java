package com.praveen.llm_gateway.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.praveen.llm_gateway.model.Role;
import com.praveen.llm_gateway.model.User;
import com.praveen.llm_gateway.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ManualVerificationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void runManualVerification() throws Exception {
        userRepository.save(new User("devuser", passwordEncoder.encode("devpass"), Role.DEVELOPER, "dev-key"));
        userRepository.save(new User("adminuser", passwordEncoder.encode("adminpass"), Role.ADMIN, "admin-key"));

        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        // 1. Login as dev
        String loginBody = "{\"username\":\"devuser\",\"password\":\"devpass\"}";
        HttpRequest loginReq = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(loginBody))
                .build();
        
        HttpResponse<String> loginRes = client.send(loginReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, loginRes.statusCode());
        JsonNode node = mapper.readTree(loginRes.body());
        String token = node.get("token").asText();

        // 2. Dev queries own key -> should be 200
        HttpRequest ownKeyReq = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/usage/dev-key"))
                .header("Authorization", "Bearer " + token)
                .GET().build();
        assertEquals(200, client.send(ownKeyReq, HttpResponse.BodyHandlers.ofString()).statusCode());

        // 3. Dev queries other key -> should be 403
        HttpRequest otherKeyReq = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/usage/admin-key"))
                .header("Authorization", "Bearer " + token)
                .GET().build();
        HttpResponse<String> otherKeyRes = client.send(otherKeyReq, HttpResponse.BodyHandlers.ofString());
        System.out.println("STATUS 403 EXPECTED, GOT: " + otherKeyRes.statusCode());
        System.out.println("BODY: " + otherKeyRes.body());
        assertEquals(403, otherKeyRes.statusCode());

        // 4. Dev queries summary -> should be 403
        HttpRequest summaryReq = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/usage/summary"))
                .header("Authorization", "Bearer " + token)
                .GET().build();
        assertEquals(403, client.send(summaryReq, HttpResponse.BodyHandlers.ofString()).statusCode());
    }
}
