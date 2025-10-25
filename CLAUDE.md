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

## Custom Slash Commands for Productivity

This project includes custom slash commands to streamline common workflows:

### `/directive <content>`
Add a new coding directive to CLAUDE.md automatically.
- **Usage:** `/directive "Always use constructor injection over field injection"`
- **What it does:** Adds the directive to the appropriate section (General/Backend/Frontend)
- **Benefit:** Quick way to capture new learnings without manually editing documentation

### `/start-session [context]`
Initialize a new development session with standard context loading.
- **Usage:** `/start-session "Fixing authentication bugs"`
- **What it does:** Loads CLAUDE.md, CODING_STYLE.md, README.md, reviews roadmap, checks git status
- **Benefit:** Standardized session startup - no need to manually type the same prompt every time

### `/finish-session [commit-context]`
Complete a development session with tests, docs update, and commit.
- **Usage:** `/finish-session "Implemented comment endpoints"`
- **What it does:** Runs tests in quiet mode → Prompts for doc updates → Shows git diff → Creates commit
- **Benefit:** Ensures you never forget to test, document, or commit changes

### `/review-code [path-or-scope]`
Analyze code quality and generate improvement report.
- **Usage:** `/review-code services/api/service/`
- **What it does:** Checks adherence to CODING_STYLE.md, test coverage, documentation, security, and performance
- **Benefit:** Automated code review before commits or PRs, identifies issues early

### `/update-roadmap <what-was-completed>`
Update the "Next Steps (Roadmap)" section in CLAUDE.md.
- **Usage:** `/update-roadmap "Integration tests with Testcontainers completed"`
- **What it does:** Moves completed items to "Implemented", updates priorities, adds new tasks
- **Benefit:** Keeps roadmap accurate and up-to-date without manual editing

### `/quick-test <ServiceName>`
Run tests for a specific service or controller class.
- **Usage:** `/quick-test ReviewService`
- **What it does:** Runs both unit tests (*Test) and integration tests (*IT) for the specified class
- **Benefit:** Fast feedback during development, no need to run entire test suite

**How to use:** Simply type the command in Claude Code CLI (e.g., `/start-session`). The command will expand into a full prompt automatically.

### Reusing Commands in Other Projects

All custom commands in this project are also available in a reusable template repository:

**Location:** `C:\repo\claude-command-templates\`

This template repository contains:
- ✅ **Generic commands** - Work with any project (directive, start-session, finish-session, review-code)
- ✅ **Java/Spring Boot commands** - Build, test, Docker, API docs, etc.
- 🚧 **Flutter commands** - Coming soon
- 🚧 **Node.js commands** - Coming soon

**To reuse in new projects:**

```bash
# Option 1: Manual copy (recommended)
cp C:/repo/claude-command-templates/generic/* /path/to/new-project/.claude/commands/
cp C:/repo/claude-command-templates/java-spring/* /path/to/new-project/.claude/commands/

# Option 2: Bootstrap script (Windows PowerShell)
cd C:\repo\claude-command-templates
.\bootstrap.ps1 -StackType java-spring -ProjectDir C:\path\to\new-project

# Option 3: Bootstrap script (Linux/Mac)
cd /repo/claude-command-templates
./bootstrap.sh java-spring /path/to/new-project
```

**Important:** Each command includes an **"Adaptation Guide"** section with instructions for adapting to your project's structure (paths, ports, tools, etc.). Always review and adapt commands after copying.

**See also:** `C:\repo\claude-command-templates\README.md` for full documentation.

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

**CRITICAL RULE: Always Use Non-Verbose Mode (Token Efficiency)**

To optimize token usage during Claude Code sessions, **ALWAYS** execute commands in non-verbose/quiet mode unless explicitly requested otherwise by the user.

### Maven Commands
- ✅ **Correct:** `./mvnw test -q` (quiet mode)
- ❌ **Incorrect:** `./mvnw test` (verbose, wastes tokens)
- ✅ **Correct:** `./mvnw verify -q` (quiet mode)
- ❌ **Incorrect:** `./mvnw verify --batch-mode` (still verbose)

### Docker Commands
- ✅ **Correct:** `docker compose up -d --build --quiet-pull` (quiet mode)
- ❌ **Incorrect:** `docker compose up -d --build` (verbose image pulls)
- ✅ **Correct:** `docker compose down` (already quiet)

### Other Commands
- ✅ **Correct:** Use `--quiet`, `-q`, `--silent`, or equivalent flags
- ❌ **Incorrect:** Running commands without quiet flags

**Exception:** Only use verbose mode if the user explicitly asks for it (e.g., "run tests with verbose output").

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
- **Always use imports** - Never use full class names (e.g., use `@ActiveProfiles` not `@org.springframework.test.context.ActiveProfiles`)
- **Use .getFirst() over .get(0)** - For Java 21+ collections, prefer `list.getFirst()` instead of `list.get(0)`
- **Show git diff before commit** - ALWAYS show consolidated `git diff` output for user review before committing changes
- **Auto-update directives** - When new directives are added, automatically update CLAUDE.md and other relevant documentation files

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

### Important Coding Standards

- **Models and DTOs using freezed + json_serializable** - Immutable data classes with code generation
- **Consistent formatting with `dart format`** - Always format before committing
- **Widget tests and golden tests for UI components** - Visual regression testing
- **Error handling in dio interceptors with retry logic** - Resilient HTTP client

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
- **Testcontainers integration test infrastructure** (`AbstractIntegrationTest`, `application-integration.yml`)

### CI/CD
- GitHub Actions for API (`ci-api.yml`) with path filters
- GitHub Actions for Mobile (`ci-app.yml`) with path filters
- Release workflow (`release.yml`)

### Testing Coverage (Backend)
- **Unit Tests:** 46 tests covering services, controllers, exceptions, validators
- **Integration Tests:** 36 tests covering API endpoints with real PostgreSQL database
- **Total:** 82 tests (all passing)
- **Coverage:** Review CRUD (100%), Auth (100%), Database constraints (100%), Exception scenarios (100%)

### Mobile App (Flutter) - **✨ NEW** (as of 2025-10-25)
- **Project initialized:** Flutter 3.35.6 with package `com.winereviewer.wine_reviewer_mobile`
- **Dependencies configured (10 packages):**
  - `flutter_riverpod` - State management + DI
  - `go_router` - Navigation with deep linking
  - `dio` - HTTP client with interceptors
  - `freezed` + `json_serializable` - Immutable models
  - `flutter_secure_storage` - Encrypted token storage
  - `image_picker` + `cached_network_image` - Image handling
  - `google_sign_in` - OAuth authentication
  - `build_runner` + `golden_toolkit` - Code gen + testing
- **Feature-first architecture:** `lib/features/` (auth, review, wine), `lib/core/`, `lib/common/`
- **Core configuration files:**
  - `app_colors.dart` - Color palette (wine theme)
  - `app_theme.dart` - Material Design 3 theme
  - `api_constants.dart` - API URLs, endpoints, timeouts
- **Documentation:** `DEPENDENCIES_EXPLAINED.md`, `SETUP_INSTRUCTIONS.md`
- **Platform support:** Android (primary), iOS, Web, Windows, macOS, Linux (generated)

## 🚧 In Progress / TODO
- **Mobile app (Flutter):** Core network layer (Dio client, auth interceptor) - in progress
- Image upload with pre-signed URLs (backend)
- Observability (metrics, tracing)

## 🎯 Next Steps (Roadmap)

**IMPORTANT:** This section should be updated at the **end of each development session** to track what's next.

**Last updated:** 2025-10-25 (Session 4 - Flutter Mobile App Initialization - In Progress)

### Immediate Next Steps (Priority Order)

1. **🚧 IN PROGRESS: Complete Flutter Core Infrastructure** (Started 2025-10-25)
   - ✅ Initialize Flutter 3.35.6 project with feature-first architecture
   - ✅ Configure 10 essential dependencies (Riverpod, dio, go_router, freezed, etc.)
   - ✅ Create core configuration files (app_colors, app_theme, api_constants)
   - ⏳ **Next:** Create Dio HTTP client with auth interceptor
   - ⏳ Setup go_router navigation structure (splash, login, home, review details)
   - ⏳ Create initial main.dart with ProviderScope and MaterialApp
   - ⏳ Test app compiles and runs on Android emulator

2. **Implement Flutter Authentication Flow (F2 Phase)**
   - Create auth feature structure (data/domain/presentation/providers)
   - Implement Google Sign-In integration
   - Create login screen UI with Material Design 3
   - Setup flutter_secure_storage for JWT token persistence
   - Implement auto-login (check token on app startup)

3. **Implement Image Upload with Pre-signed URLs (Backend)**
   - Choose storage provider (S3 Free Tier or Supabase Storage)
   - Implement pre-signed URL generation endpoint
   - Add image upload validation (size, MIME type)
   - Update Review entity to store image URLs
   - Add integration tests for image upload flow

4. **Implement Comment System (Backend)**
   - Complete CRUD endpoints for comments
   - Add OpenAPI documentation
   - Create unit and integration tests for comment endpoints

### Future Backlog (Post-MVP)

- **Observability:** Metrics, distributed tracing, structured logging
- **User Follow System:** Follow/unfollow users
- **Wine Recommendations:** Recommendation algorithm
- **Internationalization:** i18n support for multiple languages
- **iOS Support:** Expand Flutter app to iOS

### Blocked/Waiting

- None currently

---

## 📚 Learnings & Technical Decisions Log

### Session 2025-10-22: Integration Test Authentication Architecture

**Problem:** Integration tests with Testcontainers were failing due to JWT authentication being enforced but no valid tokens being provided.

**Initial Approach Considered (REJECTED):**
- ❌ Adding `X-User-Id` header to controller endpoints
- **Why rejected:** Security anti-pattern - allows any client to impersonate users; mixes test code with production code

**Final Solution (ACCEPTED):**
1. **TestSecurityConfig**: Disables security only for integration tests (via `@Profile("integration")`)
2. **ReviewController**: Modified to extract `userId` from Spring Security `Authentication.getName()` (production-ready)
3. **AbstractIntegrationTest**: Added `authenticated(UUID userId)` helper that creates `UsernamePasswordAuthenticationToken`
4. **Tests**: Use `.with(authenticated(testUser.getId()))` in `mockMvc.perform()` calls

**Key Insights:**
- Never compromise production code security for testing convenience
- Spring Security Testing provides proper mechanisms (`RequestPostProcessor`) for authentication in tests
- Separation of concerns: Test infrastructure should not leak into production code
- `@Profile` annotations are powerful for test-specific configurations

**Formatting Standard Established:**
- Lambda closing parentheses should be on the same line as the last method call
- Example: `.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());` (not `)` on separate line)
- Documented in `CODING_STYLE.md` section "Formatação de Lambdas e Method Chaining"

**IntelliJ Working Mode:**
- ⚠️ Running Claude Code inside IntelliJ terminal causes conflicts with auto-formatters
- **Solution for next session:** Run Claude Code in separate terminal to avoid file modification conflicts

### Session 2025-10-22 (Part 2): Integration Test Fixes & Code Quality Improvements

**Problems Fixed:**
1. **Cascade Delete Tests Failing** - Hibernate cache wasn't seeing database CASCADE DELETE
2. **AuthController Test Mismatch** - Expected 500 but got 404 (correct behavior)
3. **Optional Fields JSON Test** - Expected `.isEmpty()` but Jackson omits null fields
4. **Unit Test Authentication** - ReviewControllerTest missing proper authentication setup

**Solutions:**
1. **Cascade Delete:** Added `entityManager.clear()` after `flush()` to force Hibernate to reload from database
2. **AuthController:** Renamed test and changed expectation from 500 → 404 (user not found is 404, not 500)
3. **Optional Fields:** Changed `.isEmpty()` → `.doesNotExist()` (Jackson default behavior)
4. **Unit Test Auth:** Added `.with(user(userId.toString()))` + import `SecurityMockMvcRequestPostProcessors.user`

**New Directives Established:**
- **Always use imports over full class names** - Improves readability and follows Java conventions
- **Prefer `.getFirst()` over `.get(0)`** - Java 21+ idiomatic code
- **Show git diff before commit** - User must review all changes before committing
- **Auto-update directives** - New directives automatically added to CLAUDE.md

**Test Results:**
- ✅ **82 tests passing** (46 unit + 36 integration)
- ✅ All authentication tests working correctly
- ✅ All cascade delete tests passing
- ✅ All JSON serialization tests passing

**Token Efficiency Improvements:**
- Added "Command Execution Guidelines" section to CLAUDE.md
- Updated all commands to use quiet mode (`-q`, `--quiet-pull`)
- Documented rationale: Optimize token usage in Claude Code sessions

### Session 2025-10-22 (Part 3): Integration Test Authentication Completed

**Context:**
- Previous session left integration tests in non-functional state
- Expected to manually add `.with(authenticated())` to 21 test methods
- Expected cascade delete and AuthController failures

**Discoveries:**
1. **Authentication Already Implemented** - All 21 `mockMvc.perform()` calls already had `.with(authenticated(testUser.getId()))` from previous session
2. **Tests Already Passing** - No cascade delete or AuthController failures found
3. **Previous Session Incomplete Commit** - Work was committed in non-functional state but was actually functional

**Verification:**
- ✅ Integration tests: 36 tests passing (13 AuthController + 23 ReviewController)
- ✅ Unit tests: 46 tests passing
- ✅ **Total: 82 tests passing** (100% success rate)

**Key Insight:**
- Running Claude Code in IntelliJ terminal (from previous session) may have caused confusion about actual file state
- Current session (running Claude Code in separate terminal) revealed code was already functional
- Importance of verifying test state before assuming failures

**Working Mode Recommendation Confirmed:**
- ✅ **Always run Claude Code in separate terminal** (not inside IntelliJ terminal)
- ✅ Avoids auto-formatter conflicts and state confusion
- ✅ Clearer view of actual file state

---

## Useful References

- **Coding Style Guide:** See `CODING_STYLE.md` for detailed conventions (organized by General/Backend/Frontend)
- **Main Prompt Pack:** See `prompts/PACK.md` for comprehensive AI guidance and agent schemas
- **README Files:** Each subdirectory (apps/mobile, services/api, infra) has specific setup instructions
- **GitHub Actions:** Workflows use path filters for monorepo efficiency
- **Docker Compose:** Start here for local development (`infra/docker-compose.yml`)
- claude.md preciso ir trabalhar. Nas proximas eu nao vou abrir o claude no terminal do intellij, mas a parte. 
registre os aprendizados e commite, mesmo estando em estado nao-funcional. Na proxima sessao nos fazemos os ajustes. 
adicione as correcoes pendentes a area de proximos passos do claude.md
