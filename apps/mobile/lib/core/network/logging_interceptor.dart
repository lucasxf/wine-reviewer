import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';

/// Interceptor de logging - loga requisições e respostas HTTP
///
/// EXPLICAÇÃO (Backend Context):
/// - Similar a um logging filter do Spring Boot
/// - Loga método, URL, headers, body de requests e responses
/// - Útil para debug e troubleshooting
///
/// ANALOGIA Backend (Spring Boot):
/// ```java
/// @Slf4j
/// @Component
/// public class LoggingFilter extends OncePerRequestFilter {
///   @Override
///   protected void doFilterInternal(HttpServletRequest request, ...) {
///     log.info("Request: {} {}", request.getMethod(), request.getRequestURI());
///     log.info("Headers: {}", request.getHeaders());
///     chain.doFilter(request, response);
///     log.info("Response: {} - {}", response.getStatus(), response.getBody());
///   }
/// }
/// ```
///
/// IMPORTANTE - Logs apenas em DEBUG MODE:
/// - kDebugMode = true: Logs habilitados (development)
/// - kDebugMode = false: Logs desabilitados (production/release)
/// - Evita expor dados sensíveis em produção
/// - Similar a: logging.level.root=DEBUG (Spring Boot)
class LoggingInterceptor extends Interceptor {
  /// onRequest - Loga requisição ANTES de enviar
  ///
  /// LOGS:
  /// - Método HTTP (GET, POST, PUT, DELETE)
  /// - URL completa
  /// - Headers (incluindo Authorization)
  /// - Body (JSON payload)
  ///
  /// EXEMPLO DE LOG:
  /// ```
  /// ╔════════════════════════════════════════
  /// ║ REQUEST
  /// ╠════════════════════════════════════════
  /// ║ POST http://10.0.2.2:8080/api/reviews
  /// ║ Headers: {Authorization: Bearer eyJ..., Content-Type: application/json}
  /// ║ Body: {"wineId":"uuid","rating":5,"notes":"Excelente!"}
  /// ╚════════════════════════════════════════
  /// ```
  @override
  void onRequest(RequestOptions options, RequestInterceptorHandler handler) {
    // Apenas loga em debug mode (development)
    if (kDebugMode) {
      print('╔════════════════════════════════════════');
      print('║ REQUEST');
      print('╠════════════════════════════════════════');
      print('║ ${options.method} ${options.uri}');
      print('║ Headers: ${options.headers}');
      if (options.data != null) {
        print('║ Body: ${options.data}');
      }
      print('╚════════════════════════════════════════');
    }

    // Continua com requisição
    return handler.next(options);
  }

  /// onResponse - Loga resposta APÓS receber
  ///
  /// LOGS:
  /// - Status code (200, 201, 204, etc.)
  /// - Headers de resposta
  /// - Body da resposta (JSON)
  ///
  /// EXEMPLO DE LOG:
  /// ```
  /// ╔════════════════════════════════════════
  /// ║ RESPONSE
  /// ╠════════════════════════════════════════
  /// ║ Status: 200 OK
  /// ║ URL: http://10.0.2.2:8080/api/reviews
  /// ║ Headers: {Content-Type: application/json}
  /// ║ Body: {"id":"uuid","rating":5,...}
  /// ╚════════════════════════════════════════
  /// ```
  @override
  void onResponse(Response response, ResponseInterceptorHandler handler) {
    // Apenas loga em debug mode
    if (kDebugMode) {
      print('╔════════════════════════════════════════');
      print('║ RESPONSE');
      print('╠════════════════════════════════════════');
      print('║ Status: ${response.statusCode} ${response.statusMessage}');
      print('║ URL: ${response.requestOptions.uri}');
      print('║ Headers: ${response.headers}');
      if (response.data != null) {
        print('║ Body: ${response.data}');
      }
      print('╚════════════════════════════════════════');
    }

    // Continua com resposta
    return handler.next(response);
  }

  /// onError - Loga erros de requisição
  ///
  /// LOGS:
  /// - Tipo de erro (DioExceptionType)
  /// - Status code (se disponível)
  /// - Mensagem de erro
  /// - URL que falhou
  /// - Response body (se disponível)
  ///
  /// TIPOS DE ERRO (DioExceptionType):
  /// - connectionTimeout: Falha ao conectar (timeout)
  /// - sendTimeout: Timeout ao enviar dados
  /// - receiveTimeout: Timeout ao receber resposta
  /// - badResponse: Servidor retornou erro (4xx, 5xx)
  /// - cancel: Requisição cancelada
  /// - connectionError: Erro de conexão (sem internet, DNS falhou)
  /// - unknown: Erro desconhecido
  ///
  /// EXEMPLO DE LOG:
  /// ```
  /// ╔════════════════════════════════════════
  /// ║ ERROR
  /// ╠════════════════════════════════════════
  /// ║ Type: DioExceptionType.badResponse
  /// ║ Status: 404 Not Found
  /// ║ URL: http://10.0.2.2:8080/api/reviews/uuid-invalid
  /// ║ Message: Review not found
  /// ║ Response: {"timestamp":"2025-10-25T...","status":404,...}
  /// ╚════════════════════════════════════════
  /// ```
  @override
  void onError(DioException err, ErrorInterceptorHandler handler) {
    // Apenas loga em debug mode
    if (kDebugMode) {
      print('╔════════════════════════════════════════');
      print('║ ERROR');
      print('╠════════════════════════════════════════');
      print('║ Type: ${err.type}');

      // Se erro tem status code (erro HTTP 4xx/5xx)
      if (err.response != null) {
        print('║ Status: ${err.response?.statusCode} ${err.response?.statusMessage}');
        print('║ URL: ${err.requestOptions.uri}');

        // Log response body (mensagem de erro do backend)
        if (err.response?.data != null) {
          print('║ Response: ${err.response?.data}');
        }
      } else {
        // Erro de conexão (sem resposta do servidor)
        print('║ URL: ${err.requestOptions.uri}');
        print('║ Message: ${err.message}');
      }

      print('╚════════════════════════════════════════');
    }

    // Propaga erro para próximo interceptor
    return handler.next(err);
  }
}

/// COMO USAR (integração com Dio):
///
/// ```dart
/// import 'package:dio/dio.dart';
/// import 'package:wine_reviewer_mobile/core/network/logging_interceptor.dart';
///
/// // Criar Dio e adicionar logging interceptor
/// final dio = Dio();
/// dio.interceptors.add(LoggingInterceptor());
///
/// // Agora todas as requisições são logadas automaticamente!
/// final response = await dio.get('/reviews');
/// // Logs aparecerão no console durante desenvolvimento
/// ```
///
/// ORDEM DE INTERCEPTORS (importante!):
/// ```dart
/// dio.interceptors.add(LoggingInterceptor()); // 1. Logging (primeiro para logar tudo)
/// dio.interceptors.add(AuthInterceptor());    // 2. Auth (adiciona token)
/// ```
///
/// BOAS PRÁTICAS:
/// - Sempre adicione LoggingInterceptor PRIMEIRO (para logar requisições já modificadas)
/// - Logs apenas em kDebugMode (não loga em produção)
/// - Útil para debugar problemas de API
/// - Ver tokens JWT, payloads, erros detalhados
///
/// PRODUÇÃO vs DESENVOLVIMENTO:
/// - Development (kDebugMode = true): Logs habilitados
/// - Production (kDebugMode = false): Logs desabilitados automaticamente
/// - Não precisa remover código ao fazer build de release!
///
/// ANALOGIA Backend:
/// - É como configurar logging.level.root=DEBUG no Spring Boot
/// - Mas com controle automático por ambiente (debug vs release)
