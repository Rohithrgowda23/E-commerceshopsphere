package com.ecommerce.authservice.service.impl;

import com.ecommerce.authservice.dto.request.LoginRequest;
import com.ecommerce.authservice.dto.request.RegisterRequest;
import com.ecommerce.authservice.dto.response.AuthResponse;
import com.ecommerce.authservice.entity.RefreshToken;
import com.ecommerce.authservice.entity.Role;
import com.ecommerce.authservice.entity.User;
import com.ecommerce.authservice.exception.InvalidCredentialsException;
import com.ecommerce.authservice.exception.InvalidRefreshTokenException;
import com.ecommerce.authservice.exception.UserAlreadyExistsException;
import com.ecommerce.authservice.kafka.event.UserRegisteredEvent;
import com.ecommerce.authservice.kafka.producer.UserEventProducer;
import com.ecommerce.authservice.mapper.AuthMapper;
import com.ecommerce.authservice.repository.RefreshTokenRepository;
import com.ecommerce.authservice.repository.UserRepository;
import com.ecommerce.authservice.security.InvalidGoogleTokenException;
import com.ecommerce.authservice.security.JwtTokenProvider;
import com.ecommerce.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ecommerce.authservice.dto.request.GoogleLoginRequest;
import com.ecommerce.authservice.entity.AuthProvider;
import com.ecommerce.authservice.security.GoogleTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthMapper authMapper;
    private final UserEventProducer userEventProducer;
    private final GoogleTokenVerifier googleTokenVerifier;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new UserAlreadyExistsException("An account with this email already exists");
        }

        User user = User.builder()
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(Role.USER))
                .enabled(true)
                .accountNonLocked(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Registered new user userId={} email={}", savedUser.getId(), savedUser.getEmail());

        publishUserRegisteredEvent(savedUser, request.getFirstName(), request.getLastName());

        return issueTokens(savedUser);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (!user.isEnabled() || !user.isAccountNonLocked()) {
            throw new InvalidCredentialsException("Account is disabled or locked");
        }

        log.info("User logged in userId={} email={}", user.getId(), user.getEmail());
        return issueTokens(user);
    }

    @Override
    @Transactional
    public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
        GoogleIdToken.Payload payload = googleTokenVerifier.verify(request.getIdToken());

        String email = payload.getEmail();
        Boolean emailVerified = payload.getEmailVerified();
        if (email == null || Boolean.FALSE.equals(emailVerified)) {
            throw new InvalidGoogleTokenException("Google account email is missing or unverified");
        }

        String normalizedEmail = email.trim().toLowerCase();
        String firstName = (String) payload.get("given_name");
        String lastName = (String) payload.get("family_name");

        User user = userRepository.findByEmail(normalizedEmail).orElse(null);
        boolean isNewUser = user == null;

        if (isNewUser) {
            user = User.builder()
                    .email(normalizedEmail)
                    // OAuth accounts have no local password — store a random
                    // bcrypt hash so the (non-null) column is satisfied with
                    // a value nobody could ever type at the login form.
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .roles(Set.of(Role.USER))
                    .enabled(true)
                    .accountNonLocked(true)
                    .authProvider(AuthProvider.GOOGLE)
                    .build();
            user = userRepository.save(user);
            log.info("Created new user via Google OAuth userId={} email={}", user.getId(), user.getEmail());

            publishUserRegisteredEvent(user,
                    firstName != null ? firstName : "",
                    lastName != null ? lastName : "");
        } else {
            log.info("Existing user logged in via Google OAuth userId={} email={}", user.getId(), user.getEmail());
        }

        return issueTokens(user);
    }

    @Override
    @Transactional
    public AuthResponse refresh(String refreshTokenValue) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token not recognized"));

        if (storedToken.isRevoked()) {
            throw new InvalidRefreshTokenException("Refresh token has been revoked");
        }
        if (storedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidRefreshTokenException("Refresh token has expired");
        }

        User user = userRepository.findById(storedToken.getUserId())
                .orElseThrow(() -> new InvalidRefreshTokenException("User for refresh token no longer exists"));

        // Rotate: revoke the used refresh token and issue a brand new pair.
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        log.info("Refreshed tokens for userId={}", user.getId());
        return issueTokens(user);
    }

    @Override
    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            log.info("User logged out, refresh token revoked for userId={}", token.getUserId());
        });
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshTokenValue = jwtTokenProvider.generateRefreshTokenValue();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .userId(user.getId())
                .expiryDate(LocalDateTime.now().plusSeconds(
                        jwtTokenProvider.getRefreshTokenExpirationMs() / 1000))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        return authMapper.toAuthResponse(
                user, accessToken, refreshTokenValue, jwtTokenProvider.getAccessTokenExpirationMs());
    }

    private void publishUserRegisteredEvent(User user, String firstName, String lastName) {
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("USER_REGISTERED")
                .timestamp(LocalDateTime.now())
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(firstName)
                .lastName(lastName)
                .build();
        try {
            userEventProducer.publishUserRegistered(event);
        } catch (Exception e) {
            // Kafka being slow/unreachable must never fail registration
            // itself — the account is already saved in MySQL at this
            // point. user-service just won't get its profile-creation
            // event until Kafka is back up (or a manual replay/backfill).
            log.error("Failed to publish UserRegisteredEvent for userId={}: {}",
                    user.getId(), e.getMessage(), e);
        }
    }
}
