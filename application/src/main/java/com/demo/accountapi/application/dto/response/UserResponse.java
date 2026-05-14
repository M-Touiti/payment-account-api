package com.demo.accountapi.application.dto.response;

import com.demo.accountapi.domain.model.User;
import java.time.LocalDateTime; import java.util.UUID;

public record UserResponse(UUID id, String email, String role, LocalDateTime createdAt) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getRole().name(), u.getCreatedAt());
    }
}
