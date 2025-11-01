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

import static org.junit.jupiter.api.Assertions.*;
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
    void givenAddCommentRequest_whenAddComment_thenReturnComment() {
        // given
        final var request = getCreateCommentRequest(reviewId);
        user = getUser(userId);
        review = getReview(reviewId, user);
        var author = getAuthor(user);
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        final var response = commentService.addComment(request, userId);

        // then
        assertNotNull(response.id());
        assertNotNull(response.text(), request.text());
        assertNotNull(response.createdAt());
        assertNotNull(response.updatedAt());
        assertEquals(response.author(), author);

        verify(reviewRepository, times(1)).findById(reviewId);
        verify(userRepository, times(1)).findById(userId);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void givenAddCommentRequest_whenReviewNotFound_thenThrowException() {
        // given
        final var request = getCreateCommentRequest(reviewId);
        final Optional<Review> reviewNotFound = Optional.empty();

        when(reviewRepository.findById(reviewId)).thenReturn(reviewNotFound);

        // when & then
        final var exception = assertThrows(ResourceNotFoundException.class,
                () -> commentService.addComment(request, userId));

        // then
        assertTrue(exception.getMessage().contains("Review"));
        assertTrue(exception.getMessage().contains(reviewId.toString()));

        verify(reviewRepository, times(1)).findById(reviewId);
        verify(userRepository, never()).findById(any());
        verify(commentRepository, never()).save(any());
    }

    @Test
    void givenAddCommentRequest_whenUserNotFound_thenThrowException() {
        // given
        final var request = getCreateCommentRequest(reviewId);
        review = getReview(reviewId, user);
        final Optional<User> userNotFound = Optional.empty();

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(userRepository.findById(userId)).thenReturn(userNotFound);

        // when & then
        final var exception = assertThrows(ResourceNotFoundException.class,
                () -> commentService.addComment(request, userId));

        assertTrue(exception.getMessage().contains("User"));
        assertTrue(exception.getMessage().contains(userId.toString()));

        verify(reviewRepository, times(1)).findById(reviewId);
        verify(userRepository, times(1)).findById(userId);
        verify(commentRepository, never()).save(any());
    }

    @Test
    void givenUpdateCommentRequest_whenUpdateComment_thenReturnComment() {
        // given
        final var request = getUpdateCommentRequest(commentId);
        user = getUser(userId);
        review = getReview(reviewId, user);
        comment = getComment(commentId, review, user);
        var author = getAuthor(user);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // when
        final var response = commentService.updateComment(request, userId);

        // then
        assertNotNull(response.id());
        assertNotNull(response.text(), request.text());
        assertNotNull(response.createdAt());
        assertNotNull(response.updatedAt());
        assertEquals(response.author(), author);

        verify(commentRepository, times(1)).findById(commentId);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void givenUpdateCommentRequest_whenCommentNotFound_thenThrowException() {
        // given
        final var request = getUpdateCommentRequest(reviewId);
        review = getReview(reviewId, user);
        when(commentRepository.findById(reviewId)).thenReturn(Optional.empty());

        // when
        final var exception = assertThrows(ResourceNotFoundException.class, () ->
                commentService.updateComment(request, userId));

        // then
        assertTrue(exception.getMessage().contains("Comment"));
        final String commentId = request.commentId();
        assertTrue(exception.getMessage().contains(commentId));

        verify(commentRepository, times(1)).findById(UUID.fromString(commentId));
        verify(userRepository, never()).findById(userId);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void givenUpdateCommentRequest_whenUnauthorizedUser_thenThrowException() {
        // given
        final var request = getUpdateCommentRequest(commentId);
        var storedUser = getUser(UUID.randomUUID());
        user = getUser(userId);
        review = getReview(reviewId, user);

        comment = getComment(commentId, review, storedUser);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // when
        final var exception = assertThrows(UnauthorizedAccessException.class,
                () -> commentService.updateComment(request, userId));

        // then
        assertTrue(exception.getMessage().contains("Comment"));
        assertTrue(exception.getMessage().contains(userId.toString()));

        verify(commentRepository, times(1)).findById(commentId);
        verify(userRepository, never()).findById(userId);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void givenGetCommentsPerUser_whenGetComments_thenReturnComments() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        user = getUser(userId);
        review = getReview(reviewId, user);
        comment = getComment(commentId, review, user);
        final List<Comment> comments = List.of(comment);
        final Page<Comment> commentsPage = new PageImpl<>(comments, pageable, comments.size());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(commentRepository.findByAuthorOrderByCreatedAtDesc(user, pageable)).thenReturn(commentsPage);

        // when
        final Page<CommentResponse> responsePage = commentService.getCommentsPerUser(userId, pageable);

        // then
        assertTrue(responsePage.hasContent());
        assertEquals(responsePage.getContent().size(), comments.size());
        assertEquals(responsePage.getContent().getFirst().id(), comments.getFirst().getId().toString());

        verify(userRepository, times(1)).findById(userId);
        verify(commentRepository, times(1)).findByAuthorOrderByCreatedAtDesc(user, pageable);
    }

    @Test
    void givenGetCommentsPerUser_whenUserNotFound_thenThrowException() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        user = getUser(userId);
        review = getReview(reviewId, user);
        comment = getComment(commentId, review, user);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        final var exception = assertThrows(ResourceNotFoundException.class,
                () -> commentService.getCommentsPerUser(userId, pageable));

        // then
        assertTrue(exception.getMessage().contains("User"));
        assertTrue(exception.getMessage().contains(userId.toString()));

        verify(userRepository, times(1)).findById(userId);
        verify(commentRepository, never()).findByAuthorOrderByCreatedAtDesc(user, pageable);
    }

    @Test
    void givenGetCommentsPerReview_whenGetComments_thenReturnComments() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        user = getUser(userId);
        review = getReview(reviewId, user);
        comment = getComment(commentId, review, user);
        final List<Comment> comments = List.of(comment);
        final Page<Comment> commentsPage = new PageImpl<>(comments, pageable, comments.size());

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(commentRepository.findByReviewOrderByCreatedAtAsc(review, pageable)).thenReturn(commentsPage);

        // when
        final Page<CommentResponse> responsePage = commentService.getCommentsPerReview(reviewId, pageable);

        // then
        assertTrue(responsePage.hasContent());
        assertEquals(responsePage.getContent().size(), comments.size());
        assertEquals(responsePage.getContent().getFirst().id(), comments.getFirst().getId().toString());

        verify(reviewRepository, times(1)).findById(reviewId);
        verify(commentRepository, times(1)).findByReviewOrderByCreatedAtAsc(review, pageable);
    }

    @Test
    void givenGetCommentsPerReview_whenReviewNotFound_thenThrowException() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        user = getUser(userId);
        review = getReview(reviewId, user);
        comment = getComment(commentId, review, user);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        // when & then
        final var exception = assertThrows(ResourceNotFoundException.class,
                () -> commentService.getCommentsPerReview(reviewId, pageable));

        // then
        assertTrue(exception.getMessage().contains("Review"));
        assertTrue(exception.getMessage().contains(reviewId.toString()));

        verify(reviewRepository, times(1)).findById(reviewId);
        verify(commentRepository, never()).findByReviewOrderByCreatedAtAsc(review, pageable);
    }

    @Test
    void givenDeleteComment_whenDeleteComment_thenDeleteComment() {
        // given
        user = getUser(userId);
        review = getReview(reviewId, user);
        comment = getComment(commentId, review, user);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // when
        commentService.deleteComment(commentId, userId);

        // then
        verify(commentRepository, times(1)).findById(commentId);
        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    void givenDeleteComment_whenCommentNotFound_thenThrowException() {
        // given
        user = getUser(userId);
        review = getReview(reviewId, user);
        comment = getComment(commentId, review, user);

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // when & then
        final var exception = assertThrows(ResourceNotFoundException.class,
                () -> commentService.deleteComment(commentId, userId));

        // then
        assertTrue(exception.getMessage().contains("Comment"));
        assertTrue(exception.getMessage().contains(commentId.toString()));

        verify(commentRepository, times(1)).findById(commentId);
        verify(commentRepository, never()).delete(comment);
    }

    @Test
    void givenDeleteComment_whenUnauthorizedUser_thenThrowException() {
        // given
        final User author = getUser(UUID.randomUUID());
        user = getUser(userId);
        review = getReview(reviewId, author);
        comment = getComment(commentId, review, author);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // when & then
        final var exception = assertThrows(UnauthorizedAccessException.class,
                () -> commentService.deleteComment(commentId, userId));

        // then
        assertTrue(exception.getMessage().contains("Comment"));
        assertTrue(exception.getMessage().contains(userId.toString()));

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
