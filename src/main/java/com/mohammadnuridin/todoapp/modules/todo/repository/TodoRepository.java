package com.mohammadnuridin.todoapp.modules.todo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.mohammadnuridin.todoapp.modules.todo.domain.Todo;

@Repository
public interface TodoRepository extends JpaRepository<Todo, String>,
                JpaSpecificationExecutor<Todo> {

        // Ownership check sekaligus fetch
        Optional<Todo> findByIdAndUserId(String id, String userId);

        // Semua todo milik user (tanpa filter) — diurutkan by createdAt desc
        @EntityGraph(attributePaths = { "categories", "user" })
        List<Todo> findByUserIdAndIsDeletedFalse(String userId, Sort sort);

        // Count todo milik user (untuk stats)
        long countByUserId(String userId);

        // Count todo completed milik user
        long countByUserIdAndCompleted(String userId, boolean completed);

        Optional<Todo> findByIdAndUserIdAndIsDeletedFalse(String id, String userId);

        // Custom query dengan JPQL
        // @Query("""
        // SELECT t FROM Todo t
        // WHERE t.id = :id
        // AND t.isDeleted = false
        // AND (:completed IS NULL OR t.completed = :completed)
        // ORDER BY t.createdAt DESC
        // """)
        // Page<Todo> findUserTodos(
        // @Param("id") String userId,
        // @Param("completed") Boolean completed,
        // Pageable pageable);
}