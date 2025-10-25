import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:wine_reviewer_mobile/core/network/auth_interceptor.dart';
import 'package:wine_reviewer_mobile/core/network/dio_client.dart';

/// Providers da camada de network - Injeção de Dependência com Riverpod
///
/// EXPLICAÇÃO (Backend Context):
/// - Riverpod = Framework de Dependency Injection (DI) do Flutter
/// - Similar a: Spring @Bean, CDI @Produces, Dagger @Provides
/// - Provider = Factory de objetos (cria e gerencia ciclo de vida)
///
/// ANALOGIA Backend (Spring Boot):
/// ```java
/// @Configuration
/// public class NetworkConfig {
///   @Bean
///   public FlutterSecureStorage secureStorage() {
///     return new FlutterSecureStorage();
///   }
///
///   @Bean
///   public AuthInterceptor authInterceptor(FlutterSecureStorage storage) {
///     return new AuthInterceptor(storage);
///   }
///
///   @Bean
///   public DioClient dioClient(AuthInterceptor interceptor) {
///     return new DioClient(interceptor);
///   }
/// }
/// ```
///
/// RIVERPOD PROVIDERS:
/// - `Provider<T>`: Objeto imutável, criado uma vez (similar a @Bean singleton)
/// - `StateProvider<T>`: Estado mutável simples (contador, toggle)
/// - `StateNotifierProvider<T>`: Estado mutável complexo (lista, auth state)
/// - `FutureProvider<T>`: Operação assíncrona (carregar dados da API)
/// - `StreamProvider<T>`: Stream de dados (WebSocket, notificações)
///
/// POR QUE RIVERPOD (vs Provider/GetIt)?
/// - ✅ Compile-time safety (erros em tempo de compilação, não runtime)
/// - ✅ Testável (fácil mockar providers em testes)
/// - ✅ Não precisa BuildContext (usa WidgetRef)
/// - ✅ Auto-dispose (limpa recursos automaticamente)
/// - ✅ Dependências explícitas (ref.watch/read deixa claro quem depende de quem)

// =============================================================================
// Flutter Secure Storage Provider
// =============================================================================

/// Provider para FlutterSecureStorage
///
/// EXPLICAÇÃO - Flutter Secure Storage:
/// - Armazena dados criptografados no device
/// - Android: usa KeyStore (hardware encryption)
/// - iOS: usa Keychain (hardware encryption)
/// - Usado para: JWT tokens, refresh tokens, credenciais
///
/// ESCOPO:
/// - Global (disponível em toda aplicação)
/// - Singleton (mesma instância sempre)
/// - Imutável (configuração não muda)
///
/// USO:
/// ```dart
/// // Em widget
/// final storage = ref.watch(secureStorageProvider);
/// await storage.write(key: 'token', value: 'abc');
/// ```
final secureStorageProvider = Provider<FlutterSecureStorage>((ref) {
  return const FlutterSecureStorage();
});

// =============================================================================
// Auth Interceptor Provider
// =============================================================================

/// Provider para AuthInterceptor
///
/// DEPENDÊNCIAS:
/// - secureStorageProvider (injetado via ref.watch)
///
/// ESCOPO:
/// - Global (disponível em toda aplicação)
/// - Singleton (mesma instância sempre)
/// - Stateful (mantém referência ao storage)
///
/// CICLO DE VIDA:
/// 1. App inicia
/// 2. Riverpod cria secureStorageProvider (se necessário)
/// 3. Riverpod cria authInterceptorProvider (injeta storage)
/// 4. AuthInterceptor fica disponível para toda aplicação
///
/// USO (normalmente não usado diretamente, mas via dioClientProvider):
/// ```dart
/// final authInterceptor = ref.watch(authInterceptorProvider);
/// await authInterceptor.saveToken('jwt-token');
/// ```
final authInterceptorProvider = Provider<AuthInterceptor>((ref) {
  // Injeta dependência (secureStorageProvider)
  final storage = ref.watch(secureStorageProvider);

  // Cria e retorna AuthInterceptor
  return AuthInterceptor(storage);
});

// =============================================================================
// Dio Client Provider
// =============================================================================

/// Provider para DioClient
///
/// DEPENDÊNCIAS:
/// - authInterceptorProvider (injetado via ref.watch)
///
/// ESCOPO:
/// - Global (disponível em toda aplicação)
/// - Singleton (mesma instância sempre)
/// - Stateful (mantém interceptors configurados)
///
/// CONFIGURAÇÃO AUTOMÁTICA:
/// - BaseURL, timeouts, headers padrão (via ApiConstants)
/// - LoggingInterceptor (logs em debug mode)
/// - AuthInterceptor (adiciona JWT token)
///
/// USO (em services):
/// ```dart
/// class ReviewService {
///   final DioClient _client;
///
///   ReviewService(this._client);
///
///   Future<List<Review>> getReviews() async {
///     final response = await _client.get(ApiConstants.reviews);
///     return parseReviews(response.data);
///   }
/// }
///
/// // Provider do service
/// final reviewServiceProvider = Provider<ReviewService>((ref) {
///   final client = ref.watch(dioClientProvider);
///   return ReviewService(client);
/// });
/// ```
final dioClientProvider = Provider<DioClient>((ref) {
  // Injeta dependência (authInterceptorProvider)
  final authInterceptor = ref.watch(authInterceptorProvider);

  // Cria e retorna DioClient (Singleton interno)
  return DioClient(authInterceptor);
});

/// COMO FUNCIONA (Dependency Injection com Riverpod):
///
/// GRAFO DE DEPENDÊNCIAS:
/// ```
/// secureStorageProvider
///         ↓
/// authInterceptorProvider
///         ↓
/// dioClientProvider
///         ↓
/// reviewServiceProvider (exemplo)
/// ```
///
/// CRIAÇÃO LAZY (on-demand):
/// 1. Widget precisa de reviewService
/// 2. reviewServiceProvider precisa de dioClient → cria dioClientProvider
/// 3. dioClientProvider precisa de authInterceptor → cria authInterceptorProvider
/// 4. authInterceptorProvider precisa de storage → cria secureStorageProvider
/// 5. Todas dependências criadas na ordem correta!
///
/// SINGLETON AUTOMÁTICO:
/// - Riverpod garante que cada Provider é criado APENAS UMA VEZ
/// - Próximas chamadas retornam mesma instância
/// - Similar a @Bean singleton do Spring Boot
///
/// TESTABILIDADE:
/// ```dart
/// // Em testes, você pode sobrescrever providers:
/// testWidgets('Test review service', (tester) async {
///   await tester.pumpWidget(
///     ProviderScope(
///       overrides: [
///         // Mock DioClient em testes
///         dioClientProvider.overrideWithValue(mockDioClient),
///       ],
///       child: MyApp(),
///     ),
///   );
/// });
/// ```
///
/// LEITURA DE PROVIDERS (2 formas):
///
/// 1. ref.watch() - Escuta mudanças (rebuilds widget quando muda)
/// ```dart
/// class MyWidget extends ConsumerWidget {
///   @override
///   Widget build(BuildContext context, WidgetRef ref) {
///     // Widget rebuilds se provider mudar
///     final client = ref.watch(dioClientProvider);
///     return Text('Client: $client');
///   }
/// }
/// ```
///
/// 2. ref.read() - Leitura única (não escuta mudanças)
/// ```dart
/// // Em callbacks, use ref.read (não causa rebuild)
/// ElevatedButton(
///   onPressed: () {
///     final client = ref.read(dioClientProvider);
///     client.get('/reviews');
///   },
///   child: Text('Load Reviews'),
/// )
/// ```
///
/// BOAS PRÁTICAS:
/// - Use Provider<T> para objetos stateless (DioClient, Services, Repositories)
/// - Use StateNotifierProvider<T> para objetos stateful (AuthState, ReviewListState)
/// - Sempre declare providers no nível top-level (global)
/// - Use ref.watch em build(), ref.read em callbacks/métodos
/// - Sobrescreva providers em testes para mockar dependências
///
/// COMPARAÇÃO COM BACKEND DI:
///
/// | Riverpod          | Spring Boot       | CDI/Jakarta EE  |
/// |-------------------|-------------------|-----------------|
/// | Provider<T>       | @Bean             | @Produces       |
/// | ref.watch()       | @Autowired        | @Inject         |
/// | ref.read()        | context.getBean() | lookup()        |
/// | ProviderScope     | ApplicationContext| BeanManager     |
/// | overrideWith()    | @Profile("test")  | @Alternative    |
