# Wine Reviewer - Project Roadmap

**Last updated:** 2025-10-29 (Session 11 - Custom Agent Suite Expansion)

This file tracks the current implementation status and next steps for the Wine Reviewer project.

---

## ✅ Implemented

### Backend API (Spring Boot)

**Core Features:**
- Complete Review CRUD endpoints (`ReviewController`, `ReviewService`)
- Comment endpoints (`CommentController`)
- JWT authentication structure (`JwtUtil`, `JwtProperties`) - updated to JJWT 0.12.x
- Google OAuth authentication (`AuthService`, `GoogleTokenValidator`)
- **File Upload with S3** (`S3Service`, `FileUploadController`) - ✅ NEW (2025-10-26)
  - Pre-signed URL generation for direct browser uploads
  - File validation (size limits, MIME types)
  - Custom exceptions: `FileTooLargeException`, `FileUploadException`, `InvalidFileException`, `UnsupportedFileTypeException`
  - AWS S3 integration with SDK v2
- Domain exception hierarchy (`DomainException` base, `ResourceNotFoundException`, `InvalidRatingException`, `UnauthorizedAccessException`, `BusinessRuleViolationException`, `InvalidTokenException`)
- Global exception handling (`GlobalExceptionHandler`) with domain exception support
- Database entities: User, Wine, Review, Comment (with domain exception validation)
- Flyway migrations setup (V1 init + V2 seed mock user)
- OpenAPI/Swagger documentation
- Application configuration with profiles (dev/prod)
- Docker support (Dockerfile + docker-compose)

**Testing:** - ✅ UPDATED (2025-10-26)
- Complete unit test suite (58 tests, 100% passing)
  - `ReviewControllerTest`, `ReviewServiceTest`, `S3ServiceTest` (NEW)
  - `DomainExceptionTest`, `AuthServiceTest`, `GoogleTokenValidatorTest`
- Integration tests with Testcontainers (45 tests, 100% passing)
  - `ReviewControllerIT` (23 tests), `AuthControllerIT` (13 tests), `FileUploadControllerIT` (9 tests - NEW)
  - Real PostgreSQL container with production parity
  - Shared container pattern for performance (`.withReuse(true)`)
  - Authentication helpers (`authenticated(UUID userId)`)
  - Mock external dependencies (GoogleTokenValidator, S3Client)
- **Total:** 103 tests (all passing) - UP from 82 tests
- **Coverage:** Review CRUD (100%), Auth (100%), File Upload (100%), Database constraints (100%), Exception scenarios (100%)
- **Documentation:** Comprehensive Testcontainers guidelines in CLAUDE.md (65+ lines of best practices)

### Mobile App (Flutter)

**Project Setup:** (as of 2025-10-25)
- Flutter 3.35.6 initialized with package `com.winereviewer.wine_reviewer_mobile`
- Feature-first architecture: `lib/features/` (auth, review, wine), `lib/core/`, `lib/common/`
- Platform support: Android (primary), iOS, Web, Windows, macOS, Linux (generated)

**Dependencies Configured (10 packages):** - ✅ UPDATED (2025-10-28)
- `flutter_riverpod` 3.0.3 - State management + DI (updated from 2.6.1, using legacy StateNotifierProvider)
- `go_router` 16.3.0 - Navigation with deep linking (updated from 14.6.2)
- `dio` 5.7.0 - HTTP client with interceptors
- `freezed` 3.2.3 + `json_serializable` 6.11.1 - Immutable models (updated from 2.5.7, breaking changes resolved)
- `flutter_secure_storage` 9.2.3 - Encrypted token storage
- `image_picker` 1.1.2 + `cached_network_image` 3.4.1 - Image handling
- `google_sign_in` 6.3.0 - OAuth authentication (kept at 6.x, defer 7.x migration)
- `build_runner` 2.7.1 + `golden_toolkit` 0.15.0 - Code gen + testing

**Dependency Updates Completed:** (2025-10-28)
- ✅ Updated 7 packages to latest stable versions (Riverpod 2.x→3.x, Freezed 2.x→3.x, go_router 14.x→16.x)
- ✅ Resolved breaking changes: Riverpod StateNotifierProvider → legacy.dart, Freezed abstract/sealed modifiers
- ✅ Regenerated all freezed/json_serializable models with updated code generation
- ✅ Updated comprehensive documentation in DEPENDENCIES_EXPLAINED.md (51+ lines of migration notes)
- ✅ Verified build success: 8 info messages (deprecations), 0 errors, 0 warnings
- ✅ Cleaned up .gitignore to exclude auto-generated plugin registrant files

**Core Configuration:**
- `app_colors.dart` - Color palette (wine theme)
- `app_theme.dart` - Material Design 3 theme
- `api_constants.dart` - API URLs, endpoints, timeouts

**Network Layer (HTTP Client):**
- `dio_client.dart` - HTTP client configurado (Singleton, timeouts, headers)
- `auth_interceptor.dart` - JWT token automático (flutter_secure_storage)
- `logging_interceptor.dart` - Logging de requisições/respostas (debug mode)
- `network_exception.dart` - 9 exceções customizadas (401, 403, 404, 400, 422, 5xx, timeout, connection)
- `network_providers.dart` - Riverpod providers (DI para DioClient, AuthInterceptor, Storage)
- `lib/core/network/README.md` - Documentação completa de uso

**Router & Navigation (go_router):**
- `app_router.dart` - Configuração declarativa de rotas (4 rotas + error handler)
- `splash_screen.dart` - Tela inicial (verifica autenticação, redireciona)
- `login_screen.dart` - Tela de login (Google Sign-In + MVP testing)
- `home_screen.dart` - Feed de reviews (ListView.builder com 10 reviews mockados)
- `review_details_screen.dart` - Detalhes do review (parametrizado por ID)
- Rotas: `/` (splash), `/login`, `/home`, `/review/:id`
- Deep linking support, parametrized routes, error handling (404)

**Authentication (AuthService):** (as of 2025-10-25)
- `lib/features/auth/domain/models/` - Domain models (User, AuthResponse, GoogleSignInRequest) with freezed
- `lib/features/auth/data/services/` - AuthService interface + implementation (Google Sign-In + backend API)
- `lib/features/auth/providers/` - Riverpod providers (AuthState, AuthStateNotifier, auth providers)
- `lib/core/storage/` - Secure storage documentation (README.md, storage_keys.dart)
- Complete authentication flow: Google OAuth → Backend validation → JWT token → Secure storage
- State management with Riverpod (AuthState union type with 4 states)
- Auto-login support (checkAuthStatus on app startup)
- Comprehensive documentation with backend analogies

**Documentation:**
- `DEPENDENCIES_EXPLAINED.md` - Detailed package explanations
- `SETUP_INSTRUCTIONS.md` - Development environment setup
- `lib/core/storage/README.md` - Flutter Secure Storage documentation

### Infrastructure

**Docker & Testing:**
- Docker Compose with PostgreSQL 16 and API service
- Health checks and dependencies configured
- Testcontainers integration test infrastructure (`AbstractIntegrationTest`, `application-integration.yml`)
- **Cross-platform support** (Windows ↔ Linux) - ✅ NEW (2025-10-27)
  - `.gitattributes` enforces Unix line endings (LF) for Maven Wrapper scripts
  - Dockerfile with defensive line ending normalization (`sed -i 's/\r$//' mvnw`)
  - Fixed `.dockerignore` to include Maven Wrapper JAR (required for `mvnw` to work)

**CI/CD:**
- GitHub Actions for API (`ci-api.yml`) with path filters
- GitHub Actions for Mobile (`ci-app.yml`) with path filters
- Release workflow (`release.yml`)

**Custom Agent Suite:** - ✅ NEW (2025-10-29)
- 8 specialized agents in `.claude/agents/` with distinct responsibilities
  - `security-guardian` - Security and secrets management
  - `test-architect` - Test strategies and TDD workflows
  - `code-reviewer` - Code quality and CODING_STYLE.md enforcement
  - `mobile-specialist` - Flutter/Dart expertise with beginner-friendly explanations
  - `performance-sentinel` - Performance analysis and optimization
  - `integration-engineer` - System integration and E2E workflows
  - `tech-writer` - Documentation specialist (ADRs, Javadoc, OpenAPI, Dartdoc) ⭐ NEW
  - `automation-sentinel` - Meta-agent for automation lifecycle management ⭐ NEW
- Agent coordination system with README.md (anti-cyclic dependency rules)
- Automated documentation updates (`/finish-session` delegates to tech-writer and automation-sentinel)
- Health monitoring with automation metrics and recommendations

---

## 🚧 In Progress

- None currently

---

## 🎯 Next Steps (Priority Order)

### 1. 📱 PRIORITY 1: Integrate Authentication with UI (Started 2025-10-25)

**Status:** Ready to start

**Goal:** Connect AuthService with login screen and implement full authentication flow

**Tasks:**
- Update `main.dart` to initialize AuthStateNotifier (checkAuthStatus on startup)
- Update `login_screen.dart` to use AuthStateNotifier (replace mock with real Google Sign-In)
- Update `splash_screen.dart` to handle AuthState (initial → authenticated/unauthenticated)
- Update `app_router.dart` to protect routes (redirect to login if unauthenticated)
- Test complete flow: App startup → Auto-login OR Login screen → Home screen → Logout
- Handle errors gracefully (show SnackBar with error messages)

**Acceptance Criteria:**
- ✅ App starts with splash screen (checks if token exists)
- ✅ If token exists → Home screen (auto-login)
- ✅ If no token → Login screen
- ✅ Click "Sign in with Google" → Opens Google dialog → Authenticates → Home screen
- ✅ Click "Logout" → Clears tokens → Login screen
- ✅ Error handling (cancel login, network error, invalid token)

---

### 2. 📱 Implement Flutter Authentication Flow (F2 Phase) - ✅ COMPLETED

**Status:** ✅ Completed (2025-10-25)

**Completed:**
- ✅ Create auth feature structure (data/domain/presentation/providers)
- ✅ Implement Google Sign-In integration (AuthServiceImpl)
- ✅ Setup flutter_secure_storage for JWT token persistence (StorageKeys, documentation)
- ✅ Implement auto-login support (checkAuthStatus method)
- ✅ Riverpod state management (AuthState, AuthStateNotifier, providers)
- ✅ Comprehensive documentation (storage README, backend analogies)

**Pending:**
- ⏳ UI integration (connect AuthService with screens)
- ⏳ End-to-end testing (full authentication flow)

---

### 3. 🖼️ Implement Image Upload with Pre-signed URLs (Backend) - ✅ COMPLETED

**Status:** ✅ Completed (2025-10-26)

**Completed:**
- ✅ Chose AWS S3 as storage provider (Free Tier)
- ✅ Implemented S3Service with AWS SDK v2 integration
- ✅ Created FileUploadController with pre-signed URL generation endpoint
- ✅ Added file validation (size limits, MIME types: image/jpeg, image/png, image/webp)
- ✅ Created custom exception hierarchy (FileTooLargeException, FileUploadException, etc.)
- ✅ Added comprehensive tests (S3ServiceTest: 12 tests, FileUploadControllerIT: 9 tests)
- ✅ OpenAPI/Swagger documentation for new endpoints

**Pending:**
- ⏳ Update Review entity to use uploaded image URLs (future work)
- ⏳ Frontend integration (Flutter image picker → upload flow)

---

### 4. 💬 Implement Comment System (Backend)

**Goal:** Enable users to comment on wine reviews

**Tasks:**
- Complete CRUD endpoints for comments
- Add OpenAPI/Swagger documentation
- Create unit tests for comment service
- Create integration tests for comment endpoints
- Test cascade delete (comments deleted when review deleted)

---

## 📍 Future Backlog (Post-MVP)

### Observability
- Structured JSON logging
- HTTP metrics (p95 latency)
- Distributed tracing with OpenTelemetry
- OTLP export to Grafana Cloud Free or CloudWatch Free

### User Follow System
- Follow/unfollow users
- View followed users' reviews
- Notifications for new reviews from followed users

### Wine Recommendations
- Recommendation algorithm based on user preferences
- "Users who liked this also liked..." feature

### Internationalization
- i18n support for multiple languages
- PT-BR and EN-US initially

### iOS Support
- Expand Flutter app to iOS
- App Store submission
- iOS-specific testing and optimization

---

## 🚫 Blocked / Waiting

- None currently

---

## 📊 Metrics

| Metric | Value |
|--------|-------|
| **Backend Tests** | 103 (58 unit + 45 integration) ⬆️ |
| **Test Pass Rate** | 100% ✅ |
| **Backend Endpoints** | Review CRUD + Auth + File Upload |
| **Flutter Dependencies** | 10 configured (updated 2025-10-28) |
| **Flutter Auth Components** | 18 files (models, services, providers, docs) |
| **Flutter Screens** | 4 (splash, login, home, review details) |
| **Custom Agents** | 8 (6 existing + 2 new: tech-writer, automation-sentinel) ⬆️ |
| **CI/CD Pipelines** | 3 (API, Mobile, Release) |
| **Documentation Files** | ADR-001 (agent architecture) created by tech-writer |

---

## 🔄 Update Instructions

**When to update this file:**
- At the end of each development session
- When completing a major feature or task
- When adding new planned features to backlog

**How to update:**
1. Move completed tasks from "In Progress" to "Implemented"
2. Update "Next Steps" priorities (reprioritize 1, 2, 3, 4...)
3. Add new tasks discovered during development
4. Update "Last updated" timestamp at top
5. Update metrics table if applicable

**Commands that update this file:**
- `/update-roadmap <what-was-completed>` - Automated update
- `/finish-session [commit-context]` - Prompts for roadmap update
