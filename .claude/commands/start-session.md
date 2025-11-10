---
description: Start a new development session with stack-specific context loading
argument-hint: <optional: --stack=backend|frontend|infra|docs|full or context description>
---

# Session Context Loading Strategy

## Step 1: Determine Stack Focus

**If user provided `--stack` parameter:**
- `--stack=backend` → Load backend-specific files
- `--stack=frontend` → Load frontend-specific files
- `--stack=infra` → Load infrastructure-specific files
- `--stack=docs` → Load minimal documentation files
- `--stack=full` → Load all files (legacy behavior)

**If NO `--stack` parameter provided:**
- Analyze `$ARGUMENTS` for context clues:
  - Keywords: "controller", "service", "repository", "JPA", "Spring" → **backend**
  - Keywords: "widget", "screen", "Riverpod", "Flutter", "Dart" → **frontend**
  - Keywords: "Docker", "Testcontainers", "CI/CD", "pipeline" → **infra**
  - Keywords: "documentation", "README", "CLAUDE.md", "docs" → **docs**
- If ambiguous or unclear, use **AskUserQuestion** to prompt:
  - Question: "Which stack are you working on for this session?"
  - Options:
    1. Backend (Java/Spring Boot) - API, controllers, services, database
    2. Frontend (Flutter/Dart) - Mobile app, UI, widgets, screens
    3. Infrastructure (Docker/CI/CD) - Containers, tests, pipelines
    4. Documentation - CLAUDE.md, README.md, coding styles
    5. Full Context - Load everything (use sparingly)

## Step 2: Load Files Based on Stack

### Backend Session
@CLAUDE.md
@CODING_STYLE_GENERAL.md
@services/api/CODING_STYLE_BACKEND.md
@ROADMAP.md
@README.md
@.claude/agents/README.md

### Frontend Session
@CLAUDE.md
@CODING_STYLE_GENERAL.md
@apps/mobile/CODING_STYLE_FRONTEND.md
@ROADMAP.md
@README.md
@.claude/agents/README.md

### Infrastructure Session
@CLAUDE.md
@CODING_STYLE_GENERAL.md
@infra/CODING_STYLE_INFRASTRUCTURE.md
@ROADMAP.md
@README.md
@.claude/agents/README.md

### Documentation Session
@CLAUDE.md
@ROADMAP.md
@README.md
@.claude/agents/README.md

### Full Context Session (Legacy)
@CLAUDE.md
@CODING_STYLE_GENERAL.md
@services/api/CODING_STYLE_BACKEND.md
@apps/mobile/CODING_STYLE_FRONTEND.md
@infra/CODING_STYLE_INFRASTRUCTURE.md
@ROADMAP.md
@README.md
@.claude/agents/README.md

**Session Context:** $ARGUMENTS

## Step 3: Standard Session Initialization

1. Review ROADMAP.md to understand:
   - Current implementation status
   - What's in progress
   - Next priority tasks (Priority 1, 2, 3...)
2. Review .claude/agents/README.md to understand:
   - Available specialized agents
   - When to trigger each agent proactively
3. Review recent commits with `git log --oneline -5` to understand latest changes
4. Check current git status to see uncommitted changes
5. Provide a brief summary of:
   - **Stack focus for this session** (backend/frontend/infra/docs/full)
   - Current project state (from ROADMAP.md)
   - Next priority tasks (from "Next Steps" section)
   - Any uncommitted changes that need attention
   - Quick reminder of key architectural patterns (from CLAUDE.md)
   - Available agents for this session's work
   - **Token savings achieved** (if stack-specific loading was used)

**Ready to start development following all project guidelines.**

**Note:** Load LEARNINGS.md only if user mentions investigating past decisions or specific session details.

## Token Savings Report

After loading, report approximate token savings:
- Backend session: ~41% reduction vs full load
- Frontend session: ~46% reduction vs full load
- Infrastructure session: ~47% reduction vs full load
- Documentation session: ~54% reduction vs full load

