package com.winereviewer.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.winereviewer.api.application.dto.request.CreateReviewRequest;
import com.winereviewer.api.application.dto.response.ReviewResponse;
import com.winereviewer.api.application.dto.response.UserSummaryResponse;
import com.winereviewer.api.application.dto.response.WineSummaryResponse;
import com.winereviewer.api.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ReviewController.
 * <p>
 * Tests REST endpoints with full Spring context including security and validation.
 * Uses @SpringBootTest for complete integration while mocking only the service layer.
 * <p>
 * <strong>Design decision (Alternativa 4 - @Bean explícito):</strong>
 * - JwtAuthenticationFilter is declared as @Bean in SecurityConfig (not @Component)
 * - This provides clear ownership: filter is part of security configuration
 * - Allows easy mocking in tests while keeping production code clean
 * - Spring manages lifecycle, enabling proper dependency injection
 * <p>
 * <strong>Why @SpringBootTest:</strong>
 * - **Realismo:** Tests with actual security, validation, and error handling
 * - **Fidelidade:** Follows Spring best practices for filter management
 * - **Simplicidade:** No complex test configuration needed
 * - **Velocidade:** Acceptable trade-off for integration-level confidence
 *
 * @author lucas
 * @date 20/10/2025
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewService service;

    @Test
    void shouldCreateReviewWhenValidRequest() throws Exception {
        // Given
        final var wineId = UUID.randomUUID().toString();
        final var userId = UUID.randomUUID();
        final var reviewId = UUID.randomUUID().toString();

        final var request = new CreateReviewRequest(
                wineId,
                5,
                "Excelente vinho! Notas de frutas vermelhas.",
                "https://example.com/photo.jpg"
        );

        final var userSummary = new UserSummaryResponse(
                userId.toString(),
                "João Silva",
                "https://example.com/avatar.jpg"
        );

        final var wineSummary = new WineSummaryResponse(
                wineId,
                "Château Margaux 2015",
                "Château Margaux",
                "França",
                2015,
                "https://example.com/wine.jpg"
        );

        final var expectedResponse = new ReviewResponse(
                reviewId,
                5,
                "Excelente vinho! Notas de frutas vermelhas.",
                "https://example.com/photo.jpg",
                LocalDateTime.now(),
                null,
                userSummary,
                wineSummary,
                0
        );

        when(service.createReview(any(CreateReviewRequest.class), any(UUID.class)))
                .thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(post("/reviews")
                        .with(csrf())
                        .with(user(userId.toString()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(reviewId))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.notes").value("Excelente vinho! Notas de frutas vermelhas."))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/photo.jpg"))
                .andExpect(jsonPath("$.author.displayName").value("João Silva"))
                .andExpect(jsonPath("$.wine.name").value("Château Margaux 2015"))
                .andExpect(jsonPath("$.commentCount").value(0));
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequestWhenRatingIsInvalid() throws Exception {
        // Given - rating fora do intervalo 1-5
        final var request = new CreateReviewRequest(
                UUID.randomUUID().toString(),
                6, // Rating inválido
                "Nota inválida",
                null
        );

        // When & Then
        mockMvc.perform(post("/reviews")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequestWhenWineIdIsNull() throws Exception {
        // Given - wineId nulo
        final var request = new CreateReviewRequest(
                null, // WineId nulo
                5,
                "Bom vinho",
                null
        );

        // When & Then
        mockMvc.perform(post("/reviews")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnForbiddenWhenNotAuthenticated() throws Exception {
        // Given - sem autenticação
        final var request = new CreateReviewRequest(
                UUID.randomUUID().toString(),
                5,
                "Tentando sem autenticação",
                null
        );

        // When & Then - Spring Security bloqueia com 403 Forbidden
        mockMvc.perform(post("/reviews")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

}