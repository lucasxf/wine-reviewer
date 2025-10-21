# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Wine Reviewer** is a mobile-first wine rating application built as a monorepo. The MVP allows users to rate wines (1-5 glasses, not stars), view others' reviews, comment on reviews, and optionally upload photos.

**Core Principles:**
- Quality over speed
- MVP must use 100% free hosting
- Source code in English (Brazilian developer, PT-BR for documentation/comments when beneficial)

## Tech Stack

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

## Development Commands

### Mobile App
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
```

### Backend API
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

### Infrastructure
```bash
cd infra

# Start all services (Postgres + API)
docker compose up -d --build

# View logs
docker compose logs -f

# Stop services and remove volumes
docker compose down -v
```

### CI/CD
- **API Pipeline:** Triggers on changes to `services/api/**` or `.github/workflows/ci-api.yml`
- **Mobile Pipeline:** Triggers on changes to `apps/mobile/**` or `.github/workflows/ci-app.yml`
- **Release:** Manual workflow dispatch with semantic versioning input

## Architecture & Design

### Monorepo Structure
```
wine-reviewer/
├── apps/mobile/          # Flutter mobile app
├── services/api/         # Spring Boot API
├── infra/                # Docker Compose, deployment configs
├── prompts/              # AI prompt pack and agent schemas
├── .github/workflows/    # CI/CD pipelines with path filters
└── ADRs/                 # Architecture Decision Records (future)
```

### Domain Model (DDD-lite)
Core entities:
- **User** (`app_user`): display_name, email, avatar_url
- **Wine**: name, winery, country, grape, year, image_url
- **Review**: user_id, wine_id, rating (1-5), notes, photo_url
- **Comment**: review_id, author_id, text

### Database Schema
PostgreSQL with:
- UUIDs as primary keys
- Timestamptz for audit fields
- Indexes on foreign keys and sorting columns (e.g., `created_at DESC`)
- Cascade delete for comments when review is deleted
- Check constraints (e.g., rating between 1-5)

Key indexes:
- `idx_review_wine_created` on review(wine_id, created_at desc)
- `idx_review_user_created` on review(user_id, created_at desc)
- `idx_comment_review_created` on comment(review_id, created_at desc)

### Authentication Flow
1. User signs in with Google (mobile app uses google_sign_in)
2. App sends Google ID token to backend
3. Backend validates with Google OAuth/OpenID, creates/updates user
4. Backend issues short-lived JWT + refresh token
5. App stores tokens securely (flutter_secure_storage)
6. Protected endpoints require valid JWT

### Image Upload Flow
1. Client requests pre-signed URL from API
2. API generates pre-signed URL (S3 or Supabase Storage)
3. Client uploads image directly to storage using pre-signed URL (PUT)
4. Client sends final photo URL to API with review data
5. Enforce limits: file size, MIME types, expiration

### Observability
- Structured JSON logs
- HTTP metrics (p95 latency)
- Distributed tracing with OpenTelemetry
- OTLP export to Grafana Cloud Free or CloudWatch Free

## Development Phases

The project follows a phased approach:
- **F0 (Setup):** Monorepo, linters, Docker Compose, basic CI/CD
- **F1 (Domain & API):** Core entities, Review/Comment endpoints, Google login, Flyway, Testcontainers
- **F2 (Flutter MVP):** Login screen, feed, new review with photo, review details with comments
- **F3 (Observability):** Logs, metrics, tracing
- **F4 (CI/CD):** Full pipelines, Docker builds, free-tier deployment
- **F5 (Play Store):** App signing, bundle, store listing, privacy policy
- **F6+ (Evolution):** Follow users, recommendations, i18n, API integrations

## Code Conventions

### General
- All source code in English
- Comments and logs can be in Portuguese (Brazilian developer)
- Quality over speed—take time to design and test properly
- Avoid overengineering; implement MVP features first
- Never commit secrets, API keys, or credentials
- Use free tools and assets with proper licenses only
- **ALWAYS add blank line before closing bracket of classes (except records)**

### Java/Spring Boot

**Package Structure (CRUD-style for current implementation):**
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

**Important Coding Standards:**
- **Constructor injection only** - Never use `@Autowired` on fields
- **ConfigurationProperties over @Value** - Always use `@ConfigurationProperties` with POJOs for type-safety and testability
- **Maven dependency versions in `<properties>`** - Centralize versions, use placeholders like `${jwt.version}`
- **Large numbers with underscores** - Always: `3_600_000` (not `3600000`)
- **Method ordering: public → private** - Public methods first, then private methods ordered by invocation flow (top-down reading)
- **Java 21 features encouraged** - Use `var`, records, sealed classes, pattern matching, text blocks
- **Lombok selectively** - `@Slf4j` for logging, `@Getter` selectively, avoid `@Data` on domain entities
- **Javadoc required** - Include `@author` and `@date` on public classes

**Exception Handling:**
- Custom domain exceptions extending base `DomainException`
- Clear messages (can be in Portuguese)
- GlobalExceptionHandler with `@ControllerAdvice` for REST error responses
- Never use generic RuntimeExceptions for business logic errors

**Logging:**
- Use `@Slf4j` from Lombok
- Log messages can be in Portuguese for context
- Include relevant IDs and context (e.g., `log.info("Review criada com sucesso. ID: {}", review.id())`)

**REST Controllers & OpenAPI Documentation:**
- **CRITICAL:** ALWAYS add OpenAPI/Swagger annotations when creating new REST endpoints
- Required annotations:
  - `@Tag` - Class level (group endpoints)
  - `@Operation` - Method level (summary + description)
  - `@ApiResponses` - Document ALL possible HTTP status codes
  - `@Parameter` - For path variables and query params
- Workflow: Implement → Document → Test in Swagger UI → Update README.md
- Verify documentation at `/swagger-ui.html` after changes

### Flutter/Dart
- Feature-based folder structure: `lib/features/`, `lib/core/`, `lib/common/widgets/`
- Models and DTOs using freezed + json_serializable
- Consistent formatting with `dart format`
- Widget tests and golden tests for UI components
- Error handling in dio interceptors with retry logic

### Docker
- Multi-stage builds for smaller images
- Health checks for dependent services
- Named volumes for data persistence

### CI/CD
- Path filters to avoid unnecessary pipeline runs
- Caching for dependencies (Maven, Flutter pub)
- Run tests before build/deploy steps

## Testing Strategy

**CRITICAL RULE: Test-After-Implementation**
- **Always create tests immediately after implementing testable classes**
- Testable classes include: Services, Repositories, Controllers, Utilities, Business Logic
- Non-testable classes (skip tests): Configuration classes, DTOs, Entities (unless complex logic)
- **Workflow:** Implement class → Write tests → Verify tests pass → Move to next task
- Never defer test writing to "later" - tests are part of the implementation

### Backend (Pyramid Approach)
- **Unit Tests:** Business logic in services, utilities
- **Integration Tests:** API endpoints with Testcontainers (real Postgres)
- Focus on critical paths: authentication, review creation, pagination, comments
- **Naming Convention:** `ClassNameTest` for unit tests, `ClassNameIT` for integration tests
- **Location:** `src/test/java/` mirroring the same package structure as `src/main/java/`

### Mobile (Pyramid Approach)
- **Unit Tests:** Business logic, state management
- **Widget Tests:** Individual widgets and screens
- **Golden Tests:** Visual regression testing for key UI components
- Focus on: authentication flow, form validation, image upload, feed rendering

## Documentation Strategy

**CRITICAL RULE: Living Documentation**

Documentation must be updated **at the end of each development session** to reflect the current state of the application.

### Files to Update After Significant Changes

1. **`CLAUDE.md`** (this file)
   - Update when: New architectural decisions, directives, or patterns are adopted
   - Include: Context about why decisions were made, tradeoffs considered

2. **`CODING_STYLE.md`**
   - Update when: New code patterns or conventions are identified
   - Include: Examples of correct/incorrect usage, rationale

3. **`README.md`** (main project README)
   - Update when: Application state changes (new features, endpoints, configuration)
   - Include: Current implementation status, setup instructions, API overview
   - Keep accurate: What's implemented vs. what's planned

4. **OpenAPI/Swagger Documentation**
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

## 🎯 Next Steps (Roadmap)

**IMPORTANT:** This section should be updated at the **end of each development session** to track what's next.

**Last updated:** 2025-10-21

### Immediate Next Steps (Priority Order)

1. **Implement missing Review endpoints**
   - `GET /reviews` - List reviews with pagination and filters (wineId, userId)
   - `DELETE /reviews/{id}` - Delete review with ownership validation
   - Add OpenAPI documentation for both endpoints
   - Create tests for new endpoints

2. **Implement Google OAuth integration**
   - Backend: OAuth token validation with Google
   - Create/update user on successful authentication
   - Issue JWT tokens after Google login
   - Test authentication flow end-to-end

3. **Add Integration Tests with Testcontainers**
   - Setup Testcontainers for PostgreSQL
   - Create integration tests for Review endpoints
   - Test exception handling in real scenarios
   - Test database constraints and validations

4. **Implement Image Upload with Pre-signed URLs**
   - Choose storage provider (S3 Free Tier or Supabase Storage)
   - Implement pre-signed URL generation endpoint
   - Add image upload validation (size, MIME type)
   - Update Review entity to store image URLs

### Future Backlog (Post-MVP)

- **Observability:** Metrics, distributed tracing, structured logging
- **Comment System:** Full CRUD for comments on reviews
- **User Follow System:** Follow/unfollow users
- **Wine Recommendations:** Recommendation algorithm
- **Internationalization:** i18n support for multiple languages
- **Mobile App:** Flutter implementation (F2 phase)

### Blocked/Waiting

- None currently

---

## Current Implementation Status

### ✅ Implemented (as of 2025-10-21)
- **Backend API (Spring Boot):**
  - Complete Review CRUD endpoints (`ReviewController`, `ReviewService`)
  - Comment endpoints (`CommentController`)
  - JWT authentication structure (`JwtUtil`, `JwtProperties`) - updated to JJWT 0.12.x
  - **Domain exception hierarchy** (`DomainException` base, `ResourceNotFoundException`, `InvalidRatingException`, `UnauthorizedAccessException`, `BusinessRuleViolationException`)
  - Global exception handling (`GlobalExceptionHandler`) with domain exception support
  - Database entities: User, Wine, Review, Comment (with domain exception validation)
  - Flyway migrations setup
  - OpenAPI/Swagger documentation
  - Application configuration with profiles (dev/prod)
  - Docker support (Dockerfile + docker-compose)
  - **Complete test suite** (27 tests: ReviewControllerTest, ReviewServiceTest, DomainExceptionTest)

- **Infrastructure:**
  - Docker Compose with PostgreSQL 16 and API service
  - Health checks and dependencies configured

- **CI/CD:**
  - GitHub Actions for API (`ci-api.yml`) with path filters
  - GitHub Actions for Mobile (`ci-app.yml`) with path filters
  - Release workflow (`release.yml`)

### 🚧 In Progress / TODO
- Mobile app (Flutter) - not yet initialized
- Google OAuth integration (backend structure ready, needs implementation)
- Image upload with pre-signed URLs
- Integration tests with Testcontainers
- Observability (metrics, tracing)

### 📍 Key Entry Points for Development
- **API Main Application:** `services/api/src/main/java/com/winereviewer/api/WineReviewerApiApplication.java`
- **Review Endpoints:** `services/api/src/main/java/com/winereviewer/api/controller/ReviewController.java`
- **Database Migrations:** `services/api/src/main/resources/db/migration/`
- **Configuration:** `services/api/src/main/resources/application.yml`
- **Docker Setup:** `infra/docker-compose.yml`

## Useful References

- **Coding Style Guide:** See `CODING_STYLE.md` for detailed Java/Spring Boot conventions
- **Main Prompt Pack:** See `prompts/PACK.md` for comprehensive AI guidance and agent schemas
- **README Files:** Each subdirectory (apps/mobile, services/api, infra) has specific setup instructions
- **GitHub Actions:** Workflows use path filters for monorepo efficiency
- **Docker Compose:** Start here for local development (`infra/docker-compose.yml`)
