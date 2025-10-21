---
description: Start all Docker services (PostgreSQL + API)
---

Start all services with Docker Compose

```bash
cd infra && docker compose up -d --build
```

View logs:
```bash
docker compose logs -f --tail=50 api
```
