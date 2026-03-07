package com.mohammadnuridin.todoapp.modules.category.service;

import com.mohammadnuridin.todoapp.modules.category.dto.CategoryRequest;
import com.mohammadnuridin.todoapp.modules.category.dto.CategoryResponse;

import java.util.List;

public interface CategoryService {

    List<CategoryResponse> getAll();

    CategoryResponse getById(String id);

    CategoryResponse create(CategoryRequest request);

    CategoryResponse update(String id, CategoryRequest request);

    void delete(String id);
}