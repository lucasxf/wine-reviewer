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
- **Test-After-Implementation** - Sempre criar testes imediatamente após implementar classe testável

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

- **2025-11-10** - Split CODING_STYLE.md into stack-specific files for optimized session context loading
- **2025-10-22 (v6)** - Adicionada PART 4: INFRASTRUCTURE com padrões de Testcontainers, Docker e CI/CD
- **2025-10-21 (v5)** - Adicionada regra crítica de organização de documentação (estrutura 3 partes)
- **2025-10-21 (v4)** - Reestruturado em 3 partes (GENERAL/BACKEND/FRONTEND)

