package com.mohammadnuridin.todoapp.modules.category.repository;

import com.mohammadnuridin.todoapp.modules.category.domain.Category;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {

    // Semua kategori milik user — diurutkan by name
    @EntityGraph(attributePaths = "user")
    List<Category> findByUserIdOrderByNameAsc(String userId);

    // Cek nama kategori sudah ada untuk user yang sama
    boolean existsByUserIdAndName(String userId, String name);

    // Cek nama kategori sudah ada untuk user yang sama, kecuali category tertentu
    // (untuk update)
    boolean existsByUserIdAndNameAndIdNot(String userId, String name, String id);

    // Cari category by id dan pastikan milik user (ownership check)
    Optional<Category> findByIdAndUserId(String id, String userId);

    // Hapus semua kategori milik user (saat delete account)
    void deleteAllByUserId(String userId);
}