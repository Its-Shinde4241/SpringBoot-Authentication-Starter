package com.app.securitydemo.dto;

public record RegisterRequest(
        String name,
        String password,
        String email
) {
}
