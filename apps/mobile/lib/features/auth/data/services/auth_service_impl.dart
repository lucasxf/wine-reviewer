import 'dart:convert';

import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:google_sign_in/google_sign_in.dart';
import 'package:wine_reviewer_mobile/core/constants/api_constants.dart';
import 'package:wine_reviewer_mobile/core/network/auth_interceptor.dart';
import 'package:wine_reviewer_mobile/core/network/dio_client.dart';
import 'package:wine_reviewer_mobile/core/network/network_exception.dart';
import 'package:wine_reviewer_mobile/core/storage/storage_keys.dart';
import 'package:wine_reviewer_mobile/features/auth/domain/models/auth_response.dart';
import 'package:wine_reviewer_mobile/features/auth/domain/models/google_sign_in_request.dart';
import 'package:wine_reviewer_mobile/features/auth/domain/models/user.dart';

import 'auth_service.dart';

/// Concrete implementation of [AuthService].
///
/// MUDANÇA (2025-10-29): Added FlutterSecureStorage for user data caching
///
/// This class handles all authentication operations including:
/// - Google Sign-In integration
/// - Backend API communication
/// - JWT token management
/// - User data caching (for auto-login)
///
/// **Dependencies (injected via constructor):**
/// - [DioClient] - HTTP client for API requests
/// - [GoogleSignIn] - Google OAuth authentication
/// - [AuthInterceptor] - Manages JWT tokens in secure storage
/// - [FlutterSecureStorage] - Manages user data caching (FIX: Added for auto-login)
///
/// **Why use constructor injection:**
/// - Testability (easy to mock dependencies in unit tests)
/// - Explicit dependencies (clear what this class needs)
/// - Riverpod provides dependencies automatically (Dependency Injection)
class AuthServiceImpl implements AuthService {
  final DioClient _client;
  final GoogleSignIn _googleSignIn;
  final AuthInterceptor _authInterceptor;
  final FlutterSecureStorage _storage; // FIX: Added for user data caching

  /// Constructor with dependency injection.
  ///
  /// All dependencies are required and must be provided by Riverpod providers.
  AuthServiceImpl({
    required DioClient client,
    required GoogleSignIn googleSignIn,
    required AuthInterceptor authInterceptor,
    required FlutterSecureStorage storage, // FIX: Added for user data caching
  })  : _client = client,
        _googleSignIn = googleSignIn,
        _authInterceptor = authInterceptor,
        _storage = storage; // FIX: Initialize storage

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

      // 8. Save user data to secure storage (FIX: Enable auto-login)
      // This allows getCurrentUser() to return user data without calling backend
      // User data is stored as JSON string in encrypted storage
      final userJson = jsonEncode(user.toJson());
      await _storage.write(key: StorageKeys.userCache, value: userJson);

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

      // 2. Clear user data from secure storage (FIX: Complete logout)
      await _storage.delete(key: StorageKeys.userCache);

      // 3. Sign out from Google
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

      // 2. Read cached user data from secure storage (FIX: Enable auto-login)
      // User data was saved during signInWithGoogle() as JSON string
      final userJsonString = await _storage.read(key: StorageKeys.userCache);

      if (userJsonString == null || userJsonString.isEmpty) {
        // Token exists but no cached user data
        // This can happen if user logged in before this fix
        // Clear token and force re-login
        await _authInterceptor.clearToken();
        return null;
      }

      // 3. Deserialize JSON string to User object
      final userMap = jsonDecode(userJsonString) as Map<String, dynamic>;
      final user = User.fromJson(userMap);

      return user;
    } catch (e, stackTrace) {
      // If any error occurs (JSON parsing, storage read, etc.)
      // Log the specific error for debugging
      print('Error in getCurrentUser: $e');
      print('Stack trace: $stackTrace');
      // Clear token and return null (force re-login)
      try {
        await _authInterceptor.clearToken();
        await _storage.delete(key: StorageKeys.userCache);
      } catch (_) {
        // Ignore cleanup errors
      }
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
