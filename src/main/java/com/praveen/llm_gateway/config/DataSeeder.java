package com.praveen.llm_gateway.config;

import com.praveen.llm_gateway.model.Role;
import com.praveen.llm_gateway.model.User;
import com.praveen.llm_gateway.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

// Demo-only seeding for a portfolio project with no public registration flow. 
// A real system would have a proper signup/invite process, not hardcoded demo accounts.
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByUsername("admin").isEmpty()) {
            String adminApiKey = "admin-key-" + UUID.randomUUID().toString().substring(0, 8);
            User admin = new User("admin", passwordEncoder.encode("admin123"), Role.ADMIN, adminApiKey);
            userRepository.save(admin);
            System.out.println("Seeded admin user — password: admin123 (DEV/DEMO ONLY) — apiKey: " + adminApiKey);
        } else {
            User admin = userRepository.findByUsername("admin").get();
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            userRepository.save(admin);
            System.out.println("Admin password reset to: admin123");
        }

        if (userRepository.findByUsername("developer").isEmpty()) {
            String devApiKey = "dev-key-" + UUID.randomUUID().toString().substring(0, 8);
            User dev = new User("developer", passwordEncoder.encode("dev123"), Role.DEVELOPER, devApiKey);
            userRepository.save(dev);
            System.out.println("Seeded developer user — password: dev123 (DEV/DEMO ONLY) — apiKey: " + devApiKey);
        } else {
            User dev = userRepository.findByUsername("developer").get();
            dev.setPasswordHash(passwordEncoder.encode("dev123"));
            userRepository.save(dev);
            System.out.println("Developer password reset to: dev123");
        }
    }
}
