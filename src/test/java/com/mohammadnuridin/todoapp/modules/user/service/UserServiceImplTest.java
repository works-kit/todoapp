package com.mohammadnuridin.todoapp.modules.user.service;

import com.mohammadnuridin.todoapp.core.exception.AppException;
import com.mohammadnuridin.todoapp.core.exception.ErrorCode;
import com.mohammadnuridin.todoapp.core.util.SecurityUtil;
import com.mohammadnuridin.todoapp.modules.auth.service.TokenBlacklistService;
import com.mohammadnuridin.todoapp.modules.user.domain.Role;
import com.mohammadnuridin.todoapp.modules.user.domain.User;
import com.mohammadnuridin.todoapp.modules.user.dto.ChangePasswordRequest;
import com.mohammadnuridin.todoapp.modules.user.dto.UpdateProfileRequest;
import com.mohammadnuridin.todoapp.modules.user.dto.UserResponse;
import com.mohammadnuridin.todoapp.modules.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private UserServiceImpl userService;

    private MockedStatic<SecurityUtil> securityUtilMock;

    private User mockUser;
    private static final String USER_ID = "user-id-abc123";

    @BeforeEach
    void setUp() {
        securityUtilMock = mockStatic(SecurityUtil.class);
        securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(USER_ID);

        mockUser = User.builder()
                .name("Mohammad Nuridin")
                .email("nuridin@example.com")
                .password("$2a$10$encodedPassword")
                .role(Role.USER)
                .refreshToken("some-refresh-token")
                .refreshTokenExpiredAt(System.currentTimeMillis() + 86400000L)
                .build();

        // Inject id via reflection since it's in BaseEntity
        try {
            var field = mockUser.getClass().getSuperclass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(mockUser, USER_ID);
        } catch (Exception ignored) { /* id tidak tersedia di BaseEntity test env */ }
    }

    @AfterEach
    void tearDown() {
        securityUtilMock.close();
    }

    // ─────────────────────────────────────────────────────────────────
    // getProfile()
    // ─────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getProfile()")
    class GetProfile {

        @Test
        @DisplayName("should return UserResponse for current authenticated user")
        void shouldReturnUserResponse_forCurrentUser() {
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(mockUser));

            UserResponse response = userService.getProfile();

            assertThat(response).isNotNull();
            assertThat(response.email()).isEqualTo("nuridin@example.com");
            assertThat(response.name()).isEqualTo("Mohammad Nuridin");
            verify(userRepository, times(1)).findById(USER_ID);
        }

        @Test
        @DisplayName("should throw USER_NOT_FOUND when user does not exist")
        void shouldThrowUserNotFound_whenUserMissing() {
            given(userRepository.findById(USER_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getProfile())
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // updateProfile()
    // ─────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("updateProfile()")
    class UpdateProfile {

        @Test
        @DisplayName("should update name only when only name is provided")
        void shouldUpdateName_whenOnlyNameProvided() {
            UpdateProfileRequest request = new UpdateProfileRequest("New Name", null);
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(mockUser));
            given(userRepository.save(mockUser)).willReturn(mockUser);

            UserResponse response = userService.updateProfile(request);

            assertThat(mockUser.getName()).isEqualTo("New Name");
            verify(userRepository).save(mockUser);
        }

        @Test
        @DisplayName("should update email when new email is unique")
        void shouldUpdateEmail_whenEmailIsUnique() {
            UpdateProfileRequest request = new UpdateProfileRequest(null, "new@example.com");
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(mockUser));
            given(userRepository.existsByEmail("new@example.com")).willReturn(false);
            given(userRepository.save(mockUser)).willReturn(mockUser);

            userService.updateProfile(request);

            assertThat(mockUser.getEmail()).isEqualTo("new@example.com");
            verify(userRepository).existsByEmail("new@example.com");
        }

        @Test
        @DisplayName("should not update email when new email is the same as current")
        void shouldNotUpdateEmail_whenSameAsCurrent() {
            UpdateProfileRequest request = new UpdateProfileRequest(null, "nuridin@example.com");
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(mockUser));
            given(userRepository.save(mockUser)).willReturn(mockUser);

            userService.updateProfile(request);

            // existsByEmail tidak dipanggil karena email sama
            verify(userRepository, never()).existsByEmail(anyString());
        }

        @Test
        @DisplayName("should throw EMAIL_ALREADY_EXISTS when email is taken by another user")
        void shouldThrowEmailAlreadyExists_whenEmailTaken() {
            UpdateProfileRequest request = new UpdateProfileRequest(null, "taken@example.com");
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(mockUser));
            given(userRepository.existsByEmail("taken@example.com")).willReturn(true);

            assertThatThrownBy(() -> userService.updateProfile(request))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_ALREADY_EXISTS);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw VALIDATION_ERROR when request is empty")
        void shouldThrowValidationError_whenRequestIsEmpty() {
            UpdateProfileRequest request = mock(UpdateProfileRequest.class);
            given(request.isEmpty()).willReturn(true);

            assertThatThrownBy(() -> userService.updateProfile(request))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_ERROR);

            verify(userRepository, never()).findById(anyString());
        }

        @Test
        @DisplayName("should trim whitespace from name and lowercase email")
        void shouldTrimName_andLowercaseEmail() {
            UpdateProfileRequest request = new UpdateProfileRequest("  Trimmed Name  ", "  UPPER@EXAMPLE.COM  ");
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(mockUser));
            given(userRepository.existsByEmail("upper@example.com")).willReturn(false);
            given(userRepository.save(mockUser)).willReturn(mockUser);

            userService.updateProfile(request);

            assertThat(mockUser.getName()).isEqualTo("Trimmed Name");
            assertThat(mockUser.getEmail()).isEqualTo("upper@example.com");
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // changePassword()
    // ─────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("changePassword()")
    class ChangePassword {

        @Test
        @DisplayName("should change password and invalidate refresh token")
        void shouldChangePassword_andInvalidateRefreshToken() {
            ChangePasswordRequest request = new ChangePasswordRequest("oldPass123", "newPass456");
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(mockUser));
            given(passwordEncoder.matches("oldPass123", mockUser.getPassword())).willReturn(true);
            given(passwordEncoder.matches("newPass456", mockUser.getPassword())).willReturn(false);
            given(passwordEncoder.encode("newPass456")).willReturn("$2a$10$newEncodedPassword");

            userService.changePassword(request);

            assertThat(mockUser.getPassword()).isEqualTo("$2a$10$newEncodedPassword");
            assertThat(mockUser.getRefreshToken()).isNull();
            assertThat(mockUser.getRefreshTokenExpiredAt()).isNull();
            verify(userRepository).save(mockUser);
        }

        @Test
        @DisplayName("should throw WRONG_PASSWORD when current password is incorrect")
        void shouldThrowWrongPassword_whenCurrentPasswordIncorrect() {
            ChangePasswordRequest request = new ChangePasswordRequest("wrongPass", "newPass456");
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(mockUser));
            given(passwordEncoder.matches("wrongPass", mockUser.getPassword())).willReturn(false);

            assertThatThrownBy(() -> userService.changePassword(request))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WRONG_PASSWORD);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw VALIDATION_ERROR when new password is same as current")
        void shouldThrowValidationError_whenNewPasswordSameAsCurrent() {
            ChangePasswordRequest request = new ChangePasswordRequest("samePass123", "samePass123");
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(mockUser));
            given(passwordEncoder.matches("samePass123", mockUser.getPassword())).willReturn(true);

            assertThatThrownBy(() -> userService.changePassword(request))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_ERROR);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw USER_NOT_FOUND when user does not exist")
        void shouldThrowUserNotFound_whenUserMissing() {
            ChangePasswordRequest request = new ChangePasswordRequest("old", "new");
            given(userRepository.findById(USER_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.changePassword(request))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // deleteAccount()
    // ─────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("deleteAccount()")
    class DeleteAccount {

        @Test
        @DisplayName("should soft-delete account and clear refresh token")
        void shouldSoftDeleteAccount_andClearRefreshToken() {
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(mockUser));
            given(userRepository.save(mockUser)).willReturn(mockUser);

            userService.deleteAccount();

            assertThat(mockUser.getDeletedAt()).isNotNull();
            assertThat(mockUser.getRefreshToken()).isNull();
            assertThat(mockUser.getRefreshTokenExpiredAt()).isNull();
            verify(userRepository).save(mockUser);
        }

        @Test
        @DisplayName("should throw USER_NOT_FOUND when user does not exist")
        void shouldThrowUserNotFound_whenUserMissing() {
            given(userRepository.findById(USER_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.deleteAccount())
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should set deletedAt timestamp close to now")
        void shouldSetDeletedAt_closeToNow() {
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(mockUser));
            given(userRepository.save(mockUser)).willReturn(mockUser);

            long before = System.currentTimeMillis();
            userService.deleteAccount();
            long after = System.currentTimeMillis();

            assertThat(mockUser.getDeletedAt()).isNotNull();
            assertThat(mockUser.getDeletedAt().toEpochMilli())
                    .isBetween(before, after);
        }
    }
}
