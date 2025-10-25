import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:wine_reviewer_mobile/core/providers/network_providers.dart';

/// Splash Screen - Tela de carregamento inicial
///
/// EXPLICAÇÃO (O que é):
/// - Primeira tela que aparece quando o app inicia
/// - Verifica se usuário está autenticado (tem JWT token)
/// - Redireciona para /home (se autenticado) ou /login (se não autenticado)
///
/// ANALOGIA Backend:
/// - É como um Filter/Interceptor do Spring Security
/// - Verifica autenticação e redireciona para página correta
/// - Similar a: SecurityFilterChain → check authentication → redirect
///
/// POR QUE SPLASH SCREEN?
/// - ✅ Evita mostrar tela errada por 1-2 segundos (flickering)
/// - ✅ Feedback visual ao usuário (app está carregando)
/// - ✅ Tempo para verificar token, carregar configurações
///
/// FLUXO:
/// 1. App inicia → mostra Splash (logo + loading)
/// 2. Verifica se token JWT existe no storage
/// 3. Se token existe → redireciona para /home
/// 4. Se token não existe → redireciona para /login
///
/// CONSUMER WIDGET (Riverpod):
/// - ConsumerWidget = StatelessWidget + acesso a providers
/// - Permite ler providers com ref.watch() ou ref.read()
/// - Não precisa de BuildContext para acessar providers
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

  /// Verifica se usuário está autenticado e redireciona
  Future<void> _checkAuthentication() async {
    // Simula delay de carregamento (1 segundo)
    // Em produção, você pode remover ou reduzir para 300-500ms
    await Future.delayed(const Duration(seconds: 1));

    // Verifica se widget ainda está montado (não foi destruído)
    // IMPORTANTE: sempre verificar antes de usar context após await
    if (!mounted) return;

    // Lê authInterceptorProvider para verificar se token existe
    final authInterceptor = ref.read(authInterceptorProvider);
    final hasToken = await authInterceptor.hasToken();

    // Verifica novamente se widget ainda está montado
    if (!mounted) return;

    // Redireciona baseado na autenticação
    if (hasToken) {
      // Usuário autenticado → vai para home
      // context.go() = navegação sem voltar (substitui tela atual)
      context.go('/home');
    } else {
      // Usuário não autenticado → vai para login
      context.go('/login');
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
