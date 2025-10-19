package com.winereviewer.api.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Summary DTO for a wine.
 *
 * Contains minimal wine information for embedding in review responses.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record WineSummaryResponse(
    String id,
    String name,
    String winery,
    String country,
    Integer year,
    String imageUrl
) {
}
