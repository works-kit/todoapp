package com.mohammadnuridin.todoapp.modules.auth.service;

import com.mohammadnuridin.todoapp.modules.auth.dto.LoginRequest;
import com.mohammadnuridin.todoapp.modules.auth.dto.RefreshTokenRequest;
import com.mohammadnuridin.todoapp.modules.auth.dto.RegisterRequest;
import com.mohammadnuridin.todoapp.modules.auth.dto.TokenResponse;

public interface AuthService {

    void register(RegisterRequest request);

    TokenResponse login(LoginRequest request);

    TokenResponse refreshToken(RefreshTokenRequest request);

    void logout(String accessToken);
}
