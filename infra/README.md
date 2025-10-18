# 🏗️ Infraestrutura (Local & Deploy Free)

> Somente recursos gratuitos no MVP. Compose para desenvolvimento local.

## ▶️ Docker Compose (dev)
- `postgres`: banco de dados
- `api`: serviço Spring Boot (build local)

### Executar
```bash
docker compose up -d --build
docker compose logs -f
```

### Parar
```bash
docker compose down -v
```

## ☁️ Deploy Free (guia)
- EC2 Free Tier **ou** Render Free (alternativa).
- Storage de imagens: S3 Free **ou** Supabase Storage Free.
- Observabilidade: Grafana Cloud Free **ou** CloudWatch (limites Free).
