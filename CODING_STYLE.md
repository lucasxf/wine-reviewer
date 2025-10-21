# Coding Style Guide - Lucas Xavier Ferreira

> Este documento define o estilo de programação do desenvolvedor para manter consistência em todos os projetos.
> Baseado na análise do repositório: https://github.com/lucasxf/estudos

---

## 🎯 Princípios Gerais

- **Qualidade sobre velocidade** - Tomar o tempo necessário para fazer certo
- **Código em inglês** - Nomes de classes, métodos, variáveis sempre em inglês
- **Comentários e logs podem ser em português** - Documentação Javadoc e mensagens de log
- **DDD e Event Sourcing** - Forte influência de Domain-Driven Design
- **Separation of Concerns** - Clara divisão entre camadas

---

## 📦 Estrutura de Pacotes

### Padrão Preferido (DDD/CQRS)
```
com.winereviewer.api/
├── application/
│   ├── api/              # Controllers (REST endpoints)
│   │   └── dto/          # Request/Response DTOs
│   ├── commands/         # Command handlers
│   │   └── handlers/     # Implementações de handlers
│   └── process/          # Sagas e processos
├── domain/               # Domain layer
│   ├── [aggregate]/      # Por agregado (ex: account, subscription)
│   │   ├── command/      # Commands do agregado
│   │   └── event/        # Events do agregado
│   └── exception/        # Domain exceptions
├── infrastructure/       # Infrastructure layer
│   ├── repository/       # Implementações de repositórios
│   ├── messaging/        # Event bus, publishers
│   ├── context/          # Context management (correlation/causation)
│   └── serialization/    # Mappers e serializers
└── config/               # Spring configuration
```

### Para Projetos CRUD Simples
```
com.winereviewer.api/
├── controller/           # REST endpoints
├── service/              # Business logic
├── repository/           # Data access
├── domain/               # Entities
├── dto/                  # Data Transfer Objects
├── exception/            # Custom exceptions
├── config/               # Configuration classes (@Configuration, @ConfigurationProperties)
└── security/             # Security filters, utils (não configs)
```

**Regra importante - Organização de configs:**
- Classes `@Configuration` e `@ConfigurationProperties` → `/config`
- Classes de segurança (filters, utils) → `/security`
- Exemplo: `JwtProperties` fica em `/config`, `JwtUtil` fica em `/security`

---

## 🏗️ Padrões Arquiteturais

### 1. Event Sourcing / CQRS (quando aplicável)
**Aggregates:**
- Imutáveis com estado reconstruído a partir de eventos
- Método `decide()` para comandos → retorna eventos
- Método privado `apply()` para aplicar eventos ao estado
- Método `replay()` para reconstruir estado do histórico

**Exemplo:**
```java
public class AccountAggregate {
    private final UUID id;
    private final List<AccountEvent> history;
    private long version = -1L;
    @Getter
    private AccountStatus status;

    public static AccountAggregate from(UUID id, List<AccountEvent> history, long lastVersion) {
        final var aggregate = new AccountAggregate(id, history, lastVersion);
        aggregate.version = lastVersion;
        return aggregate;
    }

    public AccountCreated decide(CreateAccount command) {
        validateCommand(command);
        // Business rules
        return new AccountCreated(...);
    }

    private void replay() {
        for (AccountEvent event : history) {
            switch (event) {
                case AccountCreated e -> apply(e);
                // ...
            }
        }
    }
}
```

### 2. Command Handlers
- Interface `CommandHandler<T extends Command>`
- Implementações separadas por comando
- Fluxo: Load → Decide → Append → Publish
- Sempre usar `ContextScope` para correlation/causation IDs

**Exemplo:**
```java
@Slf4j
@Component
public class CreateAccountHandler implements CommandHandler<CreateAccount> {

    @Override
    public void handle(CreateAccount command, UUID correlationId) {
        try (final var scope = ContextScope.open(correlationId, command.commandId())) {
            log.info("Handling CreateAccount command for username: {}", command.username());

            // 1. Load current state
            final var stream = repository.load(accountId);
            final var aggregate = AccountAggregate.from(...);

            // 2. Decide new events
            final var event = aggregate.decide(command);

            // 3. Persist new events
            final var events = repository.append(...);

            // 4. Publish new events
            eventBus.publishAll(events, correlationId, commandId);
        }
    }
}
```

---

## 📝 Convenções de Código

### Nomenclatura
- **Classes:** PascalCase - `AccountAggregate`, `CreateAccountHandler`
- **Métodos:** camelCase - `createAccount()`, `validateCommand()`
- **Variáveis:** camelCase - `correlationId`, `accountStream`
- **Constantes:** UPPER_SNAKE_CASE - `EMAIL_PATTERN`, `MAX_RETRY_ATTEMPTS`
- **Pacotes:** lowercase - `subscriptions_billing`, `domain.account`
- **Números grandes:** SEMPRE usar underscore para separar milhares - `3_600_000` (não `3600000`)

**Exemplo de números com agrupamento:**
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

### Ordenação de Métodos em Classes

**Regra:** Métodos devem ser ordenados de **público para privado**, do **topo para baixo**.

**Princípios:**
1. **Visibilidade decrescente:** public → protected → package-private → private
2. **Nenhum método privado pode aparecer acima de um método público**
3. **Métodos privados são ordenados pela ordem de invocação:**
   - Métodos privados chamados primeiro pelos públicos aparecem primeiro
   - Métodos privados chamados depois aparecem depois
   - Isso cria um "fluxo de leitura" natural: top-down

**Exemplo correto:**
```java
@Service
public class ReviewServiceImpl implements ReviewService {

    // 1. Campos (private final)
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final WineRepository wineRepository;

    // 2. Construtor
    public ReviewServiceImpl(ReviewRepository reviewRepository, ...) {
        this.reviewRepository = reviewRepository;
        // ...
    }

    // 3. Métodos públicos (interface implementation)
    @Override
    public ReviewResponse createReview(CreateReviewRequest request, UUID userId) {
        var user = userRepository.findById(userId).orElseThrow(...);
        var wine = wineRepository.findById(wineId).orElseThrow(...);
        var review = toReview(request, user, wine);  // ← chama privado
        reviewRepository.save(review);
        return toReviewResponse(review, ...);  // ← chama privado
    }

    @Override
    public ReviewResponse updateReview(UUID reviewId, ...) {
        // ...
    }

    // 4. Métodos privados (ordenados pela ordem de chamada acima)
    private Review toReview(CreateReviewRequest request, User user, Wine wine) {
        // Chamado primeiro no createReview
    }

    private ReviewResponse toReviewResponse(Review review, ...) {
        // Chamado depois no createReview
    }

    private UserSummaryResponse toUserSummary(User user) {
        // Chamado por toReviewResponse
    }

    private WineSummaryResponse toWineSummary(Wine wine) {
        // Chamado por toReviewResponse
    }
}
```

**Exemplo incorreto (❌):**
```java
public class ReviewServiceImpl {

    // ❌ ERRADO: método privado acima do público
    private Review toReview(...) { }

    @Override
    public ReviewResponse createReview(...) { }

    @Override
    public ReviewResponse updateReview(...) { }

    // ❌ ERRADO: toReviewResponse deveria vir antes de toUserSummary
    //           pois é chamado primeiro
    private UserSummaryResponse toUserSummary(...) { }
    private ReviewResponse toReviewResponse(...) { }
}
```

**Benefícios:**
- Leitura natural top-down (public API → implementação)
- Facilita navegação no código
- Entendimento progressivo: vê-se primeiro "o que" a classe faz, depois "como"

### Java Moderno (Java 21)
- ✅ **`var`** para inferência de tipo (quando o tipo é óbvio)
- ✅ **Records** para DTOs e Commands/Events imutáveis
- ✅ **Sealed classes** para hierarquias fechadas (Commands, Events)
- ✅ **Pattern matching** com `switch` e `instanceof`
- ✅ **Text blocks** para strings multilinha

**Exemplo:**
```java
// Records para Commands
public sealed interface AccountCommand extends Command {
    record CreateAccount(
        UUID commandId,
        Instant timestamp,
        UUID accountId,
        String username,
        String usernameKey
    ) implements AccountCommand {}
}

// Pattern matching em switch
private void replay() {
    for (AccountEvent event : history) {
        switch (event) {
            case AccountCreated e -> apply(e);
            case AccountDeleted e -> apply(e);
            default -> throw new DomainException("Unexpected: " + event);
        }
    }
}

// var para clareza
final var accountStream = repository.load(accountId);
final var accountAggregate = AccountAggregate.from(...);
```

### Anotações Lombok
- ✅ `@Slf4j` - Logging automático
- ✅ `@Getter` - Getters seletivos (não usar `@Data` indiscriminadamente)
- ❌ Evitar `@Data` em entidades de domínio (preferir imutabilidade)

### Tratamento de Exceções
- **Domain exceptions** específicas por tipo de erro
- Hierarquia de exceções: `DomainException` → exceções específicas
- Validações com mensagens claras em português

**Exemplo:**
```java
public class DomainException extends RuntimeException { }
public class InvalidAccountException extends DomainException { }
public class AccountCreationException extends DomainException { }

// Uso
if (status != AccountStatus.NEW) {
    throw new AccountCreationException(
        "Uma conta já com este nome de usuário já foi criada " +
        "ou está em um estado inválido: " + command.username()
    );
}
```

---

## 🎨 Estilo de Controllers

### REST Controllers
- Anotação `@RestController` + `@RequestMapping`
- Injeção via construtor (não `@Autowired` em fields)
- Validação no controller, business logic no handler/service
- Headers opcionais: `X-Correlation-Id` para rastreamento
- Logs informativos em português

**Exemplo:**
```java
@Slf4j
@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountCommandHandler commandHandler;

    public AccountController(AccountCommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    public ResponseEntity<AccountResponse> create(
            @PathVariable String username,
            @RequestHeader(value = "X-Correlation-Id", required = false) String corr) {

        var correlationId = getCorrelationId(corr);
        var causationId = UUID.randomUUID();

        try (var scope = ContextScope.open(correlationId, causationId)) {
            log.info("Received request to create account for username: {}", username);

            var command = createAccountCommand(...);
            commandHandler.handle(correlationId, command);

            log.info("Account creation command processed for username: {}", username);
            return ResponseEntity.accepted().build();
        }
    }
}
```

---

## 📋 Javadoc e Comentários

### Javadoc
- **Obrigatório** para classes públicas e interfaces
- Incluir `@author` e `@date`
- Descrição concisa em português

**Exemplo:**
```java
/**
 * Controller para gerenciamento de contas de usuário.
 *
 * @author Lucas Xavier Ferreira
 * @date 22/09/2025
 */
@RestController
public class AccountController { }
```

### Comentários inline
- Usar quando a lógica não é óbvia
- Preferir código autoexplicativo a comentários excessivos
- Podem ser em português

---

## 🧪 Testes

### Estrutura de Testes
- JUnit 5
- Cobertura de caminhos de sucesso e falha
- Testes de integração com contexto completo
- Mock apenas quando necessário (preferir testes reais)

### Nomenclatura
- `should[ExpectedBehavior]When[StateUnderTest]`
- Exemplo: `shouldCreateAccountWhenValidCommand()`

---

## 🔧 Logging

- `@Slf4j` do Lombok
- Mensagens informativas em português
- Incluir contexto relevante (IDs, usernames, etc.)
- Níveis apropriados: INFO para fluxo, DEBUG para detalhes, ERROR para exceções

**Exemplo:**
```java
log.info("Handling CreateAccount command for username: {}", command.username());
log.info("Account {} created successfully for username: {}", accountId, command.username());
```

---

## 📦 Maven / Gerenciamento de Dependências

### Versões de Dependências em Properties

**Regra:** Sempre declarar versões de dependências em `<properties>` e referenciar via placeholders.

**Por quê:**
- Centraliza versões em um único lugar
- Facilita upgrades (atualiza em um lugar só)
- Evita inconsistências de versões entre módulos
- Melhora legibilidade do pom.xml

**Exemplo:**
```xml
<properties>
    <!-- Java -->
    <java.version>21</java.version>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>

    <!-- Spring -->
    <spring-boot.version>3.2.0</spring-boot.version>

    <!-- Security -->
    <jwt.version>0.12.3</jwt.version>

    <!-- Database -->
    <postgresql.version>42.7.1</postgresql.version>
    <flyway.version>10.4.1</flyway.version>

    <!-- Testing -->
    <testcontainers.version>1.19.3</testcontainers.version>

    <!-- Utilities -->
    <lombok.version>1.18.30</lombok.version>
</properties>

<dependencies>
    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>${jwt.version}</version>  <!-- ← Placeholder -->
    </dependency>

    <!-- PostgreSQL -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <version>${postgresql.version}</version>  <!-- ← Placeholder -->
    </dependency>
</dependencies>
```

**Nomenclatura de propriedades:**
- Usar kebab-case ou dot notation: `jwt.version` ou `jwt-version`
- Preferir sufixo `.version` para clareza
- Agrupar por categoria (Spring, Database, Security, etc.)

---

## 🔧 Injeção de Dependências e Configurações

### Injeção de Propriedades (Configuration Properties)

**Regra:** NUNCA usar `@Value` ou field injection. SEMPRE usar `@ConfigurationProperties` com POJOs dedicados e injeção via construtor.

**Por quê:**
- Type-safe: validação em tempo de compilação
- Testável: fácil criar instâncias para testes
- Centralizado: todas as configs de um módulo em uma classe
- Imutável: usar `final` nos campos para segurança
- Sem reflexão: injeção via construtor é explícita

**Exemplo CORRETO (✅):**
```java
// 1. POJO de configuração
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private final String secret;
    private final Long expiration;

    public JwtProperties(String secret, Long expiration) {
        this.secret = secret;
        this.expiration = expiration;
    }

    public String getSecret() {
        return secret;
    }

    public Long getExpiration() {
        return expiration;
    }
}

// 2. Habilitar no config
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {
    // ...
}

// 3. Usar via constructor injection
@Component
public class JwtUtil {
    private final String secret;
    private final Long expirationMs;

    public JwtUtil(JwtProperties properties) {
        this.secret = properties.getSecret();
        this.expirationMs = properties.getExpiration();
    }

    public String generateToken(UUID userId) {
        // Usa this.secret e this.expirationMs
    }
}
```

**Exemplo INCORRETO (❌):**
```java
// ❌ NUNCA FAZER ISSO
@Component
public class JwtUtil {
    @Value("${jwt.secret}")  // ← Field injection!
    private String secret;

    @Value("${jwt.expiration}")
    private Long expirationMs;
}
```

**application.yml correspondente:**
```yaml
jwt:
  secret: your-secret-key-min-256-bits-32chars
  expiration: 3600000  # 1 hora em milissegundos
```

---

## 🚫 Anti-Padrões a Evitar

- ❌ `@Autowired` em fields (usar injeção via construtor)
- ❌ **`@Value` para propriedades** (usar `@ConfigurationProperties` com POJOs)
- ❌ **Field injection** (sempre usar constructor injection)
- ❌ Getters/setters desnecessários (usar Lombok seletivamente)
- ❌ Lógica de negócio em controllers
- ❌ Exceptions genéricas (`throw new Exception()`)
- ❌ Magic numbers/strings (usar constantes)
- ❌ Null checks excessivos (usar `Optional` ou validação antecipada)
- ❌ **Versões hardcoded em dependencies** (usar `<properties>`)

---

## ✅ Checklist de Code Review

- [ ] Código em inglês, comentários podem ser em português
- [ ] Injeção de dependências via construtor (NUNCA field injection)
- [ ] **Propriedades via `@ConfigurationProperties` (NUNCA `@Value`)**
- [ ] Exceptions específicas do domínio
- [ ] Logs informativos nos pontos-chave
- [ ] Javadoc em classes públicas com @author e @date
- [ ] Uso apropriado de Java 21 features (var, records, pattern matching)
- [ ] Separação clara entre camadas (domain/application/infrastructure)
- [ ] Testes cobrindo casos de sucesso e falha
- [ ] Validações com mensagens claras
- [ ] Sem magic numbers ou strings hardcoded
- [ ] **Versões de dependências em `<properties>` com placeholders**
- [ ] **Métodos ordenados: public → private (invocation flow)**

---

## 🗄️ JPA / Hibernate

### Callbacks de Entidade

**REGRA CRÍTICA:** Apenas **UM método por tipo de callback** por entidade.

Jakarta Persistence permite apenas um método anotado com cada tipo de callback:
- `@PrePersist` - UM método apenas
- `@PreUpdate` - UM método apenas
- `@PostPersist`, `@PostUpdate`, `@PreRemove`, `@PostRemove`, `@PostLoad` - UM de cada

**❌ INCORRETO - Múltiplos callbacks do mesmo tipo:**
```java
@Entity
public class Review {

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    @PrePersist
    @PreUpdate  // ❌ ERRO: segundo @PreUpdate
    protected void validate() {
        // validações
    }

    @PrePersist
    @PreUpdate  // ❌ ERRO: terceiro @PreUpdate
    protected void normalize() {
        // normalizações
    }
}
// Resultado: PersistenceException ao inicializar EntityManagerFactory
```

**✅ CORRETO - Um callback chama método privado:**
```java
@Entity
public class Review {

    @PrePersist
    protected void onCreate() {
        validateAndNormalize();
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        validateAndNormalize();
        updatedAt = Instant.now();
    }

    private void validateAndNormalize() {
        // Normaliza campos
        if (notes != null) {
            notes = notes.trim();
        }

        // Valida regras de negócio
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating deve estar entre 1 e 5");
        }
    }
}
```

**Por que essa limitação?**
- JPA precisa saber exatamente qual método executar em cada fase do ciclo de vida
- Múltiplos callbacks causam ambiguidade sobre ordem de execução
- Especificação Jakarta Persistence proíbe explicitamente

### Palavras Reservadas SQL

**Atenção:** Alguns nomes de colunas são palavras reservadas em bancos de dados.

**Exemplos comuns:**
- `year`, `month`, `day`, `hour` (temporais)
- `user`, `group`, `order` (entidades comuns)
- `key`, `value`, `index` (estruturas)

**Solução:** Escapar com backticks na anotação `@Column`:

```java
@Entity
public class Wine {

    @Column(name = "`year`")  // ✅ Escapado - funciona em H2, PostgreSQL, MySQL
    private Integer year;

    @Column(name = "`order`")  // ✅ Escapado
    private Integer order;
}
```

**Nota:** Backticks funcionam na maioria dos bancos. PostgreSQL também aceita aspas duplas (`"year"`), mas backticks são mais portáveis.

---

## 🔒 Spring Security

### Filtros de Segurança como @Bean

**REGRA:** Filtros customizados devem ser declarados como `@Bean` em classes de configuração, **NÃO** como `@Component`.

**❌ INCORRETO - Filtro como @Component:**
```java
@Component  // ❌ Registro implícito, dificulta testes
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // ...
}

@Configuration
public class SecurityConfig {
    private final JwtAuthenticationFilter filter;  // Injetado

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http.addFilterBefore(filter, ...).build();
    }
}
```

**Problemas:**
- Filtro é registrado automaticamente pelo Spring
- Dificulta testes (precisa carregar todo contexto de segurança)
- Não fica claro que é parte da configuração de segurança

**✅ CORRETO - Filtro como @Bean:**
```java
// SEM @Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // Classe POJO, sem anotações Spring
}

@Configuration
public class SecurityConfig {

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtUtil jwtUtil,
            UserDetailsService userDetailsService) {
        return new JwtAuthenticationFilter(jwtUtil, userDetailsService);
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtAuthenticationFilter filter) {  // Injetado como parâmetro
        return http.addFilterBefore(filter, ...).build();
    }
}
```

**Vantagens:**
- **Controle explícito:** Fica claro que filtro é parte da configuração de segurança
- **Facilita testes:** Pode ser facilmente mockado em `@WebMvcTest` ou `@SpringBootTest`
- **Segue padrão Spring Security:** Configurações devem declarar seus beans
- **Evita registro duplo:** Filtro só é criado quando necessário

---

## 🔄 Atualizações

Este documento será atualizado continuamente à medida que novos padrões e preferências forem identificados.

**Última atualização:** 2025-10-20 (atualizado por Claude Code - Adicionadas regras de JPA callbacks e Spring Security filters)
