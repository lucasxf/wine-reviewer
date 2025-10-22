---
description: Finish session with tests, docs update, and commit
argument-hint: <optional-commit-message-context>
---

**Session Finalization Workflow**

Additional context for commit message: $ARGUMENTS

Execute the following steps in order:

## 1. Run Tests
```bash
cd services/api && ./mvnw test -q
```

## 2. Update Documentation
Review and update if changes were significant:
- CLAUDE.md (architecture, directives, learnings)
- CODING_STYLE.md (new patterns or conventions)
- README.md (implementation status, endpoints)
- OpenAPI annotations (if new endpoints were added)

Show me which files need updates based on what was implemented this session.

## 3. Review Changes
Show consolidated git diff for all modified files so I can review before committing.

## 4. Commit
After I approve the diff, create a commit with:
- Proper semantic commit message (feat/fix/docs/refactor/test/chore)
- Reference to what was implemented
- Claude Code footer

## 5. Session Summary
Provide a brief summary:
- What was accomplished
- Test results
- What should be tackled next session
- Any blockers or pending items
