import 'package:freezed_annotation/freezed_annotation.dart';

part 'auth_response.freezed.dart';
part 'auth_response.g.dart';

/// Response from the backend authentication endpoint (POST /api/auth/google).
///
/// Contains the JWT access token and user data.
/// This model matches the backend API response structure (flat, no nested user object).
///
/// Backend response:
/// ```json
/// {
///   "token": "eyJhbGciOiJIUzUxMiJ9...",
///   "userId": "uuid",
///   "email": "user@example.com",
///   "displayName": "João Silva",
///   "avatarUrl": "https://..."
/// }
/// ```
@freezed
class AuthResponse with _$AuthResponse {
  const factory AuthResponse({
    required String token,
    required String userId,
    required String email,
    required String displayName,
    String? avatarUrl,
  }) = _AuthResponse;

  factory AuthResponse.fromJson(Map<String, dynamic> json) =>
      _$AuthResponseFromJson(json);
}
