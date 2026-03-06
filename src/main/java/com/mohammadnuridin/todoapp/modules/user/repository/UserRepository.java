package com.mohammadnuridin.todoapp.modules.user.repository;

import com.mohammadnuridin.todoapp.modules.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByRefreshToken(String refreshToken);

    boolean existsByEmail(String email);
}
