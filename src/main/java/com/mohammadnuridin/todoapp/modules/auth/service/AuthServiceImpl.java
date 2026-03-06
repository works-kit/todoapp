package com.mohammadnuridin.todoapp.modules.auth.service;

import com.mohammadnuridin.todoapp.core.exception.AppException;
import com.mohammadnuridin.todoapp.core.exception.ErrorCode;
import com.mohammadnuridin.todoapp.core.security.JwtService;
import com.mohammadnuridin.todoapp.modules.auth.dto.LoginRequest;
import com.mohammadnuridin.todoapp.modules.auth.dto.RefreshTokenRequest;
import com.mohammadnuridin.todoapp.modules.auth.dto.RegisterRequest;
import com.mohammadnuridin.todoapp.modules.auth.dto.TokenResponse;
import com.mohammadnuridin.todoapp.modules.user.domain.Role;
import com.mohammadnuridin.todoapp.modules.user.domain.User;
import com.mohammadnuridin.todoapp.modules.user.domain.UserDetailsImpl;
import com.mohammadnuridin.todoapp.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository        userRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtService            jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService tokenBlacklistService;

    @Value("${app.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    // ── REGISTER ──────────────────────────────────────────────
    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .isActive(true)
                .build();

        userRepository.save(user);
        log.info("New user registered: {}", request.email());
    }

    // ── LOGIN ─────────────────────────────────────────────────
    @Override
    @Transactional
    public TokenResponse login(LoginRequest request) {
        // Spring Security handle validasi credentials + disabled account
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String accessToken  = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Simpan refresh token ke DB
        User user = userRepository.findByEmail(userDetails.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiredAt(
                System.currentTimeMillis() + refreshTokenExpiration
        );
        userRepository.save(user);

        log.info("User logged in: {}", userDetails.getEmail());
        return TokenResponse.of(accessToken, refreshToken, accessTokenExpiration / 1000);
    }

    // ── REFRESH TOKEN ─────────────────────────────────────────
    @Override
    @Transactional
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        // Cari user berdasarkan refresh token di DB
        User user = userRepository.findByRefreshToken(request.refreshToken())
                .orElseThrow(() -> new AppException(ErrorCode.REFRESH_TOKEN_INVALID));

        // Validasi refresh token belum expired
        if (user.getRefreshTokenExpiredAt() == null ||
                System.currentTimeMillis() > user.getRefreshTokenExpiredAt()) {
            // Hapus refresh token yang expired
            user.setRefreshToken(null);
            user.setRefreshTokenExpiredAt(null);
            userRepository.save(user);
            throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        // Cek user masih aktif
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        }

        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        // Generate access token baru
        String newAccessToken  = jwtService.generateAccessToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        // Rotate refresh token — invalidasi yang lama, simpan yang baru
        user.setRefreshToken(newRefreshToken);
        user.setRefreshTokenExpiredAt(
                System.currentTimeMillis() + refreshTokenExpiration
        );
        userRepository.save(user);

        log.info("Token refreshed for: {}", user.getEmail());
        return TokenResponse.of(newAccessToken, newRefreshToken, accessTokenExpiration / 1000);
    }

    // ── LOGOUT ────────────────────────────────────────────────
    @Override
    @Transactional
    public void logout(String accessToken) {
        // Blacklist access token di Redis
        long ttl = jwtService.getRemainingTtlSeconds(accessToken);
        tokenBlacklistService.blacklist(accessToken, ttl);

        // Hapus refresh token dari DB
        try {
            String email = jwtService.extractEmail(accessToken);
            userRepository.findByEmail(email).ifPresent(user -> {
                user.setRefreshToken(null);
                user.setRefreshTokenExpiredAt(null);
                userRepository.save(user);
            });
        } catch (Exception e) {
            // Token mungkin sudah expired tapi tetap logout — tidak perlu error
            log.warn("Could not extract email during logout: {}", e.getMessage());
        }

        log.info("User logged out, token blacklisted");
    }
}
