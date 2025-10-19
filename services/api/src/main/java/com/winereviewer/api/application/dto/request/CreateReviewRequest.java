package com.winereviewer.api.application.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new wine review.
 * <p>
 * Used when a user posts a review with rating, optional notes, and optional photo.
 */
public record CreateReviewRequest(

        @NotNull(message = "{review.wine.required}")
        String wineId,

        @NotNull(message = "{review.rating.required}")
        @Min(value = 1, message = "{review.rating.min}")
        @Max(value = 5, message = "{review.rating.max}")
        Integer rating,

        @Size(max = 1000, message = "{review.notes.size}")
        String notes,

        String imageUrl
) {
}
