package com.mohammadnuridin.todoapp.modules.user.service;

import com.mohammadnuridin.todoapp.modules.user.dto.ChangePasswordRequest;
import com.mohammadnuridin.todoapp.modules.user.dto.UpdateProfileRequest;
import com.mohammadnuridin.todoapp.modules.user.dto.UserResponse;

public interface UserService {

    UserResponse getProfile();

    UserResponse updateProfile(UpdateProfileRequest request);

    void changePassword(ChangePasswordRequest request);

    void deleteAccount();
}