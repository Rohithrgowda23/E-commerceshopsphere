package com.ecommerce.userservice.service;

import com.ecommerce.userservice.dto.request.UpdateProfileRequest;
import com.ecommerce.userservice.dto.response.UserProfileResponse;

public interface UserProfileService {

    UserProfileResponse getProfile(String userId);

    UserProfileResponse updateProfile(String userId, UpdateProfileRequest request);
}
