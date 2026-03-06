package com.mohammadnuridin.todoapp.modules.auth.api;

import com.mohammadnuridin.todoapp.core.response.ApiResponse;
import com.mohammadnuridin.todoapp.core.util.MessageService;
import com.mohammadnuridin.todoapp.modules.auth.dto.LoginRequest;
import com.mohammadnuridin.todoapp.modules.auth.dto.RefreshTokenRequest;
import com.mohammadnuridin.todoapp.modules.auth.dto.RegisterRequest;
import com.mohammadnuridin.todoapp.modules.auth.dto.TokenResponse;
import com.mohammadnuridin.todoapp.modules.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

        private final AuthService authService;
        private final MessageService msg;

        // ── POST /api/auth/register ───────────────────────────────
        @PostMapping("/register")
        public ResponseEntity<ApiResponse<Void>> register(
                        @Valid @RequestBody RegisterRequest request) {

                authService.register(request);
                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(ApiResponse.ok(msg.get("success.register")));
        }

        // ── POST /api/auth/login ──────────────────────────────────
        @PostMapping("/login")
        public ResponseEntity<ApiResponse<TokenResponse>> login(
                        @Valid @RequestBody LoginRequest request) {

                TokenResponse token = authService.login(request);
                return ResponseEntity.ok(
                                ApiResponse.ok(msg.get("success.login"), token));
        }

        // ── POST /api/auth/refresh-token ──────────────────────────
        @PostMapping("/refresh-token")
        public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
                        @Valid @RequestBody RefreshTokenRequest request) {

                TokenResponse token = authService.refreshToken(request);
                return ResponseEntity.ok(
                                ApiResponse.ok(msg.get("success.token.refreshed"), token));
        }

        // ── POST /api/auth/logout ─────────────────────────────────
        @PostMapping("/logout")
        public ResponseEntity<ApiResponse<Void>> logout(
                        @RequestHeader("Authorization") String authHeader) {

                // Strip "Bearer " prefix
                String token = authHeader.substring(7);
                authService.logout(token);
                return ResponseEntity.ok(
                                ApiResponse.ok(msg.get("success.logout")));
        }
}
