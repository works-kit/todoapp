package com.mohammadnuridin.todoapp.modules.todo.dto;

import com.mohammadnuridin.todoapp.modules.todo.domain.Priority;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record TodoFilterRequest(

        // Filter
        Boolean completed,
        Priority priority,
        String categoryId,
        String search, // fulltext search pada title & description

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueBefore,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueAfter,

        // Pagination
        Integer page, // default 0
        Integer size, // default 10

        // Sorting — field:direction, contoh: "createdAt:desc"
        String sort // default "createdAt:desc"
) {
    // Normalized defaults
    public int getPage() {
        return page != null && page >= 0 ? page : 0;
    }

    public int getSize() {
        return size != null && size > 0 && size <= 100 ? size : 10;
    }

    public String getSort() {
        return sort != null ? sort : "createdAt:desc";
    }
}