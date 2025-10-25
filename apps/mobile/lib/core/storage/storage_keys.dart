/// Storage keys for flutter_secure_storage.
///
/// **Why use constants:**
/// - Prevents typos (compile-time errors instead of runtime bugs)
/// - Centralized key management (easy to find all keys)
/// - Easy to refactor (change key name in one place)
/// - Self-documenting (comments explain what each key stores)
///
/// **Example usage:**
/// ```dart
/// // Write
/// await storage.write(key: StorageKeys.authToken, value: jwtToken);
///
/// // Read
/// final token = await storage.read(key: StorageKeys.authToken);
///
/// // Delete
/// await storage.delete(key: StorageKeys.authToken);
/// ```
///
/// **IMPORTANT - Security:**
/// - These keys are used with flutter_secure_storage (encrypted storage)
/// - Never use these keys with SharedPreferences (plain text)
/// - Add new keys here when storing sensitive data
///
/// **Naming Convention:**
/// - Prefix with domain: `auth_`, `user_`, `settings_`
/// - Use snake_case: `auth_jwt_token`, `user_profile_cache`
/// - Descriptive names: `auth_token` (bad) → `auth_jwt_token` (good)
class StorageKeys {
  // Private constructor - prevents instantiation
  StorageKeys._();

  // =========================================================================
  // Authentication Keys
  // =========================================================================

  /// JWT access token received from backend after login.
  ///
  /// **Format:** JWT string (e.g., "eyJhbGciOiJIUzUxMiJ9...")
  /// **Expires:** Typically 15 minutes (backend configured)
  /// **When to use:**
  /// - Save after successful login (AuthServiceImpl)
  /// - Read on every HTTP request (AuthInterceptor)
  /// - Delete on logout (AuthServiceImpl)
  ///
  /// **Security:**
  /// - Stored encrypted (flutter_secure_storage)
  /// - Automatically added to HTTP headers by AuthInterceptor
  /// - Cleared on logout
  static const String authToken = 'auth_jwt_token';

  /// Refresh token for renewing expired access tokens.
  ///
  /// **Format:** String (backend-defined format)
  /// **Expires:** Typically 7-30 days (backend configured)
  /// **When to use:**
  /// - Save after successful login (AuthServiceImpl)
  /// - Use when access token expires (AuthService.refreshAccessToken)
  /// - Delete on logout (AuthServiceImpl)
  ///
  /// **Status:** NOT IMPLEMENTED YET (backend needs /api/auth/refresh endpoint)
  ///
  /// **Future implementation:**
  /// ```dart
  /// // After login
  /// await storage.write(key: StorageKeys.refreshToken, value: refreshToken);
  ///
  /// // When access token expires (401 error)
  /// final refreshToken = await storage.read(key: StorageKeys.refreshToken);
  /// final newAccessToken = await authService.refreshAccessToken(refreshToken);
  /// await storage.write(key: StorageKeys.authToken, value: newAccessToken);
  /// ```
  static const String refreshToken = 'auth_refresh_token';

  // =========================================================================
  // User Data Keys (Optional - for caching)
  // =========================================================================

  /// Cached user data to avoid unnecessary API calls.
  ///
  /// **Format:** JSON string of User object
  /// **When to use:**
  /// - Save after successful login (optional optimization)
  /// - Read on app startup (fast auto-login)
  /// - Delete on logout or profile update
  ///
  /// **Example:**
  /// ```dart
  /// // Save
  /// final userJson = jsonEncode(user.toJson());
  /// await storage.write(key: StorageKeys.userCache, value: userJson);
  ///
  /// // Read
  /// final userJsonString = await storage.read(key: StorageKeys.userCache);
  /// if (userJsonString != null) {
  ///   final userMap = jsonDecode(userJsonString);
  ///   final user = User.fromJson(userMap);
  /// }
  /// ```
  ///
  /// **Trade-offs:**
  /// - ✅ Faster app startup (no API call needed)
  /// - ✅ Works offline (user can see profile)
  /// - ❌ Can be outdated (if profile changed on another device)
  /// - ❌ Adds complexity (need to sync with backend)
  ///
  /// **Recommendation:** Only implement if app startup is slow
  static const String userCache = 'user_profile_cache';

  // =========================================================================
  // App Settings Keys (Optional - for sensitive preferences)
  // =========================================================================

  /// User ID of the currently logged-in user.
  ///
  /// **Format:** UUID string
  /// **When to use:**
  /// - Save after successful login
  /// - Read when needed (e.g., analytics, logging)
  /// - Delete on logout
  ///
  /// **Note:** This is redundant if using userCache (User already has ID)
  /// Only use if you need userId without loading full User object.
  static const String userId = 'user_id';

  // =========================================================================
  // Helper Methods (Optional)
  // =========================================================================

  /// Returns all authentication-related keys.
  ///
  /// **Use case:** Delete all auth data on logout
  ///
  /// ```dart
  /// for (final key in StorageKeys.authKeys) {
  ///   await storage.delete(key: key);
  /// }
  /// ```
  static List<String> get authKeys => [
        authToken,
        refreshToken,
        userCache,
        userId,
      ];

  /// Returns all keys used in the app.
  ///
  /// **Use case:** Debug/testing (list all stored data)
  ///
  /// ```dart
  /// for (final key in StorageKeys.allKeys) {
  ///   final value = await storage.read(key: key);
  ///   print('$key: $value');
  /// }
  /// ```
  static List<String> get allKeys => [
        ...authKeys,
        // Add other categories here (settings, cache, etc.)
      ];
}
