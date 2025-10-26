package com.winereviewer.api.repository;

import com.winereviewer.api.domain.Comment;
import com.winereviewer.api.domain.Review;
import com.winereviewer.api.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * @author lucas
 * @date 18/10/2025 15:03
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findByReviewOrderByCreatedAtAsc(Review review);

    List<Comment> findByAuthorOrderByCreatedAtDesc(User author);

}
