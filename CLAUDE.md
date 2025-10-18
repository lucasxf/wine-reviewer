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
./mvnw -q -DskipTests=false verify

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
- Quality over speed—take time to design and test properly
- Avoid overengineering; implement MVP features first
- Never commit secrets, API keys, or credentials
- Use free tools and assets with proper licenses only

### Java/Spring Boot
- Package structure follows domain/feature separation
- DTOs for API contracts with validation annotations
- Service layer for business logic
- Repository pattern with Spring Data JPA
- Comprehensive Javadoc for public APIs
- Integration tests with Testcontainers

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

### Backend (Pyramid Approach)
- **Unit Tests:** Business logic in services, utilities
- **Integration Tests:** API endpoints with Testcontainers (real Postgres)
- Focus on critical paths: authentication, review creation, pagination, comments

### Mobile (Pyramid Approach)
- **Unit Tests:** Business logic, state management
- **Widget Tests:** Individual widgets and screens
- **Golden Tests:** Visual regression testing for key UI components
- Focus on: authentication flow, form validation, image upload, feed rendering

## Important Constraints

1. **Free Hosting Only:** MVP must run on 100% free services (AWS Free Tier preferred but not required)
2. **No Paid Services:** All tools, libraries, and cloud resources must have free tiers adequate for ~1000 users
3. **Security First:** Short-lived tokens, CORS restrictions, input validation, secure storage
4. **Mobile-First:** Primary target is Android (Galaxy S24 Ultra); iOS support is future work
5. **Scalability Awareness:** Design for moderate scale but avoid premature optimization

## Useful References

- **Main Prompt Pack:** See `prompts/PACK.md` for comprehensive AI guidance and agent schemas
- **README Files:** Each subdirectory (apps/mobile, services/api, infra) has specific setup instructions
- **GitHub Actions:** Workflows use path filters for monorepo efficiency
- **Docker Compose:** Start here for local development (`infra/docker-compose.yml`)
