---
description: Finish session with tests, docs update, and commit
argument-hint: <optional-commit-message-context>
---

**Session Finalization Workflow**

Additional context for commit message: $ARGUMENTS

Execute the following steps in order:

## 1. Run Tests (if applicable)
```bash
# Backend:
cd services/api && ./mvnw test -q

# Frontend:
cd apps/mobile && flutter test

# Choose based on what was modified this session
```

## 2. Update ROADMAP.md (REQUIRED)
**Always update ROADMAP.md** to reflect session progress:
- Move completed tasks from "In Progress" → "Implemented" section
- Update "In Progress" with current work
- Reprioritize "Next Steps" (Priority 1, 2, 3...)
- Update "Last updated" timestamp
- Update metrics table if applicable

Ask user: "What was completed this session?" (for ROADMAP.md update)

## 3. Update LEARNINGS.md (if significant)
**Prompt user:** "Were there significant learnings, problems solved, or technical decisions made this session?"

If YES → Add new session entry using hybrid format:
```markdown
## Session YYYY-MM-DD: Brief Title

### ☕ Backend (if applicable)
**Context:** ...
**What Was Done:** ...
**Key Insights:** ...

### 📱 Frontend (if applicable)
**Context:** ...
**What Was Done:** ...
**Key Insights:** ...
```

If NO → Skip LEARNINGS.md update

## 4. Update Other Documentation (if needed)
Review and update if changes were significant:
- **CLAUDE.md** - Only for new architecture patterns or critical directives
- **CODING_STYLE.md** - Only for new coding conventions
- **README.md** - If new features/endpoints were added
- **OpenAPI annotations** - If new REST endpoints were added

Show me which files need updates based on what was implemented this session.

## 5. Review Changes
Show consolidated git diff for all modified files so I can review before committing.

## 6. Commit
After I approve the diff, create a commit with:
- Proper semantic commit message (feat/fix/docs/refactor/test/chore)
- Reference to what was implemented
- Claude Code footer

## 7. Session Summary
Provide a brief summary:
- What was accomplished
- Test results (if tests were run)
- What's next (link to ROADMAP.md Priority 1)
- Any blockers or pending items
