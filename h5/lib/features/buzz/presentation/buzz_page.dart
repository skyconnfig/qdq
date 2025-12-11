import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:vibration/vibration.dart';
import '../../../core/theme/app_theme.dart';

/// 抢答状态
enum BuzzState {
  waiting,    // 等待开始
  ready,      // 准备抢答
  buzzing,    // 正在抢答
  success,    // 抢答成功
  failed,     // 抢答失败
  answering,  // 正在答题
}

/// 抢答页面
class BuzzPage extends ConsumerStatefulWidget {
  final int sessionId;
  
  const BuzzPage({super.key, required this.sessionId});

  @override
  ConsumerState<BuzzPage> createState() => _BuzzPageState();
}

class _BuzzPageState extends ConsumerState<BuzzPage> 
    with SingleTickerProviderStateMixin {
  BuzzState _buzzState = BuzzState.waiting;
  String _message = '等待比赛开始...';
  Map<String, dynamic>? _currentQuestion;
  int _countdown = 0;
  Timer? _countdownTimer;
  
  // 长按相关
  bool _isPressed = false;
  double _pressProgress = 0;
  Timer? _pressTimer;
  static const _longPressDuration = 500; // 长按时间（毫秒）

  late AnimationController _pulseController;
  late Animation<double> _pulseAnimation;

  @override
  void initState() {
    super.initState();
    _initAnimation();
    _connectWebSocket();
  }

  void _initAnimation() {
    _pulseController = AnimationController(
      duration: const Duration(milliseconds: 1500),
      vsync: this,
    )..repeat(reverse: true);
    
    _pulseAnimation = Tween<double>(begin: 1.0, end: 1.1).animate(
      CurvedAnimation(parent: _pulseController, curve: Curves.easeInOut),
    );
  }

  void _connectWebSocket() {
    // TODO: 连接WebSocket，监听事件
    // 模拟收到题目
    Future.delayed(const Duration(seconds: 2), () {
      if (mounted) {
        setState(() {
          _buzzState = BuzzState.ready;
          _message = '准备抢答！';
          _currentQuestion = {
            'id': 1,
            'title': '中国的首都是哪里？',
            'type': 1,
            'options': [
              {'key': 'A', 'value': '北京'},
              {'key': 'B', 'value': '上海'},
              {'key': 'C', 'value': '广州'},
              {'key': 'D', 'value': '深圳'},
            ],
          };
          _countdown = 30;
          _startCountdown();
        });
      }
    });
  }

  void _startCountdown() {
    _countdownTimer?.cancel();
    _countdownTimer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (_countdown > 0) {
        setState(() => _countdown--);
      } else {
        timer.cancel();
        _handleTimeout();
      }
    });
  }

  void _handleTimeout() {
    setState(() {
      _buzzState = BuzzState.waiting;
      _message = '时间到！等待下一题...';
    });
  }

  // 处理按下开始
  void _onPressStart() {
    if (_buzzState != BuzzState.ready) return;
    
    setState(() {
      _isPressed = true;
      _pressProgress = 0;
    });
    
    // 开始长按计时
    const tickDuration = 10; // 每10毫秒更新一次
    _pressTimer = Timer.periodic(
      const Duration(milliseconds: tickDuration),
      (timer) {
        final newProgress = _pressProgress + (tickDuration / _longPressDuration);
        
        if (newProgress >= 1.0) {
          timer.cancel();
          _triggerBuzz();
        } else {
          setState(() => _pressProgress = newProgress);
        }
      },
    );
  }

  // 处理松开
  void _onPressEnd() {
    _pressTimer?.cancel();
    
    if (_pressProgress < 1.0 && _isPressed) {
      // 未完成长按，取消
      setState(() {
        _isPressed = false;
        _pressProgress = 0;
      });
    }
  }

  // 触发抢答
  void _triggerBuzz() async {
    // 震动反馈
    if (await Vibration.hasVibrator() ?? false) {
      Vibration.vibrate(duration: 100);
    }
    
    // 触感反馈
    HapticFeedback.heavyImpact();
    
    setState(() {
      _buzzState = BuzzState.buzzing;
      _message = '抢答中...';
      _isPressed = false;
    });
    
    // TODO: 发送抢答请求到服务器
    // 模拟抢答结果
    await Future.delayed(const Duration(milliseconds: 500));
    
    if (mounted) {
      // 模拟随机成功/失败
      final success = DateTime.now().millisecond % 3 != 0;
      
      setState(() {
        if (success) {
          _buzzState = BuzzState.success;
          _message = '抢答成功！';
          _countdownTimer?.cancel();
        } else {
          _buzzState = BuzzState.failed;
          _message = '很遗憾，有人比你更快';
        }
      });
      
      // 如果成功，进入答题状态
      if (success) {
        await Future.delayed(const Duration(seconds: 1));
        if (mounted) {
          setState(() {
            _buzzState = BuzzState.answering;
            _message = '请作答';
          });
        }
      }
    }
  }

  @override
  void dispose() {
    _pulseController.dispose();
    _countdownTimer?.cancel();
    _pressTimer?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: _getBackgroundColor(),
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        foregroundColor: Colors.white,
        elevation: 0,
        title: const Text('抢答'),
        actions: [
          if (_countdown > 0)
            Container(
              margin: const EdgeInsets.only(right: 16),
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
              decoration: BoxDecoration(
                color: Colors.white24,
                borderRadius: BorderRadius.circular(16),
              ),
              child: Row(
                children: [
                  const Icon(Icons.timer, size: 18),
                  const SizedBox(width: 4),
                  Text(
                    '${_countdown}s',
                    style: const TextStyle(fontWeight: FontWeight.bold),
                  ),
                ],
              ),
            ),
        ],
      ),
      body: SafeArea(
        child: Column(
          children: [
            // 状态消息
            Padding(
              padding: const EdgeInsets.all(16),
              child: Text(
                _message,
                style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                  color: Colors.white,
                  fontWeight: FontWeight.bold,
                ),
                textAlign: TextAlign.center,
              ),
            ),
            
            // 题目区域
            if (_currentQuestion != null && _buzzState != BuzzState.waiting)
              Expanded(
                child: Container(
                  margin: const EdgeInsets.all(16),
                  padding: const EdgeInsets.all(20),
                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(16),
                  ),
                  child: _buildQuestionContent(),
                ),
              ),
            
            // 抢答按钮区域
            if (_buzzState == BuzzState.ready || _buzzState == BuzzState.buzzing)
              Expanded(
                child: Center(
                  child: _buildBuzzButton(),
                ),
              ),
            
            // 答题选项
            if (_buzzState == BuzzState.answering)
              Expanded(
                child: _buildAnswerOptions(),
              ),
            
            // 结果展示
            if (_buzzState == BuzzState.success || _buzzState == BuzzState.failed)
              _buildResultBanner(),
          ],
        ),
      ),
    );
  }

  Color _getBackgroundColor() {
    switch (_buzzState) {
      case BuzzState.success:
        return AppTheme.successColor;
      case BuzzState.failed:
        return AppTheme.dangerColor;
      case BuzzState.ready:
      case BuzzState.buzzing:
        return AppTheme.primaryColor;
      default:
        return const Color(0xFF1A1A2E);
    }
  }

  Widget _buildQuestionContent() {
    return SingleChildScrollView(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
            decoration: BoxDecoration(
              color: AppTheme.primaryColor.withOpacity(0.1),
              borderRadius: BorderRadius.circular(8),
            ),
            child: Text(
              '单选题',
              style: TextStyle(
                color: AppTheme.primaryColor,
                fontWeight: FontWeight.w500,
              ),
            ),
          ),
          const SizedBox(height: 16),
          Text(
            _currentQuestion!['title'] as String,
            style: Theme.of(context).textTheme.titleLarge,
          ),
        ],
      ),
    );
  }

  Widget _buildBuzzButton() {
    return GestureDetector(
      onTapDown: (_) => _onPressStart(),
      onTapUp: (_) => _onPressEnd(),
      onTapCancel: _onPressEnd,
      child: AnimatedBuilder(
        animation: _pulseAnimation,
        builder: (context, child) {
          return Transform.scale(
            scale: _buzzState == BuzzState.ready ? _pulseAnimation.value : 1.0,
            child: Stack(
              alignment: Alignment.center,
              children: [
                // 进度圆环
                SizedBox(
                  width: 200,
                  height: 200,
                  child: CircularProgressIndicator(
                    value: _pressProgress,
                    strokeWidth: 8,
                    backgroundColor: Colors.white30,
                    valueColor: const AlwaysStoppedAnimation(Colors.white),
                  ),
                ),
                // 按钮
                Container(
                  width: 180,
                  height: 180,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    color: _isPressed ? Colors.white : Colors.white24,
                    boxShadow: [
                      BoxShadow(
                        color: Colors.black.withOpacity(0.2),
                        blurRadius: 20,
                        offset: const Offset(0, 10),
                      ),
                    ],
                  ),
                  child: Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(
                          _buzzState == BuzzState.buzzing
                              ? Icons.hourglass_top
                              : Icons.touch_app,
                          size: 48,
                          color: _isPressed ? AppTheme.primaryColor : Colors.white,
                        ),
                        const SizedBox(height: 8),
                        Text(
                          _buzzState == BuzzState.buzzing ? '请稍候' : '长按抢答',
                          style: TextStyle(
                            fontSize: 18,
                            fontWeight: FontWeight.bold,
                            color: _isPressed ? AppTheme.primaryColor : Colors.white,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }

  Widget _buildAnswerOptions() {
    final options = _currentQuestion!['options'] as List;
    
    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: options.length,
      itemBuilder: (context, index) {
        final option = options[index] as Map<String, dynamic>;
        
        return Container(
          margin: const EdgeInsets.only(bottom: 12),
          child: Material(
            color: Colors.white,
            borderRadius: BorderRadius.circular(12),
            child: InkWell(
              onTap: () => _submitAnswer(option['key']),
              borderRadius: BorderRadius.circular(12),
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Row(
                  children: [
                    Container(
                      width: 40,
                      height: 40,
                      decoration: BoxDecoration(
                        color: AppTheme.primaryColor.withOpacity(0.1),
                        borderRadius: BorderRadius.circular(20),
                      ),
                      child: Center(
                        child: Text(
                          option['key'] as String,
                          style: TextStyle(
                            fontSize: 18,
                            fontWeight: FontWeight.bold,
                            color: AppTheme.primaryColor,
                          ),
                        ),
                      ),
                    ),
                    const SizedBox(width: 16),
                    Expanded(
                      child: Text(
                        option['value'] as String,
                        style: Theme.of(context).textTheme.titleMedium,
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        );
      },
    );
  }

  Widget _buildResultBanner() {
    final isSuccess = _buzzState == BuzzState.success;
    
    return Container(
      padding: const EdgeInsets.all(32),
      child: Column(
        children: [
          Icon(
            isSuccess ? Icons.check_circle : Icons.cancel,
            size: 80,
            color: Colors.white,
          ),
          const SizedBox(height: 16),
          Text(
            isSuccess ? '太棒了！' : '再接再厉！',
            style: const TextStyle(
              fontSize: 24,
              fontWeight: FontWeight.bold,
              color: Colors.white,
            ),
          ),
        ],
      ),
    );
  }

  void _submitAnswer(String answer) async {
    // TODO: 提交答案到服务器
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('已提交答案: $answer')),
    );
    
    // 等待下一题
    setState(() {
      _buzzState = BuzzState.waiting;
      _message = '答案已提交，等待下一题...';
      _currentQuestion = null;
    });
  }
}
