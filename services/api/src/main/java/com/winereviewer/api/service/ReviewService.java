package com.winereviewer.api.service;

import com.winereviewer.api.application.dto.request.CreateReviewRequest;
import com.winereviewer.api.application.dto.request.UpdateReviewRequest;
import com.winereviewer.api.application.dto.response.ReviewResponse;
import com.winereviewer.api.exception.ResourceNotFoundException;
import com.winereviewer.api.exception.UnauthorizedAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service for managing wine reviews.
 *
 * @author lucas
 * @date 19/10/2025 08:02
 */
public interface ReviewService {

    /**
     * Creates a new review for a wine.
     *
     * @param request the review data
     * @param userId  the authenticated user ID
     * @return the created review
     * @throws IllegalArgumentException if wine or user not found
     */
    ReviewResponse createReview(CreateReviewRequest request, UUID userId);

    /**
     * Updates an existing review.
     *
     * @param reviewId the review ID to update
     * @param request  the updated review data
     * @param userId   the authenticated user ID (must be the review owner)
     * @return the updated review
     * @throws IllegalArgumentException if review not found
     * @throws SecurityException        if user is not the review owner
     */
    ReviewResponse updateReview(UUID reviewId, UpdateReviewRequest request, UUID userId);

    /**
     * Gets a review by its ID.
     *
     * @param reviewId the review ID
     * @return the review
     * @throws ResourceNotFoundException if review not found
     */
    ReviewResponse getReviewById(UUID reviewId);

    /**
     * Lists reviews with optional filters and pagination.
     *
     * @param wineId   optional wine ID filter
     * @param userId   optional user ID filter
     * @param pageable pagination parameters (page, size, sort)
     * @return paginated list of reviews
     */
    Page<ReviewResponse> listReviews(UUID wineId, UUID userId, Pageable pageable);

    /**
     * Deletes a review by its ID.
     *
     * @param reviewId the review ID to delete
     * @param userId   the authenticated user ID (must be the review owner)
     * @throws ResourceNotFoundException if review not found
     * @throws UnauthorizedAccessException if user is not the review owner
     */
    void deleteReview(UUID reviewId, UUID userId);

}
