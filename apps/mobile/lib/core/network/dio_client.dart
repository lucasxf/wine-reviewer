import 'package:dio/dio.dart';
import 'package:wine_reviewer_mobile/core/constants/api_constants.dart';
import 'package:wine_reviewer_mobile/core/network/auth_interceptor.dart';
import 'package:wine_reviewer_mobile/core/network/logging_interceptor.dart';

/// Cliente HTTP configurado com Dio - Singleton
///
/// EXPLICAÇÃO (Backend Context):
/// - Similar a um RestTemplate configurado do Spring Boot
/// - Centraliza configuração de HTTP client (timeouts, headers, interceptors)
/// - Singleton garante uma única instância na aplicação
///
/// ANALOGIA Backend (Spring Boot):
/// ```java
/// @Configuration
/// public class RestTemplateConfig {
///   @Bean
///   public RestTemplate restTemplate() {
///     RestTemplate template = new RestTemplate();
///     template.setConnectTimeout(10000);
///     template.setReadTimeout(30000);
///     template.getInterceptors().add(new JwtAuthInterceptor());
///     template.getInterceptors().add(new LoggingInterceptor());
///     return template;
///   }
/// }
/// ```
///
/// DIO (Data Input/Output):
/// - HTTP client para Dart/Flutter (similar a: OkHttp, Apache HttpClient, RestTemplate)
/// - Suporta interceptors, timeouts, retry, cancel, upload/download progress
/// - Mais poderoso que http package padrão do Flutter
///
/// POR QUE DIO (vs http package)?
/// - ✅ Interceptors nativos (auth, logging, retry)
/// - ✅ Request/Response transformers
/// - ✅ Timeout configurável (connect, send, receive)
/// - ✅ Cancel requests em andamento
/// - ✅ Upload/download com progress tracking
/// - ✅ Melhor tratamento de erros
class DioClient {
  // =========================================================================
  // Singleton Pattern
  // =========================================================================

  /// Instância única (Singleton)
  ///
  /// EXPLICAÇÃO - Singleton:
  /// - Apenas UMA instância de DioClient existe na aplicação inteira
  /// - Reutilizar conexões TCP (pool de conexões)
  /// - Evitar reconfigurar Dio múltiplas vezes
  ///
  /// COMO FUNCIONA:
  /// 1. Primeira chamada: DioClient() → cria instância e salva em _instance
  /// 2. Próximas chamadas: DioClient() → retorna _instance existente
  ///
  /// ANALOGIA Backend:
  /// - Similar a @Bean singleton do Spring Boot
  /// - Ou @Singleton do CDI/Jakarta EE
  static DioClient? _instance;

  /// Instância do Dio (HTTP client)
  late final Dio _dio;

  /// Factory constructor - retorna singleton
  ///
  /// USO:
  /// ```dart
  /// final client = DioClient(authInterceptor); // Primeira chamada: cria
  /// final client2 = DioClient(authInterceptor); // Retorna mesma instância
  /// assert(identical(client, client2)); // true - mesma instância
  /// ```
  factory DioClient(AuthInterceptor authInterceptor) {
    // Se instância não existe, cria
    _instance ??= DioClient._internal(authInterceptor);
    return _instance!;
  }

  /// Private constructor - chamado apenas uma vez
  ///
  /// CONFIGURAÇÕES:
  /// 1. BaseOptions: URL base, timeouts, headers padrão
  /// 2. Interceptors: Logging, Auth (ordem importa!)
  DioClient._internal(AuthInterceptor authInterceptor) {
    _dio = Dio(
      BaseOptions(
        // URL base da API (todas as requisições usarão este prefixo)
        // Exemplo: dio.get('/reviews') → http://10.0.2.2:8080/api/reviews
        baseUrl: ApiConstants.baseUrl,

        // Timeouts configurados no ApiConstants
        connectTimeout: ApiConstants.connectTimeout,
        receiveTimeout: ApiConstants.receiveTimeout,
        sendTimeout: ApiConstants.sendTimeout,

        // Headers padrão (todas requisições incluem automaticamente)
        headers: {
          ApiConstants.contentTypeHeader: ApiConstants.jsonContentType,
          ApiConstants.acceptHeader: ApiConstants.jsonContentType,
        },

        // Response type: JSON (parse automático para Map<String, dynamic>)
        responseType: ResponseType.json,
      ),
    );

    // =========================================================================
    // Adicionar Interceptors
    // =========================================================================
    //
    // ORDEM IMPORTA!
    // 1. LoggingInterceptor (primeiro para logar TUDO)
    // 2. AuthInterceptor (adiciona token após logging inicial)
    //
    // FLUXO DE REQUISIÇÃO:
    // Request → LoggingInterceptor.onRequest → AuthInterceptor.onRequest → Servidor
    //
    // FLUXO DE RESPOSTA:
    // Servidor → AuthInterceptor.onResponse → LoggingInterceptor.onResponse → App
    //
    // FLUXO DE ERRO:
    // Servidor → AuthInterceptor.onError → LoggingInterceptor.onError → App
    _dio.interceptors.addAll([
      LoggingInterceptor(), // 1. Logging (debug)
      authInterceptor,      // 2. Auth (adiciona JWT token)
    ]);
  }

  // =========================================================================
  // Getter - acesso ao Dio
  // =========================================================================

  /// Expõe instância do Dio para uso externo
  ///
  /// USO (em services):
  /// ```dart
  /// class ReviewService {
  ///   final DioClient _client;
  ///
  ///   ReviewService(this._client);
  ///
  ///   Future<List<Review>> getReviews() async {
  ///     final response = await _client.dio.get('/reviews');
  ///     return parseReviews(response.data);
  ///   }
  /// }
  /// ```
  Dio get dio => _dio;

  // =========================================================================
  // Métodos de conveniência (opcional - wrappers para métodos comuns)
  // =========================================================================

  /// GET request
  ///
  /// EXEMPLO:
  /// ```dart
  /// // Listar reviews
  /// final response = await dioClient.get(
  ///   ApiConstants.reviews,
  ///   queryParameters: {'page': 0, 'size': 20},
  /// );
  /// ```
  Future<Response> get(
    String path, {
    Map<String, dynamic>? queryParameters,
    Options? options,
    CancelToken? cancelToken,
  }) async {
    return await _dio.get(
      path,
      queryParameters: queryParameters,
      options: options,
      cancelToken: cancelToken,
    );
  }

  /// POST request
  ///
  /// EXEMPLO:
  /// ```dart
  /// // Criar review
  /// final response = await dioClient.post(
  ///   ApiConstants.createReview,
  ///   data: {
  ///     'wineId': 'uuid',
  ///     'rating': 5,
  ///     'notes': 'Excelente!',
  ///   },
  /// );
  /// ```
  Future<Response> post(
    String path, {
    dynamic data,
    Map<String, dynamic>? queryParameters,
    Options? options,
    CancelToken? cancelToken,
  }) async {
    return await _dio.post(
      path,
      data: data,
      queryParameters: queryParameters,
      options: options,
      cancelToken: cancelToken,
    );
  }

  /// PUT request
  ///
  /// EXEMPLO:
  /// ```dart
  /// // Atualizar review
  /// final response = await dioClient.put(
  ///   ApiConstants.updateReview('uuid-123'),
  ///   data: {
  ///     'rating': 4,
  ///     'notes': 'Mudei de opinião',
  ///   },
  /// );
  /// ```
  Future<Response> put(
    String path, {
    dynamic data,
    Map<String, dynamic>? queryParameters,
    Options? options,
    CancelToken? cancelToken,
  }) async {
    return await _dio.put(
      path,
      data: data,
      queryParameters: queryParameters,
      options: options,
      cancelToken: cancelToken,
    );
  }

  /// DELETE request
  ///
  /// EXEMPLO:
  /// ```dart
  /// // Deletar review
  /// final response = await dioClient.delete(
  ///   ApiConstants.deleteReview('uuid-123'),
  /// );
  /// ```
  Future<Response> delete(
    String path, {
    dynamic data,
    Map<String, dynamic>? queryParameters,
    Options? options,
    CancelToken? cancelToken,
  }) async {
    return await _dio.delete(
      path,
      data: data,
      queryParameters: queryParameters,
      options: options,
      cancelToken: cancelToken,
    );
  }
}

/// COMO USAR (passo a passo):
///
/// 1. CRIAR DEPENDÊNCIAS (normalmente em providers):
/// ```dart
/// // lib/core/providers/network_providers.dart
/// import 'package:flutter_riverpod/flutter_riverpod.dart';
/// import 'package:flutter_secure_storage/flutter_secure_storage.dart';
///
/// // Provider para FlutterSecureStorage
/// final secureStorageProvider = Provider<FlutterSecureStorage>((ref) {
///   return const FlutterSecureStorage();
/// });
///
/// // Provider para AuthInterceptor
/// final authInterceptorProvider = Provider<AuthInterceptor>((ref) {
///   final storage = ref.watch(secureStorageProvider);
///   return AuthInterceptor(storage);
/// });
///
/// // Provider para DioClient
/// final dioClientProvider = Provider<DioClient>((ref) {
///   final authInterceptor = ref.watch(authInterceptorProvider);
///   return DioClient(authInterceptor);
/// });
/// ```
///
/// 2. USAR EM SERVICES:
/// ```dart
/// // lib/features/review/data/review_service.dart
/// class ReviewService {
///   final DioClient _client;
///
///   ReviewService(this._client);
///
///   Future<List<Review>> getReviews() async {
///     try {
///       // Requisição GET /reviews
///       final response = await _client.get(ApiConstants.reviews);
///
///       // Parse resposta
///       final List<dynamic> data = response.data['content'];
///       return data.map((json) => Review.fromJson(json)).toList();
///     } on DioException catch (e) {
///       // Tratar erros (404, 401, timeout, etc.)
///       throw _handleError(e);
///     }
///   }
/// }
/// ```
///
/// 3. INJETAR COM RIVERPOD:
/// ```dart
/// // Provider para ReviewService
/// final reviewServiceProvider = Provider<ReviewService>((ref) {
///   final client = ref.watch(dioClientProvider);
///   return ReviewService(client);
/// });
///
/// // Usar no widget
/// class ReviewListScreen extends ConsumerWidget {
///   @override
///   Widget build(BuildContext context, WidgetRef ref) {
///     final reviewService = ref.watch(reviewServiceProvider);
///     // ...usar reviewService para carregar reviews
///   }
/// }
/// ```
///
/// BOAS PRÁTICAS:
/// - Sempre injetar DioClient via Riverpod (não criar diretamente)
/// - Usar métodos wrapper (get, post, put, delete) para simplicidade
/// - Tratar DioException em services (não deixar propagar para UI)
/// - Manter DioClient focado em configuração HTTP (não adicionar lógica de negócio)
///
/// SEGURANÇA:
/// - JWT token adicionado automaticamente por AuthInterceptor
/// - HTTPS obrigatório em produção (mude baseUrl no ApiConstants)
/// - Logs desabilitados em release builds (LoggingInterceptor usa kDebugMode)
