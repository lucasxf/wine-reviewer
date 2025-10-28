import 'package:flutter_riverpod/legacy.dart'; // TODO: Migrate to Notifier API (Riverpod 3.0)
import 'package:wine_reviewer_mobile/features/auth/data/services/auth_service.dart';
import 'package:wine_reviewer_mobile/features/auth/domain/models/user.dart';

import 'auth_state.dart';

/// State notifier that manages authentication state.
///
/// This class is responsible for:
/// - Managing authentication state (initial, authenticated, unauthenticated, loading)
/// - Exposing login/logout methods to UI
/// - Handling errors during authentication
///
/// **Why use StateNotifier instead of ChangeNotifier:**
/// - Immutable state (prevents bugs from accidental mutations)
/// - Type-safe state changes (compiler ensures state is valid)
/// - Better testability (easy to test state transitions)
/// - Riverpod integration (compile-time safe)
///
/// **How it works:**
/// 1. UI calls `signInWithGoogle()`
/// 2. StateNotifier sets state to `loading`
/// 3. StateNotifier calls `AuthService.signInWithGoogle()`
/// 4. On success: sets state to `authenticated(user)`
/// 5. On error: sets state to `unauthenticated()` and rethrows error
///
/// **Analogia Backend (Spring MVC):**
/// - Similar to @Service class that calls repository and updates session
/// - StateNotifier = @Service (business logic)
/// - AuthService = Repository (data access)
/// - AuthState = Session/SecurityContext (current state)
///
/// **Example usage in UI:**
/// ```dart
/// // In a widget
/// final authNotifier = ref.read(authStateNotifierProvider.notifier);
///
/// ElevatedButton(
///   onPressed: () async {
///     try {
///       await authNotifier.signInWithGoogle();
///       // Navigate to home screen
///     } catch (e) {
///       // Show error message
///     }
///   },
///   child: Text('Sign in with Google'),
/// )
/// ```
class AuthStateNotifier extends StateNotifier<AuthState> {
  final AuthService _authService;

  /// Constructor with dependency injection.
  ///
  /// Initial state is [AuthState.initial()] - app just started.
  /// The [checkAuthStatus()] method should be called on app startup
  /// to check if user is already logged in.
  AuthStateNotifier(this._authService) : super(const AuthState.initial());

  /// Check if user is already authenticated on app startup.
  ///
  /// This method is called once when the app starts (usually in main.dart).
  ///
  /// Flow:
  /// 1. Calls AuthService.getCurrentUser()
  /// 2. If user exists → sets state to authenticated(user)
  /// 3. If user is null → sets state to unauthenticated()
  ///
  /// This allows auto-login (user doesn't need to login every time).
  Future<void> checkAuthStatus() async {
    // Start in initial state (splash screen can show loading)
    state = const AuthState.initial();

    try {
      final user = await _authService.getCurrentUser();

      if (user != null) {
        // User is logged in
        state = AuthState.authenticated(user);
      } else {
        // User is not logged in
        state = const AuthState.unauthenticated();
      }
    } catch (e) {
      // Error checking auth status → assume user is not logged in
      state = const AuthState.unauthenticated();
    }
  }

  /// Sign in with Google OAuth.
  ///
  /// This method is called when user clicks "Sign in with Google" button.
  ///
  /// Flow:
  /// 1. Sets state to loading (UI shows loading indicator)
  /// 2. Calls AuthService.signInWithGoogle()
  /// 3. On success: sets state to authenticated(user)
  /// 4. On error: sets state to unauthenticated() and rethrows error
  ///
  /// Throws:
  /// - Exception if user cancels Google Sign-In
  /// - NetworkException if network request fails
  ///
  /// UI should catch exceptions and show error messages to user.
  Future<void> signInWithGoogle() async {
    // Set loading state (UI shows loading indicator)
    state = const AuthState.loading();

    try {
      // Call AuthService to perform login
      final user = await _authService.signInWithGoogle();

      // Success → set authenticated state
      state = AuthState.authenticated(user);
    } catch (e) {
      // Error → set unauthenticated state
      state = const AuthState.unauthenticated();

      // Rethrow so UI can catch and show error message
      rethrow;
    }
  }

  /// Sign out the current user.
  ///
  /// This method is called when user clicks "Sign out" button.
  ///
  /// Flow:
  /// 1. Sets state to loading (UI shows loading indicator)
  /// 2. Calls AuthService.signOut() (clears tokens, signs out from Google)
  /// 3. Sets state to unauthenticated()
  ///
  /// This method never throws (sign out always succeeds).
  Future<void> signOut() async {
    // Set loading state (UI shows loading indicator)
    state = const AuthState.loading();

    try {
      // Call AuthService to perform logout
      await _authService.signOut();
    } catch (e) {
      // Ignore errors (sign out should always succeed)
    } finally {
      // Always set unauthenticated state (even if error)
      state = const AuthState.unauthenticated();
    }
  }

  /// Update user data after profile changes.
  ///
  /// This method is called when user updates their profile (name, avatar, etc.).
  ///
  /// **Note:** This only updates the local state.
  /// Backend API call to update profile should be done separately.
  void updateUser(User user) {
    state = AuthState.authenticated(user);
  }
}
