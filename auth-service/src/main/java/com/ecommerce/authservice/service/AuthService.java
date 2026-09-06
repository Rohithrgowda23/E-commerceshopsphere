package com.ecommerce.authservice.service;

import com.ecommerce.authservice.dto.request.GoogleLoginRequest;
import com.ecommerce.authservice.dto.request.LoginRequest;
import com.ecommerce.authservice.dto.request.RegisterRequest;
import com.ecommerce.authservice.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(String refreshTokenValue);

    AuthResponse loginWithGoogle(GoogleLoginRequest request);

    void logout(String refreshTokenValue);


}
