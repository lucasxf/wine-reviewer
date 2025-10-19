package com.winereviewer.api.application.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating an existing wine review.
 * <p>
 * All fields are optional - only provided fields will be updated.
 */
public record UpdateReviewRequest(

        @Min(value = 1, message = "{review.rating.min}")
        @Max(value = 5, message = "{review.rating.max}")
        Integer rating,

        @Size(max = 1000, message = "{review.notes.size}")
        String notes,

        String imageUrl
) {
}
