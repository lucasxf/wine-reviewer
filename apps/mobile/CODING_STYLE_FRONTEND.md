# Coding Style Guide - Frontend (Flutter/Dart)

> Frontend-specific coding standards for mobile app development with Flutter.
> **Part of:** Wine Reviewer Project
> **Applies to:** `apps/mobile/` (Flutter 3.x, Dart, Riverpod)

**For universal cross-stack guidelines, see:** `../../CODING_STYLE_GENERAL.md`

---

# 📱 FRONTEND STANDARDS (Flutter/Dart)

## 📦 Estrutura de Projeto (Flutter)

### Feature-based Folder Structure
```
lib/
├── features/              # Features do app
│   ├── auth/             # Feature de autenticação
│   │   ├── data/         # Data sources, repositories
│   │   ├── domain/       # Entities, use cases
│   │   └── presentation/ # Screens, widgets, providers
│   ├── reviews/          # Feature de reviews
│   └── wines/            # Feature de vinhos
├── core/                  # Código compartilhado
│   ├── router/           # Configuração de rotas (go_router)
│   ├── theme/            # Tema do app
│   ├── utils/            # Utilitários
│   └── constants/        # Constantes
└── common/                # Widgets e código reutilizável
    ├── widgets/          # Widgets comuns
    └── models/           # Models compartilhados
```

## 📝 Convenções de Código (Flutter/Dart)

### Nomenclatura

- **Classes:** PascalCase - `ReviewCard`, `AuthProvider`, `WineList`
- **Arquivos:** snake_case - `review_card.dart`, `auth_provider.dart`, `wine_list.dart`
- **Variáveis/Métodos:** camelCase - `getUserById`, `isLoading`, `reviewList`
- **Constantes:** lowerCamelCase (Dart convention) - `maxFileSize`, `apiBaseUrl`

### Models e DTOs

- Usar **freezed** para models imutáveis
- Usar **json_serializable** para serialização
- Gerar código com `flutter pub run build_runner build --delete-conflicting-outputs`

**Exemplo:**
```dart
import 'package:freezed_annotation/freezed_annotation.dart';

part 'review.freezed.dart';
part 'review.g.dart';

@freezed
class Review with _$Review {
  const factory Review({
    required String id,
    required int rating,
    required String notes,
    String? imageUrl,
  }) = _Review;

  factory Review.fromJson(Map<String, dynamic> json) => _$ReviewFromJson(json);
}
```

### State Management (Riverpod)

- Usar Riverpod para state management
- Providers no mesmo arquivo da feature ou em `providers/`
- Naming: `reviewProvider`, `authStateProvider`

**Exemplo:**
```dart
@riverpod
class ReviewList extends _$ReviewList {
  @override
  Future<List<Review>> build() async {
    final repository = ref.read(reviewRepositoryProvider);
    return repository.getReviews();
  }

  Future<void> addReview(Review review) async {
    final repository = ref.read(reviewRepositoryProvider);
    await repository.createReview(review);
    ref.invalidateSelf();
  }
}
```

### Error Handling

- Usar dio interceptors para retry logic
- Tratar erros de rede gracefully
- Mostrar mensagens de erro amigáveis ao usuário

**Exemplo:**
```dart
class DioClient {
  final Dio _dio = Dio()
    ..interceptors.add(
      RetryInterceptor(
        dio: _dio,
        maxRetries: 3,
        retryDelays: const [
          Duration(seconds: 1),
          Duration(seconds: 2),
          Duration(seconds: 3),
        ],
      ),
    );
}
```

### Widgets

- Preferir StatelessWidget quando possível
- Extrair widgets complexos em componentes separados
- Usar `const` constructors sempre que possível para performance

### Formatação

- Usar `dart format .` para formatar código
- Seguir Effective Dart style guide
- Limite de 80 caracteres por linha (configurável)

## 🧪 Testes (Flutter)

### Tipos de Testes

1. **Unit Tests:** Lógica de negócio, providers, repositories
2. **Widget Tests:** Widgets individuais e telas
3. **Golden Tests:** Testes de regressão visual

### Nomenclatura

- Arquivos de teste: `_test.dart` suffix
- Exemplo: `review_card_test.dart`, `auth_provider_test.dart`

**Exemplo de Widget Test:**
```dart
void main() {
  testWidgets('ReviewCard displays review information', (tester) async {
    final review = Review(
      id: '1',
      rating: 5,
      notes: 'Excellent wine!',
    );

    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: ReviewCard(review: review),
        ),
      ),
    );

    expect(find.text('Excellent wine!'), findsOneWidget);
    expect(find.text('5'), findsOneWidget);
  });
}
```

## 🎨 UI/UX

- Material Design como base
- Tema customizado em `core/theme/`
- Responsividade: testar em diferentes tamanhos de tela
- Acessibilidade: labels para screen readers

## ✅ Checklist de Code Review (Flutter)

- [ ] Código segue Effective Dart guidelines
- [ ] Models usam freezed + json_serializable
- [ ] State management com Riverpod
- [ ] Widgets extraídos quando complexos
- [ ] Uso de `const` constructors
- [ ] Error handling apropriado
- [ ] Testes (unit/widget) para lógica crítica
- [ ] Formatação com `dart format`
- [ ] Sem warnings no `flutter analyze`

---

**For general cross-stack conventions, see:** `../../CODING_STYLE_GENERAL.md`

