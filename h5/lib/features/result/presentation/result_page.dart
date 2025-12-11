import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/services/api_service.dart';
import '../../../core/theme/app_theme.dart';

/// 比赛结果页面
class ResultPage extends ConsumerStatefulWidget {
  final int sessionId;

  const ResultPage({super.key, required this.sessionId});

  @override
  ConsumerState<ResultPage> createState() => _ResultPageState();
}

class _ResultPageState extends ConsumerState<ResultPage>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;
  
  Map<String, dynamic>? _sessionInfo;
  List<dynamic> _leaderboard = [];
  Map<String, dynamic>? _myResult;
  List<dynamic> _answerHistory = [];
  
  bool _isLoading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 3, vsync: this);
    _loadData();
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  Future<void> _loadData() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      final api = ref.read(apiServiceProvider);
      
      // 并行加载数据
      final results = await Future.wait([
        api.getSession(widget.sessionId),
        api.getLeaderboard(widget.sessionId),
        api.getMyTeam(widget.sessionId),
      ]);
      
      final sessionResponse = results[0] as ApiResponse<Map<String, dynamic>>;
      final leaderboardResponse = results[1] as ApiResponse<List<dynamic>>;
      final myTeamResponse = results[2] as ApiResponse<Map<String, dynamic>>;
      
      setState(() {
        if (sessionResponse.isSuccess) {
          _sessionInfo = sessionResponse.data;
        }
        if (leaderboardResponse.isSuccess) {
          _leaderboard = leaderboardResponse.data ?? [];
        }
        if (myTeamResponse.isSuccess) {
          _myResult = myTeamResponse.data;
        }
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _error = '加载失败: $e';
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('比赛结果'),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => context.go('/home'),
        ),
        bottom: TabBar(
          controller: _tabController,
          labelColor: Colors.white,
          unselectedLabelColor: Colors.white70,
          indicatorColor: Colors.white,
          tabs: const [
            Tab(text: '排行榜', icon: Icon(Icons.leaderboard, size: 20)),
            Tab(text: '我的成绩', icon: Icon(Icons.person, size: 20)),
            Tab(text: '答题记录', icon: Icon(Icons.history, size: 20)),
          ],
        ),
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _error != null
              ? _buildErrorView()
              : TabBarView(
                  controller: _tabController,
                  children: [
                    _buildLeaderboardTab(),
                    _buildMyResultTab(),
                    _buildHistoryTab(),
                  ],
                ),
    );
  }

  Widget _buildErrorView() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Icon(Icons.error_outline, size: 64, color: Colors.grey),
          const SizedBox(height: 16),
          Text(_error!),
          const SizedBox(height: 24),
          ElevatedButton(
            onPressed: _loadData,
            child: const Text('重试'),
          ),
        ],
      ),
    );
  }

  Widget _buildLeaderboardTab() {
    if (_leaderboard.isEmpty) {
      return const Center(child: Text('暂无排行数据'));
    }

    return RefreshIndicator(
      onRefresh: _loadData,
      child: ListView.builder(
        padding: const EdgeInsets.all(16),
        itemCount: _leaderboard.length,
        itemBuilder: (context, index) {
          final team = _leaderboard[index];
          final rank = index + 1;
          return _buildRankItem(rank, team);
        },
      ),
    );
  }

  Widget _buildRankItem(int rank, Map<String, dynamic> team) {
    final isTopThree = rank <= 3;
    final isMe = team['id'] == _myResult?['teamId'];
    
    Color rankColor;
    IconData? rankIcon;
    
    switch (rank) {
      case 1:
        rankColor = const Color(0xFFFFD700); // 金色
        rankIcon = Icons.emoji_events;
        break;
      case 2:
        rankColor = const Color(0xFFC0C0C0); // 银色
        rankIcon = Icons.emoji_events;
        break;
      case 3:
        rankColor = const Color(0xFFCD7F32); // 铜色
        rankIcon = Icons.emoji_events;
        break;
      default:
        rankColor = Colors.grey;
        rankIcon = null;
    }

    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      decoration: BoxDecoration(
        color: isMe ? AppColors.primary.withOpacity(0.1) : Colors.white,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(
          color: isMe ? AppColors.primary : Colors.grey[200]!,
          width: isMe ? 2 : 1,
        ),
        boxShadow: isTopThree
            ? [
                BoxShadow(
                  color: rankColor.withOpacity(0.3),
                  blurRadius: 8,
                  offset: const Offset(0, 2),
                ),
              ]
            : null,
      ),
      child: ListTile(
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        leading: Container(
          width: 48,
          height: 48,
          decoration: BoxDecoration(
            color: rankColor.withOpacity(isTopThree ? 1 : 0.2),
            shape: BoxShape.circle,
          ),
          child: Center(
            child: rankIcon != null
                ? Icon(rankIcon, color: Colors.white, size: 24)
                : Text(
                    '$rank',
                    style: TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
                      color: isTopThree ? Colors.white : Colors.grey[600],
                    ),
                  ),
          ),
        ),
        title: Row(
          children: [
            Expanded(
              child: Text(
                team['name'] ?? '队伍$rank',
                style: TextStyle(
                  fontSize: 16,
                  fontWeight: isMe ? FontWeight.bold : FontWeight.w500,
                ),
              ),
            ),
            if (isMe)
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                decoration: BoxDecoration(
                  color: AppColors.primary,
                  borderRadius: BorderRadius.circular(10),
                ),
                child: const Text(
                  '我',
                  style: TextStyle(color: Colors.white, fontSize: 12),
                ),
              ),
          ],
        ),
        subtitle: Text(
          '正确率: ${team['correctRate'] ?? 0}%  |  用时: ${team['totalTime'] ?? 0}s',
          style: TextStyle(fontSize: 12, color: Colors.grey[600]),
        ),
        trailing: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.end,
          children: [
            Text(
              '${team['score'] ?? 0}',
              style: TextStyle(
                fontSize: 24,
                fontWeight: FontWeight.bold,
                color: AppColors.primary,
              ),
            ),
            const Text('分', style: TextStyle(fontSize: 12, color: Colors.grey)),
          ],
        ),
      ),
    );
  }

  Widget _buildMyResultTab() {
    if (_myResult == null) {
      return const Center(child: Text('暂无成绩数据'));
    }

    final result = _myResult!;
    final rank = result['rank'] ?? 0;
    final score = result['score'] ?? 0;
    final correctCount = result['correctCount'] ?? 0;
    final totalCount = result['totalCount'] ?? 0;
    final correctRate = totalCount > 0 ? (correctCount / totalCount * 100).toInt() : 0;

    return SingleChildScrollView(
      padding: const EdgeInsets.all(20),
      child: Column(
        children: [
          // 排名卡片
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(24),
            decoration: BoxDecoration(
              gradient: LinearGradient(
                colors: [
                  AppColors.primary,
                  AppColors.primary.withOpacity(0.8),
                ],
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
              ),
              borderRadius: BorderRadius.circular(16),
              boxShadow: [
                BoxShadow(
                  color: AppColors.primary.withOpacity(0.3),
                  blurRadius: 12,
                  offset: const Offset(0, 4),
                ),
              ],
            ),
            child: Column(
              children: [
                const Text(
                  '最终排名',
                  style: TextStyle(
                    color: Colors.white70,
                    fontSize: 14,
                  ),
                ),
                const SizedBox(height: 8),
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  crossAxisAlignment: CrossAxisAlignment.baseline,
                  textBaseline: TextBaseline.alphabetic,
                  children: [
                    const Text(
                      '第',
                      style: TextStyle(color: Colors.white, fontSize: 24),
                    ),
                    Text(
                      ' $rank ',
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 64,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const Text(
                      '名',
                      style: TextStyle(color: Colors.white, fontSize: 24),
                    ),
                  ],
                ),
                const SizedBox(height: 16),
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    _buildMedalIcon(rank),
                    const SizedBox(width: 8),
                    Text(
                      _getRankTitle(rank),
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 16,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
          
          const SizedBox(height: 24),
          
          // 统计卡片
          Row(
            children: [
              Expanded(
                child: _buildStatCard(
                  '总分',
                  '$score',
                  Icons.stars,
                  Colors.amber,
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: _buildStatCard(
                  '正确',
                  '$correctCount/$totalCount',
                  Icons.check_circle,
                  AppColors.primary,
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: _buildStatCard(
                  '正确率',
                  '$correctRate%',
                  Icons.pie_chart,
                  Colors.blue,
                ),
              ),
            ],
          ),
          
          const SizedBox(height: 24),
          
          // 详细数据
          _buildDetailSection(result),
        ],
      ),
    );
  }

  Widget _buildMedalIcon(int rank) {
    if (rank == 1) {
      return const Icon(Icons.emoji_events, color: Color(0xFFFFD700), size: 28);
    } else if (rank == 2) {
      return const Icon(Icons.emoji_events, color: Color(0xFFC0C0C0), size: 28);
    } else if (rank == 3) {
      return const Icon(Icons.emoji_events, color: Color(0xFFCD7F32), size: 28);
    }
    return const SizedBox.shrink();
  }

  String _getRankTitle(int rank) {
    switch (rank) {
      case 1:
        return '冠军';
      case 2:
        return '亚军';
      case 3:
        return '季军';
      default:
        return '完成比赛';
    }
  }

  Widget _buildStatCard(String label, String value, IconData icon, Color color) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 8,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        children: [
          Icon(icon, color: color, size: 28),
          const SizedBox(height: 8),
          Text(
            value,
            style: TextStyle(
              fontSize: 20,
              fontWeight: FontWeight.bold,
              color: color,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            label,
            style: TextStyle(
              fontSize: 12,
              color: Colors.grey[600],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildDetailSection(Map<String, dynamic> result) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 8,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            '详细数据',
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 16),
          _buildDetailRow('队伍名称', result['teamName'] ?? '-'),
          _buildDetailRow('答题数量', '${result['totalCount'] ?? 0} 题'),
          _buildDetailRow('抢答成功', '${result['buzzCount'] ?? 0} 次'),
          _buildDetailRow('平均用时', '${result['avgTime'] ?? 0} 秒'),
          _buildDetailRow('最快答题', '${result['fastestTime'] ?? 0} 秒'),
        ],
      ),
    );
  }

  Widget _buildDetailRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            label,
            style: TextStyle(
              fontSize: 14,
              color: Colors.grey[600],
            ),
          ),
          Text(
            value,
            style: const TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.w500,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildHistoryTab() {
    if (_answerHistory.isEmpty) {
      return const Center(child: Text('暂无答题记录'));
    }

    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: _answerHistory.length,
      itemBuilder: (context, index) {
        final record = _answerHistory[index];
        return _buildHistoryItem(index + 1, record);
      },
    );
  }

  Widget _buildHistoryItem(int index, Map<String, dynamic> record) {
    final isCorrect = record['correct'] ?? false;
    
    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(
          color: isCorrect
              ? AppColors.primary.withOpacity(0.3)
              : AppColors.danger.withOpacity(0.3),
        ),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                width: 28,
                height: 28,
                decoration: BoxDecoration(
                  color: isCorrect
                      ? AppColors.primary.withOpacity(0.1)
                      : AppColors.danger.withOpacity(0.1),
                  shape: BoxShape.circle,
                ),
                child: Center(
                  child: Text(
                    '$index',
                    style: TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.bold,
                      color: isCorrect ? AppColors.primary : AppColors.danger,
                    ),
                  ),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Text(
                  record['questionTitle'] ?? '题目$index',
                  style: const TextStyle(fontWeight: FontWeight.w500),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
              ),
              Icon(
                isCorrect ? Icons.check_circle : Icons.cancel,
                color: isCorrect ? AppColors.primary : AppColors.danger,
              ),
            ],
          ),
          const SizedBox(height: 12),
          Row(
            children: [
              _buildHistoryTag('您的答案: ${record['userAnswer'] ?? '-'}'),
              const SizedBox(width: 8),
              if (!isCorrect)
                _buildHistoryTag(
                  '正确答案: ${record['correctAnswer'] ?? '-'}',
                  color: AppColors.primary,
                ),
            ],
          ),
          const SizedBox(height: 8),
          Text(
            '得分: ${record['score'] ?? 0}  |  用时: ${record['time'] ?? 0}s',
            style: TextStyle(fontSize: 12, color: Colors.grey[600]),
          ),
        ],
      ),
    );
  }

  Widget _buildHistoryTag(String text, {Color? color}) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: (color ?? Colors.grey).withOpacity(0.1),
        borderRadius: BorderRadius.circular(4),
      ),
      child: Text(
        text,
        style: TextStyle(
          fontSize: 12,
          color: color ?? Colors.grey[600],
        ),
      ),
    );
  }
}
