import 'package:freezed_annotation/freezed_annotation.dart';

part 'google_sign_in_request.freezed.dart';
part 'google_sign_in_request.g.dart';

/// Request payload sent to the backend for Google Sign-In authentication.
///
/// Contains the Google ID token obtained from google_sign_in package.
/// The backend will validate this token with Google OAuth servers.
@freezed
class GoogleSignInRequest with _$GoogleSignInRequest {
  const factory GoogleSignInRequest({
    required String idToken,
  }) = _GoogleSignInRequest;

  factory GoogleSignInRequest.fromJson(Map<String, dynamic> json) =>
      _$GoogleSignInRequestFromJson(json);

  Map<String, dynamic> toJson() => {'idToken': idToken};
}
