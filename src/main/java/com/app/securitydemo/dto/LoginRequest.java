package com.app.securitydemo.dto;

public record LoginRequest(
        String email,
        String password
) {
}
