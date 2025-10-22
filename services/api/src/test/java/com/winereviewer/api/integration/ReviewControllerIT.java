package com.winereviewer.api.integration;

import com.winereviewer.api.application.dto.request.CreateReviewRequest;
import com.winereviewer.api.application.dto.request.UpdateReviewRequest;
import com.winereviewer.api.domain.User;
import com.winereviewer.api.domain.Wine;
import com.winereviewer.api.domain.Review;
import com.winereviewer.api.repository.UserRepository;
import com.winereviewer.api.repository.WineRepository;
import com.winereviewer.api.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ReviewController with Testcontainers PostgreSQL.
 * <p>
 * Tests all CRUD endpoints against a real PostgreSQL database to verify:
 * - REST API behavior
 * - Database constraints (rating 1-5, cascade delete, foreign keys)
 * - Exception handling (404, 403, 400, 422)
 * - Pagination and sorting
 * - Request/response serialization
 * <p>
 * <strong>Test strategy:</strong>
 * - Uses real PostgreSQL via Testcontainers (production-like environment)
 * - Each test is @Transactional (auto-rollback for isolation)
 * - Test data inserted directly via repositories for speed
 * - MockMvc for HTTP request/response testing
 * - @WithMockUser for authentication bypass (JWT not tested here)
 *
 * @author lucas
 * @date 22/10/2025
 */
class ReviewControllerIT extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WineRepository wineRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private User testUser;
    private Wine testWine;

    @BeforeEach
    void setupTestData() {
        // Create test user
        testUser = new User();
        testUser.setDisplayName("João Silva");
        testUser.setEmail("joao.silva@example.com");
        testUser.setGoogleId("google-id-123");
        testUser.setAvatarUrl("https://example.com/avatar.jpg");
        testUser = userRepository.save(testUser);

        // Create test wine
        testWine = new Wine();
        testWine.setName("Château Margaux 2015");
        testWine.setWinery("Château Margaux");
        testWine.setCountry("França");
        testWine.setGrape("Cabernet Sauvignon");
        testWine.setYear(2015);
        testWine.setImageUrl("https://example.com/wine.jpg");
        testWine = wineRepository.save(testWine);
    }

    // =========================================================================
    // POST /reviews - Create Review
    // =========================================================================

    @Test
    void shouldCreateReviewWithValidData() throws Exception {
        // Given
        final var request = new CreateReviewRequest(
                testWine.getId().toString(),
                5,
                "Excelente vinho! Notas de frutas vermelhas.",
                "https://example.com/review-photo.jpg"
        );

        // When
        final ResultActions result = mockMvc.perform(post("/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .with(authenticated(testUser.getId()))
                .content(objectMapper.writeValueAsString(request)));

        // Then
        result.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.notes").value("Excelente vinho! Notas de frutas vermelhas."))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/review-photo.jpg"))
                .andExpect(jsonPath("$.author.displayName").value(testUser.getDisplayName()))
                .andExpect(jsonPath("$.wine.name").value(testWine.getName()))
                .andExpect(jsonPath("$.commentCount").value(0))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());

        // Verify database persistence
        final var reviews = reviewRepository.findAll();
        assertThat(reviews).hasSize(1);
        assertThat(reviews.getFirst().getRating()).isEqualTo(5);
    }

    @Test
    void shouldCreateReviewWithoutOptionalFields() throws Exception {
        // Given - notes and imageUrl are optional
        final var request = new CreateReviewRequest(
                testWine.getId().toString(),
                4,
                null,
                null
        );

        // When & Then
        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                .with(authenticated(testUser.getId()))
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.notes").doesNotExist()) // null fields are omitted from JSON
                .andExpect(jsonPath("$.imageUrl").doesNotExist()); // null fields are omitted from JSON
    }

    @Test
    void shouldReturnBadRequestWhenRatingIsLessThan1() throws Exception {
        // Given - rating = 0 (invalid)
        final var request = new CreateReviewRequest(
                testWine.getId().toString(),
                0,
                "Rating inválido",
                null
        );

        // When & Then
        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                .with(authenticated(testUser.getId()))
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenRatingIsGreaterThan5() throws Exception {
        // Given - rating = 6 (invalid)
        final var request = new CreateReviewRequest(
                testWine.getId().toString(),
                6,
                "Rating inválido",
                null
        );

        // When & Then
        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                .with(authenticated(testUser.getId()))
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenWineIdIsNull() throws Exception {
        // Given - wineId is null
        final var request = new CreateReviewRequest(
                null,
                5,
                "Wine ID nulo",
                null
        );

        // When & Then
        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                .with(authenticated(testUser.getId()))
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenWineDoesNotExist() throws Exception {
        // Given - non-existent wine ID
        final var nonExistentWineId = UUID.randomUUID().toString();
        final var request = new CreateReviewRequest(
                nonExistentWineId,
                5,
                "Vinho não existe",
                null
        );

        // When & Then
        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                .with(authenticated(testUser.getId()))
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnForbiddenWhenNotAuthenticated() throws Exception {
        // Given - no authentication
        final var request = new CreateReviewRequest(
                testWine.getId().toString(),
                5,
                "Sem autenticação",
                null
        );

        // When & Then - DO NOT add .with(authenticated()) - this test verifies 403 Forbidden
        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // GET /reviews/{id} - Get Review by ID
    // =========================================================================

    @Test
    void shouldGetReviewById() throws Exception {
        // Given - create a review in database
        final var review = createTestReview(testUser, testWine, 5, "Ótimo vinho!");

        // When & Then
        mockMvc.perform(get("/reviews/{id}", review.getId())
                        .with(authenticated(testUser.getId())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(review.getId().toString()))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.notes").value("Ótimo vinho!"))
                .andExpect(jsonPath("$.author.id").value(testUser.getId().toString()))
                .andExpect(jsonPath("$.wine.id").value(testWine.getId().toString()));
    }

    @Test
    void shouldReturnNotFoundWhenReviewDoesNotExist() throws Exception {
        // Given - non-existent review ID
        final var nonExistentId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(get("/reviews/{id}", nonExistentId)
                        .with(authenticated(testUser.getId())))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // GET /reviews - List Reviews with Pagination
    // =========================================================================

    @Test
    void shouldListAllReviews() throws Exception {
        // Given - create multiple reviews
        createTestReview(testUser, testWine, 5, "Excelente");
        createTestReview(testUser, testWine, 4, "Muito bom");
        createTestReview(testUser, testWine, 3, "Bom");

        // When & Then
        mockMvc.perform(get("/reviews")
                        .with(authenticated(testUser.getId())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.number").value(0))  // first page
                .andExpect(jsonPath("$.size").value(20));  // default size
    }

    @Test
    void shouldListReviewsWithPagination() throws Exception {
        // Given - create 5 reviews
        for (int i = 1; i <= 5; i++) {
            createTestReview(testUser, testWine, i, "Review " + i);
        }

        // When & Then - request page 0 with size 2
        mockMvc.perform(get("/reviews")
                        .with(authenticated(testUser.getId()))
                        .param("page", "0")
                        .param("size", "2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void shouldListReviewsSortedByRating() throws Exception {
        // Given - create reviews with different ratings
        createTestReview(testUser, testWine, 3, "Médio");
        createTestReview(testUser, testWine, 5, "Excelente");
        createTestReview(testUser, testWine, 1, "Ruim");

        // When & Then - sort by rating descending
        mockMvc.perform(get("/reviews")
                        .with(authenticated(testUser.getId()))
                        .param("sort", "rating,desc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].rating").value(5))
                .andExpect(jsonPath("$.content[1].rating").value(3))
                .andExpect(jsonPath("$.content[2].rating").value(1));
    }

    @Test
    void shouldFilterReviewsByWineId() throws Exception {
        // Given - create another wine and reviews
        var anotherWine = new Wine();
        anotherWine.setName("Wine 2");
        anotherWine.setWinery("Winery 2");
        anotherWine.setCountry("Brasil");
        anotherWine = wineRepository.save(anotherWine);

        createTestReview(testUser, testWine, 5, "Review for Wine 1");
        createTestReview(testUser, anotherWine, 4, "Review for Wine 2");

        // When & Then - filter by testWine ID
        mockMvc.perform(get("/reviews")
                        .with(authenticated(testUser.getId()))
                        .param("wineId", testWine.getId().toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].wine.id").value(testWine.getId().toString()));
    }

    @Test
    void shouldFilterReviewsByUserId() throws Exception {
        // Given - create another user and reviews
        var anotherUser = new User();
        anotherUser.setDisplayName("Maria Santos");
        anotherUser.setEmail("maria@example.com");
        anotherUser.setGoogleId("google-id-456");
        anotherUser = userRepository.save(anotherUser);

        createTestReview(testUser, testWine, 5, "Review by João");
        createTestReview(anotherUser, testWine, 4, "Review by Maria");

        // When & Then - filter by testUser ID
        mockMvc.perform(get("/reviews")
                        .with(authenticated(testUser.getId()))
                        .param("userId", testUser.getId().toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].author.id").value(testUser.getId().toString()));
    }

    // =========================================================================
    // PUT /reviews/{id} - Update Review
    // =========================================================================

    @Test
    void shouldUpdateReview() throws Exception {
        // Given - create a review
        final var review = createTestReview(testUser, testWine, 3, "Nota inicial");

        final var updateRequest = new UpdateReviewRequest(
                5,
                "Nota atualizada - mudei de opinião!",
                "https://example.com/new-photo.jpg"
        );

        // When & Then
        mockMvc.perform(put("/reviews/{id}", review.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                .with(authenticated(testUser.getId()))
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(review.getId().toString()))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.notes").value("Nota atualizada - mudei de opinião!"))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/new-photo.jpg"));

        // Verify database update
        final var updatedReview = reviewRepository.findById(review.getId()).orElseThrow();
        assertThat(updatedReview.getRating()).isEqualTo(5);
        assertThat(updatedReview.getNotes()).isEqualTo("Nota atualizada - mudei de opinião!");
    }

    @Test
    void shouldUpdateReviewPartially() throws Exception {
        // Given - update only rating
        final var review = createTestReview(testUser, testWine, 3, "Nota inicial");

        final var updateRequest = new UpdateReviewRequest(
                5,
                null,  // don't update notes
                null   // don't update imageUrl
        );

        // When & Then
        mockMvc.perform(put("/reviews/{id}", review.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                .with(authenticated(testUser.getId()))
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.notes").value("Nota inicial"));  // unchanged
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentReview() throws Exception {
        // Given - non-existent review ID
        final var nonExistentId = UUID.randomUUID();
        final var updateRequest = new UpdateReviewRequest(5, "Update", null);

        // When & Then
        mockMvc.perform(put("/reviews/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                .with(authenticated(testUser.getId()))
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingWithInvalidRating() throws Exception {
        // Given - invalid rating
        final var review = createTestReview(testUser, testWine, 3, "Original");
        final var updateRequest = new UpdateReviewRequest(
                10,  // invalid rating > 5
                "Invalid rating",
                null
        );

        // When & Then
        mockMvc.perform(put("/reviews/{id}", review.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                .with(authenticated(testUser.getId()))
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // DELETE /reviews/{id} - Delete Review
    // =========================================================================

    @Test
    void shouldDeleteReview() throws Exception {
        // Given - create a review
        final var review = createTestReview(testUser, testWine, 5, "To be deleted");

        // When & Then
        mockMvc.perform(delete("/reviews/{id}", review.getId())
                        .with(authenticated(testUser.getId())))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Verify database deletion
        assertThat(reviewRepository.findById(review.getId())).isEmpty();
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentReview() throws Exception {
        // Given - non-existent review ID
        final var nonExistentId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(delete("/reviews/{id}", nonExistentId)
                        .with(authenticated(testUser.getId())))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // Database Constraints Tests
    // =========================================================================

    @Test
    void shouldEnforceForeignKeyConstraint() throws Exception {
        // Given - non-existent wine ID (foreign key violation)
        final var nonExistentWineId = UUID.randomUUID().toString();
        final var request = new CreateReviewRequest(
                nonExistentWineId,
                5,
                "Vinho inexistente",
                null
        );

        // When & Then - should fail with 404 or 422
        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                .with(authenticated(testUser.getId()))
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCascadeDeleteReviewsWhenUserIsDeleted() {
        // Given - create a review
        final var review = createTestReview(testUser, testWine, 5, "Review to cascade");
        final var reviewId = review.getId();

        // When - delete user (should cascade to reviews via database ON DELETE CASCADE)
        userRepository.delete(testUser);
        userRepository.flush();
        entityManager.clear(); // Clear Hibernate cache to see database cascade effect

        // Then - review should be deleted by database cascade
        assertThat(reviewRepository.findById(reviewId)).isEmpty();
    }

    @Test
    void shouldCascadeDeleteReviewsWhenWineIsDeleted() {
        // Given - create a review
        final var review = createTestReview(testUser, testWine, 5, "Review to cascade");
        final var reviewId = review.getId();

        // When - delete wine (should cascade to reviews via database ON DELETE CASCADE)
        wineRepository.delete(testWine);
        wineRepository.flush();
        entityManager.clear(); // Clear Hibernate cache to see database cascade effect

        // Then - review should be deleted by database cascade
        assertThat(reviewRepository.findById(reviewId)).isEmpty();
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private Review createTestReview(User user, Wine wine, int rating, String notes) {
        final var review = new Review();
        review.setUser(user);
        review.setWine(wine);
        review.setRating(rating);
        review.setNotes(notes);
        return reviewRepository.save(review);
    }

}
