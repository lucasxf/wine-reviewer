package com.winereviewer.api.repository;

import com.winereviewer.api.domain.Comment;
import com.winereviewer.api.domain.Review;
import com.winereviewer.api.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * @author lucas
 * @date 18/10/2025 15:03
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    Page<Comment> findByReviewOrderByCreatedAtAsc(Review review, Pageable pageable);

    Page<Comment> findByAuthorOrderByCreatedAtDesc(User author, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Comment c JOIN c.review r WHERE r.id = :reviewId")
    long countCommentByReview(String reviewId);

}
