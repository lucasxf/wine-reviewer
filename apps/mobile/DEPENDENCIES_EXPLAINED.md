# 📦 Dependências do Flutter - Explicação Detalhada

> **Contexto:** Você é engenheiro backend. Este documento explica cada dependência Flutter em termos familiares.

---

## 🎯 Dependências de Produção (`dependencies`)

### 1. **flutter_riverpod** (State Management)
```yaml
flutter_riverpod: ^2.6.1
riverpod_annotation: ^2.6.1
```

**O que é:**
- Framework de gerenciamento de estado reativo
- Pense como: **Spring IoC Container + RxJava** combinados
- Gerencia estado da aplicação e injeção de dependências

**Por que usar:**
- Substitui `setState()` (estado local primitivo)
- Providers são como `@Service` do Spring (singletons injetáveis)
- Reatividade automática: quando o estado muda, widgets se atualizam sozinhos

**Analogia Backend:**
```java
// Spring Boot
@Service
public class UserService {
    // Estado gerenciado pelo Spring
}

// Riverpod (Flutter)
final userServiceProvider = Provider((ref) => UserService());
// Estado gerenciado pelo Riverpod
```

**Uso prático:**
- `Provider` → Serviços read-only (como `UserRepository`)
- `StateNotifierProvider` → Estado mutável (como `AuthenticationState`)
- `FutureProvider` → Chamadas async (como API calls)

---

### 2. **go_router** (Navigation)
```yaml
go_router: ^14.6.2
```

**O que é:**
- Sistema de navegação declarativa para Flutter
- Pense como: **Spring MVC @RequestMapping** mas para rotas mobile

**Por que usar:**
- Deep linking (abrir `/reviews/123` diretamente)
- Type-safe routes (rotas com tipos, não strings mágicas)
- Suporte a web (mesma navegação funciona em web e mobile)

**Analogia Backend:**
```java
// Spring MVC
@GetMapping("/reviews/{id}")
public Review getReview(@PathVariable String id) { }

// go_router (Flutter)
GoRoute(
  path: '/reviews/:id',
  builder: (context, state) => ReviewDetailsScreen(id: state.params['id']),
)
```

**Uso prático:**
- Define rotas no início do app
- `context.go('/login')` → Navega para tela de login
- `context.push('/reviews/123')` → Abre review (mantém histórico)

---

### 3. **dio** (HTTP Client)
```yaml
dio: ^5.7.0
```

**O que é:**
- Cliente HTTP poderoso para Flutter
- Pense como: **OkHttp (Android) + Axios (Node.js)** combinados

**Por que usar:**
- Interceptors (para adicionar JWT token automaticamente)
- Retry automático em caso de falha
- Timeouts configuráveis
- Melhor que `http` (package básico do Dart)

**Analogia Backend:**
```java
// RestTemplate (Spring)
restTemplate.getForObject("http://api.com/users", User.class);

// Dio (Flutter)
final response = await dio.get('/users');
final user = User.fromJson(response.data);
```

**Uso prático:**
- Configurar baseUrl (`http://localhost:8080/api`)
- Adicionar interceptor para JWT (`Authorization: Bearer <token>`)
- Tratar erros HTTP (401 → redirecionar para login)

---

### 4. **freezed** (Code Generation)
```yaml
freezed_annotation: ^2.4.4
json_annotation: ^4.9.0
```

**O que é:**
- Gerador de código para classes imutáveis
- Pense como: **Lombok** do Java

**Por que usar:**
- Gera automaticamente: `copyWith()`, `toString()`, `hashCode()`, `equals()`
- Classes imutáveis (final fields)
- Serialização JSON automática

**Analogia Backend:**
```java
// Lombok (Java)
@Data
@AllArgsConstructor
public class User {
    private final String id;
    private final String name;
}

// Freezed (Dart)
@freezed
class User with _$User {
  const factory User({
    required String id,
    required String name,
  }) = _User;

  factory User.fromJson(Map<String, dynamic> json) => _$UserFromJson(json);
}
```

**Uso prático:**
- DTOs imutáveis para API responses
- `user.copyWith(name: 'Novo Nome')` → Cria nova instância com campo alterado
- `User.fromJson(json)` → Desserialização automática

---

### 5. **flutter_secure_storage** (Secure Storage)
```yaml
flutter_secure_storage: ^9.2.2
```

**O que é:**
- Armazenamento criptografado de key-value
- Pense como: **Android Keystore + iOS Keychain** abstração

**Por que usar:**
- Guardar JWT tokens com segurança
- Criptografia nativa do SO (não é SharedPreferences simples)
- Dados persistem mesmo após fechar o app

**Analogia Backend:**
```java
// Spring Security - Session
session.setAttribute("token", jwtToken); // Memória/Redis

// flutter_secure_storage (Flutter)
await secureStorage.write(key: 'jwt_token', value: jwtToken); // Criptografado
```

**Uso prático:**
- Guardar JWT: `await storage.write(key: 'token', value: jwt)`
- Ler JWT: `final token = await storage.read(key: 'token')`
- Deletar ao logout: `await storage.delete(key: 'token')`

---

### 6. **image_picker** (Image Selection)
```yaml
image_picker: ^1.1.2
```

**O que é:**
- Plugin para selecionar imagens da câmera ou galeria
- Pense como: **HTML `<input type="file">`** mas nativo mobile

**Por que usar:**
- Acesso à câmera e galeria do dispositivo
- Retorna `File` ou bytes da imagem
- Multiplataforma (Android/iOS)

**Analogia Backend:**
```java
// Spring MVC - Upload
@PostMapping("/upload")
public void upload(@RequestParam("file") MultipartFile file) { }

// image_picker (Flutter)
final XFile? image = await ImagePicker().pickFromGallery();
// Agora envia para backend via pre-signed URL
```

**Uso prático:**
- Usuário tira foto do vinho
- App pega o arquivo da foto
- Envia para S3 via pre-signed URL

---

### 7. **cached_network_image** (Image Caching)
```yaml
cached_network_image: ^3.4.1
```

**O que é:**
- Widget que baixa e cacheia imagens da rede
- Pense como: **Browser cache** para imagens

**Por que usar:**
- Baixa imagem 1 vez, reutiliza do cache depois
- Placeholder enquanto carrega
- Melhora performance (menos requests)

**Analogia Backend:**
```java
// Você já implementou cache HTTP (ETags, Cache-Control)
// cached_network_image faz o mesmo no cliente

// Uso Flutter
CachedNetworkImage(
  imageUrl: 'https://example.com/wine.jpg',
  placeholder: (context, url) => CircularProgressIndicator(),
  errorWidget: (context, url, error) => Icon(Icons.error),
)
```

---

### 8. **google_sign_in** (OAuth)
```yaml
google_sign_in: ^6.2.2
```

**O que é:**
- Plugin oficial Google para autenticação OAuth
- Pense como: **Spring Security OAuth2 Client**

**Por que usar:**
- Login com Google (botão "Sign in with Google")
- Retorna Google ID Token (JWT)
- Envia token para seu backend validar

**Fluxo:**
```
1. Usuário clica "Login com Google"
2. google_sign_in abre tela Google OAuth
3. Usuário faz login no Google
4. google_sign_in retorna Google ID Token
5. App envia token para seu backend (/auth/google)
6. Backend valida token e retorna seu JWT
7. App guarda JWT no flutter_secure_storage
```

---

## 🛠️ Dependências de Desenvolvimento (`dev_dependencies`)

### 9. **build_runner** (Code Generation)
```yaml
build_runner: ^2.4.13
freezed: ^2.5.7
json_serializable: ^6.8.0
riverpod_generator: ^2.6.2
```

**O que é:**
- Ferramenta que roda geradores de código
- Pense como: **Maven Annotation Processor** ou **Lombok processor**

**Por que usar:**
- Gera código para freezed, json_serializable, riverpod
- Roda via comando: `flutter pub run build_runner build`

**Analogia Backend:**
```bash
# Maven - Gera código via annotation processors
mvn clean install

# Flutter - Gera código via build_runner
flutter pub run build_runner build --delete-conflicting-outputs
```

---

### 10. **golden_toolkit** (Visual Regression Testing)
```yaml
golden_toolkit: ^0.15.0
```

**O que é:**
- Testes de regressão visual (screenshots comparativos)
- Pense como: **Percy.io** ou **Chromatic** mas local

**Por que usar:**
- Tira screenshot da tela (golden file)
- Em próximos testes, compara se UI mudou
- Detecta mudanças visuais inesperadas

**Analogia Backend:**
```java
// Teste unitário (comportamento)
assertEquals(5, service.getRating());

// Golden test (visual)
expect(loginScreen, matchesGoldenFile('login_screen.png'));
// Se UI mudou → teste falha
```

---

## 📝 Comandos Úteis

```bash
# Instalar todas as dependências (como mvn install)
flutter pub get

# Atualizar dependências (como mvn dependency:resolve)
flutter pub upgrade

# Ver dependências desatualizadas (como mvn versions:display-dependency-updates)
flutter pub outdated

# Gerar código (freezed, json_serializable, riverpod)
flutter pub run build_runner build --delete-conflicting-outputs

# Limpar cache de geração
flutter pub run build_runner clean
```

---

## ✅ Resumo: Por que cada uma?

| Dependência | Função | Analogia Backend |
|-------------|--------|------------------|
| **flutter_riverpod** | State + DI | Spring IoC + RxJava |
| **go_router** | Navegação | Spring MVC Routes |
| **dio** | HTTP Client | OkHttp / RestTemplate |
| **freezed** | Imutáveis | Lombok |
| **flutter_secure_storage** | Tokens | Keystore/Keychain |
| **image_picker** | Fotos | Multipart upload |
| **cached_network_image** | Cache imgs | HTTP Cache |
| **google_sign_in** | OAuth | Spring OAuth2 Client |
| **build_runner** | Codegen | Maven processors |
| **golden_toolkit** | Visual tests | Screenshot testing |

---

**Próximo passo:** Rodar `flutter pub get` para baixar todas essas dependências.
