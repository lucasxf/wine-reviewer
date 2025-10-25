# 🚀 Instruções de Setup do Flutter App

## Passo 1: Aceitar Licenças Android

**O que é:** Licenças do Android SDK precisam ser aceitas para compilar APKs.

**Por que:** Sem aceitar, o `flutter build` vai falhar.

**Como fazer:**
1. Abra PowerShell ou CMD no Windows
2. Execute:
   ```powershell
   flutter doctor --android-licenses
   ```
3. Pressione `y` para aceitar todas as licenças

---

## Passo 2: Criar Projeto Flutter

**O que é:** `flutter create` gera a estrutura base do projeto Flutter.

**Por que:** Cria arquivos de configuração (pubspec.yaml, Android/iOS configs, main.dart template).

**Como fazer:**
1. No PowerShell, navegue até o diretório do projeto:
   ```powershell
   cd C:\repo\wine-reviewer\apps\mobile
   ```

2. Crie o projeto Flutter:
   ```powershell
   flutter create --org com.winereviewer --project-name wine_reviewer_mobile .
   ```

**Explicação dos parâmetros:**
- `--org com.winereviewer` → Define o package name Android como `com.winereviewer.wine_reviewer_mobile`
- `--project-name wine_reviewer_mobile` → Nome do projeto (snake_case obrigatório em Dart)
- `.` → Cria no diretório atual (apps/mobile)

**O que será criado:**
```
apps/mobile/
├── android/           # Configurações Android (Gradle, AndroidManifest.xml)
├── ios/               # Configurações iOS (Xcode, Info.plist)
├── lib/               # Código Dart (main.dart inicial)
├── test/              # Testes unitários
├── pubspec.yaml       # Dependências (equivalente ao package.json do Node ou pom.xml do Maven)
├── analysis_options.yaml  # Regras de linting Dart
└── README.md          # Sobrescreverá o README atual
```

---

## Passo 3: Verificar Criação

Execute:
```powershell
flutter doctor -v
```

Deve mostrar tudo OK para Android.

---

## Passo 4: Notificar Claude

Após executar os comandos acima, me avise que concluiu. Eu continuarei com:
- Configuração do `pubspec.yaml` (adicionar dependências)
- Criação da estrutura de pastas (features/, core/, common/)
- Setup de Riverpod, go_router, dio
- Criação do `main.dart` inicial

---

## ℹ️ Por que não posso fazer isso automaticamente?

Claude Code está rodando em WSL/Linux (bash), mas o Flutter está instalado no Windows.
Por isso, comandos `flutter` precisam ser executados no PowerShell/CMD do Windows.
