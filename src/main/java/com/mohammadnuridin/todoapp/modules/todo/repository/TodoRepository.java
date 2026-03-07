package com.mohammadnuridin.todoapp.modules.todo.repository;

import com.mohammadnuridin.todoapp.modules.todo.domain.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TodoRepository extends JpaRepository<Todo, String>,
        JpaSpecificationExecutor<Todo> {

    // Ownership check sekaligus fetch
    Optional<Todo> findByIdAndUserId(String id, String userId);

    // Count todo milik user (untuk stats)
    long countByUserId(String userId);

    // Count todo completed milik user
    long countByUserIdAndCompleted(String userId, boolean completed);
}