import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/services/socket_service.dart';
import '../../../core/services/api_service.dart';
import '../../../core/theme/app_theme.dart';

/// 比赛页面 - 答题界面
class QuizPage extends ConsumerStatefulWidget {
  final int sessionId;

  const QuizPage({super.key, required this.sessionId});

  @override
  ConsumerState<QuizPage> createState() => _QuizPageState();
}

class _QuizPageState extends ConsumerState<QuizPage> {
  // 场次信息
  Map<String, dynamic>? _sessionInfo;
  
  // 当前题目
  Map<String, dynamic>? _currentQuestion;
  
  // 答案选择
  dynamic _selectedAnswer;
  
  // 答题状态
  bool _isAnswering = false;
  bool _hasSubmitted = false;
  bool _isCorrect = false;
  int _earnedScore = 0;
  
  // 计时器
  int _remainingTime = 0;
  Timer? _timer;
  
  // 排行榜
  List<dynamic> _leaderboard = [];
  
  // 加载状态
  bool _isLoading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _initPage();
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }

  Future<void> _initPage() async {
    await _loadSessionInfo();
    _setupSocketListeners();
  }

  Future<void> _loadSessionInfo() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      final api = ref.read(apiServiceProvider);
      final response = await api.getSession(widget.sessionId);
      
      if (response.isSuccess && response.data != null) {
        setState(() {
          _sessionInfo = response.data;
          _isLoading = false;
        });
        
        // 加载排行榜
        _loadLeaderboard();
        
        // 连接 WebSocket
        final socket = ref.read(socketServiceProvider);
        final token = api.token;
        if (token != null) {
          socket.connect(token);
        }
      } else {
        setState(() {
          _error = response.message;
          _isLoading = false;
        });
      }
    } catch (e) {
      setState(() {
        _error = '加载失败: $e';
        _isLoading = false;
      });
    }
  }

  Future<void> _loadLeaderboard() async {
    try {
      final api = ref.read(apiServiceProvider);
      final response = await api.getLeaderboard(widget.sessionId);
      if (response.isSuccess && response.data != null) {
        setState(() {
          _leaderboard = response.data!;
        });
      }
    } catch (e) {
      print('加载排行榜失败: $e');
    }
  }

  void _setupSocketListeners() {
    final socket = ref.read(socketServiceProvider);
    
    // 监听题目推送
    ref.listen(currentQuestionProvider, (prev, next) {
      next.when(
        data: (question) {
          setState(() {
            _currentQuestion = question;
            _selectedAnswer = null;
            _hasSubmitted = false;
            _isAnswering = true;
            _isCorrect = false;
            _earnedScore = 0;
            _remainingTime = question['timeLimit'] ?? 30;
          });
          _startTimer();
        },
        loading: () {},
        error: (e, s) {},
      );
    });
    
    // 监听答案结果
    ref.listen(answerResultProvider, (prev, next) {
      next.when(
        data: (result) {
          setState(() {
            _isCorrect = result['correct'] ?? false;
            _earnedScore = result['score'] ?? 0;
          });
          _showResultDialog();
        },
        loading: () {},
        error: (e, s) {},
      );
    });
    
    // 监听分数更新
    ref.listen(scoreUpdateProvider, (prev, next) {
      next.when(
        data: (scores) {
          _loadLeaderboard();
        },
        loading: () {},
        error: (e, s) {},
      );
    });
    
    // 监听计时器同步
    ref.listen(timerStream, (prev, next) {
      next.when(
        data: (timer) {
          final remaining = timer['remaining'] as int?;
          if (remaining != null) {
            setState(() {
              _remainingTime = remaining;
            });
          }
        },
        loading: () {},
        error: (e, s) {},
      );
    });
  }

  void _startTimer() {
    _timer?.cancel();
    _timer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (_remainingTime > 0) {
        setState(() {
          _remainingTime--;
        });
      } else {
        timer.cancel();
        if (!_hasSubmitted) {
          _submitAnswer();
        }
      }
    });
  }

  Future<void> _submitAnswer() async {
    if (_hasSubmitted || _currentQuestion == null) return;
    
    setState(() {
      _hasSubmitted = true;
      _isAnswering = false;
    });
    
    _timer?.cancel();
    
    try {
      final api = ref.read(apiServiceProvider);
      final response = await api.submitAnswer(
        sessionId: widget.sessionId,
        questionId: _currentQuestion!['id'],
        teamId: 1, // TODO: 从用户状态获取
        answer: _selectedAnswer,
      );
      
      if (!response.isSuccess) {
        _showSnackBar(response.message);
      }
    } catch (e) {
      _showSnackBar('提交失败: $e');
    }
  }

  void _showResultDialog() {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              _isCorrect ? Icons.check_circle : Icons.cancel,
              size: 80,
              color: _isCorrect ? AppColors.primary : AppColors.danger,
            ),
            const SizedBox(height: 16),
            Text(
              _isCorrect ? '回答正确！' : '回答错误',
              style: const TextStyle(
                fontSize: 24,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              _isCorrect ? '+$_earnedScore 分' : '本题未得分',
              style: TextStyle(
                fontSize: 18,
                color: _isCorrect ? AppColors.primary : Colors.grey,
              ),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('确定'),
          ),
        ],
      ),
    );
  }

  void _showSnackBar(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        behavior: SnackBarBehavior.floating,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return Scaffold(
        appBar: AppBar(title: const Text('知识竞赛')),
        body: const Center(child: CircularProgressIndicator()),
      );
    }

    if (_error != null) {
      return Scaffold(
        appBar: AppBar(title: const Text('知识竞赛')),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.error_outline, size: 64, color: Colors.grey),
              const SizedBox(height: 16),
              Text(_error!),
              const SizedBox(height: 24),
              ElevatedButton(
                onPressed: _loadSessionInfo,
                child: const Text('重试'),
              ),
            ],
          ),
        ),
      );
    }

    return Scaffold(
      appBar: AppBar(
        title: Text(_sessionInfo?['name'] ?? '知识竞赛'),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => context.go('/home'),
        ),
        actions: [
          // 抢答按钮
          TextButton.icon(
            onPressed: () => context.go('/buzz/${widget.sessionId}'),
            icon: const Icon(Icons.flash_on, color: Colors.white),
            label: const Text('抢答', style: TextStyle(color: Colors.white)),
          ),
        ],
      ),
      body: _currentQuestion == null ? _buildWaitingView() : _buildQuizView(),
    );
  }

  Widget _buildWaitingView() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Icon(
            Icons.hourglass_empty,
            size: 80,
            color: Colors.grey,
          ),
          const SizedBox(height: 24),
          const Text(
            '等待出题...',
            style: TextStyle(fontSize: 20, fontWeight: FontWeight.w500),
          ),
          const SizedBox(height: 8),
          const Text(
            '请等待主持人发布题目',
            style: TextStyle(fontSize: 14, color: Colors.grey),
          ),
          const SizedBox(height: 48),
          // 排行榜
          if (_leaderboard.isNotEmpty) ...[
            const Text(
              '当前排行',
              style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 16),
            SizedBox(
              width: 300,
              child: _buildLeaderboard(),
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildQuizView() {
    final question = _currentQuestion!;
    final type = question['type'] as int? ?? 1;
    final options = question['options'] as List<dynamic>? ?? [];
    
    return Column(
      children: [
        // 计时器和进度
        _buildTimerBar(),
        
        Expanded(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(20),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // 题目类型标签
                _buildQuestionTypeTag(type),
                const SizedBox(height: 12),
                
                // 题目标题
                Text(
                  question['title'] ?? '',
                  style: const TextStyle(
                    fontSize: 20,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 16),
                
                // 题目内容
                if (question['content'] != null) ...[
                  Text(
                    question['content'],
                    style: const TextStyle(fontSize: 16),
                  ),
                  const SizedBox(height: 24),
                ],
                
                // 答案选项
                _buildAnswerOptions(type, options),
              ],
            ),
          ),
        ),
        
        // 提交按钮
        _buildSubmitButton(),
      ],
    );
  }

  Widget _buildTimerBar() {
    final progress = _currentQuestion != null
        ? _remainingTime / (_currentQuestion!['timeLimit'] ?? 30)
        : 0.0;
    
    final isUrgent = _remainingTime <= 5;
    
    return Container(
      padding: const EdgeInsets.all(16),
      color: isUrgent ? AppColors.danger.withOpacity(0.1) : Colors.grey[100],
      child: Row(
        children: [
          Icon(
            Icons.timer,
            color: isUrgent ? AppColors.danger : AppColors.primary,
          ),
          const SizedBox(width: 8),
          Text(
            '$_remainingTime 秒',
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.bold,
              color: isUrgent ? AppColors.danger : AppColors.primary,
            ),
          ),
          const SizedBox(width: 16),
          Expanded(
            child: LinearProgressIndicator(
              value: progress,
              backgroundColor: Colors.grey[300],
              valueColor: AlwaysStoppedAnimation(
                isUrgent ? AppColors.danger : AppColors.primary,
              ),
            ),
          ),
          const SizedBox(width: 16),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
            decoration: BoxDecoration(
              color: AppColors.primary,
              borderRadius: BorderRadius.circular(16),
            ),
            child: Text(
              '${_currentQuestion?['score'] ?? 0} 分',
              style: const TextStyle(
                color: Colors.white,
                fontWeight: FontWeight.bold,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildQuestionTypeTag(int type) {
    String label;
    Color color;
    
    switch (type) {
      case 1:
        label = '单选题';
        color = Colors.blue;
        break;
      case 2:
        label = '多选题';
        color = Colors.purple;
        break;
      case 3:
        label = '判断题';
        color = Colors.orange;
        break;
      case 4:
        label = '填空题';
        color = Colors.teal;
        break;
      case 5:
        label = '主观题';
        color = Colors.pink;
        break;
      default:
        label = '未知';
        color = Colors.grey;
    }
    
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
      decoration: BoxDecoration(
        color: color.withOpacity(0.1),
        borderRadius: BorderRadius.circular(4),
        border: Border.all(color: color.withOpacity(0.3)),
      ),
      child: Text(
        label,
        style: TextStyle(
          color: color,
          fontSize: 12,
          fontWeight: FontWeight.w500,
        ),
      ),
    );
  }

  Widget _buildAnswerOptions(int type, List<dynamic> options) {
    if (type == 3) {
      // 判断题
      return Row(
        children: [
          Expanded(
            child: _buildOptionButton('正确', true),
          ),
          const SizedBox(width: 16),
          Expanded(
            child: _buildOptionButton('错误', false),
          ),
        ],
      );
    }
    
    if (type == 4 || type == 5) {
      // 填空题/主观题
      return TextField(
        onChanged: (value) => setState(() => _selectedAnswer = value),
        enabled: _isAnswering,
        maxLines: type == 5 ? 5 : 1,
        decoration: InputDecoration(
          hintText: type == 5 ? '请输入您的答案...' : '请填写答案',
          border: const OutlineInputBorder(),
          focusedBorder: OutlineInputBorder(
            borderSide: BorderSide(color: AppColors.primary, width: 2),
          ),
        ),
      );
    }
    
    // 单选题/多选题
    return Column(
      children: options.asMap().entries.map((entry) {
        final index = entry.key;
        final option = entry.value;
        final optionLabel = String.fromCharCode(65 + index); // A, B, C, D
        final optionText = option is Map ? option['text'] : option.toString();
        
        return _buildChoiceOption(optionLabel, optionText, type == 2);
      }).toList(),
    );
  }

  Widget _buildOptionButton(String text, bool value) {
    final isSelected = _selectedAnswer == value;
    
    return GestureDetector(
      onTap: _isAnswering ? () => setState(() => _selectedAnswer = value) : null,
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 20),
        decoration: BoxDecoration(
          color: isSelected ? AppColors.primary : Colors.white,
          borderRadius: BorderRadius.circular(12),
          border: Border.all(
            color: isSelected ? AppColors.primary : Colors.grey[300]!,
            width: 2,
          ),
        ),
        child: Center(
          child: Text(
            text,
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.bold,
              color: isSelected ? Colors.white : Colors.black87,
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildChoiceOption(String label, String text, bool isMultiple) {
    final isSelected = isMultiple
        ? (_selectedAnswer as List?)?.contains(label) ?? false
        : _selectedAnswer == label;
    
    return GestureDetector(
      onTap: _isAnswering
          ? () {
              setState(() {
                if (isMultiple) {
                  final list = (_selectedAnswer as List?) ?? [];
                  if (list.contains(label)) {
                    list.remove(label);
                  } else {
                    list.add(label);
                  }
                  _selectedAnswer = list;
                } else {
                  _selectedAnswer = label;
                }
              });
            }
          : null,
      child: Container(
        margin: const EdgeInsets.only(bottom: 12),
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: isSelected ? AppColors.primary.withOpacity(0.1) : Colors.white,
          borderRadius: BorderRadius.circular(12),
          border: Border.all(
            color: isSelected ? AppColors.primary : Colors.grey[300]!,
            width: isSelected ? 2 : 1,
          ),
        ),
        child: Row(
          children: [
            Container(
              width: 36,
              height: 36,
              decoration: BoxDecoration(
                color: isSelected ? AppColors.primary : Colors.grey[200],
                shape: BoxShape.circle,
              ),
              child: Center(
                child: Text(
                  label,
                  style: TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                    color: isSelected ? Colors.white : Colors.black54,
                  ),
                ),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Text(
                text,
                style: const TextStyle(fontSize: 16),
              ),
            ),
            if (isSelected)
              Icon(
                isMultiple ? Icons.check_box : Icons.check_circle,
                color: AppColors.primary,
              ),
          ],
        ),
      ),
    );
  }

  Widget _buildSubmitButton() {
    return Container(
      padding: const EdgeInsets.all(20),
      child: SizedBox(
        width: double.infinity,
        height: 50,
        child: ElevatedButton(
          onPressed: _isAnswering && _selectedAnswer != null ? _submitAnswer : null,
          style: ElevatedButton.styleFrom(
            backgroundColor: AppColors.primary,
            disabledBackgroundColor: Colors.grey[300],
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(8),
            ),
          ),
          child: Text(
            _hasSubmitted ? '已提交' : '提交答案',
            style: const TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.bold,
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildLeaderboard() {
    return Card(
      child: Column(
        children: _leaderboard.take(5).map((team) {
          final rank = _leaderboard.indexOf(team) + 1;
          return ListTile(
            leading: CircleAvatar(
              backgroundColor: rank <= 3 ? AppColors.primary : Colors.grey,
              child: Text(
                '$rank',
                style: const TextStyle(color: Colors.white),
              ),
            ),
            title: Text(team['name'] ?? '队伍$rank'),
            trailing: Text(
              '${team['score'] ?? 0} 分',
              style: const TextStyle(
                fontWeight: FontWeight.bold,
                color: AppColors.primary,
              ),
            ),
          );
        }).toList(),
      ),
    );
  }
}

/// Timer Stream Provider
final timerStream = StreamProvider<Map<String, dynamic>>((ref) {
  final service = ref.watch(socketServiceProvider);
  return service.timerStream;
});
