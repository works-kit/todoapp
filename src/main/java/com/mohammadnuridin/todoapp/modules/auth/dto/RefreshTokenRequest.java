package com.mohammadnuridin.todoapp.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(

        @NotBlank(message = "{user.refresh.token.invalid}")
        String refreshToken
) {}
