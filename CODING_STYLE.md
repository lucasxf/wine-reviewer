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
├── config/               # Configuration classes
└── security/             # Security configs
```

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

## 🚫 Anti-Padrões a Evitar

- ❌ `@Autowired` em fields (usar injeção via construtor)
- ❌ Getters/setters desnecessários (usar Lombok seletivamente)
- ❌ Lógica de negócio em controllers
- ❌ Exceptions genéricas (`throw new Exception()`)
- ❌ Magic numbers/strings (usar constantes)
- ❌ Null checks excessivos (usar `Optional` ou validação antecipada)

---

## ✅ Checklist de Code Review

- [ ] Código em inglês, comentários podem ser em português
- [ ] Injeção de dependências via construtor
- [ ] Exceptions específicas do domínio
- [ ] Logs informativos nos pontos-chave
- [ ] Javadoc em classes públicas com @author e @date
- [ ] Uso apropriado de Java 21 features (var, records, pattern matching)
- [ ] Separação clara entre camadas (domain/application/infrastructure)
- [ ] Testes cobrindo casos de sucesso e falha
- [ ] Validações com mensagens claras
- [ ] Sem magic numbers ou strings hardcoded

---

## 🔄 Atualizações

Este documento será atualizado continuamente à medida que novos padrões e preferências forem identificados.

**Última atualização:** 2025-10-19 (atualizado por Claude Code)
