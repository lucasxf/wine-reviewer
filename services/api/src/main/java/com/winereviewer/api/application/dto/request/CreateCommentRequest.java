package com.winereviewer.api.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new comment on a review.
 */
public record CreateCommentRequest(

        @NotNull(message = "{comment.review.required}")
        String reviewId,

        @NotBlank(message = "{comment.text.blank}")
        @Size(min = 1, max = 500, message = "{comment.text.size}")
        String text) {
}
