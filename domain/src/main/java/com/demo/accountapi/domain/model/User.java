package com.demo.accountapi.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class User {

    private final UUID id;
    private final String email;
    private String passwordHash;
    private final Role role;
    private final LocalDateTime createdAt;
    private boolean enabled;

    private User(Builder builder) {
        this.id = builder.id;
        this.email = builder.email;
        this.passwordHash = builder.passwordHash;
        this.role = builder.role;
        this.createdAt = builder.createdAt;
        this.enabled = builder.enabled;
    }

    public static User createNew(String email, String passwordHash, Role role) {
        return new Builder()
                .id(UUID.randomUUID())
                .email(email)
                .passwordHash(passwordHash)
                .role(role)
                .createdAt(LocalDateTime.now())
                .enabled(true)
                .build();
    }

    public boolean isAdmin() { return Role.ADMIN.equals(role); }

    // Getters
    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isEnabled() { return enabled; }

    public static class Builder {
        private UUID id;
        private String email;
        private String passwordHash;
        private Role role;
        private LocalDateTime createdAt;
        private boolean enabled = true;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder passwordHash(String passwordHash) { this.passwordHash = passwordHash; return this; }
        public Builder role(Role role) { this.role = role; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder enabled(boolean enabled) { this.enabled = enabled; return this; }
        public User build() { return new User(this); }
    }
}
