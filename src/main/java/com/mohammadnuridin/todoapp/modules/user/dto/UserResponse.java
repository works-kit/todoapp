package com.mohammadnuridin.todoapp.modules.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mohammadnuridin.todoapp.modules.user.domain.Role;
import com.mohammadnuridin.todoapp.modules.user.domain.User;

import java.time.LocalDateTime;

public record UserResponse(

        String id,
        String name,
        String email,
        Role role,

        @JsonProperty("is_active") Boolean isActive,

        @JsonProperty("created_at") LocalDateTime createdAt,

        @JsonProperty("updated_at") LocalDateTime updatedAt) {
    // Factory dari entity — hindari MapStruct untuk DTO sederhana ini
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getIsActive(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}