/// API constants - URLs, endpoints, timeouts, headers
///
/// EXPLICAÇÃO (Backend Context):
/// - Centraliza todas as constantes relacionadas à API REST
/// - Similar a application.properties do Spring Boot
/// - Evita "magic strings" espalhados pelo código
///
/// ANALOGIA Backend:
/// ```java
/// // Spring Boot - application.properties
/// api.base-url=http://localhost:8080/api
/// api.timeout.connect=10000
/// api.timeout.read=30000
/// ```
class ApiConstants {
  // Private constructor - previne instanciação
  ApiConstants._();

  // =========================================================================
  // Base URLs
  // =========================================================================

  /// Base URL da API backend
  ///
  /// IMPORTANTE - Android Emulator:
  /// - NÃO use 'http://localhost:8080' → localhost do emulator (não funciona)
  /// - NÃO use 'http://127.0.0.1:8080' → mesmo problema
  /// - USE 'http://10.0.2.2:8080' → IP especial que aponta para host Windows
  ///
  /// ANALOGIA:
  /// - É como usar 'host.docker.internal' no Docker Compose
  /// - 10.0.2.2 = localhost da máquina host (Windows)
  ///
  /// AMBIENTES:
  /// - Development (emulator): http://10.0.2.2:8080/api
  /// - Development (dispositivo físico): http://192.168.x.x:8080/api (IP LAN)
  /// - Production: https://api.winereviewer.com/api
  ///
  /// TODO: Usar variáveis de ambiente (--dart-define) para alterar por ambiente
  static const String baseUrl = 'http://10.0.2.2:8080/api';

  // =========================================================================
  // Auth Endpoints - Autenticação
  // =========================================================================

  /// POST /auth/google - Autenticar com Google OAuth
  ///
  /// REQUEST:
  /// ```json
  /// {
  ///   "googleIdToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6..."
  /// }
  /// ```
  ///
  /// RESPONSE (200 OK):
  /// ```json
  /// {
  ///   "token": "eyJhbGciOiJIUzUxMiJ9...",
  ///   "userId": "uuid",
  ///   "email": "user@example.com",
  ///   "displayName": "João Silva",
  ///   "avatarUrl": "https://..."
  /// }
  /// ```
  static const String googleAuth = '/auth/google';

  /// POST /auth/login - Login simples (email only, sem senha por enquanto)
  ///
  /// REQUEST:
  /// ```json
  /// {
  ///   "email": "user@example.com"
  /// }
  /// ```
  ///
  /// RESPONSE (200 OK):
  /// ```json
  /// {
  ///   "token": "eyJhbGciOiJIUzUxMiJ9...",
  ///   "userId": "uuid"
  /// }
  /// ```
  static const String login = '/auth/login';

  // =========================================================================
  // Review Endpoints - Reviews de vinhos
  // =========================================================================

  /// GET /reviews - Listar reviews (paginado)
  ///
  /// QUERY PARAMS:
  /// - page: int (default 0)
  /// - size: int (default 20)
  /// - sort: string (default "createdAt,desc")
  /// - wineId: UUID (opcional - filtrar por vinho)
  /// - userId: UUID (opcional - filtrar por usuário)
  ///
  /// RESPONSE (200 OK):
  /// ```json
  /// {
  ///   "content": [...],
  ///   "totalElements": 100,
  ///   "totalPages": 5,
  ///   "number": 0,
  ///   "size": 20
  /// }
  /// ```
  static const String reviews = '/reviews';

  /// GET /reviews/{id} - Buscar review por ID
  ///
  /// PATH PARAMS:
  /// - id: UUID
  ///
  /// RESPONSE (200 OK):
  /// ```json
  /// {
  ///   "id": "uuid",
  ///   "rating": 5,
  ///   "notes": "Excelente!",
  ///   "imageUrl": "https://...",
  ///   "author": {...},
  ///   "wine": {...},
  ///   "commentCount": 3,
  ///   "createdAt": "2025-10-22T14:30:00Z"
  /// }
  /// ```
  ///
  /// USO:
  /// ```dart
  /// final url = '${ApiConstants.baseUrl}${ApiConstants.reviewById('uuid-123')}';
  /// // Resultado: http://10.0.2.2:8080/api/reviews/uuid-123
  /// ```
  static String reviewById(String id) => '/reviews/$id';

  /// POST /reviews - Criar novo review
  ///
  /// HEADERS:
  /// - Authorization: Bearer <jwt-token>
  ///
  /// REQUEST:
  /// ```json
  /// {
  ///   "wineId": "uuid",
  ///   "rating": 5,
  ///   "notes": "Excelente vinho!",
  ///   "imageUrl": "https://..." // opcional
  /// }
  /// ```
  ///
  /// RESPONSE (201 Created):
  /// ```json
  /// {
  ///   "id": "uuid",
  ///   "rating": 5,
  ///   ...
  /// }
  /// ```
  static const String createReview = '/reviews';

  /// PUT /reviews/{id} - Atualizar review
  ///
  /// HEADERS:
  /// - Authorization: Bearer <jwt-token>
  ///
  /// REQUEST:
  /// ```json
  /// {
  ///   "rating": 4,
  ///   "notes": "Mudei de opinião",
  ///   "imageUrl": "https://..."
  /// }
  /// ```
  ///
  /// RESPONSE (200 OK):
  /// ```json
  /// {
  ///   "id": "uuid",
  ///   "rating": 4,
  ///   ...
  /// }
  /// ```
  static String updateReview(String id) => '/reviews/$id';

  /// DELETE /reviews/{id} - Deletar review
  ///
  /// HEADERS:
  /// - Authorization: Bearer <jwt-token>
  ///
  /// RESPONSE (204 No Content)
  static String deleteReview(String id) => '/reviews/$id';

  // =========================================================================
  // Comment Endpoints - Comentários (futuro)
  // =========================================================================

  /// GET /reviews/{reviewId}/comments - Listar comentários de um review
  static String reviewComments(String reviewId) => '/reviews/$reviewId/comments';

  /// POST /reviews/{reviewId}/comments - Criar comentário
  static String createComment(String reviewId) => '/reviews/$reviewId/comments';

  // =========================================================================
  // Wine Endpoints - Vinhos (futuro)
  // =========================================================================

  /// GET /wines - Listar vinhos
  static const String wines = '/wines';

  /// GET /wines/{id} - Buscar vinho por ID
  static String wineById(String id) => '/wines/$id';

  // =========================================================================
  // Timeouts - Tempos limite para requisições HTTP
  // =========================================================================

  /// Connect Timeout - Tempo limite para estabelecer conexão TCP
  ///
  /// EXPLICAÇÃO:
  /// - Tempo máximo para conectar ao servidor (TCP handshake)
  /// - Se servidor não responder em 10s, falha com DioException (type: connectTimeout)
  ///
  /// ANALOGIA Backend:
  /// - RestTemplate.setConnectTimeout(10000) // 10 segundos
  static const Duration connectTimeout = Duration(seconds: 10);

  /// Receive Timeout - Tempo limite para receber resposta completa
  ///
  /// EXPLICAÇÃO:
  /// - Tempo máximo para receber dados após conexão estabelecida
  /// - Se servidor não enviar resposta em 30s, falha com DioException (type: receiveTimeout)
  /// - Útil para operações longas (upload de imagem, processamento pesado)
  ///
  /// ANALOGIA Backend:
  /// - RestTemplate.setReadTimeout(30000) // 30 segundos
  static const Duration receiveTimeout = Duration(seconds: 30);

  /// Send Timeout - Tempo limite para enviar requisição
  ///
  /// EXPLICAÇÃO:
  /// - Tempo máximo para enviar dados (upload de imagem grande)
  /// - Se não conseguir enviar em 30s, falha com DioException (type: sendTimeout)
  static const Duration sendTimeout = Duration(seconds: 30);

  // =========================================================================
  // HTTP Headers - Nomes de headers usados
  // =========================================================================

  /// Authorization header - JWT token
  ///
  /// USO:
  /// ```dart
  /// final headers = {
  ///   ApiConstants.authorizationHeader: 'Bearer $jwtToken',
  /// };
  /// ```
  static const String authorizationHeader = 'Authorization';

  /// Content-Type header - Tipo do conteúdo (JSON)
  static const String contentTypeHeader = 'Content-Type';

  /// Accept header - Tipo de resposta aceita (JSON)
  static const String acceptHeader = 'Accept';

  /// Content-Type: application/json
  static const String jsonContentType = 'application/json';

  // =========================================================================
  // Pagination - Valores padrão de paginação
  // =========================================================================

  /// Página inicial padrão (zero-indexed, como Spring Data)
  static const int defaultPage = 0;

  /// Tamanho de página padrão (20 itens por página)
  static const int defaultPageSize = 20;

  /// Ordenação padrão (mais recente primeiro)
  static const String defaultSort = 'createdAt,desc';
}

/// COMO USAR (exemplos):
///
/// ```dart
/// import 'package:dio/dio.dart';
/// import 'package:wine_reviewer_mobile/core/constants/api_constants.dart';
///
/// // 1. Construir URL completa
/// final url = '${ApiConstants.baseUrl}${ApiConstants.reviews}';
/// // Resultado: http://10.0.2.2:8080/api/reviews
///
/// // 2. Usar em requisição Dio
/// final dio = Dio();
/// final response = await dio.get(
///   '${ApiConstants.baseUrl}${ApiConstants.reviews}',
///   queryParameters: {
///     'page': ApiConstants.defaultPage,
///     'size': ApiConstants.defaultPageSize,
///   },
/// );
///
/// // 3. Headers com token JWT
/// final headers = {
///   ApiConstants.authorizationHeader: 'Bearer $jwtToken',
///   ApiConstants.contentTypeHeader: ApiConstants.jsonContentType,
/// };
///
/// // 4. Endpoint dinâmico (com path param)
/// final reviewUrl = ApiConstants.reviewById('uuid-123');
/// // Resultado: /reviews/uuid-123
/// ```
///
/// BOAS PRÁTICAS:
/// - SEMPRE usar ApiConstants.* ao invés de strings hard-coded
/// - Se precisar de novo endpoint, adicione aqui (não crie em outro lugar)
/// - Documente REQUEST/RESPONSE de cada endpoint (facilita uso)
