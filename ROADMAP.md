# Wine Reviewer - Project Roadmap

**Last updated:** 2025-10-25 (Session 7 - AuthService Implementation - Completed)

This file tracks the current implementation status and next steps for the Wine Reviewer project.

---

## ✅ Implemented

### Backend API (Spring Boot)

**Core Features:**
- Complete Review CRUD endpoints (`ReviewController`, `ReviewService`)
- Comment endpoints (`CommentController`)
- JWT authentication structure (`JwtUtil`, `JwtProperties`) - updated to JJWT 0.12.x
- Google OAuth authentication (`AuthService`, `GoogleTokenValidator`)
- Domain exception hierarchy (`DomainException` base, `ResourceNotFoundException`, `InvalidRatingException`, `UnauthorizedAccessException`, `BusinessRuleViolationException`, `InvalidTokenException`)
- Global exception handling (`GlobalExceptionHandler`) with domain exception support
- Database entities: User, Wine, Review, Comment (with domain exception validation)
- Flyway migrations setup
- OpenAPI/Swagger documentation
- Application configuration with profiles (dev/prod)
- Docker support (Dockerfile + docker-compose)

**Testing:**
- Complete unit test suite (46 tests, 100% passing)
  - `ReviewControllerTest`, `ReviewServiceTest`
  - `DomainExceptionTest`, `AuthServiceTest`, `GoogleTokenValidatorTest`
- Integration tests with Testcontainers (36 tests)
  - `ReviewControllerIT`, `AuthControllerIT`
- **Total:** 82 tests (all passing)
- **Coverage:** Review CRUD (100%), Auth (100%), Database constraints (100%), Exception scenarios (100%)

### Mobile App (Flutter)

**Project Setup:** (as of 2025-10-25)
- Flutter 3.35.6 initialized with package `com.winereviewer.wine_reviewer_mobile`
- Feature-first architecture: `lib/features/` (auth, review, wine), `lib/core/`, `lib/common/`
- Platform support: Android (primary), iOS, Web, Windows, macOS, Linux (generated)

**Dependencies Configured (10 packages):**
- `flutter_riverpod` - State management + DI
- `go_router` - Navigation with deep linking
- `dio` - HTTP client with interceptors
- `freezed` + `json_serializable` - Immutable models
- `flutter_secure_storage` - Encrypted token storage
- `image_picker` + `cached_network_image` - Image handling
- `google_sign_in` - OAuth authentication
- `build_runner` + `golden_toolkit` - Code gen + testing

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

**CI/CD:**
- GitHub Actions for API (`ci-api.yml`) with path filters
- GitHub Actions for Mobile (`ci-app.yml`) with path filters
- Release workflow (`release.yml`)

---

## 🚧 In Progress

### Mobile App (Flutter)
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

### 3. 🖼️ Implement Image Upload with Pre-signed URLs (Backend)

**Goal:** Allow users to upload wine review photos

**Tasks:**
- Choose storage provider (S3 Free Tier or Supabase Storage)
- Implement pre-signed URL generation endpoint
- Add image upload validation (size, MIME type, expiration)
- Update Review entity to store image URLs
- Add integration tests for image upload flow
- Document new endpoints in OpenAPI/Swagger

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
| **Backend Tests** | 82 (46 unit + 36 integration) |
| **Test Pass Rate** | 100% |
| **Backend Endpoints** | Review CRUD + Auth |
| **Flutter Dependencies** | 10 configured |
| **Flutter Auth Components** | 18 files (models, services, providers, docs) |
| **Flutter Screens** | 4 (splash, login, home, review details) |
| **CI/CD Pipelines** | 3 (API, Mobile, Release) |

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
