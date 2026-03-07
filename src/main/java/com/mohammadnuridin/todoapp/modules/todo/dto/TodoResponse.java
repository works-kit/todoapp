package com.mohammadnuridin.todoapp.modules.todo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mohammadnuridin.todoapp.modules.category.dto.CategoryResponse;
import com.mohammadnuridin.todoapp.modules.todo.domain.Priority;
import com.mohammadnuridin.todoapp.modules.todo.domain.Todo;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record TodoResponse(

        String id,
        String title,
        String description,
        Boolean completed,
        Priority priority,

        @JsonProperty("due_date") LocalDateTime dueDate,

        Set<CategoryResponse> categories,

        @JsonProperty("created_at") LocalDateTime createdAt,

        @JsonProperty("updated_at") LocalDateTime updatedAt) {
    public static TodoResponse from(Todo todo) {
        return new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getDescription(),
                todo.getCompleted(),
                todo.getPriority(),
                todo.getDueDate(),
                todo.getCategories().stream()
                        .map(CategoryResponse::from)
                        .collect(Collectors.toSet()),
                todo.getCreatedAt(),
                todo.getUpdatedAt());
    }
}