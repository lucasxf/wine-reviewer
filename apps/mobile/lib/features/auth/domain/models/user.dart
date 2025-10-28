import 'package:freezed_annotation/freezed_annotation.dart';

part 'user.freezed.dart';
part 'user.g.dart';

/// Domain model representing an authenticated user.
///
/// This model is immutable (freezed) and serializable (json_serializable).
/// It represents the user data returned from the backend API.
@freezed
abstract class User with _$User {
  const factory User({
    required String id,
    required String email,
    required String displayName,
    String? avatarUrl,
    String? googleId,
  }) = _User;

  factory User.fromJson(Map<String, dynamic> json) => _$UserFromJson(json);
}
