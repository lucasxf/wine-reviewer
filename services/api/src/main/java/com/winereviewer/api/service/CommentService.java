package com.winereviewer.api.service;

import com.winereviewer.api.application.dto.request.CreateCommentRequest;
import com.winereviewer.api.application.dto.request.UpdateCommentRequest;
import com.winereviewer.api.application.dto.response.CommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * @author lucas
 * @date 30/10/2025 07:45
 */
public interface CommentService {

    CommentResponse addComment(CreateCommentRequest request, UUID userId);

    CommentResponse updateComment(UpdateCommentRequest request, UUID userId);

    Page<CommentResponse> getCommentsPerUser(UUID userId, Pageable pagination);

    Page<CommentResponse> getCommentsPerReview(UUID reviewId, Pageable pagination);

    void deleteComment(UUID commentId, UUID userId);

}
