# 🧠 Antigravity Codebase Knowledge

> **Purpose:** This file serves as the **ultimate source of truth** for AI agents working on the `wine-reviewer` project. It synthesizes information from all documentation files to provide immediate, deep context.

---

## 1. 🎯 Project Overview & Core Principles

**Wine Reviewer** is a mobile-first wine rating application (1-5 glasses) with social features.

### Critical Directives
1.  **Quality Over Speed:** Take time to design and test properly. No "quick fixes".
2.  **100% Free Tier MVP:** Architecture MUST run on free services (Render, Supabase, AWS Free Tier). No paid dependencies.
3.  **Language:** Source code in **English**. Comments/Logs/Docs can be in **Portuguese** (Brazilian dev).
4.  **Living Documentation:** Update `README.md`, `ROADMAP.md`, and `LEARNINGS.md` at the end of *every* session.

---

## 2. 🏗️ Architecture & Tech Stack

### 📂 Monorepo Structure
- `apps/mobile/` → **Flutter** (Frontend)
- `services/api/` → **Spring Boot** (Backend)
- `infra/` → **Docker/DevOps**

### 📱 Frontend (Mobile)
- **Framework:** Flutter 3.x
- **State Management:** **Riverpod** (Providers, StateNotifiers). *Think: Spring IoC + RxJava.*
- **Navigation:** **go_router** (Declarative, Deep linking). *Think: Spring MVC @RequestMapping.*
- **HTTP:** **Dio** (Interceptors for Auth, Retry). *Think: RestTemplate/OkHttp.*
- **Models:** **Freezed** + **json_serializable** (Immutable DTOs). *Think: Lombok @Value.*
- **Storage:** `flutter_secure_storage` for JWTs.
- **Pattern:** Feature-first (`lib/features/auth`, `lib/features/reviews`).

### ☕ Backend (API)
- **Language:** Java 21 (Records, Pattern Matching, `var`).
- **Framework:** Spring Boot 3.
- **Database:** PostgreSQL 16.
- **Migrations:** Flyway.
- **Docs:** OpenAPI/Swagger (`springdoc-openapi`) - **MANDATORY** for all endpoints.
- **Auth:** Google OAuth + JWT (Stateless).

### 🐳 Infrastructure
- **Local:** Docker Compose (`postgres` + `api`).
- **CI/CD:** GitHub Actions with path-based triggers (`services/api/**`, `apps/mobile/**`).
- **Testing:** Testcontainers (Real PostgreSQL integration tests).

---

## 3. 🧪 Testing Strategy (TDD/BDD)

**CRITICAL RULE:** All new features MUST follow **Red-Green-Refactor**.

### Universal Rules
- **Naming:** `should[ExpectedBehavior]When[StateUnderTest]`
- **Structure:** Given / When / Then
- **Coverage Goals:** Critical paths (100%), Business logic (90%), Controllers/UI (80%).

### Stack-Specifics
- **Backend:**
    - **Unit:** JUnit 5 + Mockito + AssertJ.
    - **Integration:** `AbstractIntegrationTest` with Testcontainers (reused Postgres container).
    - **Anti-Pattern:** Do NOT mock everything. Use real DB for integration tests.
- **Frontend:**
    - **Unit:** `flutter_test` + `mocktail`.
    - **Widget:** `WidgetTester`.
    - **Golden:** `golden_toolkit` for visual regression.

---

## 4. 📝 Coding Conventions

### General
- **Classes:** PascalCase.
- **Methods/Vars:** camelCase.
- **Constants:** UPPER_SNAKE_CASE.
- **Large Numbers:** Use underscores (e.g., `3_600_000`).

### Backend (Java)
- **Injection:** Constructor injection ONLY. No `@Autowired` on fields.
- **Config:** Use `@ConfigurationProperties`, not `@Value`.
- **Method Order:** Public → Private (Top-down invocation flow).
- **Formatting:** Always leave a blank line before the closing `}` of a class.
- **Lambdas:** Closing parenthesis `)` on the same line as the last argument.

### Frontend (Dart)
- **Immutability:** Use `freezed` for all models/states.
- **Widgets:** Prefer `StatelessWidget`. Use `const` constructors.
- **Files:** `snake_case.dart`.

---

## 5. 🤖 Automation Ecosystem (Claude Code)

> **Context:** This project uses a suite of custom agents and slash commands to accelerate development.

### 🕵️ Specialized Agents (`.claude/agents/`)
| Agent | Role | Use Case |
|-------|------|----------|
| **automation-sentinel** | **Meta-Agent** | Checks ecosystem health, redundancy, and metrics. |
| **backend-code-reviewer** | **Reviewer** | Reviews Java/Spring Boot code for best practices & security. |
| **flutter-implementation-coach** | **Coach** | Guides Flutter implementation, explains Riverpod/Dio patterns. |
| **frontend-ux-specialist** | **Designer** | Designs UI screens, wireframes, and ensures accessibility. |
| **cross-project-architect** | **Architect** | Extracts patterns and creates templates for new projects. |
| **learning-tutor** | **Teacher** | Teaches concepts (e.g., "Explain Riverpod") with exercises. |
| **pulse** | **Metrics** | Collects usage stats (delta tracking) for the sentinel. |
| **session-optimizer** | **Planner** | Plans efficient sessions to save tokens. |
| **tech-writer** | **Writer** | Writes docs, ADRs, Javadoc, and OpenAPI annotations. |

### ⚡ Key Slash Commands (`.claude/commands/`)
- **/start-session**: Loads stack-specific context (backend/frontend/infra) to save tokens.
- **/finish-session**: Runs tests, updates docs, and commits changes.
- **/create-pr**: Creates a GitHub PR and triggers automation checks.
- **/review-code**: Triggers code analysis.
- **/update-roadmap**: Updates `ROADMAP.md` with progress.
- **/directive**: Adds a new directive to `CLAUDE.md`.

### 🔄 Anti-Cyclic Rule
**CRITICAL:** Commands can call Agents. Agents can call Agents. **Agents MUST NEVER call Commands.**

---

## 6. 🚀 Quick Start & Deployment

### Commands
```bash
# Backend
cd services/api && ./mvnw spring-boot:run
cd services/api && ./mvnw verify  # Run all tests

# Mobile
cd apps/mobile && flutter run
cd apps/mobile && flutter test --coverage
cd apps/mobile && flutter pub run build_runner build --delete-conflicting-outputs

# Infra
cd infra && docker compose up -d --build
```

### Deployment (Free Tier Strategy)
- **Backend:** Render Free / AWS EC2 Free Tier.
- **DB:** Supabase Free / AWS RDS Free Tier.
- **Storage:** S3 Free Tier / Supabase Storage.
- **Observability:** Grafana Cloud Free.

---

## 7. 📚 Key Documentation Links
- **`CLAUDE.md`**: The master guide.
- **`TESTING.md`**: Detailed TDD/BDD guide.
- **`ADRs/`**: Architectural decisions.
- **`apps/mobile/DEPENDENCIES_EXPLAINED.md`**: Great for backend devs learning Flutter.
