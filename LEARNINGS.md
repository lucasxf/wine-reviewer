# Wine Reviewer - Technical Learnings & Decisions Log

This file archives session logs, technical decisions, problems encountered, and key insights organized chronologically by session.

**Organization:** Sessions are listed newest-first, with subsections for Backend (☕), Frontend (📱), and Infrastructure (🐳) work done in each session.

---

## Session 2025-10-25 (Session 7): AuthService Implementation

**Session Goal:** Implement complete authentication service with Google Sign-In, backend API integration, Riverpod state management, and secure token storage

### 📱 Frontend (Flutter)

**Context:** Backend has Google OAuth authentication (POST /api/auth/google) returning JWT token. Mobile app needs complete authentication flow with auto-login support.

**What Was Done:**
1. **Domain Layer (Models):**
   - Created freezed models: `User`, `AuthResponse`, `GoogleSignInRequest`
   - Matched `AuthResponse` structure with backend (flat fields: token, userId, email, displayName, avatarUrl)
   - Generated `.freezed.dart` and `.g.dart` files with build_runner

2. **Data Layer (AuthService):**
   - Created `AuthService` interface (abstract contract)
   - Implemented `AuthServiceImpl` with Google Sign-In + backend API integration
   - Methods: `signInWithGoogle()`, `signOut()`, `getCurrentUser()`, `refreshAccessToken()`
   - Complete OAuth flow: Google dialog → ID token → Backend validation → JWT storage

3. **Providers (Riverpod State Management):**
   - Created `AuthState` union type with 4 states (initial, authenticated, unauthenticated, loading)
   - Implemented `AuthStateNotifier` (StateNotifier pattern)
   - Created 4 providers: `googleSignInProvider`, `authServiceProvider`, `authStateNotifierProvider`, `currentUserProvider`
   - Full dependency injection with Riverpod (no tight coupling)

4. **Secure Storage (Documentation & Integration):**
   - Created comprehensive README.md (453 lines) explaining flutter_secure_storage
   - Created `StorageKeys` class with centralized key constants
   - Refactored `AuthInterceptor` to use `StorageKeys.authToken`
   - Documented KeyStore/Keychain hardware encryption

**Key Insights:**

**1. Union Types vs Boolean Flags (Type-Safety)**
- **Problem:** Boolean flags allow invalid states (`isLoading=true + isAuthenticated=true`)
- **Solution:** Freezed union types ensure type-safe states (compiler enforces valid states)
- **Example:**
  ```dart
  // ❌ Bad (boolean flags)
  class AuthState { bool isLoading; bool isAuth; User? user; }

  // ✅ Good (union type)
  @freezed
  class AuthState with _$AuthState {
    const factory AuthState.loading() = _Loading;
    const factory AuthState.authenticated(User user) = _Authenticated;
  }
  ```
- **Benefit:** If `authenticated`, user is ALWAYS non-null (compiler guarantee)

**2. StateNotifier vs ChangeNotifier (Immutability)**
- **Problem:** ChangeNotifier uses mutable state (prone to bugs from accidental mutations)
- **Solution:** StateNotifier enforces immutable state changes
- **Example:**
  ```dart
  // StateNotifier - immutable
  state = const AuthState.loading();  // Replaces entire state
  state = AuthState.authenticated(user);  // New state object
  ```
- **Benefit:** Predictable state transitions, easier debugging, time-travel debugging support

**3. Provider vs Service Locator (Compile-Time Safety)**
- **Problem:** GetIt/ServiceLocator uses runtime lookups (crashes if dependency not registered)
- **Solution:** Riverpod providers fail at compile-time if dependency missing
- **Example:**
  ```dart
  // Riverpod - compile-time safe
  final authService = ref.watch(authServiceProvider);  // Compiler knows type

  // GetIt - runtime only
  final authService = getIt<AuthService>();  // Crashes at runtime if not registered
  ```
- **Benefit:** Catch errors during development, not production

**4. Derived Providers (Boilerplate Reduction)**
- **Problem:** UI needs to extract data from complex state (e.g., User from AuthState)
- **Solution:** Create derived provider that auto-updates when source changes
- **Example:**
  ```dart
  final currentUserProvider = Provider<User?>((ref) {
    final authState = ref.watch(authStateNotifierProvider);
    return authState.maybeWhen(
      authenticated: (user) => user,
      orElse: () => null,
    );
  });
  ```
- **Benefit:** Simplifies UI code (no need for `.when()` pattern matching everywhere)

**5. Interface Segregation (Testability)**
- **Decision:** Separate interface (`AuthService`) from implementation (`AuthServiceImpl`)
- **Reason:** Easy to mock in tests, swap implementations, follow SOLID principles
- **Example:**
  ```dart
  // Production
  final authServiceProvider = Provider<AuthService>((ref) {
    return AuthServiceImpl(...);
  });

  // Tests
  final mockAuthService = MockAuthService();
  container.overrideWithValue(authServiceProvider, mockAuthService);
  ```
- **Benefit:** Tests don't depend on real Google Sign-In or backend API

**6. Hardware Encryption (Security)**
- **Learning:** flutter_secure_storage uses native platform encryption (not software AES)
- **Android:** KeyStore with hardware-backed keys (Secure Element)
- **iOS:** Keychain with Secure Enclave (dedicated crypto chip)
- **Benefit:** Tokens protected even if device is rooted/jailbroken

**Problems Encountered:**

1. **Problem:** AuthResponse model didn't match backend structure (had nested `user` object)
   - **Root cause:** Initial assumption backend would return `{token, refreshToken, user: {...}}`
   - **Reality:** Backend returns flat `{token, userId, email, displayName, avatarUrl}`
   - **Solution:** Refactored `AuthResponse` to match backend exactly (removed nested `user`, no `refreshToken`)
   - **Lesson:** Always check backend API response structure before creating models

2. **Problem:** Dio client `post<T>()` generic type caused analyzer error
   - **Error:** `wrong_number_of_type_arguments_method`
   - **Root cause:** DioClient.post() doesn't accept type parameter (returns `Response<dynamic>`)
   - **Solution:** Removed `<Map<String, dynamic>>` and cast `response.data as Map<String, dynamic>`
   - **Lesson:** Check Dio method signatures before using generics

3. **Problem:** Freezed `toJson()` override caused `annotate_overrides` warning
   - **Root cause:** Manually added `toJson()` but freezed already generates it
   - **Solution:** Removed manual `toJson()` method (let freezed handle it)
   - **Lesson:** Trust freezed code generation (don't override generated methods)

**Solutions Applied:**

1. **AuthResponse refactor:** Changed from nested to flat structure matching backend
2. **Dio client casting:** Use `response.data as Map<String, dynamic>` instead of generic type
3. **Build runner fixes:** Removed manual `toJson()`, regenerated freezed code
4. **Storage keys:** Created `StorageKeys` class to prevent magic strings and typos
5. **Documentation:** Added comprehensive README.md explaining hardware encryption and best practices

**Metrics:**
- **Files created:** 18 (3 models + 6 generated + 2 services + 3 providers + 1 generated + 2 storage + 1 README)
- **Lines of code:** ~3500 (including generated code + documentation)
- **Commits:** 5 (domain models, freezed generation, AuthService, Riverpod providers, storage docs)
- **Dependencies:** 0 new (all packages already configured)
- **Tests:** 0 (unit tests for AuthService will be added in future session)

**Next Steps:**
- Integrate AuthService with UI (main.dart, login_screen.dart, splash_screen.dart, app_router.dart)
- Add unit tests for AuthService (mock GoogleSignIn, DioClient)
- Implement getCurrentUser() completely (decode JWT or call backend /api/auth/me)
- Add refresh token support when backend implements /api/auth/refresh

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
