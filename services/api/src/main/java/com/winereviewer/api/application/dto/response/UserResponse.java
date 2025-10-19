package com.winereviewer.api.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * Response DTO for complete user details.
 * <p>
 * Includes user profile information.
 * Email is only included for the authenticated user (self).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(
        String id,
        String displayName,
        String email,
        String avatarUrl,
        LocalDateTime createdAt,
        Integer reviewCount,
        Integer commentCount
) {
}
