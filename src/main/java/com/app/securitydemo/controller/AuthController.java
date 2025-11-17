package com.app.securitydemo.controller;

import com.app.securitydemo.Service.JwtService;
import com.app.securitydemo.Service.UserService;
import com.app.securitydemo.dto.LoginRequest;
import com.app.securitydemo.dto.RegisterRequest;
import com.app.securitydemo.model.MyUserDetails;
import com.app.securitydemo.model.User;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    AuthController(UserService userService, JwtService jwtService, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        System.out.println("Attempting to authenticate user: " + loginRequest.email());
        try {
            Authentication authenticationRequest =
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.email(),
                            loginRequest.password()
                    );

            Authentication authenticationResponse =
                    this.authenticationManager.authenticate(authenticationRequest);

            MyUserDetails myUserDetails = (MyUserDetails) authenticationResponse.getPrincipal();
            User user = myUserDetails.getUser();

            String jwtToken = jwtService.generateToken(authenticationResponse.getName());

            System.out.println("User " + user + " authenticated successfully.");

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwtToken);
            response.put("user", userService.getUserResponse(user));
            response.put("message", "Login successful");
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (Exception e) {
            System.out.println("Authentication failed for user: " + loginRequest.email() + " Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        User user;
        try {
            String jwtToken = jwtService.generateToken(registerRequest.email());
            if (userService.findByEmail(registerRequest.email()).isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User already exists with email: " + registerRequest.email());
            }
            user = userService.createUser(registerRequest.email(), registerRequest.password(), registerRequest.name());

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwtToken);
            response.put("user", userService.getUserResponse(user));
            response.put("message", "Registration successful");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            System.out.println("Registration failed: " + e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/oauth2/success")
    public ResponseEntity<?> googleRegisterAndLogin(OAuth2AuthenticationToken oAuth2AuthenticationToken) {
        try {
            OAuth2User oAuth2User = oAuth2AuthenticationToken.getPrincipal();
            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            String userProfileImageUrl = oAuth2User.getAttribute("picture");
            String googleId = oAuth2User.getAttribute("sub");


            User user = userService.findByEmail(email).orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setName(name);
                newUser.setPassword(passwordEncoder.encode("123456"));
                newUser.setGoogleId(googleId);
                newUser.setProfileImageUrl(userProfileImageUrl);
                return userService.createOauthUser(newUser);
            });

            user.setGoogleId(googleId);
            user.setUpdatedAt(LocalDateTime.now());
            user = userService.updateUser(user);

            Map<String, Object> response = new HashMap<>();
            String jwtToken = jwtService.generateToken(email);
            response.put("token", jwtToken);
            response.put("user", userService.getUserResponse(user));
            response.put("message", "Login successful via Google");

            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (Exception e) {
            System.out.println("Google login failed: " + e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/google/login")
    public void redirectToGoogle(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/google");
    }
}
