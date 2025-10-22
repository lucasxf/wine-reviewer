package com.winereviewer.api.integration.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for integration tests.
 * <p>
 * <strong>Purpose:</strong> Disable authentication in integration tests to simplify testing.
 * <p>
 * <strong>Why disable security in tests:</strong>
 * - Integration tests focus on business logic and database constraints
 * - JWT authentication adds complexity (generating tokens, managing expiration)
 * - Security concerns are tested separately in AuthControllerIT
 * - Allows tests to focus on CRUD operations, validation, pagination, etc.
 * <p>
 * <strong>How it works:</strong>
 * - @Profile("integration") ensures this config only loads in integration tests
 * - @TestConfiguration marks this as a test-only configuration
 * - SecurityFilterChain permits all requests (no authentication required)
 * <p>
 * <strong>Trade-offs:</strong>
 * - ✅ Simpler tests (no JWT token generation needed)
 * - ✅ Faster execution (no authentication overhead)
 * - ✅ Focus on business logic, not security
 * - ⚠️ Security testing must be done separately (AuthControllerIT)
 * <p>
 * <strong>Alternative approach (if needed):</strong>
 * If you want to test with real JWT authentication, create a helper method
 * in AbstractIntegrationTest to generate valid JWT tokens:
 * <pre>
 * protected String generateValidJwt(UUID userId) {
 *     return jwtUtil.generateToken(userId);
 * }
 * </pre>
 * Then add the token to requests:
 * <pre>
 * mockMvc.perform(get("/reviews")
 *     .header("Authorization", "Bearer " + generateValidJwt(testUser.getId())))
 * </pre>
 *
 * @author lucas
 * @date 22/10/2025
 */
@TestConfiguration
@EnableWebSecurity
@Profile("integration")
public class TestSecurityConfig {

    /**
     * Disables all security for integration tests.
     * <p>
     * This allows MockMvc requests to bypass JWT authentication,
     * simplifying test code and focusing on business logic.
     *
     * @param http HttpSecurity builder
     * @return SecurityFilterChain that permits all requests
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll());
        return http.build();
    }

}
