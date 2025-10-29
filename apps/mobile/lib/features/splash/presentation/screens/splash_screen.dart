import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:wine_reviewer_mobile/features/auth/providers/auth_providers.dart';
import 'package:wine_reviewer_mobile/features/auth/providers/auth_state.dart';

/// Splash Screen - Tela de carregamento inicial com integração AuthState
///
/// MUDANÇA (2025-10-29): Integrada com AuthStateNotifier
///
/// EXPLICAÇÃO (O que é):
/// - Primeira tela que aparece quando o app inicia
/// - Observa AuthState para determinar para onde redirecionar
/// - Redireciona para /home (se authenticated) ou /login (se unauthenticated)
///
/// FLUXO (novo):
/// 1. main.dart chama AuthStateNotifier.checkAuthStatus() ANTES de runApp()
/// 2. SplashScreen monta com AuthState já definido (authenticated/unauthenticated)
/// 3. initState() verifica o AuthState atual
/// 4. Redireciona imediatamente para tela correta (sem delay)
///
/// ANALOGIA Backend:
/// - É como um Filter/Interceptor do Spring Security
/// - Verifica autenticação e redireciona para página correta
/// - Similar a: SecurityFilterChain → check authentication → redirect
///
/// POR QUE SPLASH SCREEN?
/// - ✅ Evita mostrar tela errada por 1-2 segundos (flickering)
/// - ✅ Feedback visual ao usuário (app está carregando)
/// - ✅ Centraliza lógica de redirecionamento baseada em autenticação
///
/// CONSUMER STATEFUL WIDGET (Riverpod):
/// - ConsumerStatefulWidget = StatefulWidget + acesso a providers
/// - Permite usar ref.watch/read em initState, build, etc.
/// - Necessário porque precisamos de initState para redirecionar
class SplashScreen extends ConsumerStatefulWidget {
  const SplashScreen({super.key});

  @override
  ConsumerState<SplashScreen> createState() => _SplashScreenState();
}

/// State da Splash Screen
///
/// EXPLICAÇÃO - ConsumerStatefulWidget:
/// - Usado quando precisa executar código após a tela ser montada (initState)
/// - ConsumerState = State + acesso a providers (ref)
///
/// LIFECYCLE (ciclo de vida):
/// 1. initState() - chamado UMA VEZ quando widget é criado
/// 2. build() - chamado toda vez que widget precisa ser desenhado
/// 3. dispose() - chamado quando widget é removido (cleanup)
class _SplashScreenState extends ConsumerState<SplashScreen> {
  /// Maximum number of retry attempts to check authentication status
  /// Prevents infinite recursion if auth state remains in Initial/Loading
  static const int _maxRetries = 10;

  /// Current retry counter
  int _retryCount = 0;
  /// initState - executado UMA VEZ quando tela é criada
  ///
  /// QUANDO USAR:
  /// - Inicializar variáveis
  /// - Fazer requisições HTTP
  /// - Verificar autenticação
  /// - Configurar listeners
  ///
  /// IMPORTANTE:
  /// - Não pode ser async diretamente
  /// - Para operações assíncronas, chame método async separado
  @override
  void initState() {
    super.initState();

    // Chama método async para verificar autenticação
    // Future.delayed simula delay de 1 segundo (carregamento)
    _checkAuthentication();
  }

  /// Verifica AuthState e redireciona para tela apropriada
  ///
  /// MUDANÇA (2025-10-29): Usa AuthState ao invés de authInterceptor
  /// MUDANÇA (2025-10-29): Adiciona retry counter para prevenir recursão infinita
  ///
  /// FLUXO:
  /// 1. Lê AuthState atual do AuthStateNotifierProvider
  /// 2. Usa pattern matching (switch expression) para determinar redirecionamento
  /// 3. Redireciona para /home (authenticated) ou /login (unauthenticated)
  /// 4. Se initial/loading → aguarda 500ms e tenta novamente (máximo 10 vezes)
  /// 5. Se exceder máximo de tentativas → redireciona para /login como fallback
  ///
  /// POR QUE RETRY COUNTER?
  /// - Previne recursão infinita se auth state nunca for resolvido
  /// - Evita stack overflow em cenários edge case (ex: falha de rede persistente)
  /// - Fallback para login garante que usuário não fica preso na splash screen
  ///
  /// POR QUE NÃO TEM DELAY DE 1 SEGUNDO?
  /// - AuthState já foi verificado em main.dart antes de runApp()
  /// - Podemos redirecionar imediatamente (já sabemos se usuário está logado)
  /// - Delay seria apenas cosmético (pode adicionar 300-500ms se quiser)
  Future<void> _checkAuthentication() async {
    // Opcional: delay cosmético (deixa usuário ver logo por 300ms)
    // Remova esta linha se quiser redirecionamento instantâneo
    await Future.delayed(const Duration(milliseconds: 300));

    // Verifica se widget ainda está montado
    // IMPORTANTE: sempre verificar antes de usar context após await
    if (!mounted) return;

    // Lê AuthState atual (já foi inicializado em main.dart)
    //
    // ref.read() = lê provider UMA VEZ (não escuta mudanças)
    // Usamos read() porque queremos apenas o valor atual, não escutar mudanças
    //
    // ANALOGIA Backend:
    // - Similar a: SecurityContext.getAuthentication()
    final authState = ref.read(authStateNotifierProvider);

    // Verifica novamente se widget ainda está montado
    if (!mounted) return;

    // Pattern matching com switch expression (Dart 3.0)
    //
    // Redireciona baseado no tipo de AuthState:
    // - AuthStateAuthenticated → /home (usuário logado)
    // - AuthStateUnauthenticated → /login (usuário não logado)
    // - AuthStateInitial/Loading → aguarda 500ms e tenta novamente (máx 10x)
    //
    // POR QUE initial/loading são raros?
    // - main.dart já chamou checkAuthStatus() antes de runApp()
    // - Quando SplashScreen monta, estado já deve ser authenticated/unauthenticated
    //
    // PROTEÇÃO CONTRA RECURSÃO INFINITA:
    // - Contador de tentativas (_retryCount) limita a 10 tentativas
    // - Se exceder, redireciona para /login como fallback
    // - Previne stack overflow em edge cases (ex: auth service offline)
    //
    // ANALOGIA Backend:
    // - Similar a: switch (securityContext.getAuthentication()) { ... }
    switch (authState) {
      case AuthStateAuthenticated():
        // Usuário autenticado → vai para home
        // context.go() = navegação sem voltar (substitui tela atual)
        context.go('/home');
        return;
      case AuthStateUnauthenticated():
        // Usuário não autenticado → vai para login
        context.go('/login');
        return;
      case AuthStateInitial():
      case AuthStateLoading():
        // Estado ainda não definido (raro) → aguarda e tenta novamente
        // Isso pode acontecer se checkAuthStatus() estiver lento
        _retryCount++;
        
        // Previne recursão infinita com contador de tentativas
        if (_retryCount >= _maxRetries) {
          // Máximo de tentativas excedido → vai para login como fallback
          // Isso previne stack overflow se auth state nunca for resolvido
          context.go('/login');
          return;
        }
        
        await Future.delayed(const Duration(milliseconds: 500));
        if (mounted) {
          _checkAuthentication(); // Tenta novamente
        }
        return;
    }
  }

  /// build - constrói a UI da tela
  ///
  /// EXPLICAÇÃO:
  /// - Chamado toda vez que widget precisa ser redesenhado
  /// - Retorna uma árvore de widgets (UI)
  /// - Similar a: render() do React, view.html do Thymeleaf
  @override
  Widget build(BuildContext context) {
    // Scaffold = estrutura base Material Design
    // Fornece: AppBar, Body, BottomNavigationBar, etc.
    return Scaffold(
      // backgroundColor = cor de fundo da tela
      backgroundColor: Theme.of(context).colorScheme.primary,

      // body = conteúdo principal da tela
      // Center = centraliza children horizontal e verticalmente
      body: Center(
        // Column = organiza widgets verticalmente (um embaixo do outro)
        // Similar a: <div style="flex-direction: column">
        child: Column(
          // mainAxisAlignment = alinhamento vertical (Column)
          // MainAxisAlignment.center = centraliza verticalmente
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            // Icon = ícone do Material Design
            // Icons.wine_bar = ícone de taça de vinho 🍷
            Icon(
              Icons.wine_bar,
              size: 80,
              color: Theme.of(context).colorScheme.onPrimary,
            ),

            // SizedBox = espaçamento (padding/margin)
            // height = espaço vertical entre widgets
            const SizedBox(height: 24),

            // Text = widget de texto
            Text(
              'Wine Reviewer',
              style: Theme.of(context).textTheme.headlineLarge?.copyWith(
                    color: Theme.of(context).colorScheme.onPrimary,
                    fontWeight: FontWeight.bold,
                  ),
            ),

            const SizedBox(height: 48),

            // CircularProgressIndicator = loading spinner (bolinha girando)
            // Indica que algo está carregando
            CircularProgressIndicator(
              color: Theme.of(context).colorScheme.onPrimary,
            ),
          ],
        ),
      ),
    );
  }
}

/// EXPLICAÇÃO DETALHADA - CONCEITOS IMPORTANTES:
///
/// 1. CONSUMER STATE WIDGET vs STATELESS:
///
/// StatelessWidget (imutável):
/// ```dart
/// class MyScreen extends StatelessWidget {
///   @override
///   Widget build(BuildContext context) {
///     return Scaffold(...);
///   }
/// }
/// ```
///
/// ConsumerStatefulWidget (mutável + Riverpod):
/// ```dart
/// class MyScreen extends ConsumerStatefulWidget {
///   @override
///   ConsumerState<MyScreen> createState() => _MyScreenState();
/// }
///
/// class _MyScreenState extends ConsumerState<MyScreen> {
///   @override
///   void initState() { /* código de inicialização */ }
///
///   @override
///   Widget build(BuildContext context) { /* UI */ }
/// }
/// ```
///
/// 2. SCAFFOLD (estrutura Material Design):
/// ```dart
/// Scaffold(
///   appBar: AppBar(title: Text('Título')),        // Barra superior
///   body: Center(child: Text('Conteúdo')),        // Conteúdo principal
///   bottomNavigationBar: BottomNavigationBar(),   // Barra inferior
///   floatingActionButton: FloatingActionButton(), // Botão flutuante (+)
/// )
/// ```
///
/// 3. LAYOUT WIDGETS (organização):
///
/// Column (vertical):
/// ```dart
/// Column(
///   children: [
///     Text('Item 1'),
///     Text('Item 2'),  // Um embaixo do outro
///   ],
/// )
/// ```
///
/// Row (horizontal):
/// ```dart
/// Row(
///   children: [
///     Text('Item 1'),
///     Text('Item 2'),  // Um ao lado do outro
///   ],
/// )
/// ```
///
/// 4. THEME (tema/cores):
/// - Theme.of(context) = acessa tema atual da aplicação
/// - colorScheme.primary = cor primária (definida em app_theme.dart)
/// - colorScheme.onPrimary = cor do texto sobre a cor primária
/// - textTheme.headlineLarge = estilo de texto grande (título)
///
/// 5. CONTEXT.GO() (navegação):
/// - context.go('/home') = vai para /home SEM poder voltar (substitui)
/// - context.push('/details') = vai para /details PODENDO voltar (empilha)
/// - Similar a: redirect:/home (Spring MVC)
///
/// 6. MOUNTED (verificação de segurança):
/// - mounted = true se widget ainda está na tela
/// - mounted = false se widget foi removido (usuário saiu da tela)
/// - SEMPRE verificar mounted após await (evita erros de context inválido)
///
/// ANALOGIA COMPLETA:
/// - SplashScreen = index.html (primeira página)
/// - initState() = @PostConstruct (inicialização)
/// - build() = render() do React, view.html do Thymeleaf
/// - Scaffold = layout base (header + body + footer)
/// - Column/Row = flex-direction: column/row (CSS)
/// - Theme.of(context) = variáveis CSS, @ConfigurationProperties
/// - context.go() = redirect:/ (Spring MVC)
