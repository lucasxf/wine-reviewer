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
- **Framework:** Flutter 3.x | **State Management:** Riverpod
- **Navigation:** go_router | **HTTP Client:** dio
- **Models:** freezed + json_serializable | **Storage:** flutter_secure_storage

### Backend API (`services/api/`)
- **Framework:** Spring Boot 3 with Java 21 | **Database:** PostgreSQL 16
- **Testing:** Testcontainers | **API Docs:** springdoc-openapi (Swagger)
- **Auth:** Google OAuth/OpenID + JWT + refresh tokens

### Infrastructure (`infra/`)
- **Local Dev:** Docker Compose | **CI/CD:** GitHub Actions with path filters
- **Hosting (MVP):** 100% free tier (EC2 Free/Render Free, Supabase/S3, Grafana Cloud Free)

## Quick Commands

**Most Common Commands:**
```bash
# Backend API
./mvnw verify -q                    # Build + tests (quiet)
./mvnw spring-boot:run              # Run API locally
docker compose up -d --build        # Start PostgreSQL + API

# Mobile App
flutter run                         # Run app
flutter test --coverage             # Run tests with coverage
flutter pub run build_runner build --delete-conflicting-outputs

# Infrastructure
docker compose logs -f --tail=50 api     # View API logs
docker compose down -v                    # Stop and remove volumes
```

**For comprehensive command references:** See stack-specific COMMANDS.md files:
- [services/api/COMMANDS.md](services/api/COMMANDS.md) - Backend commands
- [apps/mobile/COMMANDS.md](apps/mobile/COMMANDS.md) - Frontend commands
- [infra/COMMANDS.md](infra/COMMANDS.md) - Infrastructure commands

## Architecture & Design

### Monorepo Structure
```
wine-reviewer/
├── apps/mobile/          # Flutter mobile app
├── services/api/         # Spring Boot API
├── infra/                # Docker Compose, deployment configs
├── .claude/              # Agents, commands, metrics
└── .github/workflows/    # CI/CD pipelines with path filters
```

### Domain Model (DDD-lite)
- **User** (`app_user`): display_name, email, avatar_url, google_id
- **Wine**: name, winery, country, grape, year, image_url
- **Review**: user_id, wine_id, rating (1-5), notes, photo_url
- **Comment**: review_id, author_id, text

### Authentication Flow
1. User signs in with Google → App sends ID token to backend
2. Backend validates with Google OAuth, creates/updates user
3. Backend issues JWT + refresh token → App stores securely
4. Protected endpoints require valid JWT

### Image Upload Flow
1. Client requests pre-signed URL from API
2. API generates pre-signed URL (S3/Supabase Storage)
3. Client uploads image directly using pre-signed URL
4. Client sends photo URL to API with review data

## General Code Conventions

### Universal Rules
- All source code in English (comments/logs can be Portuguese)
- Quality over speed—design and test properly
- Avoid overengineering; implement MVP features first
- Never commit secrets, API keys, or credentials
- **Auto-add new commands to JSON config files** - When creating slash commands, add to `.claude/commands.json` automatically

### Git & CI/CD
- **Git Flow:** Feature branches → `develop` → `main` (ALL PRs target `develop` by default)
- **Branch Separation:** Create automation branches from `develop`, not from feature branches
- **CI/CD Pipelines:** Path filters (API: `services/api/**`, Mobile: `apps/mobile/**`)
- **Caching:** Maven dependencies, Flutter pub cache

### Docker
- Multi-stage builds for smaller images
- Health checks for dependent services
- Named volumes for data persistence

## Testing Strategy (TDD + BDD)

**CRITICAL RULE:** ALL new features MUST follow the TDD Red-Green-Refactor cycle (Red: write failing test, Green: make it pass, Refactor: clean up code).

**Key Requirements:**
- **Test naming:** `should[ExpectedBehavior]When[StateUnderTest]` (e.g., `shouldCreateReviewWhenValidDataProvided`)
- **Structure:** Given/When/Then (BDD)
- **Coverage goals:** 100% critical paths, 90%+ business logic, 80%+ controllers/UI, 70%+ overall
- **Testable classes:** Services, repositories, controllers, utilities, widgets, providers
- **Non-testable classes:** Configuration classes, simple DTOs, entities (unless complex domain logic)

**Stack-specific tools:**
- **Backend:** JUnit 5 + Mockito + AssertJ + Testcontainers (real PostgreSQL)
- **Frontend:** flutter_test + mocktail + WidgetTester + golden_toolkit
- **Infrastructure:** Testcontainers + Docker health checks + CI/CD validation

**For comprehensive testing guidelines:** See [TESTING.md](TESTING.md) for TDD/BDD details, code smells, anti-patterns, and examples.

## Documentation Strategy

**CRITICAL RULE: Living Documentation**

Documentation must be updated **at the end of each development session** to reflect the current state of the application.

**Documentation Organization (4-Part Structure):**
1. **PART 1: GENERAL** - Cross-stack guidelines, project overview, universal rules
2. **PART 2: BACKEND** - Backend-specific (Java/Spring Boot) setup, conventions, testing
3. **PART 3: FRONTEND** - Frontend-specific (Flutter/Dart) setup, conventions, testing
4. **PART 4: INFRASTRUCTURE** - Infrastructure-specific (Docker, Testcontainers, CI/CD)

**Files to Update After Significant Changes:**

1. **`ROADMAP.md`** (UPDATED EACH SESSION)
   - Update when: End of every development session
   - Use command: `/update-roadmap` or `/finish-session`

2. **`LEARNINGS.md`** (updated when significant)
   - Update when: Significant learnings, technical decisions, or problems solved

3. **`CLAUDE.md`** (this file - update rarely)
   - Update when: New architectural patterns, critical directives
   - Don't update: For roadmap or learnings

4. **`CODING_STYLE files`** (update when new conventions)
   - Update when: New code patterns or conventions identified
   - Files: CODING_STYLE_GENERAL.md + stack-specific (BACKEND/FRONTEND/INFRASTRUCTURE)

5. **`README.md`** (update when features added)
   - Update when: New features, endpoints, or major configuration changes

6. **OpenAPI/Swagger Documentation** (Backend - CRITICAL)
   - **Update immediately** when creating/modifying REST API endpoints
   - Required annotations: `@Tag`, `@Operation`, `@ApiResponses`, `@Parameter`

## Custom Slash Commands & Agents

**Slash Commands (Workflow automation):**
- `/start-session [--stack=backend|frontend|infra|docs]` - Load stack-specific context (40-54% token reduction)
- `/finish-session` - Run tests, update docs, commit (auto-offers PR for feature branches)
- `/create-pr` - Create PR with gh CLI, auto-trigger automation-sentinel
- `/update-roadmap <what-completed>` - Update ROADMAP.md
- `/directive <content>` - Add directive with deduplication
- `/review-code [path]` - Analyze code quality, test coverage

**Custom Agents (9 specialized agents):**
1. **automation-sentinel** - Meta-agent for automation health, metrics analysis
2. **backend-code-reviewer** - Java/Spring Boot code review
3. **cross-project-architect** - Pattern extraction, templates, new projects
4. **flutter-implementation-coach** - Flutter coding, Riverpod, Dio, debugging
5. **frontend-ux-specialist** - UI/UX design, screen layouts
6. **learning-tutor** - Teaching concepts, structured learning
7. **pulse** - Metrics collection (Haiku, auto-triggered before sentinel)
8. **session-optimizer** - Token efficiency, session planning
9. **tech-writer** - Documentation, ADRs, Javadoc, OpenAPI

**For detailed agent guide:** See [.claude/agents-readme.md](.claude/agents-readme.md)

**Automation Metrics System:**
- **`.claude/metrics/usage-stats.toml`** - Git-tracked metrics (TOML format)
- **Delta tracking** - Incremental updates (50-80% token savings)
- **Two-step workflow** - `pulse` (data collection) → `automation-sentinel` (analysis)
- **For metrics details:** See [.claude/METRICS.md](.claude/METRICS.md)

## Important Constraints

1. **Free Hosting Only:** MVP must run on 100% free services
2. **No Paid Services:** All tools must have free tiers adequate for ~1000 users
3. **Security First:** Short-lived tokens, CORS restrictions, input validation, secure storage
4. **Mobile-First:** Primary target is Android (Galaxy S24 Ultra); iOS support is future work
5. **Scalability Awareness:** Design for moderate scale but avoid premature optimization

---

# ⚙️ PART 2: BACKEND (Java/Spring Boot Specific)

> **Use this section for:** Backend-only projects, microservices, Spring Boot APIs.
> **Copy from here when creating:** Backend microservices, REST APIs, Spring Boot projects.

## Backend Architecture

### Package Structure (CRUD-style)
```
com.winereviewer.api/
├── application/dto/      # Request/Response DTOs
├── config/               # @Configuration, @ConfigurationProperties
├── controller/           # REST endpoints
├── domain/               # Entities
├── exception/            # Custom exceptions + @ControllerAdvice
├── repository/           # Spring Data JPA
├── security/             # Security filters (not @Configuration)
├── service/              # Business logic
```

### Database Schema (PostgreSQL)
- UUIDs as primary keys | Timestamptz for audit fields
- Indexes on foreign keys and sorting columns
- Cascade delete (comments → reviews) | Check constraints (rating 1-5)

**Key indexes:**
- `idx_review_wine_created` on review(wine_id, created_at desc)
- `idx_review_user_created` on review(user_id, created_at desc)
- `idx_comment_review_created` on comment(review_id, created_at desc)

## Backend Code Conventions

**See `services/api/CODING_STYLE_BACKEND.md` for detailed conventions.**

**Key Rules:**
- Constructor injection only (never `@Autowired` on fields)
- `@ConfigurationProperties` over `@Value`
- Java 21 features encouraged (var, records, pattern matching)
- Method ordering: public → private (invocation flow)
- OpenAPI/Swagger annotations required for all REST endpoints

### Exception Handling
- Custom domain exceptions extending `DomainException`
- GlobalExceptionHandler with `@ControllerAdvice`
- Clear messages (can be in Portuguese)

**Exception Hierarchy:**
- `ResourceNotFoundException` (404 NOT FOUND)
- `InvalidRatingException` (400 BAD REQUEST)
- `UnauthorizedAccessException` (403 FORBIDDEN)
- `BusinessRuleViolationException` (422 UNPROCESSABLE ENTITY)
- `InvalidTokenException` (401 UNAUTHORIZED)

### REST Controllers & OpenAPI

**Required annotations:**
- `@Tag` - Class level (group endpoints)
- `@Operation` - Method level (summary + description)
- `@ApiResponses` - Document ALL possible HTTP status codes
- `@Parameter` - For path variables and query params

**Workflow:** Implement → Document → Test in Swagger UI → Update README.md

## Backend Testing

**Pyramid Approach:**
- **Unit Tests:** Services, utilities (JUnit 5 + Mockito + AssertJ)
- **Integration Tests:** API endpoints with Testcontainers (real PostgreSQL)
- **Naming:** `ClassNameTest` (unit), `ClassNameIT` (integration)

**Testcontainers Pattern:**
- **Base class:** `AbstractIntegrationTest` (shared PostgreSQL container)
- **Profile:** `application-integration.yml`
- **Isolation:** `@Transactional` for automatic rollback
- **Mock externals:** GoogleTokenValidator, S3Client

**Why Testcontainers:**
- ✅ Production parity (real PostgreSQL)
- ✅ Database constraints work exactly like production
- ✅ CI/CD ready (no manual setup)

## Backend Entry Points

- **API Main:** `services/api/src/main/java/com/winereviewer/api/WineReviewerApiApplication.java`
- **Review Endpoints:** `services/api/src/main/java/com/winereviewer/api/controller/ReviewController.java`
- **Migrations:** `services/api/src/main/resources/db/migration/`
- **Config:** `services/api/src/main/resources/application.yml`

---

# 📱 PART 3: FRONTEND (Flutter/Dart Specific)

> **Use this section for:** Mobile app development, Flutter projects.
> **Copy from here when creating:** Mobile apps, Flutter projects, cross-platform apps.

## Frontend Architecture

### Folder Structure
- Feature-first: `lib/features/`, `lib/core/`, `lib/common/widgets/`
- Models: freezed + json_serializable (immutable data classes)
- Formatting: `dart format` before committing
- Error handling: dio interceptors with retry logic

## Frontend Code Conventions

**See `apps/mobile/CODING_STYLE_FRONTEND.md` for detailed conventions.**

**Key Rules:**
- Feature-first folder structure
- Models with freezed + json_serializable
- Riverpod for state management (compile-time safe, testable)
- Widget tests + golden tests for UI components

### Critical Directive: Detailed Frontend Explanations (Added 2025-10-22)

**Context:** User is a backend engineer with no Flutter knowledge.

**Rule:** For **ALL** frontend changes, provide detailed explanations:
1. **What** - What is being created/modified
2. **Why** - Why this approach is being used
3. **How** - How it works in Flutter/Dart context
4. **Alternatives** - What other approaches exist and why this one was chosen
5. **Best Practices** - Flutter/Dart conventions being followed

**Example:**
```
"I'll use Riverpod for state management.

**Explanation:**
- **What:** Riverpod is a reactive caching/data-binding framework
- **Why:** Modern evolution of Provider with compile-time safety
- **How:** Uses providers (like services) that widgets watch/read. State changes trigger rebuilds.
- **Key Concepts:**
  - Provider: Read-only values (repositories, services)
  - StateNotifierProvider: Mutable state (auth status, user profile)
  - FutureProvider: Async operations (API calls)
  - ref.watch(): Listens to changes (rebuilds widget)
  - ref.read(): One-time read (for callbacks)
- **Alternatives:** BLoC (more verbose), GetX (less type-safe), setState (doesn't scale)
- **Best Practice:** Dependency injection, testable, decoupled code"
```

**Never skip explanations, even for "simple" changes. Always assume zero Flutter knowledge.**

## Frontend Testing

**Pyramid Approach:**
- **Unit Tests:** Business logic, state management
- **Widget Tests:** Individual widgets and screens
- **Golden Tests:** Visual regression for key UI components
- **Focus:** Authentication flow, form validation, image upload, feed rendering

## Frontend Entry Points

- **App Main:** `apps/mobile/lib/main.dart`
- **Router:** `apps/mobile/lib/core/router/app_router.dart`
- **Features:** `apps/mobile/lib/features/`

---

# 🐳 PART 4: INFRASTRUCTURE (Docker, Testcontainers, CI/CD)

> **Use this section for:** DevOps, containerization, testing infrastructure, deployment.
> **Copy from here when creating:** Microservices, containerized apps, CI/CD pipelines.

## Docker & Testcontainers

### Docker Compose Setup
- **Services:** PostgreSQL 16 + API
- **Health Checks:** Configured for all services
- **Volumes:** Named volumes for data persistence

**Key Files:**
- `infra/docker-compose.yml` - Service definitions
- `services/api/Dockerfile` - Multi-stage build for API

### Testcontainers Integration Tests

**Purpose:** Run integration tests against real PostgreSQL in isolated Docker containers.

**Configuration:**
- **Base Class:** `AbstractIntegrationTest` (shared PostgreSQL container)
- **Profile:** `application-integration.yml`
- **Container:** `postgres:16-alpine` with reuse enabled
- **Isolation:** `@Transactional` for auto-rollback

**Test Structure:**
```
src/test/java/com/winereviewer/api/integration/
├── AbstractIntegrationTest.java       # Testcontainers setup
├── ReviewControllerIT.java            # Review endpoints (23 tests)
└── AuthControllerIT.java              # Auth endpoints (14 tests)
```

**Important Notes:**
- Requires Docker daemon running
- Static container reused across test classes (performance)
- Flyway migrations run automatically
- Mock external APIs (GoogleTokenValidator, S3Client)

## CI/CD Pipeline

### GitHub Actions Workflows
- **API Pipeline:** `.github/workflows/ci-api.yml` (triggers on `services/api/**`)
- **Mobile Pipeline:** `.github/workflows/ci-app.yml` (triggers on `apps/mobile/**`)
- **Release:** `.github/workflows/release.yml` (manual, semantic versioning)

**Caching:** Maven dependencies + Flutter pub cache

## Infrastructure Entry Points

- **Docker Compose:** `infra/docker-compose.yml`
- **Integration Test Base:** `services/api/src/test/java/com/winereviewer/api/integration/AbstractIntegrationTest.java`
- **CI/CD Workflows:** `.github/workflows/`

---

# 🔗 Quick Links

**For comprehensive documentation, see [README.md](README.md) - Useful References section.**

**Key Resources:**
- **[ROADMAP.md](ROADMAP.md)** - Current status, next steps, backlog
- **[TESTING.md](TESTING.md)** - TDD/BDD strategy, conventions, examples
- **[.claude/METRICS.md](.claude/METRICS.md)** - Automation metrics system
- **[.claude/agents/README.md](.claude/agents/README.md)** - Custom agents guide
- **CODING_STYLE files** - Detailed conventions (GENERAL + BACKEND + FRONTEND + INFRASTRUCTURE)
- **COMMANDS.md files** - Command references per stack (api, mobile, infra)
- **[LEARNINGS.md](LEARNINGS.md)** - Session logs, technical decisions
