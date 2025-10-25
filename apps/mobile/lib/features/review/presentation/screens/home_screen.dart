import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

/// Home Screen - Tela principal (feed de reviews)
///
/// EXPLICAÇÃO (O que é):
/// - Tela principal do app após login
/// - Lista reviews de vinhos (feed)
/// - Por enquanto é placeholder com lista mockada
/// - Futuramente: integrará ReviewService + paginação
///
/// ANALOGIA Backend:
/// - É como uma página index.html que lista itens
/// - Faz GET /reviews (paginado)
/// - Renderiza lista com dados do backend
///
/// LISTVIEW (lista de itens):
/// - Widget que renderiza lista scrollável
/// - Similar a: <ul><li> (HTML), RecyclerView (Android), UITableView (iOS)
/// - Otimizado: apenas renderiza itens visíveis na tela (lazy loading)
class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      // AppBar com título e ações
      appBar: AppBar(
        title: const Text('Wine Reviewer'),
        centerTitle: true,

        // actions = botões no canto direito do AppBar
        // List<Widget> = lista de widgets (botões, ícones)
        actions: [
          // IconButton = botão apenas com ícone (sem texto)
          IconButton(
            icon: const Icon(Icons.logout),
            tooltip: 'Sair', // Tooltip = texto ao segurar botão (acessibilidade)
            onPressed: () => _handleLogout(context),
          ),
        ],
      ),

      // body = conteúdo principal
      // Lista de reviews mockada (placeholder)
      body: ListView.builder(
        // itemCount = número de itens na lista
        // Se null, lista é infinita (scroll infinito)
        itemCount: 10, // 10 itens mockados

        // padding = espaçamento interno da lista
        padding: const EdgeInsets.all(8),

        // itemBuilder = função que constrói cada item da lista
        //
        // EXPLICAÇÃO - itemBuilder:
        // - Chamado para cada item da lista (lazy loading)
        // - Parâmetros:
        //   - context: BuildContext
        //   - index: índice do item (0, 1, 2, ...)
        // - Retorna: Widget que representa o item
        //
        // ANALOGIA Backend:
        // - Similar a: forEach loop em JSP/Thymeleaf
        // - <c:forEach items="${reviews}" var="review">
        itemBuilder: (context, index) {
          // Retorna Card para cada item
          // Card = widget Material Design (elevado, com bordas arredondadas)
          return Card(
            // margin = espaçamento externo entre cards
            margin: const EdgeInsets.symmetric(
              vertical: 8,
              horizontal: 4,
            ),

            // child = conteúdo do card
            // ListTile = widget Material Design para lista de itens
            // Fornece: leading (ícone esquerda), title, subtitle, trailing (ícone direita)
            child: ListTile(
              // leading = widget à esquerda (geralmente ícone ou avatar)
              leading: CircleAvatar(
                // backgroundColor = cor de fundo do avatar
                backgroundColor: Theme.of(context).colorScheme.primaryContainer,
                child: Icon(
                  Icons.wine_bar,
                  color: Theme.of(context).colorScheme.onPrimaryContainer,
                ),
              ),

              // title = texto principal (negrito)
              title: Text('Vinho Mockado ${index + 1}'),

              // subtitle = texto secundário (menor, cinza)
              subtitle: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const SizedBox(height: 4),

                  // Rating (estrelas) - por enquanto mockado
                  Row(
                    children: [
                      // Icon com tamanho pequeno (estrela)
                      ...List.generate(
                        5, // 5 estrelas (rating mockado)
                        (starIndex) => Icon(
                          Icons.wine_bar,
                          size: 16,
                          color: starIndex < 4
                              ? Theme.of(context).colorScheme.primary
                              : Colors.grey[300],
                        ),
                      ),
                      const SizedBox(width: 8),
                      Text(
                        '4.0',
                        style: Theme.of(context).textTheme.bodySmall,
                      ),
                    ],
                  ),

                  const SizedBox(height: 4),

                  // Notas do review (truncado)
                  Text(
                    'Este é um review mockado com notas sobre o vinho. Em breve será substituído por dados reais da API.',
                    maxLines: 2, // Máximo 2 linhas
                    overflow: TextOverflow.ellipsis, // ... no final se passar 2 linhas
                  ),
                ],
              ),

              // trailing = widget à direita (geralmente ícone, badge)
              trailing: const Icon(Icons.chevron_right),

              // onTap = callback quando item é clicado
              //
              // NAVEGAÇÃO PARAMETRIZADA:
              // - context.go('/review/${index + 1}')
              // - Passa ID do review como parâmetro na URL
              // - Similar a: <a href="/review/1"> (HTML)
              onTap: () {
                // Navega para tela de detalhes do review
                // Em produção: passar ID real do review
                context.go('/review/${index + 1}');
              },
            ),
          );
        },
      ),

      // floatingActionButton = botão flutuante (canto inferior direito)
      //
      // EXPLICAÇÃO - FAB (Floating Action Button):
      // - Botão circular flutuante (Material Design)
      // - Usado para ação primária da tela (criar, adicionar, etc.)
      // - Geralmente no canto inferior direito
      //
      // QUANDO USAR:
      // - Ação primária da tela (criar review, adicionar item)
      // - Ação que se destaca das outras
      floatingActionButton: FloatingActionButton.extended(
        // onPressed = callback quando botão é clicado
        onPressed: () {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('Criar review será implementado em breve'),
            ),
          );
        },

        // icon = ícone do botão
        icon: const Icon(Icons.add),

        // label = texto do botão (extended FAB tem texto + ícone)
        label: const Text('Novo Review'),
      ),
    );
  }

  /// Handler de logout
  ///
  /// FLUXO:
  /// 1. Limpa JWT token do storage (authInterceptor.clearToken())
  /// 2. Redireciona para /login
  ///
  /// POR ENQUANTO: apenas redireciona (simula logout)
  void _handleLogout(BuildContext context) {
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text('Logout simulado! Redirecionando...'),
        duration: Duration(seconds: 1),
      ),
    );

    // Aguarda 1 segundo e redireciona para login
    Future.delayed(const Duration(seconds: 1), () {
      if (context.mounted) {
        // context.go() = substitui tela atual (não pode voltar)
        context.go('/login');
      }
    });
  }
}

/// EXPLICAÇÃO DETALHADA - CONCEITOS IMPORTANTES:
///
/// 1. LISTVIEW.BUILDER (lista otimizada):
///
/// Por que builder? (lazy loading)
/// - ListView simples: cria TODOS os itens de uma vez (ruim para listas grandes)
/// - ListView.builder: cria apenas itens VISÍVEIS na tela (performance)
///
/// ```dart
/// ListView.builder(
///   itemCount: 100,  // 100 itens
///   itemBuilder: (context, index) {
///     return ListTile(title: Text('Item $index'));
///   },
/// )
/// ```
///
/// Analogia:
/// - Similar a: paginação do Spring Data (Page<Review>)
/// - Similar a: RecyclerView do Android (view recycling)
///
/// 2. CARD (widget elevado):
/// ```dart
/// Card(
///   elevation: 4,              // Altura da sombra (0-24)
///   margin: EdgeInsets.all(8), // Espaçamento externo
///   child: ListTile(...),      // Conteúdo do card
/// )
/// ```
///
/// 3. LISTTILE (item de lista padrão):
/// ```dart
/// ListTile(
///   leading: Icon(Icons.person),      // Ícone esquerda
///   title: Text('Nome'),              // Texto principal
///   subtitle: Text('Descrição'),      // Texto secundário
///   trailing: Icon(Icons.arrow_right), // Ícone direita
///   onTap: () { /* ação */ },         // Callback de clique
/// )
/// ```
///
/// 4. CIRCLEAVATAR (avatar circular):
/// ```dart
/// CircleAvatar(
///   radius: 20,                   // Tamanho do círculo
///   backgroundColor: Colors.blue, // Cor de fundo
///   backgroundImage: NetworkImage('url'), // Imagem de rede
///   child: Icon(Icons.person),    // Ícone (se não tem imagem)
/// )
/// ```
///
/// 5. ROW + LIST.GENERATE (criar múltiplos widgets):
/// ```dart
/// Row(
///   children: [
///     ...List.generate(5, (index) => Icon(Icons.star)),
///     // Gera 5 ícones de estrela
///   ],
/// )
/// ```
///
/// Equivalente a:
/// ```dart
/// Row(
///   children: [
///     Icon(Icons.star),
///     Icon(Icons.star),
///     Icon(Icons.star),
///     Icon(Icons.star),
///     Icon(Icons.star),
///   ],
/// )
/// ```
///
/// 6. TEXT OVERFLOW (truncar texto):
/// ```dart
/// Text(
///   'Texto muito longo que será truncado...',
///   maxLines: 2,                      // Máximo 2 linhas
///   overflow: TextOverflow.ellipsis,  // ... no final
/// )
/// ```
///
/// Opções de overflow:
/// - TextOverflow.clip = corta texto
/// - TextOverflow.fade = fade no final
/// - TextOverflow.ellipsis = ... no final
///
/// 7. FLOATING ACTION BUTTON (FAB):
///
/// FAB simples (apenas ícone):
/// ```dart
/// FloatingActionButton(
///   onPressed: () { /* ação */ },
///   child: Icon(Icons.add),
/// )
/// ```
///
/// FAB extended (ícone + texto):
/// ```dart
/// FloatingActionButton.extended(
///   onPressed: () { /* ação */ },
///   icon: Icon(Icons.add),
///   label: Text('Adicionar'),
/// )
/// ```
///
/// 8. NAVEGAÇÃO COM PARÂMETROS:
/// ```dart
/// // Navegar passando ID na URL
/// context.go('/review/123');
///
/// // No router, definir rota com parâmetro:
/// GoRoute(
///   path: '/review/:id',  // :id = parâmetro dinâmico
///   builder: (context, state) {
///     final id = state.pathParameters['id']; // Lê parâmetro
///     return ReviewDetailsScreen(reviewId: id);
///   },
/// )
/// ```
///
/// ANALOGIA COMPLETA:
/// - ListView.builder = Page<Review> (paginação backend)
/// - Card = <div class="card"> (Bootstrap)
/// - ListTile = <li> com estrutura padrão (leading + title + subtitle + trailing)
/// - CircleAvatar = avatar circular (similar a profile pic)
/// - FloatingActionButton = botão de ação primária (+ no canto)
/// - context.go('/review/:id') = redirect:/review/1 (Spring MVC)
