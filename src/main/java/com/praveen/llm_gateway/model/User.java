package com.praveen.llm_gateway.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String passwordHash;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    
    @Column(unique = true, nullable = false)
    private String apiKey;

    public User() {}

    public User(String username, String passwordHash, Role role, String apiKey) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.apiKey = apiKey;
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Role getRole() { return role; }
    public String getApiKey() { return apiKey; }
}
