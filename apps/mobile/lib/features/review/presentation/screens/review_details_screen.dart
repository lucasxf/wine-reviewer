import 'package:flutter/material.dart';

/// Review Details Screen - Tela de detalhes do review
///
/// EXPLICAÇÃO (O que é):
/// - Tela que mostra detalhes completos de um review
/// - Recebe reviewId como parâmetro da rota
/// - Por enquanto é placeholder com dados mockados
/// - Futuramente: integrará ReviewService.getReviewById(id)
///
/// ANALOGIA Backend:
/// - É como uma página details.html que mostra item específico
/// - Faz GET /reviews/{id}
/// - Renderiza dados detalhados do review
///
/// ROTA PARAMETRIZADA:
/// - Rota: /review/:id
/// - Acesso: final id = state.pathParameters['id']
/// - Navegação: context.go('/review/123')
class ReviewDetailsScreen extends StatelessWidget {
  /// ID do review (passado pela rota)
  ///
  /// EXPLICAÇÃO - Parâmetros de rota:
  /// - Passado via URL: /review/123
  /// - Extraído pelo router: state.pathParameters['id']
  /// - Injetado no constructor: ReviewDetailsScreen(reviewId: '123')
  final String reviewId;

  const ReviewDetailsScreen({
    super.key,
    required this.reviewId,
  });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      // AppBar com botão voltar automático
      // Scaffold adiciona botão voltar automaticamente quando há rota anterior
      appBar: AppBar(
        title: const Text('Detalhes do Review'),
        centerTitle: true,

        // actions = botões no canto direito
        actions: [
          // Menu de opções (editar, deletar)
          PopupMenuButton<String>(
            // onSelected = callback quando opção é selecionada
            onSelected: (value) => _handleMenuAction(context, value),

            // itemBuilder = constrói itens do menu
            //
            // POPUP MENU:
            // - Menu dropdown que aparece ao clicar no botão
            // - Similar a: dropdown, context menu
            itemBuilder: (context) => [
              const PopupMenuItem(
                value: 'edit',
                child: Row(
                  children: [
                    Icon(Icons.edit),
                    SizedBox(width: 8),
                    Text('Editar'),
                  ],
                ),
              ),
              const PopupMenuItem(
                value: 'delete',
                child: Row(
                  children: [
                    Icon(Icons.delete),
                    SizedBox(width: 8),
                    Text('Deletar'),
                  ],
                ),
              ),
            ],
          ),
        ],
      ),

      // body = conteúdo principal
      // SingleChildScrollView = permite scroll se conteúdo não cabe na tela
      //
      // EXPLICAÇÃO - SingleChildScrollView:
      // - Torna conteúdo scrollável verticalmente
      // - Use quando conteúdo pode ser maior que a tela
      // - Similar a: overflow-y: auto (CSS)
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),

        // Column = organiza widgets verticalmente
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Imagem do vinho (placeholder)
            _buildWineImage(context),

            const SizedBox(height: 24),

            // Nome do vinho
            _buildWineName(context),

            const SizedBox(height: 8),

            // Informações do vinho (vinícola, país, ano)
            _buildWineInfo(context),

            const SizedBox(height: 24),

            // Rating (taças de vinho)
            _buildRating(context),

            const SizedBox(height: 24),

            // Notas do review
            _buildReviewNotes(context),

            const SizedBox(height: 24),

            // Autor do review
            _buildAuthorInfo(context),

            const SizedBox(height: 24),

            // Data de criação
            _buildCreationDate(context),

            const SizedBox(height: 32),

            // Seção de comentários (futuro)
            _buildCommentsSection(context),
          ],
        ),
      ),
    );
  }

  /// Imagem do vinho
  Widget _buildWineImage(BuildContext context) {
    return ClipRRect(
      // borderRadius = bordas arredondadas
      borderRadius: BorderRadius.circular(12),

      // Container = box com cor de fundo, padding, margin, etc.
      //
      // EXPLICAÇÃO - Container:
      // - Widget versátil para estilização (similar a <div> no HTML)
      // - Pode ter: cor, padding, margin, borda, sombra, etc.
      child: Container(
        height: 200,
        width: double.infinity, // Largura total disponível
        color: Colors.grey[300],

        // Center = centraliza child
        child: const Icon(
          Icons.wine_bar,
          size: 80,
          color: Colors.grey,
        ),
      ),
    );
  }

  /// Nome do vinho
  Widget _buildWineName(BuildContext context) {
    return Text(
      'Vinho Mockado $reviewId', // Usa reviewId no nome
      style: Theme.of(context).textTheme.headlineMedium?.copyWith(
            fontWeight: FontWeight.bold,
          ),
    );
  }

  /// Informações do vinho (vinícola, país, ano)
  Widget _buildWineInfo(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        _buildInfoRow(Icons.business, 'Vinícola Mockada'),
        const SizedBox(height: 4),
        _buildInfoRow(Icons.public, 'País Mockado'),
        const SizedBox(height: 4),
        _buildInfoRow(Icons.calendar_today, '2020'),
        const SizedBox(height: 4),
        _buildInfoRow(Icons.category, 'Uva Mockada'),
      ],
    );
  }

  /// Row de informação (ícone + texto)
  ///
  /// EXPLICAÇÃO - Método helper (extração de widget):
  /// - Extrai widget reutilizável em método
  /// - Evita duplicação de código
  /// - Facilita manutenção
  Widget _buildInfoRow(IconData icon, String text) {
    return Row(
      children: [
        Icon(icon, size: 16, color: Colors.grey[600]),
        const SizedBox(width: 8),
        Text(
          text,
          style: TextStyle(
            fontSize: 14,
            color: Colors.grey[600],
          ),
        ),
      ],
    );
  }

  /// Rating (taças de vinho)
  Widget _buildRating(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Avaliação',
          style: Theme.of(context).textTheme.titleLarge?.copyWith(
                fontWeight: FontWeight.bold,
              ),
        ),
        const SizedBox(height: 8),

        // Row com taças de vinho (rating)
        Row(
          children: [
            // Gera 5 taças (4 preenchidas, 1 vazia)
            ...List.generate(5, (index) {
              return Icon(
                Icons.wine_bar,
                size: 32,
                color: index < 4
                    ? Theme.of(context).colorScheme.primary
                    : Colors.grey[300],
              );
            }),
            const SizedBox(width: 12),
            Text(
              '4.0',
              style: Theme.of(context).textTheme.titleLarge?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
            ),
          ],
        ),
      ],
    );
  }

  /// Notas do review
  Widget _buildReviewNotes(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Minhas Notas',
          style: Theme.of(context).textTheme.titleLarge?.copyWith(
                fontWeight: FontWeight.bold,
              ),
        ),
        const SizedBox(height: 8),

        // Card com notas
        Card(
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Text(
              'Este é um review mockado com notas detalhadas sobre o vinho. '
              'Em breve será substituído por dados reais da API. '
              'Aqui o usuário pode escrever suas impressões sobre aroma, sabor, '
              'corpo, final, etc.',
              style: Theme.of(context).textTheme.bodyMedium,
            ),
          ),
        ),
      ],
    );
  }

  /// Informações do autor
  Widget _buildAuthorInfo(BuildContext context) {
    return Row(
      children: [
        // Avatar do autor
        CircleAvatar(
          backgroundColor: Theme.of(context).colorScheme.primaryContainer,
          child: Icon(
            Icons.person,
            color: Theme.of(context).colorScheme.onPrimaryContainer,
          ),
        ),

        const SizedBox(width: 12),

        // Nome do autor
        Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Autor Mockado',
              style: Theme.of(context).textTheme.titleSmall?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
            ),
            Text(
              'autor@example.com',
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                    color: Colors.grey[600],
                  ),
            ),
          ],
        ),
      ],
    );
  }

  /// Data de criação
  Widget _buildCreationDate(BuildContext context) {
    return Row(
      children: [
        Icon(Icons.schedule, size: 16, color: Colors.grey[600]),
        const SizedBox(width: 4),
        Text(
          'Criado em 25/10/2025',
          style: TextStyle(
            fontSize: 12,
            color: Colors.grey[600],
          ),
        ),
      ],
    );
  }

  /// Seção de comentários (placeholder)
  Widget _buildCommentsSection(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Comentários',
          style: Theme.of(context).textTheme.titleLarge?.copyWith(
                fontWeight: FontWeight.bold,
              ),
        ),
        const SizedBox(height: 8),

        // Mensagem de placeholder
        Card(
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Center(
              child: Text(
                'Comentários serão implementados em breve',
                style: TextStyle(color: Colors.grey[600]),
              ),
            ),
          ),
        ),
      ],
    );
  }

  /// Handler de ações do menu
  void _handleMenuAction(BuildContext context, String action) {
    switch (action) {
      case 'edit':
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Editar review (em breve)')),
        );
        break;

      case 'delete':
        // Mostra dialog de confirmação
        _showDeleteConfirmation(context);
        break;
    }
  }

  /// Mostra dialog de confirmação de exclusão
  ///
  /// EXPLICAÇÃO - Dialog (caixa de diálogo):
  /// - Modal que aparece sobre a tela atual
  /// - Bloqueia interação com tela de fundo
  /// - Similar a: alert(), confirm() do JavaScript
  void _showDeleteConfirmation(BuildContext context) {
    showDialog(
      context: context,
      builder: (dialogContext) => AlertDialog(
        // title = título do dialog
        title: const Text('Confirmar Exclusão'),

        // content = conteúdo do dialog
        content: const Text('Tem certeza que deseja deletar este review?'),

        // actions = botões do dialog
        actions: [
          // Botão cancelar
          TextButton(
            onPressed: () => Navigator.of(dialogContext).pop(), // Fecha dialog
            child: const Text('Cancelar'),
          ),

          // Botão confirmar
          ElevatedButton(
            onPressed: () {
              Navigator.of(dialogContext).pop(); // Fecha dialog
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('Review deletado (mockado)')),
              );
            },
            child: const Text('Deletar'),
          ),
        ],
      ),
    );
  }
}

/// EXPLICAÇÃO DETALHADA - CONCEITOS IMPORTANTES:
///
/// 1. SINGLECHILDSCROLLVIEW (scroll vertical):
/// ```dart
/// SingleChildScrollView(
///   padding: EdgeInsets.all(16),
///   child: Column(
///     children: [ /* widgets */ ],
///   ),
/// )
/// ```
///
/// Quando usar:
/// - Conteúdo pode ser maior que a tela
/// - Evita overflow errors
/// - Similar a: overflow-y: auto (CSS)
///
/// 2. CONTAINER (box estilizado):
/// ```dart
/// Container(
///   width: 100,
///   height: 100,
///   padding: EdgeInsets.all(16),
///   margin: EdgeInsets.all(8),
///   decoration: BoxDecoration(
///     color: Colors.blue,
///     borderRadius: BorderRadius.circular(12),
///     boxShadow: [BoxShadow(...)],
///   ),
///   child: Text('Conteúdo'),
/// )
/// ```
///
/// 3. CLIPRRECT (bordas arredondadas):
/// ```dart
/// ClipRRect(
///   borderRadius: BorderRadius.circular(12),
///   child: Image.network('url'),  // Imagem com bordas arredondadas
/// )
/// ```
///
/// 4. POPUPMENU (menu dropdown):
/// ```dart
/// PopupMenuButton<String>(
///   onSelected: (value) { print(value); },
///   itemBuilder: (context) => [
///     PopupMenuItem(value: 'edit', child: Text('Editar')),
///     PopupMenuItem(value: 'delete', child: Text('Deletar')),
///   ],
/// )
/// ```
///
/// 5. ALERTDIALOG (caixa de diálogo):
/// ```dart
/// showDialog(
///   context: context,
///   builder: (context) => AlertDialog(
///     title: Text('Título'),
///     content: Text('Mensagem'),
///     actions: [
///       TextButton(
///         onPressed: () => Navigator.pop(context),
///         child: Text('Cancelar'),
///       ),
///       ElevatedButton(
///         onPressed: () {
///           Navigator.pop(context);
///           // Ação confirmada
///         },
///         child: Text('Confirmar'),
///       ),
///     ],
///   ),
/// )
/// ```
///
/// 6. EXTRAÇÃO DE WIDGETS (métodos helper):
///
/// Ruim (build() muito grande):
/// ```dart
/// @override
/// Widget build(BuildContext context) {
///   return Column(
///     children: [
///       Row(...),  // 50 linhas
///       Row(...),  // 50 linhas
///       Row(...),  // 50 linhas
///     ],
///   );
/// }
/// ```
///
/// Bom (extrair em métodos):
/// ```dart
/// @override
/// Widget build(BuildContext context) {
///   return Column(
///     children: [
///       _buildHeader(context),
///       _buildContent(context),
///       _buildFooter(context),
///     ],
///   );
/// }
///
/// Widget _buildHeader(BuildContext context) { /* ... */ }
/// Widget _buildContent(BuildContext context) { /* ... */ }
/// Widget _buildFooter(BuildContext context) { /* ... */ }
/// ```
///
/// ANALOGIA COMPLETA:
/// - SingleChildScrollView = overflow-y: auto (CSS)
/// - Container = <div> (HTML)
/// - ClipRRect = border-radius (CSS)
/// - PopupMenuButton = dropdown menu (HTML select)
/// - AlertDialog = confirm() (JavaScript)
/// - _buildXxx() = métodos privados para organizar código (refactoring)
