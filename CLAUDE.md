1# CLAUDE.md

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
- **Auto-add new commands to JSON config files** - When creating new slash commands or custom commands, automatically add them to the appropriate JSON configuration files (e.g., `.claude/commands.json`, VSCode settings, etc.) without requiring explicit user request. This ensures commands are immediately available for use. *(Added 2025-10-22)*

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

1. **`ROADMAP.md`** (UPDATED EACH SESSION)
   - Update when: End of every development session
   - Include: Move completed items to "Implemented", update priorities, new tasks
   - Use command: `/update-roadmap` or `/finish-session`

2. **`LEARNINGS.md`** (updated when significant)
   - Update when: Significant learnings, technical decisions, or problems solved
   - Include: Session log with Backend/Frontend/Infrastructure subsections
   - Format: Hybrid chronological (sessions with stack subsections)

3. **`CLAUDE.md`** (this file - update rarely)
   - Update when: New architectural patterns, critical directives
   - Don't update: For roadmap (use ROADMAP.md) or learnings (use LEARNINGS.md)
   - **Structure:** 3 parts (General/Backend/Frontend/Infrastructure)

4. **`CODING_STYLE.md`** (update when new conventions)
   - Update when: New code patterns or conventions are identified
   - Include: Examples of correct/incorrect usage, rationale
   - **Structure:** 3 parts (General/Backend/Frontend/Infrastructure)

5. **`README.md`** (update when features added)
   - Update when: New features, endpoints, or major configuration changes
   - Include: Setup instructions, feature overview
   - **Structure:** 3 parts (General/Backend/Frontend)

6. **OpenAPI/Swagger Documentation** (Backend - CRITICAL)
   - **Update immediately** when creating/modifying REST API endpoints
   - How: Add/update `@Tag`, `@Operation`, `@ApiResponses`, `@Parameter` annotations
   - Required for: **Every new endpoint** (no exceptions)
   - Verify: Check Swagger UI at `/swagger-ui.html` after updates

### What Constitutes "Significant Changes"
- ✅ New features implemented (services, controllers, domain logic)
- ✅ New REST endpoints created or existing ones modified
- ✅ Architectural changes (new exception hierarchy, security patterns)
- ✅ New coding conventions identified
- ✅ Important dependency updates
- ❌ Minor bug fixes or refactorings (unless they establish new patterns)

### Update Format
- **ROADMAP.md:** Update at end of each session (use `/update-roadmap` or `/finish-session`)
- **LEARNINGS.md:** Add session entry when significant decisions/learnings occur
- **CLAUDE.md / CODING_STYLE.md:** Update only for new patterns/conventions, include date
- **README.md:** Update when features/setup changes, keep current with implementation

## Custom Slash Commands for Productivity

This project includes custom slash commands to streamline common workflows. Type the command in Claude Code CLI to expand it into a full prompt.

**Workflow Commands:**
- `/start-session [context]` - Load project context (CLAUDE.md, CODING_STYLE.md, ROADMAP.md, README.md) to begin session
- `/finish-session [commit-context]` - Run tests, prompt for doc updates, show git diff, create commit
- `/update-roadmap <what-was-completed>` - Update ROADMAP.md (move completed items, reprioritize next steps)

**Development Commands:**
- `/directive <content>` - Add new directive to CLAUDE.md or CODING_STYLE.md with deduplication check
- `/review-code [path]` - Analyze code quality, test coverage, CODING_STYLE.md adherence
- `/quick-test <ServiceName>` - Run unit + integration tests for specific class

**Backend Commands (Java/Spring):**
- `/build-quiet` - Clean build in quiet mode
- `/verify-quiet` - Run full verification (build + tests) in quiet mode
- `/test-service` - Run tests for specific service class
- `/docker-start` - Start all Docker services (PostgreSQL + API)
- `/docker-stop` - Stop all Docker services
- `/api-doc` - Open API documentation (Swagger UI)

**Command Templates:** Reusable versions available at `C:\repo\claude-command-templates\` for new projects.

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

## Command Execution Guidelines

**CRITICAL RULE:** Always use non-verbose/quiet mode for commands to optimize token usage.

**Examples:**
- Maven: `./mvnw test -q`, `./mvnw verify -q`
- Docker: `docker compose up -d --build --quiet-pull`
- Use `--quiet`, `-q`, or `--silent` flags when available

**Exception:** Use verbose mode only if user explicitly requests it.

## Backend Development Commands

```bash
cd services/api

# Run locally (requires local Postgres or use docker-compose)
./mvnw spring-boot:run

# Build and run tests (quiet mode)
./mvnw verify -q

# Run tests only (quiet mode)
./mvnw test -q

# Run integration tests only (quiet mode)
./mvnw test -q -Dtest=*IT

# Clean build (quiet mode)
./mvnw clean install -q

# Run with specific profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Access OpenAPI docs when running
# http://localhost:8080/swagger-ui.html
```

### Infrastructure (Backend-specific)
```bash
cd infra

# Start all services (Postgres + API) - quiet mode
docker compose up -d --build --quiet-pull

# View logs
docker compose logs -f --tail=50 api

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

**See `CODING_STYLE.md` Part 2 (Backend) for detailed conventions.**

**Key Rules:**
- Constructor injection only (never `@Autowired` on fields)
- `@ConfigurationProperties` over `@Value` (type-safe configuration)
- Maven versions in `<properties>` with placeholders
- Java 21 features encouraged (var, records, pattern matching)
- Method ordering: public → private (invocation flow)
- OpenAPI/Swagger annotations required for all REST endpoints

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

**See `CODING_STYLE.md` Part 3 (Frontend) for detailed conventions.**

**Key Rules:**
- Feature-first folder structure (`lib/features/`, `lib/core/`, `lib/common/`)
- Models with freezed + json_serializable (immutable data classes)
- Riverpod for state management (compile-time safe, testable)
- Widget tests + golden tests for UI components
- Always format with `dart format` before committing

### Critical Directive: Detailed Frontend Explanations (Added 2025-10-22)

**Context:** The user is a backend engineer with little frontend experience and no Flutter knowledge.

**Rule:** For **ALL** frontend modifications and decisions (directories, files, classes, methods, scripts, widgets, state management, etc.), you **MUST** provide detailed explanations covering:

1. **What** - What is being created/modified (file, class, widget, etc.)
2. **Why** - Why this approach/pattern is being used
3. **How** - How it works in Flutter/Dart context
4. **Alternatives** - What other approaches exist and why this one was chosen
5. **Best Practices** - Flutter/Dart conventions being followed

**Examples:**

<example>
**Bad (no explanation):**
"I'll create `lib/features/auth/presentation/screens/login_screen.dart` with a LoginScreen widget."

**Good (with explanation):**
"I'll create `lib/features/auth/presentation/screens/login_screen.dart` with a LoginScreen widget.

**Explanation:**
- **What:** A StatelessWidget that displays the login UI
- **Why:** Flutter uses widgets as the building blocks of UI. StatelessWidget is used because the UI doesn't need internal mutable state (Riverpod will manage state externally)
- **How:** The widget builds a widget tree with Scaffold (material design structure), Column (vertical layout), TextFormField (email input), and ElevatedButton (submit button)
- **Structure:** Following feature-first architecture - `features/auth/presentation/screens/` separates presentation layer from business logic
- **Alternative:** Could use StatefulWidget with setState, but Riverpod provides better state management for complex apps
- **Best Practice:** Keeping widgets small and focused on presentation, delegating business logic to providers"
</example>

<example>
**Bad (no explanation):**
"I'll use Riverpod for state management."

**Good (with explanation):**
"I'll use Riverpod for state management.

**Explanation:**
- **What:** Riverpod is a reactive caching and data-binding framework for Flutter
- **Why:** It's the modern evolution of Provider (Flutter's recommended state management), with compile-time safety and better testing support
- **How:** Uses providers (similar to services) that widgets can watch/read. When provider state changes, widgets automatically rebuild
- **Key Concepts:**
  - `Provider` - Exposes read-only values (like repositories, services)
  - `StateNotifierProvider` - Exposes mutable state (like authentication status, user profile)
  - `FutureProvider` - Handles async operations (like API calls)
  - `ref.watch()` - Listens to provider changes and rebuilds widget
  - `ref.read()` - One-time read without listening (for callbacks)
- **Alternatives:** BLoC (more verbose, steeper learning curve), GetX (less type-safe), setState (doesn't scale well)
- **Best Practice:** Riverpod provides dependency injection, making code testable and decoupled"
</example>

**Never skip these explanations, even for "simple" changes. Always assume zero Flutter knowledge.**

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

# 🎯 Current Status & Roadmap

**See `ROADMAP.md` for detailed implementation status, next steps, and backlog.**

**Quick Summary:**
- ✅ Backend API: Review CRUD + Auth (82 tests, 100% passing)
- ✅ Mobile App: Flutter initialized with 10 dependencies
- 🚧 In Progress: Flutter core infrastructure (network layer, router, main.dart)
- 📍 Next: Flutter authentication flow → Image upload → Comments

---

## 📚 Learnings & Technical Decisions Log

**See `LEARNINGS.md` for detailed session logs, technical decisions, problems encountered, and solutions.**

Sessions are organized chronologically (newest-first) with subsections for Backend (☕), Frontend (📱), and Infrastructure (🐳) work.

**Recent Sessions:**
- 2025-10-25: Flutter Mobile App Initialization
- 2025-10-22 (Part 3): Integration Test Authentication Completed
- 2025-10-22 (Part 2): Integration Test Fixes & Code Quality
- 2025-10-22 (Part 1): Integration Test Authentication Architecture

---

## Useful References

**Core Documentation:**
- **`ROADMAP.md`** - Current implementation status, next steps, backlog (updated each session)
- **`LEARNINGS.md`** - Session logs, technical decisions, problems & solutions (chronological archive)
- **`CODING_STYLE.md`** - Detailed coding conventions (organized by General/Backend/Frontend/Infrastructure)
- **`README.md`** - Project setup instructions and overview

**Efficiency & Best Practices:**
- **`C:\repo\ai\claude-code\tips\EFFICIENCY.md`** - Claude Code efficiency guide (token optimization, workflow strategies, file organization tips) - Optional reference for improving development performance

**Other References:**
- **Prompt Pack:** `prompts/PACK.md` - AI guidance and agent schemas
- **Stack-specific READMEs:** `apps/mobile/`, `services/api/`, `infra/` - Detailed setup per stack
- **OpenAPI/Swagger:** `http://localhost:8080/swagger-ui.html` - Live API documentation (when backend running)
- **GitHub Actions:** `.github/workflows/` - CI/CD pipelines with path filters
- **Docker Compose:** `infra/docker-compose.yml` - Local development environment
- **Command Templates:** `C:\repo\claude-command-templates\` - Reusable slash commands for new projects
