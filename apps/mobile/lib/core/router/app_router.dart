import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:wine_reviewer_mobile/features/auth/presentation/screens/login_screen.dart';
import 'package:wine_reviewer_mobile/features/review/presentation/screens/home_screen.dart';
import 'package:wine_reviewer_mobile/features/review/presentation/screens/review_details_screen.dart';
import 'package:wine_reviewer_mobile/features/splash/presentation/screens/splash_screen.dart';

/// Configuração de rotas do app - go_router
///
/// EXPLICAÇÃO (O que é):
/// - Define TODAS as rotas (URLs) do aplicativo
/// - Mapeia URL → Screen (similar a @GetMapping do Spring)
/// - Gerencia navegação, deep linking, parâmetros
///
/// ANALOGIA Backend (Spring Boot):
/// ```java
/// @RestController
/// public class AppController {
///   @GetMapping("/")
///   public String splash() { return "splash"; }
///
///   @GetMapping("/login")
///   public String login() { return "login"; }
///
///   @GetMapping("/home")
///   public String home() { return "home"; }
///
///   @GetMapping("/review/{id}")
///   public String reviewDetails(@PathVariable String id) {
///     return "review-details";
///   }
/// }
/// ```
///
/// GO_ROUTER (navegação declarativa):
/// - Define rotas de forma declarativa (lista de rotas)
/// - Suporta: deep linking, parâmetros, query strings, redirecionamento
/// - Integração com Material/Cupertino navigation
///
/// POR QUE GO_ROUTER (vs Navigator 1.0)?
/// - ✅ Deep linking nativo (abrir /review/123 diretamente)
/// - ✅ URL-based navigation (similar a web)
/// - ✅ Type-safe navigation (erros em compile-time)
/// - ✅ Suporte a parâmetros de rota
/// - ✅ Redirecionamento condicional
/// - ✅ Integração com back button do Android
///
/// ROTAS DO APP:
/// - / → SplashScreen (verifica autenticação)
/// - /login → LoginScreen (autenticação)
/// - /home → HomeScreen (feed de reviews)
/// - /review/:id → ReviewDetailsScreen (detalhes do review)

/// Provider do router (Riverpod)
///
/// IMPORTANTE:
/// - Por enquanto, retorna GoRouter diretamente
/// - Futuramente: converter para Provider (Riverpod) para injeção de dependência
///
/// EXEMPLO (futuro):
/// ```dart
/// final routerProvider = Provider<GoRouter>((ref) {
///   final authInterceptor = ref.watch(authInterceptorProvider);
///   return createRouter(authInterceptor);
/// });
/// ```
GoRouter createAppRouter() {
  return GoRouter(
    // initialLocation = rota inicial do app
    // Sempre começa na splash screen (verifica autenticação)
    initialLocation: '/',

    // debugLogDiagnostics = mostra logs de navegação no console (debug mode)
    // Útil para debugar problemas de navegação
    debugLogDiagnostics: true,

    // routes = lista de rotas do aplicativo
    //
    // TIPOS DE ROTAS:
    // - GoRoute: rota simples (uma tela)
    // - ShellRoute: rota com layout persistente (bottom nav, drawer)
    // - StatefulShellRoute: shell com múltiplas páginas (tabs)
    routes: [
      // ========================================================================
      // Splash Screen (/) - Rota inicial
      // ========================================================================
      //
      // FLUXO:
      // 1. App abre → mostra splash
      // 2. Verifica token no storage
      // 3. Redireciona para /home (se autenticado) ou /login (se não)
      GoRoute(
        // path = URL da rota
        path: '/',

        // name = nome da rota (opcional, facilita navegação)
        // Permite: context.goNamed('splash') ao invés de context.go('/')
        name: 'splash',

        // builder = função que constrói a screen
        //
        // PARÂMETROS:
        // - context: BuildContext (acesso a tema, navegação, etc.)
        // - state: GoRouterState (informações da rota: params, query, etc.)
        //
        // RETORNA: Widget (a screen)
        builder: (context, state) => const SplashScreen(),
      ),

      // ========================================================================
      // Login Screen (/login)
      // ========================================================================
      //
      // QUANDO ACESSADA:
      // - Redirecionamento da splash (usuário não autenticado)
      // - Logout manual (usuário clicou em "Sair")
      // - Token expirado (401 Unauthorized)
      GoRoute(
        path: '/login',
        name: 'login',
        builder: (context, state) => const LoginScreen(),
      ),

      // ========================================================================
      // Home Screen (/home) - Feed de reviews
      // ========================================================================
      //
      // QUANDO ACESSADA:
      // - Redirecionamento da splash (usuário autenticado)
      // - Login bem-sucedido
      // - Voltar de tela de detalhes (back button)
      GoRoute(
        path: '/home',
        name: 'home',
        builder: (context, state) => const HomeScreen(),
      ),

      // ========================================================================
      // Review Details Screen (/review/:id) - Detalhes do review
      // ========================================================================
      //
      // ROTA PARAMETRIZADA:
      // - :id = parâmetro dinâmico (reviewId)
      // - Exemplo: /review/123 → id = '123'
      //
      // ACESSO AO PARÂMETRO:
      // - state.pathParameters['id']
      //
      // NAVEGAÇÃO:
      // - context.go('/review/123')
      // - context.goNamed('review-details', pathParameters: {'id': '123'})
      GoRoute(
        path: '/review/:id',
        name: 'review-details',
        builder: (context, state) {
          // Extrai parâmetro 'id' da URL
          final reviewId = state.pathParameters['id']!;

          // Retorna screen passando reviewId
          return ReviewDetailsScreen(reviewId: reviewId);
        },
      ),
    ],

    // ========================================================================
    // Error Handling - Rota não encontrada (404)
    // ========================================================================
    //
    // QUANDO OCORRE:
    // - URL inválida (ex: /page-nao-existe)
    // - Deep link com rota inexistente
    //
    // EXEMPLO: usuário abre link /review (sem ID)
    errorBuilder: (context, state) => Scaffold(
      appBar: AppBar(
        title: const Text('Página não encontrada'),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(
              Icons.error_outline,
              size: 80,
              color: Colors.grey,
            ),
            const SizedBox(height: 16),
            const Text(
              'Página não encontrada',
              style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 8),
            Text(
              'A página "${state.uri}" não existe',
              style: const TextStyle(color: Colors.grey),
            ),
            const SizedBox(height: 24),
            ElevatedButton(
              onPressed: () => context.go('/home'),
              child: const Text('Voltar para Home'),
            ),
          ],
        ),
      ),
    ),

    // ========================================================================
    // Redirect (Redirecionamento condicional) - Futuro
    // ========================================================================
    //
    // QUANDO USAR:
    // - Proteger rotas autenticadas (redirecionar para login se não autenticado)
    // - Redirecionar usuário autenticado de /login para /home
    // - Forçar onboarding na primeira vez
    //
    // EXEMPLO (futuro - quando implementar autenticação):
    // ```dart
    // redirect: (context, state) {
    //   final isAuthenticated = authState.isAuthenticated;
    //   final isGoingToLogin = state.uri.toString() == '/login';
    //
    //   // Se não autenticado e não indo para login → redireciona para login
    //   if (!isAuthenticated && !isGoingToLogin) {
    //     return '/login';
    //   }
    //
    //   // Se autenticado e indo para login → redireciona para home
    //   if (isAuthenticated && isGoingToLogin) {
    //     return '/home';
    //   }
    //
    //   // Caso contrário, deixa seguir normalmente
    //   return null;
    // },
    // ```
  );
}

/// EXPLICAÇÃO DETALHADA - CONCEITOS IMPORTANTES:
///
/// 1. GO_ROUTER (navegação declarativa):
///
/// Navegação imperativa (Navigator 1.0 - antigo):
/// ```dart
/// Navigator.of(context).push(
///   MaterialPageRoute(builder: (_) => LoginScreen()),
/// );
/// ```
///
/// Navegação declarativa (go_router - moderno):
/// ```dart
/// context.go('/login');
/// ```
///
/// Vantagens:
/// - URL-based (similar a web)
/// - Deep linking automático
/// - Type-safe com named routes
/// - Mais fácil de testar
///
/// 2. ROTAS PARAMETRIZADAS:
///
/// Definição:
/// ```dart
/// GoRoute(
///   path: '/review/:id',       // :id = parâmetro
///   builder: (context, state) {
///     final id = state.pathParameters['id'];
///     return ReviewDetailsScreen(reviewId: id);
///   },
/// )
/// ```
///
/// Navegação:
/// ```dart
/// // Forma 1: path direto
/// context.go('/review/123');
///
/// // Forma 2: named route + params
/// context.goNamed('review-details', pathParameters: {'id': '123'});
/// ```
///
/// 3. QUERY PARAMETERS (futuro):
///
/// Definição (não precisa definir na rota):
/// ```dart
/// GoRoute(
///   path: '/reviews',
///   builder: (context, state) {
///     final page = state.uri.queryParameters['page'] ?? '0';
///     final size = state.uri.queryParameters['size'] ?? '20';
///     return ReviewListScreen(page: page, size: size);
///   },
/// )
/// ```
///
/// Navegação:
/// ```dart
/// context.go('/reviews?page=2&size=10');
/// ```
///
/// 4. MÉTODOS DE NAVEGAÇÃO:
///
/// context.go() - substitui tela (não pode voltar):
/// ```dart
/// context.go('/home');  // Substitui tela atual
/// ```
///
/// context.push() - empilha tela (pode voltar):
/// ```dart
/// context.push('/review/123');  // Adiciona tela na pilha
/// ```
///
/// context.pop() - volta para tela anterior:
/// ```dart
/// context.pop();  // Equivalente a Navigator.pop()
/// ```
///
/// context.goNamed() - navegação com nome:
/// ```dart
/// context.goNamed('review-details', pathParameters: {'id': '123'});
/// ```
///
/// 5. DEEP LINKING (abrir URL diretamente):
///
/// Exemplos:
/// - wine-reviewer://review/123 → abre app na tela de detalhes
/// - https://winereviewer.com/review/123 → abre app na tela de detalhes
///
/// Configuração (Android):
/// - AndroidManifest.xml: <intent-filter> com <data android:scheme="wine-reviewer">
///
/// Configuração (iOS):
/// - Info.plist: CFBundleURLSchemes
///
/// 6. REDIRECT (proteção de rotas):
///
/// Uso comum:
/// ```dart
/// redirect: (context, state) {
///   final isAuthenticated = checkAuth();
///   final isGoingToLogin = state.uri.toString() == '/login';
///
///   // Protege rotas autenticadas
///   if (!isAuthenticated && !isGoingToLogin) {
///     return '/login';
///   }
///
///   return null; // Segue normalmente
/// },
/// ```
///
/// 7. ERROR BUILDER (404):
///
/// Página de erro customizada:
/// ```dart
/// errorBuilder: (context, state) => ErrorScreen(
///   message: 'Página não encontrada: ${state.uri}',
/// ),
/// ```
///
/// ANALOGIA COMPLETA:
/// - GoRoute = @GetMapping (Spring Boot)
/// - path = @RequestMapping(path="/review/{id}")
/// - pathParameters = @PathVariable
/// - queryParameters = @RequestParam
/// - redirect = Filter/Interceptor (proteção de rotas)
/// - errorBuilder = @ExceptionHandler (404 handler)
/// - context.go() = redirect:/ (Spring MVC)
/// - context.push() = forward:/ (Spring MVC)
///
/// ESTRUTURA DE NAVEGAÇÃO DO APP:
/// ```
/// / (Splash)
/// ├── /login (Login)
/// │   └── /home (Home - após login)
/// └── /home (Home - já autenticado)
///     └── /review/:id (Review Details)
/// ```
///
/// FLUXO COMPLETO:
/// 1. App abre → Splash (/)
/// 2. Verifica token
/// 3a. Se autenticado → Home (/home)
/// 3b. Se não autenticado → Login (/login)
/// 4. Login bem-sucedido → Home (/home)
/// 5. Clica em review → Details (/review/123)
/// 6. Clica em voltar → Home (/home)
/// 7. Clica em logout → Login (/login)
