package com.ecommerce.userservice.controller;

import com.ecommerce.userservice.dto.request.UpdateProfileRequest;
import com.ecommerce.userservice.dto.response.UserProfileResponse;
import com.ecommerce.userservice.security.SecurityUtils;
import com.ecommerce.userservice.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "View and update a user's profile")
public class UserController {

    private final UserProfileService userProfileService;
    private final SecurityUtils securityUtils;

    @GetMapping("/{id}")
    @Operation(summary = "Get a user's profile (owner or ADMIN only)")
    public ResponseEntity<UserProfileResponse> getProfile(@PathVariable String id) {
        securityUtils.assertOwnerOrAdmin(id);
        return ResponseEntity.ok(userProfileService.getProfile(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a user's profile (owner or ADMIN only)")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @PathVariable String id, @Valid @RequestBody UpdateProfileRequest request) {
        securityUtils.assertOwnerOrAdmin(id);
        return ResponseEntity.ok(userProfileService.updateProfile(id, request));
    }
}
