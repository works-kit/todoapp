package com.mohammadnuridin.todoapp.modules.category.service;

import com.mohammadnuridin.todoapp.core.exception.AppException;
import com.mohammadnuridin.todoapp.core.exception.ErrorCode;
import com.mohammadnuridin.todoapp.core.util.SecurityUtil;
import com.mohammadnuridin.todoapp.modules.category.domain.Category;
import com.mohammadnuridin.todoapp.modules.category.dto.CategoryRequest;
import com.mohammadnuridin.todoapp.modules.category.dto.CategoryResponse;
import com.mohammadnuridin.todoapp.modules.category.repository.CategoryRepository;
import com.mohammadnuridin.todoapp.modules.user.domain.User;
import com.mohammadnuridin.todoapp.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    // ── GET ALL ───────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll() {
        String userId = SecurityUtil.getCurrentUserId();
        return categoryRepository.findByUserIdOrderByNameAsc(userId)
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    // ── GET BY ID ─────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(String id) {
        Category category = getOwnedCategory(id);
        return CategoryResponse.from(category);
    }

    // ── CREATE ────────────────────────────────────────────────
    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String userId = SecurityUtil.getCurrentUserId();

        // Cek nama duplikat untuk user yang sama
        if (categoryRepository.existsByUserIdAndName(userId, request.name().trim())) {
            throw new AppException(ErrorCode.CATEGORY_NAME_EXISTS);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Jika request set is_default = true, unset default kategori lama
        if (Boolean.TRUE.equals(request.isDefault())) {
            unsetCurrentDefault(userId);
        }

        Category category = Category.builder()
                .user(user)
                .name(request.name().trim())
                .description(request.description())
                .color(request.color())
                .isDefault(request.isDefault() != null ? request.isDefault() : false)
                .build();

        Category saved = categoryRepository.save(category);
        log.info("Category created: {} for user: {}", saved.getId(), userId);
        return CategoryResponse.from(saved);
    }

    // ── UPDATE ────────────────────────────────────────────────
    @Override
    @Transactional
    public CategoryResponse update(String id, CategoryRequest request) {
        String userId = SecurityUtil.getCurrentUserId();
        Category category = getOwnedCategory(id);

        // Cek nama duplikat, kecuali nama kategori ini sendiri
        if (request.name() != null && !request.name().isBlank()) {
            String newName = request.name().trim();
            if (!newName.equalsIgnoreCase(category.getName()) &&
                    categoryRepository.existsByUserIdAndNameAndIdNot(userId, newName, id)) {
                throw new AppException(ErrorCode.CATEGORY_NAME_EXISTS);
            }
            category.setName(newName);
        }

        if (request.description() != null) {
            category.setDescription(request.description());
        }

        if (request.color() != null) {
            category.setColor(request.color());
        }

        // Handle is_default update
        if (Boolean.TRUE.equals(request.isDefault()) && !Boolean.TRUE.equals(category.getIsDefault())) {
            unsetCurrentDefault(userId);
            category.setIsDefault(true);
        } else if (Boolean.FALSE.equals(request.isDefault())) {
            category.setIsDefault(false);
        }

        Category saved = categoryRepository.save(category);
        log.info("Category updated: {} for user: {}", id, userId);
        return CategoryResponse.from(saved);
    }

    // ── DELETE ────────────────────────────────────────────────
    @Override
    @Transactional
    public void delete(String id) {
        Category category = getOwnedCategory(id);
        categoryRepository.delete(category);
        log.info("Category deleted: {} for user: {}", id, SecurityUtil.getCurrentUserId());
    }

    // ── Helper: ambil category dan validasi ownership ─────────
    private Category getOwnedCategory(String id) {
        String userId = SecurityUtil.getCurrentUserId();
        return categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    // ── Helper: unset is_default dari semua kategori user ─────
    private void unsetCurrentDefault(String userId) {
        categoryRepository.findByUserIdOrderByNameAsc(userId)
                .stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsDefault()))
                .forEach(c -> {
                    c.setIsDefault(false);
                    categoryRepository.save(c);
                });
    }
}