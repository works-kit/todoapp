package com.mohammadnuridin.todoapp.modules.auth.api;

import com.mohammadnuridin.todoapp.core.exception.AppException;
import com.mohammadnuridin.todoapp.core.exception.ErrorCode;
import com.mohammadnuridin.todoapp.core.response.ApiResponse;
import com.mohammadnuridin.todoapp.core.util.CookieUtil;
import com.mohammadnuridin.todoapp.core.util.MessageService;
import com.mohammadnuridin.todoapp.modules.auth.dto.AuthResult;
import com.mohammadnuridin.todoapp.modules.auth.dto.ClientType;
import com.mohammadnuridin.todoapp.modules.auth.dto.LoginRequest;
import com.mohammadnuridin.todoapp.modules.auth.dto.RefreshTokenRequest;
import com.mohammadnuridin.todoapp.modules.auth.dto.RegisterRequest;
import com.mohammadnuridin.todoapp.modules.auth.dto.TokenResponse;
import com.mohammadnuridin.todoapp.modules.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

        private final AuthService authService;
        private final MessageService msg;

        @Value("${app.jwt.refresh-token-expiration}")
        private long refreshTokenExpiration;

        // ─────────────────────────────────────────────────────────────────────────
        // POST /api/auth/register
        // ─────────────────────────────────────────────────────────────────────────
        @PostMapping("/register")
        public ResponseEntity<ApiResponse<Void>> register(
                        @Valid @RequestBody RegisterRequest request) {

                authService.register(request);
                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(ApiResponse.ok(msg.get("success.register")));
        }

        // ─────────────────────────────────────────────────────────────────────────
        // POST /api/auth/login
        // Header: X-Client-Type: web | mobile (default: mobile)
        //
        // Web → access_token di body, refresh_token di HttpOnly Cookie
        // Mobile → access_token + refresh_token di body
        // ─────────────────────────────────────────────────────────────────────────
        @PostMapping("/login")
        public ResponseEntity<ApiResponse<TokenResponse>> login(
                        @Valid @RequestBody LoginRequest request,
                        @RequestHeader(value = "X-Client-Type", defaultValue = "mobile") String clientTypeHeader,
                        HttpServletResponse response) {

                boolean isWeb = ClientType.from(clientTypeHeader) == ClientType.WEB;

                AuthResult result = authService.login(request);

                if (isWeb) {
                        CookieUtil.setRefreshTokenCookie(
                                        response,
                                        result.refreshToken(),
                                        refreshTokenExpiration / 1000);
                }

                return ResponseEntity.ok(
                                ApiResponse.ok(msg.get("success.login"), result.toResponse(isWeb)));
        }

        // ─────────────────────────────────────────────────────────────────────────
        // POST /api/auth/refresh-token
        // Header: X-Client-Type: web | mobile (default: mobile)
        //
        // Web → refresh_token dibaca dari Cookie, Cookie baru di-set setelah rotate
        // Mobile → refresh_token dari body, token baru di body response
        // ─────────────────────────────────────────────────────────────────────────
        @PostMapping("/refresh-token")
        public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
                        @RequestBody(required = false) RefreshTokenRequest body,
                        @RequestHeader(value = "X-Client-Type", defaultValue = "mobile") String clientTypeHeader,
                        HttpServletRequest request,
                        HttpServletResponse response) {

                boolean isWeb = ClientType.from(clientTypeHeader) == ClientType.WEB;

                // Ambil refresh token dari sumber yang sesuai
                String rawRefreshToken = isWeb
                                ? CookieUtil.extractRefreshTokenFromCookie(request)
                                : extractFromBody(body);

                AuthResult result = authService.refreshToken(rawRefreshToken);

                // Web → rotate Cookie dengan refresh token baru
                if (isWeb) {
                        CookieUtil.setRefreshTokenCookie(
                                        response,
                                        result.refreshToken(),
                                        refreshTokenExpiration / 1000);
                }

                return ResponseEntity.ok(
                                ApiResponse.ok(msg.get("success.token.refreshed"), result.toResponse(isWeb)));
        }

        // ─────────────────────────────────────────────────────────────────────────
        // POST /api/auth/logout
        // Header: Authorization: Bearer <access_token>
        // X-Client-Type: web | mobile (default: mobile)
        //
        // Web → blacklist access token + clear Cookie
        // Mobile → blacklist access token saja
        // ─────────────────────────────────────────────────────────────────────────
        @PostMapping("/logout")
        public ResponseEntity<ApiResponse<Void>> logout(
                        @RequestHeader("Authorization") String authHeader,
                        @RequestHeader(value = "X-Client-Type", defaultValue = "mobile") String clientTypeHeader,
                        HttpServletResponse response) {

                boolean isWeb = ClientType.from(clientTypeHeader) == ClientType.WEB;

                String accessToken = authHeader.substring(7); // strip "Bearer "
                authService.logout(accessToken);

                // Web → hapus Cookie refresh token di browser
                if (isWeb) {
                        CookieUtil.clearRefreshTokenCookie(response);
                }

                return ResponseEntity.ok(
                                ApiResponse.ok(msg.get("success.logout")));
        }

        // ── Helper: validasi & ambil refresh token dari body ─────
        private String extractFromBody(RefreshTokenRequest body) {
                if (body == null || body.refreshToken() == null
                                || body.refreshToken().isBlank()) {
                        throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
                }
                return body.refreshToken();
        }
}
