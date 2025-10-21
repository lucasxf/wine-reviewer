package com.winereviewer.api.service;

import com.winereviewer.api.application.dto.request.CreateReviewRequest;
import com.winereviewer.api.application.dto.request.UpdateReviewRequest;
import com.winereviewer.api.domain.Review;
import com.winereviewer.api.domain.User;
import com.winereviewer.api.domain.Wine;
import com.winereviewer.api.exception.ResourceNotFoundException;
import com.winereviewer.api.exception.UnauthorizedAccessException;
import com.winereviewer.api.repository.ReviewRepository;
import com.winereviewer.api.repository.UserRepository;
import com.winereviewer.api.repository.WineRepository;
import com.winereviewer.api.service.impl.ReviewServiceImpl;
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

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WineRepository wineRepository;

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
    }

    @Test
    void givenReviewRequestAndUserId_WhenCreateReview_ThenReturnReview() {
        // given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(wineRepository.findById(wineId)).thenReturn(Optional.of(wine));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        // when
        final var response = reviewService.createReview(
                new CreateReviewRequest(
                        wineId.toString(),
                        5,
                        "Great wine!",
                        "https://example.com/photo.jpg"),
                userId);

        // then
        final var author = response.author();
        final var wineResponse = response.wine();
        assertNotNull(response);
        assertEquals(5, response.rating());
        assertEquals("Great wine!", response.notes());
        assertEquals("https://example.com/photo.jpg", response.imageUrl());
        assertEquals(userId.toString(), author.id());
        assertEquals(wineId.toString(), wineResponse.id());

        verify(userRepository, times(1)).findById(userId);
        verify(wineRepository, times(1)).findById(wineId);
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void givenUserNotFound_WhenCreateReview_ThenThrowException() {
        // given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        final var exception = assertThrows(ResourceNotFoundException.class, () ->
                reviewService.createReview(
                        new CreateReviewRequest(wineId.toString(), 5, "Great wine!", null),
                        userId)
        );

        assertTrue(exception.getMessage().contains("User"));
        assertTrue(exception.getMessage().contains(userId.toString()));
        verify(userRepository, times(1)).findById(userId);
        verify(wineRepository, never()).findById(any());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void givenWineNotFound_WhenCreateReview_ThenThrowException() {
        // given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(wineRepository.findById(wineId)).thenReturn(Optional.empty());

        // when & then
        final var exception = assertThrows(ResourceNotFoundException.class, () ->
                reviewService.createReview(
                        new CreateReviewRequest(wineId.toString(), 5, "Great wine!", null),
                        userId)
        );

        assertTrue(exception.getMessage().contains("Wine"));
        assertTrue(exception.getMessage().contains(wineId.toString()));
        verify(userRepository, times(1)).findById(userId);
        verify(wineRepository, times(1)).findById(wineId);
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void givenReviewWithoutImage_WhenCreateReview_ThenReturnReviewWithoutImage() {
        // given
        review.setImageUrl(null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(wineRepository.findById(wineId)).thenReturn(Optional.of(wine));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        // when
        final var response = reviewService.createReview(
                new CreateReviewRequest(wineId.toString(), 4, "Good wine", null),
                userId);

        // then
        assertNotNull(response);
        assertNull(response.imageUrl());
        assertEquals(4, response.rating());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void givenReviewWithoutNotes_WhenCreateReview_ThenReturnReviewWithoutNotes() {
        // given
        review.setNotes(null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(wineRepository.findById(wineId)).thenReturn(Optional.of(wine));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        // when
        final var response = reviewService.createReview(
                new CreateReviewRequest(wineId.toString(), 3, null, "https://example.com/photo.jpg"),
                userId);

        // then
        assertNotNull(response);
        assertNull(response.notes());
        assertEquals(3, response.rating());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void givenValidUpdateRequest_WhenUpdateReview_ThenReturnUpdatedReview() {
        // given
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        // when
        final var response = reviewService.updateReview(
                reviewId,
                new UpdateReviewRequest(4, "Updated notes", "https://example.com/new-photo.jpg"),
                userId);

        // then
        assertNotNull(response);
        assertEquals(4, review.getRating());
        assertEquals("Updated notes", review.getNotes());
        assertEquals("https://example.com/new-photo.jpg", review.getImageUrl());
        verify(reviewRepository, times(1)).findById(reviewId);
        verify(reviewRepository, times(1)).save(review);
    }

    @Test
    void givenPartialUpdateRequest_WhenUpdateReview_ThenUpdateOnlyProvidedFields() {
        // given
        final var originalNotes = review.getNotes();
        final var originalImageUrl = review.getImageUrl();
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        // when
        final var response = reviewService.updateReview(
                reviewId,
                new UpdateReviewRequest(3, null, null),
                userId);

        // then
        assertNotNull(response);
        assertEquals(3, review.getRating());
        assertEquals(originalNotes, review.getNotes());
        assertEquals(originalImageUrl, review.getImageUrl());
        verify(reviewRepository, times(1)).save(review);
    }

    @Test
    void givenReviewNotFound_WhenUpdateReview_ThenThrowException() {
        // given
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        // when & then
        final var exception = assertThrows(ResourceNotFoundException.class, () ->
                reviewService.updateReview(
                        reviewId,
                        new UpdateReviewRequest(4, "Updated", null),
                        userId)
        );

        assertTrue(exception.getMessage().contains("Review"));
        assertTrue(exception.getMessage().contains(reviewId.toString()));
        verify(reviewRepository, times(1)).findById(reviewId);
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void givenUserNotOwner_WhenUpdateReview_ThenThrowUnauthorizedAccessException() {
        // given
        final var differentUserId = UUID.randomUUID();
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        // when & then
        final var exception = assertThrows(UnauthorizedAccessException.class, () ->
                reviewService.updateReview(
                        reviewId,
                        new UpdateReviewRequest(4, "Updated", null),
                        differentUserId)
        );

        assertTrue(exception.getMessage().contains(differentUserId.toString()));
        assertTrue(exception.getMessage().contains("não tem permissão"));
        verify(reviewRepository, times(1)).findById(reviewId);
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void givenReviewId_WhenGetReviewById_ThenReturnReview() {
        // given
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        // when
        final var response = reviewService.getReviewById(reviewId);

        // then
        assertNotNull(response);
        assertEquals(reviewId.toString(), response.id());
        assertEquals(5, response.rating());
        assertEquals("Excellent wine!", response.notes());
        assertEquals(userId.toString(), response.author().id());
        assertEquals(wineId.toString(), response.wine().id());
        verify(reviewRepository, times(1)).findById(reviewId);
    }

    @Test
    void givenReviewNotFound_WhenGetReviewById_ThenThrowException() {
        // given
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        // when & then
        final var exception = assertThrows(ResourceNotFoundException.class, () ->
                reviewService.getReviewById(reviewId)
        );

        assertTrue(exception.getMessage().contains("Review"));
        assertTrue(exception.getMessage().contains(reviewId.toString()));
        verify(reviewRepository, times(1)).findById(reviewId);
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