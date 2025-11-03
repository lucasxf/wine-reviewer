# Wine Reviewer - Technical Learnings & Decisions Log

This file archives session logs, technical decisions, problems encountered, and key insights organized chronologically by session.

**Organization:** Sessions are listed newest-first, with subsections for Backend (☕), Frontend (📱), and Infrastructure (🐳) work done in each session.

---

## Session 2025-11-03 (Session 11): Documentation & Development Tooling Improvements

**Session Goal:** Establish parity between custom agents and slash commands documentation, verify documentation health

### 📚 Documentation & Tooling

**Context:** Project had comprehensive custom agent suite (6 agents) with detailed README, but slash commands (13 commands) lacked similar documentation. Needed to create parallel documentation structure.

**What Was Done:**

1. **Created `.claude/commands/README.md` (500+ lines):**
   - **Structure:** Mirrored `.claude/agents/README.md` organization for consistency
   - **Content:** Command overview table, usage guide per command, category breakdown (workflow/documentation/testing/build/infrastructure)
   - **Workflows:** 4 complete workflows (daily development, feature implementation, bug fix, refactoring)
   - **Decision Support:** Decision trees for command selection, command vs agent comparison
   - **Best Practices:** Examples of good/poor workflows, token efficiency patterns

2. **Documentation Health Assessment:**
   - Verified all core docs (CLAUDE.md, CODING_STYLE.md, ROADMAP.md, README.md) are current
   - Analyzed PACK.md status → Determined it's historical AI prompt pack reference (v2.2 from 2025-10-18)
   - Confirmed 3-part documentation structure (General/Backend/Frontend) is consistently applied

3. **Tooling Documentation Parity:**
   - **Agents README:** 510 lines covering 6 specialized agents with workflows
   - **Commands README:** 500+ lines covering 13 slash commands with workflows
   - Both use parallel structures: overview table → usage guide → workflows → best practices → decision trees

**Key Insights:**

1. **Commands vs Agents Distinction:**
   - **Commands:** Structured, repeatable workflows (context loading, testing, building) - token-efficient
   - **Agents:** Complex reasoning, design decisions, learning, code review - require deeper analysis
   - **Together:** Commands handle routine, agents handle creative - complementary approach

2. **Documentation Organization Pattern:**
   - `.claude/agents/README.md` - Comprehensive guide (who, when, why, how)
   - `.claude/commands/README.md` - Parallel structure for consistency
   - Both serve as onboarding and reference materials

3. **Token Efficiency Through Documentation:**
   - Well-documented commands reduce need for context explanation
   - Decision trees eliminate ambiguity in tool selection
   - Workflow examples prevent trial-and-error token waste

**Impact:**
- Developer can quickly find right command or agent for task
- Consistent documentation structure reduces cognitive load
- Workflows demonstrate best practices for efficiency
- New contributors/AI sessions can reference comprehensive guides

---

## Session 2025-10-28 (Session 10): Flutter Dependency Major Version Updates

**Session Goal:** Update outdated Flutter dependencies to latest stable versions and resolve breaking changes

### 📱 Frontend

**Context:** Mobile app dependencies were using older versions (Riverpod 2.x, Freezed 2.x, go_router 14.x). Needed to update to latest stable versions to avoid technical debt and leverage new features.

**What Was Done:**

1. **Dependency Version Analysis:**
   - Identified outdated packages: `flutter_riverpod` 2.6.1 → 3.0.3, `freezed` 2.5.8 → 3.2.3, `go_router` 14.8.1 → 16.3.0
   - Researched breaking changes in each major version upgrade
   - Created migration plan with priority order (least breaking first)

2. **Package Updates (7 packages):**
   - `flutter_riverpod`: 2.6.1 → **3.0.3** (major breaking changes)
   - `riverpod_annotation`: 2.6.1 → **3.0.3**
   - `riverpod_generator`: 2.6.2 → **3.0.3**
   - `freezed`: 2.5.8 → **3.2.3** (major breaking changes)
   - `freezed_annotation`: 2.4.4 → **3.1.0**
   - `go_router`: 14.8.1 → **16.3.0** (minimal breaking changes)
   - `flutter_lints`: 5.0.0 → **6.0.0**
   - `json_serializable`: 6.9.5 → **6.11.1** (minor update)
   - `build_runner`: 2.5.4 → **2.7.1** (limited by Flutter SDK)

3. **Breaking Changes Resolution:**

   **Riverpod 3.0:**
   - **Breaking change:** `StateNotifierProvider` moved to `package:flutter_riverpod/legacy.dart`
   - **Reason:** New `Notifier` API is preferred, `StateNotifierProvider` deprecated
   - **Solution:** Added import `import 'package:flutter_riverpod/legacy.dart';` to `auth_providers.dart`
   - **Future work:** Migrate to new `Notifier` API (deferred to avoid scope creep)

   **Freezed 3.0:**
   - **Breaking change:** Simple classes require `abstract` modifier
   - **Example:** `@freezed class User` → `@freezed abstract class User`
   - **Affected:** `User`, `AuthResponse`, `GoogleSignInRequest`

   - **Breaking change:** Union types require `sealed` modifier
   - **Example:** `@freezed class AuthState` → `@freezed sealed class AuthState`
   - **Affected:** `AuthState` (4 union states: initial, loading, authenticated, unauthenticated)

   - **Breaking change:** Union factory names changed
   - **Before:** `_Loading`, `_Authenticated` (underscore prefix)
   - **After:** `AuthStateLoading`, `AuthStateAuthenticated` (descriptive names)
   - **Impact:** `.when()` and `.maybeWhen()` still work (not removed despite docs saying otherwise)

4. **Code Generation Fixes:**
   - Deleted all `.freezed.dart` and `.g.dart` files
   - Re-ran `flutter pub run build_runner build --delete-conflicting-outputs`
   - **Result:** 8 info messages (deprecations), 0 errors, 0 warnings ✅

5. **google_sign_in Decision (Deferred):**
   - **Current version:** 6.3.0
   - **Latest version:** 7.0.0+
   - **Decision:** Keep at 6.x, defer 7.x upgrade to later session
   - **Rationale:** v7 has extensive breaking changes (singleton pattern, `initialize()` required, auth/authorization separation)
   - **Plan:** Upgrade when implementing auth UI integration (Priority 1 task)

6. **Documentation Updates:**
   - Added 51-line "Histórico de Atualizações" section to `DEPENDENCIES_EXPLAINED.md`
   - Documented all breaking changes with examples
   - Added TODO notes for future Riverpod Notifier migration and google_sign_in 7.x upgrade
   - Updated version numbers throughout documentation

7. **Cleanup:**
   - Added `.claude/` to `.gitignore` (Claude Code local settings)
   - Removed deleted `test/widget_test.dart` from staging
   - Deleted auto-generated plugin registrant headers (Linux/Windows)

**Key Insights:**

**1. Riverpod 3.0 Migration Path (StateNotifierProvider → Notifier)**
- **Why change:** New `Notifier` API is simpler, more consistent with other Riverpod providers
- **Legacy support:** `StateNotifierProvider` still works via `legacy.dart` import (not breaking immediately)
- **Migration complexity:** Low-medium (requires refactoring state classes, but compile-time safe)
- **When to migrate:** When adding new features to auth (not during dependency update session)
- **Lesson:** Don't mix dependency updates with architecture refactoring (scope creep risk)

**2. Freezed 3.0 Abstract/Sealed Modifiers (Type Safety)**
- **Why change:** Dart 3 sealed classes provide better exhaustiveness checking for union types
- **Simple classes:** `abstract` prevents direct instantiation (use factories only)
- **Union types:** `sealed` enables compiler to verify all cases covered in `.when()`
- **Migration effort:** Minimal (just add keywords, regenerate code)
- **Benefit:** Compile-time guarantee that all states handled (no runtime surprises)

**3. Freezed Union Factory Naming (Breaking but Good)**
- **Old naming:** `_Loading`, `_Authenticated` (private-looking names)
- **New naming:** `AuthStateLoading`, `AuthStateAuthenticated` (fully qualified)
- **Impact:** Code generation changed, but `.when()` pattern matching still works
- **Lesson:** Freezed's breaking changes are usually improvements (better naming, stronger types)

**4. Incremental Migration Strategy (Risk Management)**
- **Approach:** Update dependencies first, defer architecture changes (Riverpod Notifier, google_sign_in 7.x)
- **Benefit:** Reduces risk of multiple simultaneous breaking changes
- **Trade-off:** Using deprecated APIs temporarily (but with clear migration path)
- **When to complete:** During related feature work (auth UI integration for google_sign_in, state management refactor for Notifier)

**5. Code Generation Tooling (build_runner)**
- **Version constraint:** Limited to 2.7.1 (Flutter SDK compatibility)
- **Best practice:** Always delete generated files before regenerating (`--delete-conflicting-outputs`)
- **Why:** Prevents stale code from mixing with new generation patterns
- **Lesson:** Treat generated code as disposable (never edit `.freezed.dart` or `.g.dart` manually)

**6. Documentation as Migration Guide (Future-Proofing)**
- **Added:** Detailed "Histórico de Atualizações" section with all breaking changes
- **Benefit:** Next developer (or future self) knows exactly what changed and why
- **Format:** Breaking changes → solutions → TODO for future work
- **Lesson:** Document deferred migrations (google_sign_in 7.x, Riverpod Notifier) so they're not forgotten

**Problems Encountered:**

1. **Problem:** Freezed 3.0 code generation errors after version update
   - **Error:** "Classes marked with @freezed must be abstract or sealed"
   - **Root cause:** Freezed 3.0 requires explicit `abstract` or `sealed` modifiers
   - **Solution:** Added `abstract` to simple classes (`User`, `AuthResponse`, `GoogleSignInRequest`), `sealed` to union type (`AuthState`)
   - **Lesson:** Read migration guides before updating major versions

2. **Problem:** Union factory naming mismatch in generated code
   - **Symptom:** `.when()` callbacks using old names didn't match generated code
   - **Root cause:** Freezed 3.0 changed union factory naming convention
   - **Solution:** Regenerated code with `--delete-conflicting-outputs` (freezed updated `.when()` too)
   - **Lesson:** Delete old generated files before regenerating (prevents stale code confusion)

3. **Problem:** Riverpod 3.0 deprecation warnings for `StateNotifierProvider`
   - **Warning:** "StateNotifierProvider is deprecated, use Notifier instead"
   - **Root cause:** Riverpod 3.0 introduced new `Notifier` API as replacement
   - **Solution:** Moved import to `package:flutter_riverpod/legacy.dart` (suppresses warning, still works)
   - **Deferred:** Full migration to `Notifier` API (out of scope for dependency update session)

**Solutions Applied:**

1. **Freezed modifiers:** Added `abstract`/`sealed` keywords to all `@freezed` classes
2. **Code regeneration:** Deleted all `.freezed.dart`/`.g.dart`, re-ran `build_runner`
3. **Riverpod legacy import:** Changed to `package:flutter_riverpod/legacy.dart` for `StateNotifierProvider`
4. **Documentation:** Added comprehensive migration notes to `DEPENDENCIES_EXPLAINED.md`
5. **Deferred upgrades:** Documented TODOs for google_sign_in 7.x and Riverpod Notifier migration

**Build Results:**
- ✅ **Info messages:** 8 (all deprecation warnings for legacy APIs)
- ✅ **Errors:** 0
- ✅ **Warnings:** 0
- ✅ **Generated files:** 8 (4 `.freezed.dart` + 4 `.g.dart`)
- ✅ **Build status:** Success

**Metrics:**
- **Packages updated:** 7 (3 major version updates + 4 minor updates)
- **Breaking changes resolved:** 3 (Riverpod StateNotifierProvider, Freezed abstract/sealed, Freezed union naming)
- **Documentation added:** 51 lines (DEPENDENCIES_EXPLAINED.md update history)
- **Files modified:** 21 (pubspec, models, providers, docs, gitignore)
- **Commits:** 1 (consolidated dependency update)

**Next Steps:**
- Integrate AuthService with UI (Priority 1 from ROADMAP.md)
- Complete migration to Riverpod `Notifier` API (when refactoring auth state management)
- Upgrade google_sign_in to 7.x (when implementing Google Sign-In UI flow)
- Add unit tests for AuthService (mock GoogleSignIn, DioClient)

---

## Session 2025-10-27 (Session 9): Docker Cross-Platform Fixes

**Session Goal:** Review and validate Docker Compose fixes for cross-platform development (Windows ↔ Linux)

### 🐳 Infrastructure

**Context:** User set up development environment on a second PC (Windows). Encountered `docker compose up` failures related to line endings and Maven Wrapper. Googled solutions and fixed the issues independently. This session reviews those fixes.

**What Was Done:**

1. **Line Ending Fix (`.gitattributes`):**
   - **Problem:** Maven Wrapper (`mvnw`) uses Unix shell syntax (#!/bin/sh). Windows Git checks out files with CRLF line endings by default. When Docker (Linux) tries to execute `mvnw`, it fails with `/bin/sh^M: bad interpreter` (^M is carriage return \r).
   - **Solution:** Created `.gitattributes` to enforce LF (Unix) line endings for Maven Wrapper scripts:
     ```gitattributes
     mvnw text eol=lf
     .mvn/wrapper/maven-wrapper.properties text eol=lf
     ```
   - **How it works:** Git auto-converts line endings when checking out/committing, ensuring scripts always have LF regardless of platform.
   - **Benefit:** Prevents line ending issues at the source (Git level).

2. **Defensive Line Ending Normalization (Dockerfile):**
   - **Problem:** If files were already committed with CRLF before `.gitattributes` was added, Git won't retroactively fix them.
   - **Solution:** Added defensive `sed` command in Dockerfile to strip CRLF:
     ```dockerfile
     # Normalize line endings (in case CRLF from Windows)
     RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
     ```
   - **How it works:** Strips carriage return (\r) from end of each line, then makes script executable.
   - **Benefit:** Belt-and-suspenders approach. Ensures Docker build works even if `.gitattributes` fails or wasn't committed yet.

3. **Maven Wrapper JAR Fix (`.dockerignore`):**
   - **Problem:** `.dockerignore` was excluding `.mvn/wrapper/maven-wrapper.jar`, causing `./mvnw` to fail with "wrapper JAR not found".
   - **Solution:** Commented out the exclusion (JAR now copied to Docker):
     ```diff
     -.mvn/wrapper/maven-wrapper.jar
     +# .mvn/wrapper/maven-wrapper.jar
     ```
   - **How it works:** Maven Wrapper (`mvnw`) requires `maven-wrapper.jar` to bootstrap Maven. Without it, `mvnw` can't download Maven dependencies.
   - **Benefit:** Allows `./mvnw` to work inside Docker container.

4. **Tests Verification:**
   - Ran `./mvnw test -q` to verify Docker fixes didn't break anything
   - **Result:** All 103 tests passing (58 unit + 45 integration) ✅

**Key Insights:**

**1. Cross-Platform Line Ending Issues (CRLF vs LF)**
- **Root Cause:** Git default behavior on Windows uses CRLF (`\r\n`), but Linux uses LF (`\n`). Shell scripts with CRLF fail in Docker (Linux).
- **Solution Hierarchy:**
  1. **Git Level:** `.gitattributes` (prevents problem at source)
  2. **Build Level:** Dockerfile `sed` (fixes problem at build time)
  3. **Why Both:** Defense-in-depth. `.gitattributes` is primary solution; Dockerfile `sed` is safety net.
- **Lesson:** Always use `.gitattributes` for cross-platform projects with shell scripts.

**2. Maven Wrapper Internals**
- **How it works:** `mvnw` is a shell script that:
  1. Checks for `MAVEN_HOME` environment variable
  2. If not found, uses `.mvn/wrapper/maven-wrapper.jar` to download Maven
  3. Executes Maven commands with downloaded binary
- **Critical files:**
  - `mvnw` - Shell script (requires LF line endings)
  - `.mvn/wrapper/maven-wrapper.jar` - Bootstrap JAR (must be in Docker)
  - `.mvn/wrapper/maven-wrapper.properties` - Maven version config
- **Lesson:** Never exclude Maven Wrapper JAR from Docker; it's required for `mvnw` to work.

**3. Docker `.dockerignore` Best Practices**
- **Purpose:** Reduce build context size, exclude unnecessary files (logs, IDE config, etc.)
- **Caution:** Don't exclude files required for build (Maven Wrapper JAR, source code, `pom.xml`)
- **Common Mistake:** Blindly excluding `.mvn/` directory or `*.jar` files
- **Lesson:** Test Docker builds on clean checkout to catch missing dependencies early.

**4. Debugging Docker Build Failures (Cross-Platform)**
- **Symptoms:**
  - `/bin/sh^M: bad interpreter` → CRLF line endings
  - `wrapper JAR not found` → Missing `.mvn/wrapper/maven-wrapper.jar`
  - `Permission denied` → Missing executable bit (`chmod +x`)
- **Debugging Tools:**
  - `git ls-files --eol` - Shows line endings for tracked files
  - `docker run --rm -it <image> sh` - Interactive shell in container for debugging
  - `od -c mvnw | head` - Inspect file for CRLF (`\r\n`)
- **Lesson:** Line endings and file permissions are common cross-platform pitfalls.

**5. Documentation Anti-Pattern (Unrelated Changes)**
- **Problem:** User added `settings.local.json fix this file and remove duplicate entries` to CLAUDE.md (unrelated to Docker fixes).
- **Why it's bad:** Mixing unrelated changes in same commit makes git history harder to understand.
- **Correct approach:** Create separate commits for separate concerns (Docker fixes vs settings cleanup).
- **Lesson:** Keep commits focused on single logical change.

**Recommendations:**

1. **Add comment to `.gitattributes`:**
   ```gitattributes
   # Maven Wrapper scripts must use LF (Unix line endings) to run in Docker containers
   mvnw text eol=lf
   .mvn/wrapper/maven-wrapper.properties text eol=lf
   ```

2. **Remove CLAUDE.md unrelated change** before committing

3. **Consider adding to `.gitattributes`:**
   ```gitattributes
   # Force LF for all shell scripts
   *.sh text eol=lf
   ```

---

## Session 2025-10-26 (Session 8): Test Documentation & File Upload Feature

**Session Goal:** Review test fixes, document Testcontainers best practices, and verify file upload feature implementation

### ☕ Backend

**Context:** User fixed all failing tests independently (103 tests now passing). Need to review changes, extract learnings, and document best practices for future development.

**What Was Done:**

1. **Test Fixes Review:**
   - Reviewed AbstractIntegrationTest with shared PostgreSQL container pattern
   - Analyzed authentication helper: `authenticated(UUID userId)` for Spring Security integration
   - Reviewed AuthControllerIT (13 tests) and ReviewControllerIT (23 tests)
   - Verified S3ServiceTest (12 tests) and FileUploadControllerIT (9 tests)
   - **Result:** All 103 tests passing (58 unit + 45 integration) ✅

2. **Documentation Updates:**
   - Added 65+ line "Testcontainers Integration Tests" section to CLAUDE.md
   - Updated README.md with enhanced test suite technical details
   - Verified CODING_STYLE.md closing parenthesis directive (already documented)
   - Updated ROADMAP.md with file upload completion and test metrics

3. **Bug Fix:**
   - Fixed `WineReviewerApiApplication.main()` missing `public` modifier
   - **Problem:** Docker build failed with "Unable to find main class"
   - **Solution:** Added `public` modifier to `main()` method

4. **File Upload Feature (Completed by User):**
   - S3Service with AWS SDK v2 integration
   - FileUploadController with pre-signed URL generation
   - Custom exceptions: FileTooLargeException, FileUploadException, InvalidFileException, UnsupportedFileTypeException
   - Comprehensive tests: S3ServiceTest (12 tests), FileUploadControllerIT (9 tests)

**Key Insights:**

**1. Testcontainers Over H2 (Production Parity)**
- **Problem:** H2 in-memory database has different constraint behavior than PostgreSQL
- **Solution:** Use Testcontainers with real PostgreSQL container
- **Benefits:**
  - Database constraints (CHECK, FK, CASCADE) work exactly like production
  - SQL dialects match (no surprises in production)
  - CI/CD ready (no manual database setup)
- **Example:**
  ```java
  @Container
  protected static final PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("winereviewer_test")
          .withReuse(true); // ← Performance optimization
  ```

**2. Shared Container Pattern (Performance)**
- **Problem:** Starting new PostgreSQL container per test class is slow (3-5s overhead each)
- **Solution:** Use static `@Container` with `.withReuse(true)`
- **How it works:**
  - Static container starts once and is shared across all test classes
  - Ryuk container manages cleanup (terminates on JVM exit)
  - `@Transactional` ensures test isolation (each test gets rollback)
- **Performance gain:** ~90% faster (3-5s → <0.5s per test class after first)
- **Trade-off:** Must enable reuse in `~/.testcontainers.properties`: `testcontainers.reuse.enable=true`

**3. Authentication Helpers (Integration Tests)**
- **Problem:** Integration tests need to simulate authenticated requests without real JWT tokens
- **Solution:** Create `authenticated(UUID userId)` helper in AbstractIntegrationTest
- **Implementation:**
  ```java
  protected RequestPostProcessor authenticated(UUID userId) {
      Authentication auth = new UsernamePasswordAuthenticationToken(
          userId.toString(), null,
          List.of(new SimpleGrantedAuthority("ROLE_USER")));
      return authentication(auth);
  }
  ```
- **Usage:** `mockMvc.perform(post("/reviews").with(authenticated(testUser.getId())))`
- **Benefit:** Simulates Spring Security authentication without requiring real JWT validation

**4. Mock External Dependencies (Not Infrastructure)**
- **Problem:** Integration tests should test YOUR code, not external services
- **Solution:** Mock external APIs (GoogleTokenValidator, S3Client) but use real infrastructure (database)
- **Pattern:**
  ```java
  @MockBean
  private GoogleTokenValidator googleTokenValidator;  // External API

  @MockBean
  private S3Client s3Client;  // AWS service

  // PostgreSQL container is REAL (infrastructure)
  ```
- **Rationale:** Database is part of your system architecture; Google OAuth and AWS S3 are external dependencies

**5. Test Isolation with @Transactional (Automatic Rollback)**
- **Problem:** Tests pollute database state (one test's data affects next test)
- **Solution:** Add `@Transactional` on test class
- **How it works:**
  - Each test method runs in a transaction
  - Transaction is automatically rolled back after test completes
  - Next test starts with clean database state
- **Benefit:** No manual cleanup code needed, tests can run in any order

**6. Documentation Organization (Single Source of Truth)**
- **Decision:** Do NOT create separate TESTING_GUIDELINES.md file
- **Rationale:**
  - Testing guidelines already comprehensive in CLAUDE.md Part 2 (Backend)
  - Avoids duplication and maintenance overhead
  - Single source of truth easier to keep updated
  - Follows project's documentation strategy (CLAUDE.md for architecture)

**Problems Encountered:**

1. **Problem:** Docker build failed with "Unable to find main class"
   - **Root cause:** `WineReviewerApiApplication.main()` was missing `public` modifier
   - **Error:** Spring Boot Maven plugin couldn't detect main class during repackage
   - **Solution:** Changed `static void main(...)` → `public static void main(...)`
   - **Lesson:** Always use `public static void main(String[] args)` for Spring Boot applications

2. **Problem:** Initial confusion about whether TESTING_GUIDELINES.md was needed
   - **Root cause:** Large amount of testing best practices to document
   - **Decision:** Keep all guidelines in CLAUDE.md (single source of truth)
   - **Benefit:** Easier maintenance, no duplication, follows existing doc strategy

**Solutions Applied:**

1. **CLAUDE.md Enhancement:**
   - Added "Testcontainers Integration Tests (CRITICAL)" section (65+ lines)
   - Why Testcontainers over H2
   - Base class pattern with performance optimizations
   - Authentication helpers documentation
   - Mock external dependencies pattern
   - Integration test structure and key testing patterns
   - Complete example test with CRUD coverage
   - Common mistakes to avoid section

2. **README.md Update:**
   - Enhanced test suite description with technical details
   - Added shared container pattern, authentication helpers
   - Highlighted full CRUD coverage and edge case testing

3. **Main Class Fix:**
   - Added `public` modifier to `WineReviewerApiApplication.main()`

**Test Results:**
- ✅ **Unit tests:** 58/58 passing (ReviewControllerTest: 4, DomainExceptionTest: 12, AuthServiceTest: 5, GoogleTokenValidatorTest: 5, ReviewServiceTest: 20, S3ServiceTest: 12)
- ✅ **Integration tests:** 45/45 passing (ReviewControllerIT: 23, AuthControllerIT: 13, FileUploadControllerIT: 9)
- ✅ **Total:** 103/103 tests passing (100% pass rate)
- ✅ **Coverage:** Review CRUD (100%), Auth (100%), File Upload (100%), Database constraints (100%), Exception scenarios (100%)

**Metrics:**
- **Files updated:** 3 (CLAUDE.md, README.md, WineReviewerApiApplication.java)
- **Test count increase:** 82 → 103 (+21 tests, +25%)
- **Documentation added:** 65+ lines of Testcontainers best practices
- **Commits:** 1 comprehensive documentation update

**Next Steps:**
- Integrate file upload with Review entity (add imageUrl field usage)
- Frontend Flutter integration (image picker → S3 upload flow)
- Continue with Priority 1 from ROADMAP.md (Flutter authentication UI integration)

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
