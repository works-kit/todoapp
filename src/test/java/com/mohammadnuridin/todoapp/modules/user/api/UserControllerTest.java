package com.mohammadnuridin.todoapp.modules.user.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mohammadnuridin.todoapp.core.exception.AppException;
import com.mohammadnuridin.todoapp.core.exception.ErrorCode;
import com.mohammadnuridin.todoapp.core.ratelimiter.RateLimiterFilter;
import com.mohammadnuridin.todoapp.core.ratelimiter.RedisRateLimiter;
import com.mohammadnuridin.todoapp.core.security.JwtAuthEntryPoint;
import com.mohammadnuridin.todoapp.core.security.JwtAuthFilter;
import com.mohammadnuridin.todoapp.core.security.SecurityHeadersFilter;
import com.mohammadnuridin.todoapp.core.util.JwtService;
import com.mohammadnuridin.todoapp.core.util.MessageService;
import com.mohammadnuridin.todoapp.modules.auth.service.TokenBlacklistService;
import com.mohammadnuridin.todoapp.modules.auth.service.UserDetailsServiceImpl;
import com.mohammadnuridin.todoapp.modules.user.domain.Role;
import com.mohammadnuridin.todoapp.modules.user.dto.ChangePasswordRequest;
import com.mohammadnuridin.todoapp.modules.user.dto.UpdateProfileRequest;
import com.mohammadnuridin.todoapp.modules.user.dto.UserResponse;
import com.mohammadnuridin.todoapp.modules.user.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@DisplayName("UserController Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // ── Target test ───────────────────────────────────────────────────────────
    @MockBean
    private UserService userService;

    @MockBean
    private MessageService messageService;

    // ── SecurityConfig dependencies ───────────────────────────────────────────
    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private JwtAuthEntryPoint jwtAuthEntryPoint;

    @MockBean
    private SecurityHeadersFilter securityHeadersFilter;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    // ── JwtAuthFilter dependencies ────────────────────────────────────────────
    @MockBean
    private JwtService jwtService;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    // ── RateLimiterFilter: chain Redis yang di-exclude di profile local ────────
    // RateLimiterFilter → RedisRateLimiter →
    // RateLimiterConfig.rateLimiterRedisTemplate
    // → RedisConnectionFactory (tidak tersedia karena Redis di-exclude)
    // Mock RateLimiterFilter langsung — Spring tidak akan instantiate
    // RedisRateLimiter dan RedisConnectionFactory sama sekali
    @MockBean
    private RateLimiterFilter rateLimiterFilter;

    @MockBean
    private RedisRateLimiter redisRateLimiter;

    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    private UserResponse mockUserResponse;

    @BeforeEach
    void setUp() throws Exception {
        mockUserResponse = new UserResponse(
                "user-id-abc123",
                "Mohammad Nuridin",
                "nuridin@example.com",
                Role.USER,
                false,
                null,
                null);

        given(messageService.get("success.user.updated"))
                .willReturn("Profile updated successfully");
        given(messageService.get("success.user.password_changed"))
                .willReturn("Password changed successfully");

        // Pass-through: filter mock tidak boleh menelan request
        doAnswer(inv -> {
            ((FilterChain) inv.getArgument(2))
                    .doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(jwtAuthFilter).doFilter(
                any(HttpServletRequest.class),
                any(HttpServletResponse.class),
                any(FilterChain.class));

        doAnswer(inv -> {
            ((FilterChain) inv.getArgument(2))
                    .doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(securityHeadersFilter).doFilter(
                any(HttpServletRequest.class),
                any(HttpServletResponse.class),
                any(FilterChain.class));

        doAnswer(inv -> {
            ((FilterChain) inv.getArgument(2))
                    .doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(rateLimiterFilter).doFilter(
                any(HttpServletRequest.class),
                any(HttpServletResponse.class),
                any(FilterChain.class));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/users/me
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /users/me")
    class GetProfile {

        @Test
        @WithMockUser(username = "nuridin@example.com", roles = "USER")
        @DisplayName("should return 200 with user profile")
        void shouldReturn200_withUserProfile() throws Exception {
            given(userService.getProfile()).willReturn(mockUserResponse);

            mockMvc.perform(get("/api/users/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value("user-id-abc123"))
                    .andExpect(jsonPath("$.data.name").value("Mohammad Nuridin"))
                    .andExpect(jsonPath("$.data.email").value("nuridin@example.com"));
        }

        @Test
        @DisplayName("should return 401 when not authenticated")
        void shouldReturn401_whenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/users/me"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser
        @DisplayName("should return 404 when user not found")
        void shouldReturn404_whenUserNotFound() throws Exception {
            given(userService.getProfile())
                    .willThrow(new AppException(ErrorCode.USER_NOT_FOUND));

            mockMvc.perform(get("/api/users/me"))
                    .andExpect(status().isNotFound());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/users/me
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("PUT /users/me")
    class UpdateProfile {

        @Test
        @WithMockUser
        @DisplayName("should return 200 with updated profile")
        void shouldReturn200_withUpdatedProfile() throws Exception {
            UpdateProfileRequest request = new UpdateProfileRequest("Updated Name", null);
            given(userService.updateProfile(any(UpdateProfileRequest.class)))
                    .willReturn(mockUserResponse);

            mockMvc.perform(put("/api/users/me")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Profile updated successfully"))
                    .andExpect(jsonPath("$.data.email").value("nuridin@example.com"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 409 when email already taken")
        void shouldReturn409_whenEmailAlreadyExists() throws Exception {
            UpdateProfileRequest request = new UpdateProfileRequest(null, "taken@example.com");
            given(userService.updateProfile(any(UpdateProfileRequest.class)))
                    .willThrow(new AppException(ErrorCode.EMAIL_ALREADY_EXISTS));

            mockMvc.perform(put("/api/users/me")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("should return 401 when not authenticated")
        void shouldReturn401_whenNotAuthenticated() throws Exception {
            mockMvc.perform(put("/api/users/me")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser
        @DisplayName("should return 422 when request fields are empty")
        void shouldReturn422_whenValidationError() throws Exception {
            UpdateProfileRequest request = new UpdateProfileRequest(null, null);
            given(userService.updateProfile(any(UpdateProfileRequest.class)))
                    .willThrow(new AppException(ErrorCode.VALIDATION_ERROR));

            mockMvc.perform(put("/api/users/me")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PATCH /api/users/me/password
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("PATCH /users/me/password")
    class ChangePassword {

        @Test
        @WithMockUser
        @DisplayName("should return 200 when password changed successfully")
        void shouldReturn200_whenPasswordChanged() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest("oldPass123!", "newPass456!");
            willDoNothing().given(userService).changePassword(any(ChangePasswordRequest.class));

            mockMvc.perform(patch("/api/users/me/password")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Password changed successfully"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 400 when current password is wrong")
        void shouldReturn400_whenWrongPassword() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest("wrongPass", "newPass456!");
            willThrow(new AppException(ErrorCode.WRONG_PASSWORD))
                    .given(userService).changePassword(any(ChangePasswordRequest.class));

            mockMvc.perform(patch("/api/users/me/password")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("should return 422 when new password same as current")
        void shouldReturn422_whenNewPasswordSameAsCurrent() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest("samePass123!", "samePass123!");
            willThrow(new AppException(ErrorCode.VALIDATION_ERROR))
                    .given(userService).changePassword(any(ChangePasswordRequest.class));

            mockMvc.perform(patch("/api/users/me/password")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("should return 401 when not authenticated")
        void shouldReturn401_whenNotAuthenticated() throws Exception {
            mockMvc.perform(patch("/api/users/me/password")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/users/me
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /users/me")
    class DeleteAccount {

        @Test
        @WithMockUser
        @DisplayName("should return 200 when account deleted successfully")
        void shouldReturn200_whenAccountDeleted() throws Exception {
            willDoNothing().given(userService).deleteAccount();

            mockMvc.perform(delete("/api/users/me").with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Account deactivated successfully"));
        }

        @Test
        @DisplayName("should return 401 when not authenticated")
        void shouldReturn401_whenNotAuthenticated() throws Exception {
            mockMvc.perform(delete("/api/users/me").with(csrf()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser
        @DisplayName("should return 404 when user not found")
        void shouldReturn404_whenUserNotFound() throws Exception {
            willThrow(new AppException(ErrorCode.USER_NOT_FOUND))
                    .given(userService).deleteAccount();

            mockMvc.perform(delete("/api/users/me").with(csrf()))
                    .andExpect(status().isNotFound());
        }
    }
}