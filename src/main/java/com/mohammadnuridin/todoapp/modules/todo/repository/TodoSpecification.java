package com.mohammadnuridin.todoapp.modules.todo.repository;

import com.mohammadnuridin.todoapp.modules.todo.domain.Todo;
import com.mohammadnuridin.todoapp.modules.todo.dto.TodoFilterRequest;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class TodoSpecification {

    private TodoSpecification() {
    }

    public static Specification<Todo> filter(String userId, TodoFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // ── Wajib: hanya todo milik user ini ─────────────
            predicates.add(cb.equal(root.get("user").get("id"), userId));

            // ── Filter: completed ─────────────────────────────
            if (filter.completed() != null) {
                predicates.add(cb.equal(root.get("completed"), filter.completed()));
            }

            // ── Filter: priority ──────────────────────────────
            if (filter.priority() != null) {
                predicates.add(cb.equal(root.get("priority"), filter.priority()));
            }

            // ── Filter: category ──────────────────────────────
            if (filter.categoryId() != null && !filter.categoryId().isBlank()) {
                Join<Object, Object> categories = root.join("categories", JoinType.INNER);
                predicates.add(cb.equal(categories.get("id"), filter.categoryId()));
            }

            // ── Filter: due date range ────────────────────────
            if (filter.dueBefore() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("dueDate"), filter.dueBefore()));
            }
            if (filter.dueAfter() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("dueDate"), filter.dueAfter()));
            }

            // ── Filter: search (title / description) ─────────
            if (filter.search() != null && !filter.search().isBlank()) {
                String pattern = "%" + filter.search().toLowerCase() + "%";
                Predicate titleMatch = cb.like(
                        cb.lower(root.get("title")), pattern);
                Predicate descMatch = cb.like(
                        cb.lower(root.get("description")), pattern);
                predicates.add(cb.or(titleMatch, descMatch));
            }

            // ── Distinct — karena ada join ke categories ──────
            if (query != null)
                query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}