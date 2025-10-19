package com.winereviewer.api.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * Response DTO for a complete wine review.
 * <p>
 * Includes full details of the review, author, and wine.
 * Used when fetching a single review or detailed review list.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReviewResponse(
        String id,
        Integer rating,
        String notes,
        String imageUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        UserSummaryResponse author,
        WineSummaryResponse wine,
        Integer commentCount
) {
}
