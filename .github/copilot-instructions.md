# GitHub Copilot Instructions for Wine Reviewer

This file provides guidance to GitHub Copilot when working with code in this repository.

## Project Overview

**Wine Reviewer** is a mobile-first wine rating application built as a monorepo. The MVP allows users to rate wines (1-5 glasses, not stars), view others' reviews, comment on reviews, and optionally upload photos.

**Core Principles:**
- Quality over speed—take time to design and test properly
- MVP must use 100% free hosting
- Source code in English (Brazilian developer, PT-BR for documentation/comments when beneficial)
- Never commit secrets, API keys, or credentials
- Use free tools and assets with proper licenses only

## Repository Structure

```
wine-reviewer/
├── apps/mobile/          # Flutter mobile app (Android-first)
├── services/api/         # Spring Boot API (Java 21)
├── infra/                # Docker Compose, deployment configs
├── prompts/              # AI prompt pack and agent schemas
├── .github/workflows/    # CI/CD pipelines with path filters
└── ADRs/                 # Architecture Decision Records (future)
```

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

## Development Commands

### Mobile App (Flutter)
```bash
cd apps/mobile
flutter pub get           # Install dependencies
flutter run              # Run app on device
flutter analyze          # Lint and analyze
flutter test --coverage  # Run tests with coverage
dart format .            # Format code
```

### Backend API (Spring Boot)
```bash
cd services/api
./mvnw spring-boot:run   # Run locally (requires Postgres)
./mvnw verify            # Build and run tests
./mvnw -q -DskipTests=false verify  # Run tests only
# OpenAPI docs: http://localhost:8080/swagger-ui.html
```

### Infrastructure
```bash
cd infra
docker compose up -d --build    # Start all services
docker compose logs -f          # View logs
docker compose down -v          # Stop and remove volumes
```

## Code Conventions

### General Guidelines
- All source code in English
- Quality over speed—avoid overengineering; implement MVP features first
- Make minimal, focused changes
- Write comprehensive tests for new features
- Document public APIs with Javadoc (Java) or doc comments (Dart)

### Java/Spring Boot
- Package structure follows domain/feature separation
- DTOs for API contracts with validation annotations (@Valid, @NotNull, etc.)
- Service layer for business logic
- Repository pattern with Spring Data JPA
- Comprehensive Javadoc for public APIs
- Integration tests with Testcontainers

Example package structure:
```
com.winereview.api/
├── domain/           # Domain entities
├── dto/              # Data transfer objects
├── repository/       # Spring Data repositories
├── service/          # Business logic
├── controller/       # REST controllers
└── config/           # Configuration classes
```

### Flutter/Dart
- Feature-based folder structure: `lib/features/`, `lib/core/`, `lib/common/widgets/`
- Models and DTOs using freezed + json_serializable
- Consistent formatting with `dart format`
- Widget tests and golden tests for UI components
- Error handling in dio interceptors with retry logic

Example folder structure:
```
lib/
├── features/         # Feature modules
│   ├── auth/
│   ├── reviews/
│   └── wines/
├── core/             # Core utilities, constants
├── common/           # Shared widgets, models
└── main.dart
```

### Docker
- Multi-stage builds for smaller images
- Health checks for dependent services
- Named volumes for data persistence

### CI/CD
- Path filters to avoid unnecessary pipeline runs
- Caching for dependencies (Maven, Flutter pub)
- Run tests before build/deploy steps

## Testing Strategy

### Backend (Test Pyramid Approach)
- **Unit Tests:** Business logic in services, utilities
- **Integration Tests:** API endpoints with Testcontainers (real Postgres)
- Focus on critical paths: authentication, review creation, pagination, comments
- Use meaningful test names: `shouldReturnReviewsWhenValidUserRequests()`

### Mobile (Test Pyramid Approach)
- **Unit Tests:** Business logic, state management
- **Widget Tests:** Individual widgets and screens
- **Golden Tests:** Visual regression testing for key UI components
- Focus on: authentication flow, form validation, image upload, feed rendering

## Domain Model

Core entities (DDD-lite approach):
- **User** (`app_user`): display_name, email, avatar_url
- **Wine**: name, winery, country, grape, year, image_url
- **Review**: user_id, wine_id, rating (1-5), notes, photo_url
- **Comment**: review_id, author_id, text

Database design:
- UUIDs as primary keys
- Timestamptz for audit fields (created_at, updated_at)
- Indexes on foreign keys and sorting columns
- Cascade delete for comments when review is deleted
- Check constraints (e.g., rating between 1-5)

## Authentication Flow

1. User signs in with Google (mobile app uses google_sign_in package)
2. App sends Google ID token to backend
3. Backend validates with Google OAuth/OpenID, creates/updates user
4. Backend issues short-lived JWT + refresh token
5. App stores tokens securely (flutter_secure_storage)
6. Protected endpoints require valid JWT in Authorization header

## Image Upload Flow

1. Client requests pre-signed URL from API
2. API generates pre-signed URL (S3 or Supabase Storage)
3. Client uploads image directly to storage using pre-signed URL (PUT)
4. Client sends final photo URL to API with review data
5. Enforce limits: file size, MIME types, expiration

## Important Constraints

1. **Free Hosting Only:** MVP must run on 100% free services (AWS Free Tier preferred)
2. **No Paid Services:** All tools, libraries, and cloud resources must have free tiers
3. **Security First:** Short-lived tokens, CORS restrictions, input validation, secure storage
4. **Mobile-First:** Primary target is Android (Galaxy S24 Ultra); iOS support is future work
5. **Scalability Awareness:** Design for moderate scale (~1000 users) but avoid premature optimization

## Common Patterns

### API Response Format
```java
@Data
public class ApiResponse<T> {
    private T data;
    private String message;
    private LocalDateTime timestamp;
}
```

### Error Handling (Flutter)
```dart
try {
  final response = await dio.get('/api/reviews');
  return parseReviews(response.data);
} on DioException catch (e) {
  if (e.type == DioExceptionType.connectionTimeout) {
    throw NetworkException('Connection timeout');
  }
  throw ApiException(e.response?.statusCode, e.message);
}
```

### Repository Pattern (Spring Boot)
```java
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findByWineIdOrderByCreatedAtDesc(UUID wineId);
    Page<Review> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}
```

## Observability

- Structured JSON logs (Spring Boot uses Logback with JSON encoder)
- HTTP metrics (p95 latency)
- Distributed tracing with OpenTelemetry (future enhancement)
- OTLP export to Grafana Cloud Free or CloudWatch Free

## Development Phases

The project follows a phased approach:
- **F0 (Setup):** Monorepo, linters, Docker Compose, basic CI/CD ✅
- **F1 (Domain & API):** Core entities, Review/Comment endpoints, Google login, Flyway, Testcontainers
- **F2 (Flutter MVP):** Login screen, feed, new review with photo, review details with comments
- **F3 (Observability):** Logs, metrics, tracing
- **F4 (CI/CD):** Full pipelines, Docker builds, free-tier deployment
- **F5 (Play Store):** App signing, bundle, store listing, privacy policy
- **F6+ (Evolution):** Follow users, recommendations, i18n, API integrations

## Helpful Resources

- **Main Prompt Pack:** See `prompts/PACK.md` for comprehensive AI guidance
- **README Files:** Each subdirectory has specific setup instructions
- **CLAUDE.md:** Additional context for Claude AI
- **GitHub Actions:** Workflows use path filters for monorepo efficiency
- **Docker Compose:** Start here for local development (`infra/docker-compose.yml`)

## When Suggesting Code

- Prefer minimal, focused changes over large refactorings
- Follow existing patterns in the codebase
- Add appropriate error handling
- Include relevant tests
- Consider security implications (input validation, SQL injection, XSS, etc.)
- Use dependency injection and follow SOLID principles
- Avoid hardcoded values; use configuration or constants
- Check for existing utilities before creating new ones
