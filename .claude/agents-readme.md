# Custom Agent Suite - Wine Reviewer Project

> **Purpose:** Specialized agents to accelerate development, learning, and project efficiency.
> **Created:** 2025-10-28
> **Developer:** Backend engineer (Java/Spring Boot expert) learning Flutter/Dart

---

## 📚 Agent Overview

This project includes **9 custom agents** designed to address specific needs (alphabetically ordered):

| Agent | Purpose | Model | Created | Last Updated |
|-------|---------|-------|---------|--------------|
| **automation-sentinel** | Meta-agent: automation health, metrics analysis, optimization | Sonnet | 2025-10-29 | 2025-11-11 |
| **backend-code-reviewer** | Java/Spring Boot code review, best practices | Sonnet | 2025-10-28 | 2025-11-11 |
| **cross-project-architect** | Pattern extraction, templates, new projects | Sonnet | 2025-10-28 | 2025-11-11 |
| **flutter-implementation-coach** | Flutter coding, Riverpod, Dio, debugging | Sonnet | 2025-10-28 | 2025-10-28 |
| **frontend-ux-specialist** | UI/UX design, screen layouts, Material Design | Sonnet | 2025-10-28 | 2025-11-11 |
| **learning-tutor** | Teaching concepts, structured learning, exercises | Sonnet | 2025-10-28 | 2025-10-28 |
| **pulse** | Metrics collection: agent/command usage tracking | Haiku | 2025-11-11 | 2025-11-12 |
| **session-optimizer** | Token efficiency, session planning, workflow | Haiku | 2025-10-28 | 2025-11-10 |
| **tech-writer** | Documentation (external + in-code), ADRs, Javadoc, OpenAPI | Sonnet | 2025-10-29 | 2025-11-11 |

---

## 🚨 Auto-Trigger Protocols (CRITICAL)

> **Purpose:** Centralized registry of when Claude MUST automatically invoke specific agents instead of manual approach.
> **Added:** 2025-12-08 (imported from project AI PR #10)

**Why Auto-Trigger Protocols Matter:**
During a backend code review session in project AI, Claude forgot to invoke the backend-code-reviewer agent and performed manual review instead. This revealed that without explicit, emphatic protocols, agents may be skipped even when they provide superior systematic analysis. These protocols prevent that failure mode.

---

### 1. Backend Code Reviewer Auto-Trigger Protocol

**CRITICAL:** ALWAYS invoke this agent (never manual review) when user:
- Says **"review"** + backend file path (e.g., "review ReviewService.java")
- Says **"check/validate/analyze"** + Java/Spring Boot code
- Says **"I just finished implementing [Service|Controller|Repository|...]"**
- Says **"before committing..."** or **"I'm about to commit..."**
- Provides code with request for feedback

**Why:** Agent's 10-dimension systematic review (architecture, security, performance, testing, OpenAPI docs, etc.) is ALWAYS superior to ad-hoc manual review. Manual reviews miss critical dimensions.

**Exception:** Only skip agent if user explicitly says **"quick check"** or **"sanity check"**.

**Cross-Agent Collaboration:**
- If OpenAPI documentation missing → Automatically invoke **tech-writer** agent
- If test coverage insufficient → Recommend test improvements with examples

**See:** [backend-code-reviewer.md](agents/backend-code-reviewer.md)

---

### 2. Learning Tutor Auto-Trigger Protocol

**CRITICAL:** ALWAYS invoke this agent when user:
- Says **"teach me"** + topic
- Says **"explain"** + concept (not implementation-specific)
- Says **"I want to understand"** + topic
- Says **"how does [concept] work?"** (conceptual, not "how do I implement")
- Says **"what's the difference between X and Y?"**
- Asks conceptual questions (not implementation tasks)

**Why:** Agent provides structured lessons with exercises, backend parallels, and spaced repetition—not just explanations. Builds deep understanding vs surface knowledge.

**Exception:** Skip for simple factual questions (e.g., "What's the Dart syntax for list comprehension?"). Use direct answer instead.

**Teaching Approach:**
- Uses Feynman Technique (explain simply)
- Provides backend parallels (Java/Spring Boot → Flutter/Dart)
- Includes exercises and assessments
- Tracks learning progress across sessions

**See:** [learning-tutor.md](agents/learning-tutor.md)

---

### 3. Tech Writer Auto-Trigger Protocol

**CRITICAL:** ALWAYS invoke this agent when user:
- Says **"add OpenAPI annotations"** + controller/endpoint name
- Says **"create ADR for"** + decision/topic
- Says **"add Javadoc to"** + class name
- Says **"update LEARNINGS.md"** or **"update ROADMAP.md"**
- Says **"document this endpoint/class/method"**
- **After creating/modifying any REST endpoint** (automatic)

**Why:** Agent enforces complete documentation standards (all HTTP status codes, @author tags, business rules, etc.). Manual documentation frequently misses critical elements like error responses or edge cases.

**Exception:** None for OpenAPI documentation—ALWAYS required for REST endpoints per project conventions.

**Cross-Agent Collaboration:**
- **backend-code-reviewer** may invoke this agent if documentation gaps found
- **/finish-session** command invokes this agent to update ROADMAP.md

**Documentation Standards:**
- OpenAPI: ALL HTTP status codes documented (200, 201, 204, 400, 401, 403, 404, 422, 500, 501)
- Javadoc: @author, @date, business rules, examples
- ADRs: Follow template (Context, Decision, Rationale, Consequences, Alternatives)

**See:** [tech-writer.md](agents/tech-writer.md)

---

### Protocol Compliance Checklist

When invoking agents, verify:
- [ ] Used agent instead of manual approach for systematic tasks
- [ ] Followed phrase-based trigger patterns
- [ ] Invoked cross-agent collaborations when needed
- [ ] Applied exceptions appropriately (don't over-invoke)
- [ ] Documented when new auto-trigger patterns emerge

**Monitoring:** automation-sentinel tracks agent invocation patterns and identifies missed opportunities.

---

## 🎯 Quick Start

### How Agents Work

1. **Automatic Invocation** - Claude Code automatically selects agents based on your message
2. **Explicit Invocation** - You can request specific agents: "Use the frontend-ux-specialist to design this screen"
3. **Complementary** - Multiple agents can work together (e.g., UX specialist designs, implementation coach codes)

### ⚠️ Anti-Cyclic Dependency Rule (CRITICAL)

**To prevent infinite loops, this hierarchy must be strictly enforced:**

```
Slash Commands (high-level orchestration)
    ↓ can call
Agents (task execution)
    ↓ can call
Other Agents (delegation)
    ↓ NEVER call
Slash Commands ❌ (would create cycle)
```

**Rules:**
- ✅ **Commands CAN call agents** - Example: `/finish-session` calls `tech-writer`
- ✅ **Agents CAN call other agents** - Example: `backend-code-reviewer` calls `tech-writer`
- ❌ **Agents MUST NEVER call commands** - Would create infinite loops

**Why this matters:**
- Commands are "entry points" (user-initiated workflows)
- Agents are "workers" (execute specific tasks)
- Workers can delegate to other workers, but never back to entry points
- Violating this rule causes cyclic dependencies and infinite loops

**Example Safe Delegation:**
```
/finish-session (command)
  → calls tech-writer (agent)
    → calls automation-sentinel (agent)  ✅ SAFE
    → NEVER calls /finish-session again  ❌ CYCLIC
```

### First Time Setup

Agents are already configured in `.claude/agents/`. No additional setup needed!

```bash
# Verify agents are available
ls .claude/agents/

# Should see (alphabetically):
# - automation-sentinel.md
# - backend-code-reviewer.md
# - cross-project-architect.md
# - flutter-implementation-coach.md
# - frontend-ux-specialist.md
# - learning-tutor.md
# - README.md
# - session-optimizer.md
# - tech-writer.md
```

---

## 🚀 Agent Usage Guide

### 1. Automation Sentinel (Meta-Agent)

**🔍 Use when:**
- Checking automation ecosystem health
- Finding redundant agents or commands
- Generating automation usage reports
- Detecting obsolete automations
- Getting optimization recommendations

**Example prompts:**
- "Check automation health"
- "Generate automation ecosystem report"
- "Are my agents redundant?"
- "Which automations are most valuable?"
- "Find unused commands"

**What you get:**
- Comprehensive health report (schema validation, dependency checks)
- Usage analytics dashboard (most/least used automations)
- Redundancy detection (overlapping responsibilities)
- Obsolescence warnings (unused automations)
- Optimization recommendations (consolidation, new automation ideas)

**Strengths:**
- Meta-level oversight (monitors all other agents)
- Data-driven recommendations
- Self-monitoring (validates its own schema)
- Non-destructive (recommends, never auto-deletes)

---

### 2. Pulse (Metrics Collection)

**⚡ Use when:**
- Automatically triggered before automation-sentinel (no manual invocation needed)
- Manually updating automation metrics
- Resetting metrics baseline

**Example prompts:**
- "Update automation metrics"
- "Collect usage data"
- "Reset metrics baseline"

**What you get:**
- Updated `.claude/metrics/usage-stats.toml` (TOML format, git-tracked)
- Delta report showing new activity since last run
- Consolidated lifetime totals (not just incremental changes)
- Fast execution (~30 seconds, ~500-1000 tokens using Haiku)

**Strengths:**
- Uses Haiku model (10x cheaper than Sonnet)
- Delta mode (50-80% token savings vs full history scans)
- Git-aware checkpoints (uses commit SHA for synchronization)
- Feeds data to automation-sentinel (separation of concerns)

**Integration with automation-sentinel:**
```
/create-pr workflow:
  1. pulse --mode=delta (collect metrics, update TOML)
  2. automation-sentinel --mode=delta (read TOML, analyze)

Result: 75% token reduction vs previous full-scan approach
```

---

### 3. Frontend/UX Specialist

**🎨 Use when:**
- Designing new screens
- Improving existing UI
- Making UX decisions
- Applying Nielsen's Heuristics
- Ensuring accessibility

**Example prompts:**
- "Design the wine review feed screen"
- "How should I layout the login page?"
- "This screen feels cluttered, help me improve it"
- "What's the best UX pattern for image upload?"
- "Make this screen more accessible"

**What you get:**
- ASCII wireframes showing layout
- Complete Flutter code with detailed comments
- Material Design 3 implementation
- Accessibility features (semantic labels, contrast ratios)
- UX best practices explained

**Strengths:**
- Compensates for your frontend weakness
- Teaches UI/UX principles while designing
- Ensures accessible, beautiful designs
- Explains every design decision

---

### 3. Flutter Implementation Coach

**💻 Use when:**
- Implementing Flutter features
- Working with Riverpod state management
- Integrating APIs with Dio
- Debugging Flutter-specific issues
- Writing tests for Flutter code

**Example prompts:**
- "Help me implement the review submission flow"
- "My Riverpod provider isn't updating the UI"
- "How do I connect this API to the widget?"
- "Debug this Dio request failure"
- "Implement review list with pagination"

**What you get:**
- Complete feature implementation (data → domain → presentation)
- Backend parallels (compares Flutter to Spring Boot)
- Error handling patterns
- Testing guidance
- Common pitfall warnings

**Strengths:**
- Bridges backend expertise to frontend
- Explains Flutter through Java/Spring analogies
- Systematic implementation approach
- Prevents common mistakes

---

### 4. Learning Tutor

**📖 Use when:**
- Learning new concepts
- Understanding patterns deeply
- Getting structured lessons
- Practicing with exercises
- Reviewing fundamentals

**Example prompts:**
- "Teach me Riverpod state management"
- "Explain the difference between StatelessWidget and StatefulWidget"
- "I want to understand async/await deeply"
- "Give me exercises to practice providers"
- "What's the best way to learn Flutter testing?"

**What you get:**
- Structured lessons (concept → example → exercise)
- Backend parallels (connects to Java/Spring knowledge)
- Hands-on exercises with solutions
- Spaced repetition for reinforcement
- Learning path recommendations

**Strengths:**
- Tailored to backend engineers
- Active learning (not just reading)
- Builds understanding, not just memorization
- Tracks progress, reinforces concepts

---

### 5. Session Optimizer

**⚡ Use when:**
- Starting a new work session
- Planning complex tasks
- Token usage is getting high
- Session feels unfocused
- Want to work more efficiently

**Example prompts:**
- "Help me plan this session efficiently"
- "I'm wasting tokens, how can I optimize?"
- "What's the best way to load context for this task?"
- "This conversation is getting too long"
- "Plan token-efficient implementation of feature X"

**What you get:**
- Session plan with token budget
- Smart file loading strategy
- Efficiency tactics (quiet mode, parallel calls)
- Warning signs for waste
- Recommended workflow

**Strengths:**
- Uses Haiku (fast, cheap)
- Reduces token waste 30-50%
- Keeps sessions focused
- Prevents scope creep

---

### 6. Cross-Project Architect

**🏗️ Use when:**
- Starting a new project
- Extracting reusable patterns
- Creating project templates
- Applying Wine Reviewer patterns elsewhere
- Building pattern library

**Example prompts:**
- "I want to start a new Spring Boot + Flutter project"
- "Extract the exception handling pattern for reuse"
- "How do I apply this architecture to an e-commerce app?"
- "Create a template from Wine Reviewer's test setup"
- "What patterns from this project are reusable?"

**What you get:**
- Pattern extraction analysis
- Reusable templates with customization points
- New project initialization guide
- Pattern documentation
- Template repository structure

**Strengths:**
- Builds from proven patterns
- Accelerates new project setup
- Creates consistency across projects
- Documents patterns systematically

---

### 7. Backend Code Reviewer

**🔍 Use when:**
- After implementing backend features
- Before committing significant changes
- Refactoring existing code
- Learning Spring Boot best practices
- Improving code quality

**Example prompts:**
- "Review my ReviewService implementation"
- "Check this controller for best practices"
- "I just implemented authentication, review it"
- "Analyze test coverage for my service layer"
- "Review this exception hierarchy design"

**What you get:**
- Comprehensive code review (10 dimensions)
- Strengths and issues identified
- Specific improvement recommendations
- Best practices explained
- Prioritized action items

**Strengths:**
- Enforces project conventions
- Checks OpenAPI documentation
- Validates testing patterns
- Security and performance analysis

---

### 8. Tech Writer

**📝 Use when:**
- Creating/updating documentation
- Adding Javadoc to classes
- Adding OpenAPI/Swagger annotations to endpoints
- Creating ADRs (Architecture Decision Records)
- Updating ROADMAP.md or LEARNINGS.md
- Writing README sections

**Example prompts:**
- "Add OpenAPI annotations to ReviewController"
- "Create ADR for the authentication flow decision"
- "Add comprehensive Javadoc to ReviewService"
- "Update LEARNINGS.md with today's session"
- "Document this endpoint with Swagger annotations"

**What you get:**
- Comprehensive external documentation (CLAUDE.md, README, LEARNINGS, ADRs)
- In-code documentation (Javadoc with @author, @date, examples)
- Complete OpenAPI/Swagger annotations (all HTTP status codes documented)
- Dartdoc for Flutter widgets
- Follows 4-part structure (GENERAL/BACKEND/FRONTEND/INFRASTRUCTURE) for CLAUDE.md/README.md, split files for CODING_STYLE

**Strengths:**
- Enforces OpenAPI documentation (CRITICAL for all REST endpoints)
- Maintains documentation consistency across project
- Integrates with backend-code-reviewer (fills doc gaps)
- Automatic trigger after REST endpoint creation

---

## 🎭 Agent Combinations (Workflows)

### Workflow 1: Feature Development (Frontend + Backend)

**Goal:** Implement complete feature end-to-end

**Steps:**
1. **Session Optimizer** - Plan token-efficient session
2. **Frontend UX Specialist** - Design UI/screens
3. **Flutter Implementation Coach** - Implement frontend
4. **Backend Code Reviewer** - Review backend API
5. **Session Optimizer** - Finish session efficiently

**Example:**
```
"Plan an efficient session to implement wine review submission"
→ Session Optimizer provides plan

"Design the review submission screen"
→ Frontend UX Specialist designs UI

"Implement the review submission flow with Riverpod"
→ Flutter Implementation Coach codes feature

"Review the ReviewController and ReviewService"
→ Backend Code Reviewer analyzes quality

"/finish-session review submission feature complete"
→ Session Optimizer guides completion
```

---

### Workflow 2: Learning + Practice

**Goal:** Learn new concept, practice, apply to project

**Steps:**
1. **Learning Tutor** - Teach concept with exercises
2. **Flutter Implementation Coach** - Apply to real feature
3. **Frontend UX Specialist** - Refine UI implementation

**Example:**
```
"Teach me Riverpod StateNotifier pattern"
→ Learning Tutor provides lesson + exercises

"Now implement review list using StateNotifier"
→ Flutter Implementation Coach guides implementation

"Improve the review list UI with better loading states"
→ Frontend UX Specialist enhances UX
```

---

### Workflow 3: New Project Setup

**Goal:** Start new project with proven patterns

**Steps:**
1. **Cross-Project Architect** - Extract patterns, create templates
2. **Session Optimizer** - Plan efficient initialization
3. **Backend Code Reviewer** - Review initial setup
4. **Frontend UX Specialist** - Design initial screens

**Example:**
```
"Extract reusable patterns from Wine Reviewer for new e-commerce project"
→ Cross-Project Architect analyzes and extracts

"Plan efficient session to initialize new project"
→ Session Optimizer provides setup plan

"Review the initial backend structure I copied"
→ Backend Code Reviewer validates setup

"Design the product catalog screen"
→ Frontend UX Specialist creates UI
```

---

## 💡 Best Practices

### 1. Let Agents Choose Automatically

**✅ Good:**
```
"I need to design the login screen with Google Sign-In"
```
Claude Code sees "design" + "screen" → invokes frontend-ux-specialist automatically

**❌ Less Efficient:**
```
"Use the frontend-ux-specialist agent to design the login screen"
```
Explicit invocation works but wastes tokens

**When to be explicit:**
- Agent selection is ambiguous
- You want specific agent's approach
- Testing agent behavior

---

### 2. Use Session Optimizer at Session Start

**Start every session:**
```
@ROADMAP.md

Quick session start via session-optimizer.

Goal: [specific goal]
Scope: [in/out]
```

**Benefits:**
- Minimal token load (ROADMAP.md only)
- Clear plan prevents waste
- Token budget awareness

---

### 3. Combine Agents for Complex Tasks

**Example: New feature from design to tests**
```
Step 1: "Design review detail screen" → frontend-ux-specialist
Step 2: "Implement screen with Riverpod" → flutter-implementation-coach
Step 3: "Review backend API" → backend-code-reviewer
Step 4: "Teach me testing patterns" → learning-tutor
```

---

### 4. Use Learning Tutor Before Implementation

**When stuck on concept:**
```
1. "Teach me AsyncValue in Riverpod" → learning-tutor
2. Do exercises offline
3. "Now implement review list with AsyncValue" → flutter-implementation-coach
```

**Benefits:**
- Understand before implementing
- Fewer mistakes
- More efficient implementation

---

### 5. Review Backend Code Proactively

**After implementing testable class:**
```
"Just finished ReviewService implementation"
→ backend-code-reviewer automatically invoked

OR

"/finish-session review service complete"
→ Runs tests, then suggests code review
```

---

## 🎨 Agent Personalities

### Automation Sentinel
- 🔍 **Style:** Vigilant watchdog, data analyst
- 💬 **Tone:** Analytical, metric-driven, proactive
- 🎯 **Focus:** Automation health + optimization + ROI
- 📏 **Output:** Health reports + metrics + recommendations

### Backend Code Reviewer
- 🔍 **Style:** Critical but constructive reviewer
- 💬 **Tone:** Professional, thorough, didactic
- 🎯 **Focus:** Quality + best practices + security
- 📏 **Output:** Analysis + recommendations + examples

### Cross-Project Architect
- 🏗️ **Style:** Pattern extracter, template builder
- 💬 **Tone:** Systematic, reuse-focused, organized
- 🎯 **Focus:** Scalability + consistency
- 📏 **Output:** Templates + patterns + guides

### Flutter Implementation Coach
- 💻 **Style:** Patient teacher, backend translator
- 💬 **Tone:** Systematic, comparative, thorough
- 🎯 **Focus:** Understanding + best practices
- 📏 **Output:** Complete features + backend parallels

### Frontend UX Specialist
- 🎨 **Style:** Designer who teaches
- 💬 **Tone:** Enthusiastic, visual, detailed
- 🎯 **Focus:** Beauty + accessibility + usability
- 📏 **Output:** Wireframes + code + explanations

### Learning Tutor
- 📚 **Style:** Educator using structured lessons
- 💬 **Tone:** Encouraging, methodical, engaging
- 🎯 **Focus:** Deep understanding + practice
- 📏 **Output:** Lessons + exercises + assessments

### Pulse
- ⚡ **Style:** Data collector, metric tracker
- 💬 **Tone:** Factual, concise, systematic
- 🎯 **Focus:** Accurate data collection + delta tracking
- 📏 **Output:** Updated metrics file (TOML) + delta reports

### Session Optimizer
- ⚡ **Style:** Efficiency expert, planner
- 💬 **Tone:** Concise, strategic, practical
- 🎯 **Focus:** Token savings + focused work
- 📏 **Output:** Plans + checklists + budgets

### Tech Writer
- 📝 **Style:** Documentation specialist, standards enforcer
- 💬 **Tone:** Precise, structured, comprehensive
- 🎯 **Focus:** Documentation completeness + consistency
- 📏 **Output:** ADRs + Javadoc + OpenAPI + READMEs

---

## 📊 Agent Selection Guide

**Use this decision tree:**

```
What's your task?
│
├─ Checking automation health / finding redundancy → automation-sentinel
│
├─ Collecting automation metrics (usually auto-triggered) → pulse
│
├─ Reviewing backend code → backend-code-reviewer
│
├─ New project / extracting patterns → cross-project-architect
│
├─ Coding Flutter feature → flutter-implementation-coach
│
├─ Designing UI/screens → frontend-ux-specialist
│
├─ Learning concept → learning-tutor
│
├─ Starting session / optimizing tokens → session-optimizer
│
└─ Creating docs / ADRs / Javadoc / OpenAPI → tech-writer
```

---

## 🚦 Status & Maintenance

### Current Status
- ✅ All 9 agents created (2025-11-11 update: added pulse)
- ✅ Tailored to your profile (backend expert, frontend novice)
- ✅ Integrated with project conventions (CLAUDE.md, CODING_STYLE_GENERAL.md + stack-specific files)
- ✅ Anti-cyclic dependency rule documented and enforced
- ✅ Alphabetically organized for easy navigation
- ✅ Ready to use

### Recent Updates (2025-11-11)
- ✅ **pulse** - Metrics collection agent (Haiku model, feeds automation-sentinel)
- ✅ **Stateful delta tracking** - `.claude/metrics/usage-stats.toml` with git-aware checkpoints
- ✅ **automation-sentinel refactored** - Now reads pre-collected metrics (50-80% token savings)
- ✅ **`/create-pr` updated** - Two-step workflow (pulse → sentinel) for efficiency

### Maintenance
- **Update agents** when project conventions change
- **Add new agents** as new needs emerge
- **Archive agents** if no longer useful
- **Run automation-sentinel** monthly to check ecosystem health
- **Share agents** with team or across projects

### Future Improvement Ideas
- Create agent for deployment/DevOps workflows
- Create agent for database migration management
- Implement automated usage tracking (metrics collection)
- Add agent chaining automation (backend-code-reviewer → tech-writer)

---

## 🛠️ Agent Creation Standards

**CRITICAL RULE: Front Matter Required for All Agents** *(Added 2025-10-31)*

When creating new custom agents in `.claude/agents/`, **always include YAML front matter** for Claude Code's agent discovery system:

### Required Front Matter Fields

```yaml
---
name: agent-name
description: Use this agent when [trigger conditions]. Examples - User: "[example 1]" → Use this agent. User: "[example 2]" → Use this agent.
model: sonnet|haiku|opus
color: purple|blue|cyan|yellow|etc
---
```

### Pre-Commit Quality Checklist

- [ ] Front matter exists with all required fields (`name`, `description`, `model`, `color`)
- [ ] Description includes clear trigger conditions with examples
- [ ] Agent body has required sections:
  - [ ] Purpose statement
  - [ ] Core Responsibilities
  - [ ] When to Trigger (Automatic + Manual)
  - [ ] Integration with other agents (if applicable)
  - [ ] Usage examples (minimum 2)
- [ ] Markdown syntax is valid (no broken links, proper headers)
- [ ] Code blocks have language specifiers
- [ ] Agent follows project conventions (CLAUDE.md, CODING_STYLE_GENERAL.md + stack-specific files)

### Why This Matters

- **Without front matter:** Agents can't be auto-triggered by Claude Code
- **Missing sections:** Reduces agent effectiveness and discoverability
- **Consistent structure:** Ensures maintainability across agent suite

### Validation

Before committing new agents, run `automation-sentinel` to validate schema and check for issues.

---

## 📝 Notes

**This agent suite was designed specifically for you:**
- Backend expert (Java/Spring Boot) → extensive backend parallels
- Frontend novice (Flutter/Dart) → detailed explanations, teaching approach
- Quality-focused → systematic patterns, best practices
- Token-conscious → session optimizer, efficient workflows
- Documentation-driven → integrates with your CLAUDE.md/ROADMAP.md/LEARNINGS.md system

**As you grow:**
- **Frontend expertise increases** → Agents can be more concise
- **New projects emerge** → Cross-project architect becomes more valuable
- **Patterns solidify** → Templates accelerate future work

**Remember:** Agents learn from your project context. Keep CLAUDE.md, CODING_STYLE_GENERAL.md, stack-specific CODING_STYLE files, and ROADMAP.md updated so agents have accurate information.

---

**Happy building! 🚀**
