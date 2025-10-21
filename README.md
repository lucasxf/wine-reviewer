# 🍷 Wine Reviewer

> A mobile-first wine rating application with Google authentication, photo uploads, and community reviews.

[![API CI/CD](https://github.com/username/wine-reviewer/actions/workflows/ci-api.yml/badge.svg)](https://github.com/username/wine-reviewer/actions/workflows/ci-api.yml)
[![Mobile CI/CD](https://github.com/username/wine-reviewer/actions/workflows/ci-app.yml/badge.svg)](https://github.com/username/wine-reviewer/actions/workflows/ci-app.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Development](#development)
- [Testing](#testing)
- [Deployment](#deployment)
- [Contributing](#contributing)
- [License](#license)

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

### Current (v0.1.0 - Updated 2025-10-21)
- ✅ Complete Review CRUD API endpoints
- ✅ Comment system for reviews
- ✅ JWT authentication structure (JJWT 0.12.6)
- ✅ **Domain exception hierarchy** with proper HTTP status mapping
- ✅ PostgreSQL with Flyway migrations
- ✅ OpenAPI/Swagger documentation
- ✅ Docker Compose setup for local development
- ✅ **Comprehensive test suite** (27 tests, 100% passing)

### Exception Handling System
- ✅ `DomainException` abstract base class with `getHttpStatus()` method
- ✅ `ResourceNotFoundException` (404) - Resources not found by ID
- ✅ `InvalidRatingException` (400) - Rating validation (1-5 glasses)
- ✅ `UnauthorizedAccessException` (403) - Ownership violations
- ✅ `BusinessRuleViolationException` (422) - Business rule violations
- ✅ `GlobalExceptionHandler` with unified domain exception handling

### In Progress
- 🚧 Flutter mobile app
- 🚧 Google OAuth integration
- 🚧 Image upload with pre-signed URLs
- 🚧 Integration tests with Testcontainers

### Planned
- 📍 Observability (metrics, distributed tracing)
- 📍 User follow system
- 📍 Wine recommendations
- 📍 Internationalization (i18n)

## 🛠 Tech Stack

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
- **Testing:** Testcontainers
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

## 🚀 Getting Started

### Prerequisites

- **Java 21** (JDK)
- **Maven 3.8+** (or use `./mvnw`)
- **Flutter 3.x**
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

### Manual Backend Setup

```bash
cd services/api

# Run with Maven wrapper
./mvnw spring-boot:run

# Or run with specific profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Mobile App Setup

```bash
cd apps/mobile

# Install dependencies
flutter pub get

# Run app (select device)
flutter run

# Build for Android
flutter build apk
```

## 💻 Development

### Backend API

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

**Key API Endpoints:**
- `POST /api/reviews` - Create review (201 Created)
- `GET /api/reviews` - List reviews - ⚠️ Not implemented yet (501)
- `GET /api/reviews/{id}` - Get review details (200 OK, 404 Not Found)
- `PUT /api/reviews/{id}` - Update review (200 OK, 404 Not Found, 403 Forbidden)
- `DELETE /api/reviews/{id}` - Delete review - ⚠️ Not implemented yet (501)
- `POST /api/reviews/{id}/comments` - Add comment - ⚠️ Not implemented yet
- `GET /api/reviews/{id}/comments` - List comments - ⚠️ Not implemented yet

**HTTP Status Codes:**
- `200 OK` - Successful GET/PUT
- `201 Created` - Successful POST
- `204 No Content` - Successful DELETE
- `400 Bad Request` - Invalid input (validation errors, invalid rating)
- `403 Forbidden` - Ownership violation (trying to modify another user's resource)
- `404 Not Found` - Resource not found (review, wine, user)
- `422 Unprocessable Entity` - Business rule violation (e.g., invalid wine year)
- `500 Internal Server Error` - Unexpected errors
- `501 Not Implemented` - Endpoint planned but not implemented yet

### Mobile App

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

### Docker Commands

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

## 🧪 Testing

### Backend Testing Strategy (Test Pyramid)

- **Unit Tests:** Business logic, services, utilities
- **Integration Tests:** API endpoints with Testcontainers (real PostgreSQL)
- **Focus Areas:** Authentication, review CRUD, pagination, comments

```bash
# Run only unit tests
./mvnw test -Dtest="*Test"

# Run only integration tests
./mvnw test -Dtest="*IT"

# Run specific test class
./mvnw test -Dtest=ReviewServiceTest
```

### Mobile Testing Strategy

- **Unit Tests:** Business logic, state management
- **Widget Tests:** Individual widgets and screens
- **Golden Tests:** Visual regression testing

```bash
flutter test                    # All tests
flutter test test/unit/         # Unit tests only
flutter test test/widget/       # Widget tests only
```

## 🚀 Deployment

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

## 🤝 Contributing

### Code Conventions

**Java/Spring Boot:**
- Constructor injection only (no `@Autowired` on fields)
- Use `@ConfigurationProperties` instead of `@Value`
- Centralize dependency versions in Maven `<properties>`
- Use underscores for large numbers: `3_600_000`
- Method order: public methods first, then private (top-down)
- Include `@author` and `@date` in Javadoc

**Flutter/Dart:**
- Feature-based folder structure
- Use freezed for models
- Follow Effective Dart style guide
- Widget tests for all screens

See `CLAUDE.md` for detailed conventions.

### Development Phases

- **F0 (Setup):** ✅ Monorepo, Docker, CI/CD
- **F1 (Domain & API):** ✅ Entities, CRUD, migrations
- **F2 (Flutter MVP):** 🚧 Login, feed, review screens
- **F3 (Observability):** 📍 Logs, metrics, tracing
- **F4 (CI/CD):** 📍 Full pipelines, deployment
- **F5 (Play Store):** 📍 App signing, release
- **F6+ (Evolution):** 📍 Advanced features

## 📚 Documentation

- **`CLAUDE.md`** - Comprehensive project guide for AI assistants
- **`prompts/PACK.md`** - AI prompt pack and agent schemas
- **`services/api/README.md`** - Backend setup and API details
- **`apps/mobile/README.md`** - Mobile app setup and architecture
- **`ADRs/`** - Architecture decision records (future)

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Icons and images from OpenMoji and Undraw (free licenses)
- Built with Spring Boot, Flutter, and PostgreSQL
- Inspired by wine enthusiasts worldwide

---

**Made with ❤️ by a Brazilian developer | Quality over speed, always.**
