import 'package:dio/dio.dart';

/// Exceções de rede customizadas - Tratamento de erros HTTP
///
/// EXPLICAÇÃO (Backend Context):
/// - Similar a criar custom exceptions no Spring Boot
/// - Mapeia DioException para exceções de domínio (mais semânticas)
/// - UI pode tratar exceções de forma user-friendly
///
/// ANALOGIA Backend (Spring Boot):
/// ```java
/// // Custom exceptions
/// public class NetworkException extends RuntimeException { }
/// public class UnauthorizedException extends NetworkException { }
/// public class NotFoundException extends NetworkException { }
/// public class ServerException extends NetworkException { }
///
/// // Exception handler
/// @ControllerAdvice
/// public class GlobalExceptionHandler {
///   @ExceptionHandler(NotFoundException.class)
///   public ResponseEntity<ErrorResponse> handle(NotFoundException ex) {
///     return ResponseEntity.status(404).body(new ErrorResponse(ex.getMessage()));
///   }
/// }
/// ```
///
/// DIO EXCEPTIONS vs CUSTOM EXCEPTIONS:
/// - DioException: Exceção genérica do Dio (baixo nível, muitos detalhes HTTP)
/// - NetworkException: Exceção de domínio (alto nível, user-friendly)
/// - Conversão: DioException → NetworkException (via NetworkExceptionHandler)

// =============================================================================
// Base Exception
// =============================================================================

/// Exceção base para erros de rede
///
/// HIERARQUIA:
/// ```
/// NetworkException (abstract)
/// ├── UnauthorizedException (401)
/// ├── ForbiddenException (403)
/// ├── NotFoundException (404)
/// ├── ServerException (5xx)
/// ├── TimeoutException (timeout)
/// ├── ConnectionException (sem internet)
/// └── UnknownNetworkException (outros)
/// ```
abstract class NetworkException implements Exception {
  final String message;
  final int? statusCode;

  NetworkException(this.message, [this.statusCode]);

  @override
  String toString() => 'NetworkException: $message (statusCode: $statusCode)';
}

// =============================================================================
// HTTP Status Code Exceptions (4xx, 5xx)
// =============================================================================

/// 401 Unauthorized - Token inválido ou expirado
///
/// QUANDO OCORRE:
/// - JWT token expirado
/// - Token inválido ou malformado
/// - Usuário não autenticado
///
/// TRATAMENTO NA UI:
/// - Mostrar mensagem "Sessão expirada"
/// - Redirecionar para tela de login
/// - Limpar token do storage
class UnauthorizedException extends NetworkException {
  UnauthorizedException([String? message])
      : super(message ?? 'Não autorizado. Por favor, faça login novamente.', 401);
}

/// 403 Forbidden - Usuário não tem permissão
///
/// QUANDO OCORRE:
/// - Tentativa de editar/deletar review de outro usuário
/// - Acesso a recurso sem permissão
///
/// TRATAMENTO NA UI:
/// - Mostrar mensagem "Você não tem permissão para realizar esta ação"
/// - Desabilitar botões de ações não permitidas
class ForbiddenException extends NetworkException {
  ForbiddenException([String? message])
      : super(message ?? 'Você não tem permissão para realizar esta ação.', 403);
}

/// 404 Not Found - Recurso não encontrado
///
/// QUANDO OCORRE:
/// - Review não existe
/// - Vinho não existe
/// - Endpoint incorreto
///
/// TRATAMENTO NA UI:
/// - Mostrar mensagem "Review não encontrado"
/// - Redirecionar para lista de reviews
class NotFoundException extends NetworkException {
  NotFoundException([String? message])
      : super(message ?? 'Recurso não encontrado.', 404);
}

/// 400 Bad Request - Dados inválidos
///
/// QUANDO OCORRE:
/// - Rating fora do intervalo (1-5)
/// - Campos obrigatórios faltando
/// - Formato de dados incorreto
///
/// TRATAMENTO NA UI:
/// - Mostrar mensagem de erro específica
/// - Destacar campo com erro
class BadRequestException extends NetworkException {
  BadRequestException([String? message])
      : super(message ?? 'Requisição inválida. Verifique os dados enviados.', 400);
}

/// 422 Unprocessable Entity - Violação de regra de negócio
///
/// QUANDO OCORRE:
/// - Ano do vinho inválido
/// - Regra de negócio violada
///
/// TRATAMENTO NA UI:
/// - Mostrar mensagem de erro da regra de negócio
/// - Explicar qual regra foi violada
class UnprocessableEntityException extends NetworkException {
  UnprocessableEntityException([String? message])
      : super(message ?? 'Não foi possível processar a requisição.', 422);
}

/// 5xx Server Error - Erro no servidor
///
/// QUANDO OCORRE:
/// - Exception não tratada no backend
/// - Database down
/// - Serviço indisponível
///
/// TRATAMENTO NA UI:
/// - Mostrar mensagem "Erro no servidor. Tente novamente mais tarde."
/// - Oferecer botão para retry
class ServerException extends NetworkException {
  ServerException([String? message])
      : super(message ?? 'Erro no servidor. Tente novamente mais tarde.', 500);
}

// =============================================================================
// Connection/Timeout Exceptions
// =============================================================================

/// Timeout - Requisição demorou muito
///
/// QUANDO OCORRE:
/// - connectTimeout: Servidor não responde (10s)
/// - sendTimeout: Upload demorou muito (30s)
/// - receiveTimeout: Servidor não enviou resposta (30s)
///
/// TRATAMENTO NA UI:
/// - Mostrar mensagem "A requisição demorou muito. Verifique sua conexão."
/// - Oferecer botão para retry
class TimeoutException extends NetworkException {
  TimeoutException([String? message])
      : super(message ?? 'A requisição demorou muito. Tente novamente.', null);
}

/// Connection Error - Sem internet ou servidor inacessível
///
/// QUANDO OCORRE:
/// - WiFi/dados móveis desligados
/// - Servidor offline
/// - DNS não resolve
/// - Firewall bloqueia
///
/// TRATAMENTO NA UI:
/// - Mostrar mensagem "Sem conexão com internet"
/// - Mostrar ícone de "offline"
/// - Oferecer botão para retry
class ConnectionException extends NetworkException {
  ConnectionException([String? message])
      : super(message ?? 'Falha na conexão. Verifique sua internet.', null);
}

/// Unknown Error - Erro desconhecido
///
/// QUANDO OCORRE:
/// - Erro não mapeado
/// - Situação inesperada
///
/// TRATAMENTO NA UI:
/// - Mostrar mensagem genérica
/// - Logar erro para investigação
class UnknownNetworkException extends NetworkException {
  UnknownNetworkException([String? message])
      : super(message ?? 'Erro desconhecido. Tente novamente.', null);
}

// =============================================================================
// Exception Handler - Converte DioException → NetworkException
// =============================================================================

/// Handler para converter DioException em NetworkException
///
/// USO (em services):
/// ```dart
/// try {
///   final response = await _client.get('/reviews');
///   return parseReviews(response.data);
/// } on DioException catch (e) {
///   throw NetworkExceptionHandler.handleError(e);
/// }
/// ```
class NetworkExceptionHandler {
  /// Converte DioException em NetworkException
  ///
  /// MAPEAMENTO:
  /// - DioExceptionType.badResponse → mapeia por status code (400, 401, 404, 5xx)
  /// - DioExceptionType.connectionTimeout → TimeoutException
  /// - DioExceptionType.sendTimeout → TimeoutException
  /// - DioExceptionType.receiveTimeout → TimeoutException
  /// - DioExceptionType.connectionError → ConnectionException
  /// - DioExceptionType.cancel → Ignora (requisição cancelada pelo usuário)
  /// - DioExceptionType.unknown → UnknownNetworkException
  static NetworkException handleError(DioException error) {
    switch (error.type) {
      // ========================================================================
      // Erros HTTP (status code 4xx, 5xx)
      // ========================================================================
      case DioExceptionType.badResponse:
        final statusCode = error.response?.statusCode;
        final message = _extractErrorMessage(error.response);

        switch (statusCode) {
          case 400:
            return BadRequestException(message);
          case 401:
            return UnauthorizedException(message);
          case 403:
            return ForbiddenException(message);
          case 404:
            return NotFoundException(message);
          case 422:
            return UnprocessableEntityException(message);
          case 500:
          case 501:
          case 502:
          case 503:
          case 504:
            return ServerException(message);
          default:
            return UnknownNetworkException(message);
        }

      // ========================================================================
      // Timeout Errors
      // ========================================================================
      case DioExceptionType.connectionTimeout:
      case DioExceptionType.sendTimeout:
      case DioExceptionType.receiveTimeout:
        return TimeoutException();

      // ========================================================================
      // Connection Errors (sem internet, servidor offline)
      // ========================================================================
      case DioExceptionType.connectionError:
        return ConnectionException();

      // ========================================================================
      // Request Cancelled (usuário cancelou)
      // ========================================================================
      case DioExceptionType.cancel:
        return UnknownNetworkException('Requisição cancelada');

      // ========================================================================
      // Unknown Errors
      // ========================================================================
      case DioExceptionType.badCertificate:
        return UnknownNetworkException('Certificado SSL inválido');

      case DioExceptionType.unknown:
        return UnknownNetworkException(error.message);
    }
  }

  /// Extrai mensagem de erro da resposta HTTP
  ///
  /// FORMATO BACKEND (Spring Boot):
  /// ```json
  /// {
  ///   "timestamp": "2025-10-25T12:00:00Z",
  ///   "status": 404,
  ///   "error": "Not Found",
  ///   "message": "Review not found",
  ///   "path": "/api/reviews/uuid-invalid"
  /// }
  /// ```
  ///
  /// EXTRAÇÃO:
  /// 1. Tenta ler response.data['message']
  /// 2. Se não existe, usa response.statusMessage
  /// 3. Se não existe, retorna null (usa mensagem padrão da exception)
  static String? _extractErrorMessage(Response? response) {
    if (response?.data is Map) {
      // Backend retornou JSON com campo 'message'
      return response?.data['message'] as String?;
    }

    // Fallback: usar statusMessage (Not Found, Internal Server Error, etc.)
    return response?.statusMessage;
  }
}

/// COMO USAR (em services):
///
/// ```dart
/// class ReviewService {
///   final DioClient _client;
///
///   ReviewService(this._client);
///
///   Future<List<Review>> getReviews() async {
///     try {
///       final response = await _client.get(ApiConstants.reviews);
///       final List<dynamic> data = response.data['content'];
///       return data.map((json) => Review.fromJson(json)).toList();
///     } on DioException catch (e) {
///       // Converte DioException → NetworkException
///       throw NetworkExceptionHandler.handleError(e);
///     }
///   }
///
///   Future<void> deleteReview(String id) async {
///     try {
///       await _client.delete(ApiConstants.deleteReview(id));
///     } on DioException catch (e) {
///       throw NetworkExceptionHandler.handleError(e);
///     }
///   }
/// }
/// ```
///
/// TRATAMENTO NA UI:
///
/// ```dart
/// // Widget com tratamento de erro
/// class ReviewListScreen extends ConsumerWidget {
///   @override
///   Widget build(BuildContext context, WidgetRef ref) {
///     return FutureBuilder<List<Review>>(
///       future: ref.read(reviewServiceProvider).getReviews(),
///       builder: (context, snapshot) {
///         if (snapshot.hasError) {
///           final error = snapshot.error;
///
///           // Tratamento específico por tipo de erro
///           if (error is UnauthorizedException) {
///             // Redirecionar para login
///             Navigator.of(context).pushReplacementNamed('/login');
///           } else if (error is ConnectionException) {
///             // Mostrar mensagem de sem internet
///             return ErrorWidget('Sem conexão com internet');
///           } else if (error is ServerException) {
///             // Mostrar mensagem de erro do servidor
///             return ErrorWidget('Erro no servidor. Tente novamente.');
///           }
///         }
///
///         // ... UI normal
///       },
///     );
///   }
/// }
/// ```
///
/// BOAS PRÁTICAS:
/// - Sempre converter DioException → NetworkException em services
/// - Nunca deixar DioException vazar para UI (baixo nível, difícil tratar)
/// - Tratar cada tipo de exception na UI de forma user-friendly
/// - Logar erros desconhecidos para investigação
/// - Oferecer retry para erros recuperáveis (timeout, connection, server)
