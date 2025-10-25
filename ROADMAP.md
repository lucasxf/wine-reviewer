# Wine Reviewer - Project Roadmap

**Last updated:** 2025-10-25 (Session 4 - Flutter Mobile App Initialization - In Progress)

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

**Documentation:**
- `DEPENDENCIES_EXPLAINED.md` - Detailed package explanations
- `SETUP_INSTRUCTIONS.md` - Development environment setup

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
- Core network layer (Dio client, auth interceptor)
- Router configuration (go_router with splash, login, home, review details)
- Initial main.dart with ProviderScope and MaterialApp
- Android emulator testing

---

## 🎯 Next Steps (Priority Order)

### 1. 🚧 PRIORITY 1: Complete Flutter Core Infrastructure (Started 2025-10-25)

**Status:** In Progress

**Completed:**
- ✅ Initialize Flutter 3.35.6 project with feature-first architecture
- ✅ Configure 10 essential dependencies (Riverpod, dio, go_router, freezed, etc.)
- ✅ Create core configuration files (app_colors, app_theme, api_constants)

**Next Actions:**
- ⏳ Create Dio HTTP client with auth interceptor
- ⏳ Setup go_router navigation structure (splash, login, home, review details)
- ⏳ Create initial main.dart with ProviderScope and MaterialApp
- ⏳ Test app compiles and runs on Android emulator

---

### 2. 📱 Implement Flutter Authentication Flow (F2 Phase)

**Goal:** Complete user authentication with Google Sign-In

**Tasks:**
- Create auth feature structure (data/domain/presentation/providers)
- Implement Google Sign-In integration
- Create login screen UI with Material Design 3
- Setup flutter_secure_storage for JWT token persistence
- Implement auto-login (check token on app startup)
- Test authentication flow end-to-end

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
| **Flutter Screens** | 0 (in progress) |
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
