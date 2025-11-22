# Backend API Commands

Comprehensive command reference for the Wine Reviewer backend API (Spring Boot + Java 21).

---

## Development Commands

### Run Application

```bash
# Run locally with default profile (requires PostgreSQL running)
./mvnw spring-boot:run

# Run with specific profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Run with debug mode enabled (port 5005)
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
```

**Access points when running:**
- API: `http://localhost:8080`
- OpenAPI/Swagger UI: `http://localhost:8080/swagger-ui.html`
- API Docs (JSON): `http://localhost:8080/v3/api-docs`

---

## Testing Commands

### Run All Tests

```bash
# Run all tests (unit + integration) - quiet mode
./mvnw verify -q

# Run all tests with full output
./mvnw verify

# Run all tests and skip build
./mvnw test

# Clean and run all tests
./mvnw clean verify -q
```

### Run Specific Test Types

```bash
# Run only unit tests (excludes *IT.java)
./mvnw test -q

# Run only integration tests (*IT.java)
./mvnw test -q -Dtest=*IT

# Run specific test class
./mvnw test -q -Dtest=ReviewServiceTest

# Run specific test method
./mvnw test -q -Dtest=ReviewServiceTest#shouldCreateReviewWhenValidDataProvided
```

### Test Coverage

```bash
# Run tests with coverage report (JaCoCo)
./mvnw verify

# View coverage report (after running tests)
# Open: target/site/jacoco/index.html
```

---

## Build Commands

### Build Application

```bash
# Build without running tests (quick mode)
./mvnw clean install -q -DskipTests

# Build with tests (quiet mode)
./mvnw clean install -q

# Build with full output
./mvnw clean install

# Build JAR only (skip install to local Maven repo)
./mvnw clean package -q
```

### Clean Build Artifacts

```bash
# Clean target directory
./mvnw clean

# Clean and rebuild from scratch
./mvnw clean install -q
```

---

## Database Commands

### Flyway Migrations

```bash
# Check migration status
./mvnw flyway:info

# Run pending migrations
./mvnw flyway:migrate

# Rollback last migration (careful!)
./mvnw flyway:undo

# Clean database (drops all objects - use only in dev!)
./mvnw flyway:clean
```

**Note:** Migrations run automatically on application startup. Manual commands are for troubleshooting.

---

## Docker Commands (Backend-specific)

```bash
# Start PostgreSQL only
cd ../../infra
docker compose up -d postgres

# Start all services (PostgreSQL + API)
docker compose up -d --build --quiet-pull

# View API logs
docker compose logs -f --tail=50 api

# Stop all services
docker compose down

# Stop and remove volumes (data loss!)
docker compose down -v
```

---

## Dependency Management

### Update Dependencies

```bash
# List available dependency updates
./mvnw versions:display-dependency-updates

# List available plugin updates
./mvnw versions:display-plugin-updates

# Update dependencies to latest versions (careful!)
./mvnw versions:use-latest-versions
```

### Dependency Tree

```bash
# Display dependency tree
./mvnw dependency:tree

# Find specific dependency
./mvnw dependency:tree | grep "spring-boot"

# Display dependency tree for specific scope
./mvnw dependency:tree -Dscope=test
```

---

## Code Quality Commands

### Formatting and Linting

```bash
# Format code with Spring Java Format
./mvnw spring-javaformat:apply

# Validate code formatting
./mvnw spring-javaformat:validate

# Run Checkstyle (if configured)
./mvnw checkstyle:check
```

### Static Analysis

```bash
# Run SpotBugs (if configured)
./mvnw spotbugs:check

# Run PMD (if configured)
./mvnw pmd:check
```

---

## Troubleshooting

### Common Issues

**Issue:** "Port 8080 already in use"
```bash
# Find process using port 8080
netstat -ano | findstr :8080  # Windows
lsof -i :8080                 # Linux/Mac

# Kill process
taskkill /PID <PID> /F        # Windows
kill -9 <PID>                 # Linux/Mac
```

**Issue:** "Cannot connect to PostgreSQL"
```bash
# Check if PostgreSQL is running
docker compose ps

# Restart PostgreSQL
docker compose restart postgres

# Check PostgreSQL logs
docker compose logs postgres
```

**Issue:** "Tests fail with Testcontainers error"
```bash
# Ensure Docker is running
docker ps

# Clean Testcontainers resources
docker system prune -f

# Re-run tests
./mvnw clean test
```

**Issue:** "Maven dependencies not resolving"
```bash
# Force update dependencies
./mvnw clean install -U

# Clear local Maven cache (nuclear option)
rm -rf ~/.m2/repository  # Linux/Mac
rmdir /s /q %USERPROFILE%\.m2\repository  # Windows
```

---

## Performance Profiling

### JVM Monitoring

```bash
# Run with JVM monitoring enabled
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx512m -Xms256m -XX:+HeapDumpOnOutOfMemoryError"

# Generate heap dump
jmap -dump:live,format=b,file=heap.bin <PID>

# View heap usage
jstat -gc <PID> 1000
```

---

## CI/CD Commands

### Pre-Commit Checks

```bash
# Run all pre-commit checks (format, lint, test)
./mvnw clean verify -q

# Quick validation (format + compile only)
./mvnw spring-javaformat:validate compile
```

### Release Commands

```bash
# Set new version
./mvnw versions:set -DnewVersion=1.2.0

# Commit version change
./mvnw versions:commit

# Rollback version change (if mistake)
./mvnw versions:revert
```

---

## Environment-Specific Commands

### Development Environment

```bash
# Run with dev profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Run with live reload enabled (spring-boot-devtools)
./mvnw spring-boot:run
```

### Integration Testing Environment

```bash
# Run with integration profile
./mvnw test -Dspring.profiles.active=integration
```

### Production Simulation

```bash
# Build production JAR
./mvnw clean package -DskipTests -Pprod

# Run production JAR
java -jar target/wine-reviewer-api-1.0.0.jar --spring.profiles.active=prod
```

---

## Useful Aliases (Optional)

Add to `.bashrc` or `.zshrc` for convenience:

```bash
# Backend aliases
alias api-run='cd services/api && ./mvnw spring-boot:run'
alias api-test='cd services/api && ./mvnw test -q'
alias api-verify='cd services/api && ./mvnw verify -q'
alias api-build='cd services/api && ./mvnw clean install -q -DskipTests'
alias api-clean='cd services/api && ./mvnw clean'
```

---

## Related Documentation

- **CODING_STYLE_BACKEND.md** - Java/Spring Boot coding conventions
- **README.md** - Backend architecture and setup instructions
- **CLAUDE.md** - Project-wide guidelines
- **OpenAPI Docs** - http://localhost:8080/swagger-ui.html (when running)
