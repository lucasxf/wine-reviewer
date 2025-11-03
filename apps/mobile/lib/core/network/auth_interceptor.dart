import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:wine_reviewer_mobile/core/constants/api_constants.dart';
import 'package:wine_reviewer_mobile/core/storage/storage_keys.dart';

/// Interceptor de autenticação - adiciona JWT token automaticamente
///
/// EXPLICAÇÃO (Backend Context):
/// - Similar a um Filter/Interceptor do Spring Security
/// - Adiciona header Authorization: Bearer {token} em toda requisição
/// - Lê token do storage seguro (flutter_secure_storage)
///
/// ANALOGIA Backend (Spring Security):
/// ```java
/// @Component
/// public class JwtAuthenticationFilter extends OncePerRequestFilter {
///   @Override
///   protected void doFilterInternal(HttpServletRequest request, ...) {
///     String token = extractToken(request);
///     request.addHeader("Authorization", "Bearer " + token);
///     chain.doFilter(request, response);
///   }
/// }
/// ```
///
/// COMO FUNCIONA (Dio Interceptors):
/// 1. Usuário faz requisição: `dio.get('/reviews')`
/// 2. AuthInterceptor intercepta ANTES de enviar
/// 3. Lê JWT token do flutter_secure_storage
/// 4. Adiciona header: Authorization: Bearer {token}
/// 5. Envia requisição com token
///
/// FLUTTER_SECURE_STORAGE:
/// - Armazena dados criptografados (como JWT tokens)
/// - Android: usa KeyStore (hardware encryption)
/// - iOS: usa Keychain (hardware encryption)
/// - Similar a: Spring Security TokenStore, mas no device
class AuthInterceptor extends Interceptor {
  /// Storage seguro para ler JWT token
  ///
  /// IMPORTANTE:
  /// - NÃO usar SharedPreferences para tokens (não é seguro!)
  /// - flutter_secure_storage usa criptografia nativa do SO
  final FlutterSecureStorage _storage;

  AuthInterceptor(this._storage);

  /// onRequest - Intercepta requisição ANTES de enviar
  ///
  /// PARÂMETROS:
  /// - options: Configurações da requisição (URL, headers, method, etc.)
  /// - handler: Permite continuar (resolve) ou cancelar (reject) requisição
  ///
  /// FLUXO:
  /// 1. Lê token do storage
  /// 2. Se token existe, adiciona no header Authorization
  /// 3. Continua com requisição modificada
  @override
  void onRequest(
    RequestOptions options,
    RequestInterceptorHandler handler,
  ) async {
    // Lê JWT token do storage seguro
    final token = await _storage.read(key: StorageKeys.authToken);

    // Se token existe, adiciona no header Authorization
    if (token != null && token.isNotEmpty) {
      options.headers[ApiConstants.authorizationHeader] = 'Bearer $token';
    }

    // IMPORTANTE: sempre chamar handler.next() para continuar requisição
    // Se não chamar, requisição fica travada!
    return handler.next(options);
  }

  /// onError - Intercepta erros de requisição
  ///
  /// USO FUTURO (não implementado ainda):
  /// - Detectar erro 401 Unauthorized (token expirado)
  /// - Tentar refresh token automaticamente
  /// - Se refresh falhar, redirecionar para login
  ///
  /// ANALOGIA Backend:
  /// - É como um @ExceptionHandler no Spring
  /// - Trata erros de autenticação de forma centralizada
  @override
  void onError(DioException err, ErrorInterceptorHandler handler) {
    // TODO (futuro): Implementar refresh token automático
    // if (err.response?.statusCode == 401) {
    //   // Token expirado, tentar refresh
    //   final newToken = await refreshToken();
    //   if (newToken != null) {
    //     // Retry requisição com novo token
    //     return handler.resolve(await _retry(err.requestOptions));
    //   } else {
    //     // Refresh falhou, logout e redirecionar para login
    //     await logout();
    //   }
    // }

    // Por enquanto, apenas propaga erro para próximo interceptor
    return handler.next(err);
  }

  /// Salva JWT token no storage seguro
  ///
  /// QUANDO USAR:
  /// - Após login com sucesso
  /// - Após refresh token
  ///
  /// EXEMPLO:
  /// ```dart
  /// // No AuthService, após login bem-sucedido:
  /// final response = await dio.post('/auth/google', data: {...});
  /// final token = response.data['token'];
  /// await authInterceptor.saveToken(token);
  /// ```
  Future<void> saveToken(String token) async {
    await _storage.write(key: StorageKeys.authToken, value: token);
  }

  /// Remove JWT token do storage (logout)
  ///
  /// QUANDO USAR:
  /// - Logout manual do usuário
  /// - Token inválido ou expirado (sem refresh disponível)
  ///
  /// EXEMPLO:
  /// ```dart
  /// // No AuthService, quando usuário faz logout:
  /// await authInterceptor.clearToken();
  /// ```
  Future<void> clearToken() async {
    await _storage.delete(key: StorageKeys.authToken);
  }

  /// Verifica se token existe no storage
  ///
  /// QUANDO USAR:
  /// - Verificar se usuário está autenticado (app startup)
  /// - Decidir qual tela mostrar (login ou home)
  ///
  /// EXEMPLO:
  /// ```dart
  /// // No main.dart ou splash screen:
  /// final hasToken = await authInterceptor.hasToken();
  /// if (hasToken) {
  ///   // Redirecionar para home
  /// } else {
  ///   // Redirecionar para login
  /// }
  /// ```
  Future<bool> hasToken() async {
    final token = await _storage.read(key: StorageKeys.authToken);
    return token != null && token.isNotEmpty;
  }
}

/// COMO USAR (integração com Dio):
///
/// ```dart
/// import 'package:dio/dio.dart';
/// import 'package:flutter_secure_storage/flutter_secure_storage.dart';
/// import 'package:wine_reviewer_mobile/core/network/auth_interceptor.dart';
///
/// // 1. Criar storage e interceptor
/// final storage = FlutterSecureStorage();
/// final authInterceptor = AuthInterceptor(storage);
///
/// // 2. Criar Dio e adicionar interceptor
/// final dio = Dio();
/// dio.interceptors.add(authInterceptor);
///
/// // 3. Agora todas as requisições incluem token automaticamente!
/// final response = await dio.get('/reviews'); // Header Authorization adicionado
///
/// // 4. Após login, salvar token:
/// await authInterceptor.saveToken('eyJhbGciOiJIUzUxMiJ9...');
///
/// // 5. Logout:
/// await authInterceptor.clearToken();
/// ```
///
/// BOAS PRÁTICAS:
/// - Sempre use FlutterSecureStorage para tokens (nunca SharedPreferences!)
/// - Chame saveToken() apenas após login bem-sucedido
/// - Chame clearToken() no logout
/// - Use hasToken() para verificar se usuário está autenticado
///
/// SEGURANÇA:
/// - Token armazenado com criptografia nativa (KeyStore/Keychain)
/// - Token NUNCA fica em plain text no device
/// - Se device for rooteado/jailbroken, token ainda está protegido
