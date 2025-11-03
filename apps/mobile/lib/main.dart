import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:wine_reviewer_mobile/core/router/app_router.dart';
import 'package:wine_reviewer_mobile/core/theme/app_theme.dart';
import 'package:wine_reviewer_mobile/features/auth/providers/auth_providers.dart';

/// Entry point da aplicação Wine Reviewer
///
/// EXPLICAÇÃO (O que é):
/// - Arquivo principal que inicia a aplicação Flutter
/// - Similar a: public static void main() do Java
/// - Ponto de entrada único da aplicação
///
/// ANALOGIA Backend (Spring Boot):
/// ```java
/// @SpringBootApplication
/// public class WineReviewerApplication {
///   public static void main(String[] args) {
///     SpringApplication.run(WineReviewerApplication.class, args);
///   }
/// }
/// ```
///
/// FUNÇÃO main():
/// - Primeira função executada quando app inicia
/// - Chama runApp() para iniciar o app Flutter
/// - Pode executar inicializações antes de runApp() (ex: Firebase.initializeApp())

/// main() - Entry point assíncrono com inicialização de autenticação
///
/// MUDANÇA (2025-10-29): Tornada assíncrona para verificar auth status antes de runApp()
///
/// FLUXO DE INICIALIZAÇÃO:
/// 1. WidgetsFlutterBinding.ensureInitialized() - inicializa Flutter framework
/// 2. Cria ProviderContainer - container DI do Riverpod
/// 3. Chama AuthStateNotifier.checkAuthStatus() - verifica se usuário está logado
///    - Se token existe → AuthState.authenticated(user)
///    - Se não existe → AuthState.unauthenticated()
/// 4. runApp() com UncontrolledProviderScope - inicia app com container pré-configurado
///
/// POR QUE ASYNC MAIN?
/// - ✅ Permite verificar autenticação ANTES de construir UI
/// - ✅ Evita "flickering" (mostrar tela errada por 1-2 segundos)
/// - ✅ AuthState já está definido quando SplashScreen monta
///
/// ANALOGIA Backend (Spring Boot):
/// Similar a @PostConstruct ou CommandLineRunner que executa antes do app aceitar requests
void main() async {
  // WidgetsFlutterBinding.ensureInitialized() - OBRIGATÓRIO para async main()
  //
  // EXPLICAÇÃO:
  // - Inicializa o framework Flutter antes de código assíncrono
  // - Necessário quando main() é async
  // - Garante que plugins nativos (storage, Google Sign-In) funcionem
  //
  // SEM ISSO:
  // - Crash: "ServicesBinding.defaultBinaryMessenger was accessed before binding was initialized"
  WidgetsFlutterBinding.ensureInitialized();

  // ProviderContainer - container DI do Riverpod
  //
  // EXPLICAÇÃO:
  // - Cria container de providers FORA da UI
  // - Permite acessar providers antes de runApp()
  // - Usado para inicializações assíncronas
  //
  // DIFERENÇA:
  // - ProviderScope: usado dentro de runApp() (reativo)
  // - ProviderContainer: usado fora de runApp() (manual)
  //
  // ANALOGIA Backend:
  // - Similar a: ApplicationContext do Spring Boot
  final container = ProviderContainer();

  // Verifica se usuário já está autenticado (auto-login)
  //
  // EXPLICAÇÃO:
  // - Chama AuthStateNotifier.checkAuthStatus()
  // - Verifica se JWT token existe no flutter_secure_storage
  // - Se existe → busca usuário no backend → AuthState.authenticated(user)
  // - Se não existe → AuthState.unauthenticated()
  //
  // POR QUE AQUI (não no SplashScreen)?
  // - AuthState já está pronto quando SplashScreen monta
  // - Evita race conditions (SplashScreen tentando ler estado que ainda não foi verificado)
  // - Segue padrão recomendado pelo Riverpod
  await container.read(authStateNotifierProvider.notifier).checkAuthStatus();

  // runApp() - inicializa e executa a aplicação Flutter
  //
  // EXPLICAÇÃO:
  // - Infla o widget root (MyApp) na tela
  // - Inicia o framework Flutter
  // - Gerencia lifecycle da aplicação
  //
  // IMPORTANTE:
  // - Tudo dentro de runApp() é um Widget
  // - MyApp é o widget root (raiz da árvore de widgets)
  runApp(
    // UncontrolledProviderScope - wrapper do Riverpod com container pré-configurado
    //
    // DIFERENÇA - ProviderScope vs UncontrolledProviderScope:
    // - ProviderScope: cria container automaticamente (uso normal)
    // - UncontrolledProviderScope: usa container existente (uso avançado)
    //
    // POR QUE UncontrolledProviderScope?
    // - Precisamos do container FORA de runApp() para chamar checkAuthStatus()
    // - Passamos o mesmo container para dentro da UI
    // - Garante que AuthState já está inicializado
    //
    // ESTRUTURA:
    // ```
    // UncontrolledProviderScope (container pré-configurado)
    //   └── MyApp
    //       └── MaterialApp.router
    //           └── Screen (pode acessar providers)
    // ```
    //
    // ANALOGIA Backend:
    // - Similar a: passar ApplicationContext configurado para servlet container
    UncontrolledProviderScope(
      container: container,
      child: const MyApp(),
    ),
  );
}

/// Widget root da aplicação
///
/// MUDANÇA (2025-10-29): Convertido para ConsumerWidget
///
/// EXPLICAÇÃO:
/// - Widget raiz (root) que configura toda aplicação
/// - Define: tema, router, título, locale
/// - Similar a: Application class do Android
///
/// CONSUMER WIDGET (novo):
/// - Necessário para acessar ref e passar para createAppRouter(ref)
/// - Permite que router acesse authStateNotifierProvider
/// - ref é usado para verificar autenticação em redirect callback
///
/// POR QUE CONSUMER WIDGET (não StatelessWidget)?
/// - ✅ createAppRouter(ref) precisa de ref para proteção de rotas
/// - ✅ Permite router acessar AuthState para redirect condicional
/// - ✅ Mantém router reativo (escuta mudanças de autenticação)
class MyApp extends ConsumerWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    // MaterialApp.router = app Material Design com navegação customizada
    //
    // EXPLICAÇÃO - MaterialApp.router:
    // - Configura app com Material Design (Google)
    // - .router = usa router customizado (go_router)
    // - Alternativa: MaterialApp (usa Navigator 1.0 - antigo)
    //
    // MATERIAL DESIGN:
    // - Design system do Google
    // - Widgets: Scaffold, AppBar, Card, FloatingActionButton, etc.
    // - Cores, tipografia, animações padronizadas
    //
    // ALTERNATIVAS:
    // - MaterialApp = Material Design (Android style)
    // - CupertinoApp = Cupertino Design (iOS style)
    // - WidgetsApp = Sem design system (básico)
    return MaterialApp.router(
      // ========================================================================
      // Configurações Básicas
      // ========================================================================

      // title = nome do app (aparece no task switcher, navegador)
      //
      // ONDE APARECE:
      // - Android: task switcher (quando troca de apps)
      // - iOS: App Switcher
      // - Web: título da aba do navegador
      title: 'Wine Reviewer',

      // debugShowCheckedModeBanner = mostra banner "DEBUG" no canto superior direito
      //
      // QUANDO DESABILITAR:
      // - Screenshots/demos (fica mais profissional)
      // - Apresentações
      //
      // MANTER true EM:
      // - Desenvolvimento (lembra que está em debug mode)
      // - Testes (identifica builds não otimizados)
      debugShowCheckedModeBanner: false,

      // ========================================================================
      // Tema (Cores, Tipografia, etc.)
      // ========================================================================

      // theme = tema claro (light mode)
      //
      // EXPLICAÇÃO - Theme:
      // - Define cores, tipografia, estilos de componentes
      // - Todos os widgets descendentes herdam o tema
      // - Acesso: Theme.of(context).colorScheme.primary
      //
      // NOSSA IMPLEMENTAÇÃO:
      // - Importado de app_theme.dart
      // - Cores personalizadas (tema vinho)
      // - Material Design 3
      theme: AppTheme.lightTheme,

      // darkTheme = tema escuro (dark mode)
      //
      // QUANDO USAR:
      // - Suporte a dark mode (seguir preferência do sistema)
      // - themeMode: ThemeMode.system = segue sistema operacional
      //
      // POR ENQUANTO:
      // - Comentado (apenas light mode)
      // - Futuro: implementar tema escuro
      // darkTheme: AppTheme.darkTheme,

      // themeMode = qual tema usar (light, dark, system)
      //
      // OPÇÕES:
      // - ThemeMode.light = sempre light (ignorar preferência do sistema)
      // - ThemeMode.dark = sempre dark
      // - ThemeMode.system = seguir preferência do sistema (automático)
      themeMode: ThemeMode.light,

      // ========================================================================
      // Router (Navegação)
      // ========================================================================

      // routerConfig = configuração do router (go_router)
      //
      // MUDANÇA (2025-10-29): Passa ref para createAppRouter(ref)
      //
      // EXPLICAÇÃO:
      // - Integra MaterialApp com go_router
      // - Define todas as rotas da aplicação
      // - Importado de app_router.dart
      // - ref permite router acessar AuthState para proteção de rotas
      //
      // ESTRUTURA DE ROTAS:
      // - / (splash) → verifica autenticação
      // - /login → tela de login
      // - /home → feed de reviews (protegida)
      // - /review/:id → detalhes do review (protegida)
      //
      // PROTEÇÃO DE ROTAS:
      // - Router usa ref.read(authStateNotifierProvider) para verificar autenticação
      // - Redireciona para /login se usuário não autenticado tentar acessar rota protegida
      // - Redireciona para /home se usuário autenticado tentar acessar /login
      //
      // POR QUE routerConfig (não routeInformationParser)?
      // - routerConfig = API nova (Go Router 7+)
      // - Mais simples e menos verboso
      // - Recomendado pelo pacote go_router
      routerConfig: createAppRouter(ref),

      // ========================================================================
      // Locale & Internacionalização (i18n) - Futuro
      // ========================================================================

      // locale = idioma/região do app
      //
      // QUANDO USAR:
      // - Suporte a múltiplos idiomas
      // - Formatos regionais (data, moeda)
      //
      // EXEMPLO (futuro):
      // ```dart
      // locale: const Locale('pt', 'BR'),  // Português Brasil
      // localizationsDelegates: [
      //   GlobalMaterialLocalizations.delegate,
      //   GlobalWidgetsLocalizations.delegate,
      // ],
      // supportedLocales: [
      //   const Locale('pt', 'BR'),
      //   const Locale('en', 'US'),
      // ],
      // ```

      // ========================================================================
      // Builder (customização adicional) - Opcional
      // ========================================================================

      // builder = wrapper para customizar cada screen
      //
      // QUANDO USAR:
      // - Adicionar widget global (ex: loading overlay)
      // - Customizar transições de tela
      // - Adicionar banner/watermark
      //
      // EXEMPLO (futuro):
      // ```dart
      // builder: (context, child) {
      //   return Stack(
      //     children: [
      //       child!,  // Tela atual
      //       if (isLoading) LoadingOverlay(),  // Overlay global
      //     ],
      //   );
      // },
      // ```
    );
  }
}

/// EXPLICAÇÃO DETALHADA - CONCEITOS IMPORTANTES:
///
/// 1. FUNÇÃO main() (entry point):
///
/// Estrutura básica:
/// ```dart
/// void main() {
///   runApp(MyApp());
/// }
/// ```
///
/// Com inicializações:
/// ```dart
/// void main() async {
///   WidgetsFlutterBinding.ensureInitialized();  // Inicializa Flutter
///   await Firebase.initializeApp();             // Inicializa Firebase
///   runApp(MyApp());
/// }
/// ```
///
/// 2. PROVIDERSCOPE (Riverpod DI Container):
///
/// O que faz:
/// - Container que armazena todos os providers
/// - Permite acesso a providers em qualquer widget descendente
/// - Similar a ApplicationContext (Spring Boot)
///
/// Uso:
/// ```dart
/// runApp(
///   ProviderScope(
///     child: MyApp(),
///   ),
/// );
/// ```
///
/// Sobrescrever providers (testes):
/// ```dart
/// ProviderScope(
///   overrides: [
///     dioClientProvider.overrideWithValue(mockDioClient),
///   ],
///   child: MyApp(),
/// )
/// ```
///
/// 3. MATERIALAPP vs MATERIALAPP.ROUTER:
///
/// MaterialApp (navegação tradicional):
/// ```dart
/// MaterialApp(
///   routes: {
///     '/': (context) => HomeScreen(),
///     '/login': (context) => LoginScreen(),
///   },
///   home: HomeScreen(),
/// )
/// ```
///
/// MaterialApp.router (navegação moderna com go_router):
/// ```dart
/// MaterialApp.router(
///   routerConfig: createAppRouter(),  // go_router
/// )
/// ```
///
/// Vantagens do .router:
/// - ✅ Deep linking automático
/// - ✅ URL-based navigation
/// - ✅ Type-safe routing
/// - ✅ Query parameters, path parameters
///
/// 4. THEME (tema da aplicação):
///
/// Estrutura de tema:
/// ```dart
/// ThemeData(
///   colorScheme: ColorScheme.fromSeed(seedColor: Colors.blue),
///   textTheme: TextTheme(...),
///   appBarTheme: AppBarTheme(...),
///   cardTheme: CardTheme(...),
///   // etc.
/// )
/// ```
///
/// Acesso ao tema:
/// ```dart
/// Theme.of(context).colorScheme.primary       // Cor primária
/// Theme.of(context).textTheme.headlineLarge  // Estilo de texto
/// ```
///
/// 5. THEME MODE (light/dark):
///
/// Opções:
/// ```dart
/// themeMode: ThemeMode.light,   // Sempre light
/// themeMode: ThemeMode.dark,    // Sempre dark
/// themeMode: ThemeMode.system,  // Segue sistema operacional
/// ```
///
/// Com dark theme:
/// ```dart
/// MaterialApp(
///   theme: AppTheme.lightTheme,      // Tema claro
///   darkTheme: AppTheme.darkTheme,   // Tema escuro
///   themeMode: ThemeMode.system,     // Segue sistema
/// )
/// ```
///
/// 6. ROUTER CONFIG (go_router):
///
/// Criação do router:
/// ```dart
/// GoRouter createAppRouter() {
///   return GoRouter(
///     initialLocation: '/',
///     routes: [
///       GoRoute(path: '/', builder: (context, state) => SplashScreen()),
///       GoRoute(path: '/login', builder: (context, state) => LoginScreen()),
///       GoRoute(path: '/home', builder: (context, state) => HomeScreen()),
///     ],
///   );
/// }
/// ```
///
/// Uso no MaterialApp:
/// ```dart
/// MaterialApp.router(
///   routerConfig: createAppRouter(),
/// )
/// ```
///
/// 7. DEBUG BANNER:
///
/// Com banner (default):
/// - Mostra "DEBUG" no canto superior direito
/// - Indica que app está em debug mode (não otimizado)
///
/// Sem banner:
/// ```dart
/// MaterialApp(
///   debugShowCheckedModeBanner: false,
/// )
/// ```
///
/// ANALOGIA COMPLETA:
///
/// | Flutter | Spring Boot | Descrição |
/// |---------|-------------|-----------|
/// | `main()` | `public static void main()` | Entry point |
/// | `runApp()` | `SpringApplication.run()` | Inicia aplicação |
/// | `ProviderScope` | `ApplicationContext` | DI Container |
/// | `MaterialApp.router` | `@SpringBootApplication` | Configuração root |
/// | `theme` | `application.properties` | Configurações globais |
/// | `routerConfig` | `@RequestMapping` | Configuração de rotas |
///
/// ESTRUTURA FINAL DA APLICAÇÃO:
/// ```
/// main()
///   └── ProviderScope (DI Container)
///       └── MyApp (Root Widget)
///           └── MaterialApp.router (Material Design + Router)
///               ├── theme (Tema claro)
///               └── routerConfig (go_router)
///                   └── SplashScreen (Tela inicial)
///                       ├── /login (Login)
///                       └── /home (Home)
///                           └── /review/:id (Details)
/// ```
///
/// FLUXO DE INICIALIZAÇÃO:
/// 1. Sistema operacional chama main()
/// 2. main() chama runApp()
/// 3. runApp() cria ProviderScope (inicializa providers)
/// 4. ProviderScope cria MyApp
/// 5. MyApp cria MaterialApp.router
/// 6. MaterialApp.router carrega tema e router
/// 7. Router navega para initialLocation (/)
/// 8. SplashScreen é exibida
/// 9. SplashScreen verifica autenticação
/// 10. Redireciona para /home ou /login
///
/// COMPARAÇÃO COM HTML/WEB:
/// ```html
/// <!DOCTYPE html>
/// <html>                      <!-- ProviderScope -->
///   <head>
///     <title>Wine Reviewer</title>  <!-- title -->
///     <link rel="stylesheet">        <!-- theme -->
///   </head>
///   <body>                    <!-- MaterialApp.router -->
///     <div id="app">          <!-- routerConfig -->
///       <router-view />       <!-- Screens -->
///     </div>
///   </body>
/// </html>
/// ```
