# Wine Reviewer - Technical Learnings & Decisions Log

This file archives session logs, technical decisions, problems encountered, and key insights organized chronologically by session.

**Organization:** Sessions are listed newest-first, with subsections for Backend (☕), Frontend (📱), and Infrastructure (🐳) work done in each session.

---

## Session 2025-10-25 (Part 2): Documentation Optimization & Structure

**Session Goal:** Reduce CLAUDE.md bloat (40k chars) while preserving essential context for AI and developer

### 📚 General (Cross-stack)

**Context:** CLAUDE.md had grown to 869 lines (~40k chars) and would grow indefinitely with session logs and roadmap updates.

**What Was Done:**
- Analyzed CLAUDE.md bloat sources (session logs: 90 lines, roadmap: 100 lines, duplicate conventions: 50+ lines)
- Designed 3-file strategy: CLAUDE.md (architecture) + ROADMAP.md (status) + LEARNINGS.md (history)
- Created ROADMAP.md (154 lines) - current status, in-progress work, prioritized next steps
- Created LEARNINGS.md (213 lines) - session logs with hybrid chronological format (sessions with Backend/Frontend/Infrastructure subsections)
- Condensed CLAUDE.md by 44% (869 → 489 lines, 40k → 28k chars)
- Enhanced /directive command with smart deduplication (searches CLAUDE.md + CODING_STYLE.md)
- Updated /start-session to load ROADMAP.md
- Updated /finish-session to prompt for ROADMAP.md + LEARNINGS.md updates
- Updated /update-roadmap to target ROADMAP.md instead of CLAUDE.md

**Key Insights:**
- **Hybrid chronological format wins:** Sessions organized chronologically with Backend/Frontend/Infrastructure subsections reflects real mixed-stack work better than pure sectioned format
- **Separation of concerns:** Architecture (CLAUDE.md) vs Status (ROADMAP.md) vs History (LEARNINGS.md) prevents bloat and improves maintainability
- **Smart deduplication prevents redundancy:** Enhanced /directive searches both files for similar directives before adding
- **Scalability:** LEARNINGS.md can grow indefinitely without bloating CLAUDE.md (core architecture)
- **Token efficiency:** 44% reduction in main file = faster AI context loading

**Problems Encountered:**
- None - smooth implementation with clear user requirements and approved strategy

**Solutions Applied:**
- File structure: CLAUDE.md (architecture patterns) + ROADMAP.md (status tracking) + LEARNINGS.md (session logs)
- Command updates: All workflow commands now reference correct files
- Cross-references updated: Documentation strategy section now mentions all 3 files with clear update triggers

**Metrics:**
- CLAUDE.md: 869 → 489 lines (44% reduction)
- New files: ROADMAP.md (154 lines), LEARNINGS.md (213 lines)
- Total: 856 lines vs 869 original (but most sessions load only CLAUDE.md + ROADMAP.md ~35k chars)

---

## Session 2025-10-25: Flutter Mobile App Initialization

**Session Goal:** Initialize Flutter mobile app with core dependencies and project structure

### 📱 Frontend

**Context:** Starting from scratch - no Flutter project existed.

**What Was Done:**
- Initialized Flutter 3.35.6 project with package `com.winereviewer.wine_reviewer_mobile`
- Created feature-first architecture: `lib/features/` (auth, review, wine), `lib/core/`, `lib/common/`
- Configured 10 essential dependencies:
  - State management: `flutter_riverpod`
  - Navigation: `go_router`
  - HTTP client: `dio`
  - Models: `freezed` + `json_serializable`
  - Storage: `flutter_secure_storage`
  - Images: `image_picker` + `cached_network_image`
  - Auth: `google_sign_in`
  - Testing: `golden_toolkit`
  - Code gen: `build_runner`
- Created core configuration files:
  - `app_colors.dart` - Wine-themed color palette
  - `app_theme.dart` - Material Design 3 theme
  - `api_constants.dart` - API URLs, endpoints, timeouts
- Created documentation:
  - `DEPENDENCIES_EXPLAINED.md` - Detailed package explanations for backend developer
  - `SETUP_INSTRUCTIONS.md` - Development environment setup

**Key Insights:**
- **Feature-first vs layer-first:** Feature-first architecture (`features/auth/`, `features/review/`) scales better than layer-first (`screens/`, `widgets/`, `services/`) for medium-to-large apps
- **Riverpod over BLoC:** Riverpod chosen for state management due to better DI, compile-time safety, and simpler testing compared to BLoC (which has steeper learning curve)
- **Freezed for models:** Using `freezed` + `json_serializable` provides immutable data classes with equality, copyWith, and JSON serialization out of the box
- **Documentation for non-Flutter dev:** As a backend engineer learning Flutter, detailed explanations (What/Why/How/Alternatives) are critical for understanding decisions

**Problems Encountered:**
- None yet (initialization phase)

**Next Session:**
- Implement Dio HTTP client with auth interceptor
- Setup go_router navigation structure
- Create initial main.dart with ProviderScope

---

## Session 2025-10-22 (Part 3): Integration Test Authentication Completed

**Session Goal:** Verify integration tests are functional after previous session's incomplete commit

### ☕ Backend

**Context:** Previous session (Part 2) ended with commit stating "integration tests in non-functional state" - expected to manually fix 21 test methods.

**What Was Done:**
- Verified all integration tests already functional (previous session completed more work than documented)
- Confirmed all 36 integration tests passing (13 AuthController + 23 ReviewController)
- Total test suite: 82 tests (46 unit + 36 integration) - 100% pass rate

**Key Insights:**
- **Working mode matters:** Running Claude Code inside IntelliJ terminal (from previous session) may have caused confusion about actual file state
- **Always verify first:** Don't assume failures based on commit messages - verify actual test state before making changes
- **Separate terminal recommended:** Running Claude Code in separate terminal (not inside IntelliJ) avoids auto-formatter conflicts and provides clearer view of actual file state

**Problems Encountered:**
- Initial confusion about test state (expected failures that didn't exist)
- Root cause: Running Claude in IntelliJ terminal caused state confusion

**Solutions:**
- ✅ **Always run Claude Code in separate terminal** (not inside IDE terminal)
- ✅ Verify test state before assuming failures

---

## Session 2025-10-22 (Part 2): Integration Test Fixes & Code Quality Improvements

**Session Goal:** Fix failing integration tests and improve code quality

### ☕ Backend

**Context:** Integration tests had 4 types of failures after authentication implementation.

**Problems Fixed:**

1. **Cascade Delete Tests Failing**
   - **Problem:** Hibernate cache wasn't seeing database CASCADE DELETE
   - **Root cause:** JPA first-level cache holds deleted child entities even after database CASCADE DELETE
   - **Solution:** Added `entityManager.clear()` after `flush()` to force Hibernate to reload from database
   ```java
   // Before: commentRepository.findById(comment.getId()) returned cached entity
   // After: entityManager.clear() forces fresh load from DB
   commentRepository.delete(review);
   entityManager.flush();
   entityManager.clear(); // ← Critical for cascade delete tests
   ```

2. **AuthController Test Mismatch**
   - **Problem:** Expected 500 Internal Server Error but got 404 Not Found
   - **Root cause:** Test expectation was wrong - "user not found" should be 404, not 500
   - **Solution:** Renamed test and changed expectation from 500 → 404 (correct behavior)

3. **Optional Fields JSON Test**
   - **Problem:** Expected `.isEmpty()` but Jackson omits null fields from JSON
   - **Root cause:** Jackson default behavior is to omit null fields (cleaner JSON)
   - **Solution:** Changed assertion from `.isEmpty()` → `.doesNotExist()` (Jackson standard)

4. **Unit Test Authentication**
   - **Problem:** `ReviewControllerTest` missing proper authentication setup
   - **Solution:** Added `.with(user(userId.toString()))` + import `SecurityMockMvcRequestPostProcessors.user`

**New Directives Established:**
- **Always use imports over full class names** - Improves readability and follows Java conventions
- **Prefer `.getFirst()` over `.get(0)`** - Java 21+ idiomatic code
- **Show git diff before commit** - User must review all changes before committing
- **Auto-update directives** - New directives automatically added to CLAUDE.md

**Key Insights:**
- **Hibernate cache behavior:** JPA first-level cache can hide database CASCADE DELETE effects - always clear cache in cascade delete tests
- **Exception expectations:** Verify HTTP status codes match domain exceptions (404 for not found, 400 for validation, 422 for business rules)
- **Jackson serialization:** Default behavior is to omit null fields - use `.doesNotExist()` for optional field tests

**Token Efficiency Improvements:**
- Added "Command Execution Guidelines" section to CLAUDE.md
- Updated all commands to use quiet mode (`-q`, `--quiet-pull`)
- Documented rationale: Optimize token usage in Claude Code sessions

**Test Results:**
- ✅ **82 tests passing** (46 unit + 36 integration)
- ✅ All authentication tests working correctly
- ✅ All cascade delete tests passing
- ✅ All JSON serialization tests passing

---

## Session 2025-10-22 (Part 1): Integration Test Authentication Architecture

**Session Goal:** Fix failing integration tests due to JWT authentication enforcement

### ☕ Backend

**Context:** Integration tests with Testcontainers were failing because JWT authentication was enforced but no valid tokens were being provided in test requests.

**Problem:** How to authenticate test requests without compromising production code security?

**Initial Approach Considered (REJECTED):**
- ❌ Adding `X-User-Id` header to controller endpoints
- **Why rejected:**
  - Security anti-pattern - allows any client to impersonate users
  - Mixes test code with production code
  - Defeats purpose of authentication layer

**Final Solution (ACCEPTED):**

1. **TestSecurityConfig**
   - Disables security only for integration tests (via `@Profile("integration")`)
   - Keeps production security untouched
   ```java
   @Profile("integration")
   @Configuration
   public class TestSecurityConfig {
       @Bean
       public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) {
           return http.csrf(AbstractHttpConfigurer::disable)
                   .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                   .build();
       }
   }
   ```

2. **ReviewController**
   - Modified to extract `userId` from Spring Security `Authentication.getName()` (production-ready)
   - No test-specific code in controller
   ```java
   public ResponseEntity<ReviewResponse> create(
           @RequestBody CreateReviewRequest request,
           Authentication authentication) {
       UUID userId = UUID.fromString(authentication.getName());
       // business logic...
   }
   ```

3. **AbstractIntegrationTest**
   - Added `authenticated(UUID userId)` helper that creates `UsernamePasswordAuthenticationToken`
   ```java
   protected RequestPostProcessor authenticated(UUID userId) {
       return request -> {
           var auth = new UsernamePasswordAuthenticationToken(
               userId.toString(), null, List.of()
           );
           SecurityContextHolder.getContext().setAuthentication(auth);
           return request;
       };
   }
   ```

4. **Integration Tests**
   - Use `.with(authenticated(testUser.getId()))` in `mockMvc.perform()` calls
   ```java
   mockMvc.perform(post("/reviews")
           .with(authenticated(testUser.getId()))
           .contentType(MediaType.APPLICATION_JSON)
           .content(json))
       .andExpect(status().isCreated());
   ```

**Key Insights:**
- **Never compromise production code security for testing convenience**
- **Spring Security Testing provides proper mechanisms:** `RequestPostProcessor` for authentication in tests
- **Separation of concerns:** Test infrastructure should not leak into production code
- **`@Profile` annotations are powerful:** Use for test-specific configurations without affecting production

**Formatting Standard Established:**
- Lambda closing parentheses should be on the same line as the last method call
- Example: `.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());` (not `)` on separate line)
- Documented in `CODING_STYLE.md` section "Formatação de Lambdas e Method Chaining"

**Working Mode Recommendation:**
- ⚠️ Running Claude Code inside IntelliJ terminal causes conflicts with auto-formatters
- **Solution for next session:** Run Claude Code in separate terminal to avoid file modification conflicts

**Test Results:**
- ✅ All 36 integration tests passing after authentication implementation
- ✅ Production code remains secure (no test-specific hacks)
- ✅ Tests use proper Spring Security Testing mechanisms

---

## Update Instructions

**When to add new session:**
- At the end of each development session
- When significant technical decisions are made
- When problems are encountered and solved

**Session Template:**
```markdown
## Session YYYY-MM-DD: Brief Title

**Session Goal:** What you intended to accomplish

### ☕ Backend (if applicable)
**Context:** ...
**What Was Done:** ...
**Key Insights:** ...
**Problems Encountered:** ...
**Solutions:** ...

### 📱 Frontend (if applicable)
**Context:** ...
**What Was Done:** ...
**Key Insights:** ...
**Problems Encountered:** ...
**Solutions:** ...

### 🐳 Infrastructure (if applicable)
**Context:** ...
**What Was Done:** ...
**Key Insights:** ...
**Problems Encountered:** ...
**Solutions:** ...
```

**Commands that update this file:**
- `/finish-session` - Prompts: "Add significant learnings to LEARNINGS.md?"
- Manual editing - For detailed session notes
