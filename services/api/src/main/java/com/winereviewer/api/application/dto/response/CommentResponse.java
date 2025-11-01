package com.winereviewer.api.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * Response DTO for a comment on a review.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CommentResponse(
        String id,
        String text,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        UserSummaryResponse author) {
}
