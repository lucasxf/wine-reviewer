# Coding Style Guide - Wine Reviewer Project

> **Master Index:** This file provides navigation to stack-specific coding standards.
> **Last updated:** 2025-11-10

---

## 📚 Overview

The Wine Reviewer project uses **stack-specific coding style guides** to optimize context loading and reduce token usage during development sessions.

**Benefits:**
- ✅ **Targeted loading:** Load only relevant guidelines (40-54% token reduction)
- ✅ **Faster sessions:** Reduced context means faster initialization
- ✅ **Clear separation:** Backend, frontend, and infrastructure standards are isolated
- ✅ **Scalable:** Easy to add new stacks (BFF, mobile backend, etc.)

---

## 🗂️ Stack-Specific Coding Styles

### 🌍 General (Cross-Stack)
**File:** `CODING_STYLE_GENERAL.md` (root directory)
**Applies to:** All stacks
**Contains:** Universal conventions, nomenclature, documentation strategy
**Load when:** Every session (foundational rules)

**Topics covered:**
- Universal nomenclature (PascalCase, camelCase, UPPER_SNAKE_CASE)
- Number formatting (underscores for large numbers)
- Documentation principles (3-part structure)
- Update strategy for significant changes

---

### ☕ Backend (Java/Spring Boot)
**File:** `services/api/CODING_STYLE_BACKEND.md`
**Applies to:** `services/api/` (Java 21, Spring Boot 3, PostgreSQL)
**Contains:** Backend-specific patterns, JPA, REST, testing
**Load when:** Working on Backend (Spring Boot API, controllers, services, repositories)

**Topics covered:**
- Package structure (DDD/CQRS vs CRUD)
- Method ordering (public → private, invocation flow)
- Java 21 features (var, records, sealed classes, pattern matching)
- Lombok conventions (@Slf4j, @Getter, avoid @Data)
- Exception handling (DomainException hierarchy)
- REST Controllers + OpenAPI/Swagger documentation (CRITICAL)
- Javadoc requirements (@author, @date)
- Maven dependency management (versions in `<properties>`)
- Configuration properties (@ConfigurationProperties, not @Value)
- JPA callbacks (one method per lifecycle event)
- Spring Security filters (declare as @Bean, not @Component)

---

### 📱 Frontend (Flutter/Dart)
**File:** `apps/mobile/CODING_STYLE_FRONTEND.md`
**Applies to:** `apps/mobile/` (Flutter 3.x, Dart, Riverpod)
**Contains:** Flutter conventions, widgets, state management, testing
**Load when:** Working on mobile app, UI screens, widgets, providers

**Topics covered:**
- Feature-based folder structure (`features/`, `core/`, `common/`)
- Dart nomenclature (PascalCase classes, snake_case files, camelCase variables)
- Models with freezed + json_serializable
- Riverpod state management patterns
- Error handling with Dio interceptors
- Widget conventions (StatelessWidget vs StatefulWidget, const constructors)
- Testing (unit tests, widget tests, golden tests)
- UI/UX standards (Material Design, responsiveness, accessibility)

---

### 🐳 Infrastructure (Docker, Testing, CI/CD)
**File:** `infra/CODING_STYLE_INFRASTRUCTURE.md`
**Applies to:** `infra/` (Docker, Testcontainers, GitHub Actions)
**Contains:** DevOps patterns, integration testing, containerization
**Load when:** Working on Docker Compose, Testcontainers, CI/CD pipelines

**Topics covered:**
- Testcontainers integration test patterns
- Naming conventions (`*Test.java` vs `*IT.java`)
- AbstractIntegrationTest base class pattern
- Test data setup and cleanup strategies
- Docker multi-stage builds
- Docker Compose for local development
- GitHub Actions workflows (path-based triggers, caching)

---

## 🚀 Usage Guide

### When to Load Which Files

| Your Task | Load These Files |
|-----------|-----------------|
| **Backend development** | `CODING_STYLE_GENERAL.md` + `services/api/CODING_STYLE_BACKEND.md` |
| **Frontend development** | `CODING_STYLE_GENERAL.md` + `apps/mobile/CODING_STYLE_FRONTEND.md` |
| **Infrastructure work** | `CODING_STYLE_GENERAL.md` + `infra/CODING_STYLE_INFRASTRUCTURE.md` |
| **Full-stack feature** | All 4 files (General + Backend + Frontend + Infrastructure) |
| **Documentation only** | `CODING_STYLE_GENERAL.md` only |

### Using with /start-session Command

The `/start-session` command supports stack-specific context loading:

```bash
# Interactive prompt (recommended)
/start-session

# Explicit stack selection
/start-session --stack=backend    # Loads backend-specific files
/start-session --stack=frontend   # Loads frontend-specific files
/start-session --stack=infra      # Loads infra-specific files
/start-session --stack=docs       # Loads minimal context
/start-session --stack=full       # Loads all files (legacy behavior)
```

**Token savings:**
- Backend session: **~41% reduction** (vs full load)
- Frontend session: **~46% reduction**
- Infrastructure session: **~47% reduction**
- Documentation session: **~54% reduction**

---

## 📝 Maintenance Guidelines

### When to Update Coding Styles

**Update immediately when:**
- ✅ New patterns are established (e.g., new exception types, new widget patterns)
- ✅ Architectural decisions change conventions
- ✅ New tools or libraries are adopted with specific usage patterns
- ✅ Code review identifies recurring anti-patterns

**Don't update for:**
- ❌ One-off code snippets
- ❌ Project-specific business logic
- ❌ Temporary workarounds

### Which File to Update

**CODING_STYLE_GENERAL.md:**
- Cross-stack conventions (nomenclature, documentation strategy)
- Universal principles applicable to all languages/frameworks

**CODING_STYLE_BACKEND.md:**
- Java/Spring Boot specific patterns
- JPA, REST controllers, Maven, configuration

**CODING_STYLE_FRONTEND.md:**
- Flutter/Dart specific patterns
- Widgets, Riverpod, Dio, Material Design

**CODING_STYLE_INFRASTRUCTURE.md:**
- Docker, Testcontainers, CI/CD patterns
- Testing infrastructure, deployment

### Cross-References

All stack-specific files include cross-references to `CODING_STYLE_GENERAL.md` at the top and bottom:

```markdown
**For universal cross-stack guidelines, see:** `../../CODING_STYLE_GENERAL.md`
```

This ensures developers always have access to foundational rules.

---

## 🔄 Version History

- **2025-11-10** - Split CODING_STYLE.md into 4 stack-specific files for optimized session context loading (40-54% token reduction)
- **2025-10-22 (v6)** - Added PART 4: INFRASTRUCTURE with Testcontainers, Docker, CI/CD standards
- **2025-10-21 (v5)** - Added critical rule: 3-part documentation structure (General/Backend/Frontend)
- **2025-10-21 (v4)** - Restructured into 3 parts (GENERAL/BACKEND/FRONTEND) for reusability
- **2025-10-21 (v2)** - Added OpenAPI/Swagger documentation requirements, exception hierarchy
- **2025-10-20** - Initial version with JPA callbacks, Spring Security filters

---

## 📚 Related Documentation

- **CLAUDE.md** - Comprehensive project guidelines for AI assistants
- **ROADMAP.md** - Project status, next steps, backlog
- **README.md** - Setup instructions, architecture overview
- **.claude/agents/README.md** - 8 specialized agents for development workflows
- **.claude/commands/** - Custom slash commands for common tasks

---

**For questions or suggestions, consult the project lead or update this file with new conventions.**

