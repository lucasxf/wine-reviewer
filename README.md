# 🍷 Avaliador de Vinhos — Monorepo

> **Diretrizes principais:** qualidade > velocidade; todo o **MVP** deve usar **hospedagem gratuita**; código-fonte em **inglês**.

## 📦 Estrutura
```
wine-reviewer/
├─ apps/
│  └─ mobile/                 # Flutter app
├─ services/
│  └─ api/                    # Spring Boot (Java 21)
├─ infra/                     # Docker compose, IaC, scripts
├─ prompts/                   # Prompt Pack e agentes
├─ ADRs/                      # Architecture Decision Records
└─ .github/workflows/         # GitHub Actions (CI/CD)
```

## 🚀 Objetivo do MVP
- App mobile Flutter (Android primeiro).
- Backend Spring Boot 3 (Java 21) + Postgres.
- Login com **Google** na **Fase 1**.
- Upload opcional de fotos (pre-signed URL).
- Observabilidade básica (logs, métricas, tracing).
- CI/CD com GitHub Actions.
- **Hospedagem 100% gratuita** no MVP.

## 🔧 Requisitos locais mínimos
- Flutter 3.x
- Java 21 + Maven/Gradle
- Docker + Docker Compose
- Git

## 🧭 Como começar
1. Leia `prompts/PACK.md` para o **Prompt Principal (v2.2 Final)**.
2. Siga `apps/mobile/README.md` para setup do Flutter.
3. Siga `services/api/README.md` para a API (inclui Flyway e Testcontainers).
4. Para executar local: `infra/docker-compose.yml`.
5. CI/CD: ver `.github/workflows/*`.

## 🧪 Qualidade
- Lint, testes unitários e integração (Testcontainers).
- Logs estruturados e métricas HTTP p95.
- Checklists de aceitação por etapa.

## 📄 Licenças e ativos
- Utilize apenas ícones/imagens com licença livre (OpenMoji, Undraw, etc.).

---
**Nota:** sempre priorizar **qualidade sobre velocidade**; respostas e decisões podem levar o tempo necessário para garantir precisão e clareza.
