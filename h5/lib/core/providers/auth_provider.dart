import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// 认证状态
class AuthState {
  final bool isLoggedIn;
  final String? token;
  final Map<String, dynamic>? userInfo;

  const AuthState({
    this.isLoggedIn = false,
    this.token,
    this.userInfo,
  });

  AuthState copyWith({
    bool? isLoggedIn,
    String? token,
    Map<String, dynamic>? userInfo,
  }) {
    return AuthState(
      isLoggedIn: isLoggedIn ?? this.isLoggedIn,
      token: token ?? this.token,
      userInfo: userInfo ?? this.userInfo,
    );
  }
}

/// 认证状态Notifier
class AuthNotifier extends StateNotifier<AuthState> {
  AuthNotifier() : super(const AuthState()) {
    _loadFromStorage();
  }

  /// 从本地存储加载登录状态
  Future<void> _loadFromStorage() async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('token');
    if (token != null && token.isNotEmpty) {
      state = state.copyWith(isLoggedIn: true, token: token);
    }
  }

  /// 登录
  Future<void> login(String token, Map<String, dynamic> userInfo) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('token', token);
    
    state = AuthState(
      isLoggedIn: true,
      token: token,
      userInfo: userInfo,
    );
  }

  /// 登出
  Future<void> logout() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove('token');
    
    state = const AuthState();
  }

  /// 更新用户信息
  void updateUserInfo(Map<String, dynamic> userInfo) {
    state = state.copyWith(userInfo: userInfo);
  }
}

/// 认证状态Provider
final authStateProvider = StateNotifierProvider<AuthNotifier, AuthState>((ref) {
  return AuthNotifier();
});

/// Token Provider
final tokenProvider = Provider<String?>((ref) {
  return ref.watch(authStateProvider).token;
});

/// 是否登录Provider
final isLoggedInProvider = Provider<bool>((ref) {
  return ref.watch(authStateProvider).isLoggedIn;
});
