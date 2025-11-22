# Automation Metrics System

Comprehensive guide to automation metrics collection, analysis, and optimization for the Wine Reviewer project.

---

## Overview

The Wine Reviewer project uses a **two-agent metrics system** to track automation usage (custom agents and slash commands) and provide actionable insights for workflow optimization.

**System Components:**
- **`.claude/metrics/usage-stats.toml`** - Git-tracked metrics database (TOML format)
- **`pulse` agent** - Lightweight metrics collector (Haiku model)
- **`automation-sentinel` agent** - Strategic analyzer and optimization advisor (Sonnet model)

---

## Architecture

### Two-Step Workflow

```
┌──────────────────────────────────────────────────────────────┐
│  Step 1: Data Collection (pulse agent)                       │
│  - Scans git log for agent/command invocations               │
│  - Calculates delta since last checkpoint                    │
│  - Updates usage-stats.toml with new metrics                 │
│  - Records commit SHA as checkpoint                          │
│  - Model: Haiku (fast, low-cost)                             │
└──────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌──────────────────────────────────────────────────────────────┐
│  Step 2: Analysis & Insights (automation-sentinel)           │
│  - Reads usage-stats.toml                                    │
│  - Identifies optimization opportunities                     │
│  - Detects redundancy, obsolescence, missing coverage        │
│  - Generates recommendations and reports                     │
│  - Model: Sonnet (strategic analysis)                        │
└──────────────────────────────────────────────────────────────┘
```

### Delta Tracking (Incremental Updates)

**Problem:** Scanning entire git history every time is wasteful (1000+ commits × 4 tokens = 4000+ tokens per analysis)

**Solution:** Track only changes since last checkpoint

**How it works:**
1. `pulse` scans git log from last checkpoint SHA to HEAD
2. Updates metrics with NEW invocations only
3. Records current HEAD SHA as new checkpoint
4. Next run starts from this SHA

**Benefits:**
- **50-80% token reduction** (only scan new commits)
- **Faster execution** (fewer commits to process)
- **Accurate history** (cumulative totals maintained)

**Example:**
```toml
[meta]
last_checkpoint = "29d8f49a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q"  # Git SHA
last_updated = "2025-11-21T15:30:00Z"
schema_version = "1.0"
```

---

## Usage Stats Format (TOML)

### File Structure

**Location:** `.claude/metrics/usage-stats.toml`

**Schema:**
```toml
[meta]
last_checkpoint = "commit-sha"      # Git SHA of last metrics update
last_updated = "2025-11-21T15:30:00Z"
schema_version = "1.0"

# Slash Commands
[commands.finish-session]
invocations = 13
estimated_time_saved_minutes = 390  # 13 × 30 min/session
last_used = "2025-11-18T10:15:00Z"

[commands.create-pr]
invocations = 8
estimated_time_saved_minutes = 160  # 8 × 20 min/PR
last_used = "2025-11-17T14:20:00Z"

# Custom Agents
[agents.tech-writer]
invocations = 15
estimated_time_saved_minutes = 375  # 15 × 25 min/doc
last_used = "2025-11-19T09:45:00Z"

[agents.backend-code-reviewer]
invocations = 12
estimated_time_saved_minutes = 240  # 12 × 20 min/review
last_used = "2025-11-18T16:30:00Z"

# Lines of Code Metrics (Added 2025-11-12)
[meta.locs]
total_added = 1234
total_deleted = 567
net_locs = 667  # added - deleted
agent_assisted_added = 890
agent_assisted_deleted = 320
agent_assisted_net = 570
manual_added = 344
manual_deleted = 247
manual_net = 97
agent_contribution_percentage = 85.5  # (570 / 667) × 100
```

### Time Savings Estimates

**Command estimates:**
- `/finish-session`: 30 min (tests + docs + commit + PR offer)
- `/create-pr`: 20 min (diff analysis + PR creation + sentinel report)
- `/update-roadmap`: 15 min (context loading + roadmap update)
- `/directive`: 10 min (search + deduplication + insertion)
- `/review-code`: 25 min (code analysis + report generation)
- `/start-session`: 5 min (context loading time saved)

**Agent estimates:**
- `tech-writer`: 25 min (docs research + writing + formatting)
- `backend-code-reviewer`: 20 min (code review + recommendations)
- `automation-sentinel`: 15 min (metrics analysis + optimization)
- `session-optimizer`: 10 min (session planning + token analysis)
- `flutter-implementation-coach`: 30 min (Flutter guidance + debugging)
- `frontend-ux-specialist`: 25 min (UI/UX design + rationale)
- `learning-tutor`: 20 min (concept explanation + exercises)
- `cross-project-architect`: 30 min (pattern extraction + templates)
- `pulse`: 5 min (metrics collection time saved)

---

## Metrics Collection with `pulse` Agent

### When to Run

**Automatic triggers:**
- Before `/create-pr` (integrated into PR workflow)
- Before `automation-sentinel` analysis

**Manual triggers:**
- `/update-metrics` command (if configured)
- When you want to see updated stats

### How to Run

```bash
# Automatic (preferred) - runs before sentinel during PR creation
/create-pr "Feature X implementation"

# Manual invocation (if needed)
# Use Task tool with subagent_type='pulse'
```

### What `pulse` Does

1. **Reads last checkpoint** from `usage-stats.toml`
2. **Scans git log** from checkpoint SHA to HEAD
3. **Identifies automation invocations:**
   - Commit messages with `agent:` prefix (e.g., `agent: tech-writer`)
   - Commit messages with `command:` prefix (e.g., `command: finish-session`)
   - LEARNINGS.md session entries with agent/command mentions
4. **Calculates LOCs** (lines added/deleted) for agent vs manual commits
5. **Updates TOML file** with new metrics
6. **Records new checkpoint** (current HEAD SHA)

### Delta vs Full Mode

**Delta mode (default):**
- Scans only commits since last checkpoint
- Fast, efficient
- Use for routine updates

**Full mode:**
- Scans entire git history
- Slower, comprehensive
- Use for initial setup or checkpoint reset

---

## Analysis with `automation-sentinel` Agent

### When to Run

**Automatic triggers:**
- After `/create-pr` (analyzes feature development workflow)

**Manual triggers:**
- Check automation health
- Identify optimization opportunities
- Generate usage reports
- Detect redundancy or obsolescence

### What `automation-sentinel` Does

1. **Reads `usage-stats.toml`**
2. **Analyzes patterns:**
   - Most/least used automations
   - Time savings ROI
   - Coverage gaps (missing agents/commands)
   - Redundancy (overlapping functionality)
   - Obsolescence (unused automations)
3. **Generates recommendations:**
   - Which automations to create
   - Which to deprecate
   - Workflow optimizations
   - Token efficiency improvements

### Example Output

```markdown
## Automation Usage Report

**Top 3 Automations (47% of total time saved):**
1. finish-session: 13 invocations (6.5h saved)
2. tech-writer: 15 invocations (6.25h saved)
3. backend-code-reviewer: 12 invocations (4h saved)

**Optimization Opportunities:**
- Consider creating `test-runner` command (manual testing pattern detected 8×)
- Deprecate `old-formatter` agent (0 invocations in last 30 days)
- Merge `doc-writer` and `tech-writer` (redundant functionality)

**Coverage Gaps:**
- No agent for database migrations
- No command for deployment automation
```

---

## Metrics Collection & Analysis Best Practices

*(Added 2025-11-18)*

### CRITICAL RULE: Show Your Work

Always work step-by-step and review your math when gathering metrics with `pulse` and generating insights with `automation-sentinel`.

### Why This Matters

- **Credibility:** Metrics errors undermine analysis and articles
- **Propagation:** Incorrect calculations spread to published content
- **Embarrassment:** Math mistakes discovered by code review (human or automated)

### Best Practices

1. **Show your work**
   - Document calculation steps explicitly
   - Example: `"12.75 / 35.5 = 0.359 ≈ 36%"`

2. **Double-check formulas**
   - Verify metric definitions match their names
   - Example: "net LOCs" = added - deleted

3. **Cross-reference sources**
   - Compare git log output with TOML file values
   - Ensure consistency

4. **Validate percentages**
   - Ensure parts sum to whole
   - Example: Top 3 agents should be ≤ 100% of total

5. **Review before commit**
   - Re-read all numbers in generated reports
   - Check for arithmetic errors

6. **Use examples from data**
   - Include real calculations from current metrics
   - No placeholders or fake data

### Example (Good Practice)

```markdown
## ROI Calculation (Step-by-Step)

**Data from usage-stats.toml:**
- finish-session: 13 invocations × 30 min/session = 390 min = 6.5h saved
- tech-writer: 15 invocations × 25 min/session = 375 min = 6.25h saved
- Total savings: 35.5h

**Top 2 percentage:**
- Combined savings: 6.5h + 6.25h = 12.75h
- Percentage: 12.75h / 35.5h = 0.3591 = **35.9% ≈ 36%**

**Top 3 percentage (adding backend-code-reviewer):**
- backend-code-reviewer: 12 invocations × 20 min = 240 min = 4h
- Combined savings: 12.75h + 4h = 16.75h
- Percentage: 16.75h / 35.5h = 0.4718 = **47.2% ≈ 47%**
```

---

## Benefits of Metrics System

### Token Efficiency

**Before metrics system:**
- Scan 1000+ commits per analysis
- 15,000 tokens per sentinel invocation
- Manual tracking prone to errors

**After metrics system:**
- Delta tracking (scan only new commits)
- 3,800 tokens per sentinel invocation (**75% reduction**)
- Automated, accurate tracking

### Actionable Insights

**Metrics enable:**
- **ROI analysis** - Quantify time savings per automation
- **Prioritization** - Focus on high-impact automations
- **Optimization** - Identify redundancy and gaps
- **Communication** - Demonstrate value to stakeholders

### Example ROI Report

```markdown
**Automation ROI (Last 30 days):**
- Total time saved: 35.5 hours
- Top automation: finish-session (6.5h saved)
- Agent contribution: 85.5% of code written
- Manual work: 14.5% of code written

**Conclusion:** Automation provides 3:1 ROI (time invested vs saved)
```

---

## Troubleshooting

### Issue: Metrics Not Updating

**Cause:** Last checkpoint SHA doesn't exist in git history

**Solution:**
```bash
# Check last checkpoint SHA
cat .claude/metrics/usage-stats.toml | grep last_checkpoint

# Verify SHA exists in git log
git log --oneline | grep <sha>

# If missing, reset checkpoint (full scan on next run)
# Edit usage-stats.toml: set last_checkpoint = "HEAD~100" or remove field
```

### Issue: Duplicate Invocations Counted

**Cause:** Commit message format parsed incorrectly

**Solution:**
- Use consistent format: `agent: agent-name` or `command: command-name`
- Avoid ambiguous mentions (e.g., "I used the agent to do X")

### Issue: LOCs Calculation Incorrect

**Cause:** Binary files, generated code, or excluded paths included

**Solution:**
- Ensure `pulse` excludes binary files (images, PDFs, etc.)
- Exclude generated code paths (e.g., `build/`, `target/`, `.dart_tool/`)
- Verify LOCs calculation logic in pulse agent

---

## Related Documentation

- **`.claude/agents-readme.md`** - Custom agents guide (includes pulse and automation-sentinel)
- **`CLAUDE.md`** - Project-wide guidelines
- **`ROADMAP.md`** - Current implementation status
- **`LEARNINGS.md`** - Session logs (source of metrics data)
- **`.claude/commands/create-pr.md`** - PR creation workflow (auto-triggers pulse + sentinel)
