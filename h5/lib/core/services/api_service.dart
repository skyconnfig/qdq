import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// API 配置
class ApiConfig {
  static const String baseUrl = 'http://localhost:8080/api';
  static const int connectTimeout = 30000;
  static const int receiveTimeout = 30000;
}

/// 统一响应结构
class ApiResponse<T> {
  final int code;
  final String message;
  final T? data;

  ApiResponse({
    required this.code,
    required this.message,
    this.data,
  });

  bool get isSuccess => code == 0;

  factory ApiResponse.fromJson(
    Map<String, dynamic> json,
    T Function(dynamic)? fromJsonT,
  ) {
    return ApiResponse(
      code: json['code'] ?? -1,
      message: json['message'] ?? '',
      data: json['data'] != null && fromJsonT != null
          ? fromJsonT(json['data'])
          : json['data'],
    );
  }
}

/// API 服务
class ApiService {
  late Dio _dio;
  String? _token;

  ApiService() {
    _dio = Dio(BaseOptions(
      baseUrl: ApiConfig.baseUrl,
      connectTimeout: const Duration(milliseconds: ApiConfig.connectTimeout),
      receiveTimeout: const Duration(milliseconds: ApiConfig.receiveTimeout),
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
      },
    ));

    _setupInterceptors();
    _loadToken();
  }

  /// 加载 token
  Future<void> _loadToken() async {
    final prefs = await SharedPreferences.getInstance();
    _token = prefs.getString('token');
    if (_token != null) {
      _dio.options.headers['Authorization'] = 'Bearer $_token';
    }
  }

  /// 设置 token
  Future<void> setToken(String token) async {
    _token = token;
    _dio.options.headers['Authorization'] = 'Bearer $token';
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('token', token);
  }

  /// 清除 token
  Future<void> clearToken() async {
    _token = null;
    _dio.options.headers.remove('Authorization');
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove('token');
  }

  /// 获取 token
  String? get token => _token;

  /// 设置拦截器
  void _setupInterceptors() {
    _dio.interceptors.add(InterceptorsWrapper(
      onRequest: (options, handler) {
        print('请求: ${options.method} ${options.uri}');
        return handler.next(options);
      },
      onResponse: (response, handler) {
        print('响应: ${response.statusCode} ${response.requestOptions.uri}');
        return handler.next(response);
      },
      onError: (error, handler) {
        print('错误: ${error.message}');
        return handler.next(error);
      },
    ));
  }

  /// GET 请求
  Future<ApiResponse<T>> get<T>(
    String path, {
    Map<String, dynamic>? queryParameters,
    T Function(dynamic)? fromJsonT,
  }) async {
    try {
      final response = await _dio.get(
        path,
        queryParameters: queryParameters,
      );
      return ApiResponse.fromJson(response.data, fromJsonT);
    } on DioException catch (e) {
      return _handleError(e);
    }
  }

  /// POST 请求
  Future<ApiResponse<T>> post<T>(
    String path, {
    dynamic data,
    Map<String, dynamic>? queryParameters,
    T Function(dynamic)? fromJsonT,
  }) async {
    try {
      final response = await _dio.post(
        path,
        data: data,
        queryParameters: queryParameters,
      );
      return ApiResponse.fromJson(response.data, fromJsonT);
    } on DioException catch (e) {
      return _handleError(e);
    }
  }

  /// PUT 请求
  Future<ApiResponse<T>> put<T>(
    String path, {
    dynamic data,
    Map<String, dynamic>? queryParameters,
    T Function(dynamic)? fromJsonT,
  }) async {
    try {
      final response = await _dio.put(
        path,
        data: data,
        queryParameters: queryParameters,
      );
      return ApiResponse.fromJson(response.data, fromJsonT);
    } on DioException catch (e) {
      return _handleError(e);
    }
  }

  /// DELETE 请求
  Future<ApiResponse<T>> delete<T>(
    String path, {
    dynamic data,
    Map<String, dynamic>? queryParameters,
    T Function(dynamic)? fromJsonT,
  }) async {
    try {
      final response = await _dio.delete(
        path,
        data: data,
        queryParameters: queryParameters,
      );
      return ApiResponse.fromJson(response.data, fromJsonT);
    } on DioException catch (e) {
      return _handleError(e);
    }
  }

  /// 处理错误
  ApiResponse<T> _handleError<T>(DioException e) {
    String message;
    switch (e.type) {
      case DioExceptionType.connectionTimeout:
        message = '连接超时，请检查网络';
        break;
      case DioExceptionType.sendTimeout:
        message = '发送请求超时';
        break;
      case DioExceptionType.receiveTimeout:
        message = '接收响应超时';
        break;
      case DioExceptionType.badResponse:
        final statusCode = e.response?.statusCode;
        if (statusCode == 401) {
          message = '登录已过期，请重新登录';
        } else if (statusCode == 403) {
          message = '没有权限访问';
        } else if (statusCode == 404) {
          message = '请求的资源不存在';
        } else if (statusCode == 500) {
          message = '服务器内部错误';
        } else {
          message = '请求失败: $statusCode';
        }
        break;
      case DioExceptionType.cancel:
        message = '请求已取消';
        break;
      case DioExceptionType.connectionError:
        message = '网络连接失败，请检查网络';
        break;
      default:
        message = '网络错误: ${e.message}';
    }
    return ApiResponse(code: -1, message: message);
  }

  // ==================== 认证接口 ====================

  /// 登录
  Future<ApiResponse<Map<String, dynamic>>> login(
    String username,
    String password,
  ) async {
    final response = await post<Map<String, dynamic>>(
      '/auth/login',
      data: {'username': username, 'password': password},
      fromJsonT: (data) => data as Map<String, dynamic>,
    );
    if (response.isSuccess && response.data != null) {
      final token = response.data!['token'];
      if (token != null) {
        await setToken(token);
      }
    }
    return response;
  }

  /// 短信登录
  Future<ApiResponse<Map<String, dynamic>>> loginBySms(
    String phone,
    String code,
  ) async {
    final response = await post<Map<String, dynamic>>(
      '/auth/login/sms',
      data: {'phone': phone, 'code': code},
      fromJsonT: (data) => data as Map<String, dynamic>,
    );
    if (response.isSuccess && response.data != null) {
      final token = response.data!['token'];
      if (token != null) {
        await setToken(token);
      }
    }
    return response;
  }

  /// 发送验证码
  Future<ApiResponse<void>> sendSmsCode(String phone) async {
    return post('/auth/sms/send', data: {'phone': phone});
  }

  /// 登出
  Future<ApiResponse<void>> logout() async {
    final response = await post<void>('/auth/logout');
    await clearToken();
    return response;
  }

  /// 获取当前用户信息
  Future<ApiResponse<Map<String, dynamic>>> getCurrentUser() async {
    return get<Map<String, dynamic>>(
      '/auth/me',
      fromJsonT: (data) => data as Map<String, dynamic>,
    );
  }

  // ==================== 场次接口 ====================

  /// 获取场次列表
  Future<ApiResponse<List<dynamic>>> getSessions({
    int page = 1,
    int size = 20,
    String? status,
  }) async {
    return get<List<dynamic>>(
      '/sessions',
      queryParameters: {
        'page': page,
        'size': size,
        if (status != null) 'status': status,
      },
      fromJsonT: (data) => data as List<dynamic>,
    );
  }

  /// 获取场次详情
  Future<ApiResponse<Map<String, dynamic>>> getSession(int id) async {
    return get<Map<String, dynamic>>(
      '/sessions/$id',
      fromJsonT: (data) => data as Map<String, dynamic>,
    );
  }

  /// 加入场次
  Future<ApiResponse<Map<String, dynamic>>> joinSession(
    int sessionId,
    int teamId,
  ) async {
    return post<Map<String, dynamic>>(
      '/sessions/$sessionId/join',
      data: {'teamId': teamId},
      fromJsonT: (data) => data as Map<String, dynamic>,
    );
  }

  // ==================== 抢答接口 ====================

  /// 抢答
  Future<ApiResponse<Map<String, dynamic>>> buzz({
    required int sessionId,
    required int questionId,
    required int teamId,
  }) async {
    return post<Map<String, dynamic>>(
      '/buzz',
      data: {
        'sessionId': sessionId,
        'questionId': questionId,
        'teamId': teamId,
      },
      fromJsonT: (data) => data as Map<String, dynamic>,
    );
  }

  /// 获取抢答结果
  Future<ApiResponse<Map<String, dynamic>>> getBuzzResult(
    int sessionId,
    int questionId,
  ) async {
    return get<Map<String, dynamic>>(
      '/buzz/$sessionId/$questionId',
      fromJsonT: (data) => data as Map<String, dynamic>,
    );
  }

  // ==================== 答题接口 ====================

  /// 提交答案
  Future<ApiResponse<Map<String, dynamic>>> submitAnswer({
    required int sessionId,
    required int questionId,
    required int teamId,
    required dynamic answer,
  }) async {
    return post<Map<String, dynamic>>(
      '/answers',
      data: {
        'sessionId': sessionId,
        'questionId': questionId,
        'teamId': teamId,
        'answer': answer,
      },
      fromJsonT: (data) => data as Map<String, dynamic>,
    );
  }

  /// 获取答题结果
  Future<ApiResponse<Map<String, dynamic>>> getAnswerResult(
    int sessionId,
    int questionId,
    int teamId,
  ) async {
    return get<Map<String, dynamic>>(
      '/answers/$sessionId/$questionId/$teamId',
      fromJsonT: (data) => data as Map<String, dynamic>,
    );
  }

  // ==================== 队伍接口 ====================

  /// 获取我的队伍信息
  Future<ApiResponse<Map<String, dynamic>>> getMyTeam(int sessionId) async {
    return get<Map<String, dynamic>>(
      '/sessions/$sessionId/my-team',
      fromJsonT: (data) => data as Map<String, dynamic>,
    );
  }

  /// 获取排行榜
  Future<ApiResponse<List<dynamic>>> getLeaderboard(int sessionId) async {
    return get<List<dynamic>>(
      '/sessions/$sessionId/leaderboard',
      fromJsonT: (data) => data as List<dynamic>,
    );
  }
}

/// API 服务 Provider
final apiServiceProvider = Provider<ApiService>((ref) {
  return ApiService();
});
