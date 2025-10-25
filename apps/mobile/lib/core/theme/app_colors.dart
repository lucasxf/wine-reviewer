import 'package:flutter/material.dart';

/// App color palette - Wine Reviewer theme
///
/// EXPLICAÇÃO (Backend Context):
/// - Em Flutter, cores são definidas como constantes globais (similar a CSS variables ou @Value do Spring)
/// - Color = ARGB (Alpha, Red, Green, Blue) em hexadecimal 0xAARRGGBB
/// - Alpha: 0xFF = opaco (255), 0x00 = transparente (0)
///
/// POR QUE usar classe de cores separada:
/// - Centraliza todas as cores do app em um único lugar
/// - Facilita mudança de tema (dark mode, rebranding)
/// - Evita "magic numbers" espalhados pelo código
///
/// ANALOGIA Backend:
/// - Como ter um `application.properties` para cores ao invés de hard-coded
class AppColors {
  // Private constructor - previne instanciação (classe só tem constantes estáticas)
  AppColors._();

  // Primary colors - Cores principais (tema vinho/uva)
  // Paleta personalizada de vinhos (c:/repo/paleta-de-cores.dart)
  static const Color primary = Color(0xFF810020); // Vinho profundo (vinhoProfundo)
  static const Color primaryLight = Color(0xFF9F1D35); // Vinho vivo (vinhoVivo)
  static const Color primaryDark = Color(0xFF4C1C24); // Bordeaux escuro (bordeauxEscuro)

  // Secondary colors - Cores secundárias (tons de vinho)
  static const Color secondary = Color(0xFFB11225); // Tinto médio (tintoMedio)
  static const Color secondaryLight = Color(0xFF9F1D35); // Vinho vivo (vinhoVivo)
  static const Color secondaryDark = Color(0xFF4D4147); // Vinho acinzentado (vinhoAcinzentado)

  // Neutral colors - Cores neutras (cinzas, preto, branco)
  static const Color background = Color(0xFFF5F5F5); // Fundo claro (fundoClaro)
  static const Color surface = Color(0xFFFFFFFF); // Superfície (branco)
  static const Color textPrimary = Color(0xFF333333); // Texto principal (textoEscuro)
  static const Color textSecondary = Color(0xFF757575); // Texto secundário (cinza)
  static const Color divider = Color(0xFFBDBDBD); // Divisores/bordas (cinza médio)

  // Semantic colors - Cores semânticas (sucesso, erro, aviso, info)
  static const Color success = Color(0xFF4CAF50); // Verde - operação bem-sucedida
  static const Color error = Color(0xFFF44336); // Vermelho - erro
  static const Color warning = Color(0xFFFFC107); // Amarelo - aviso
  static const Color info = Color(0xFF2196F3); // Azul - informação

  // Rating colors - Cores para as taças de avaliação (1-5 taças)
  static const Color ratingEmpty = Color(0xFFE0E0E0); // Taça vazia (cinza claro)
  static const Color ratingFilled = Color(0xFF810020); // Taça cheia (vinho profundo)

  // Shimmer effect colors - Cores para loading skeleton
  /// EXPLICAÇÃO Shimmer:
  /// - Shimmer = efeito de "brilho" que passa por cima enquanto carrega
  /// - Usado em placeholders de imagens/texto enquanto dados carregam da API
  /// - Analogia: Como um "loading spinner" mas mais sofisticado
  static const Color shimmerBase = Color(0xFFE0E0E0); // Cor base
  static const Color shimmerHighlight = Color(0xFFF5F5F5); // Cor do brilho

  // Overlay colors - Cores de sobreposição (modals, dialogs)
  static const Color overlay = Color(0x80000000); // Preto 50% transparente (0x80 = 128/255)
  static const Color scrim = Color(0x99000000); // Preto 60% transparente (0x99 = 153/255)
}

/// COMO USAR (exemplos):
///
/// ```dart
/// // Em um Text widget (texto)
/// Text(
///   'Wine Reviewer',
///   style: TextStyle(color: AppColors.primary),
/// )
///
/// // Em um Container background
/// Container(
///   color: AppColors.background,
///   child: ...,
/// )
///
/// // Em um botão
/// ElevatedButton(
///   style: ElevatedButton.styleFrom(
///     backgroundColor: AppColors.primary,
///   ),
///   onPressed: () {},
///   child: Text('Entrar'),
/// )
/// ```
///
/// BOAS PRÁTICAS:
/// - SEMPRE usar AppColors.* ao invés de Color(0xFF...)
/// - Se precisar de nova cor, adicione aqui (não crie em outro lugar)
/// - Cores devem ter nomes semânticos (primary, error) não literais (red, blue)
