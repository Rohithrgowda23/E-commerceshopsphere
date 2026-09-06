package com.ecommerce.authservice.service;

import com.ecommerce.authservice.dto.request.LoginRequest;
import com.ecommerce.authservice.dto.request.RegisterRequest;
import com.ecommerce.authservice.dto.response.AuthResponse;
import com.ecommerce.authservice.entity.RefreshToken;
import com.ecommerce.authservice.entity.Role;
import com.ecommerce.authservice.entity.User;
import com.ecommerce.authservice.exception.InvalidCredentialsException;
import com.ecommerce.authservice.exception.InvalidRefreshTokenException;
import com.ecommerce.authservice.exception.UserAlreadyExistsException;
import com.ecommerce.authservice.kafka.producer.UserEventProducer;
import com.ecommerce.authservice.mapper.AuthMapper;
import com.ecommerce.authservice.repository.RefreshTokenRepository;
import com.ecommerce.authservice.repository.UserRepository;
import com.ecommerce.authservice.security.JwtTokenProvider;
import com.ecommerce.authservice.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private AuthMapper authMapper;
    @Mock
    private UserEventProducer userEventProducer;

    @InjectMocks
    private AuthServiceImpl authService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = User.builder()
                .id("user-1")
                .email("jane@example.com")
                .password("encoded-password")
                .roles(Set.of(Role.USER))
                .enabled(true)
                .accountNonLocked(true)
                .build();
    }

    @Test
    void register_newEmail_createsUserAndReturnsTokens() {
        RegisterRequest request = new RegisterRequest("jane@example.com", "password123", "Jane", "Doe");

        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(existingUser);
        when(jwtTokenProvider.generateAccessToken(existingUser)).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshTokenValue()).thenReturn("refresh-token");
        when(jwtTokenProvider.getRefreshTokenExpirationMs()).thenReturn(604800000L);
        when(jwtTokenProvider.getAccessTokenExpirationMs()).thenReturn(900000L);
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(authMapper.toAuthResponse(existingUser, "access-token", "refresh-token", 900000L))
                .thenReturn(AuthResponse.builder().accessToken("access-token").build());

        AuthResponse response = authService.register(request);

        assertEquals("access-token", response.getAccessToken());
        verify(userEventProducer).publishUserRegistered(any());
    }

    @Test
    void register_emailAlreadyExists_throwsException() {
        RegisterRequest request = new RegisterRequest("jane@example.com", "password123", "Jane", "Doe");
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));
    }

    @Test
    void login_validCredentials_returnsTokens() {
        LoginRequest request = new LoginRequest("jane@example.com", "password123");

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(existingUser)).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshTokenValue()).thenReturn("refresh-token");
        when(jwtTokenProvider.getRefreshTokenExpirationMs()).thenReturn(604800000L);
        when(jwtTokenProvider.getAccessTokenExpirationMs()).thenReturn(900000L);
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(authMapper.toAuthResponse(existingUser, "access-token", "refresh-token", 900000L))
                .thenReturn(AuthResponse.builder().accessToken("access-token").build());

        AuthResponse response = authService.login(request);

        assertEquals("access-token", response.getAccessToken());
    }

    @Test
    void login_wrongPassword_throwsInvalidCredentials() {
        LoginRequest request = new LoginRequest("jane@example.com", "wrong-password");

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void login_unknownEmail_throwsInvalidCredentials() {
        LoginRequest request = new LoginRequest("unknown@example.com", "password123");
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void refresh_expiredToken_throwsInvalidRefreshToken() {
        RefreshToken expired = RefreshToken.builder()
                .token("old-token")
                .userId("user-1")
                .expiryDate(LocalDateTime.now().minusDays(1))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("old-token")).thenReturn(Optional.of(expired));

        assertThrows(InvalidRefreshTokenException.class, () -> authService.refresh("old-token"));
    }

    @Test
    void refresh_revokedToken_throwsInvalidRefreshToken() {
        RefreshToken revoked = RefreshToken.builder()
                .token("revoked-token")
                .userId("user-1")
                .expiryDate(LocalDateTime.now().plusDays(1))
                .revoked(true)
                .build();

        when(refreshTokenRepository.findByToken("revoked-token")).thenReturn(Optional.of(revoked));

        assertThrows(InvalidRefreshTokenException.class, () -> authService.refresh("revoked-token"));
    }

    @Test
    void refresh_unknownToken_throwsInvalidRefreshToken() {
        when(refreshTokenRepository.findByToken("nope")).thenReturn(Optional.empty());

        assertThrows(InvalidRefreshTokenException.class, () -> authService.refresh("nope"));
    }

    @Test
    void logout_existingToken_revokesIt() {
        RefreshToken token = RefreshToken.builder()
                .token("logout-token")
                .userId("user-1")
                .expiryDate(LocalDateTime.now().plusDays(1))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("logout-token")).thenReturn(Optional.of(token));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        authService.logout("logout-token");

        assertEquals(true, token.isRevoked());
    }
}
