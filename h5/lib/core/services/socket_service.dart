import 'dart:async';
import 'dart:convert';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:socket_io_client/socket_io_client.dart' as IO;
import '../providers/auth_provider.dart';

/// Socket.IO 服务配置
class SocketConfig {
  static const String baseUrl = 'http://localhost:8080';
  static const String namespace = '/ws/quiz';
  static const int reconnectAttempts = 5;
  static const int reconnectDelay = 3000;
  static const int pingInterval = 25000;
  static const int pingTimeout = 10000;
}

/// WebSocket 连接状态
enum SocketState {
  disconnected,
  connecting,
  connected,
  reconnecting,
  error,
}

/// Socket 事件类型
class SocketEvents {
  // 客户端 -> 服务端
  static const String joinSession = 'join_session';
  static const String clientBuzz = 'client_buzz';
  static const String submitAnswer = 'submit_answer';
  static const String ping = 'ping';
  
  // 服务端 -> 客户端
  static const String sessionState = 'session_state';
  static const String questionPush = 'question_push';
  static const String buzzResult = 'buzz_result';
  static const String scoreUpdate = 'score_update';
  static const String answerResult = 'answer_result';
  static const String timerSync = 'timer_sync';
  static const String error = 'error';
}

/// Socket 服务
class SocketService {
  IO.Socket? _socket;
  SocketState _state = SocketState.disconnected;
  int _reconnectAttempts = 0;
  Timer? _heartbeatTimer;
  
  // 事件流控制器
  final _stateController = StreamController<SocketState>.broadcast();
  final _sessionStateController = StreamController<Map<String, dynamic>>.broadcast();
  final _questionController = StreamController<Map<String, dynamic>>.broadcast();
  final _buzzResultController = StreamController<Map<String, dynamic>>.broadcast();
  final _scoreUpdateController = StreamController<Map<String, dynamic>>.broadcast();
  final _answerResultController = StreamController<Map<String, dynamic>>.broadcast();
  final _timerController = StreamController<Map<String, dynamic>>.broadcast();
  final _errorController = StreamController<String>.broadcast();

  // 流访问器
  Stream<SocketState> get stateStream => _stateController.stream;
  Stream<Map<String, dynamic>> get sessionStateStream => _sessionStateController.stream;
  Stream<Map<String, dynamic>> get questionStream => _questionController.stream;
  Stream<Map<String, dynamic>> get buzzResultStream => _buzzResultController.stream;
  Stream<Map<String, dynamic>> get scoreUpdateStream => _scoreUpdateController.stream;
  Stream<Map<String, dynamic>> get answerResultStream => _answerResultController.stream;
  Stream<Map<String, dynamic>> get timerStream => _timerController.stream;
  Stream<String> get errorStream => _errorController.stream;
  
  SocketState get state => _state;
  bool get isConnected => _state == SocketState.connected;

  /// 连接到 WebSocket 服务器
  void connect(String token) {
    if (_socket != null) {
      disconnect();
    }
    
    _updateState(SocketState.connecting);
    
    _socket = IO.io(
      '${SocketConfig.baseUrl}${SocketConfig.namespace}',
      IO.OptionBuilder()
        .setTransports(['websocket'])
        .setExtraHeaders({'Authorization': 'Bearer $token'})
        .setQuery({'token': token})
        .enableAutoConnect()
        .enableReconnection()
        .setReconnectionAttempts(SocketConfig.reconnectAttempts)
        .setReconnectionDelay(SocketConfig.reconnectDelay)
        .build(),
    );
    
    _setupEventHandlers();
    _socket!.connect();
  }

  /// 设置事件处理器
  void _setupEventHandlers() {
    _socket!.onConnect((_) {
      _reconnectAttempts = 0;
      _updateState(SocketState.connected);
      _startHeartbeat();
      print('Socket.IO 已连接');
    });
    
    _socket!.onDisconnect((_) {
      _updateState(SocketState.disconnected);
      _stopHeartbeat();
      print('Socket.IO 已断开');
    });
    
    _socket!.onConnectError((error) {
      _updateState(SocketState.error);
      _errorController.add('连接错误: $error');
      print('Socket.IO 连接错误: $error');
    });
    
    _socket!.onError((error) {
      _errorController.add('Socket错误: $error');
      print('Socket.IO 错误: $error');
    });
    
    _socket!.onReconnect((_) {
      _updateState(SocketState.connected);
      print('Socket.IO 重连成功');
    });
    
    _socket!.onReconnecting((_) {
      _reconnectAttempts++;
      _updateState(SocketState.reconnecting);
      print('Socket.IO 正在重连... 第$_reconnectAttempts次');
    });
    
    _socket!.onReconnectError((error) {
      _errorController.add('重连错误: $error');
    });
    
    _socket!.onReconnectFailed((_) {
      _updateState(SocketState.error);
      _errorController.add('重连失败，请检查网络');
    });
    
    // 业务事件监听
    _socket!.on(SocketEvents.sessionState, (data) {
      _sessionStateController.add(_parseData(data));
    });
    
    _socket!.on(SocketEvents.questionPush, (data) {
      _questionController.add(_parseData(data));
    });
    
    _socket!.on(SocketEvents.buzzResult, (data) {
      _buzzResultController.add(_parseData(data));
    });
    
    _socket!.on(SocketEvents.scoreUpdate, (data) {
      _scoreUpdateController.add(_parseData(data));
    });
    
    _socket!.on(SocketEvents.answerResult, (data) {
      _answerResultController.add(_parseData(data));
    });
    
    _socket!.on(SocketEvents.timerSync, (data) {
      _timerController.add(_parseData(data));
    });
    
    _socket!.on(SocketEvents.error, (data) {
      final errorData = _parseData(data);
      _errorController.add(errorData['message'] ?? '未知错误');
    });
  }

  /// 解析数据
  Map<String, dynamic> _parseData(dynamic data) {
    if (data is Map<String, dynamic>) {
      return data;
    } else if (data is String) {
      return json.decode(data);
    }
    return {'data': data};
  }

  /// 更新状态
  void _updateState(SocketState newState) {
    _state = newState;
    _stateController.add(newState);
  }

  /// 开始心跳
  void _startHeartbeat() {
    _stopHeartbeat();
    _heartbeatTimer = Timer.periodic(
      const Duration(milliseconds: SocketConfig.pingInterval),
      (_) => _socket?.emit(SocketEvents.ping, {}),
    );
  }

  /// 停止心跳
  void _stopHeartbeat() {
    _heartbeatTimer?.cancel();
    _heartbeatTimer = null;
  }

  /// 加入比赛场次
  void joinSession(int sessionId, int teamId) {
    _socket?.emit(SocketEvents.joinSession, {
      'sessionId': sessionId,
      'teamId': teamId,
    });
  }

  /// 抢答
  void buzz(int sessionId, int questionId, int teamId) {
    _socket?.emit(SocketEvents.clientBuzz, {
      'sessionId': sessionId,
      'questionId': questionId,
      'teamId': teamId,
      'clientTime': DateTime.now().millisecondsSinceEpoch,
    });
  }

  /// 提交答案
  void submitAnswer({
    required int sessionId,
    required int questionId,
    required int teamId,
    required dynamic answer,
  }) {
    _socket?.emit(SocketEvents.submitAnswer, {
      'sessionId': sessionId,
      'questionId': questionId,
      'teamId': teamId,
      'answer': answer,
      'submitTime': DateTime.now().millisecondsSinceEpoch,
    });
  }

  /// 发送自定义事件
  void emit(String event, dynamic data) {
    _socket?.emit(event, data);
  }

  /// 监听自定义事件
  void on(String event, Function(dynamic) callback) {
    _socket?.on(event, callback);
  }

  /// 断开连接
  void disconnect() {
    _stopHeartbeat();
    _socket?.disconnect();
    _socket?.dispose();
    _socket = null;
    _updateState(SocketState.disconnected);
  }

  /// 销毁服务
  void dispose() {
    disconnect();
    _stateController.close();
    _sessionStateController.close();
    _questionController.close();
    _buzzResultController.close();
    _scoreUpdateController.close();
    _answerResultController.close();
    _timerController.close();
    _errorController.close();
  }
}

/// Socket 服务 Provider
final socketServiceProvider = Provider<SocketService>((ref) {
  final service = SocketService();
  ref.onDispose(() => service.dispose());
  return service;
});

/// Socket 状态 Provider
final socketStateProvider = StreamProvider<SocketState>((ref) {
  final service = ref.watch(socketServiceProvider);
  return service.stateStream;
});

/// 场次状态 Provider
final sessionStateProvider = StreamProvider<Map<String, dynamic>>((ref) {
  final service = ref.watch(socketServiceProvider);
  return service.sessionStateStream;
});

/// 当前题目 Provider
final currentQuestionProvider = StreamProvider<Map<String, dynamic>>((ref) {
  final service = ref.watch(socketServiceProvider);
  return service.questionStream;
});

/// 抢答结果 Provider
final buzzResultProvider = StreamProvider<Map<String, dynamic>>((ref) {
  final service = ref.watch(socketServiceProvider);
  return service.buzzResultStream;
});

/// 分数更新 Provider
final scoreUpdateProvider = StreamProvider<Map<String, dynamic>>((ref) {
  final service = ref.watch(socketServiceProvider);
  return service.scoreUpdateStream;
});

/// 答案结果 Provider
final answerResultProvider = StreamProvider<Map<String, dynamic>>((ref) {
  final service = ref.watch(socketServiceProvider);
  return service.answerResultStream;
});
