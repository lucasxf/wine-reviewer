package com.winereviewer.api.integration;

import com.winereviewer.api.application.dto.request.CreateCommentRequest;
import com.winereviewer.api.application.dto.request.UpdateCommentRequest;
import com.winereviewer.api.domain.Comment;
import com.winereviewer.api.domain.Review;
import com.winereviewer.api.domain.User;
import com.winereviewer.api.domain.Wine;
import com.winereviewer.api.repository.CommentRepository;
import com.winereviewer.api.repository.ReviewRepository;
import com.winereviewer.api.repository.UserRepository;
import com.winereviewer.api.repository.WineRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for CommentController with Testcontainers PostgreSQL.
 * <p>
 * Tests all CRUD endpoints against a real PostgreSQL database to verify:
 * - REST API behavior
 * - Comment creation, update, listing, and deletion
 * - Ownership validation (only author can update/delete)
 * - Database constraints (foreign keys, cascade delete)
 * - Exception handling (404, 403, 400)
 * - Pagination and sorting
 * <p>
 * <strong>Test strategy:</strong>
 * - Uses real PostgreSQL via Testcontainers (production-like environment)
 * - Each test is @Transactional (auto-rollback for isolation)
 * - Test data inserted directly via repositories for speed
 * - MockMvc for HTTP request/response testing
 * - authenticated(userId) helper for Spring Security authentication
 *
 * @author lucas
 * @date 01/11/2025
 */
class CommentControllerIT extends AbstractIntegrationTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WineRepository wineRepository;

    @Autowired
    private EntityManager entityManager;

    private User testUser;
    private User anotherUser;
    private Wine testWine;
    private Review testReview;

    @BeforeEach
    void setupTestData() {
        // Create test user
        testUser = new User();
        testUser.setDisplayName("João Silva");
        testUser.setEmail("joao.silva@example.com");
        testUser.setGoogleId("google-id-123");
        testUser.setAvatarUrl("https://example.com/avatar.jpg");
        testUser = userRepository.save(testUser);

        // Create another user for ownership tests
        anotherUser = new User();
        anotherUser.setDisplayName("Maria Santos");
        anotherUser.setEmail("maria.santos@example.com");
        anotherUser.setGoogleId("google-id-456");
        anotherUser = userRepository.save(anotherUser);

        // Create test wine
        testWine = new Wine();
        testWine.setName("Château Margaux 2015");
        testWine.setWinery("Château Margaux");
        testWine.setCountry("França");
        testWine.setGrape("Cabernet Sauvignon");
        testWine.setYear(2015);
        testWine = wineRepository.save(testWine);

        // Create test review
        testReview = new Review();
        testReview.setUser(testUser);
        testReview.setWine(testWine);
        testReview.setRating(5);
        testReview.setNotes("Excelente vinho!");
        testReview = reviewRepository.save(testReview);
    }

    // =========================================================================
    // POST /comments - Create Comment
    // =========================================================================

    @Test
    void shouldCreateCommentWithValidData() throws Exception {
        // Given
        final var request = new CreateCommentRequest(
                testReview.getId().toString(),
                "Concordo plenamente! Vinho magnífico."
        );

        // When
        final ResultActions result = mockMvc.perform(post("/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .with(authenticated(testUser.getId()))
                .content(objectMapper.writeValueAsString(request)));

        // Then
        result.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.text").value("Concordo plenamente! Vinho magnífico."))
                .andExpect(jsonPath("$.author.displayName").value(testUser.getDisplayName()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());

        // Verify database persistence
        final var comments = commentRepository.findAll();
        assertThat(comments).hasSize(1);
        assertThat(comments.getFirst().getContent()).isEqualTo("Concordo plenamente! Vinho magnífico.");
        assertThat(comments.getFirst().getAuthor().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void shouldReturnBadRequestWhenReviewIdIsNull() throws Exception {
        // Given - reviewId is null
        final var request = new CreateCommentRequest(null, "Comentário sem reviewId");

        // When & Then
        mockMvc.perform(post("/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authenticated(testUser.getId()))
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenTextIsBlank() throws Exception {
        // Given - text is blank
        final var request = new CreateCommentRequest(
                testReview.getId().toString(),
                "   "
        );

        // When & Then
        mockMvc.perform(post("/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authenticated(testUser.getId()))
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenReviewDoesNotExist() throws Exception {
        // Given - non-existent review ID
        final var nonExistentReviewId = UUID.randomUUID().toString();
        final var request = new CreateCommentRequest(
                nonExistentReviewId,
                "Comentário em review inexistente"
        );

        // When & Then
        mockMvc.perform(post("/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authenticated(testUser.getId()))
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnForbiddenWhenNotAuthenticated() throws Exception {
        // Given - no authentication
        final var request = new CreateCommentRequest(
                testReview.getId().toString(),
                "Sem autenticação"
        );

        // When & Then - DO NOT add .with(authenticated())
        mockMvc.perform(post("/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // PUT /comments - Update Comment
    // =========================================================================

    @Test
    void shouldUpdateCommentWhenUserIsAuthor() throws Exception {
        // Given - create a comment first
        final var comment = createTestComment(testReview, testUser, "Comentário original");

        final var updateRequest = new UpdateCommentRequest(
                comment.getId().toString(),
                "Comentário atualizado com novo texto"
        );

        // When & Then
        mockMvc.perform(put("/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authenticated(testUser.getId()))
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(comment.getId().toString()))
                .andExpect(jsonPath("$.text").value("Comentário atualizado com novo texto"))
                .andExpect(jsonPath("$.author.displayName").value(testUser.getDisplayName()));

        // Verify database update
        final var updatedComment = commentRepository.findById(comment.getId()).orElseThrow();
        assertThat(updatedComment.getContent()).isEqualTo("Comentário atualizado com novo texto");
    }

    @Test
    void shouldReturnForbiddenWhenUpdatingCommentFromAnotherUser() throws Exception {
        // Given - comment created by testUser, but anotherUser tries to update
        final var comment = createTestComment(testReview, testUser, "Comentário do João");

        final var updateRequest = new UpdateCommentRequest(
                comment.getId().toString(),
                "Maria tentando atualizar"
        );

        // When & Then - anotherUser cannot update testUser's comment
        mockMvc.perform(put("/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authenticated(anotherUser.getId()))
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isForbidden());

        // Verify comment was NOT updated
        final var unchangedComment = commentRepository.findById(comment.getId()).orElseThrow();
        assertThat(unchangedComment.getContent()).isEqualTo("Comentário do João");
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentComment() throws Exception {
        // Given - non-existent comment ID
        final var nonExistentId = UUID.randomUUID().toString();
        final var updateRequest = new UpdateCommentRequest(nonExistentId, "Texto novo");

        // When & Then
        mockMvc.perform(put("/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authenticated(testUser.getId()))
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestWhenUpdateTextIsBlank() throws Exception {
        // Given
        final var comment = createTestComment(testReview, testUser, "Comentário original");
        final var updateRequest = new UpdateCommentRequest(
                comment.getId().toString(),
                "   "  // blank text
        );

        // When & Then
        mockMvc.perform(put("/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authenticated(testUser.getId()))
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // GET /comments - List Comments by User
    // =========================================================================

    @Test
    void shouldListCommentsCreatedByAuthenticatedUser() throws Exception {
        // Given - create multiple comments by testUser
        createTestComment(testReview, testUser, "Comentário 1");
        createTestComment(testReview, testUser, "Comentário 2");
        createTestComment(testReview, testUser, "Comentário 3");

        // Create comment by another user (should NOT appear)
        createTestComment(testReview, anotherUser, "Comentário da Maria");

        // When & Then
        mockMvc.perform(get("/comments")
                        .with(authenticated(testUser.getId())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void shouldListCommentsWithPagination() throws Exception {
        // Given - create 5 comments
        for (int i = 1; i <= 5; i++) {
            createTestComment(testReview, testUser, "Comentário " + i);
        }

        // When & Then - request page 0 with size 2
        mockMvc.perform(get("/comments")
                        .with(authenticated(testUser.getId()))
                        .param("page", "0")
                        .param("size", "2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    // =========================================================================
    // GET /comments/{reviewId} - List Comments by Review
    // =========================================================================

    @Test
    void shouldListCommentsForSpecificReview() throws Exception {
        // Given - create comments on testReview
        createTestComment(testReview, testUser, "Comentário 1");
        createTestComment(testReview, anotherUser, "Comentário 2");

        // Create another review with comments (should NOT appear)
        final var anotherReview = new Review();
        anotherReview.setUser(anotherUser);
        anotherReview.setWine(testWine);
        anotherReview.setRating(4);
        anotherReview.setNotes("Outro review");
        final var savedReview = reviewRepository.save(anotherReview);

        createTestComment(savedReview, testUser, "Comentário em outro review");

        // When & Then - list comments from testReview only
        mockMvc.perform(get("/comments/{reviewId}", testReview.getId())
                        .with(authenticated(testUser.getId())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void shouldReturnNotFoundWhenListingCommentsForNonExistentReview() throws Exception {
        // Given - non-existent review ID
        final var nonExistentReviewId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(get("/comments/{reviewId}", nonExistentReviewId)
                        .with(authenticated(testUser.getId())))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldListCommentsWithPaginationByReview() throws Exception {
        // Given - create 5 comments on same review
        for (int i = 1; i <= 5; i++) {
            createTestComment(testReview, testUser, "Comentário " + i);
        }

        // When & Then - request page 0 with size 2
        mockMvc.perform(get("/comments/{reviewId}", testReview.getId())
                        .with(authenticated(testUser.getId()))
                        .param("page", "0")
                        .param("size", "2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    // =========================================================================
    // DELETE /comments/{commentId} - Delete Comment
    // =========================================================================

    @Test
    void shouldDeleteCommentWhenUserIsAuthor() throws Exception {
        // Given - create a comment
        final var comment = createTestComment(testReview, testUser, "Comentário a ser deletado");
        final var commentId = comment.getId();

        // When & Then - delete comment
        mockMvc.perform(delete("/comments/{commentId}", commentId)
                        .with(authenticated(testUser.getId())))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Verify comment was deleted from database
        assertThat(commentRepository.findById(commentId)).isEmpty();
    }

    @Test
    void shouldReturnForbiddenWhenDeletingCommentFromAnotherUser() throws Exception {
        // Given - comment created by testUser, but anotherUser tries to delete
        final var comment = createTestComment(testReview, testUser, "Comentário do João");
        final var commentId = comment.getId();

        // When & Then - anotherUser cannot delete testUser's comment
        mockMvc.perform(delete("/comments/{commentId}", commentId)
                        .with(authenticated(anotherUser.getId())))
                .andDo(print())
                .andExpect(status().isForbidden());

        // Verify comment was NOT deleted
        assertThat(commentRepository.findById(commentId)).isPresent();
        final var unchangedComment = commentRepository.findById(commentId).orElseThrow();
        assertThat(unchangedComment.getContent()).isEqualTo("Comentário do João");
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentComment() throws Exception {
        // Given - non-existent comment ID
        final var nonExistentId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(delete("/comments/{commentId}", nonExistentId)
                        .with(authenticated(testUser.getId())))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnForbiddenWhenDeletingWithoutAuthentication() throws Exception {
        // Given - create a comment
        final var comment = createTestComment(testReview, testUser, "Comentário sem autenticação");

        // When & Then - NO authentication provided
        mockMvc.perform(delete("/comments/{commentId}", comment.getId()))
                .andDo(print())
                .andExpect(status().isForbidden());

        // Verify comment was NOT deleted
        assertThat(commentRepository.findById(comment.getId())).isPresent();
    }

    // =========================================================================
    // Database Constraints Tests
    // =========================================================================

    @Test
    void shouldCascadeDeleteCommentsWhenReviewIsDeleted() {
        // Given - create a comment
        final var comment = createTestComment(testReview, testUser, "Comentário a ser deletado");
        final var commentId = comment.getId();

        // When - delete review (should cascade to comments via database ON DELETE CASCADE)
        reviewRepository.delete(testReview);
        reviewRepository.flush();
        entityManager.clear(); // Clear persistence context to force re-query from database

        // Then - comment should be deleted by database cascade
        assertThat(commentRepository.findById(commentId)).isEmpty();
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private Comment createTestComment(Review review, User author, String text) {
        final var comment = new Comment();
        comment.setReview(review);
        comment.setAuthor(author);
        comment.setContent(text);
        return commentRepository.save(comment);
    }

}
