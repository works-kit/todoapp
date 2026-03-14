package com.mohammadnuridin.todoapp.modules.user.service;

import java.time.Instant;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mohammadnuridin.todoapp.core.exception.AppException;
import com.mohammadnuridin.todoapp.core.exception.ErrorCode;
import com.mohammadnuridin.todoapp.core.util.SecurityUtil;
import com.mohammadnuridin.todoapp.modules.auth.service.TokenBlacklistService;
import com.mohammadnuridin.todoapp.modules.user.domain.User;
import com.mohammadnuridin.todoapp.modules.user.dto.ChangePasswordRequest;
import com.mohammadnuridin.todoapp.modules.user.dto.UpdateProfileRequest;
import com.mohammadnuridin.todoapp.modules.user.dto.UserResponse;
import com.mohammadnuridin.todoapp.modules.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService tokenBlacklistService;

    // ── GET PROFILE ───────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public UserResponse getProfile() {
        User user = getCurrentUserEntity();
        return UserResponse.from(user);
    }

    // ── UPDATE PROFILE ────────────────────────────────────────
    @Override
    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request) {
        // Validasi minimal satu field diisi
        if (request.isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }

        User user = getCurrentUserEntity();

        // Update name jika ada
        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name().trim());
        }

        // Update email jika ada dan berbeda dari sekarang
        if (request.email() != null && !request.email().isBlank()) {
            String newEmail = request.email().trim().toLowerCase();

            if (!newEmail.equals(user.getEmail())) {
                // Cek email sudah dipakai user lain
                if (userRepository.existsByEmail(newEmail)) {
                    throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
                }
                user.setEmail(newEmail);
            }
        }

        User saved = userRepository.save(user);
        log.info("Profile updated for user: {}", user.getId());
        return UserResponse.from(saved);
    }

    // ── CHANGE PASSWORD ───────────────────────────────────────
    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = getCurrentUserEntity();

        // Verifikasi current password
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.WRONG_PASSWORD);
        }

        // Pastikan new password tidak sama dengan current password
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));

        // Invalidasi semua refresh token — paksa login ulang di device lain
        user.setRefreshToken(null);
        user.setRefreshTokenExpiredAt(null);

        userRepository.save(user);
        log.info("Password changed for user: {}", user.getId());
    }

    // ── DELETE ACCOUNT ────────────────────────────────────────
    @Override
    @Transactional
    public void deleteAccount() {
        User user = getCurrentUserEntity();

        // Soft delete dengan timestamp
        user.setIsActive(false);
        user.setDeletedAt(Instant.now());
        user.setRefreshToken(null);
        user.setRefreshTokenExpiredAt(null);

        userRepository.save(user);
        log.info("Account soft-deleted for user: {}", user.getId());
    }

    // ── Helper: ambil entity user dari SecurityContext ────────
    private User getCurrentUserEntity() {
        String userId = SecurityUtil.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}