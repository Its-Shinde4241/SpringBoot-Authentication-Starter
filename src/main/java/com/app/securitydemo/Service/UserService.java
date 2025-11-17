package com.app.securitydemo.Service;

import com.app.securitydemo.dto.UserResponse;
import com.app.securitydemo.model.MyUserDetails;
import com.app.securitydemo.Repo.UserRepo;
import com.app.securitydemo.model.User;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;


@Service
@Transactional
public class UserService implements UserDetailsService {


    private final UserRepo userRepo;

    private final PasswordEncoder passwordEncoder;

    UserService(PasswordEncoder passwordEncoder, UserRepo userRepo) {
        this.passwordEncoder = passwordEncoder;
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return new MyUserDetails(user);
    }

    public User createUser(String email, String password, String name) {
        if (userRepo.existsByEmail(email)) {
            throw new RuntimeException("User with email " + email + " already exists.");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        String encodedPassword = passwordEncoder.encode(password);
        user.setPassword(encodedPassword);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setLoginMethod("LOCAL");

        return userRepo.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    public User updateUser(User user) {
        return userRepo.save(user);
    }

    public User createOauthUser(User user) {
        user.setPassword(null);
        user.setLoginMethod("GOOGLE");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepo.save(user);
    }

    public UserResponse getUserResponse(User user) {
        return new UserResponse(
                user.getName(),
                user.getEmail(),
                user.getProfileImageUrl(),
                user.getGoogleId(),
                user.getLoginMethod(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
