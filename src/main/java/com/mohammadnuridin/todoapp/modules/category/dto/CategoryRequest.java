package com.mohammadnuridin.todoapp.modules.category.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoryRequest(

        @NotBlank(message = "{category.name.not_blank}") @Size(min = 1, max = 100, message = "{category.name.size}") String name,

        @Size(max = 255, message = "{todo.description.size}") String description,

        @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "{category.color.pattern}") String color,

        @JsonProperty("is_default") Boolean isDefault) {
}