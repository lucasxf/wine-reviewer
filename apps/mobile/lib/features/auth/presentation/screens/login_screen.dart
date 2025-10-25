import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

/// Login Screen - Tela de autenticação
///
/// EXPLICAÇÃO (O que é):
/// - Tela onde usuário faz login com Google
/// - Por enquanto é placeholder (botão simulado)
/// - Futuramente: integrará google_sign_in + AuthService
///
/// ANALOGIA Backend:
/// - É como uma página de login HTML com formulário
/// - Chama endpoint POST /auth/google com ID token
/// - Recebe JWT token e salva no storage
///
/// STATELESS WIDGET:
/// - Widget imutável (não tem estado interno)
/// - build() é chamado toda vez que precisa redesenhar
/// - Use quando não precisa de initState/dispose ou estado mutável
///
/// QUANDO USAR StatelessWidget vs StatefulWidget:
/// - StatelessWidget: Tela estática, sem formulário, sem animações
/// - StatefulWidget: Tela com formulário, loading, animações, timers
class LoginScreen extends StatelessWidget {
  const LoginScreen({super.key});

  /// build - constrói a UI da tela
  ///
  /// PARÂMETROS:
  /// - context: BuildContext (acesso a tema, navegação, MediaQuery)
  ///
  /// BuildContext:
  /// - Representa a localização do widget na árvore de widgets
  /// - Permite acessar: Theme, Navigator, MediaQuery, InheritedWidgets
  /// - Similar a: HttpServletRequest do Java (acesso a contexto da requisição)
  @override
  Widget build(BuildContext context) {
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
                    color: Theme.of(context).colorScheme.onSurface.withOpacity(0.6),
                  ),
            ),

            const SizedBox(height: 48),

            // Botão de login com Google (placeholder)
            // ElevatedButton = botão Material Design (elevado, com sombra)
            //
            // TIPOS DE BOTÕES (Material Design):
            // - ElevatedButton: botão elevado (sombra) - ação primária
            // - TextButton: botão sem fundo - ação secundária
            // - OutlinedButton: botão com borda - ação terciária
            // - IconButton: botão apenas com ícone
            // - FloatingActionButton: botão flutuante (geralmente canto inferior direito)
            ElevatedButton.icon(
              // onPressed = callback quando botão é clicado
              //
              // CALLBACKS (funções como parâmetro):
              // - onPressed: () { ... } = função anônima (lambda)
              // - onPressed: _handleLogin = referência para método
              // - onPressed: null = botão desabilitado
              //
              // ANALOGIA Backend:
              // - Similar a: @PostMapping handler, onClick handler (JavaScript)
              onPressed: () => _handleGoogleLogin(context),

              // icon = ícone do botão
              // Icon widget representa um ícone do Material Design
              icon: const Icon(Icons.login),

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

  /// Handler de login com Google (futuro)
  ///
  /// FLUXO (quando implementado):
  /// 1. Chama google_sign_in.signIn()
  /// 2. Obtém Google ID token
  /// 3. Chama AuthService.loginWithGoogle(idToken)
  /// 4. AuthService chama POST /auth/google
  /// 5. Backend valida token e retorna JWT
  /// 6. Salva JWT no flutter_secure_storage
  /// 7. Redireciona para /home
  ///
  /// POR ENQUANTO: apenas mostra mensagem (placeholder)
  void _handleGoogleLogin(BuildContext context) {
    // ScaffoldMessenger = gerencia snackbars (mensagens temporárias)
    // Snackbar = mensagem que aparece na parte inferior da tela
    //
    // ANALOGIA Backend:
    // - Similar a: flash messages (Spring MVC)
    // - Similar a: toast messages (Android)
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text('Login com Google será implementado em breve'),
        duration: Duration(seconds: 2),
      ),
    );

    // TODO (futuro): implementar login real
    // final googleSignIn = GoogleSignIn();
    // final account = await googleSignIn.signIn();
    // final auth = await account?.authentication;
    // final idToken = auth?.idToken;
    // await authService.loginWithGoogle(idToken);
    // context.go('/home');
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
