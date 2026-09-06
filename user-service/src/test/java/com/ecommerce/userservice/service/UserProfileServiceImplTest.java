package com.ecommerce.userservice.service;

import com.ecommerce.userservice.dto.request.UpdateProfileRequest;
import com.ecommerce.userservice.dto.response.UserProfileResponse;
import com.ecommerce.userservice.entity.PreferredLanguage;
import com.ecommerce.userservice.entity.UserProfile;
import com.ecommerce.userservice.exception.ResourceNotFoundException;
import com.ecommerce.userservice.mapper.UserMapper;
import com.ecommerce.userservice.repository.UserProfileRepository;
import com.ecommerce.userservice.service.impl.UserProfileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceImplTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserProfileServiceImpl userProfileService;

    private UserProfile existingProfile;

    @BeforeEach
    void setUp() {
        existingProfile = UserProfile.builder()
                .userId("user-1")
                .email("jane@example.com")
                .firstName("Jane")
                .lastName("Doe")
                .preferredLanguage(PreferredLanguage.EN)
                .marketingOptIn(false)
                .build();
    }

    @Test
    void getProfile_existingUser_returnsProfile() {
        when(userProfileRepository.findById("user-1")).thenReturn(Optional.of(existingProfile));
        when(userMapper.toProfileResponse(existingProfile))
                .thenReturn(UserProfileResponse.builder().userId("user-1").build());

        UserProfileResponse response = userProfileService.getProfile("user-1");

        assertEquals("user-1", response.getUserId());
    }

    @Test
    void getProfile_unknownUser_throwsResourceNotFound() {
        when(userProfileRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userProfileService.getProfile("missing"));
    }

    @Test
    void updateProfile_validRequest_updatesFields() {
        UpdateProfileRequest request = new UpdateProfileRequest(
                "Janet", "Doe", "+15551234567", null, "ES", true);

        when(userProfileRepository.findById("user-1")).thenReturn(Optional.of(existingProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toProfileResponse(any(UserProfile.class)))
                .thenReturn(UserProfileResponse.builder().userId("user-1").firstName("Janet").build());

        UserProfileResponse response = userProfileService.updateProfile("user-1", request);

        assertEquals("Janet", response.getFirstName());
        assertEquals(PreferredLanguage.ES, existingProfile.getPreferredLanguage());
        assertEquals(true, existingProfile.isMarketingOptIn());
    }

    @Test
    void updateProfile_unknownUser_throwsResourceNotFound() {
        UpdateProfileRequest request = new UpdateProfileRequest(
                "Janet", "Doe", "+15551234567", null, "EN", false);
        when(userProfileRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userProfileService.updateProfile("missing", request));
    }
}
