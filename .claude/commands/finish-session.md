---
description: Finish session with tests, docs update, and commit
argument-hint: <optional-commit-message-context>
---

@CLAUDE.md
@CODING_STYLE.md
@ROADMAP.md
@README.md

**Session Finalization Workflow**

Additional context for commit message: $ARGUMENTS

**⚠️ Anti-Cyclic Dependency Note:**
This command delegates documentation tasks to `tech-writer` agent and optionally to `automation-sentinel` agent. These agents MUST NEVER call `/finish-session` back (would create infinite loop). Agents are "workers", commands are "entry points".

Execute the following steps in order:

## 1. Run Tests (if applicable)
```bash
# Backend:
cd services/api && ./mvnw test -q

# Frontend:
cd apps/mobile && flutter test

# Choose based on what was modified this session
```

## 2. Update ROADMAP.md (REQUIRED - Delegate to tech-writer)
**Determine session context:**
- If `$ARGUMENTS` contains sufficient details → Use it directly
- If `$ARGUMENTS` is empty/vague → Ask user: "What was completed this session?"

**Delegate to `tech-writer` agent** to update ROADMAP.md:
- Move completed tasks from "In Progress" → "Implemented" section
- Update "In Progress" with current work
- Reprioritize "Next Steps" (Priority 1, 2, 3...)
- Update "Last updated" timestamp to today's date
- Update metrics table if applicable

**Rationale:** tech-writer handles both simple and complex documentation updates consistently.

## 3. Update LEARNINGS.md (if significant - Delegate to tech-writer)
**Prompt user:** "Were there significant learnings, problems solved, or technical decisions made this session?"

If YES → **Delegate to `tech-writer` agent** to add new session entry using hybrid format:
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

### 🐳 Infrastructure (if applicable)
**Context:** ...
**What Was Done:** ...
**Key Insights:** ...
```

If NO → Skip LEARNINGS.md update

**Rationale:** tech-writer ensures consistent formatting and comprehensive session documentation.

## 4. Update Other Documentation (if needed - Delegate to tech-writer)
**Review what was implemented** and determine which documentation needs updates:
- **CLAUDE.md** - Only for new architecture patterns or critical directives
- **CODING_STYLE.md** - Only for new coding conventions
- **README.md** - If new features/endpoints were added
- **OpenAPI annotations** - If new REST endpoints were added (CRITICAL)
- **Javadoc** - If new backend classes/methods were added
- **Dartdoc** - If new Flutter widgets/classes were added

**If updates needed** → Delegate to `tech-writer` agent to handle all documentation updates.

**Rationale:** tech-writer ensures OpenAPI annotations are complete (CRITICAL requirement), maintains 3-part structure, and handles in-code documentation correctly.

## 5. Automation Health Check (Automatic if applicable - Delegate to automation-sentinel)
**Check if automation files were modified:**
```bash
git diff --name-only | grep -E '\.claude/(agents|commands)/'
```

**If automation files changed:**
- Delegate to `automation-sentinel` agent for quick validation:
  - Schema validation (agents have required sections, proper format)
  - Dependency check (no broken references)
  - Quick redundancy scan (flag obvious overlaps)
- **Note:** This is a lightweight check, not a full health report

**If no automation files changed:**
- Skip this step

**Rationale:** Catches automation issues immediately while context is fresh. Full health reports run monthly.

---

## 6. Review Changes
Show consolidated git diff for all modified files so I can review before committing.

## 7. Commit
After I approve the diff, create a commit with:
- Proper semantic commit message (feat/fix/docs/refactor/test/chore)
- Reference to what was implemented
- Claude Code footer

## 8. Session Summary
Provide a brief summary:
- What was accomplished
- Test results (if tests were run)
- What's next (link to ROADMAP.md Priority 1)
- Any blockers or pending items
