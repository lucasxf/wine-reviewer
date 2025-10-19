package com.winereviewer.api.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating an existing comment.
 */
public record UpdateCommentRequest(

        @NotBlank(message = "{comment.text.blank}")
        @Size(min = 1, max = 500, message = "{comment.text.size}")
        String text
) {
}
