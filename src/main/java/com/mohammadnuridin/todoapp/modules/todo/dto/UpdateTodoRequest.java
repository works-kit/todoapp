package com.mohammadnuridin.todoapp.modules.todo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mohammadnuridin.todoapp.modules.todo.domain.Priority;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Set;

public record UpdateTodoRequest(

        @Size(min = 1, max = 255, message = "{todo.title.size}") String title,

        @Size(max = 5000, message = "{todo.description.size}") String description,

        @JsonProperty("due_date") LocalDateTime dueDate,

        Priority priority,

        Boolean completed,

        @JsonProperty("category_ids") Set<String> categoryIds) {
}