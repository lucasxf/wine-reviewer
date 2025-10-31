package com.winereviewer.api.service.impl;

import com.winereviewer.api.application.dto.request.CreateCommentRequest;
import com.winereviewer.api.application.dto.request.UpdateCommentRequest;
import com.winereviewer.api.application.dto.response.CommentResponse;
import com.winereviewer.api.application.dto.response.UserSummaryResponse;
import com.winereviewer.api.domain.Comment;
import com.winereviewer.api.domain.Review;
import com.winereviewer.api.domain.User;
import com.winereviewer.api.exception.ResourceNotFoundException;
import com.winereviewer.api.exception.UnauthorizedAccessException;
import com.winereviewer.api.repository.CommentRepository;
import com.winereviewer.api.repository.ReviewRepository;
import com.winereviewer.api.repository.UserRepository;
import com.winereviewer.api.service.CommentService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

/**
 * @author lucas
 * @date 30/10/2025 07:52
 */
@Slf4j
@Service
@AllArgsConstructor
public class CommentServiceImpl implements CommentService {

    private static final String COMMENT = "Comment";
    private static final String REVIEW = "Review";
    private static final String USER = "User";

    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CommentResponse addComment(CreateCommentRequest request, UUID userId) {
        final UUID reviewId = UUID.fromString(request.reviewId());

        final var review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException(REVIEW, reviewId));
        final var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER, userId));

        final var content = request.text();
        final var comment = getComment(content, review, user);
        final var author = getAuthor(user);

        commentRepository.save(comment);
        log.info("Comentário \"{}\" adicionado com sucesso. ID: {}", content, comment.getId());

        return getResponse(comment, author);
    }

    @Override
    @Transactional
    public CommentResponse updateComment(UpdateCommentRequest request, UUID userId) {
        final UUID commentId = UUID.fromString(request.commentId());

        final var comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException(COMMENT, commentId));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new UnauthorizedAccessException(userId, COMMENT);
        }
        final var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER, userId));

        final var content = request.text();
        final var author = getAuthor(user);

        comment.setContent(content);

        commentRepository.save(comment);
        log.info("Comentário \"{}\" editado com sucesso. ID: {}", content, comment.getId());

        return getResponse(comment, author);
    }

    @Override
    public Page<CommentResponse> getCommentsPerUser(UUID userId, Pageable pageable) {
        final var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER, userId));
        final List<Comment> commentsPerAuthor =
                commentRepository.findByAuthorOrderByCreatedAtDesc(user);

        final var author = getAuthor(user);

        log.info("Carregados comentários do usuário {}", author);

        return toPageResponse(commentsPerAuthor, author);
    }

    @Override
    public Page<CommentResponse> getCommentsPerReview(UUID reviewId, Pageable pageable) {
        final var review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException(REVIEW, reviewId));
        final List<Comment> commentsPerReview =
                commentRepository.findByReviewOrderByCreatedAtAsc(review);

        final var author = getAuthor(review.getUser());

        log.info("Carregados comentários da avaliação {}", review);

        return toPageResponse(commentsPerReview, author);
    }

    @Override
    public void deleteComment(UUID commentId, UUID userId) {
        final var comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException(COMMENT, commentId));

        if (!userId.equals(comment.getAuthor().getId())) {
            throw new UnauthorizedAccessException(userId, COMMENT);
        }
        commentRepository.delete(comment);
        log.info("Comentário \"{}\" excluído com sucesso.", commentId);
    }

    private Comment getComment(String content, Review review, User user) {
        return new Comment(
                UUID.randomUUID(),
                review,
                user,
                content,
                Instant.now(),
                Instant.now());
    }

    private UserSummaryResponse getAuthor(User user) {
        return new UserSummaryResponse(
                user.getId().toString(),
                user.getDisplayName(),
                user.getAvatarUrl());
    }

    private CommentResponse getResponse(Comment comment, UserSummaryResponse author) {
        return new CommentResponse(
                comment.getId().toString(),
                comment.getContent(),
                LocalDateTime.ofInstant(comment.getCreatedAt(), ZoneOffset.UTC),
                LocalDateTime.ofInstant(comment.getUpdatedAt(), ZoneOffset.UTC),
                author);
    }

    private Page<CommentResponse> toPageResponse(List<Comment> comments, UserSummaryResponse author) {
        final Pageable pageable = Pageable.ofSize(comments.size());
        final var commentsResponse = comments.stream()
                .map(c -> getResponse(c, author))
                .toList();

        return new PageImpl<>(commentsResponse, pageable, comments.size());
    }

}
