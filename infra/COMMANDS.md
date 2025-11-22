# Infrastructure Commands

Comprehensive command reference for Wine Reviewer infrastructure (Docker, Testcontainers, CI/CD).

---

## Docker Compose Commands

### Start Services

```bash
# Start all services (PostgreSQL + API)
docker compose up -d --build

# Start all services with quiet pull
docker compose up -d --build --quiet-pull

# Start specific service only
docker compose up -d postgres
docker compose up -d api

# Start in foreground (see logs directly)
docker compose up --build
```

### Stop Services

```bash
# Stop all services (keeps containers and volumes)
docker compose stop

# Stop specific service
docker compose stop api
docker compose stop postgres

# Stop and remove containers (keeps volumes)
docker compose down

# Stop and remove containers + volumes (data loss!)
docker compose down -v

# Stop and remove containers + volumes + images
docker compose down -v --rmi all
```

### Service Management

```bash
# Restart services
docker compose restart

# Restart specific service
docker compose restart api
docker compose restart postgres

# Rebuild services without starting
docker compose build

# Force rebuild (ignore cache)
docker compose build --no-cache
```

---

## Logs and Monitoring

### View Logs

```bash
# View all logs
docker compose logs

# Follow logs (real-time)
docker compose logs -f

# View logs for specific service
docker compose logs api
docker compose logs postgres

# Follow logs with tail (last 50 lines)
docker compose logs -f --tail=50 api

# View logs since timestamp
docker compose logs --since 2025-01-01T00:00:00

# View logs with timestamps
docker compose logs -f --timestamps
```

### Service Status

```bash
# List running services
docker compose ps

# List all services (including stopped)
docker compose ps -a

# View service resource usage
docker stats

# View specific service resource usage
docker stats wine-reviewer-api wine-reviewer-postgres
```

---

## Database Management

### PostgreSQL Access

```bash
# Connect to PostgreSQL container
docker compose exec postgres psql -U winereviewer -d winereviewer

# Run SQL from command line
docker compose exec postgres psql -U winereviewer -d winereviewer -c "SELECT * FROM app_user;"

# Dump database
docker compose exec postgres pg_dump -U winereviewer winereviewer > backup.sql

# Restore database
docker compose exec -T postgres psql -U winereviewer winereviewer < backup.sql
```

### Database Troubleshooting

```bash
# Check PostgreSQL logs
docker compose logs postgres

# Restart PostgreSQL
docker compose restart postgres

# Reset database (drops all data!)
docker compose down -v
docker compose up -d postgres

# Connect and verify schema
docker compose exec postgres psql -U winereviewer -d winereviewer -c "\dt"
```

---

## Volume Management

### List Volumes

```bash
# List all volumes
docker volume ls

# List project volumes only
docker volume ls --filter name=wine-reviewer

# Inspect volume
docker volume inspect wine-reviewer_postgres-data
```

### Clean Volumes

```bash
# Remove specific volume (data loss!)
docker volume rm wine-reviewer_postgres-data

# Remove all unused volumes
docker volume prune

# Remove all unused volumes (no prompt)
docker volume prune -f
```

---

## Network Management

### Network Commands

```bash
# List networks
docker network ls

# Inspect network
docker network inspect wine-reviewer_default

# Test network connectivity between containers
docker compose exec api ping postgres
docker compose exec api nc -zv postgres 5432
```

---

## Clean Up Commands

### Remove Containers and Images

```bash
# Remove stopped containers
docker container prune

# Remove all unused images
docker image prune -a

# Remove all unused containers, networks, images (not volumes)
docker system prune

# Remove everything including volumes (nuclear option!)
docker system prune -a --volumes

# Force removal without prompt
docker system prune -f
```

### Project-Specific Cleanup

```bash
# Stop and remove project containers
docker compose down

# Stop and remove project containers + volumes
docker compose down -v

# Stop and remove project containers + volumes + images
docker compose down -v --rmi all
```

---

## Testcontainers Commands

### Run Integration Tests

```bash
# Run integration tests (from services/api/)
cd ../services/api
./mvnw test -Dtest=*IT

# Run all tests (unit + integration)
./mvnw verify

# Run specific integration test
./mvnw test -Dtest=ReviewControllerIT
```

### Testcontainers Cleanup

```bash
# List Testcontainers
docker ps -a --filter "label=org.testcontainers"

# Remove Testcontainers
docker rm -f $(docker ps -a -q --filter "label=org.testcontainers")

# Clean Testcontainers volumes
docker volume prune --filter "label=org.testcontainers"

# Full Testcontainers cleanup
docker system prune --filter "label=org.testcontainers" -f
```

### Troubleshooting Testcontainers

```bash
# Check Docker daemon status
docker ps

# Check Testcontainers logs (during test run)
# Logs appear in Maven output

# Enable Testcontainers debug logging
export TESTCONTAINERS_RYUK_DISABLED=false
./mvnw test -Dtest=*IT -Dorg.slf4j.simpleLogger.log.org.testcontainers=DEBUG

# Verify Testcontainers can access Docker
docker run hello-world
```

---

## CI/CD Commands

### GitHub Actions (Local Testing with act)

```bash
# Install act (GitHub Actions local runner)
# macOS: brew install act
# Windows: choco install act-cli
# Linux: curl https://raw.githubusercontent.com/nektos/act/master/install.sh | sudo bash

# List available workflows
act -l

# Run specific workflow
act -j build-api
act -j build-mobile

# Run workflow with secrets
act -s GITHUB_TOKEN=your_token

# Run workflow with specific event
act push
act pull_request
```

### Manual CI/CD Simulation

```bash
# Simulate API pipeline
cd services/api
./mvnw clean verify -q
docker build -t wine-reviewer-api:test .

# Simulate mobile pipeline
cd apps/mobile
flutter analyze
flutter test
flutter build apk --debug
```

---

## Health Checks

### Service Health

```bash
# Check API health endpoint
curl http://localhost:8080/actuator/health

# Check PostgreSQL health
docker compose exec postgres pg_isready -U winereviewer

# Check all services health
docker compose ps
```

### Port Checking

```bash
# Check if ports are in use
netstat -ano | findstr :8080  # Windows (API)
netstat -ano | findstr :5432  # Windows (PostgreSQL)

lsof -i :8080  # Linux/Mac (API)
lsof -i :5432  # Linux/Mac (PostgreSQL)

# Test port connectivity
telnet localhost 8080
nc -zv localhost 5432
```

---

## Performance Monitoring

### Resource Usage

```bash
# Monitor container resources
docker stats

# Monitor specific containers
docker stats wine-reviewer-api wine-reviewer-postgres

# Export metrics (Prometheus format)
curl http://localhost:8080/actuator/prometheus
```

### Database Performance

```bash
# Check active connections
docker compose exec postgres psql -U winereviewer -d winereviewer \
  -c "SELECT count(*) FROM pg_stat_activity;"

# Check slow queries
docker compose exec postgres psql -U winereviewer -d winereviewer \
  -c "SELECT query, state, query_start FROM pg_stat_activity WHERE state != 'idle';"

# Analyze table sizes
docker compose exec postgres psql -U winereviewer -d winereviewer \
  -c "SELECT schemaname, tablename, pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
      FROM pg_tables ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;"
```

---

## Backup and Restore

### Database Backup

```bash
# Backup database
docker compose exec postgres pg_dump -U winereviewer winereviewer > backup_$(date +%Y%m%d_%H%M%S).sql

# Backup with compression
docker compose exec postgres pg_dump -U winereviewer winereviewer | gzip > backup_$(date +%Y%m%d_%H%M%S).sql.gz

# Backup schema only
docker compose exec postgres pg_dump -U winereviewer --schema-only winereviewer > schema_backup.sql

# Backup data only
docker compose exec postgres pg_dump -U winereviewer --data-only winereviewer > data_backup.sql
```

### Database Restore

```bash
# Restore from backup
docker compose exec -T postgres psql -U winereviewer winereviewer < backup.sql

# Restore from compressed backup
gunzip -c backup.sql.gz | docker compose exec -T postgres psql -U winereviewer winereviewer

# Drop and recreate database before restore
docker compose exec postgres psql -U winereviewer -c "DROP DATABASE IF EXISTS winereviewer;"
docker compose exec postgres psql -U winereviewer -c "CREATE DATABASE winereviewer;"
docker compose exec -T postgres psql -U winereviewer winereviewer < backup.sql
```

---

## Troubleshooting

### Common Issues

**Issue:** "Port already in use"
```bash
# Find process using port
netstat -ano | findstr :8080  # Windows
lsof -i :8080                 # Linux/Mac

# Kill process
taskkill /PID <PID> /F        # Windows
kill -9 <PID>                 # Linux/Mac

# Or change port in docker-compose.yml
```

**Issue:** "Cannot connect to Docker daemon"
```bash
# Check Docker status
docker ps

# Start Docker Desktop (Windows/Mac)
# Or start Docker daemon (Linux)
sudo systemctl start docker

# Verify Docker is running
docker run hello-world
```

**Issue:** "Containers not starting"
```bash
# Check logs
docker compose logs

# Remove and recreate containers
docker compose down
docker compose up -d --build

# Nuclear option
docker compose down -v
docker system prune -a -f
docker compose up -d --build
```

**Issue:** "Out of disk space"
```bash
# Check Docker disk usage
docker system df

# Clean up unused resources
docker system prune -a --volumes -f

# Remove old images
docker image prune -a -f
```

---

## Environment Variables

### View Environment

```bash
# View environment variables for service
docker compose exec api env

# View specific variable
docker compose exec api printenv SPRING_DATASOURCE_URL
```

### Override Environment Variables

```bash
# Override via command line
docker compose up -e POSTGRES_PASSWORD=newpassword

# Override via .env file (create in infra/)
# DATABASE_URL=postgresql://user:pass@host:5432/db
docker compose up
```

---

## Useful Aliases (Optional)

Add to `.bashrc` or `.zshrc` for convenience:

```bash
# Docker Compose aliases
alias dc='docker compose'
alias dcup='docker compose up -d --build --quiet-pull'
alias dcdown='docker compose down'
alias dclogs='docker compose logs -f --tail=50'
alias dcps='docker compose ps'
alias dcrestart='docker compose restart'

# Cleanup aliases
alias docker-clean='docker system prune -f'
alias docker-nuke='docker system prune -a --volumes -f'
```

---

## Related Documentation

- **CODING_STYLE_INFRASTRUCTURE.md** - Docker/CI/CD conventions
- **README.md** - Infrastructure setup instructions
- **CLAUDE.md** - Project-wide guidelines
- **docker-compose.yml** - Service definitions
- **Docker Docs** - https://docs.docker.com/
