package com.winereviewer.api.service;

import com.winereviewer.api.service.GoogleTokenValidator.GoogleUserInfo;

/**
 * Service for authentication operations.
 * <p>
 * Responsible for:
 * - Authenticating users with Google OAuth
 * - Creating or updating user records
 * - Generating JWT tokens
 *
 * @author lucas
 * @date 21/10/2025
 */
public interface AuthService {

    /**
     * Authenticates a user with Google OAuth and generates JWT.
     * <p>
     * <strong>Flow:</strong>
     * <ol>
     *   <li>Validates Google ID token</li>
     *   <li>Finds existing user by googleId or creates new one</li>
     *   <li>Updates user information if needed (name, email, avatar)</li>
     *   <li>Generates JWT token for the user</li>
     *   <li>Returns authentication response with JWT and user info</li>
     * </ol>
     *
     * @param googleIdToken Google ID token sent by mobile app
     * @return AuthResponse with JWT token and user information
     * @throws com.winereviewer.api.exception.InvalidTokenException if Google token is invalid
     */
    AuthResponse authenticateWithGoogle(String googleIdToken);

    /**
     * DTO for authentication response.
     * <p>
     * Contains JWT token and user information.
     *
     * @param token       JWT token to be used in subsequent requests
     * @param userId      UUID of the authenticated user
     * @param email       user's email
     * @param displayName user's display name
     * @param avatarUrl   user's avatar URL (optional)
     */
    record AuthResponse(
            String token,
            String userId,
            String email,
            String displayName,
            String avatarUrl
    ) {
    }

}
