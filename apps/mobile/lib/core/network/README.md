# Network Layer - Camada de Rede (HTTP Client)

Esta camada fornece infraestrutura HTTP completa com autenticação JWT, logging e tratamento de erros.

---

## 📦 Componentes

### 1. **DioClient** (`dio_client.dart`)
Cliente HTTP configurado com Dio (Singleton).

**Configurações:**
- **Base URL**: `http://10.0.2.2:8080/api` (Android emulator)
- **Timeouts**: Connect (10s), Send (30s), Receive (30s)
- **Headers padrão**: `Content-Type: application/json`, `Accept: application/json`
- **Interceptors**: Logging + Auth

**Uso:**
```dart
final client = ref.watch(dioClientProvider);
final response = await client.get(ApiConstants.reviews);
```

---

### 2. **AuthInterceptor** (`auth_interceptor.dart`)
Adiciona JWT token automaticamente em todas as requisições.

**Funcionalidades:**
- Lê token do `flutter_secure_storage` (criptografado)
- Adiciona header: `Authorization: Bearer <token>`
- Métodos auxiliares: `saveToken()`, `clearToken()`, `hasToken()`

**Fluxo:**
```
Request → AuthInterceptor → Adiciona JWT → Servidor
```

**Uso:**
```dart
final authInterceptor = ref.watch(authInterceptorProvider);

// Após login bem-sucedido
await authInterceptor.saveToken('jwt-token-aqui');

// Logout
await authInterceptor.clearToken();

// Verificar se usuário está autenticado
final isAuthenticated = await authInterceptor.hasToken();
```

---

### 3. **LoggingInterceptor** (`logging_interceptor.dart`)
Loga requisições e respostas HTTP (apenas em debug mode).

**Logs:**
- Método HTTP, URL, Headers, Body (request)
- Status code, Headers, Body (response)
- Tipo de erro, mensagem (error)

**Comportamento:**
- **Debug mode** (`kDebugMode = true`): Logs habilitados
- **Release mode** (`kDebugMode = false`): Logs desabilitados automaticamente

---

### 4. **Network Exceptions** (`network_exception.dart`)
Exceções customizadas para tratamento de erros HTTP.

**Hierarquia:**
```
NetworkException (abstract)
├── UnauthorizedException (401) - Token expirado
├── ForbiddenException (403) - Sem permissão
├── NotFoundException (404) - Recurso não encontrado
├── BadRequestException (400) - Dados inválidos
├── UnprocessableEntityException (422) - Regra de negócio violada
├── ServerException (5xx) - Erro no servidor
├── TimeoutException - Timeout
├── ConnectionException - Sem internet
└── UnknownNetworkException - Outros erros
```

**Uso em Services:**
```dart
try {
  final response = await _client.get('/reviews');
  return parseReviews(response.data);
} on DioException catch (e) {
  throw NetworkExceptionHandler.handleError(e); // Converte DioException → NetworkException
}
```

**Tratamento na UI:**
```dart
if (error is UnauthorizedException) {
  // Redirecionar para login
  Navigator.of(context).pushReplacementNamed('/login');
} else if (error is ConnectionException) {
  // Mostrar mensagem de sem internet
  showSnackBar('Sem conexão com internet');
}
```

---

### 5. **Network Providers** (`network_providers.dart`)
Providers do Riverpod para injeção de dependência.

**Grafo de Dependências:**
```
FlutterSecureStorage
        ↓
AuthInterceptor
        ↓
DioClient
        ↓
Services (ReviewService, AuthService, etc.)
```

**Providers disponíveis:**
- `secureStorageProvider`: FlutterSecureStorage
- `authInterceptorProvider`: AuthInterceptor
- `dioClientProvider`: DioClient

---

## 🚀 Como Usar

### 1. Criar um Service

```dart
// lib/features/review/data/review_service.dart
import 'package:wine_reviewer_mobile/core/network/dio_client.dart';
import 'package:wine_reviewer_mobile/core/network/network_exception.dart';
import 'package:wine_reviewer_mobile/core/constants/api_constants.dart';

class ReviewService {
  final DioClient _client;

  ReviewService(this._client);

  Future<List<Review>> getReviews() async {
    try {
      final response = await _client.get(
        ApiConstants.reviews,
        queryParameters: {
          'page': 0,
          'size': 20,
        },
      );

      final List<dynamic> data = response.data['content'];
      return data.map((json) => Review.fromJson(json)).toList();
    } on DioException catch (e) {
      throw NetworkExceptionHandler.handleError(e);
    }
  }

  Future<Review> createReview({
    required String wineId,
    required int rating,
    required String notes,
    String? imageUrl,
  }) async {
    try {
      final response = await _client.post(
        ApiConstants.createReview,
        data: {
          'wineId': wineId,
          'rating': rating,
          'notes': notes,
          'imageUrl': imageUrl,
        },
      );

      return Review.fromJson(response.data);
    } on DioException catch (e) {
      throw NetworkExceptionHandler.handleError(e);
    }
  }

  Future<void> deleteReview(String id) async {
    try {
      await _client.delete(ApiConstants.deleteReview(id));
    } on DioException catch (e) {
      throw NetworkExceptionHandler.handleError(e);
    }
  }
}
```

### 2. Criar Provider para o Service

```dart
// lib/features/review/providers/review_providers.dart
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:wine_reviewer_mobile/core/providers/network_providers.dart';
import 'package:wine_reviewer_mobile/features/review/data/review_service.dart';

final reviewServiceProvider = Provider<ReviewService>((ref) {
  final client = ref.watch(dioClientProvider);
  return ReviewService(client);
});
```

### 3. Usar no Widget

```dart
// lib/features/review/presentation/screens/review_list_screen.dart
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

class ReviewListScreen extends ConsumerWidget {
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      appBar: AppBar(title: Text('Reviews')),
      body: FutureBuilder<List<Review>>(
        future: ref.read(reviewServiceProvider).getReviews(),
        builder: (context, snapshot) {
          // Loading
          if (snapshot.connectionState == ConnectionState.waiting) {
            return Center(child: CircularProgressIndicator());
          }

          // Error
          if (snapshot.hasError) {
            final error = snapshot.error;

            if (error is UnauthorizedException) {
              // Redirecionar para login
              WidgetsBinding.instance.addPostFrameCallback((_) {
                Navigator.of(context).pushReplacementNamed('/login');
              });
              return Center(child: Text('Redirecionando para login...'));
            } else if (error is ConnectionException) {
              return Center(
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Icon(Icons.wifi_off, size: 64),
                    SizedBox(height: 16),
                    Text('Sem conexão com internet'),
                    ElevatedButton(
                      onPressed: () => setState(() {}), // Retry
                      child: Text('Tentar novamente'),
                    ),
                  ],
                ),
              );
            } else {
              return Center(child: Text('Erro: ${error.toString()}'));
            }
          }

          // Success
          final reviews = snapshot.data!;
          return ListView.builder(
            itemCount: reviews.length,
            itemBuilder: (context, index) {
              final review = reviews[index];
              return ListTile(
                title: Text(review.wine.name),
                subtitle: Text('Rating: ${'🍷' * review.rating}'),
              );
            },
          );
        },
      ),
    );
  }
}
```

---

## 🔐 Autenticação (Salvar/Limpar Token)

### Após Login Bem-Sucedido

```dart
// No AuthService, após receber token do backend
final authInterceptor = ref.read(authInterceptorProvider);
await authInterceptor.saveToken(jwtToken);
```

### Logout

```dart
final authInterceptor = ref.read(authInterceptorProvider);
await authInterceptor.clearToken();
Navigator.of(context).pushReplacementNamed('/login');
```

### Verificar Autenticação (App Startup)

```dart
// No main.dart ou splash screen
final authInterceptor = ref.read(authInterceptorProvider);
final isAuthenticated = await authInterceptor.hasToken();

if (isAuthenticated) {
  // Redirecionar para home
  Navigator.of(context).pushReplacementNamed('/home');
} else {
  // Redirecionar para login
  Navigator.of(context).pushReplacementNamed('/login');
}
```

---

## 🧪 Testando (Mocking com Riverpod)

```dart
// Em testes, você pode sobrescrever providers
testWidgets('Test review service', (tester) async {
  // Mock DioClient
  final mockDioClient = MockDioClient();
  when(mockDioClient.get(any)).thenAnswer(
    (_) async => Response(
      data: {'content': []},
      statusCode: 200,
      requestOptions: RequestOptions(path: '/reviews'),
    ),
  );

  await tester.pumpWidget(
    ProviderScope(
      overrides: [
        dioClientProvider.overrideWithValue(mockDioClient),
      ],
      child: MyApp(),
    ),
  );

  // ... testes
});
```

---

## 📊 Estrutura de Arquivos

```
lib/core/network/
├── auth_interceptor.dart       # Interceptor de autenticação (JWT)
├── logging_interceptor.dart    # Interceptor de logging (debug)
├── dio_client.dart             # Cliente HTTP configurado (Singleton)
├── network_exception.dart      # Exceções customizadas + handler
└── README.md                   # Esta documentação

lib/core/providers/
└── network_providers.dart      # Providers do Riverpod (DI)

lib/core/constants/
└── api_constants.dart          # URLs, endpoints, timeouts
```

---

## 🔗 Referências

- **Dio Package**: https://pub.dev/packages/dio
- **Riverpod**: https://riverpod.dev/
- **Flutter Secure Storage**: https://pub.dev/packages/flutter_secure_storage

---

## ✅ Checklist de Implementação

- [x] DioClient configurado (base URL, timeouts, headers)
- [x] AuthInterceptor (JWT token automático)
- [x] LoggingInterceptor (debug logs)
- [x] Network exceptions (tratamento de erros)
- [x] Providers do Riverpod (DI)
- [ ] Services implementados (ReviewService, AuthService, etc.) - **Próximo passo**
- [ ] Testes unitários (services + interceptors)
- [ ] Testes de integração (requisições reais)

---

**Última atualização:** 2025-10-25
