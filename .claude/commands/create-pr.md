---
description: Create pull request and analyze feature development workflow
argument-hint: <optional-pr-title>
---

@ROADMAP.md
@.claude/agents/automation-sentinel.md

**Pull Request Creation + Feature Analysis Workflow**

Additional context for PR title: $ARGUMENTS

**⚠️ Anti-Cyclic Dependency Note:**
This command delegates analysis to `automation-sentinel` agent. The agent MUST NEVER call `/create-pr` back (would create infinite loop). Agents are "workers", commands are "entry points".

Execute the following steps in order:

## 1. Detect Current Branch and Validate

```bash
# Get current branch
CURRENT_BRANCH=$(git branch --show-current)

# Check if on a feature branch (not main/develop)
if [[ "$CURRENT_BRANCH" == "main" || "$CURRENT_BRANCH" == "develop" ]]; then
  echo "❌ ERROR: Cannot create PR from main/develop branch"
  echo "Current branch: $CURRENT_BRANCH"
  exit 1
fi

# Show current branch
echo "Current branch: $CURRENT_BRANCH"
```

## 2. Determine Base Branch

**Check which base branch to target:**
```bash
# Default to 'develop' if it exists, otherwise 'main'
if git show-ref --verify --quiet refs/heads/develop; then
  BASE_BRANCH="develop"
else
  BASE_BRANCH="main"
fi

echo "Base branch: $BASE_BRANCH"
```

**Prompt user for confirmation:**
"Target base branch is `$BASE_BRANCH`. Is this correct? (y/n)"

If NO → Ask user: "Which base branch should this PR target?"

## 3. Check for Uncommitted Changes

```bash
# Ensure working directory is clean
if [[ -n $(git status --porcelain) ]]; then
  echo "⚠️ WARNING: You have uncommitted changes:"
  git status --short
  echo ""
  echo "Commit or stash changes before creating PR? (commit/stash/cancel)"
fi
```

**Handle uncommitted changes:**
- If user chooses "commit" → Use `/finish-session` workflow to commit first
- If user chooses "stash" → `git stash` and continue
- If user chooses "cancel" → Exit without creating PR

## 4. Generate PR Title and Description

**Determine PR title:**
- If `$ARGUMENTS` is provided → Use it as title
- If `$ARGUMENTS` is empty → Generate from branch name and commits

**Auto-generate title from branch name:**
```bash
# Extract feature name from branch (e.g., feature/comment-system → Comment System)
FEATURE_NAME=$(echo "$CURRENT_BRANCH" | sed 's|feature/||' | sed 's|-| |g' | sed 's/\b\w/\u&/g')

# Default title format
PR_TITLE="feat: $FEATURE_NAME"
```

**Auto-generate description from commit history:**
```bash
# Get commits unique to this branch (not in base branch)
git log $BASE_BRANCH..HEAD --oneline --pretty=format:"- %s" > /tmp/pr_commits.txt

# Show preview
echo "Commits in this branch:"
cat /tmp/pr_commits.txt
```

**Create description template:**
```markdown
## Summary
[Brief description of what this feature implements]

## Changes
$(cat /tmp/pr_commits.txt)

## Testing
- [ ] Unit tests passing (backend: 103 tests)
- [ ] Integration tests passing (Testcontainers)
- [ ] Flutter tests passing (if applicable)
- [ ] Manual testing completed

## Documentation
- [ ] ROADMAP.md updated
- [ ] OpenAPI/Swagger annotations added (if new endpoints)
- [ ] README.md updated (if user-facing changes)

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
```

**Prompt user:**
"Generated PR title: `$PR_TITLE`"
"Edit title? (y/n)"

If yes → Ask: "Enter new PR title:"

## 5. Create Pull Request with GitHub CLI

```bash
# Create PR using gh CLI
gh pr create \
  --base "$BASE_BRANCH" \
  --head "$CURRENT_BRANCH" \
  --title "$PR_TITLE" \
  --body "$(cat <<'EOF'
[Generated description from step 4]
EOF
)"
```

**Handle gh CLI errors:**
- If `gh` not installed → Show error: "GitHub CLI not found. Install: https://cli.github.com/"
- If not authenticated → Show error: "Not authenticated. Run: `gh auth login`"
- If PR already exists → Show existing PR URL

**Capture PR URL:**
```bash
# Get PR URL for newly created PR
PR_URL=$(gh pr view --json url --jq .url)
echo "✅ Pull Request created: $PR_URL"
```

## 6. Analyze Feature Development Workflow (Delegate to automation-sentinel)

**Automatically trigger `automation-sentinel` agent** to analyze this feature's development:

**Provide context to automation-sentinel:**
- **Feature branch:** `$CURRENT_BRANCH`
- **Base branch:** `$BASE_BRANCH`
- **Commits in feature:** Output of `git log $BASE_BRANCH..HEAD`
- **Files changed:** Output of `git diff $BASE_BRANCH..HEAD --name-only`
- **Duration:** First commit date → Last commit date
- **PR URL:** `$PR_URL`

**Request from automation-sentinel:**
Generate a **Feature Development Report** with:
1. **Workflow Analysis:**
   - Which agents were likely used (infer from commit messages, file types)
   - Which commands were invoked (grep git log for "/command" patterns)
   - Development patterns observed (e.g., test-driven, backend-first, etc.)

2. **Metrics:**
   - Total commits in feature branch
   - Files modified (breakdown by backend/frontend/infrastructure)
   - Feature duration (days from first to last commit)
   - Estimated time investment (if calculable)

3. **Recommendations:**
   - What went well (patterns to repeat)
   - Potential improvements for next feature
   - Suggested agent usage for similar features

4. **Learning Capture:**
   - Update internal automation usage metrics
   - Identify most valuable agents for this feature type
   - Flag any automation gaps (repeated manual work)

**Output Format:**
```markdown
# Feature Development Report: $FEATURE_NAME

**Branch:** $CURRENT_BRANCH → $BASE_BRANCH
**Duration:** [First commit date] to [Last commit date] ([X] days)
**Commits:** [N] commits
**PR:** $PR_URL

## Workflow Analysis
[automation-sentinel's analysis here]

## Metrics
[automation-sentinel's metrics here]

## Recommendations
[automation-sentinel's recommendations here]

## Next Steps
- Review PR: $PR_URL
- Prepare for merge to $BASE_BRANCH
- Consider automation improvements identified above
```

**Rationale:** This automatic analysis captures real-world automation usage patterns at natural feature boundaries, providing actionable insights for continuous improvement.

---

## 7. Final Summary

Provide comprehensive summary:
```
✅ Pull Request Created

📋 PR Details:
- Title: $PR_TITLE
- URL: $PR_URL
- Base: $BASE_BRANCH ← $CURRENT_BRANCH
- Commits: [N] commits

📊 Feature Analysis:
[Show key highlights from automation-sentinel report]

🎯 Next Steps:
1. Review PR at: $PR_URL
2. Address any CI/CD failures
3. Wait for approval and merge
4. Consider automation recommendations above

💡 Automation Insights:
[Top 3 recommendations from automation-sentinel]
```

**Note:** Keep report concise but actionable. Full detailed report is available by asking automation-sentinel directly.
