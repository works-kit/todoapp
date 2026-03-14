package com.mohammadnuridin.todoapp.modules.todo.api;

import com.mohammadnuridin.todoapp.core.response.ApiResponse;
import com.mohammadnuridin.todoapp.core.response.PageResponse;
import com.mohammadnuridin.todoapp.core.util.MessageService;
import com.mohammadnuridin.todoapp.modules.todo.dto.CreateTodoRequest;
import com.mohammadnuridin.todoapp.modules.todo.dto.TodoFilterRequest;
import com.mohammadnuridin.todoapp.modules.todo.dto.TodoResponse;
import com.mohammadnuridin.todoapp.modules.todo.dto.UpdateTodoRequest;
import com.mohammadnuridin.todoapp.modules.todo.service.TodoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/todos")
@RequiredArgsConstructor
public class TodoController {

        private final TodoService todoService;
        private final MessageService msg;

        // ── GET /api/todos ────────────────────────────────────────
        // Query params: completed, priority, categoryId, search,
        // dueBefore, dueAfter, page, size, sort
        @GetMapping
        public ResponseEntity<ApiResponse<PageResponse<TodoResponse>>> getAll(
                        @ModelAttribute TodoFilterRequest filter) {

                PageResponse<TodoResponse> result = todoService.getAll(filter);
                return ResponseEntity.ok(
                                ApiResponse.ok("success", result));
        }

        // ── GET /api/todos/all ────────────────────────────────────
        // Semua todo milik user tanpa filter, diurutkan by createdAt desc
        @GetMapping("/all")
        public ResponseEntity<ApiResponse<List<TodoResponse>>> getAllUnfiltered() {

                List<TodoResponse> result = todoService.getAllUnfiltered();
                return ResponseEntity.ok(
                                ApiResponse.ok("success", result));
        }

        // ── GET /api/todos/{id} ───────────────────────────────────
        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<TodoResponse>> getById(
                        @PathVariable String id) {

                TodoResponse todo = todoService.getById(id);
                return ResponseEntity.ok(
                                ApiResponse.ok("success", todo));
        }

        // ── POST /api/todos ───────────────────────────────────────
        @PostMapping
        public ResponseEntity<ApiResponse<TodoResponse>> create(
                        @Valid @RequestBody CreateTodoRequest request) {

                TodoResponse created = todoService.create(request);
                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(ApiResponse.ok(msg.get("success.todo.created"), created));
        }

        // ── PUT /api/todos/{id} ───────────────────────────────────
        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<TodoResponse>> update(
                        @PathVariable String id,
                        @Valid @RequestBody UpdateTodoRequest request) {

                TodoResponse updated = todoService.update(id, request);
                return ResponseEntity.ok(
                                ApiResponse.ok(msg.get("success.todo.updated"), updated));
        }

        // ── DELETE /api/todos/{id} ────────────────────────────────
        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse<Void>> delete(
                        @PathVariable String id) {

                todoService.delete(id);
                return ResponseEntity.ok(
                                ApiResponse.ok(msg.get("success.todo.deleted")));
        }

        // ── PATCH /api/todos/{id}/toggle ──────────────────────────
        // Toggle completed true ↔ false
        @PatchMapping("/{id}/toggle")
        public ResponseEntity<ApiResponse<TodoResponse>> toggleComplete(
                        @PathVariable String id) {

                TodoResponse toggled = todoService.toggleComplete(id);
                return ResponseEntity.ok(
                                ApiResponse.ok(msg.get("success.todo.status_updated"), toggled));
        }
}