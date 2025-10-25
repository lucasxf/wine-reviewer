// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'google_sign_in_request.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
  'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models',
);

GoogleSignInRequest _$GoogleSignInRequestFromJson(Map<String, dynamic> json) {
  return _GoogleSignInRequest.fromJson(json);
}

/// @nodoc
mixin _$GoogleSignInRequest {
  String get idToken => throw _privateConstructorUsedError;

  /// Serializes this GoogleSignInRequest to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of GoogleSignInRequest
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $GoogleSignInRequestCopyWith<GoogleSignInRequest> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $GoogleSignInRequestCopyWith<$Res> {
  factory $GoogleSignInRequestCopyWith(
    GoogleSignInRequest value,
    $Res Function(GoogleSignInRequest) then,
  ) = _$GoogleSignInRequestCopyWithImpl<$Res, GoogleSignInRequest>;
  @useResult
  $Res call({String idToken});
}

/// @nodoc
class _$GoogleSignInRequestCopyWithImpl<$Res, $Val extends GoogleSignInRequest>
    implements $GoogleSignInRequestCopyWith<$Res> {
  _$GoogleSignInRequestCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of GoogleSignInRequest
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({Object? idToken = null}) {
    return _then(
      _value.copyWith(
            idToken: null == idToken
                ? _value.idToken
                : idToken // ignore: cast_nullable_to_non_nullable
                      as String,
          )
          as $Val,
    );
  }
}

/// @nodoc
abstract class _$$GoogleSignInRequestImplCopyWith<$Res>
    implements $GoogleSignInRequestCopyWith<$Res> {
  factory _$$GoogleSignInRequestImplCopyWith(
    _$GoogleSignInRequestImpl value,
    $Res Function(_$GoogleSignInRequestImpl) then,
  ) = __$$GoogleSignInRequestImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String idToken});
}

/// @nodoc
class __$$GoogleSignInRequestImplCopyWithImpl<$Res>
    extends _$GoogleSignInRequestCopyWithImpl<$Res, _$GoogleSignInRequestImpl>
    implements _$$GoogleSignInRequestImplCopyWith<$Res> {
  __$$GoogleSignInRequestImplCopyWithImpl(
    _$GoogleSignInRequestImpl _value,
    $Res Function(_$GoogleSignInRequestImpl) _then,
  ) : super(_value, _then);

  /// Create a copy of GoogleSignInRequest
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({Object? idToken = null}) {
    return _then(
      _$GoogleSignInRequestImpl(
        idToken: null == idToken
            ? _value.idToken
            : idToken // ignore: cast_nullable_to_non_nullable
                  as String,
      ),
    );
  }
}

/// @nodoc
@JsonSerializable()
class _$GoogleSignInRequestImpl implements _GoogleSignInRequest {
  const _$GoogleSignInRequestImpl({required this.idToken});

  factory _$GoogleSignInRequestImpl.fromJson(Map<String, dynamic> json) =>
      _$$GoogleSignInRequestImplFromJson(json);

  @override
  final String idToken;

  @override
  String toString() {
    return 'GoogleSignInRequest(idToken: $idToken)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$GoogleSignInRequestImpl &&
            (identical(other.idToken, idToken) || other.idToken == idToken));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, idToken);

  /// Create a copy of GoogleSignInRequest
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$GoogleSignInRequestImplCopyWith<_$GoogleSignInRequestImpl> get copyWith =>
      __$$GoogleSignInRequestImplCopyWithImpl<_$GoogleSignInRequestImpl>(
        this,
        _$identity,
      );

  @override
  Map<String, dynamic> toJson() {
    return _$$GoogleSignInRequestImplToJson(this);
  }
}

abstract class _GoogleSignInRequest implements GoogleSignInRequest {
  const factory _GoogleSignInRequest({required final String idToken}) =
      _$GoogleSignInRequestImpl;

  factory _GoogleSignInRequest.fromJson(Map<String, dynamic> json) =
      _$GoogleSignInRequestImpl.fromJson;

  @override
  String get idToken;

  /// Create a copy of GoogleSignInRequest
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$GoogleSignInRequestImplCopyWith<_$GoogleSignInRequestImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
