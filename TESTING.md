# Testing Strategy (TDD + BDD)

Comprehensive testing guidelines for the Wine Reviewer project following Test-Driven Development (TDD) and Behavior-Driven Development (BDD) principles.

---

## CRITICAL RULE: Test-Driven Development (TDD) Workflow

ALL new features MUST follow the TDD Red-Green-Refactor cycle:

### The TDD Cycle

#### 1. RED - Write Failing Test First

- Define expected behavior BEFORE writing production code
- Test should fail because feature doesn't exist yet
- Clarifies requirements and API design upfront

**Why this matters:**
- Prevents over-engineering (you only write code to pass the test)
- Ensures testability from the start
- Acts as executable specification

**Example:**
```java
@Test
void shouldCreateReviewWhenValidDataProvided() {
    // This test will fail because ReviewService.createReview() doesn't exist yet
    final var request = new CreateReviewRequest(wineId, 5, "Great wine!", null);
    final var result = reviewService.createReview(userId, request);
    assertThat(result).isNotNull();
    assertThat(result.getRating()).isEqualTo(5);
}
```

#### 2. GREEN - Write Minimal Code to Pass

- Implement simplest solution that makes test pass
- Don't worry about perfection yet
- Goal: Get to green status quickly

**Why this matters:**
- Fast feedback loop
- Focuses on making tests pass, not on perfect code
- Prevents premature optimization

**Example:**
```java
public Review createReview(UUID userId, CreateReviewRequest request) {
    // Minimal implementation to make test pass
    final var review = new Review();
    review.setUserId(userId);
    review.setWineId(UUID.fromString(request.wineId()));
    review.setRating(request.rating());
    review.setNotes(request.notes());
    return reviewRepository.save(review);
}
```

#### 3. REFACTOR - Clean Up Code and Tests

- **CRITICAL:** Don't skip this step (most common TDD failure per Martin Fowler)
- Remove duplication, improve naming, optimize structure
- Tests must still pass after refactoring
- If tests break, analyze for code smells BEFORE adjusting tests

**Why this matters:**
- Keeps codebase maintainable
- Prevents technical debt accumulation
- Ensures tests remain stable and meaningful

**Example:**
```java
// BEFORE REFACTOR (duplication)
public Review createReview(UUID userId, CreateReviewRequest request) {
    final var review = new Review();
    review.setUserId(userId);
    review.setWineId(UUID.fromString(request.wineId()));
    review.setRating(request.rating());
    review.setNotes(request.notes());
    review.setPhotoUrl(request.photoUrl());
    review.setCreatedAt(LocalDateTime.now());
    return reviewRepository.save(review);
}

// AFTER REFACTOR (extracted builder method)
public Review createReview(UUID userId, CreateReviewRequest request) {
    final var review = buildReviewFromRequest(userId, request);
    return reviewRepository.save(review);
}

private Review buildReviewFromRequest(UUID userId, CreateReviewRequest request) {
    return Review.builder()
        .userId(userId)
        .wineId(UUID.fromString(request.wineId()))
        .rating(request.rating())
        .notes(request.notes())
        .photoUrl(request.photoUrl())
        .createdAt(LocalDateTime.now())
        .build();
}
```

---

## When Tests Keep Breaking - Critical Analysis

**CRITICAL RULE:** If tests consistently break during refactoring or minor changes, **STOP** and analyze implementation for code smells BEFORE adjusting tests.

### Common Code Smells Indicating Brittle Tests

1. **Tight Coupling:** Implementation details leaking into tests
   - Symptom: Tests break when you change internal implementation
   - Solution: Test behavior, not implementation

2. **God Objects:** Classes doing too much, tests cover multiple concerns
   - Symptom: One test breaks when you change unrelated functionality
   - Solution: Split class into smaller, focused classes (Single Responsibility Principle)

3. **Fragile Base Class:** Inheritance causing cascading failures
   - Symptom: Changing parent class breaks all child class tests
   - Solution: Favor composition over inheritance

4. **Primitive Obsession:** Using primitives instead of domain objects
   - Symptom: Many tests break when you change data type (e.g., String → UUID)
   - Solution: Use value objects and domain types

5. **Shotgun Surgery:** One feature change breaks tests in multiple places
   - Symptom: Changing rating range (1-5 → 1-10) breaks 20 tests
   - Solution: Extract constants, use configuration

### Resolution Process

1. **Identify the underlying design smell in production code**
   - Ask: "Why did this test break?"
   - Example: "Test broke because I changed internal Map to List"

2. **Refactor production code to fix the design problem**
   - Example: Extract interface, hide implementation details

3. **Update tests to reflect improved design**
   - Example: Test against interface, not concrete implementation

4. **Validate that tests are now more stable and maintainable**
   - Run tests multiple times, make small changes, verify stability

**Why:** Broken tests are symptoms, not root causes. The implementation is the problem.

---

## BDD - Given/When/Then Structure

**CRITICAL RULE:** ALL tests MUST follow Given/When/Then structure for clarity and consistency.

### The Pattern

#### Given (Arrange)
Set up preconditions and initial state
- Create test data, mock dependencies, configure system
- Establish context for the behavior being tested

**Example:**
```java
// Given: A user and a wine exist in the database
final var testUser = createTestUser();
final var testWine = createTestWine();
final var request = new CreateReviewRequest(
    testWine.getId().toString(), 5, "Excellent!", null);
```

#### When (Act)
Execute the specific behavior under test
- Call the method, trigger the event, send the request
- Single action being tested (not multiple actions)

**Example:**
```java
// When: User creates a review
final var result = reviewService.createReview(testUser.getId(), request);
```

#### Then (Assert)
Verify expected outcome
- Assert results, check state changes, verify interactions
- Multiple assertions OK if testing single behavior

**Example:**
```java
// Then: Review is created with correct data
assertThat(result).isNotNull();
assertThat(result.getRating()).isEqualTo(5);
assertThat(result.getNotes()).isEqualTo("Excellent!");
assertThat(result.getUserId()).isEqualTo(testUser.getId());
assertThat(result.getWineId()).isEqualTo(testWine.getId());
```

### Test Naming Convention

**Format:** `should[ExpectedBehavior]When[StateUnderTest]`

**Good Examples:**
- `shouldCreateReviewWhenValidDataProvided`
- `shouldThrowExceptionWhenRatingOutOfRange`
- `shouldReturn403WhenUserIsNotOwner`
- `shouldUpdateReviewWhenUserIsOwner`
- `shouldDeleteAllCommentsWhenReviewIsDeleted`

**Bad Examples:**
- ❌ `testCreateReview` - Not descriptive
- ❌ `test1` - No context
- ❌ `createReview_validData` - Not behavior-focused
- ❌ `whenValidDataProvidedShouldCreateReview` - Wrong order

### Benefits of BDD Structure

- ✅ **Clear, self-documenting test names**
- ✅ **Behavior-focused (not implementation-focused)**
- ✅ **Business language (readable by non-developers)**
- ✅ **Easy to identify what's being tested**
- ✅ **Facilitates communication with stakeholders**

---

## Test-After-Implementation Rule

**CRITICAL RULE:** Immediately create tests after implementing testable classes (part of GREEN phase in TDD).

### Testable Classes (MUST have tests)

- ✅ **Services** (business logic)
- ✅ **Repositories** (data access)
- ✅ **Controllers** (REST endpoints)
- ✅ **Utilities** (helper functions)
- ✅ **Widgets** (Flutter UI components)
- ✅ **Providers** (Riverpod state management)

### Non-Testable Classes (SKIP tests)

- ❌ **Configuration classes** (@Configuration, @ConfigurationProperties)
- ❌ **Simple DTOs** (no logic, just data)
- ❌ **Entities** (unless complex domain logic)

### TDD Workflow

1. **Write failing test (RED)**
   - Define expected behavior
   - Run test, verify it fails

2. **Implement class/method (GREEN)**
   - Write minimal code to pass test
   - Run test, verify it passes

3. **Refactor and clean up (REFACTOR)**
   - Improve code quality
   - Remove duplication
   - Run tests, verify still passing

4. **Run tests, verify pass**
   - All tests green
   - No regressions

5. **Commit code + tests together**
   - Atomic commits
   - Code and tests always in sync

6. **Never defer test writing to "later"**
   - Tests are not optional
   - "Later" never comes

---

## Test Coverage Goals

### Target Coverage Levels

- **Critical paths:** 100% coverage (authentication, payments, data integrity)
- **Business logic:** 90%+ coverage (services, domain layer)
- **Controllers/UI:** 80%+ coverage (happy path + error cases)
- **Overall project:** 70%+ minimum coverage

### What to Measure

- **Line coverage:** Percentage of code lines executed
- **Branch coverage:** Percentage of decision branches tested
- **Path coverage:** Percentage of execution paths tested

### Coverage is Not a Goal

**Important:** High coverage ≠ good tests
- Focus on meaningful tests, not just coverage percentage
- 100% coverage with bad tests is worse than 70% with good tests
- Test behavior, not implementation

---

## Stack-Specific Testing

### Backend (Java/Spring Boot)

**Tools:**
- **Unit tests:** JUnit 5 + Mockito + AssertJ
- **Integration tests:** Testcontainers (real PostgreSQL)
- **Coverage:** JaCoCo

**File conventions:**
- **Unit tests:** `ClassNameTest.java` (e.g., `ReviewServiceTest.java`)
- **Integration tests:** `ClassNameIT.java` (e.g., `ReviewControllerIT.java`)
- **Location:** `src/test/java/` mirroring `src/main/java/` package structure

**Commands:**
```bash
# Run all tests
./mvnw verify -q

# Run unit tests only
./mvnw test -q

# Run integration tests only
./mvnw test -q -Dtest=*IT

# View coverage report
# Open: target/site/jacoco/index.html
```

**See:** `services/api/CODING_STYLE_BACKEND.md` for detailed conventions

---

### Frontend (Flutter/Dart)

**Tools:**
- **Unit tests:** flutter_test + mocktail
- **Widget tests:** flutter_test + WidgetTester
- **Golden tests:** golden_toolkit (visual regression)
- **Coverage:** flutter test --coverage

**File conventions:**
- **Unit tests:** `class_name_test.dart` (e.g., `auth_service_test.dart`)
- **Widget tests:** `widget_name_test.dart` (e.g., `login_screen_test.dart`)
- **Location:** `test/` mirroring `lib/` folder structure

**Commands:**
```bash
# Run all tests
flutter test

# Run tests with coverage
flutter test --coverage

# Run specific test file
flutter test test/features/auth/auth_test.dart

# View coverage report
genhtml coverage/lcov.info -o coverage/html
# Open: coverage/html/index.html
```

**See:** `apps/mobile/CODING_STYLE_FRONTEND.md` for detailed conventions

---

### Infrastructure (Docker/Testcontainers)

**Tools:**
- **Testcontainers:** Real PostgreSQL in Docker for integration tests
- **Docker health checks:** Service dependency validation
- **CI/CD pipeline validation:** GitHub Actions workflows

**Testcontainers setup:**
- **Base class:** `AbstractIntegrationTest`
- **Profile:** `application-integration.yml`
- **Container:** `postgres:16-alpine`
- **Isolation:** `@Transactional` for auto-rollback

**Commands:**
```bash
# Run integration tests with Testcontainers
cd services/api
./mvnw test -Dtest=*IT

# Clean Testcontainers resources
docker system prune --filter "label=org.testcontainers" -f
```

**See:** `infra/CODING_STYLE_INFRASTRUCTURE.md` for detailed conventions

---

## Testing Anti-Patterns

### What NOT to Do

1. ❌ **Testing implementation details**
   - Bad: Testing private methods
   - Good: Testing public API behavior

2. ❌ **Tight coupling to test data**
   - Bad: Hardcoded IDs, dates, strings
   - Good: Test data builders, factories

3. ❌ **Tests that depend on other tests**
   - Bad: Test 2 assumes Test 1 ran first
   - Good: Each test is independent

4. ❌ **Tests that depend on external state**
   - Bad: Tests assume database has specific data
   - Good: Tests set up own data (Given phase)

5. ❌ **Slow tests**
   - Bad: Tests take 10 minutes to run
   - Good: Tests run in <2 minutes

6. ❌ **Flaky tests**
   - Bad: Tests pass/fail randomly
   - Good: Tests are deterministic

7. ❌ **Testing the framework**
   - Bad: Testing Spring Boot, Flutter framework behavior
   - Good: Testing YOUR code

8. ❌ **Mocking everything**
   - Bad: Mocking every dependency (tests meaningless)
   - Good: Mock external dependencies only (DB, API, file system)

---

## Test Organization

### Test Folder Structure

**Backend (Java):**
```
src/test/java/
├── com.winereviewer.api/
│   ├── controller/
│   │   ├── ReviewControllerTest.java        # Unit tests
│   │   └── ReviewControllerIT.java          # Integration tests
│   ├── service/
│   │   ├── ReviewServiceTest.java           # Unit tests
│   │   └── ReviewServiceIT.java             # Integration tests
│   ├── repository/
│   │   └── ReviewRepositoryIT.java          # Integration tests only
│   └── integration/
│       └── AbstractIntegrationTest.java     # Base class for ITs
```

**Frontend (Flutter):**
```
test/
├── features/
│   ├── auth/
│   │   ├── auth_service_test.dart           # Unit tests
│   │   └── login_screen_test.dart           # Widget tests
│   ├── review/
│   │   ├── review_service_test.dart
│   │   └── review_list_test.dart
├── golden/
│   └── login_screen_golden_test.dart        # Golden tests
└── integration_test/
    └── app_test.dart                         # Integration tests
```

---

## Related Documentation

- **CODING_STYLE_GENERAL.md** - Universal coding conventions
- **services/api/CODING_STYLE_BACKEND.md** - Backend testing details (Testcontainers, AssertJ)
- **apps/mobile/CODING_STYLE_FRONTEND.md** - Frontend testing details (Widget tests, Golden tests)
- **infra/CODING_STYLE_INFRASTRUCTURE.md** - Infrastructure testing details (Docker, CI/CD)
- **CLAUDE.md** - Project-wide guidelines
- **services/api/COMMANDS.md** - Backend test commands
- **apps/mobile/COMMANDS.md** - Frontend test commands
- **infra/COMMANDS.md** - Infrastructure test commands
