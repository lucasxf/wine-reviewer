package com.winereviewer.api.repository;

import com.winereviewer.api.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * @author lucas
 * @date 18/10/2025 15:14
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by email
     *
     * @param email
     * @return
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by google id
     *
     * @param googleId
     * @return
     */
    Optional<User> findByGoogleId(String googleId);

    /**
     * Check if user exists by email
     *
     * @param email
     * @return
     */
    boolean existsByEmail(String email);

}
