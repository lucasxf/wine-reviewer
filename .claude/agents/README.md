# Custom Agent Suite - Wine Reviewer Project

> **Purpose:** Specialized agents to accelerate development, learning, and project efficiency.
> **Created:** 2025-10-28
> **Developer:** Backend engineer (Java/Spring Boot expert) learning Flutter/Dart

---

## 📚 Agent Overview

This project includes **6 custom agents** designed to address specific needs:

| Agent | Purpose | Model | When to Use |
|-------|---------|-------|-------------|
| **frontend-ux-specialist** | UI/UX design, screen layouts, Material Design | Sonnet | Designing screens, improving UX, accessibility |
| **flutter-implementation-coach** | Flutter coding, Riverpod, Dio, debugging | Sonnet | Implementing features, state management, API integration |
| **learning-tutor** | Teaching concepts, structured learning, exercises | Sonnet | Learning new topics, understanding patterns |
| **session-optimizer** | Token efficiency, session planning, workflow | Haiku | Starting sessions, optimizing token usage |
| **cross-project-architect** | Pattern extraction, templates, new projects | Sonnet | Starting new projects, extracting reusable patterns |
| **backend-code-reviewer** | Java/Spring Boot code review, best practices | Sonnet | Reviewing backend code after implementation |

---

## 🎯 Quick Start

### How Agents Work

1. **Automatic Invocation** - Claude Code automatically selects agents based on your message
2. **Explicit Invocation** - You can request specific agents: "Use the frontend-ux-specialist to design this screen"
3. **Complementary** - Multiple agents can work together (e.g., UX specialist designs, implementation coach codes)

### First Time Setup

Agents are already configured in `.claude/agents/`. No additional setup needed!

```bash
# Verify agents are available
ls .claude/agents/

# Should see:
# - backend-code-reviewer.md
# - frontend-ux-specialist.md
# - flutter-implementation-coach.md
# - learning-tutor.md
# - session-optimizer.md
# - cross-project-architect.md
```

---

## 🚀 Agent Usage Guide

### 1. Frontend/UX Specialist

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

### 2. Flutter Implementation Coach

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

### 3. Learning Tutor

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

### 4. Session Optimizer

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

### 5. Cross-Project Architect

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

### 6. Backend Code Reviewer

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

### Frontend UX Specialist
- 🎨 **Style:** Designer who teaches
- 💬 **Tone:** Enthusiastic, visual, detailed
- 🎯 **Focus:** Beauty + accessibility + usability
- 📏 **Output:** Wireframes + code + explanations

### Flutter Implementation Coach
- 💻 **Style:** Patient teacher, backend translator
- 💬 **Tone:** Systematic, comparative, thorough
- 🎯 **Focus:** Understanding + best practices
- 📏 **Output:** Complete features + backend parallels

### Learning Tutor
- 📚 **Style:** Educator using structured lessons
- 💬 **Tone:** Encouraging, methodical, engaging
- 🎯 **Focus:** Deep understanding + practice
- 📏 **Output:** Lessons + exercises + assessments

### Session Optimizer
- ⚡ **Style:** Efficiency expert, planner
- 💬 **Tone:** Concise, strategic, practical
- 🎯 **Focus:** Token savings + focused work
- 📏 **Output:** Plans + checklists + budgets

### Cross-Project Architect
- 🏗️ **Style:** Pattern extracter, template builder
- 💬 **Tone:** Systematic, reuse-focused, organized
- 🎯 **Focus:** Scalability + consistency
- 📏 **Output:** Templates + patterns + guides

### Backend Code Reviewer
- 🔍 **Style:** Critical but constructive reviewer
- 💬 **Tone:** Professional, thorough, didactic
- 🎯 **Focus:** Quality + best practices + security
- 📏 **Output:** Analysis + recommendations + examples

---

## 📊 Agent Selection Guide

**Use this decision tree:**

```
What's your task?
│
├─ Designing UI/screens → frontend-ux-specialist
│
├─ Coding Flutter feature → flutter-implementation-coach
│
├─ Learning concept → learning-tutor
│
├─ Starting session / optimizing tokens → session-optimizer
│
├─ New project / extracting patterns → cross-project-architect
│
└─ Reviewing backend code → backend-code-reviewer
```

---

## 🚦 Status & Maintenance

### Current Status
- ✅ All 6 agents created
- ✅ Tailored to your profile (backend expert, frontend novice)
- ✅ Integrated with project conventions (CLAUDE.md, CODING_STYLE.md)
- ✅ Ready to use

### Maintenance
- **Update agents** when project conventions change
- **Add new agents** as new needs emerge
- **Archive agents** if no longer useful
- **Share agents** with team or across projects

### Improvement Ideas
- Add metrics tracking (which agents most useful?)
- Create agent for API design (OpenAPI/Swagger focus)
- Create agent for deployment/DevOps
- Create agent for writing documentation

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

**Remember:** Agents learn from your project context. Keep CLAUDE.md, CODING_STYLE.md, and ROADMAP.md updated so agents have accurate information.

---

**Happy building! 🚀**
