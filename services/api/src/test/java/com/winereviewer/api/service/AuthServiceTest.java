package com.winereviewer.api.service;

import com.winereviewer.api.domain.User;
import com.winereviewer.api.exception.InvalidTokenException;
import com.winereviewer.api.repository.UserRepository;
import com.winereviewer.api.security.JwtUtil;
import com.winereviewer.api.service.GoogleTokenValidator.GoogleUserInfo;
import com.winereviewer.api.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes para AuthService.
 * <p>
 * Testa o fluxo completo de autenticação Google OAuth.
 *
 * @author lucas
 * @date 21/10/2025
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private GoogleTokenValidator googleTokenValidator;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private GoogleUserInfo googleUserInfo;
    private String googleIdToken;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        googleIdToken = "google-id-token-mock";
        jwtToken = "jwt-token-mock";
        googleUserInfo = new GoogleUserInfo(
                "google-user-123",
                "user@example.com",
                "Test User",
                "https://example.com/avatar.jpg"
        );
    }

    @Test
    void givenValidGoogleToken_WhenAuthenticateNewUser_ThenCreateUserAndReturnJWT() {
        // given
        final var newUserId = UUID.randomUUID();
        final var newUser = createUser(newUserId, googleUserInfo);

        when(googleTokenValidator.validateToken(googleIdToken)).thenReturn(googleUserInfo);
        when(userRepository.findByGoogleId(googleUserInfo.googleId())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(jwtUtil.generateToken(newUserId)).thenReturn(jwtToken);

        // when
        final var response = authService.authenticateWithGoogle(googleIdToken);

        // then
        assertNotNull(response);
        assertEquals(jwtToken, response.token());
        assertEquals(newUserId.toString(), response.userId());
        assertEquals(googleUserInfo.email(), response.email());
        assertEquals(googleUserInfo.name(), response.displayName());
        assertEquals(googleUserInfo.picture(), response.avatarUrl());

        verify(googleTokenValidator, times(1)).validateToken(googleIdToken);
        verify(userRepository, times(1)).findByGoogleId(googleUserInfo.googleId());
        verify(userRepository, times(1)).save(any(User.class));
        verify(jwtUtil, times(1)).generateToken(newUserId);
    }

    @Test
    void givenValidGoogleToken_WhenAuthenticateExistingUser_ThenReturnJWT() {
        // given
        final var existingUserId = UUID.randomUUID();
        final var existingUser = createUser(existingUserId, googleUserInfo);

        when(googleTokenValidator.validateToken(googleIdToken)).thenReturn(googleUserInfo);
        when(userRepository.findByGoogleId(googleUserInfo.googleId())).thenReturn(Optional.of(existingUser));
        when(jwtUtil.generateToken(existingUserId)).thenReturn(jwtToken);

        // when
        final var response = authService.authenticateWithGoogle(googleIdToken);

        // then
        assertNotNull(response);
        assertEquals(jwtToken, response.token());
        assertEquals(existingUserId.toString(), response.userId());
        assertEquals(googleUserInfo.email(), response.email());

        verify(googleTokenValidator, times(1)).validateToken(googleIdToken);
        verify(userRepository, times(1)).findByGoogleId(googleUserInfo.googleId());
        verify(userRepository, never()).save(any(User.class)); // Não salva se não houver mudanças
        verify(jwtUtil, times(1)).generateToken(existingUserId);
    }

    @Test
    void givenValidGoogleToken_WhenUserInfoChanged_ThenUpdateUserAndReturnJWT() {
        // given
        final var existingUserId = UUID.randomUUID();
        final var existingUser = createUser(existingUserId, new GoogleUserInfo(
                "google-user-123",
                "old-email@example.com",
                "Old Name",
                "https://example.com/old-avatar.jpg"
        ));

        when(googleTokenValidator.validateToken(googleIdToken)).thenReturn(googleUserInfo);
        when(userRepository.findByGoogleId(googleUserInfo.googleId())).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(jwtUtil.generateToken(existingUserId)).thenReturn(jwtToken);

        // when
        final var response = authService.authenticateWithGoogle(googleIdToken);

        // then
        assertNotNull(response);
        assertEquals(jwtToken, response.token());
        assertEquals(googleUserInfo.email(), response.email());
        assertEquals(googleUserInfo.name(), response.displayName());
        assertEquals(googleUserInfo.picture(), response.avatarUrl());

        // Verifica que o usuário foi atualizado
        assertEquals(googleUserInfo.email(), existingUser.getEmail());
        assertEquals(googleUserInfo.name(), existingUser.getDisplayName());
        assertEquals(googleUserInfo.picture(), existingUser.getAvatarUrl());

        verify(userRepository, times(1)).save(existingUser);
        verify(jwtUtil, times(1)).generateToken(existingUserId);
    }

    @Test
    void givenInvalidGoogleToken_WhenAuthenticate_ThenThrowInvalidTokenException() {
        // given
        when(googleTokenValidator.validateToken(googleIdToken))
                .thenThrow(new InvalidTokenException("Token inválido"));

        // when & then
        final var exception = assertThrows(InvalidTokenException.class, () ->
                authService.authenticateWithGoogle(googleIdToken)
        );

        assertTrue(exception.getMessage().contains("Token inválido"));
        verify(googleTokenValidator, times(1)).validateToken(googleIdToken);
        verify(userRepository, never()).findByGoogleId(any());
        verify(userRepository, never()).save(any());
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    void givenExpiredGoogleToken_WhenAuthenticate_ThenThrowInvalidTokenException() {
        // given
        when(googleTokenValidator.validateToken(googleIdToken))
                .thenThrow(new InvalidTokenException("Token expirado"));

        // when & then
        final var exception = assertThrows(InvalidTokenException.class, () ->
                authService.authenticateWithGoogle(googleIdToken)
        );

        assertTrue(exception.getMessage().contains("Token expirado"));
        verify(googleTokenValidator, times(1)).validateToken(googleIdToken);
    }

    private User createUser(UUID userId, GoogleUserInfo googleUserInfo) {
        final var user = new User();
        user.setId(userId);
        user.setGoogleId(googleUserInfo.googleId());
        user.setEmail(googleUserInfo.email());
        user.setDisplayName(googleUserInfo.name());
        user.setAvatarUrl(googleUserInfo.picture());
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return user;
    }

}
