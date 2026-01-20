package com.app.securitydemo.dto;

import java.time.LocalDateTime;

public record UserResponse(
        String userId,
        String name,
        String email,
        String profileImageUrl,
        String googleId,
        String loginMethod,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
