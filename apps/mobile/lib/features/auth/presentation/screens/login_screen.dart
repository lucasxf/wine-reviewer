import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:wine_reviewer_mobile/features/auth/providers/auth_providers.dart';
import 'package:wine_reviewer_mobile/features/auth/providers/auth_state.dart';

/// Login Screen - Tela de autenticação com Google Sign-In
///
/// MUDANÇA (2025-10-29): Integração completa com AuthStateNotifier
///
/// EXPLICAÇÃO (O que é):
/// - Tela onde usuário faz login com Google OAuth
/// - Integrada com AuthService e AuthStateNotifier
/// - Gerencia loading states e error handling
///
/// ANALOGIA Backend:
/// - É como uma página de login HTML com formulário
/// - Chama endpoint POST /auth/google com ID token
/// - Recebe JWT token e salva no storage
///
/// CONSUMER WIDGET (Riverpod):
/// - ConsumerWidget = StatelessWidget + acesso a providers
/// - Permite ler/escrever providers com ref.watch() ou ref.read()
/// - Use quando precisa acessar state management (Riverpod)
///
/// QUANDO USAR ConsumerWidget vs StatelessWidget:
/// - ConsumerWidget: Precisa acessar providers (state, services, etc.)
/// - StatelessWidget: Tela estática, sem acesso a providers
///
/// POR QUE ConsumerWidget (não StatelessWidget)?
/// - ✅ Precisa chamar AuthStateNotifier.signInWithGoogle()
/// - ✅ Precisa ler AuthState para mostrar loading/errors
/// - ✅ Acesso a ref.read() e ref.watch()
class LoginScreen extends ConsumerWidget {
  const LoginScreen({super.key});

  /// build - constrói a UI da tela
  ///
  /// PARÂMETROS:
  /// - context: BuildContext (acesso a tema, navegação, MediaQuery)
  /// - ref: WidgetRef (acesso a providers do Riverpod)
  ///
  /// BuildContext:
  /// - Representa a localização do widget na árvore de widgets
  /// - Permite acessar: Theme, Navigator, MediaQuery, InheritedWidgets
  /// - Similar a: HttpServletRequest do Java (acesso a contexto da requisição)
  ///
  /// WidgetRef (novo):
  /// - Permite acessar providers com ref.read() e ref.watch()
  /// - ref.watch() - escuta mudanças no provider (rebuilds widget)
  /// - ref.read() - lê valor uma vez (usado em callbacks)
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    // Watch authState to show loading indicator when loading
    //
    // ref.watch() - Observa mudanças no authStateNotifierProvider
    // Quando AuthState muda (initial → loading → authenticated/unauthenticated),
    // o widget é reconstruído automaticamente
    //
    // ANALOGIA Backend:
    // - Similar a: @Autowired + observer pattern
    // - Como se o widget se "inscrevesse" para receber updates do AuthState
    final authState = ref.watch(authStateNotifierProvider);
    return Scaffold(
      // AppBar = barra superior da tela
      // Material Design: geralmente contém título + ações
      appBar: AppBar(
        // title = texto do título (centralizado ou alinhado à esquerda)
        title: const Text('Login'),
        // centerTitle = centraliza o título
        centerTitle: true,
      ),

      // body = conteúdo principal da tela
      // Padding = adiciona espaçamento interno (padding)
      body: Padding(
        // EdgeInsets.all(24) = 24px de padding em todos os lados
        // Similar a: padding: 24px; (CSS)
        padding: const EdgeInsets.all(24.0),

        // Column = organiza widgets verticalmente
        child: Column(
          // mainAxisAlignment = alinhamento vertical (Column)
          // MainAxisAlignment.center = centraliza verticalmente
          mainAxisAlignment: MainAxisAlignment.center,

          // crossAxisAlignment = alinhamento horizontal (Column)
          // CrossAxisAlignment.stretch = estica widgets para ocupar largura total
          crossAxisAlignment: CrossAxisAlignment.stretch,

          children: [
            // Ícone de vinho (decoração)
            Icon(
              Icons.wine_bar,
              size: 100,
              color: Theme.of(context).colorScheme.primary,
            ),

            const SizedBox(height: 32),

            // Título da tela
            Text(
              'Bem-vindo!',
              textAlign: TextAlign.center,
              style: Theme.of(context).textTheme.headlineLarge,
            ),

            const SizedBox(height: 8),

            // Subtítulo
            Text(
              'Faça login para avaliar e descobrir novos vinhos',
              textAlign: TextAlign.center,
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                    color: Theme.of(context).colorScheme.onSurface.withAlpha(153), // 0.6 opacity = 153/255
                  ),
            ),

            const SizedBox(height: 48),

            // Botão de login com Google (integrado com AuthStateNotifier)
            // ElevatedButton = botão Material Design (elevado, com sombra)
            //
            // MUDANÇA (2025-10-29): Integrado com AuthStateNotifier
            // - Desabilita botão durante loading (evita múltiplos cliques)
            // - Mostra spinner durante login
            // - Chama AuthStateNotifier.signInWithGoogle() ao clicar
            //
            // TIPOS DE BOTÕES (Material Design):
            // - ElevatedButton: botão elevado (sombra) - ação primária
            // - TextButton: botão sem fundo - ação secundária
            // - OutlinedButton: botão com borda - ação terciária
            // - IconButton: botão apenas com ícone
            // - FloatingActionButton: botão flutuante (geralmente canto inferior direito)
            ElevatedButton.icon(
              // onPressed - desabilitado se loading, senão chama _handleGoogleLogin
              //
              // TYPE CHECKING (is operator):
              // - authState is AuthStateLoading → true se estado é loading
              // - onPressed: null = botão desabilitado (cinza, sem clique)
              // - onPressed: () => _handleGoogleLogin(...) = botão habilitado
              //
              // POR QUE DESABILITAR DURANTE LOADING?
              // - Evita múltiplos cliques enquanto login está em andamento
              // - UX melhor: usuário vê que algo está acontecendo
              //
              // ANALOGIA Backend:
              // - Similar a: if (status == Status.LOADING) button.setEnabled(false);
              onPressed: authState is AuthStateLoading
                  ? null  // Desabilita botão se loading
                  : () => _handleGoogleLogin(context, ref),  // Habilita botão

              // icon - mostra loading spinner se loading, senão ícone de login
              //
              // OPERADOR TERNÁRIO (condição ? true : false):
              // - Se authState é loading → CircularProgressIndicator (spinner)
              // - Se não → Icon(Icons.login) (ícone normal)
              icon: authState is AuthStateLoading
                  ? const SizedBox(
                      width: 20,
                      height: 20,
                      child: CircularProgressIndicator(
                        strokeWidth: 2,
                        valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
                      ),
                    )
                  : const Icon(Icons.login),

              // label = texto do botão
              label: const Text('Entrar com Google'),

              // style = personalização do botão
              style: ElevatedButton.styleFrom(
                // padding = espaçamento interno do botão
                padding: const EdgeInsets.symmetric(vertical: 16),

                // textStyle = estilo do texto do botão
                textStyle: const TextStyle(fontSize: 16),
              ),
            ),

            const SizedBox(height: 16),

            // Botão de login simples (MVP testing - sem Google)
            // OutlinedButton = botão com borda (ação secundária)
            OutlinedButton.icon(
              onPressed: () => _handleSimpleLogin(context),
              icon: const Icon(Icons.person),
              label: const Text('Login Simples (MVP)'),
              style: OutlinedButton.styleFrom(
                padding: const EdgeInsets.symmetric(vertical: 16),
                textStyle: const TextStyle(fontSize: 16),
              ),
            ),
          ],
        ),
      ),
    );
  }

  /// Handler de login com Google (implementação real)
  ///
  /// MUDANÇA (2025-10-29): Implementação completa com AuthStateNotifier
  ///
  /// FLUXO:
  /// 1. Chama AuthStateNotifier.signInWithGoogle()
  /// 2. AuthStateNotifier chama AuthService.signInWithGoogle()
  /// 3. AuthService:
  ///    a. Chama google_sign_in.signIn() (abre dialog do Google)
  ///    b. Obtém Google ID token
  ///    c. Chama POST /api/auth/google (backend)
  ///    d. Backend valida token com Google e retorna JWT
  ///    e. Salva JWT no flutter_secure_storage
  ///    f. Retorna User
  /// 4. AuthStateNotifier atualiza estado para authenticated(user)
  /// 5. Redireciona para /home
  ///
  /// ERROR HANDLING:
  /// - Se usuário cancela login → mostra SnackBar "Login cancelado"
  /// - Se ocorre erro (rede, backend, etc.) → mostra SnackBar com mensagem de erro
  /// - AuthStateNotifier volta estado para unauthenticated() em caso de erro
  ///
  /// ANALOGIA Backend:
  /// - Similar a: controller method que chama service e trata exceções
  /// - Similar a: @ExceptionHandler (trata erros e mostra mensagens)
  Future<void> _handleGoogleLogin(BuildContext context, WidgetRef ref) async {
    try {
      // Chama AuthStateNotifier.signInWithGoogle()
      //
      // EXPLICAÇÃO:
      // - ref.read() = lê provider UMA VEZ (não escuta mudanças)
      // - .notifier = acessa o AuthStateNotifier (classe com métodos)
      // - .signInWithGoogle() = método async que faz o login
      //
      // ANALOGIA Backend:
      // - Similar a: authService.login(email, password) em um controller
      await ref.read(authStateNotifierProvider.notifier).signInWithGoogle();

      // Se chegou aqui, login foi bem-sucedido
      // Verifica se widget ainda está montado (segurança)
      if (context.mounted) {
        // Redireciona para home
        context.go('/home');
      }
    } catch (e) {
      // Login falhou (usuário cancelou ou erro ocorreu)
      //
      // EXCEÇÕES POSSÍVEIS:
      // - Usuário cancelou login (fechou dialog do Google)
      // - Erro de rede (sem internet)
      // - Erro no backend (servidor fora do ar, token inválido)
      // - Erro no storage (não conseguiu salvar token)
      //
      // TRATAMENTO:
      // - Mostra SnackBar com mensagem de erro
      // - Usuário pode tentar novamente clicando no botão
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Erro ao fazer login: ${e.toString()}'),
            duration: const Duration(seconds: 4),
            backgroundColor: Theme.of(context).colorScheme.error,
            action: SnackBarAction(
              label: 'OK',
              textColor: Colors.white,
              onPressed: () {
                ScaffoldMessenger.of(context).hideCurrentSnackBar();
              },
            ),
          ),
        );
      }
    }
  }

  /// Handler de login simples (MVP testing)
  ///
  /// FLUXO:
  /// 1. Chama POST /auth/login com email fixo
  /// 2. Backend retorna JWT token
  /// 3. Salva token no storage
  /// 4. Redireciona para /home
  ///
  /// POR ENQUANTO: apenas redireciona (simula login bem-sucedido)
  void _handleSimpleLogin(BuildContext context) {
    // Simula login bem-sucedido
    // Em produção: chamar AuthService.login(email)
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text('Login simulado! Redirecionando...'),
        duration: Duration(seconds: 1),
      ),
    );

    // Aguarda 1 segundo e redireciona para home
    Future.delayed(const Duration(seconds: 1), () {
      // Verifica se context ainda é válido
      // context.mounted = true se widget ainda está na tela (Flutter 3.7+)
      if (context.mounted) {
        context.go('/home');
      }
    });
  }
}

/// EXPLICAÇÃO DETALHADA - CONCEITOS IMPORTANTES:
///
/// 1. STATELESS WIDGET (imutável):
/// - Não tem estado interno mutável
/// - build() é chamado quando precisa redesenhar
/// - Parâmetros devem ser final (imutáveis)
/// - Use para telas simples, estáticas
///
/// Estrutura:
/// ```dart
/// class MyWidget extends StatelessWidget {
///   final String title; // Parâmetros imutáveis
///
///   const MyWidget({required this.title});
///
///   @override
///   Widget build(BuildContext context) {
///     return Text(title);
///   }
/// }
/// ```
///
/// 2. BUILD CONTEXT:
/// - Representa localização do widget na árvore
/// - Permite acessar:
///   - Theme.of(context) = tema da aplicação
///   - Navigator.of(context) = navegação
///   - MediaQuery.of(context) = tamanho da tela, orientação
///   - ScaffoldMessenger.of(context) = mostrar snackbars
///
/// Analogia:
/// - Similar a HttpServletRequest (acesso ao contexto da requisição)
/// - Similar a ApplicationContext do Spring (acesso a beans)
///
/// 3. APPBAR (barra superior):
/// ```dart
/// AppBar(
///   title: Text('Título'),         // Título da tela
///   centerTitle: true,              // Centraliza título
///   actions: [IconButton(...)],     // Botões no canto direito
///   leading: IconButton(...),       // Botão no canto esquerdo (geralmente voltar)
/// )
/// ```
///
/// 4. PADDING (espaçamento interno):
/// ```dart
/// Padding(
///   padding: EdgeInsets.all(16),              // 16px em todos os lados
///   padding: EdgeInsets.symmetric(            // Eixos específicos
///     horizontal: 24,  // 24px esquerda e direita
///     vertical: 16,    // 16px topo e fundo
///   ),
///   padding: EdgeInsets.only(                 // Lados específicos
///     left: 8,
///     top: 16,
///     right: 8,
///     bottom: 24,
///   ),
///   child: Text('Conteúdo'),
/// )
/// ```
///
/// 5. BOTÕES (tipos e quando usar):
///
/// ElevatedButton (ação primária):
/// ```dart
/// ElevatedButton(
///   onPressed: () { print('Clicado'); },
///   child: Text('Confirmar'),
/// )
/// ```
///
/// TextButton (ação secundária):
/// ```dart
/// TextButton(
///   onPressed: () { print('Clicado'); },
///   child: Text('Cancelar'),
/// )
/// ```
///
/// OutlinedButton (ação terciária):
/// ```dart
/// OutlinedButton(
///   onPressed: () { print('Clicado'); },
///   child: Text('Voltar'),
/// )
/// ```
///
/// 6. CALLBACKS (funções como parâmetro):
/// - onPressed: () { ... } = função anônima (lambda)
/// - onPressed: _handleClick = referência para método
/// - onPressed: null = botão desabilitado
///
/// Exemplo com parâmetro:
/// ```dart
/// onPressed: () => _handleLogin(context),  // Passa context como argumento
/// ```
///
/// 7. SNACKBAR (mensagem temporária):
/// ```dart
/// ScaffoldMessenger.of(context).showSnackBar(
///   SnackBar(
///     content: Text('Mensagem'),
///     duration: Duration(seconds: 2),
///     action: SnackBarAction(
///       label: 'Desfazer',
///       onPressed: () { /* ação */ },
///     ),
///   ),
/// );
/// ```
///
/// 8. FUTURE.DELAYED (aguardar tempo):
/// ```dart
/// Future.delayed(Duration(seconds: 1), () {
///   // Código executado após 1 segundo
///   print('1 segundo se passou');
/// });
/// ```
///
/// 9. CONTEXT.MOUNTED (verificação de segurança):
/// - context.mounted = verifica se widget ainda está na tela
/// - SEMPRE verificar após Future.delayed, await
/// - Evita erros de "context used after dispose"
///
/// ANALOGIA COMPLETA:
/// - LoginScreen = login.html (página de login)
/// - ElevatedButton = <button> (HTML)
/// - onPressed = onClick (JavaScript)
/// - SnackBar = flash message (Spring MVC)
/// - Future.delayed = setTimeout (JavaScript)
/// - Padding = padding/margin (CSS)
/// - Column/Row = flex-direction (CSS Flexbox)
