package com.app.securitydemo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity(name = "users")
public class User {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(length = 1000)
    private String profileImageUrl;

    @Column(unique = true)
    private String googleId;

    @Column(name = "login_method", nullable = false)
    private String loginMethod = "LOCAL";

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
//    private String role;
}
