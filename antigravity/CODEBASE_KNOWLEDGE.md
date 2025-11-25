# 🧠 Antigravity Codebase Knowledge

> **Purpose:** This file serves as a "brain dump" for AI agents to quickly understand the `wine-reviewer` project without re-reading every file.

## 1. Project Overview
**Wine Reviewer** is a monorepo for a mobile-first wine rating app.
- **Goal:** Rate wines (1-5 glasses), share notes/photos, community interaction.
- **Core Values:** Quality over speed, 100% free hosting (MVP), English source code.

## 2. Architecture & Tech Stack

### 📂 Monorepo Structure
- `apps/mobile/` -> **Frontend (Flutter)**
- `services/api/` -> **Backend (Spring Boot)**
- `infra/` -> **Infrastructure (Docker, Deployment)**

### 📱 Frontend (Mobile)
- **Framework:** Flutter 3.x
- **State Management:** Riverpod (Providers, StateNotifiers)
- **Navigation:** go_router
- **HTTP:** Dio (with interceptors for Auth)
- **Models:** Freezed + json_serializable (Immutable)
- **Key Pattern:** Feature-first structure (`lib/features/`, `lib/core/`)

### ☕ Backend (API)
- **Language:** Java 21
- **Framework:** Spring Boot 3
- **Database:** PostgreSQL 16 (with Flyway migrations)
- **Testing:** JUnit 5 + Testcontainers (Real Postgres integration tests)
- **Docs:** OpenAPI/Swagger (`springdoc-openapi`)
- **Auth:** Google OAuth + JWT (Stateless)

### 🐳 Infrastructure
- **Local:** Docker Compose (`infra/docker-compose.yml`)
- **CI/CD:** GitHub Actions (Path-based triggers)

## 3. Key Development Patterns

### 🧪 Testing Strategy (TDD/BDD)
- **Rule:** Red-Green-Refactor is mandatory.
- **Backend:** Unit tests (Mockito) + Integration tests (Testcontainers).
- **Frontend:** Widget tests + Golden tests.
- **Naming:** `should[ExpectedBehavior]When[StateUnderTest]`

### 📝 Documentation
- **Living Docs:** Update `README.md`, `ROADMAP.md`, and `LEARNINGS.md` at the end of sessions.
- **CLAUDE.md:** Contains detailed AI-specific instructions (Read this if stuck!).
- **ADRs:** Check `ADRs/` for major architectural decisions.

## 4. "Gotchas" & Critical Context
- **Hosting:** Must use FREE TIER services only.
- **Auth:** Google Sign-In only (no email/password).
- **Images:** Pre-signed URLs for direct S3/Supabase upload (Client -> S3 -> API).
- **Language:** Code in English, but comments/docs can be PT-BR if helpful (Brazilian dev).
- **Strictness:** Constructor injection only (no `@Autowired` fields).

## 5. Quick Start Commands
```bash
# Backend
cd services/api && ./mvnw spring-boot:run
# Backend Tests
cd services/api && ./mvnw verify

# Mobile
cd apps/mobile && flutter run
# Mobile Tests
cd apps/mobile && flutter test

# Infra (Start DB + API)
cd infra && docker compose up -d
```

## 6. Where to find more info?
- `CLAUDE.md`: The ultimate guide for this project.
- `CODING_STYLE_*.md`: Specific syntax and style rules.
- `TESTING.md`: Detailed testing guide.
