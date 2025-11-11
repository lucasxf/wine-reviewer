package com.winereviewer.api.service;

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
import com.winereviewer.api.service.impl.CommentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    private UUID commentId;
    private UUID userId;
    private UUID reviewId;
    private Comment comment;
    private User user;
    private Review review;

    @BeforeEach
    void setUp() {
        commentService = new CommentServiceImpl(commentRepository, reviewRepository, userRepository);
        commentId = UUID.randomUUID();
        userId = UUID.randomUUID();
        reviewId = UUID.randomUUID();
    }

    @Test
    void shouldAddCommentWhenValidDataProvided() {
        // Given
        final var request = getCreateCommentRequest(reviewId);
        user = getUser(userId);
        review = getReview(reviewId, user);
        var author = getAuthor(user);
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        final var response = commentService.addComment(request, userId);

        // Then
        assertThat(response.id()).isNotNull();
        assertThat(response.text()).isEqualTo(request.text());
        assertThat(response.createdAt()).isNotNull();
        assertThat(response.updatedAt()).isNotNull();
        assertThat(response.author()).isEqualTo(author);

        verify(reviewRepository, times(1)).findById(reviewId);
        verify(userRepository, times(1)).findById(userId);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void shouldThrowExceptionWhenReviewNotFoundForAddComment() {
        // Given
        final var request = getCreateCommentRequest(reviewId);
        final Optional<Review> reviewNotFound = Optional.empty();

        when(reviewRepository.findById(reviewId)).thenReturn(reviewNotFound);

        // When & Then
        final var exception = assertThrows(ResourceNotFoundException.class,
                () -> commentService.addComment(request, userId));

        assertThat(exception.getMessage()).contains("Review", reviewId.toString());

        verify(reviewRepository, times(1)).findById(reviewId);
        verify(userRepository, never()).findById(any());
        verify(commentRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundForAddComment() {
        // Given
        final var request = getCreateCommentRequest(reviewId);
        review = getReview(reviewId, user);
        final Optional<User> userNotFound = Optional.empty();

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(userRepository.findById(userId)).thenReturn(userNotFound);

        // When & Then
        final var exception = assertThrows(ResourceNotFoundException.class,
                () -> commentService.addComment(request, userId));

        assertThat(exception.getMessage()).contains("User", userId.toString());

        verify(reviewRepository, times(1)).findById(reviewId);
        verify(userRepository, times(1)).findById(userId);
        verify(commentRepository, never()).save(any());
    }

    @Test
    void shouldUpdateCommentWhenValidDataProvided() {
        // Given
        final var request = getUpdateCommentRequest(commentId);
        user = getUser(userId);
        review = getReview(reviewId, user);
        comment = getComment(commentId, review, user);
        var author = getAuthor(user);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // When
        final var response = commentService.updateComment(request, userId);

        // Then
        assertThat(response.id()).isNotNull();
        assertThat(response.text()).isEqualTo(request.text());
        assertThat(response.createdAt()).isNotNull();
        assertThat(response.updatedAt()).isNotNull();
        assertThat(response.author()).isEqualTo(author);

        verify(commentRepository, times(1)).findById(commentId);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void shouldThrowExceptionWhenCommentNotFoundForUpdate() {
        // Given
        final var request = getUpdateCommentRequest(reviewId);
        review = getReview(reviewId, user);
        when(commentRepository.findById(reviewId)).thenReturn(Optional.empty());

        // When & Then
        final var exception = assertThrows(ResourceNotFoundException.class, () ->
                commentService.updateComment(request, userId));

        final String commentId = request.commentId();
        assertThat(exception.getMessage()).contains("Comment", commentId);

        verify(commentRepository, times(1)).findById(UUID.fromString(commentId));
        verify(userRepository, never()).findById(userId);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void shouldThrowUnauthorizedExceptionWhenUserNotOwnerOnUpdate() {
        // Given
        final var request = getUpdateCommentRequest(commentId);
        var storedUser = getUser(UUID.randomUUID());
        user = getUser(userId);
        review = getReview(reviewId, user);

        comment = getComment(commentId, review, storedUser);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // When & Then
        final var exception = assertThrows(UnauthorizedAccessException.class,
                () -> commentService.updateComment(request, userId));

        assertThat(exception.getMessage()).contains("Comment", userId.toString());

        verify(commentRepository, times(1)).findById(commentId);
        verify(userRepository, never()).findById(userId);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void shouldReturnCommentsPerUserWhenValidUserIdProvided() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        user = getUser(userId);
        review = getReview(reviewId, user);
        comment = getComment(commentId, review, user);
        final List<Comment> comments = List.of(comment);
        final Page<Comment> commentsPage = new PageImpl<>(comments, pageable, comments.size());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(commentRepository.findByAuthorOrderByCreatedAtDesc(user, pageable)).thenReturn(commentsPage);

        // When
        final Page<CommentResponse> responsePage = commentService.getCommentsPerUser(userId, pageable);

        // Then
        assertThat(responsePage.hasContent()).isTrue();
        assertThat(responsePage.getContent()).hasSize(comments.size());
        assertThat(responsePage.getContent().getFirst().id()).isEqualTo(comments.getFirst().getId().toString());

        verify(userRepository, times(1)).findById(userId);
        verify(commentRepository, times(1)).findByAuthorOrderByCreatedAtDesc(user, pageable);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundForGetCommentsPerUser() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        user = getUser(userId);
        review = getReview(reviewId, user);
        comment = getComment(commentId, review, user);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        final var exception = assertThrows(ResourceNotFoundException.class,
                () -> commentService.getCommentsPerUser(userId, pageable));

        assertThat(exception.getMessage()).contains("User", userId.toString());

        verify(userRepository, times(1)).findById(userId);
        verify(commentRepository, never()).findByAuthorOrderByCreatedAtDesc(user, pageable);
    }

    @Test
    void shouldReturnCommentsPerReviewWhenValidReviewIdProvided() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        user = getUser(userId);
        review = getReview(reviewId, user);
        comment = getComment(commentId, review, user);
        final List<Comment> comments = List.of(comment);
        final Page<Comment> commentsPage = new PageImpl<>(comments, pageable, comments.size());

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(commentRepository.findByReviewOrderByCreatedAtAsc(review, pageable)).thenReturn(commentsPage);

        // When
        final Page<CommentResponse> responsePage = commentService.getCommentsPerReview(reviewId, pageable);

        // Then
        assertThat(responsePage.hasContent()).isTrue();
        assertThat(responsePage.getContent()).hasSize(comments.size());
        assertThat(responsePage.getContent().getFirst().id()).isEqualTo(comments.getFirst().getId().toString());

        verify(reviewRepository, times(1)).findById(reviewId);
        verify(commentRepository, times(1)).findByReviewOrderByCreatedAtAsc(review, pageable);
    }

    @Test
    void shouldThrowExceptionWhenReviewNotFoundForGetCommentsPerReview() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        user = getUser(userId);
        review = getReview(reviewId, user);
        comment = getComment(commentId, review, user);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        // When & Then
        final var exception = assertThrows(ResourceNotFoundException.class,
                () -> commentService.getCommentsPerReview(reviewId, pageable));

        assertThat(exception.getMessage()).contains("Review", reviewId.toString());

        verify(reviewRepository, times(1)).findById(reviewId);
        verify(commentRepository, never()).findByReviewOrderByCreatedAtAsc(review, pageable);
    }

    @Test
    void shouldDeleteCommentWhenValidIdAndOwnerProvided() {
        // Given
        user = getUser(userId);
        review = getReview(reviewId, user);
        comment = getComment(commentId, review, user);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // When
        commentService.deleteComment(commentId, userId);

        // Then
        verify(commentRepository, times(1)).findById(commentId);
        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    void shouldThrowExceptionWhenCommentNotFoundForDelete() {
        // Given
        user = getUser(userId);
        review = getReview(reviewId, user);
        comment = getComment(commentId, review, user);

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // When & Then
        final var exception = assertThrows(ResourceNotFoundException.class,
                () -> commentService.deleteComment(commentId, userId));

        assertThat(exception.getMessage()).contains("Comment", commentId.toString());

        verify(commentRepository, times(1)).findById(commentId);
        verify(commentRepository, never()).delete(comment);
    }

    @Test
    void shouldThrowUnauthorizedExceptionWhenUserNotOwnerOnDelete() {
        // Given
        final User author = getUser(UUID.randomUUID());
        user = getUser(userId);
        review = getReview(reviewId, author);
        comment = getComment(commentId, review, author);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // When & Then
        final var exception = assertThrows(UnauthorizedAccessException.class,
                () -> commentService.deleteComment(commentId, userId));

        assertThat(exception.getMessage()).contains("Comment", userId.toString());

        verify(commentRepository, times(1)).findById(commentId);
        verify(commentRepository, never()).delete(comment);
    }

    private CreateCommentRequest getCreateCommentRequest(UUID reviewId) {
        return new CreateCommentRequest(reviewId.toString(), "text");
    }

    private UpdateCommentRequest getUpdateCommentRequest(UUID commentId) {
        return new UpdateCommentRequest(commentId.toString(), "text");
    }

    private Comment getComment(UUID commentId, Review review, User author) {
        return new Comment(
                commentId,
                review,
                author,
                "content",
                Instant.now(),
                Instant.now());
    }

    private Review getReview(UUID reviewId, User user) {
        return new Review(
                reviewId,
                user,
                null,
                0,
                "notes",
                "http://image-url",
                Instant.now(),
                Instant.now());
    }

    private User getUser(UUID userId) {
        return new User(
                userId,
                "Display Name",
                "email",
                Instant.now(),
                Instant.now(),
                "http://avatar-url",
                "googleId");
    }

    private UserSummaryResponse getAuthor(User user) {
        return new UserSummaryResponse(
                user.getId().toString(),
                user.getDisplayName(),
                user.getAvatarUrl());
    }

}
