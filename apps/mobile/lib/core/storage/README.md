# Secure Storage - Flutter Secure Storage Integration

Este documento explica como o **flutter_secure_storage** está integrado no projeto e como usá-lo para armazenar dados sensíveis de forma segura.

---

## 📚 Índice

1. [O que é Flutter Secure Storage](#o-que-é-flutter-secure-storage)
2. [Como Funciona](#como-funciona)
3. [Integração no Projeto](#integração-no-projeto)
4. [Como Usar](#como-usar)
5. [Boas Práticas](#boas-práticas)
6. [Troubleshooting](#troubleshooting)

---

## 🔐 O que é Flutter Secure Storage

**Flutter Secure Storage** é um plugin que armazena dados de forma **criptografada** no device, usando APIs nativas do sistema operacional:

- **Android**: KeyStore (criptografia via hardware)
- **iOS**: Keychain (criptografia via hardware)
- **Windows**: Credential Manager
- **Linux**: libsecret
- **macOS**: Keychain

### Por que NÃO usar SharedPreferences para dados sensíveis?

| Aspecto | SharedPreferences | Flutter Secure Storage |
|---------|-------------------|------------------------|
| **Criptografia** | ❌ Plain text (qualquer app pode ler) | ✅ Criptografado (hardware encryption) |
| **Segurança** | ❌ Vulnerável a root/jailbreak | ✅ Protegido mesmo com root |
| **Uso** | ✅ Dados não-sensíveis (preferências UI) | ✅ Tokens, senhas, credenciais |
| **Performance** | ✅ Rápido | ⚠️ Um pouco mais lento (criptografia) |

**Regra de ouro:**
- **SharedPreferences**: Tema escuro, idioma, configurações de UI
- **Secure Storage**: JWT tokens, refresh tokens, API keys, credenciais

---

## ⚙️ Como Funciona

### Android (KeyStore)

```
App → flutter_secure_storage → Android KeyStore → Hardware Encryption
                                        ↓
                            Encrypted Data Storage
```

**Detalhes técnicos:**
- Usa **AES-256** para criptografar dados
- Chave de criptografia armazenada no **Hardware Security Module** (se disponível)
- Se device for rooteado, dados ainda estão protegidos (chave no hardware)
- Dados ficam em: `/data/data/<package>/shared_prefs/FlutterSecureStorage.xml` (criptografados)

### iOS (Keychain)

```
App → flutter_secure_storage → iOS Keychain → Secure Enclave (A-series chips)
                                        ↓
                            Encrypted Data Storage
```

**Detalhes técnicos:**
- Usa **AES-256-GCM** para criptografar dados
- Chave de criptografia armazenada no **Secure Enclave** (chip dedicado)
- Se device for jailbroken, dados ainda estão protegidos
- Suporta biometria (Touch ID/Face ID) como camada adicional

---

## 🏗️ Integração no Projeto

### 1. Provider (Dependency Injection)

**Arquivo:** `lib/core/providers/network_providers.dart`

```dart
final secureStorageProvider = Provider<FlutterSecureStorage>((ref) {
  return const FlutterSecureStorage();
});
```

**O que faz:**
- Cria instância única (Singleton) de `FlutterSecureStorage`
- Disponível para toda aplicação via Riverpod
- Pode ser injetado em qualquer provider/service

---

### 2. AuthInterceptor (Uso Principal)

**Arquivo:** `lib/core/network/auth_interceptor.dart`

O `AuthInterceptor` usa secure storage para gerenciar **JWT tokens**:

```dart
class AuthInterceptor extends Interceptor {
  final FlutterSecureStorage _storage;
  static const String _tokenKey = 'auth_jwt_token';

  // Salvar token após login
  Future<void> saveToken(String token) async {
    await _storage.write(key: _tokenKey, value: token);
  }

  // Ler token (adicionado automaticamente em requisições HTTP)
  @override
  void onRequest(RequestOptions options, RequestInterceptorHandler handler) async {
    final token = await _storage.read(key: _tokenKey);
    if (token != null) {
      options.headers['Authorization'] = 'Bearer $token';
    }
    handler.next(options);
  }

  // Remover token no logout
  Future<void> clearToken() async {
    await _storage.delete(key: _tokenKey);
  }

  // Verificar se token existe (auto-login)
  Future<bool> hasToken() async {
    final token = await _storage.read(key: _tokenKey);
    return token != null && token.isNotEmpty;
  }
}
```

**Quando é usado:**
- ✅ **Após login**: `authInterceptor.saveToken(jwtToken)` (em `AuthServiceImpl`)
- ✅ **Em toda requisição HTTP**: Token lido automaticamente e adicionado no header
- ✅ **No logout**: `authInterceptor.clearToken()` (em `AuthServiceImpl`)
- ✅ **No app startup**: `authInterceptor.hasToken()` (verificar se usuário está logado)

---

### 3. Fluxo Completo de Autenticação com Secure Storage

```
1. Usuário faz login com Google
   ↓
2. AuthServiceImpl.signInWithGoogle()
   ↓
3. Backend retorna JWT token
   ↓
4. authInterceptor.saveToken(token)
   ↓ (Token salvo criptografado no device)
5. Secure Storage → Android KeyStore/iOS Keychain
   ↓
6. Próxima requisição HTTP
   ↓
7. AuthInterceptor.onRequest()
   ↓
8. Token lido do Secure Storage
   ↓
9. Header adicionado: Authorization: Bearer <token>
   ↓
10. Requisição enviada ao backend
```

---

## 🚀 Como Usar

### Uso Básico (via Provider)

```dart
import 'package:flutter_riverpod/flutter_riverpod.dart';

class MyWidget extends ConsumerWidget {
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final storage = ref.read(secureStorageProvider);

    return ElevatedButton(
      onPressed: () async {
        // Escrever
        await storage.write(key: 'user_preference', value: 'dark_mode');

        // Ler
        final value = await storage.read(key: 'user_preference');
        print(value); // 'dark_mode'

        // Deletar
        await storage.delete(key: 'user_preference');

        // Deletar tudo
        await storage.deleteAll();
      },
      child: Text('Test Secure Storage'),
    );
  }
}
```

---

### Armazenar Dados Complexos (JSON)

Para armazenar objetos (não apenas strings), use JSON serialization:

```dart
import 'dart:convert';

// Salvar objeto
final user = User(id: '123', name: 'João');
final userJson = jsonEncode(user.toJson());
await storage.write(key: 'current_user', value: userJson);

// Ler objeto
final userJsonString = await storage.read(key: 'current_user');
if (userJsonString != null) {
  final userMap = jsonDecode(userJsonString) as Map<String, dynamic>;
  final user = User.fromJson(userMap);
  print(user.name); // 'João'
}
```

---

### Listar Todas as Keys

```dart
// Retorna Map<String, String> com todas as keys e valores
final allData = await storage.readAll();
allData.forEach((key, value) {
  print('$key: $value');
});
```

---

## ✅ Boas Práticas

### 1. Use Constantes para Keys

❌ **Ruim (magic strings):**
```dart
await storage.write(key: 'token', value: jwt);
final token = await storage.read(key: 'tokn'); // Typo → null
```

✅ **Bom (constantes):**
```dart
class StorageKeys {
  static const String authToken = 'auth_jwt_token';
  static const String refreshToken = 'auth_refresh_token';
  static const String userId = 'user_id';
}

await storage.write(key: StorageKeys.authToken, value: jwt);
final token = await storage.read(key: StorageKeys.authToken);
```

---

### 2. Sempre Trate Valores Nulos

❌ **Ruim (pode crashar):**
```dart
final token = await storage.read(key: 'token');
final isValid = token.length > 0; // Crash se token for null!
```

✅ **Bom (null-safety):**
```dart
final token = await storage.read(key: 'token');
final isValid = token != null && token.isNotEmpty;
```

---

### 3. Não Armazene Dados Grandes

**Secure Storage NÃO é para:**
- ❌ Imagens, vídeos, arquivos grandes
- ❌ Cache de dados (use Hive/SQLite)
- ❌ Logs extensivos

**Secure Storage É para:**
- ✅ JWT tokens (tipicamente < 2KB)
- ✅ Refresh tokens
- ✅ API keys
- ✅ Credenciais de usuário
- ✅ Configurações sensíveis (IDs, secrets)

**Limite recomendado:** < 10KB por valor

---

### 4. Limpe Dados no Logout

```dart
Future<void> logout() async {
  // Limpar apenas tokens (manter outras preferências)
  await storage.delete(key: StorageKeys.authToken);
  await storage.delete(key: StorageKeys.refreshToken);

  // OU limpar TUDO (logout completo)
  await storage.deleteAll();
}
```

---

### 5. Teste em Dispositivos Reais

⚠️ **Emulador vs Device Real:**
- **Emulador**: Pode NÃO ter hardware encryption (fallback para software)
- **Device Real**: Usa KeyStore/Keychain real (mais seguro)

**Sempre teste:**
- Login → Fechar app → Abrir app (token deve persistir)
- Logout → Token deve ser removido
- Desinstalar app → Reinstalar → Token NÃO deve existir

---

## 🐛 Troubleshooting

### Problema 1: "PlatformException: read_error"

**Causa:** KeyStore/Keychain corrompido (raro, mas acontece)

**Solução:**
```dart
try {
  final token = await storage.read(key: 'token');
} on PlatformException catch (e) {
  if (e.code == 'read_error') {
    // Limpar tudo e forçar re-login
    await storage.deleteAll();
    // Redirecionar para login
  }
}
```

---

### Problema 2: Token Não Persiste Após Reiniciar App

**Causa:** Não aguardou `await` ao salvar

❌ **Ruim:**
```dart
storage.write(key: 'token', value: jwt); // SEM await
```

✅ **Bom:**
```dart
await storage.write(key: 'token', value: jwt); // COM await
```

---

### Problema 3: Android - "Encryption not available"

**Causa:** Device muito antigo (Android < 6.0) sem KeyStore

**Solução:**
```dart
final storage = FlutterSecureStorage(
  aOptions: AndroidOptions(
    encryptedSharedPreferences: true, // Fallback para AES software
  ),
);
```

---

### Problema 4: iOS - Dados Perdidos Após Update

**Causa:** Keychain acessível apenas quando device desbloqueado

**Solução:**
```dart
final storage = FlutterSecureStorage(
  iOptions: IOSOptions(
    accessibility: KeychainAccessibility.first_unlock, // Persiste após primeiro unlock
  ),
);
```

---

## 📖 Referências

- **Package oficial**: [pub.dev/packages/flutter_secure_storage](https://pub.dev/packages/flutter_secure_storage)
- **Android KeyStore**: [developer.android.com/training/articles/keystore](https://developer.android.com/training/articles/keystore)
- **iOS Keychain**: [developer.apple.com/documentation/security/keychain_services](https://developer.apple.com/documentation/security/keychain_services)

---

## 🎯 Resumo

**O que está implementado no projeto:**
- ✅ `secureStorageProvider` - Singleton via Riverpod
- ✅ `AuthInterceptor` - Gerencia JWT tokens de forma segura
- ✅ Auto-login - Token persiste entre sessões
- ✅ Logout seguro - Token removido completamente

**Keys atualmente usadas:**
- `auth_jwt_token` - JWT access token (em `AuthInterceptor`)

**Próximos passos (futuro):**
- ⏳ Adicionar `auth_refresh_token` quando backend implementar
- ⏳ Cache de User data (opcional, para evitar requisições desnecessárias)
- ⏳ Biometria para acessar tokens (Face ID/Touch ID)

---

**Última atualização:** 2025-10-25
