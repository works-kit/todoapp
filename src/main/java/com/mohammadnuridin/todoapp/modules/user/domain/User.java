package com.mohammadnuridin.todoapp.modules.user.domain;

import com.mohammadnuridin.todoapp.core.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

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

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
