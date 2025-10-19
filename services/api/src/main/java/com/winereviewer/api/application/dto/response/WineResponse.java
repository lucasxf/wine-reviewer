package com.winereviewer.api.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * Response DTO for complete wine details.
 * <p>
 * Includes all wine information plus aggregated review data.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record WineResponse(
        String id,
        String name,
        String winery,
        String country,
        String grape,
        Integer year,
        String imageUrl,
        LocalDateTime createdAt,
        Double averageRating,
        Integer reviewCount
) {
}
