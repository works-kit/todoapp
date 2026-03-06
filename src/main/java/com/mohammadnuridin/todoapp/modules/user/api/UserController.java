package com.mohammadnuridin.todoapp.modules.user.api;

import com.mohammadnuridin.todoapp.core.response.ApiResponse;
import com.mohammadnuridin.todoapp.core.util.MessageService;
import com.mohammadnuridin.todoapp.modules.user.dto.ChangePasswordRequest;
import com.mohammadnuridin.todoapp.modules.user.dto.UpdateProfileRequest;
import com.mohammadnuridin.todoapp.modules.user.dto.UserResponse;
import com.mohammadnuridin.todoapp.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final MessageService msg;

    // ── GET /api/users/me ─────────────────────────────────────
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile() {
        UserResponse profile = userService.getProfile();
        return ResponseEntity.ok(
                ApiResponse.ok("success", profile));
    }

    // ── PUT /api/users/me ─────────────────────────────────────
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request) {

        UserResponse updated = userService.updateProfile(request);
        return ResponseEntity.ok(
                ApiResponse.ok(msg.get("success.user.updated"), updated));
    }

    // ── PATCH /api/users/me/password ──────────────────────────
    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        userService.changePassword(request);
        return ResponseEntity.ok(
                ApiResponse.ok(msg.get("success.user.password_changed")));
    }

    // ── DELETE /api/users/me ──────────────────────────────────
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteAccount() {
        userService.deleteAccount();
        return ResponseEntity.ok(
                ApiResponse.ok("Account deactivated successfully"));
    }
}