package com.mohammadnuridin.todoapp.modules.category.api;

import com.mohammadnuridin.todoapp.core.response.ApiResponse;
import com.mohammadnuridin.todoapp.core.util.MessageService;
import com.mohammadnuridin.todoapp.modules.category.dto.CategoryRequest;
import com.mohammadnuridin.todoapp.modules.category.dto.CategoryResponse;
import com.mohammadnuridin.todoapp.modules.category.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final MessageService msg;

    // ── GET /api/categories ───────────────────────────────────
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAll() {
        List<CategoryResponse> categories = categoryService.getAll();
        return ResponseEntity.ok(
                ApiResponse.ok("success", categories));
    }

    // ── GET /api/categories/{id} ──────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getById(
            @PathVariable String id) {

        CategoryResponse category = categoryService.getById(id);
        return ResponseEntity.ok(
                ApiResponse.ok("success", category));
    }

    // ── POST /api/categories ──────────────────────────────────
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> create(
            @Valid @RequestBody CategoryRequest request) {

        CategoryResponse created = categoryService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(msg.get("success.category.created"), created));
    }

    // ── PUT /api/categories/{id} ──────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody CategoryRequest request) {

        CategoryResponse updated = categoryService.update(id, request);
        return ResponseEntity.ok(
                ApiResponse.ok(msg.get("success.category.updated"), updated));
    }

    // ── DELETE /api/categories/{id} ───────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String id) {

        categoryService.delete(id);
        return ResponseEntity.ok(
                ApiResponse.ok(msg.get("success.category.deleted")));
    }
}