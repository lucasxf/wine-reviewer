package com.winereviewer.api.integration;

import com.winereviewer.api.controller.AuthController;
import com.winereviewer.api.domain.User;
import com.winereviewer.api.repository.UserRepository;
import com.winereviewer.api.service.GoogleTokenValidator;
import com.winereviewer.api.exception.InvalidTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController with Testcontainers PostgreSQL.
 * <p>
 * Tests authentication endpoints against a real PostgreSQL database to verify:
 * - Google OAuth authentication flow
 * - User creation and update logic
 * - JWT token generation
 * - Input validation (invalid tokens, missing fields)
 * - Exception handling (401 Unauthorized)
 * <p>
 * <strong>Test strategy:</strong>
 * - Uses real PostgreSQL via Testcontainers (production-like environment)
 * - Mocks GoogleTokenValidator (external Google API dependency)
 * - Each test is @Transactional (auto-rollback for isolation)
 * - Test data inserted directly via repositories for speed
 * - MockMvc for HTTP request/response testing
 * <p>
 * <strong>Why mock GoogleTokenValidator:</strong>
 * - Google OAuth validation requires external API calls
 * - Integration tests should not depend on external services
 * - Allows testing invalid token scenarios without real Google tokens
 * - Focuses on controller + service + database integration
 *
 * @author lucas
 * @date 22/10/2025
 */
class AuthControllerIT extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private GoogleTokenValidator googleTokenValidator;

    @BeforeEach
    void setupMocks() {
        // Default mock behavior - valid Google token
        final var googleUserInfo = new GoogleTokenValidator.GoogleUserInfo(
                "google-id-123",
                "joao.silva@example.com",
                "João Silva",
                "https://lh3.googleusercontent.com/avatar123"
        );

        when(googleTokenValidator.validateToken(anyString()))
                .thenReturn(googleUserInfo);
    }

    // =========================================================================
    // POST /auth/google - Authenticate with Google OAuth
    // =========================================================================

    @Test
    void shouldAuthenticateNewUserWithGoogleOAuth() throws Exception {
        // Given - valid Google ID token
        final var request = new AuthController.GoogleAuthRequest("valid-google-token");

        // When & Then
        mockMvc.perform(post("/auth/google")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.userId").isNotEmpty())
                .andExpect(jsonPath("$.email").value("joao.silva@example.com"))
                .andExpect(jsonPath("$.displayName").value("João Silva"))
                .andExpect(jsonPath("$.avatarUrl").value("https://lh3.googleusercontent.com/avatar123"));

        // Verify user was created in database
        final var users = userRepository.findAll();
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getEmail()).isEqualTo("joao.silva@example.com");
        assertThat(users.get(0).getGoogleId()).isEqualTo("google-id-123");
        assertThat(users.get(0).getDisplayName()).isEqualTo("João Silva");
    }

    @Test
    void shouldAuthenticateExistingUserWithGoogleOAuth() throws Exception {
        // Given - user already exists in database
        final var existingUser = new User();
        existingUser.setEmail("joao.silva@example.com");
        existingUser.setGoogleId("google-id-123");
        existingUser.setDisplayName("João Silva Old Name");
        existingUser.setAvatarUrl("https://old-avatar.com/pic.jpg");
        userRepository.save(existingUser);

        final var request = new AuthController.GoogleAuthRequest("valid-google-token");

        // When & Then
        mockMvc.perform(post("/auth/google")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.userId").value(existingUser.getId().toString()))
                .andExpect(jsonPath("$.email").value("joao.silva@example.com"))
                .andExpect(jsonPath("$.displayName").value("João Silva"));  // updated

        // Verify user was updated (not duplicated)
        final var users = userRepository.findAll();
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getId()).isEqualTo(existingUser.getId());
        assertThat(users.get(0).getDisplayName()).isEqualTo("João Silva");  // updated
        assertThat(users.get(0).getAvatarUrl()).isEqualTo("https://lh3.googleusercontent.com/avatar123");  // updated
    }

    @Test
    void shouldReturnUnauthorizedWhenGoogleTokenIsInvalid() throws Exception {
        // Given - invalid Google token
        when(googleTokenValidator.validateToken(anyString()))
                .thenThrow(new InvalidTokenException("Token inválido ou expirado"));

        final var request = new AuthController.GoogleAuthRequest("invalid-google-token");

        // When & Then
        mockMvc.perform(post("/auth/google")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        // Verify no user was created
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void shouldReturnBadRequestWhenGoogleTokenIsNull() throws Exception {
        // Given - null Google token
        final var request = new AuthController.GoogleAuthRequest(null);

        // When & Then
        mockMvc.perform(post("/auth/google")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenGoogleTokenIsEmpty() throws Exception {
        // Given - empty Google token
        final var request = new AuthController.GoogleAuthRequest("");

        // When & Then
        mockMvc.perform(post("/auth/google")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenGoogleTokenIsBlank() throws Exception {
        // Given - blank Google token (only whitespace)
        final var request = new AuthController.GoogleAuthRequest("   ");

        // When & Then
        mockMvc.perform(post("/auth/google")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGenerateValidJwtToken() throws Exception {
        // Given - valid Google ID token
        final var request = new AuthController.GoogleAuthRequest("valid-google-token");

        // When
        final var mvcResult = mockMvc.perform(post("/auth/google")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // Then - verify JWT token is present and non-empty
        final var responseBody = mvcResult.getResponse().getContentAsString();
        final var authResponse = objectMapper.readTree(responseBody);
        final var jwtToken = authResponse.get("token").asText();

        assertThat(jwtToken).isNotNull();
        assertThat(jwtToken).isNotEmpty();
        assertThat(jwtToken).contains(".");  // JWT has dot-separated parts
    }

    // =========================================================================
    // POST /auth/login - Simple Login (MVP/Testing endpoint)
    // =========================================================================

    @Test
    void shouldLoginWithValidEmail() throws Exception {
        // Given - user exists in database
        final var existingUser = new User();
        existingUser.setEmail("joao.silva@example.com");
        existingUser.setGoogleId("google-id-123");
        existingUser.setDisplayName("João Silva");
        userRepository.save(existingUser);

        final var request = new AuthController.LoginRequest("joao.silva@example.com");

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.userId").value(existingUser.getId().toString()))
                .andExpect(jsonPath("$.email").value("joao.silva@example.com"))
                .andExpect(jsonPath("$.displayName").value("João Silva"));
    }

    @Test
    void shouldReturnBadRequestWhenLoginEmailIsInvalid() throws Exception {
        // Given - invalid email format
        final var request = new AuthController.LoginRequest("invalid-email");

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnInternalServerErrorWhenLoginEmailNotFound() throws Exception {
        // Given - user does not exist
        final var request = new AuthController.LoginRequest("notfound@example.com");

        // When & Then - returns 500 with IllegalArgumentException
        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldReturnBadRequestWhenLoginEmailIsNull() throws Exception {
        // Given - null email
        final var request = new AuthController.LoginRequest(null);

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // User Information Persistence Tests
    // =========================================================================

    @Test
    void shouldStoreCompleteUserInformationAfterGoogleAuth() throws Exception {
        // Given
        final var request = new AuthController.GoogleAuthRequest("valid-google-token");

        // When
        mockMvc.perform(post("/auth/google")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then - verify all user fields are stored
        final var user = userRepository.findByEmail("joao.silva@example.com").orElseThrow();
        assertThat(user.getId()).isNotNull();
        assertThat(user.getEmail()).isEqualTo("joao.silva@example.com");
        assertThat(user.getGoogleId()).isEqualTo("google-id-123");
        assertThat(user.getDisplayName()).isEqualTo("João Silva");
        assertThat(user.getAvatarUrl()).isEqualTo("https://lh3.googleusercontent.com/avatar123");
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldUpdateUserInformationOnSubsequentGoogleAuth() throws Exception {
        // Given - user exists with old information
        final var existingUser = new User();
        existingUser.setEmail("joao.silva@example.com");
        existingUser.setGoogleId("google-id-123");
        existingUser.setDisplayName("Old Name");
        existingUser.setAvatarUrl("https://old-avatar.com/pic.jpg");
        userRepository.save(existingUser);

        // When - authenticate again (Google might have updated user info)
        final var request = new AuthController.GoogleAuthRequest("valid-google-token");
        mockMvc.perform(post("/auth/google")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then - verify user information is updated
        final var updatedUser = userRepository.findByEmail("joao.silva@example.com").orElseThrow();
        assertThat(updatedUser.getId()).isEqualTo(existingUser.getId());  // same user
        assertThat(updatedUser.getDisplayName()).isEqualTo("João Silva");  // updated
        assertThat(updatedUser.getAvatarUrl()).isEqualTo("https://lh3.googleusercontent.com/avatar123");  // updated
    }

}
