# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

# 📋 PART 1: GENERAL (Cross-stack Guidelines)

> **Use this section for:** Monorepo-wide rules, general architecture, documentation strategy, project overview.
> **Reusable in:** Any project type (fullstack, backend-only, frontend-only).

## Project Overview

**Wine Reviewer** is a mobile-first wine rating application built as a monorepo. The MVP allows users to rate wines (1-5 glasses, not stars), view others' reviews, comment on reviews, and optionally upload photos.

**Core Principles:**
- Quality over speed
- MVP must use 100% free hosting
- Source code in English (Brazilian developer, PT-BR for documentation/comments when beneficial)

## Tech Stack Overview

### Mobile App (`apps/mobile/`)
- **Framework:** Flutter 3.x
- **State Management:** Riverpod (or Bloc if decided/documented)
- **Navigation:** go_router
- **HTTP Client:** dio
- **Models:** freezed + json_serializable
- **Storage:** flutter_secure_storage (for tokens)
- **Image Handling:** image_picker, cached_network_image

### Backend API (`services/api/`)
- **Framework:** Spring Boot 3 with Java 21
- **Database:** PostgreSQL 16
- **Migrations:** Flyway
- **Testing:** Testcontainers for integration tests
- **API Docs:** springdoc-openapi (OpenAPI/Swagger)
- **Authentication:** Google OAuth/OpenID + JWT (short-lived) + refresh tokens

### Infrastructure (`infra/`)
- **Local Dev:** Docker Compose (postgres + api services)
- **CI/CD:** GitHub Actions with path-based triggers
- **Hosting (MVP):** 100% free tier services only
  - Backend: EC2 Free Tier or Render Free or equivalent
  - Database: Supabase Free or local Postgres
  - Storage: S3 Free Tier or Supabase Storage Free (pre-signed URLs)
  - Observability: Grafana Cloud Free or CloudWatch Free Tier

## Monorepo Structure

```
wine-reviewer/
├── apps/mobile/          # Flutter mobile app
├── services/api/         # Spring Boot API
├── infra/                # Docker Compose, deployment configs
├── prompts/              # AI prompt pack and agent schemas
├── .github/workflows/    # CI/CD pipelines with path filters
└── ADRs/                 # Architecture Decision Records (future)
```

## General Code Conventions

### Universal Rules
- All source code in English
- Comments and logs can be in Portuguese (Brazilian developer)
- Quality over speed—take time to design and test properly
- Avoid overengineering; implement MVP features first
- Never commit secrets, API keys, or credentials
- Use free tools and assets with proper licenses only

### Git & CI/CD
- **CI/CD Pipelines:** Path filters to avoid unnecessary runs
  - API Pipeline: Triggers on `services/api/**` or `.github/workflows/ci-api.yml`
  - Mobile Pipeline: Triggers on `apps/mobile/**` or `.github/workflows/ci-app.yml`
  - Release: Manual workflow dispatch with semantic versioning input
- Caching for dependencies (Maven, Flutter pub)
- Run tests before build/deploy steps

### Docker
- Multi-stage builds for smaller images
- Health checks for dependent services
- Named volumes for data persistence

## Architecture & Design (Cross-stack)

### Domain Model (DDD-lite)
Core entities shared across frontend/backend:
- **User** (`app_user`): display_name, email, avatar_url, google_id
- **Wine**: name, winery, country, grape, year, image_url
- **Review**: user_id, wine_id, rating (1-5), notes, photo_url
- **Comment**: review_id, author_id, text

### Authentication Flow (Cross-stack)
1. User signs in with Google (mobile app uses google_sign_in)
2. App sends Google ID token to backend
3. Backend validates with Google OAuth/OpenID, creates/updates user
4. Backend issues short-lived JWT + refresh token
5. App stores tokens securely (flutter_secure_storage)
6. Protected endpoints require valid JWT

### Image Upload Flow (Cross-stack)
1. Client requests pre-signed URL from API
2. API generates pre-signed URL (S3 or Supabase Storage)
3. Client uploads image directly to storage using pre-signed URL (PUT)
4. Client sends final photo URL to API with review data
5. Enforce limits: file size, MIME types, expiration

## Testing Strategy (General)

**CRITICAL RULE: Test-After-Implementation**
- **Always create tests immediately after implementing testable classes**
- Testable classes include: Services, Repositories, Controllers, Utilities, Business Logic
- Non-testable classes (skip tests): Configuration classes, DTOs, Entities (unless complex logic)
- **Workflow:** Implement class → Write tests → Verify tests pass → Move to next task
- Never defer test writing to "later" - tests are part of the implementation

## Documentation Strategy

**CRITICAL RULE: Living Documentation**

Documentation must be updated **at the end of each development session** to reflect the current state of the application.

**CRITICAL RULE: Documentation Organization (3-Part Structure)**

All main documentation files (`CLAUDE.md`, `CODING_STYLE.md`, `README.md`) **must** be organized in 3 parts:
1. **PART 1: GENERAL** - Cross-stack guidelines, project overview, universal rules
2. **PART 2: BACKEND** - Backend-specific (Java/Spring Boot) setup, conventions, testing
3. **PART 3: FRONTEND** - Frontend-specific (Flutter/Dart) setup, conventions, testing

**Benefits:**
- ✅ **Reusable**: Copy only relevant sections for new projects (backend-only, frontend-only, fullstack)
- ✅ **Organized**: No mixing of stack-specific guidelines
- ✅ **Scalable**: Easy to add new sections (PART 4: BFF, PART 5: Infrastructure, etc.)
- ✅ **Clear**: Each section has clear delimiters and usage instructions

### Files to Update After Significant Changes

1. **`CLAUDE.md`** (this file)
   - Update when: New architectural decisions, directives, or patterns are adopted
   - Include: Context about why decisions were made, tradeoffs considered
   - **Structure:** 3 parts (General/Backend/Frontend)

2. **`CODING_STYLE.md`**
   - Update when: New code patterns or conventions are identified
   - Include: Examples of correct/incorrect usage, rationale
   - **Structure:** 3 parts (General/Backend/Frontend)

3. **`README.md`** (main project README)
   - Update when: Application state changes (new features, endpoints, configuration)
   - Include: Current implementation status, setup instructions, API overview
   - Keep accurate: What's implemented vs. what's planned
   - **Structure:** 3 parts (General/Backend/Frontend)

4. **OpenAPI/Swagger Documentation** (Backend)
   - **CRITICAL:** Update **immediately** when creating/modifying REST API endpoints
   - How: Add/update `@Tag`, `@Operation`, `@ApiResponses`, `@Parameter` annotations in controllers
   - Required for: **Every new endpoint** (no exceptions)
   - Verify: Check Swagger UI at `/swagger-ui.html` after updates
   - Document: All possible HTTP status codes (200, 201, 400, 403, 404, 422, 500, 501)

### What Constitutes "Significant Changes"
- ✅ New features implemented (services, controllers, domain logic)
- ✅ New REST endpoints created or existing ones modified
- ✅ Architectural changes (new exception hierarchy, security patterns)
- ✅ New coding conventions identified
- ✅ Important dependency updates
- ❌ Minor bug fixes or refactorings (unless they establish new patterns)

### Update Format
- Always include **date** of update
- Briefly describe **what changed** and **why**
- Update **Current Implementation Status** section with latest features
- Maintain clear distinction between ✅ Implemented, 🚧 In Progress, 📍 Planned
- **Update "Next Steps (Roadmap)" section** - Move completed items to "Implemented", add new next steps based on progress

## Important Constraints

1. **Free Hosting Only:** MVP must run on 100% free services (AWS Free Tier preferred but not required)
2. **No Paid Services:** All tools, libraries, and cloud resources must have free tiers adequate for ~1000 users
3. **Security First:** Short-lived tokens, CORS restrictions, input validation, secure storage
4. **Mobile-First:** Primary target is Android (Galaxy S24 Ultra); iOS support is future work
5. **Scalability Awareness:** Design for moderate scale but avoid premature optimization

## Development Phases

The project follows a phased approach:
- **F0 (Setup):** Monorepo, linters, Docker Compose, basic CI/CD
- **F1 (Domain & API):** Core entities, Review/Comment endpoints, Google login, Flyway, Testcontainers
- **F2 (Flutter MVP):** Login screen, feed, new review with photo, review details with comments
- **F3 (Observability):** Logs, metrics, tracing
- **F4 (CI/CD):** Full pipelines, Docker builds, free-tier deployment
- **F5 (Play Store):** App signing, bundle, store listing, privacy policy
- **F6+ (Evolution):** Follow users, recommendations, i18n, API integrations

---

# ⚙️ PART 2: BACKEND (Java/Spring Boot Specific)

> **Use this section for:** Backend-only projects, microservices, Spring Boot APIs.
> **Copy from here when creating:** Backend microservices, REST APIs, Spring Boot projects.

## Backend Development Commands

```bash
cd services/api

# Run locally (requires local Postgres or use docker-compose)
./mvnw spring-boot:run

# Build and run tests
./mvnw verify

# Run tests only
./mvnw test

# Clean build
./mvnw clean install

# Run with specific profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Access OpenAPI docs when running
# http://localhost:8080/swagger-ui.html
```

### Infrastructure (Backend-specific)
```bash
cd infra

# Start all services (Postgres + API)
docker compose up -d --build

# View logs
docker compose logs -f

# Stop services and remove volumes
docker compose down -v
```

## Backend Architecture

### Package Structure (CRUD-style for current implementation)
```
com.winereviewer.api/
├── application/dto/      # Request/Response DTOs
├── config/               # @Configuration, @ConfigurationProperties
├── controller/           # REST endpoints
├── domain/               # Entities
├── exception/            # Custom exceptions and @ControllerAdvice
├── repository/           # Spring Data JPA repositories
├── security/             # Security filters and utilities (not @Configuration)
├── service/              # Business logic interfaces and implementations
```

### Database Schema (PostgreSQL)
- UUIDs as primary keys
- Timestamptz for audit fields
- Indexes on foreign keys and sorting columns (e.g., `created_at DESC`)
- Cascade delete for comments when review is deleted
- Check constraints (e.g., rating between 1-5)

Key indexes:
- `idx_review_wine_created` on review(wine_id, created_at desc)
- `idx_review_user_created` on review(user_id, created_at desc)
- `idx_comment_review_created` on comment(review_id, created_at desc)

### Observability (Backend)
- Structured JSON logs
- HTTP metrics (p95 latency)
- Distributed tracing with OpenTelemetry
- OTLP export to Grafana Cloud Free or CloudWatch Free

## Backend Code Conventions

### Important Coding Standards
- **Constructor injection only** - Never use `@Autowired` on fields
- **ConfigurationProperties over @Value** - Always use `@ConfigurationProperties` with POJOs for type-safety and testability
- **Maven dependency versions in `<properties>`** - Centralize versions, use placeholders like `${jwt.version}`
- **Large numbers with underscores** - Always: `3_600_000` (not `3600000`)
- **Method ordering: public → private** - Public methods first, then private methods ordered by invocation flow (top-down reading)
- **Java 21 features encouraged** - Use `var`, records, sealed classes, pattern matching, text blocks
- **Lombok selectively** - `@Slf4j` for logging, `@Getter` selectively, avoid `@Data` on domain entities
- **Javadoc required** - Include `@author` and `@date` on public classes
- **ALWAYS add blank line before closing bracket of classes (except records)**

### Exception Handling (Backend)
- Custom domain exceptions extending base `DomainException`
- Clear messages (can be in Portuguese)
- GlobalExceptionHandler with `@ControllerAdvice` for REST error responses
- Never use generic RuntimeExceptions for business logic errors

**Exception Hierarchy:**
```
DomainException (abstract)
├── ResourceNotFoundException (404 NOT FOUND)
├── InvalidRatingException (400 BAD REQUEST)
├── UnauthorizedAccessException (403 FORBIDDEN)
├── BusinessRuleViolationException (422 UNPROCESSABLE ENTITY)
└── InvalidTokenException (401 UNAUTHORIZED)
```

### Logging (Backend)
- Use `@Slf4j` from Lombok
- Log messages can be in Portuguese for context
- Include relevant IDs and context (e.g., `log.info("Review criada com sucesso. ID: {}", review.id())`)

### REST Controllers & OpenAPI Documentation
- **CRITICAL:** ALWAYS add OpenAPI/Swagger annotations when creating new REST endpoints
- Required annotations:
  - `@Tag` - Class level (group endpoints)
  - `@Operation` - Method level (summary + description)
  - `@ApiResponses` - Document ALL possible HTTP status codes
  - `@Parameter` - For path variables and query params
- Workflow: Implement → Document → Test in Swagger UI → Update README.md
- Verify documentation at `/swagger-ui.html` after changes

**HTTP Status Codes to Document:**
- `200 OK` - Successful GET/PUT
- `201 Created` - Successful POST
- `204 No Content` - Successful DELETE
- `400 Bad Request` - Invalid input (validation errors, invalid rating)
- `401 Unauthorized` - Invalid/expired token
- `403 Forbidden` - Ownership violation (trying to modify another user's resource)
- `404 Not Found` - Resource not found (review, wine, user)
- `422 Unprocessable Entity` - Business rule violation (e.g., invalid wine year)
- `500 Internal Server Error` - Unexpected errors
- `501 Not Implemented` - Endpoint planned but not implemented yet

## Backend Testing Strategy

### Pyramid Approach
- **Unit Tests:** Business logic in services, utilities
- **Integration Tests:** API endpoints with Testcontainers (real Postgres)
- Focus on critical paths: authentication, review creation, pagination, comments
- **Naming Convention:** `ClassNameTest` for unit tests, `ClassNameIT` for integration tests
- **Location:** `src/test/java/` mirroring the same package structure as `src/main/java/`

## Backend Entry Points

- **API Main Application:** `services/api/src/main/java/com/winereviewer/api/WineReviewerApiApplication.java`
- **Review Endpoints:** `services/api/src/main/java/com/winereviewer/api/controller/ReviewController.java`
- **Database Migrations:** `services/api/src/main/resources/db/migration/`
- **Configuration:** `services/api/src/main/resources/application.yml`
- **Docker Setup:** `infra/docker-compose.yml`

---

# 📱 PART 3: FRONTEND (Flutter/Dart Specific)

> **Use this section for:** Mobile app development, Flutter projects.
> **Copy from here when creating:** Mobile apps, Flutter projects, cross-platform apps.

## Frontend Development Commands

```bash
cd apps/mobile

# Install dependencies
flutter pub get

# Run app (select Android device)
flutter run

# Lint and analyze
flutter analyze

# Run tests with coverage
flutter test --coverage

# Format code
dart format .

# Generate freezed models
flutter pub run build_runner build --delete-conflicting-outputs
```

## Frontend Architecture

### Folder Structure
- Feature-based folder structure: `lib/features/`, `lib/core/`, `lib/common/widgets/`
- Models and DTOs using freezed + json_serializable
- Consistent formatting with `dart format`
- Widget tests and golden tests for UI components
- Error handling in dio interceptors with retry logic

## Frontend Code Conventions

- Models and DTOs using freezed + json_serializable
- Consistent formatting with `dart format`
- Widget tests and golden tests for UI components
- Error handling in dio interceptors with retry logic

## Frontend Testing Strategy

### Pyramid Approach
- **Unit Tests:** Business logic, state management
- **Widget Tests:** Individual widgets and screens
- **Golden Tests:** Visual regression testing for key UI components
- Focus on: authentication flow, form validation, image upload, feed rendering

## Frontend Entry Points

- **App Main:** `apps/mobile/lib/main.dart`
- **Router:** `apps/mobile/lib/core/router/app_router.dart`
- **Features:** `apps/mobile/lib/features/`

---

# 🐳 PART 4: INFRASTRUCTURE (Docker, Testcontainers, CI/CD)

> **Use this section for:** DevOps, containerization, testing infrastructure, deployment.
> **Copy from here when creating:** Microservices, containerized apps, CI/CD pipelines.

## Infrastructure Development Commands

```bash
# Docker Compose (local development)
cd infra
docker compose up -d --build         # Start all services
docker compose logs -f               # View logs
docker compose down -v               # Stop and remove volumes

# Integration tests with Testcontainers
cd services/api
./mvnw test -Dtest=*IT              # Run integration tests only
./mvnw verify                        # Run all tests (unit + integration)
./mvnw test                          # Run unit tests only
```

## Docker & Testcontainers

### Docker Compose Setup
- **Services:** PostgreSQL 16 + API
- **Health Checks:** Configured for all dependent services
- **Volumes:** Named volumes for data persistence
- **Networks:** Bridge network for service communication

**Key Files:**
- `infra/docker-compose.yml` - Main compose configuration
- `services/api/Dockerfile` - Multi-stage build for API

### Testcontainers Integration Tests

**Purpose:** Run integration tests against real PostgreSQL database in isolated Docker containers.

**Configuration:**
- **Base Class:** `AbstractIntegrationTest` - Shared PostgreSQL container
- **Profile:** `application-integration.yml` - Test-specific configuration
- **Container:** `postgres:16-alpine` with reuse enabled for speed
- **Isolation:** `@Transactional` on test class for auto-rollback

**Test Structure:**
```
src/test/java/com/winereviewer/api/integration/
├── AbstractIntegrationTest.java       # Base class with Testcontainers setup
├── ReviewControllerIT.java            # Review endpoints (23 tests)
└── AuthControllerIT.java              # Auth endpoints (14 tests)
```

**Why Testcontainers:**
- ✅ **Production Parity:** Tests run against real PostgreSQL (same as prod)
- ✅ **Isolation:** Each test run gets fresh database state
- ✅ **Constraints:** Database constraints (CHECK, FK, CASCADE) work exactly like production
- ✅ **CI/CD Ready:** No manual database setup required

**Important Notes:**
- **Requires Docker:** Testcontainers needs Docker daemon running
- **Shared Container:** Static container reused across all test classes for performance
- **Flyway Enabled:** Migrations run automatically before tests
- **Mock External APIs:** GoogleTokenValidator mocked (no real Google API calls)

### Integration Test Naming Convention
- **Suffix:** `*IT.java` (e.g., `ReviewControllerIT`, `AuthControllerIT`)
- **Location:** Same package as production code, under `src/test/java/`
- **Profile:** Use `@ActiveProfiles("integration")` annotation

## CI/CD Pipeline

### GitHub Actions Workflows
- **API Pipeline:** `.github/workflows/ci-api.yml` - Triggers on `services/api/**` changes
- **Mobile Pipeline:** `.github/workflows/ci-app.yml` - Triggers on `apps/mobile/**` changes
- **Release:** `.github/workflows/release.yml` - Manual workflow with semantic versioning

**Path Filters:** Pipelines only run when relevant files change (avoids unnecessary builds)

**Caching Strategy:**
- Maven dependencies cached for faster API builds
- Flutter pub cache for faster mobile builds

## Infrastructure Entry Points

- **Docker Compose:** `infra/docker-compose.yml`
- **Integration Test Base:** `services/api/src/test/java/com/winereviewer/api/integration/AbstractIntegrationTest.java`
- **Integration Test Profile:** `services/api/src/test/resources/application-integration.yml`
- **CI/CD Workflows:** `.github/workflows/`

---

# 🎯 Current Implementation Status & Roadmap

## ✅ Implemented (as of 2025-10-22)

### Backend API (Spring Boot)
- Complete Review CRUD endpoints (`ReviewController`, `ReviewService`)
- Comment endpoints (`CommentController`)
- JWT authentication structure (`JwtUtil`, `JwtProperties`) - updated to JJWT 0.12.x
- **Google OAuth authentication** (`AuthService`, `GoogleTokenValidator`)
- **Domain exception hierarchy** (`DomainException` base, `ResourceNotFoundException`, `InvalidRatingException`, `UnauthorizedAccessException`, `BusinessRuleViolationException`, `InvalidTokenException`)
- Global exception handling (`GlobalExceptionHandler`) with domain exception support
- Database entities: User, Wine, Review, Comment (with domain exception validation)
- Flyway migrations setup
- OpenAPI/Swagger documentation
- Application configuration with profiles (dev/prod)
- Docker support (Dockerfile + docker-compose)
- **Complete unit test suite** (46 tests, 100% passing: ReviewControllerTest, ReviewServiceTest, DomainExceptionTest, AuthServiceTest, GoogleTokenValidatorTest)
- **✨ NEW: Integration tests with Testcontainers** (37 tests: ReviewControllerIT, AuthControllerIT)

### Infrastructure
- Docker Compose with PostgreSQL 16 and API service
- Health checks and dependencies configured
- **✨ NEW: Testcontainers integration test infrastructure** (`AbstractIntegrationTest`, `application-integration.yml`)

### CI/CD
- GitHub Actions for API (`ci-api.yml`) with path filters
- GitHub Actions for Mobile (`ci-app.yml`) with path filters
- Release workflow (`release.yml`)

### Testing Coverage
- **Unit Tests:** 46 tests covering services, controllers, exceptions, validators
- **Integration Tests:** 37 tests covering API endpoints with real PostgreSQL database
- **Total:** 83 tests (all passing)
- **Coverage:** Review CRUD (100%), Auth (100%), Database constraints (100%), Exception scenarios (100%)

## 🚧 In Progress / TODO
- Mobile app (Flutter) - not yet initialized
- Image upload with pre-signed URLs
- Observability (metrics, tracing)

## 🎯 Next Steps (Roadmap)

**IMPORTANT:** This section should be updated at the **end of each development session** to track what's next.

**Last updated:** 2025-10-22

### Immediate Next Steps (Priority Order)

1. **✅ COMPLETED: Add Integration Tests with Testcontainers** (2025-10-22)
   - ✅ Setup Testcontainers for PostgreSQL
   - ✅ Create integration tests for Review endpoints (23 tests)
   - ✅ Create integration tests for Auth endpoints (14 tests)
   - ✅ Test exception handling in real scenarios
   - ✅ Test database constraints and validations
   - ✅ Add PART 4: INFRASTRUCTURE to all documentation files

2. **Implement Image Upload with Pre-signed URLs**
   - Choose storage provider (S3 Free Tier or Supabase Storage)
   - Implement pre-signed URL generation endpoint
   - Add image upload validation (size, MIME type)
   - Update Review entity to store image URLs
   - Add integration tests for image upload flow

3. **Implement Comment System**
   - Complete CRUD endpoints for comments
   - Add OpenAPI documentation
   - Create unit and integration tests for comment endpoints

4. **Start Flutter Mobile App (F2 Phase)**
   - Initialize Flutter project structure
   - Setup Riverpod for state management
   - Implement Google Sign-In flow
   - Create login screen

### Future Backlog (Post-MVP)

- **Observability:** Metrics, distributed tracing, structured logging
- **User Follow System:** Follow/unfollow users
- **Wine Recommendations:** Recommendation algorithm
- **Internationalization:** i18n support for multiple languages
- **iOS Support:** Expand Flutter app to iOS

### Blocked/Waiting

- None currently

---

## Useful References

- **Coding Style Guide:** See `CODING_STYLE.md` for detailed conventions (organized by General/Backend/Frontend)
- **Main Prompt Pack:** See `prompts/PACK.md` for comprehensive AI guidance and agent schemas
- **README Files:** Each subdirectory (apps/mobile, services/api, infra) has specific setup instructions
- **GitHub Actions:** Workflows use path filters for monorepo efficiency
- **Docker Compose:** Start here for local development (`infra/docker-compose.yml`)
