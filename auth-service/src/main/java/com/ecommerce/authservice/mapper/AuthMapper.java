package com.ecommerce.authservice.mapper;

import com.ecommerce.authservice.dto.response.AuthResponse;
import com.ecommerce.authservice.entity.Role;
import com.ecommerce.authservice.entity.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class AuthMapper {

    public AuthResponse toAuthResponse(User user, String accessToken, String refreshToken, long expiresInMs) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresInMs(expiresInMs)
                .userId(user.getId())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(Role::name).collect(Collectors.toSet()))
                .build();
    }
}
