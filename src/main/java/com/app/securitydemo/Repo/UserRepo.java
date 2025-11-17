package com.app.securitydemo.Repo;

import com.app.securitydemo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepo extends JpaRepository<User, UUID> {
    Optional<User> findUserByName(String username);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Override
    boolean existsById(UUID uuid);
}
