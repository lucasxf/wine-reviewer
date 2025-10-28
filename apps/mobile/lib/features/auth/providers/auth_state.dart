import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:wine_reviewer_mobile/features/auth/domain/models/user.dart';

part 'auth_state.freezed.dart';

/// Authentication state managed by [AuthStateNotifier].
///
/// This is a union type with 4 possible states:
/// - [AuthState.initial] - App just started, checking if user is logged in
/// - [AuthState.authenticated] - User is logged in
/// - [AuthState.unauthenticated] - User is not logged in
/// - [AuthState.loading] - Authentication operation in progress (login/logout)
///
/// **Why use freezed union types:**
/// - Type-safe state handling (compiler ensures all cases are handled)
/// - Immutable state (prevents accidental mutations)
/// - Pattern matching with `when()` method
/// - No need for nullable fields or boolean flags
///
/// **Example usage in UI:**
/// ```dart
/// final authState = ref.watch(authStateNotifierProvider);
///
/// authState.when(
///   initial: () => SplashScreen(),
///   authenticated: (user) => HomeScreen(user: user),
///   unauthenticated: () => LoginScreen(),
///   loading: () => LoadingIndicator(),
/// );
/// ```
///
/// **Analogia Backend (Spring Security):**
/// - similar to SecurityContext.isAuthenticated()
/// - similar to Authentication.getPrincipal()
@freezed
sealed class AuthState with _$AuthState {
  /// Initial state when app starts.
  ///
  /// Use this state to show splash screen while checking if user is logged in.
  const factory AuthState.initial() = AuthStateInitial;

  /// User is authenticated.
  ///
  /// Contains the current [User] data.
  /// This is the "happy path" state where user can access protected features.
  const factory AuthState.authenticated(User user) = AuthStateAuthenticated;

  /// User is not authenticated.
  ///
  /// User should be redirected to login screen.
  const factory AuthState.unauthenticated() = AuthStateUnauthenticated;

  /// Authentication operation in progress.
  ///
  /// Shows during login or logout operations.
  /// UI should display loading indicator and disable user interactions.
  const factory AuthState.loading() = AuthStateLoading;
}
