package com.winereviewer.api.service.impl;

import com.winereviewer.api.application.dto.request.CreateReviewRequest;
import com.winereviewer.api.application.dto.request.UpdateReviewRequest;
import com.winereviewer.api.application.dto.response.ReviewResponse;
import com.winereviewer.api.application.dto.response.UserSummaryResponse;
import com.winereviewer.api.application.dto.response.WineSummaryResponse;
import com.winereviewer.api.domain.Review;
import com.winereviewer.api.domain.User;
import com.winereviewer.api.domain.Wine;
import com.winereviewer.api.repository.ReviewRepository;
import com.winereviewer.api.repository.UserRepository;
import com.winereviewer.api.repository.WineRepository;
import com.winereviewer.api.service.ReviewService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * @author lucas
 * @date 19/10/2025 08:07
 */
@Slf4j
@Service
@AllArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final WineRepository wineRepository;

    @Override
    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request, UUID userId) {
        final UUID wineId = UUID.fromString(request.wineId());
        final var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        final var wine = wineRepository.findById(wineId)
                .orElseThrow(() -> new IllegalArgumentException("Wine not found"));
        final var review = toReview(request, user, wine);
        reviewRepository.save(review);
        log.info("Review created successfully");
        final var wineSummary = toWineSummary(wine);
        final var userSummary = toUserSummary(user);
        return toReviewResponse(review, userSummary, wineSummary);
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(UUID reviewId, UpdateReviewRequest request, UUID userId) {
        // Find existing review
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found: " + reviewId));

        // Validate ownership (security)
        if (!review.getUser().getId().equals(userId)) {
            throw new SecurityException("User is not the owner of this review");
        }

        // Update fields (only if provided in request)
        if (request.rating() != null) {
            review.setRating(request.rating());
        }
        if (request.notes() != null) {
            review.setNotes(request.notes());
        }
        if (request.imageUrl() != null) {
            review.setImageUrl(request.imageUrl());
        }

        review.setUpdatedAt(Instant.now());
        reviewRepository.save(review);

        log.info("Review updated successfully: {}", reviewId);

        // Convert to response
        var wineSummary = toWineSummary(review.getWine());
        var userSummary = toUserSummary(review.getUser());
        return toReviewResponse(review, userSummary, wineSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getReviewById(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found: " + reviewId));

        var wineSummary = toWineSummary(review.getWine());
        var userSummary = toUserSummary(review.getUser());
        return toReviewResponse(review, userSummary, wineSummary);
    }

    // Private helper methods (ordered by invocation flow)

    private Review toReview(CreateReviewRequest request, User user, Wine wine) {
        Review review = new Review();
        review.setId(UUID.randomUUID());
        review.setRating(request.rating());
        review.setNotes(request.notes());
        review.setImageUrl(request.imageUrl());
        review.setUser(user);
        review.setWine(wine);
        review.setCreatedAt(Instant.now());
        review.setUpdatedAt(Instant.now());
        return review;
    }

    private WineSummaryResponse toWineSummary(Wine wine) {
        return new WineSummaryResponse(
                wine.getId().toString(),
                wine.getName(),
                wine.getWinery(),
                wine.getCountry(),
                wine.getYear(),
                wine.getImageUrl());
    }

    private UserSummaryResponse toUserSummary(User user) {
        return new UserSummaryResponse(
                user.getId().toString(),
                user.getDisplayName(),
                user.getAvatarUrl());
    }

    private ReviewResponse toReviewResponse(
            Review review,
            UserSummaryResponse author,
            WineSummaryResponse wine) {
        return new ReviewResponse(
                review.getId().toString(),
                review.getRating(),
                review.getNotes(),
                review.getImageUrl(),
                LocalDateTime.ofInstant(review.getCreatedAt(), ZoneOffset.UTC),
                LocalDateTime.ofInstant(review.getUpdatedAt(), ZoneOffset.UTC),
                author,
                wine,
                // TODO commentCount
                0);
    }

}
