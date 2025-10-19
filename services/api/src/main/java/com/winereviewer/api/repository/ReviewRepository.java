package com.winereviewer.api.repository;

import com.winereviewer.api.domain.Review;
import com.winereviewer.api.domain.User;
import com.winereviewer.api.domain.Wine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * @author lucas
 * @date 18/10/2025 15:15
 */
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByWineOrderByCreatedAtDesc(Wine wine);

    List<Review> findByUserOrderByCreatedAtDesc(User user);

    List<Review> findAllByOrderByCreatedAtDesc();


}
