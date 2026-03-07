package com.mohammadnuridin.todoapp.modules.auth.service;

import com.mohammadnuridin.todoapp.modules.auth.dto.AuthResult;
import com.mohammadnuridin.todoapp.modules.auth.dto.LoginRequest;
import com.mohammadnuridin.todoapp.modules.auth.dto.RegisterRequest;

public interface AuthService {

    void register(RegisterRequest request);

    AuthResult login(LoginRequest request);

    AuthResult refreshToken(String rawRefreshToken);

    void logout(String accessToken);
}
