import 'package:google_sign_in/google_sign_in.dart';
import 'package:wine_reviewer_mobile/core/constants/api_constants.dart';
import 'package:wine_reviewer_mobile/core/network/auth_interceptor.dart';
import 'package:wine_reviewer_mobile/core/network/dio_client.dart';
import 'package:wine_reviewer_mobile/core/network/network_exception.dart';
import 'package:wine_reviewer_mobile/features/auth/domain/models/auth_response.dart';
import 'package:wine_reviewer_mobile/features/auth/domain/models/google_sign_in_request.dart';
import 'package:wine_reviewer_mobile/features/auth/domain/models/user.dart';

import 'auth_service.dart';

/// Concrete implementation of [AuthService].
///
/// This class handles all authentication operations including:
/// - Google Sign-In integration
/// - Backend API communication
/// - JWT token management
///
/// **Dependencies (injected via constructor):**
/// - [DioClient] - HTTP client for API requests
/// - [GoogleSignIn] - Google OAuth authentication
/// - [AuthInterceptor] - Manages JWT tokens in secure storage
///
/// **Why use constructor injection:**
/// - Testability (easy to mock dependencies in unit tests)
/// - Explicit dependencies (clear what this class needs)
/// - Riverpod provides dependencies automatically (Dependency Injection)
class AuthServiceImpl implements AuthService {
  final DioClient _client;
  final GoogleSignIn _googleSignIn;
  final AuthInterceptor _authInterceptor;

  /// Constructor with dependency injection.
  ///
  /// All dependencies are required and must be provided by Riverpod providers.
  AuthServiceImpl({
    required DioClient client,
    required GoogleSignIn googleSignIn,
    required AuthInterceptor authInterceptor,
  })  : _client = client,
        _googleSignIn = googleSignIn,
        _authInterceptor = authInterceptor;

  @override
  Future<User> signInWithGoogle() async {
    try {
      // 1. Trigger Google Sign-In dialog
      // This opens a popup/bottom sheet where user selects Google account
      final googleAccount = await _googleSignIn.signIn();

      if (googleAccount == null) {
        // User canceled the sign-in dialog
        throw Exception('Google Sign-In cancelado pelo usuário');
      }

      // 2. Get Google authentication details (includes ID token)
      final googleAuth = await googleAccount.authentication;

      if (googleAuth.idToken == null) {
        throw Exception('Google ID token não foi retornado');
      }

      // 3. Create request payload with Google ID token
      final request = GoogleSignInRequest(idToken: googleAuth.idToken!);

      // 4. Send Google ID token to backend for validation
      // Backend will:
      // - Validate token with Google OAuth servers
      // - Create/update user in database
      // - Generate JWT access token
      // - Return JWT + user data
      final response = await _client.post(
        ApiConstants.googleAuth,
        data: request.toJson(),
      );

      // 5. Parse response into AuthResponse model
      final authResponse = AuthResponse.fromJson(
        response.data as Map<String, dynamic>,
      );

      // 6. Save JWT token to secure storage
      // This token will be automatically added to all future requests by AuthInterceptor
      await _authInterceptor.saveToken(authResponse.token);

      // 7. Convert AuthResponse to User domain model
      // AuthResponse is a DTO (Data Transfer Object) - used only for API communication
      // User is a domain model - used throughout the app
      final user = User(
        id: authResponse.userId,
        email: authResponse.email,
        displayName: authResponse.displayName,
        avatarUrl: authResponse.avatarUrl,
        // googleId is not returned by backend, so we don't set it here
        googleId: null,
      );

      return user;
    } on NetworkException {
      // NetworkException is already thrown by DioClient
      rethrow;
    } catch (e) {
      // Catch any other errors (Google Sign-In errors, parsing errors, etc.)
      throw Exception('Erro ao fazer login com Google: $e');
    }
  }

  @override
  Future<void> signOut() async {
    try {
      // 1. Clear JWT token from secure storage
      await _authInterceptor.clearToken();

      // 2. Sign out from Google
      // This ensures user has to select account again next time
      await _googleSignIn.signOut();
    } catch (e) {
      // Sign out should never fail loudly
      // Just ignore errors silently
      // TODO: Use proper logging framework in production (e.g., logger package)
    }
  }

  @override
  Future<User?> getCurrentUser() async {
    try {
      // 1. Check if JWT token exists in secure storage
      final hasToken = await _authInterceptor.hasToken();

      if (!hasToken) {
        // No token = user is not authenticated
        return null;
      }

      // 2. TODO: Validate token with backend
      // For now, we just check if token exists
      // In the future, we should:
      // - Call backend endpoint to get current user data
      // - Or decode JWT locally to extract user ID and fetch user data
      // - Or store user data locally (SharedPreferences/Hive) along with token

      // 3. For now, return null (not implemented yet)
      // This will be implemented in a future task
      return null;
    } catch (e) {
      return null;
    }
  }

  @override
  Future<void> refreshAccessToken() async {
    // TODO: Implement refresh token logic
    // Backend doesn't have refresh token endpoint yet
    // When implemented, this should:
    // 1. Read refresh token from secure storage
    // 2. Send refresh token to backend (POST /api/auth/refresh)
    // 3. Backend validates refresh token and issues new access token
    // 4. Save new access token to secure storage
    throw UnimplementedError(
      'Refresh token não implementado. Backend precisa adicionar endpoint /api/auth/refresh',
    );
  }
}
