# 🍷 Wine Reviewer

> A mobile-first wine rating application with Google authentication, photo uploads, and community reviews.

[![API CI/CD](https://github.com/lucasxf/wine-reviewer/actions/workflows/ci-api.yml/badge.svg)](https://github.com/lucasxf/wine-reviewer/actions/workflows/ci-api.yml)
[![Mobile CI/CD](https://github.com/lucasxf/wine-reviewer/actions/workflows/ci-app.yml/badge.svg)](https://github.com/lucasxf/wine-reviewer/actions/workflows/ci-app.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

---

# 📋 PART 1: GENERAL (Project Overview)

> **This section:** Cross-stack information, project structure, features, tech stack overview.

## 🎯 Overview

**Wine Reviewer** is a monorepo project that enables wine enthusiasts to:
- Rate wines from 1-5 glasses (not stars!)
- Share tasting notes and photos
- Browse reviews from other users
- Comment on reviews and engage with the community

**Core Principles:**
- ✨ Quality over speed
- 🆓 100% free hosting for MVP
- 🌍 Source code in English (PT-BR for docs/comments when beneficial)

## ✨ Features

### Current (v0.1.0 - Updated 2025-11-25)
**Backend API:**
- ✅ Complete Review CRUD API endpoints
- ✅ **Complete Comment System** (POST, PUT, GET, DELETE endpoints with full CRUD operations)
  - Create comments on reviews
  - Update own comments (ownership validation)
  - List comments by user or review (with pagination)
  - Delete own comments (ownership validation)
  - Database cascade delete (comments deleted when review is deleted)
- ✅ JWT authentication structure (JJWT 0.12.6)
- ✅ **Google OAuth authentication** (AuthService, GoogleTokenValidator)
- ✅ **Domain exception hierarchy** with proper HTTP status mapping
- ✅ PostgreSQL with Flyway migrations
- ✅ OpenAPI/Swagger documentation
- ✅ Docker Compose setup for local development
- ✅ **Comprehensive test suite** (135 total tests: 71 unit + 64 integration, 100% passing)
- ✅ **Integration tests with Testcontainers** (Real PostgreSQL testing with production parity)
  - Shared container pattern for performance (static @Container with reuse)
  - Proper authentication helpers (authenticated(userId) method)
  - Mock external APIs (GoogleTokenValidator, S3Client)
  - Full CRUD coverage, validation, authorization, and edge cases

**Mobile App (Flutter):**
- ✅ **Flutter 3.35.6** with feature-first architecture
- ✅ **Dio HTTP client** with auth interceptor (automatic JWT injection)
- ✅ **go_router navigation** (4 screens: splash, login, home, review details)
- ✅ **✨ NEW: Complete AuthService implementation**
  - Google Sign-In integration
  - Backend API communication (POST /api/auth/google)
  - JWT token persistence (flutter_secure_storage with hardware encryption)
  - Riverpod state management (AuthState, AuthStateNotifier)
  - Auto-login support (checkAuthStatus)
  - Comprehensive documentation (storage README, best practices)

### Exception Handling System
- ✅ `DomainException` abstract base class with `getHttpStatus()` method
- ✅ `ResourceNotFoundException` (404) - Resources not found by ID
- ✅ `InvalidRatingException` (400) - Rating validation (1-5 glasses)
- ✅ `UnauthorizedAccessException` (403) - Ownership violations
- ✅ `BusinessRuleViolationException` (422) - Business rule violations
- ✅ `InvalidTokenException` (401) - Invalid/expired authentication tokens
- ✅ `GlobalExceptionHandler` with unified domain exception handling

### In Progress
- 🚧 Flutter UI integration with AuthService (connect providers to screens)

### Planned
- 📍 Observability (metrics, distributed tracing)
- 📍 User follow system
- 📍 Wine recommendations
- 📍 Internationalization (i18n)

## 🛠 Tech Stack Overview

### Mobile App (`apps/mobile/`)
- **Framework:** Flutter 3.35.6
- **State Management:** Riverpod
- **Navigation:** go_router
- **HTTP Client:** dio
- **Models:** freezed + json_serializable
- **Storage:** flutter_secure_storage

### Backend API (`services/api/`)
- **Framework:** Spring Boot 3.3.11
- **Language:** Java 21
- **Database:** PostgreSQL 16
- **Migrations:** Flyway
- **Testing:** JUnit 5, Testcontainers
- **API Docs:** springdoc-openapi (Swagger)
- **Auth:** Google OAuth/OpenID + JWT

### Infrastructure
- **Local Dev:** Docker Compose
- **CI/CD:** GitHub Actions with path-based triggers
- **Hosting:** 100% free tier services (AWS Free Tier, Supabase, Render)

## 📦 Project Structure

```
wine-reviewer/
├── apps/
│   └── mobile/                 # Flutter mobile app
├── services/
│   └── api/                    # Spring Boot REST API
│       ├── src/main/java/com/winereviewer/api/
│       │   ├── application/dto/       # Request/Response DTOs
│       │   ├── config/                # Configuration classes
│       │   ├── controller/            # REST endpoints
│       │   ├── domain/                # JPA Entities
│       │   ├── exception/             # Exception handling
│       │   ├── repository/            # Spring Data repositories
│       │   ├── security/              # JWT utilities
│       │   └── service/               # Business logic
│       └── src/main/resources/
│           ├── application.yml        # App configuration
│           └── db/migration/          # Flyway SQL scripts
├── infra/
│   ├── docker-compose.yml      # Local environment
│   └── deployment/             # Deployment configs
├── prompts/                    # AI prompt pack
├── ADRs/                       # Architecture Decision Records
└── .github/workflows/          # CI/CD pipelines
```

## 🚀 Getting Started (Quick Start)

### Prerequisites

- **Java 21** (JDK)
- **Maven 3.8+** (or use `./mvnw`)
- **Flutter 3.x** (for mobile app)
- **Docker** and **Docker Compose**
- **Git**

### Quick Start (Backend + Database)

1. **Clone the repository:**
   ```bash
   git clone https://github.com/lucasxf/wine-reviewer.git
   cd wine-reviewer
   ```

2. **Start services with Docker Compose:**
   ```bash
   cd infra
   docker compose up -d --build
   ```

3. **Access the API:**
   - Base URL: `http://localhost:8080`
   - Swagger UI: `http://localhost:8080/swagger-ui.html`
   - API Docs: `http://localhost:8080/v3/api-docs`

4. **Check database:**
   - Host: `localhost:5432`
   - Database: `winedb`
   - User: `winereviewer`
   - Password: `winepass`

### Docker Commands (General)

```bash
cd infra

# Start services
docker compose up -d

# View logs
docker compose logs -f api

# Stop services
docker compose down

# Stop and remove volumes (clean slate)
docker compose down -v

# Rebuild after code changes
docker compose up -d --build api
```

## 🚀 Deployment (General)

### MVP Hosting (100% Free Tier)

- **Backend:** AWS EC2 Free Tier / Render Free
- **Database:** Supabase Free / AWS RDS Free Tier
- **Storage:** AWS S3 Free Tier / Supabase Storage
- **Observability:** Grafana Cloud Free / CloudWatch Free

### CI/CD Pipelines

GitHub Actions workflows with path-based triggers:

- **API Pipeline** (`.github/workflows/ci-api.yml`):
  - Triggers on `services/api/**` changes
  - Runs tests, builds JAR, creates Docker image

- **Mobile Pipeline** (`.github/workflows/ci-app.yml`):
  - Triggers on `apps/mobile/**` changes
  - Runs tests, builds APK/AAB

- **Release Workflow** (`.github/workflows/release.yml`):
  - Manual workflow dispatch
  - Semantic versioning (major.minor.patch)

## 📚 Documentation

- **`CLAUDE.md`** - Comprehensive project guide for AI assistants (4-part structure: General/Backend/Frontend/Infrastructure)
- **CODING_STYLE files** - Coding standards and conventions (split by stack):
  - `CODING_STYLE_GENERAL.md` - Universal conventions
  - `services/api/CODING_STYLE_BACKEND.md` - Java/Spring Boot
  - `apps/mobile/CODING_STYLE_FRONTEND.md` - Flutter/Dart
  - `infra/CODING_STYLE_INFRASTRUCTURE.md` - Docker/CI/CD
- **`.claude/METRICS.md`** - Automation metrics system (usage tracking, ROI analysis, delta updates)
- **`services/api/README.md`** - Backend setup and API details
- **`apps/mobile/README.md`** - Mobile app setup and architecture
- **`ADRs/`** - Architecture Decision Records (1 ADR: automation-sentinel meta-agent)

---

# ⚙️ PART 2: BACKEND (Java/Spring Boot)

> **This section:** Backend-specific setup, development, testing, and API documentation.

## Backend Development Setup

### Manual Backend Setup

```bash
cd services/api

# Run with Maven wrapper
./mvnw spring-boot:run

# Or run with specific profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Backend Development Commands

```bash
cd services/api

# Run all tests
./mvnw test

# Run with coverage
./mvnw verify

# Clean build
./mvnw clean install

# Format code (if using Spotless)
./mvnw spotless:apply
```

## API Endpoints

### Authentication
- `POST /api/auth/google` - Authenticate with Google OAuth (201 Created)
  - Request: `{ "googleIdToken": "..." }`
  - Response: `{ "token": "...", "userId": "...", "email": "...", "displayName": "..." }`

### Reviews
- `POST /api/reviews` - Create review (201 Created)
- `GET /api/reviews` - List reviews with pagination (200 OK)
  - Query params: `wineId`, `userId`, `page`, `size`, `sort`
- `GET /api/reviews/{id}` - Get review details (200 OK, 404 Not Found)
- `PUT /api/reviews/{id}` - Update review (200 OK, 404 Not Found, 403 Forbidden)
- `DELETE /api/reviews/{id}` - Delete review (204 No Content, 404 Not Found, 403 Forbidden)

### Comments
- `POST /comments` - Create comment (201 Created, 400 Bad Request, 403 Forbidden, 404 Not Found)
  - Request: `{ "reviewId": "...", "text": "..." }`
  - Response: `{ "id": "...", "text": "...", "author": {...}, "createdAt": "...", "updatedAt": "..." }`
- `PUT /comments` - Update comment (200 OK, 400 Bad Request, 403 Forbidden, 404 Not Found)
  - Request: `{ "commentId": "...", "text": "..." }`
  - Response: `{ "id": "...", "text": "...", "author": {...}, "createdAt": "...", "updatedAt": "..." }`
- `GET /comments` - List comments by authenticated user (200 OK, 403 Forbidden, 404 Not Found)
  - Query params: `page`, `size`, `sort` (default: createdAt DESC)
  - Response: Paginated list of comments
- `GET /comments/{reviewId}` - List comments by review (200 OK, 403 Forbidden, 404 Not Found)
  - Query params: `page`, `size`, `sort` (default: createdAt ASC)
  - Response: Paginated list of comments
- `DELETE /comments/{commentId}` - Delete comment (204 No Content, 401 Unauthorized, 403 Forbidden, 404 Not Found)
  - Ownership check: Only the comment author can delete their own comment
  - Response: No content (empty body)

### Health & Monitoring
- `GET /health` - Health check (200 OK)
- `GET /actuator/health` - Spring Actuator health endpoint

## HTTP Status Codes

- `200 OK` - Successful GET/PUT
- `201 Created` - Successful POST
- `204 No Content` - Successful DELETE
- `400 Bad Request` - Invalid input (validation errors, invalid rating)
- `401 Unauthorized` - Invalid/expired authentication token
- `403 Forbidden` - Ownership violation (trying to modify another user's resource)
- `404 Not Found` - Resource not found (review, wine, user)
- `422 Unprocessable Entity` - Business rule violation (e.g., invalid wine year)
- `500 Internal Server Error` - Unexpected errors
- `501 Not Implemented` - Endpoint planned but not implemented yet

## Backend Testing

### Testing Strategy (Test Pyramid)

- **Unit Tests:** Business logic, services, utilities
- **Integration Tests:** API endpoints with Testcontainers (real PostgreSQL)
- **Focus Areas:** Authentication, review CRUD, pagination, comments

### Running Tests

```bash
cd services/api

# Run all tests
./mvnw test

# Run only unit tests
./mvnw test -Dtest="*Test"

# Run only integration tests
./mvnw test -Dtest="*IT"

# Run specific test class
./mvnw test -Dtest=ReviewServiceTest

# Run with coverage
./mvnw verify
```

### Current Test Coverage

For comprehensive TDD/BDD guidelines, see [TESTING.md](TESTING.md).

- **135 tests, 100% passing** (71 unit + 64 integration)
- **Unit Tests (71 tests):**
  - `ReviewControllerTest` - 4 tests (REST endpoint validation)
  - `ReviewServiceTest` - 20 tests (business logic)
  - `CommentServiceTest` - 13 tests (comment CRUD operations)
  - `AuthServiceTest` - 5 tests (Google OAuth authentication)
  - `GoogleTokenValidatorTest` - 5 tests (token validation)
  - `S3ServiceTest` - 12 tests (file upload with S3)
  - `DomainExceptionTest` - 12 tests (exception hierarchy)
- **Integration Tests (64 tests):**
  - `ReviewControllerIT` - 23 tests (Review CRUD, pagination, validation)
  - `CommentControllerIT` - 19 tests (Comment CRUD, ownership, cascade delete, DELETE endpoint)
  - `AuthControllerIT` - 13 tests (Google OAuth, user creation)
  - `FileUploadControllerIT` - 9 tests (Pre-signed URLs, file validation)

## Backend Code Conventions

**Java/Spring Boot:**
- Constructor injection only (no `@Autowired` on fields)
- Use `@ConfigurationProperties` instead of `@Value`
- Centralize dependency versions in Maven `<properties>`
- Use underscores for large numbers: `3_600_000`
- Method order: public methods first, then private (top-down)
- Include `@author` and `@date` in Javadoc
- Always add OpenAPI/Swagger annotations to REST endpoints

See `services/api/CODING_STYLE_BACKEND.md` for detailed backend conventions.

---

# 📱 PART 3: FRONTEND (Flutter/Dart)

> **This section:** Mobile app setup, development, testing, and conventions.

## Mobile App Setup

```bash
cd apps/mobile

# Install dependencies
flutter pub get

# Run app (select device)
flutter run

# Build for Android
flutter build apk

# Build for iOS (macOS only)
flutter build ios
```

## Frontend Development Commands

```bash
cd apps/mobile

# Analyze code
flutter analyze

# Format code
dart format .

# Run tests
flutter test

# Run tests with coverage
flutter test --coverage

# Generate freezed models
flutter pub run build_runner build --delete-conflicting-outputs
```

## Frontend Testing

### Testing Strategy

- **Unit Tests:** Business logic, state management
- **Widget Tests:** Individual widgets and screens
- **Golden Tests:** Visual regression testing

### Running Tests

```bash
# All tests
flutter test

# Unit tests only
flutter test test/unit/

# Widget tests only
flutter test test/widget/

# With coverage
flutter test --coverage
```

## Frontend Code Conventions

**Flutter/Dart:**
- Feature-based folder structure (`lib/features/`, `lib/core/`, `lib/common/`)
- Use freezed for immutable models
- Use Riverpod for state management
- Follow Effective Dart style guide
- Widget tests for all screens
- Use `const` constructors for performance

See `apps/mobile/CODING_STYLE_FRONTEND.md` for detailed frontend conventions.

---

# 🐳 PART 4: INFRASTRUCTURE (Docker, Testing, CI/CD)

> **This section:** DevOps setup, integration testing with Testcontainers, deployment strategies.

## Integration Tests with Testcontainers

### Running Integration Tests

**Requirements:**
- Docker daemon running (Docker Desktop on Windows/macOS, Docker Engine on Linux)
- PostgreSQL container will be automatically created by Testcontainers

```bash
cd services/api

# Run integration tests only (requires Docker)
./mvnw test -Dtest=*IT

# Run all tests (unit + integration)
./mvnw verify

# Run unit tests only (no Docker required)
./mvnw test
```

### Integration Test Coverage

**Current Tests:** 37 integration tests covering critical API endpoints

**ReviewControllerIT (23 tests):**
- ✅ POST /reviews - Create review with validation
- ✅ GET /reviews/{id} - Get review by ID
- ✅ GET /reviews - List with pagination, sorting, filtering
- ✅ PUT /reviews/{id} - Update review
- ✅ DELETE /reviews/{id} - Delete review
- ✅ Database constraints (rating 1-5, cascade delete, foreign keys)
- ✅ Exception handling (404, 403, 400, 422)

**AuthControllerIT (14 tests):**
- ✅ POST /auth/google - Google OAuth authentication
- ✅ POST /auth/login - Simple login (MVP testing endpoint)
- ✅ User creation and update logic
- ✅ Token validation and error handling

### Testcontainers Architecture

**Base class:** `AbstractIntegrationTest`
- Shared PostgreSQL container (postgres:16-alpine)
- Automatic database migration with Flyway
- `@Transactional` test isolation (auto-rollback)
- MockMvc for HTTP request/response testing
- GoogleTokenValidator mocked (no external API calls)

**Key Files:**
- `src/test/java/com/winereviewer/api/integration/AbstractIntegrationTest.java`
- `src/test/java/com/winereviewer/api/integration/ReviewControllerIT.java`
- `src/test/java/com/winereviewer/api/integration/CommentControllerIT.java`
- `src/test/java/com/winereviewer/api/integration/AuthControllerIT.java`
- `src/test/java/com/winereviewer/api/integration/FileUploadControllerIT.java`
- `src/test/resources/application-integration.yml`

## Docker Setup

### Local Development with Docker Compose

```bash
cd infra

# Start all services (PostgreSQL + API)
docker compose up -d --build

# View logs
docker compose logs -f

# View logs for specific service
docker compose logs -f api

# Stop services
docker compose down

# Stop and remove volumes (fresh start)
docker compose down -v
```

### Docker Services

**PostgreSQL Database:**
- Image: `postgres:16-alpine`
- Port: `5432:5432`
- Health check configured
- Persistent volume: `postgres-data`

**API Service:**
- Built from `services/api/Dockerfile`
- Port: `8080:8080`
- Depends on PostgreSQL
- Auto-restarts on failure

## CI/CD Pipeline

### GitHub Actions Workflows

**API Pipeline (`.github/workflows/ci-api.yml`):**
- Triggers on `services/api/**` changes
- Maven dependency caching
- Runs unit tests + integration tests (with Testcontainers)
- Builds Docker image

**Mobile Pipeline (`.github/workflows/ci-app.yml`):**
- Triggers on `apps/mobile/**` changes
- Flutter pub cache
- Runs Flutter tests and analysis

**Release Pipeline (`.github/workflows/release.yml`):**
- Manual trigger with semantic versioning
- Tags and creates GitHub release

### Path-Based Triggers

Pipelines use path filters to avoid unnecessary runs:
```yaml
paths:
  - 'services/api/**'
  - '.github/workflows/ci-api.yml'
```

---

# 🤝 Contributing

## Development Phases

- **F0 (Setup):** ✅ Monorepo, Docker, CI/CD
- **F1 (Domain & API):** ✅ Entities, CRUD, migrations, Google OAuth
- **F2 (Flutter MVP):** 🚧 Login, feed, review screens
- **F3 (Observability):** 📍 Logs, metrics, tracing
- **F4 (CI/CD):** 📍 Full pipelines, deployment
- **F5 (Play Store):** 📍 App signing, release
- **F6+ (Evolution):** 📍 Advanced features

## Code Review Checklist

### General
- [ ] Code in English, comments can be in Portuguese
- [ ] Quality over speed
- [ ] Tests included for new features
- [ ] Documentation updated (README, CLAUDE.md, OpenAPI)

### Backend
- [ ] Constructor injection (no field injection)
- [ ] `@ConfigurationProperties` for configs (no `@Value`)
- [ ] OpenAPI/Swagger annotations on REST endpoints
- [ ] Domain exceptions with proper HTTP status
- [ ] Tests passing (unit + integration)

### Frontend
- [ ] Freezed models for DTOs
- [ ] Riverpod for state management
- [ ] Widget tests for screens
- [ ] No warnings in `flutter analyze`

---

## 📚 Useful References

### Core Documentation

- **[CLAUDE.md](CLAUDE.md)** - AI guidance for Claude Code (project guidelines, architecture, conventions)
- **[ROADMAP.md](ROADMAP.md)** - Current implementation status, next steps, backlog (updated each session)
- **[LEARNINGS.md](LEARNINGS.md)** - Session logs, technical decisions, problems & solutions (chronological archive)
- **[TESTING.md](TESTING.md)** - TDD/BDD strategy, testing conventions, examples
- **[README.md](README.md)** - This file (project setup instructions and overview)

### Coding Conventions

- **[CODING_STYLE_GENERAL.md](CODING_STYLE_GENERAL.md)** - Universal cross-stack conventions
- **[services/api/CODING_STYLE_BACKEND.md](services/api/CODING_STYLE_BACKEND.md)** - Java/Spring Boot conventions
- **[apps/mobile/CODING_STYLE_FRONTEND.md](apps/mobile/CODING_STYLE_FRONTEND.md)** - Flutter/Dart conventions
- **[infra/CODING_STYLE_INFRASTRUCTURE.md](infra/CODING_STYLE_INFRASTRUCTURE.md)** - Docker/CI/CD conventions

### Command References

- **[services/api/COMMANDS.md](services/api/COMMANDS.md)** - Backend API command reference
- **[apps/mobile/COMMANDS.md](apps/mobile/COMMANDS.md)** - Mobile app command reference
- **[infra/COMMANDS.md](infra/COMMANDS.md)** - Infrastructure command reference

### Automation & Productivity

- **[.claude/agents-readme.md](.claude/agents-readme.md)** - 9 specialized agents (automation-sentinel, backend-code-reviewer, cross-project-architect, flutter-implementation-coach, frontend-ux-specialist, learning-tutor, pulse, session-optimizer, tech-writer)
- **[.claude/METRICS.md](.claude/METRICS.md)** - Automation metrics system (usage tracking, ROI analysis)
- **[.claude/commands/](.claude/commands/)** - Custom slash commands for common workflows

### Stack-Specific Docs

- **[services/api/README.md](services/api/README.md)** - Backend architecture and setup
- **[apps/mobile/README.md](apps/mobile/README.md)** - Frontend architecture and setup
- **[infra/README.md](infra/README.md)** - Infrastructure setup and Docker configuration

### API Documentation

- **[OpenAPI/Swagger UI](http://localhost:8080/swagger-ui.html)** - Live API documentation (when backend running)
- **[API Docs (JSON)](http://localhost:8080/v3/api-docs)** - OpenAPI schema

### CI/CD

- **[.github/workflows/](.github/workflows/)** - GitHub Actions pipelines with path filters

### External Resources

- **[Claude Code Documentation](https://docs.anthropic.com/claude/docs)** - Official Claude Code documentation and best practices

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Icons and images from OpenMoji and Undraw (free licenses)
- Built with Spring Boot, Flutter, and PostgreSQL
- Inspired by wine enthusiasts worldwide

---

**Made with ❤️ by a Brazilian developer | Quality over speed, always.**
