# Coding Style Guide - General (Cross-Stack)

> Universal coding standards applicable to any language/framework.
> **Part of:** Wine Reviewer Project
> **Applies to:** All stacks (Backend, Frontend, Infrastructure)

---

## 🎯 Princípios Gerais

- **Qualidade sobre velocidade** - Tomar o tempo necessário para fazer certo
- **Código em inglês** - Nomes de classes, métodos, variáveis sempre em inglês
- **Comentários e logs podem ser em português** - Documentação e mensagens de log
- **Separation of Concerns** - Clara divisão entre camadas
- **Test-Driven Development (TDD)** - Sempre seguir ciclo Red-Green-Refactor ao criar novas features

---

## 🧪 Testing Standards (Universal)

### Test-Driven Development (TDD) Workflow

**CRITICAL RULE:** ALWAYS follow TDD workflow when implementing new features.

**The TDD Cycle (Red-Green-Refactor):**

1. **RED** - Write a failing test first
   - Define expected behavior BEFORE implementation
   - Test should fail because feature doesn't exist yet
   - Clarifies requirements and API design

2. **GREEN** - Write minimal code to make test pass
   - Implement simplest solution that passes test
   - Don't worry about perfection yet
   - Goal: Get to green quickly

3. **REFACTOR** - Clean up code and tests
   - **CRITICAL:** Don't skip this step (most common TDD failure)
   - Remove duplication, improve naming, optimize
   - Tests should still pass after refactoring
   - If tests break during refactor, analyze for code smells FIRST

**When Tests Keep Breaking - Critical Analysis Pattern:**

If tests consistently break during refactoring or minor changes, **STOP** and analyze for code smells BEFORE adjusting tests:

**Common Code Smells Indicating Brittle Tests:**
- **Tight Coupling:** Implementation details leaking into tests
- **God Objects:** Classes doing too much, tests test multiple concerns
- **Fragile Base Class:** Inheritance hierarchies causing cascading test failures
- **Primitive Obsession:** Using primitives instead of domain objects
- **Shotgun Surgery:** Changing one feature breaks tests in multiple places

**Resolution Process:**
1. **Identify Smell:** What design issue is causing test brittleness?
2. **Refactor Code:** Fix underlying design problem (not tests)
3. **Update Tests:** Only after design is improved
4. **Validate:** Tests should be more stable and maintainable

### Behavior-Driven Development (BDD) - Given/When/Then Structure

**CRITICAL RULE:** ALL tests must follow Given/When/Then structure for clarity and consistency.

**The Given/When/Then Pattern:**

- **Given (Arrange):** Set up preconditions and initial state
  - Create test data, mock dependencies, configure system
  - Establish context for the behavior being tested

- **When (Act):** Execute the specific behavior under test
  - Call the method, trigger the event, send the request
  - Single action being tested (not multiple actions)

- **Then (Assert):** Verify expected outcome
  - Assert results, check state changes, verify interactions
  - Multiple assertions OK if testing single behavior

**Parallel Patterns:**
- **Arrange-Act-Assert (AAA):** Same concept, different naming
- **Setup-Exercise-Verify-Teardown:** Extended version with cleanup

**Test Naming Convention:**

**Format:** `should[ExpectedBehavior]When[StateUnderTest]`

**Examples:**
- `shouldCreateReviewWhenValidDataProvided`
- `shouldThrowExceptionWhenRatingOutOfRange`
- `shouldReturnEmptyListWhenNoReviewsExist`
- `shouldUpdateReviewWhenUserIsOwner`
- `shouldReturn403WhenUserIsNotOwner`

**Benefits:**
- Clear, self-documenting test names
- Behavior-focused (not implementation-focused)
- Business language (readable by non-developers)
- Easy to identify what's being tested

### Test Coverage Strategy

**CRITICAL RULE:** Immediately create tests after implementing testable classes.

**Testable Classes (MUST have tests):**
- ✅ Services (business logic)
- ✅ Repositories (data access)
- ✅ Controllers (API endpoints)
- ✅ Utilities (helper functions)
- ✅ Domain logic (aggregates, commands, events)

**Non-Testable Classes (SKIP tests):**
- ❌ Configuration classes (@Configuration, @ConfigurationProperties)
- ❌ Simple DTOs (no logic, just data)
- ❌ Entities (unless complex domain logic)

**Coverage Goals:**
- Critical paths: 100% (authentication, payments, data integrity)
- Business logic: 90%+ (services, domain layer)
- Controllers: 80%+ (happy path + error cases)
- Overall: 70%+ minimum

**Workflow:**
1. Write failing test (RED)
2. Implement class/method (GREEN)
3. Refactor (REFACTOR)
4. Run tests, verify pass
5. Commit code + tests together
6. **Never defer test writing to "later"**

### Test Organization

**File Naming:**
- Unit tests: `ClassNameTest.java` or `class_name_test.dart`
- Integration tests: `ClassNameIT.java` (backend only)
- Widget tests: `widget_name_test.dart` (frontend only)

**Location:**
- Mirror production code structure under `src/test/`
- Example: `src/main/java/com/app/service/ReviewService.java`
- Test: `src/test/java/com/app/service/ReviewServiceTest.java`

**Test Class Structure:**
```
- Setup methods (@BeforeEach, @BeforeAll, setUp())
- Happy path tests (successful scenarios)
- Error path tests (failures, exceptions)
- Edge case tests (boundaries, null, empty)
- Helper methods (test data creation)
```

---

## 📋 Nomenclatura Universal

- **Classes:** PascalCase - `AccountAggregate`, `CreateAccountHandler`, `ReviewService`
- **Métodos:** camelCase - `createAccount()`, `validateCommand()`, `getUserById()`
- **Variáveis:** camelCase - `correlationId`, `accountStream`, `userId`
- **Constantes:** UPPER_SNAKE_CASE - `EMAIL_PATTERN`, `MAX_RETRY_ATTEMPTS`, `ONE_HOUR_MS`
- **Pacotes (Java) / Módulos (Dart):** lowercase - `subscriptions_billing`, `domain.account`, `features/auth`
- **Números grandes:** SEMPRE usar underscore para separar milhares - `3_600_000` (não `3600000`)

### Exemplo de Números com Agrupamento

```java
// ✅ CORRETO - Legível
private static final long ONE_HOUR_MS = 3_600_000L;
private static final long ONE_DAY_MS = 86_400_000L;
private static final int MAX_FILE_SIZE = 10_000_000;  // 10 MB

// ❌ INCORRETO - Difícil de ler
private static final long ONE_HOUR_MS = 3600000L;
private static final long ONE_DAY_MS = 86400000L;
private static final int MAX_FILE_SIZE = 10000000;
```

## 📚 Documentação Viva

### Princípio de Documentação Contínua

**REGRA:** A documentação deve ser atualizada ao final de cada sessão de desenvolvimento.

**REGRA CRÍTICA: Organização de Documentação (Estrutura 3 Partes)**

Todos os arquivos principais de documentação (`CLAUDE.md`, `CODING_STYLE.md`, `README.md`) **devem** ser organizados em 3 partes:
1. **PART 1: GENERAL** - Guidelines cross-stack, visão geral, regras universais
2. **PART 2: BACKEND** - Específico de backend (Java/Spring Boot): setup, convenções, testes
3. **PART 3: FRONTEND** - Específico de frontend (Flutter/Dart): setup, convenções, testes

**Benefícios:**
- ✅ **Reutilizável**: Copiar apenas seções relevantes para novos projetos (backend-only, frontend-only, fullstack)
- ✅ **Organizado**: Sem mistura de guidelines de stacks diferentes
- ✅ **Escalável**: Fácil adicionar novas seções (PART 4: BFF, PART 5: Infraestrutura, etc.)
- ✅ **Claro**: Cada seção tem delimitadores claros e instruções de uso

### Terminology Consistency in Quantitative Documentation *(Added 2025-11-18)*

**CRITICAL RULE:** Avoid contradictory or ambiguous terms in metrics documentation.

**Example of Contradictory Terms:**
- ❌ **INCORRECT:** "net LOCs added" (contradictory - "net" implies subtraction, "added" implies addition)
- ✅ **CORRECT:** "net LOCs" (added - deleted) OR "total LOCs added" (gross addition without subtraction)

**Example of Ambiguous Terms:**
- ❌ **INCORRECT:** "test ratio" (ambiguous - could mean test_LOCs/total_LOCs OR test_LOCs/production_LOCs)
- ✅ **CORRECT:** "Test Coverage Ratio = test_LOCs / (production_LOCs + test_LOCs)" (explicit formula)

**Guidelines:**
- Define all quantitative metrics with explicit formulas
- Use "net" only when subtraction is involved (net = gross - deductions)
- Use "total" or "gross" for raw sums without subtraction
- When multiple interpretations exist, provide formula in parentheses
- Review all metrics documentation for terminology conflicts before publication

**Why:**
- Prevents reader confusion and misinterpretation of data
- Ensures reproducibility (readers can verify calculations)
- Maintains credibility in technical writing
- Critical for articles, research papers, and audit documentation

### Correction Propagation in Documentation *(Added 2025-11-18)*

**CRITICAL RULE:** When documenting errors found in published content, explicitly list ALL affected files that need correction.

**Bad Example (Vague):**
```markdown
## Error Found
The ROI calculation was incorrect (57% should be 47%). This needs to be fixed in the article.
```

**Good Example (Explicit Checklist):**
```markdown
## Error Found
The ROI calculation was incorrect. **Correction required:**

**Incorrect value:** "57% of savings from 2 automations"
**Correct value:** "47% of savings from 3 automations"

**Files requiring correction:**
1. ✅ `.claude/metrics/article-2-key-insights-summary.md` (Line 83) - ✅ FIXED
2. ⏳ `.claude/metrics/article-2-metrics-update-2025-11-12.md` (Table 5, Line 336-339)
3. ⏳ Any published articles referencing "57%" figure
4. ⏳ Presentation slides or summary documents

**Next action:** Search codebase for "57%" to find all occurrences.
```

**Guidelines:**
- Provide exact file paths (not just "the article" or "the docs")
- Include line numbers or section names when possible
- Use checkboxes (✅/⏳) to track correction progress
- List ALL potential locations (articles, slides, summaries, related docs)
- Suggest search patterns to find other occurrences
- Don't assume you found all instances - encourage verification

**Why:**
- Prevents corrections from being missed in derived documents
- Published articles/presentations may have copied incorrect data
- Creates accountability trail for error correction
- Ensures consistency across all documentation
- Critical for maintaining credibility after publishing errors

### Arquivos a atualizar após mudanças significativas

1. **`CLAUDE.md`** - Sempre atualizar com novas diretrizes, decisões arquiteturais e aprendizados
   - **CRITICAL:** Atualizar seção "Next Steps (Roadmap)" - mover itens completos para "Implemented", adicionar novos próximos passos
   - **Estrutura:** 3 partes (General/Backend/Frontend)
2. **`CODING_STYLE.md`** (este arquivo) - Sempre atualizar com novos padrões de código identificados
   - **Estrutura:** 3 partes (General/Backend/Frontend)
3. **`README.md`** - Atualizar quando o estado da aplicação mudar (novas features, endpoints, configurações)
   - **Estrutura:** 3 partes (General/Backend/Frontend)
4. **OpenAPI/Swagger (Backend)** - Atualizar anotações nos controllers sempre que criar/modificar endpoints REST

**O que caracteriza mudança significativa:**
- ✅ Novas features implementadas
- ✅ Novos endpoints REST criados/modificados
- ✅ Mudanças arquiteturais (novos padrões, exceções, estruturas)
- ✅ Novas convenções de código identificadas
- ✅ Atualizações de dependências importantes
- ❌ Minor bug fixes ou refactorings (unless they establish new patterns)

**Formato de atualização:**
- Sempre incluir data da atualização
- Descrever brevemente o que foi adicionado/modificado
- Manter histórico de mudanças relevantes
- **Atualizar "Next Steps (Roadmap)" em CLAUDE.md:**
  - Mover tasks completadas para "Current Implementation Status"
  - Adicionar novos próximos passos baseados no progresso
  - Manter priorização clara (1, 2, 3, 4...)
  - Ajuda na carga de contexto ao início de cada nova sessão

---

## 🔄 Stack-Specific Coding Styles

This file contains **universal cross-stack guidelines**. For stack-specific conventions, see:

- **Backend (Java/Spring Boot):** `services/api/CODING_STYLE_BACKEND.md`
- **Frontend (Flutter/Dart):** `apps/mobile/CODING_STYLE_FRONTEND.md`
- **Infrastructure (Docker/CI/CD):** `infra/CODING_STYLE_INFRASTRUCTURE.md`

---

## 🔄 Histórico de Atualizações

- **2025-11-11** - Added comprehensive TDD + BDD testing standards (Red-Green-Refactor cycle, Given/When/Then structure, code smell analysis)
- **2025-11-10** - Split CODING_STYLE.md into stack-specific files for optimized session context loading
- **2025-10-22 (v6)** - Adicionada PART 4: INFRASTRUCTURE com padrões de Testcontainers, Docker e CI/CD
- **2025-10-21 (v5)** - Adicionada regra crítica de organização de documentação (estrutura 3 partes)
- **2025-10-21 (v4)** - Reestruturado em 3 partes (GENERAL/BACKEND/FRONTEND)

