# Coding Style Guide - Infrastructure (Docker, Testing, CI/CD)

> Infrastructure-specific coding standards for DevOps, containerization, and testing.
> **Part of:** Wine Reviewer Project
> **Applies to:** `infra/` (Docker, Testcontainers, GitHub Actions)

**For universal cross-stack guidelines, see:** `../CODING_STYLE_GENERAL.md`

---

# 🐳 INFRASTRUCTURE STANDARDS (Docker, Testing, CI/CD)

## 🧪 Integration Testing with Testcontainers

### Test Structure Standards

**Naming Conventions:**
- **Unit Tests:** `*Test.java` (e.g., `ReviewServiceTest`)
- **Integration Tests:** `*IT.java` (e.g., `ReviewControllerIT`, `AuthControllerIT`)
- **Location:** Mirror production package structure under `src/test/java/`

**Base Class Pattern:**
```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@Testcontainers
@Transactional
public abstract class AbstractIntegrationTest {

    @Container
    protected static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("appname_test")
                    .withUsername("test")
                    .withPassword("test")
                    .withReuse(true);  // Performance optimization

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
```

### Integration Test Best Practices

**✅ DO:**
- Use `@Transactional` for automatic rollback (test isolation)
- Use static `@Container` for shared PostgreSQL container (performance)
- Mock external APIs (Google OAuth, payment gateways, etc.)
- Test database constraints (FK, CHECK, CASCADE)
- Test pagination, sorting, filtering
- Test all HTTP status codes (200, 201, 400, 403, 404, 422, 500)

**❌ DON'T:**
- Don't create new container per test class (slow)
- Don't use H2 for integration tests (not production-like)
- Don't test implementation details (focus on API contract)
- Don't skip testing error scenarios

### Test Data Setup Pattern

```java
@BeforeEach
void setupTestData() {
    // Create test entities directly via repositories
    testUser = userRepository.save(createUser());
    testWine = wineRepository.save(createWine());
}

private User createUser() {
    var user = new User();
    user.setEmail("test@example.com");
    user.setDisplayName("Test User");
    return user;
}
```

### Integration Test Documentation

**Always include class-level Javadoc:**
```java
/**
 * Integration tests for ReviewController with Testcontainers PostgreSQL.
 * <p>
 * Tests all CRUD endpoints against a real PostgreSQL database to verify:
 * - REST API behavior
 * - Database constraints (rating 1-5, cascade delete, foreign keys)
 * - Exception handling (404, 403, 400, 422)
 * - Pagination and sorting
 * <p>
 * <strong>Test strategy:</strong>
 * - Uses real PostgreSQL via Testcontainers (production-like environment)
 * - Each test is @Transactional (auto-rollback for isolation)
 * - Test data inserted directly via repositories for speed
 * - MockMvc for HTTP request/response testing
 *
 * @author lucas
 * @date 22/10/2025
 */
class ReviewControllerIT extends AbstractIntegrationTest {
    // tests...
}
```

## 🐳 Docker Standards

### Dockerfile Best Practices

**Multi-stage builds:**
```dockerfile
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Docker Compose for local development:**
- Use `docker-compose.yml` in `infra/` directory
- Define health checks for dependent services
- Use named volumes for data persistence
- Use environment variables for configuration
- Document all services in README

## 🔧 Bash Scripting Standards

### Error Handling and Robustness

**Find Command Error Handling:** *(Added 2025-11-18)*

Always add error handling when using `find` with `wc -l` to count files. Use `2>/dev/null || echo '0'` to suppress errors and provide fallback.

**Example:**
```bash
# ✅ CORRECT - With error handling
find services/api/src/main/java -name '*.java' -exec wc -l {} + 2>/dev/null | tail -1 || echo '0'

# ❌ INCORRECT - Without error handling (fails if directory doesn't exist)
find services/api/src/main/java -name '*.java' -exec wc -l {} + | tail -1
```

**Why:**
- Prevents script failures when directories don't exist
- Handles permission denied errors gracefully
- Provides sensible fallback (0) instead of empty output
- Essential for automation scripts that may run in different environments

## 🚀 CI/CD Standards

### GitHub Actions Workflows

**Path-based triggers (monorepo):**
```yaml
on:
  push:
    branches: [main, develop]
    paths:
      - 'services/api/**'
      - '.github/workflows/ci-api.yml'
```

**Caching strategy:**
```yaml
- name: Cache Maven packages
  uses: actions/cache@v3
  with:
    path: ~/.m2
    key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
```

**Test execution in CI:**
- Run unit tests first (fast feedback)
- Run integration tests with Testcontainers
- Generate coverage reports
- Fail build if tests fail

---

**For general cross-stack conventions, see:** `../CODING_STYLE_GENERAL.md`

