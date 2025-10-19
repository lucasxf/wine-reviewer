package com.winereviewer.api.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Summary DTO for a user.
 *
 * Contains minimal user information for embedding in other responses
 * (e.g., review author, comment author).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserSummaryResponse(
    String id,
    String displayName,
    String avatarUrl
) {
}
