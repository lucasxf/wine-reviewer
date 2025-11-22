# Mobile App Commands

Comprehensive command reference for the Wine Reviewer mobile app (Flutter + Dart).

---

## Development Commands

### Run Application

```bash
# Install dependencies
flutter pub get

# Run app (auto-selects available device)
flutter run

# Run on specific device
flutter devices                    # List available devices
flutter run -d <device-id>         # Run on specific device
flutter run -d emulator-5554       # Run on Android emulator
flutter run -d chrome              # Run on Chrome (web)

# Run with hot reload enabled (default)
flutter run

# Run in release mode (optimized)
flutter run --release

# Run in profile mode (for performance analysis)
flutter run --profile
```

**While running:**
- Press `r` - Hot reload (apply code changes)
- Press `R` - Hot restart (full app restart)
- Press `p` - Toggle performance overlay
- Press `o` - Toggle platform (Android/iOS rendering)
- Press `q` - Quit

---

## Testing Commands

### Run Tests

```bash
# Run all tests
flutter test

# Run tests with coverage
flutter test --coverage

# View coverage report (requires lcov)
genhtml coverage/lcov.info -o coverage/html
# Open: coverage/html/index.html

# Run specific test file
flutter test test/features/auth/auth_test.dart

# Run tests matching pattern
flutter test --name "login"

# Run tests in watch mode (re-run on changes)
flutter test --watch
```

### Widget and Integration Tests

```bash
# Run widget tests only
flutter test test/features/

# Run integration tests
flutter test integration_test/

# Run integration tests on device
flutter test integration_test/app_test.dart -d <device-id>
```

### Golden Tests (Visual Regression)

```bash
# Generate golden files (reference images)
flutter test --update-goldens

# Run golden tests
flutter test test/golden/

# Update specific golden test
flutter test test/golden/login_screen_test.dart --update-goldens
```

---

## Code Quality Commands

### Analysis and Linting

```bash
# Analyze code for issues
flutter analyze

# Analyze with custom options
flutter analyze --no-fatal-infos
flutter analyze --watch  # Continuous analysis

# Fix auto-fixable issues
dart fix --apply
```

### Code Formatting

```bash
# Format all Dart files
dart format .

# Format specific file
dart format lib/main.dart

# Check formatting without modifying files
dart format --output=none --set-exit-if-changed .

# Format with line length limit
dart format --line-length=120 .
```

---

## Code Generation Commands

### Freezed + JSON Serialization

```bash
# Generate code (models, serialization)
flutter pub run build_runner build

# Generate with deletion of conflicting outputs
flutter pub run build_runner build --delete-conflicting-outputs

# Watch mode (auto-regenerate on file changes)
flutter pub run build_runner watch

# Clean generated files
flutter pub run build_runner clean
```

---

## Build Commands

### Android Build

```bash
# Build APK (debug)
flutter build apk

# Build APK (release)
flutter build apk --release

# Build APK (split per ABI - smaller file sizes)
flutter build apk --split-per-abi

# Build App Bundle (for Play Store)
flutter build appbundle --release

# Build with obfuscation and symbols
flutter build apk --release --obfuscate --split-debug-info=./debug-symbols
```

### iOS Build (macOS only)

```bash
# Build iOS app
flutter build ios

# Build iOS app (release)
flutter build ios --release

# Build IPA (for App Store)
flutter build ipa --release
```

### Web Build

```bash
# Build for web
flutter build web

# Build for web (release)
flutter build web --release

# Build with specific renderer
flutter build web --web-renderer canvaskit  # Better performance
flutter build web --web-renderer html       # Smaller size
```

---

## Dependency Management

### Add Dependencies

```bash
# Add dependency
flutter pub add dio
flutter pub add riverpod

# Add dev dependency
flutter pub add --dev mocktail

# Add dependency with specific version
flutter pub add dio:5.0.0
```

### Update Dependencies

```bash
# Get dependencies (install/update)
flutter pub get

# Update dependencies to latest versions
flutter pub upgrade

# Update specific dependency
flutter pub upgrade dio

# Outdated dependencies report
flutter pub outdated
```

### Remove Dependencies

```bash
# Remove dependency
flutter pub remove dio

# Remove dev dependency
flutter pub remove --dev mocktail
```

---

## Device and Emulator Management

### Device Management

```bash
# List all devices (connected + emulators)
flutter devices

# List emulators only
flutter emulators

# Launch emulator
flutter emulators --launch <emulator-id>
flutter emulators --launch Pixel_7_API_34

# Create new emulator (Android)
avdmanager create avd --name "Pixel_7_API_34" \
  --package "system-images;android-34;google_apis_playstore;x86_64" \
  --device "pixel_7"
```

### Flutter Doctor

```bash
# Check Flutter installation and dependencies
flutter doctor

# Verbose output (more details)
flutter doctor -v

# Check Android licenses
flutter doctor --android-licenses
```

---

## Performance and Debugging

### Performance Profiling

```bash
# Run in profile mode
flutter run --profile

# Run with performance overlay
flutter run --profile --trace-skia

# Dump performance data
flutter screenshot --observatory-url=<url> --type=skia
```

### Debugging

```bash
# Run with debug logging
flutter run --verbose

# Attach debugger to running app
flutter attach

# Inspect widget tree
flutter run --debug
# Then: Press 'w' to dump widget tree
```

### Observatory and DevTools

```bash
# Open DevTools
flutter pub global activate devtools
flutter pub global run devtools

# Run app with DevTools
flutter run --devtools-server-address=http://127.0.0.1:9100
```

---

## Clean and Reset

### Clean Build Artifacts

```bash
# Clean build files
flutter clean

# Clean and reinstall dependencies
flutter clean && flutter pub get

# Clean generated code
flutter pub run build_runner clean
```

### Reset Flutter

```bash
# Reset Flutter SDK (removes cached files)
flutter doctor -v  # Check current setup
flutter clean
flutter pub cache repair

# Nuclear option: Delete and re-download Flutter SDK
# (Backup your projects first!)
```

---

## Localization (i18n)

```bash
# Generate localization files (if configured)
flutter gen-l10n

# Generate with custom config
flutter gen-l10n --arb-dir=lib/l10n --output-dir=lib/generated
```

---

## Troubleshooting

### Common Issues

**Issue:** "CocoaPods not installed" (iOS)
```bash
# Install CocoaPods (macOS only)
sudo gem install cocoapods
pod setup

# Update CocoaPods
sudo gem install cocoapods --pre
```

**Issue:** "Gradle build failed" (Android)
```bash
# Clean and rebuild
flutter clean
cd android
./gradlew clean
cd ..
flutter pub get
flutter run

# Update Gradle wrapper
cd android
./gradlew wrapper --gradle-version=8.0
```

**Issue:** "Hot reload not working"
```bash
# Stop app and restart
flutter run

# If still broken, full clean
flutter clean
flutter pub get
flutter run
```

**Issue:** "Build runner conflicts"
```bash
# Delete conflicting outputs
flutter pub run build_runner build --delete-conflicting-outputs

# Nuclear option
flutter pub run build_runner clean
rm -rf .dart_tool/build
flutter pub get
flutter pub run build_runner build --delete-conflicting-outputs
```

**Issue:** "Out of memory during build"
```bash
# Increase Gradle memory (android/gradle.properties)
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=1024m

# Clear Gradle cache
cd android
./gradlew --stop
rm -rf ~/.gradle/caches
```

---

## CI/CD Commands

### Pre-Commit Checks

```bash
# Run all checks (format, analyze, test)
dart format . && flutter analyze && flutter test

# Quick validation
flutter analyze --no-fatal-infos && flutter test --no-pub
```

### Build for Release

```bash
# Android release build
flutter build apk --release --split-per-abi

# iOS release build (macOS only)
flutter build ipa --release

# Web release build
flutter build web --release --web-renderer canvaskit
```

---

## Useful Aliases (Optional)

Add to `.bashrc` or `.zshrc` for convenience:

```bash
# Flutter aliases
alias fr='flutter run'
alias ft='flutter test'
alias fa='flutter analyze'
alias fp='flutter pub get'
alias fc='flutter clean'
alias fbr='flutter pub run build_runner build --delete-conflicting-outputs'
alias fbrw='flutter pub run build_runner watch --delete-conflicting-outputs'
```

---

## Related Documentation

- **CODING_STYLE_FRONTEND.md** - Flutter/Dart coding conventions
- **README.md** - Frontend architecture and setup instructions
- **CLAUDE.md** - Project-wide guidelines
- **Flutter Docs** - https://docs.flutter.dev/
- **Dart Docs** - https://dart.dev/guides
