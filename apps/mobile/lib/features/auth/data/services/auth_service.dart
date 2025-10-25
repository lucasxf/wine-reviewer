import '../../domain/models/user.dart';

/// Abstract service contract for authentication operations.
///
/// This interface defines the authentication methods that the app can perform.
/// Using an abstract class allows for:
/// - Easy mocking in unit tests
/// - Dependency injection with Riverpod
/// - Separation of contract (interface) from implementation
///
/// The concrete implementation is [AuthServiceImpl].
abstract class AuthService {
  /// Sign in with Google OAuth.
  ///
  /// Flow:
  /// 1. Triggers Google Sign-In dialog (google_sign_in package)
  /// 2. Obtains Google ID token from user's Google account
  /// 3. Sends ID token to backend API (POST /api/auth/google)
  /// 4. Backend validates token with Google OAuth servers
  /// 5. Backend returns JWT tokens + user data
  /// 6. Stores tokens in secure storage (flutter_secure_storage)
  ///
  /// Returns authenticated [User] on success.
  ///
  /// Throws:
  /// - [NetworkException] if network request fails
  /// - [UnauthorizedException] if Google token is invalid
  /// - [Exception] if user cancels Google Sign-In dialog
  Future<User> signInWithGoogle();

  /// Sign out the current user.
  ///
  /// Flow:
  /// 1. Clears JWT tokens from secure storage
  /// 2. Signs out from Google (google_sign_in package)
  /// 3. Clears any cached user data
  ///
  /// Does not throw exceptions (fails silently).
  Future<void> signOut();

  /// Get the currently authenticated user.
  ///
  /// Flow:
  /// 1. Reads access token from secure storage
  /// 2. If token exists and is valid, returns user data
  /// 3. If token is expired, attempts to refresh using refresh token
  /// 4. If refresh fails, returns null (user must re-authenticate)
  ///
  /// Returns [User] if authenticated, null otherwise.
  ///
  /// This method is called on app startup to check if user is logged in.
  Future<User?> getCurrentUser();

  /// Refresh the access token using the refresh token.
  ///
  /// Flow:
  /// 1. Reads refresh token from secure storage
  /// 2. Sends refresh token to backend (POST /api/auth/refresh)
  /// 3. Backend validates refresh token and issues new access token
  /// 4. Stores new access token in secure storage
  ///
  /// Throws:
  /// - [NetworkException] if network request fails
  /// - [UnauthorizedException] if refresh token is invalid/expired
  Future<void> refreshAccessToken();
}
