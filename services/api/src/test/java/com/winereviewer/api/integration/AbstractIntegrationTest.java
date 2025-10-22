package com.winereviewer.api.integration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Base class for integration tests using Testcontainers with PostgreSQL.
 * <p>
 * This class sets up a real PostgreSQL container for integration testing,
 * providing a more realistic test environment compared to H2.
 * <p>
 * <strong>Design decisions:</strong>
 * - Uses @SpringBootTest for full application context (realistic integration tests)
 * - Testcontainers provides isolated PostgreSQL instance per test run
 * - @Transactional ensures test isolation (rollback after each test)
 * - Shared container across all tests for performance (static @Container)
 * - DynamicPropertySource overrides datasource properties to use container
 * <p>
 * <strong>Why Testcontainers:</strong>
 * - **Fidelidade:** Tests run against real PostgreSQL (same as production)
 * - **Isolamento:** Each test run gets fresh database state
 * - **Constraints:** Database constraints (CHECK, FK) work exactly like production
 * - **Performance:** Shared container amortizes startup cost
 * <p>
 * <strong>Usage:</strong>
 * Extend this class in your integration test classes (e.g., ReviewControllerIT).
 * All subclasses will share the same PostgreSQL container instance.
 *
 * @author lucas
 * @date 22/10/2025
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@Testcontainers
@Transactional
public abstract class AbstractIntegrationTest {

    @Container
    protected static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("winereviewer_test")
                    .withUsername("test")
                    .withPassword("test")
                    .withReuse(true); // Reuse container across test runs for faster execution

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * Dynamically configures Spring Boot datasource to use Testcontainers PostgreSQL.
     * This overrides application.yml properties with container connection details.
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        // Subclasses can override this method to add custom setup
        // Example: insert test data, configure mocks, etc.
    }

}
