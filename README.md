# 🍷 Wine Reviewer

> A mobile-first wine rating application with Google authentication, photo uploads, and community reviews.

[![API CI/CD](https://github.com/username/wine-reviewer/actions/workflows/ci-api.yml/badge.svg)](https://github.com/username/wine-reviewer/actions/workflows/ci-api.yml)
[![Mobile CI/CD](https://github.com/username/wine-reviewer/actions/workflows/ci-app.yml/badge.svg)](https://github.com/username/wine-reviewer/actions/workflows/ci-app.yml)
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

### Current (v0.1.0 - Updated 2025-10-22)
- ✅ Complete Review CRUD API endpoints
- ✅ Comment system for reviews
- ✅ JWT authentication structure (JJWT 0.12.6)
- ✅ **Google OAuth authentication** (AuthService, GoogleTokenValidator)
- ✅ **Domain exception hierarchy** with proper HTTP status mapping
- ✅ PostgreSQL with Flyway migrations
- ✅ OpenAPI/Swagger documentation
- ✅ Docker Compose setup for local development
- ✅ **Comprehensive test suite** (83 total tests: 46 unit + 37 integration)
- ✅ **✨ NEW: Integration tests with Testcontainers** (PostgreSQL, real database testing)

### Exception Handling System
- ✅ `DomainException` abstract base class with `getHttpStatus()` method
- ✅ `ResourceNotFoundException` (404) - Resources not found by ID
- ✅ `InvalidRatingException` (400) - Rating validation (1-5 glasses)
- ✅ `UnauthorizedAccessException` (403) - Ownership violations
- ✅ `BusinessRuleViolationException` (422) - Business rule violations
- ✅ `InvalidTokenException` (401) - Invalid/expired authentication tokens
- ✅ `GlobalExceptionHandler` with unified domain exception handling

### In Progress
- 🚧 Flutter mobile app
- 🚧 Image upload with pre-signed URLs
- 🚧 Integration tests with Testcontainers

### Planned
- 📍 Observability (metrics, distributed tracing)
- 📍 User follow system
- 📍 Wine recommendations
- 📍 Internationalization (i18n)

## 🛠 Tech Stack Overview

### Mobile App (`apps/mobile/`)
- **Framework:** Flutter 3.x
- **State Management:** Riverpod
- **Navigation:** go_router
- **HTTP Client:** dio
- **Models:** freezed + json_serializable
- **Storage:** flutter_secure_storage

### Backend API (`services/api/`)
- **Framework:** Spring Boot 3
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
   git clone https://github.com/username/wine-reviewer.git
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
   - Database: `winereviewer`
   - User: `winereviewer`
   - Password: `winereviewer_dev_pass`

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

- **`CLAUDE.md`** - Comprehensive project guide for AI assistants (organized by General/Backend/Frontend)
- **`CODING_STYLE.md`** - Coding standards and conventions (organized by General/Backend/Frontend)
- **`prompts/PACK.md`** - AI prompt pack and agent schemas
- **`services/api/README.md`** - Backend setup and API details
- **`apps/mobile/README.md`** - Mobile app setup and architecture
- **`ADRs/`** - Architecture decision records (future)

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
- `POST /api/reviews/{id}/comments` - Add comment - ⚠️ Not fully implemented yet
- `GET /api/reviews/{id}/comments` - List comments - ⚠️ Not fully implemented yet

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

- **46 tests, 100% passing**
- `ReviewControllerTest` - 4 tests (REST endpoint validation)
- `ReviewServiceTest` - 20 tests (business logic)
- `AuthServiceTest` - 5 tests (Google OAuth authentication)
- `GoogleTokenValidatorTest` - 5 tests (token validation)
- `DomainExceptionTest` - 12 tests (exception hierarchy)

## Backend Code Conventions

**Java/Spring Boot:**
- Constructor injection only (no `@Autowired` on fields)
- Use `@ConfigurationProperties` instead of `@Value`
- Centralize dependency versions in Maven `<properties>`
- Use underscores for large numbers: `3_600_000`
- Method order: public methods first, then private (top-down)
- Include `@author` and `@date` in Javadoc
- Always add OpenAPI/Swagger annotations to REST endpoints

See `CODING_STYLE.md` → **PART 2: BACKEND STANDARDS** for detailed conventions.

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

See `CODING_STYLE.md` → **PART 3: FRONTEND STANDARDS** for detailed conventions.

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
- `src/test/java/com/winereviewer/api/integration/AuthControllerIT.java`
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

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Icons and images from OpenMoji and Undraw (free licenses)
- Built with Spring Boot, Flutter, and PostgreSQL
- Inspired by wine enthusiasts worldwide

---

**Made with ❤️ by a Brazilian developer | Quality over speed, always.**
