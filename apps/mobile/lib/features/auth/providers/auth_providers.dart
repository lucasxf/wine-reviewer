import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_riverpod/legacy.dart'; // TODO: Migrate to Notifier API (Riverpod 3.0)
import 'package:google_sign_in/google_sign_in.dart';
import 'package:wine_reviewer_mobile/core/providers/network_providers.dart';
import 'package:wine_reviewer_mobile/features/auth/data/services/auth_service.dart';
import 'package:wine_reviewer_mobile/features/auth/data/services/auth_service_impl.dart';
import 'package:wine_reviewer_mobile/features/auth/domain/models/user.dart';

import 'auth_state.dart';
import 'auth_state_notifier.dart';

/// Providers for authentication feature - Dependency Injection with Riverpod.
///
/// This file defines all providers needed for authentication:
/// 1. GoogleSignInProvider - GoogleSignIn instance
/// 2. AuthServiceProvider - AuthService implementation with dependencies
/// 3. AuthStateNotifierProvider - Manages authentication state
/// 4. CurrentUserProvider - Derived provider that exposes current user
///
/// **Why separate providers file:**
/// - Centralized dependency injection configuration
/// - Easy to override providers in tests
/// - Clear dependency graph visualization
/// - Follows Riverpod best practices
///
/// **Dependency Graph:**
/// ```
/// secureStorageProvider (core/providers)
///         ↓
/// authInterceptorProvider (core/providers)
///         ↓
/// dioClientProvider (core/providers)
///         ↓
/// googleSignInProvider (this file)
///         ↓
/// authServiceProvider (this file)
///         ↓
/// authStateNotifierProvider (this file)
///         ↓
/// currentUserProvider (this file)
/// ```

// =============================================================================
// GoogleSignIn Provider
// =============================================================================

/// Provider for GoogleSignIn instance.
///
/// **What is GoogleSignIn:**
/// - Package from Google for OAuth authentication
/// - Opens Google account picker dialog
/// - Returns user account + ID token
///
/// **Configuration:**
/// - No scopes needed (we only need basic profile + email)
/// - scopes can be added for accessing Google APIs (Drive, Calendar, etc.)
///
/// **Singleton:**
/// - Same instance used throughout the app
/// - Maintains Google Sign-In session
///
/// **Example usage (rare - usually use authServiceProvider):**
/// ```dart
/// final googleSignIn = ref.read(googleSignInProvider);
/// final account = await googleSignIn.signIn();
/// ```
final googleSignInProvider = Provider<GoogleSignIn>((ref) {
  return GoogleSignIn(
    // Scopes define what Google data we can access
    // Empty list = only basic profile (name, email, avatar)
    // Add scopes like 'email', 'profile' if needed
    scopes: [],
  );
});

// =============================================================================
// AuthService Provider
// =============================================================================

/// Provider for AuthService implementation.
///
/// **Dependencies (auto-injected by Riverpod):**
/// - dioClientProvider - HTTP client for API requests
/// - googleSignInProvider - Google OAuth authentication
/// - authInterceptorProvider - Manages JWT tokens
///
/// **Why use Provider\<AuthService\> instead of Provider\<AuthServiceImpl\>:**
/// - Depend on interface, not implementation (Dependency Inversion Principle)
/// - Easy to mock in tests (just override this provider)
/// - Can swap implementations without changing UI code
///
/// **Example usage:**
/// ```dart
/// // In AuthStateNotifier or UI
/// final authService = ref.read(authServiceProvider);
/// final user = await authService.signInWithGoogle();
/// ```
///
/// **Analogia Backend (Spring Boot):**
/// ```java
/// @Configuration
/// public class AuthConfig {
///   @Bean
///   public AuthService authService(
///       DioClient client,
///       GoogleSignIn googleSignIn,
///       AuthInterceptor interceptor
///   ) {
///     return new AuthServiceImpl(client, googleSignIn, interceptor);
///   }
/// }
/// ```
final authServiceProvider = Provider<AuthService>((ref) {
  // Inject dependencies from other providers
  final dioClient = ref.watch(dioClientProvider);
  final googleSignIn = ref.watch(googleSignInProvider);
  final authInterceptor = ref.watch(authInterceptorProvider);
  final secureStorage = ref.watch(secureStorageProvider); // FIX: Added for user data caching

  // Create and return AuthServiceImpl with dependencies
  return AuthServiceImpl(
    client: dioClient,
    googleSignIn: googleSignIn,
    authInterceptor: authInterceptor,
    storage: secureStorage, // FIX: Inject storage for user data caching
  );
});

// =============================================================================
// AuthStateNotifier Provider
// =============================================================================

/// Provider for AuthStateNotifier - manages authentication state.
///
/// **What is StateNotifierProvider:**
/// - Provides both the state (AuthState) and the notifier (AuthStateNotifier)
/// - State is read-only from UI (immutable)
/// - Notifier exposes methods to change state (signInWithGoogle, signOut)
///
/// **How to use:**
/// ```dart
/// // Read state (rebuilds widget when state changes)
/// final authState = ref.watch(authStateNotifierProvider);
///
/// authState.when(
///   initial: () => SplashScreen(),
///   authenticated: (user) => HomeScreen(user: user),
///   unauthenticated: () => LoginScreen(),
///   loading: () => LoadingIndicator(),
/// );
///
/// // Call methods (doesn't rebuild widget)
/// final authNotifier = ref.read(authStateNotifierProvider.notifier);
/// await authNotifier.signInWithGoogle();
/// ```
///
/// **Why StateNotifierProvider\<AuthStateNotifier, AuthState\>:**
/// - First type parameter: Notifier class (AuthStateNotifier)
/// - Second type parameter: State class (AuthState)
/// - Riverpod ensures type safety at compile time
///
/// **Auto-dispose:**
/// - Use `.autoDispose` if you want provider to reset when not used
/// - For auth, we DON'T want auto-dispose (global state)
///
/// **Analogia Backend (Spring MVC):**
/// - StateNotifier = @Service (business logic)
/// - AuthState = Session/SecurityContext (current state)
/// - Methods (signInWithGoogle) = @PostMapping("/login")
final authStateNotifierProvider =
    StateNotifierProvider<AuthStateNotifier, AuthState>((ref) {
  // Inject AuthService dependency
  final authService = ref.watch(authServiceProvider);

  // Create and return AuthStateNotifier
  return AuthStateNotifier(authService);
});

// =============================================================================
// Current User Provider (Derived)
// =============================================================================

/// Provider that exposes the current authenticated user.
///
/// **What is a derived provider:**
/// - Depends on another provider (authStateNotifierProvider)
/// - Automatically recomputes when dependency changes
/// - Read-only (cannot modify user directly)
///
/// **Why use derived provider:**
/// - Simplifies UI code (no need to use authState.when() everywhere)
/// - Type-safe (returns User? instead of AuthState)
/// - Explicit dependency (clear that currentUser depends on authState)
///
/// **How it works:**
/// ```dart
/// // Watches authStateNotifierProvider
/// final authState = ref.watch(authStateNotifierProvider);
///
/// // Returns user if authenticated, null otherwise
/// return authState.maybeWhen(
///   authenticated: (user) => user,
///   orElse: () => null,
/// );
/// ```
///
/// **Example usage:**
/// ```dart
/// // In widget
/// final currentUser = ref.watch(currentUserProvider);
///
/// if (currentUser != null) {
///   // User is logged in
///   return Text('Hello, ${currentUser.displayName}');
/// } else {
///   // User is not logged in
///   return LoginButton();
/// }
/// ```
///
/// **When to use currentUserProvider vs authStateNotifierProvider:**
/// - Use currentUserProvider when you just need the User object
/// - Use authStateNotifierProvider when you need to handle loading/initial states
///
/// **Analogia Backend (Spring Security):**
/// ```java
/// // Similar to:
/// @AuthenticationPrincipal User currentUser
/// ```
final currentUserProvider = Provider<User?>((ref) {
  final authState = ref.watch(authStateNotifierProvider);

  return authState.maybeWhen(
    authenticated: (user) => user,
    orElse: () => null,
  );
});

/// HOW TO USE THESE PROVIDERS:
///
/// **1. In main.dart (app startup):**
/// ```dart
/// void main() async {
///   WidgetsFlutterBinding.ensureInitialized();
///
///   final container = ProviderContainer();
///
///   // Check if user is already logged in
///   await container.read(authStateNotifierProvider.notifier).checkAuthStatus();
///
///   runApp(
///     UncontrolledProviderScope(
///       container: container,
///       child: MyApp(),
///     ),
///   );
/// }
/// ```
///
/// **2. In login screen:**
/// ```dart
/// class LoginScreen extends ConsumerWidget {
///   @override
///   Widget build(BuildContext context, WidgetRef ref) {
///     final authState = ref.watch(authStateNotifierProvider);
///
///     return authState.when(
///       loading: () => CircularProgressIndicator(),
///       unauthenticated: () => ElevatedButton(
///         onPressed: () async {
///           try {
///             await ref.read(authStateNotifierProvider.notifier).signInWithGoogle();
///             // Navigate to home
///           } catch (e) {
///             // Show error
///           }
///         },
///         child: Text('Sign in with Google'),
///       ),
///       authenticated: (user) {
///         // Redirect to home (shouldn't reach here in login screen)
///         return Container();
///       },
///       initial: () => SplashScreen(),
///     );
///   }
/// }
/// ```
///
/// **3. In home screen (show user profile):**
/// ```dart
/// class HomeScreen extends ConsumerWidget {
///   @override
///   Widget build(BuildContext context, WidgetRef ref) {
///     final currentUser = ref.watch(currentUserProvider);
///
///     return Scaffold(
///       appBar: AppBar(
///         title: Text('Hello, ${currentUser?.displayName ?? 'Guest'}'),
///       ),
///       body: currentUser != null
///         ? Text('Email: ${currentUser.email}')
///         : LoginButton(),
///     );
///   }
/// }
/// ```
///
/// **4. In settings screen (logout button):**
/// ```dart
/// ElevatedButton(
///   onPressed: () async {
///     await ref.read(authStateNotifierProvider.notifier).signOut();
///     // Navigate to login screen
///   },
///   child: Text('Sign Out'),
/// )
/// ```
///
/// **TESTING (how to mock providers):**
/// ```dart
/// testWidgets('Login screen test', (tester) async {
///   // Create mock
///   final mockAuthService = MockAuthService();
///   when(mockAuthService.signInWithGoogle()).thenAnswer((_) async => mockUser);
///
///   await tester.pumpWidget(
///     ProviderScope(
///       overrides: [
///         // Override provider with mock
///         authServiceProvider.overrideWithValue(mockAuthService),
///       ],
///       child: MyApp(),
///     ),
///   );
///
///   // Test login flow...
/// });
/// ```
