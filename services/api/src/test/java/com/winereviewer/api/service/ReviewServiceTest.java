package com.winereviewer.api.service;

import com.winereviewer.api.application.dto.request.CreateReviewRequest;
import com.winereviewer.api.application.dto.request.UpdateReviewRequest;
import com.winereviewer.api.domain.Review;
import com.winereviewer.api.domain.User;
import com.winereviewer.api.domain.Wine;
import com.winereviewer.api.exception.ResourceNotFoundException;
import com.winereviewer.api.exception.UnauthorizedAccessException;
import com.winereviewer.api.repository.CommentRepository;
import com.winereviewer.api.repository.ReviewRepository;
import com.winereviewer.api.repository.UserRepository;
import com.winereviewer.api.repository.WineRepository;
import com.winereviewer.api.service.impl.ReviewServiceImpl;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WineRepository wineRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private UUID userId;
    private UUID wineId;
    private UUID reviewId;
    private User user;
    private Wine wine;
    private Review review;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        wineId = UUID.randomUUID();
        reviewId = UUID.randomUUID();
        user = getUser(userId);
        wine = getWine(wineId);
        review = getReview(reviewId, user, wine);

        // Mock comment count (default to 0, tests can override if needed)
        // Using lenient() because not all tests will use this mock
        lenient().when(commentRepository.countCommentByReview(any(UUID.class)))
                .thenReturn(0L);
    }

    @Test
    void shouldCreateReviewWhenValidDataProvided() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(wineRepository.findById(wineId)).thenReturn(Optional.of(wine));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        // When
        final var response = reviewService.createReview(
                new CreateReviewRequest(
                        wineId.toString(),
                        5,
                        "Great wine!",
                        "https://example.com/photo.jpg"),
                userId);

        // Then
        final var author = response.author();
        final var wineResponse = response.wine();
        assertThat(response).isNotNull();
        assertThat(response.rating()).isEqualTo(5);
        assertThat(response.notes()).isEqualTo("Great wine!");
        assertThat(response.imageUrl()).isEqualTo("https://example.com/photo.jpg");
        assertThat(author.id()).isEqualTo(userId.toString());
        assertThat(wineResponse.id()).isEqualTo(wineId.toString());

        verify(userRepository, times(1)).findById(userId);
        verify(wineRepository, times(1)).findById(wineId);
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        final CreateReviewRequest request = getCreateReviewRequest();

        // When & Then
        assertThatThrownBy(() -> reviewService.createReview(request, userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User", userId.toString());

        verify(userRepository, times(1)).findById(userId);
        verify(wineRepository, never()).findById(any());
        verify(reviewRepository, never()).save(any());
    }

    private @NotNull CreateReviewRequest getCreateReviewRequest() {
        return new CreateReviewRequest(wineId.toString(), 5, "Great wine!", null);
    }

    @Test
    void shouldThrowExceptionWhenWineNotFound() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(wineRepository.findById(wineId)).thenReturn(Optional.empty());

        // When & Then
        var request = getCreateReviewRequest();

        assertThatThrownBy(() -> reviewService.createReview(request, userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Wine", wineId.toString());

        verify(userRepository, times(1)).findById(userId);
        verify(wineRepository, times(1)).findById(wineId);
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void shouldCreateReviewWithoutImageWhenImageNotProvided() {
        // Given
        review.setImageUrl(null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(wineRepository.findById(wineId)).thenReturn(Optional.of(wine));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        // When
        final var response = reviewService.createReview(
                new CreateReviewRequest(wineId.toString(), 4, "Good wine", null),
                userId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.imageUrl()).isNull();
        assertThat(response.rating()).isEqualTo(4);
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void shouldCreateReviewWithoutNotesWhenNotesNotProvided() {
        // Given
        review.setNotes(null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(wineRepository.findById(wineId)).thenReturn(Optional.of(wine));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        // When
        final var response = reviewService.createReview(
                new CreateReviewRequest(wineId.toString(), 3, null, "https://example.com/photo.jpg"),
                userId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.notes()).isNull();
        assertThat(response.rating()).isEqualTo(3);
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void shouldUpdateReviewWhenValidDataProvided() {
        // Given
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        // When
        final var response = reviewService.updateReview(
                reviewId,
                new UpdateReviewRequest(4, "Updated notes", "https://example.com/new-photo.jpg"),
                userId);

        // Then
        assertThat(response).isNotNull();
        assertThat(review.getRating()).isEqualTo(4);
        assertThat(review.getNotes()).isEqualTo("Updated notes");
        assertThat(review.getImageUrl()).isEqualTo("https://example.com/new-photo.jpg");
        verify(reviewRepository, times(1)).findById(reviewId);
        verify(reviewRepository, times(1)).save(review);
    }

    @Test
    void shouldUpdateOnlyProvidedFieldsWhenPartialUpdateRequested() {
        // Given
        final var originalNotes = review.getNotes();
        final var originalImageUrl = review.getImageUrl();
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        // When
        final var response = reviewService.updateReview(
                reviewId,
                new UpdateReviewRequest(3, null, null),
                userId);

        // Then
        assertThat(response).isNotNull();
        assertThat(review.getRating()).isEqualTo(3);
        assertThat(review.getNotes()).isEqualTo(originalNotes);
        assertThat(review.getImageUrl()).isEqualTo(originalImageUrl);
        verify(reviewRepository, times(1)).save(review);
    }

    @Test
    void shouldThrowExceptionWhenReviewNotFoundForUpdate() {
        // Given
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        // When & Then
        final UpdateReviewRequest request = getUpdateReviewRequest();

        assertThatThrownBy(() -> reviewService.updateReview(reviewId, request, userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Review", reviewId.toString());

        verify(reviewRepository, times(1)).findById(reviewId);
        verify(reviewRepository, never()).save(any());
    }

    private @NotNull UpdateReviewRequest getUpdateReviewRequest() {
        return new UpdateReviewRequest(4, "Updated", null);
    }

    @Test
    void shouldThrowUnauthorizedExceptionWhenUserNotOwnerOnUpdate() {
        // Given
        final var differentUserId = UUID.randomUUID();
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        var request = getUpdateReviewRequest();

        // When & Then
        assertThatThrownBy(() -> reviewService.updateReview(reviewId, request, differentUserId))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("não tem permissão", differentUserId.toString());

        verify(reviewRepository, times(1)).findById(reviewId);
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void shouldReturnReviewWhenValidIdProvided() {
        // Given
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        // When
        final var response = reviewService.getReviewById(reviewId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(reviewId.toString());
        assertThat(response.rating()).isEqualTo(5);
        assertThat(response.notes()).isEqualTo("Excellent wine!");
        assertThat(response.author().id()).isEqualTo(userId.toString());
        assertThat(response.wine().id()).isEqualTo(wineId.toString());
        verify(reviewRepository, times(1)).findById(reviewId);
    }

    @Test
    void shouldThrowExceptionWhenReviewNotFoundForGet() {
        // Given
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reviewService.getReviewById(reviewId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Review", reviewId.toString());

        verify(reviewRepository, times(1)).findById(reviewId);
    }

    @Test
    void shouldReturnAllReviewsPaginatedWhenNoFiltersProvided() {
        // Given
        final var pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        final var reviews = List.of(review);
        final var page = new PageImpl<>(reviews, pageable, reviews.size());
        when(reviewRepository.findAll(pageable)).thenReturn(page);

        // When
        final var response = reviewService.listReviews(null, null, pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().getFirst().id()).isEqualTo(reviewId.toString());
        verify(reviewRepository, times(1)).findAll(pageable);
        verify(reviewRepository, never()).findByWine(any(), any());
        verify(reviewRepository, never()).findByUser(any(), any());
        verify(reviewRepository, never()).findByWineAndUser(any(), any(), any());
    }

    @Test
    void shouldReturnReviewsForWineWhenWineIdFilterProvided() {
        // Given
        final var pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        final var reviews = List.of(review);
        final var page = new PageImpl<>(reviews, pageable, reviews.size());
        when(wineRepository.findById(wineId)).thenReturn(Optional.of(wine));
        when(reviewRepository.findByWine(wine, pageable)).thenReturn(page);

        // When
        final var response = reviewService.listReviews(wineId, null, pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent().getFirst().wine().id()).isEqualTo(wineId.toString());
        verify(wineRepository, times(1)).findById(wineId);
        verify(reviewRepository, times(1)).findByWine(wine, pageable);
        verify(reviewRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void shouldReturnReviewsForUserWhenUserIdFilterProvided() {
        // Given
        final var pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        final var reviews = List.of(review);
        final var page = new PageImpl<>(reviews, pageable, reviews.size());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(reviewRepository.findByUser(user, pageable)).thenReturn(page);

        // When
        final var response = reviewService.listReviews(null, userId, pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent().getFirst().author().id()).isEqualTo(userId.toString());
        verify(userRepository, times(1)).findById(userId);
        verify(reviewRepository, times(1)).findByUser(user, pageable);
        verify(reviewRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void shouldReturnReviewsForWineAndUserWhenBothFiltersProvided() {
        // Given
        final var pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        final var reviews = List.of(review);
        final var page = new PageImpl<>(reviews, pageable, reviews.size());
        when(wineRepository.findById(wineId)).thenReturn(Optional.of(wine));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(reviewRepository.findByWineAndUser(wine, user, pageable)).thenReturn(page);

        // When
        final var response = reviewService.listReviews(wineId, userId, pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent().getFirst().wine().id()).isEqualTo(wineId.toString());
        assertThat(response.getContent().getFirst().author().id()).isEqualTo(userId.toString());
        verify(wineRepository, times(1)).findById(wineId);
        verify(userRepository, times(1)).findById(userId);
        verify(reviewRepository, times(1)).findByWineAndUser(wine, user, pageable);
        verify(reviewRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void shouldThrowExceptionWhenInvalidWineIdProvidedForList() {
        // Given
        final var invalidWineId = UUID.randomUUID();
        final var pageable = PageRequest.of(0, 10);
        when(wineRepository.findById(invalidWineId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reviewService.listReviews(invalidWineId, null, pageable))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Wine", invalidWineId.toString());

        verify(wineRepository, times(1)).findById(invalidWineId);
        verify(reviewRepository, never()).findByWine(any(), any());
    }

    @Test
    void shouldThrowExceptionWhenInvalidUserIdProvidedForList() {
        // Given
        final var invalidUserId = UUID.randomUUID();
        final var pageable = PageRequest.of(0, 10);
        when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reviewService.listReviews(null, invalidUserId, pageable))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User", invalidUserId.toString());

        verify(userRepository, times(1)).findById(invalidUserId);
        verify(reviewRepository, never()).findByUser(any(), any());
    }

    @Test
    void shouldDeleteReviewWhenValidIdAndOwnerProvided() {
        // Given
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        // When
        reviewService.deleteReview(reviewId, userId);

        // Then
        verify(reviewRepository, times(1)).findById(reviewId);
        verify(reviewRepository, times(1)).delete(review);
    }

    @Test
    void shouldThrowExceptionWhenReviewNotFoundForDelete() {
        // Given
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reviewService.deleteReview(reviewId, userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Review", reviewId.toString());

        verify(reviewRepository, times(1)).findById(reviewId);
        verify(reviewRepository, never()).delete(any());
    }

    @Test
    void shouldThrowUnauthorizedExceptionWhenUserNotOwnerOnDelete() {
        // Given
        final var differentUserId = UUID.randomUUID();
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        // When & Then
        assertThatThrownBy(() -> reviewService.deleteReview(reviewId, differentUserId))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("não tem permissão", differentUserId.toString());

        verify(reviewRepository, times(1)).findById(reviewId);
        verify(reviewRepository, never()).delete(any());
    }

    private Review getReview(UUID reviewId, User user, Wine wine) {
        final var review = new Review();
        review.setId(reviewId);
        review.setUser(user);
        review.setWine(wine);
        review.setRating(5);
        review.setNotes("Excellent wine!");
        review.setImageUrl("https://example.com/photo.jpg");
        review.setCreatedAt(Instant.now());
        review.setUpdatedAt(Instant.now());
        return review;
    }

    private Wine getWine(UUID wineId) {
        var wine = new Wine();
        wine.setId(wineId);
        wine.setName("Château Margaux");
        wine.setWinery("Château Margaux");
        wine.setCountry("France");
        wine.setGrape("Cabernet Sauvignon");
        wine.setYear(2015);
        wine.setImageUrl("https://example.com/wine.jpg");
        return wine;
    }

    private User getUser(UUID userId) {
        var user = new User();
        user.setId(userId);
        user.setDisplayName("Test User");
        user.setEmail("test@example.com");
        user.setAvatarUrl("https://example.com/avatar.jpg");
        return user;
    }

}
