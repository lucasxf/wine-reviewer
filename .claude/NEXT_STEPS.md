# 🎯 Wine Reviewer - Next Steps & Roadmap

> Plano de próximos passos para o projeto Wine Reviewer
> **Última atualização:** 2025-10-21

---

## 📊 Status Atual (v0.1.0)

### ✅ Completo
- Backend API com Review CRUD completo
- Google OAuth authentication (AuthService, GoogleTokenValidator)
- Domain exception hierarchy com HTTP status mapping
- JWT authentication structure (JJWT 0.12.6)
- PostgreSQL + Flyway migrations
- Docker Compose setup
- OpenAPI/Swagger documentation
- **46 testes passando** (100% success rate)
- GitHub Actions CI/CD pipelines
- Documentation organized in 3 parts (General/Backend/Frontend)

### 🚧 Em Progresso
- Nada no momento

### 📍 Planejado (backlog)
- Integration tests com Testcontainers
- Image upload com pre-signed URLs
- Flutter mobile app
- Observability (metrics, tracing)

---

## 🎯 Immediate Next Steps (Priority Order)

### 1. **Integration Tests com Testcontainers** [HIGH PRIORITY]
**Por quê:** Garantir que endpoints funcionam com banco real (PostgreSQL)

**Tarefas:**
- [ ] Setup Testcontainers dependency no pom.xml
- [ ] Create base integration test class (AbstractIntegrationTest)
- [ ] Integration test: POST /api/auth/google
- [ ] Integration test: POST /api/reviews (create)
- [ ] Integration test: GET /api/reviews (list with pagination)
- [ ] Integration test: GET /api/reviews/{id}
- [ ] Integration test: PUT /api/reviews/{id} (update)
- [ ] Integration test: DELETE /api/reviews/{id}
- [ ] Integration test: Database constraints (rating 1-5, cascade delete)
- [ ] Integration test: Exception handling (404, 403, 400, 422)

**Estimativa:** 3-4 horas
**Arquivos afetados:**
- `pom.xml` (add testcontainers dependency)
- `src/test/java/com/winereviewer/api/integration/AbstractIntegrationTest.java` (new)
- `src/test/java/com/winereviewer/api/integration/ReviewControllerIT.java` (new)
- `src/test/java/com/winereviewer/api/integration/AuthControllerIT.java` (new)

---

### 2. **Image Upload com Pre-signed URLs** [HIGH PRIORITY]
**Por quê:** Feature core do MVP (usuários podem anexar fotos de vinhos)

**Decisões necessárias:**
- [ ] Storage provider: S3 Free Tier ou Supabase Storage?
- [ ] Max file size: 10MB?
- [ ] Allowed MIME types: image/jpeg, image/png, image/webp?

**Tarefas:**
- [ ] Choose storage provider (S3 vs Supabase)
- [ ] Add storage SDK dependency (AWS SDK ou Supabase client)
- [ ] Create ImageUploadService
- [ ] Endpoint: POST /api/images/presigned-url (generate upload URL)
- [ ] Endpoint: POST /api/reviews (update to accept imageUrl)
- [ ] Validation: file size, MIME type, URL format
- [ ] Tests: ImageUploadServiceTest (unit)
- [ ] Tests: ImageUploadControllerTest (integration)
- [ ] OpenAPI documentation for new endpoints
- [ ] Update Review entity to store imageUrl

**Estimativa:** 4-5 horas
**Arquivos afetados:**
- `pom.xml` (add storage SDK)
- `src/main/java/com/winereviewer/api/service/ImageUploadService.java` (new)
- `src/main/java/com/winereviewer/api/controller/ImageUploadController.java` (new)
- `src/main/java/com/winereviewer/api/config/StorageProperties.java` (new)
- `application.yml` (storage config)
- Tests (new)

---

### 3. **Comment System - Full CRUD** [MEDIUM PRIORITY]
**Por quê:** Engagement e interação entre usuários

**Tarefas:**
- [ ] Endpoint: POST /api/reviews/{reviewId}/comments (create)
- [ ] Endpoint: GET /api/reviews/{reviewId}/comments (list)
- [ ] Endpoint: PUT /api/comments/{id} (update)
- [ ] Endpoint: DELETE /api/comments/{id} (delete)
- [ ] Validation: comment text (min 1, max 500 chars)
- [ ] Authorization: only owner can edit/delete
- [ ] Tests: CommentServiceTest (20+ tests)
- [ ] Tests: CommentControllerTest (integration)
- [ ] OpenAPI documentation
- [ ] Update ReviewResponse to include comment count

**Estimativa:** 3-4 horas
**Arquivos afetados:**
- `CommentController.java` (expand)
- `CommentService.java` (implement full CRUD)
- `CommentServiceTest.java` (new)
- Tests (expand)

---

### 4. **Observability - Metrics & Tracing** [MEDIUM PRIORITY]
**Por quê:** Monitorar performance e debugar problemas em produção

**Tarefas:**
- [ ] Add Micrometer dependency
- [ ] Configure Prometheus metrics endpoint
- [ ] Add custom metrics: review_creation_count, auth_success/failure
- [ ] Configure OpenTelemetry for distributed tracing
- [ ] Export traces to Grafana Cloud Free (ou CloudWatch)
- [ ] Add structured JSON logging (Logback configuration)
- [ ] Dashboard básico no Grafana Cloud

**Estimativa:** 4-5 horas
**Arquivos afetados:**
- `pom.xml` (micrometer, opentelemetry)
- `src/main/resources/logback-spring.xml` (new)
- `application.yml` (metrics config)
- Grafana Cloud dashboard (external)

---

### 5. **Flutter Mobile App - MVP** [HIGH PRIORITY - F2 Phase]
**Por quê:** Interface principal do usuário

**Decisões necessárias:**
- [ ] State management: Riverpod ou Bloc? (recomendo Riverpod)
- [ ] API base URL: localhost:8080 (dev) + production URL

**Tarefas:**
- [ ] Initialize Flutter project (`flutter create apps/mobile`)
- [ ] Setup Riverpod for state management
- [ ] Setup go_router for navigation
- [ ] Setup dio for HTTP client
- [ ] Create AuthProvider (Google Sign-In)
- [ ] Screen: Login (Google OAuth)
- [ ] Screen: Review Feed (list reviews)
- [ ] Screen: Review Details (show review + comments)
- [ ] Screen: New Review (create review with photo)
- [ ] Widget tests for all screens
- [ ] Integration with backend API
- [ ] Error handling (dio interceptors)

**Estimativa:** 10-12 horas
**Arquivos afetados:**
- `apps/mobile/` (novo diretório completo)
- `apps/mobile/lib/features/auth/` (new)
- `apps/mobile/lib/features/reviews/` (new)
- `apps/mobile/lib/core/` (new)

---

### 6. **Deployment Pipeline - Free Tier** [LOW PRIORITY]
**Por quê:** Deploy MVP em produção (100% free)

**Decisões necessárias:**
- [ ] Backend host: Render Free, Railway Free, ou EC2 Free Tier?
- [ ] Database: Supabase Free ou AWS RDS Free Tier?
- [ ] Storage: S3 Free Tier ou Supabase Storage?

**Tarefas:**
- [ ] Create Dockerfile (multi-stage build)
- [ ] Create docker-compose.prod.yml
- [ ] Setup GitHub Actions deployment workflow
- [ ] Configure environment variables (secrets)
- [ ] Deploy backend to chosen provider
- [ ] Setup PostgreSQL database (managed service)
- [ ] Configure CORS for production domain
- [ ] Setup SSL/TLS (Let's Encrypt)
- [ ] Configure health checks
- [ ] Setup monitoring alerts

**Estimativa:** 6-8 horas

---

## 🔮 Future Enhancements (Post-MVP)

### 7. **User Follow System**
- Follow/unfollow users
- Feed personalizado (reviews de users seguidos)
- Notificações de novos reviews

### 8. **Wine Recommendations**
- Algoritmo baseado em ratings e preferências
- "Vinhos similares" baseado em country/grape/winery
- Machine learning (futuro distante)

### 9. **Internationalization (i18n)**
- Suporte para múltiplos idiomas
- PT-BR, EN, ES
- Localização de datas e moedas

### 10. **Advanced Features**
- Wine barcode scanner
- Integration com APIs de vinhos (Vivino, Wine.com)
- Ranking de vinhos por região/tipo
- Export de dados (PDF, CSV)

---

## 📅 Timeline Sugerido

### Sprint 1 (Semana 1-2)
- ✅ Setup projeto (completo)
- ✅ Backend API CRUD (completo)
- ✅ Google OAuth (completo)
- ✅ Tests (completo)
- 🚧 Integration Tests com Testcontainers

### Sprint 2 (Semana 3-4)
- Image Upload com Pre-signed URLs
- Comment System completo
- Observability básica

### Sprint 3 (Semana 5-8)
- Flutter Mobile App MVP
- Integration com backend
- Testes mobile

### Sprint 4 (Semana 9-10)
- Deployment pipeline
- Production deploy
- Monitoring setup

### Sprint 5+ (Post-MVP)
- User follow system
- Recommendations
- i18n
- Advanced features

---

## 🎓 Learnings & Improvements

### Documentation
- ✅ Organized in 3 parts (General/Backend/Frontend) for reusability
- ✅ EFFICIENCY.md created with token-saving strategies
- ✅ Clear separation of concerns in docs

### Code Quality
- ✅ Domain exception hierarchy bem definida
- ✅ 46 testes com 100% success rate
- ✅ OpenAPI/Swagger documentation
- ✅ Code conventions bem documentadas

### DevOps
- ✅ GitHub Actions CI/CD
- ✅ Docker Compose para desenvolvimento
- 🚧 Deployment pipeline (pending)

---

## ✅ Definition of Done

Para cada task ser considerada completa:
- [ ] Código implementado seguindo CODING_STYLE.md
- [ ] Testes criados (unit + integration quando aplicável)
- [ ] Testes passando (100% success)
- [ ] OpenAPI documentation atualizada (se endpoint novo)
- [ ] README.md atualizado (se feature visível)
- [ ] CLAUDE.md "Next Steps" atualizado
- [ ] Build completo sem erros (`./mvnw clean verify -q`)
- [ ] Commit com mensagem clara
- [ ] Push para remote

---

## 🚀 How to Pick Next Task

1. Check "Immediate Next Steps" (prioridade)
2. Considere dependências (ex: testes integration antes de deploy)
3. Estime tempo disponível
4. Use TodoWrite para planejar antes de executar
5. Execute com comandos otimizados (use `#`, agrupe tasks)

---

**Próxima atualização:** Após completar cada sprint/task
**Responsável:** Manter este arquivo atualizado ao final de cada sessão
