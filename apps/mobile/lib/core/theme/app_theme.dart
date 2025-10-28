import 'package:flutter/material.dart';
import 'app_colors.dart';

/// App theme configuration - Material Design theme
///
/// EXPLICAÇÃO (Backend Context):
/// - ThemeData = Configuração global de estilos do Material Design
/// - É como um "CSS global" ou @Configuration do Spring Boot
/// - Define aparência padrão de TODOS os widgets (botões, textos, inputs)
///
/// POR QUE usar ThemeData:
/// - Evita repetir estilos em cada widget (DRY principle)
/// - Garante consistência visual em todo o app
/// - Facilita mudança de tema (light/dark mode)
///
/// ANALOGIA Backend:
/// - Como WebSecurityConfig do Spring que configura segurança para TODA a aplicação
/// - Ou como application.yml que define configs globais
class AppTheme {
  // Private constructor - classe só tem métodos estáticos
  AppTheme._();

  /// Light theme - Tema claro (fundo branco, texto escuro)
  ///
  /// COMO FUNCIONA:
  /// 1. MaterialApp recebe este theme no main.dart
  /// 2. Todos os widgets filhos herdam esses estilos automaticamente
  /// 3. Se um widget não especificar cor, usa a cor do tema
  static ThemeData get lightTheme {
    return ThemeData(
      // Brightness - Indica se é tema claro ou escuro
      brightness: Brightness.light,

      // Color Scheme - Esquema de cores Material Design 3
      /// EXPLICAÇÃO ColorScheme:
      /// - Material Design 3 usa "color scheme" ao invés de cores individuais
      /// - Define cores para diferentes "papéis" (primary, secondary, error, etc.)
      /// - Widgets Material usam essas cores automaticamente
      colorScheme: ColorScheme.light(
        primary: AppColors.primary, // Cor principal (botões, app bar, etc.)
        primaryContainer: AppColors.primaryLight, // Variante clara da primary
        secondary: AppColors.secondary, // Cor secundária (FABs, switches, etc.)
        secondaryContainer: AppColors.secondaryLight, // Variante clara da secondary
        error: AppColors.error, // Cor de erro (mensagens de erro, validação)
        surface: AppColors.surface, // Cor de fundo do app
        onPrimary: Colors.white, // Cor do texto/ícone SOBRE a cor primary
        onSecondary: Colors.white, // Cor do texto/ícone SOBRE a cor secondary
        onError: Colors.white, // Cor do texto/ícone SOBRE a cor error
        onSurface: AppColors.textPrimary, // Cor do texto/ícone SOBRE background
      ),

      // Scaffold Background Color - Cor de fundo padrão das telas
      /// EXPLICAÇÃO Scaffold:
      /// - Scaffold = estrutura básica de uma tela (app bar + body + bottom nav)
      /// - É como o <body> do HTML
      scaffoldBackgroundColor: AppColors.background,

      // App Bar Theme - Estilo padrão da barra superior
      /// EXPLICAÇÃO AppBar:
      /// - AppBar = barra no topo da tela (título, botão voltar, ações)
      /// - É como o <header> do HTML
      appBarTheme: const AppBarTheme(
        backgroundColor: AppColors.primary, // Fundo vinho tinto
        foregroundColor: Colors.white, // Texto/ícones brancos
        elevation: 2, // Sombra (2dp = discreto, 8dp = pronunciado)
        centerTitle: true, // Título centralizado (Android padrão = esquerda)
        titleTextStyle: TextStyle(
          color: Colors.white,
          fontSize: 20,
          fontWeight: FontWeight.w600, // Semi-bold
        ),
      ),

      // Elevated Button Theme - Estilo padrão dos botões elevados
      /// EXPLICAÇÃO ElevatedButton:
      /// - Botão com sombra e fundo colorido
      /// - Usado para ações primárias (Login, Salvar, Enviar)
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: AppColors.primary, // Fundo vinho tinto
          foregroundColor: Colors.white, // Texto branco
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(8), // Cantos arredondados (8px)
          ),
          elevation: 2, // Sombra
          textStyle: const TextStyle(
            fontSize: 16,
            fontWeight: FontWeight.w600,
          ),
        ),
      ),

      // Text Button Theme - Estilo padrão dos botões de texto
      /// EXPLICAÇÃO TextButton:
      /// - Botão sem fundo, apenas texto colorido
      /// - Usado para ações secundárias (Cancelar, Pular, Esqueci Senha)
      textButtonTheme: TextButtonThemeData(
        style: TextButton.styleFrom(
          foregroundColor: AppColors.primary, // Texto vinho tinto
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
          textStyle: const TextStyle(
            fontSize: 14,
            fontWeight: FontWeight.w500,
          ),
        ),
      ),

      // Input Decoration Theme - Estilo padrão dos campos de texto
      /// EXPLICAÇÃO TextFormField:
      /// - Campo de entrada de texto (email, senha, notas do review)
      /// - InputDecoration = borda, label, hint, ícones
      inputDecorationTheme: InputDecorationTheme(
        filled: true, // Fundo preenchido
        fillColor: AppColors.surface, // Fundo branco
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),

        // Border - Borda padrão (quando não está focado)
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: const BorderSide(color: AppColors.divider, width: 1),
        ),

        // Enabled Border - Borda quando habilitado mas não focado
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: const BorderSide(color: AppColors.divider, width: 1),
        ),

        // Focused Border - Borda quando focado (usuário clicou no campo)
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: const BorderSide(color: AppColors.primary, width: 2),
        ),

        // Error Border - Borda quando há erro de validação
        errorBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: const BorderSide(color: AppColors.error, width: 1),
        ),

        // Focused Error Border - Borda quando focado E com erro
        focusedErrorBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: const BorderSide(color: AppColors.error, width: 2),
        ),

        // Label Style - Estilo do label flutuante
        labelStyle: const TextStyle(
          color: AppColors.textSecondary,
          fontSize: 16,
        ),

        // Hint Style - Estilo do hint (placeholder)
        hintStyle: const TextStyle(
          color: AppColors.textSecondary,
          fontSize: 14,
        ),

        // Error Style - Estilo da mensagem de erro
        errorStyle: const TextStyle(
          color: AppColors.error,
          fontSize: 12,
        ),
      ),

      // Card Theme - Estilo padrão dos cards
      /// EXPLICAÇÃO Card:
      /// - Card = container com elevação e cantos arredondados
      /// - Usado para agrupar informações relacionadas (review card, wine card)
      cardTheme: const CardThemeData(
        color: AppColors.surface, // Fundo branco
        elevation: 2, // Sombra
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.all(Radius.circular(12)), // Cantos arredondados
        ),
        margin: EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      ),

      // Divider Theme - Estilo padrão dos divisores
      /// EXPLICAÇÃO Divider:
      /// - Linha horizontal fina para separar seções
      /// - Como um <hr> do HTML
      dividerTheme: const DividerThemeData(
        color: AppColors.divider,
        thickness: 1,
        space: 16, // Espaço vertical ao redor do divisor
      ),

      // Text Theme - Estilos de texto (títulos, subtítulos, corpo)
      /// EXPLICAÇÃO TextTheme:
      /// - Define hierarquia de textos (headline, title, body, label)
      /// - Material Design 3 usa nomenclatura específica
      textTheme: const TextTheme(
        // Display - Textos muito grandes (tela inicial, onboarding)
        displayLarge: TextStyle(
          fontSize: 57,
          fontWeight: FontWeight.w400,
          color: AppColors.textPrimary,
        ),

        // Headline - Títulos principais de telas
        headlineLarge: TextStyle(
          fontSize: 32,
          fontWeight: FontWeight.w600,
          color: AppColors.textPrimary,
        ),
        headlineMedium: TextStyle(
          fontSize: 28,
          fontWeight: FontWeight.w600,
          color: AppColors.textPrimary,
        ),

        // Title - Títulos de seções, cards, dialogs
        titleLarge: TextStyle(
          fontSize: 22,
          fontWeight: FontWeight.w500,
          color: AppColors.textPrimary,
        ),
        titleMedium: TextStyle(
          fontSize: 16,
          fontWeight: FontWeight.w500,
          color: AppColors.textPrimary,
        ),

        // Body - Texto de corpo (parágrafos, descrições)
        bodyLarge: TextStyle(
          fontSize: 16,
          fontWeight: FontWeight.w400,
          color: AppColors.textPrimary,
        ),
        bodyMedium: TextStyle(
          fontSize: 14,
          fontWeight: FontWeight.w400,
          color: AppColors.textPrimary,
        ),
        bodySmall: TextStyle(
          fontSize: 12,
          fontWeight: FontWeight.w400,
          color: AppColors.textSecondary,
        ),

        // Label - Textos em botões, chips, badges
        labelLarge: TextStyle(
          fontSize: 14,
          fontWeight: FontWeight.w500,
          color: AppColors.textPrimary,
        ),
      ),
    );
  }

  // TODO: Future - Dark theme
  /// Implementar tema escuro quando necessário
  /// static ThemeData get darkTheme { ... }
}

/// COMO USAR (no main.dart):
///
/// ```dart
/// import 'package:wine_reviewer_mobile/core/theme/app_theme.dart';
///
/// class MyApp extends StatelessWidget {
///   @override
///   Widget build(BuildContext context) {
///     return MaterialApp(
///       title: 'Wine Reviewer',
///       theme: AppTheme.lightTheme,  // ← Aplica tema aqui
///       home: LoginScreen(),
///     );
///   }
/// }
/// ```
///
/// BENEFÍCIOS:
/// - Todos os ElevatedButton do app ficam vinho tinto automaticamente
/// - Todos os TextFormField têm a mesma borda e estilo
/// - Consistência visual garantida em todo o app
/// - Muda estilo em 1 lugar, afeta TUDO
