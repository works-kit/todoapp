package com.mohammadnuridin.todoapp.modules.user.domain;

import java.time.Instant;

import org.hibernate.annotations.Where;

import com.mohammadnuridin.todoapp.core.audit.BaseEntity;
import com.mohammadnuridin.todoapp.core.audit.BaseEntityUser;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Where(clause = "is_active = true AND deleted_at IS NULL") // ← Hibernate filter otomatis
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntityUser {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 10)
    @Builder.Default
    private Role role = Role.USER;

    @Column(name = "refresh_token", length = 512)
    private String refreshToken;

    @Column(name = "refresh_token_expired_at")
    private Long refreshTokenExpiredAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}