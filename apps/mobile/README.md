# 📱 Mobile App (Flutter)

> Código-fonte em **inglês**. Qualidade > velocidade. Hospedagem do MVP 100% gratuita (somente serviços free para backend/storage).

## ✅ Objetivos
- Navegação com `go_router`.
- Estado com **Riverpod** (ou Bloc, se for decidido e documentado).
- Login com **Google** já na **Fase 1**.
- Feed de reviews, criação de review (1–5 taças) com **image_picker** e envio via pre-signed URL.
- Testes: unit, widget e **golden tests**.

## 🧰 Dependências recomendadas (pubspec)
- `flutter_riverpod` / `hooks_riverpod`
- `go_router`
- `dio`
- `freezed` + `json_serializable`
- `flutter_secure_storage`
- `cached_network_image`
- `image_picker`

## ▶️ Rodando o app
```bash
flutter pub get
flutter run  # selecione um dispositivo Android
```

## 🧪 Testes
```bash
flutter test --coverage
```

## 🔒 Login com Google (esboço)
- Use `google_sign_in` e integre com a API via JWT/OIDC no backend.
- Armazene tokens com `flutter_secure_storage`.
- **Nunca** comite secrets.

## 📐 Convenções
- Pastas: `lib/features`, `lib/core`, `lib/common/widgets`.
- Modelos e DTOs com `freezed` + `json_serializable`.
- Estilo consistente com `dart format` e `flutter analyze`.
