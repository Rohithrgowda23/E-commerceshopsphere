package com.ecommerce.userservice.service.impl;

import com.ecommerce.userservice.dto.request.UpdateProfileRequest;
import com.ecommerce.userservice.dto.response.UserProfileResponse;
import com.ecommerce.userservice.entity.PreferredLanguage;
import com.ecommerce.userservice.entity.UserProfile;
import com.ecommerce.userservice.exception.BadRequestException;
import com.ecommerce.userservice.exception.ResourceNotFoundException;
import com.ecommerce.userservice.mapper.UserMapper;
import com.ecommerce.userservice.repository.UserProfileRepository;
import com.ecommerce.userservice.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private static final Logger log = LoggerFactory.getLogger(UserProfileServiceImpl.class);

    private final UserProfileRepository userProfileRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String userId) {
        UserProfile profile = findProfileOrThrow(userId);
        return userMapper.toProfileResponse(profile);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(String userId, UpdateProfileRequest request) {
        UserProfile profile = findProfileOrThrow(userId);

        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setPhoneNumber(request.getPhoneNumber());

        if (request.getAvatarUrl() != null) {
            profile.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getPreferredLanguage() != null) {
            profile.setPreferredLanguage(parseLanguage(request.getPreferredLanguage()));
        }
        if (request.getMarketingOptIn() != null) {
            profile.setMarketingOptIn(request.getMarketingOptIn());
        }

        UserProfile updated = userProfileRepository.save(profile);
        log.info("Updated profile for userId={}", userId);
        return userMapper.toProfileResponse(updated);
    }

    private UserProfile findProfileOrThrow(String userId) {
        return userProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No profile found for userId: " + userId));
    }

    private PreferredLanguage parseLanguage(String value) {
        try {
            return PreferredLanguage.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unsupported preferredLanguage: " + value);
        }
    }
}
