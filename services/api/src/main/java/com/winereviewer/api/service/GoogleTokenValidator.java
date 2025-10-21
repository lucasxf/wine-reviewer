package com.winereviewer.api.service;

/**
 * Service for validating Google ID tokens.
 * <p>
 * Responsible for:
 * - Validating Google ID tokens sent by mobile app
 * - Extracting user information (email, name, picture) from token
 * - Verifying token signature and expiration
 *
 * @author lucas
 * @date 21/10/2025
 */
public interface GoogleTokenValidator {

    /**
     * Validates a Google ID token and extracts user information.
     * <p>
     * <strong>What this method does:</strong>
     * <ol>
     *   <li>Verifies token signature with Google's public keys</li>
     *   <li>Checks if token is expired</li>
     *   <li>Validates audience (client ID)</li>
     *   <li>Extracts user payload (email, name, picture, googleId)</li>
     * </ol>
     *
     * @param idToken Google ID token sent by client
     * @return GoogleUserInfo with validated user data
     * @throws com.winereviewer.api.exception.InvalidTokenException if token is invalid, expired, or has wrong audience
     */
    GoogleUserInfo validateToken(String idToken);

    /**
     * DTO containing validated Google user information.
     * <p>
     * Extracted from Google ID token payload.
     *
     * @param googleId unique Google user identifier (sub claim)
     * @param email    user's email address
     * @param name     user's display name
     * @param picture  user's profile picture URL (optional)
     */
    record GoogleUserInfo(
            String googleId,
            String email,
            String name,
            String picture
    ) {
    }

}
