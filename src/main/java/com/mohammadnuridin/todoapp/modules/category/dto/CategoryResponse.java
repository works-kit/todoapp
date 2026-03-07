package com.mohammadnuridin.todoapp.modules.category.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mohammadnuridin.todoapp.modules.category.domain.Category;

import java.time.LocalDateTime;

public record CategoryResponse(

                String id,
                String name,
                String description,
                String color,

                @JsonProperty("is_default") Boolean isDefault,

                @JsonProperty("created_at") LocalDateTime createdAt,

                @JsonProperty("updated_at") LocalDateTime updatedAt) {
        public static CategoryResponse from(Category category) {
                return new CategoryResponse(
                                category.getId(),
                                category.getName(),
                                category.getDescription(),
                                category.getColor(),
                                category.getIsDefault(),
                                category.getCreatedAt(),
                                category.getUpdatedAt());
        }
}