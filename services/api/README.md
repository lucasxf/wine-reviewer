# 🔌 API Service (Spring Boot 3 / Java 21)

> Código-fonte em **inglês**. Qualidade > velocidade. **MVP gratuito**: bancos/armazenamento e observabilidade em camadas free.

## ✅ Objetivos
- Endpoints para `Review` e `Comment` (MVP) e `Wine`/`User`.
- Autenticação: **Google OAuth/OpenID** + emissão de JWT curto + refresh.
- Persistência: **PostgreSQL** + **Flyway** (migrações).
- Testes de integração com **Testcontainers**.
- OpenAPI com `springdoc-openapi`.

## ▶️ Rodando local
Com Docker (via compose):
```bash
docker compose -f ../infra/docker-compose.yml up -d --build
```

Sem Docker (DB local):
```bash
./mvnw spring-boot:run
```

## 🧪 Testes
```bash
./mvnw -q -DskipTests=false verify
```

## 🗄️ Banco de dados
- Postgres com schema inicial via Flyway (V1__init.sql).
- Índices em colunas de ordenação/pesquisa (e.g., `created_at`).

## 🔒 Segurança
- Tokens curtos, CORS restrito, validação de payload.
- Pre-signed URL para upload de fotos (S3/Supabase Storage).

## 📑 OpenAPI
- Acesse `/swagger-ui.html` (ou caminho gerado pelo springdoc).

## 📈 Observabilidade
- OpenTelemetry OTLP → Grafana Cloud Free / CloudWatch Free Tier.
