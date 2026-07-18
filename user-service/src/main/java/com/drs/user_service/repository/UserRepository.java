package com.drs.user_service.repository;

import com.drs.user_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User , Long> {

    Optional<User> findByApiKey(String apiKey);
    Optional<User> findByEmail(String email);
}
