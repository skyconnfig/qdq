import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/services/api_service.dart';
import '../../../core/theme/app_theme.dart';

/// 场次列表页面
class SessionListPage extends ConsumerStatefulWidget {
  const SessionListPage({super.key});

  @override
  ConsumerState<SessionListPage> createState() => _SessionListPageState();
}

class _SessionListPageState extends ConsumerState<SessionListPage> {
  List<dynamic> _sessions = [];
  bool _isLoading = true;
  String? _error;
  String _currentFilter = 'all';

  @override
  void initState() {
    super.initState();
    _loadSessions();
  }

  Future<void> _loadSessions() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      final api = ref.read(apiServiceProvider);
      final status = _currentFilter == 'all' ? null : _currentFilter;
      final response = await api.getSessions(status: status);
      
      if (response.isSuccess && response.data != null) {
        setState(() {
          _sessions = response.data!;
          _isLoading = false;
        });
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

  void _onFilterChanged(String filter) {
    setState(() {
      _currentFilter = filter;
    });
    _loadSessions();
  }

  Color _getStatusColor(String status) {
    switch (status) {
      case 'running':
        return AppColors.primary;
      case 'paused':
        return Colors.orange;
      case 'finished':
        return Colors.grey;
      case 'draft':
      default:
        return Colors.blue;
    }
  }

  String _getStatusText(String status) {
    switch (status) {
      case 'running':
        return '进行中';
      case 'paused':
        return '已暂停';
      case 'finished':
        return '已结束';
      case 'draft':
      default:
        return '未开始';
    }
  }

  IconData _getStatusIcon(String status) {
    switch (status) {
      case 'running':
        return Icons.play_circle_fill;
      case 'paused':
        return Icons.pause_circle_filled;
      case 'finished':
        return Icons.check_circle;
      case 'draft':
      default:
        return Icons.schedule;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('比赛场次'),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => context.go('/home'),
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _loadSessions,
            tooltip: '刷新',
          ),
        ],
      ),
      body: Column(
        children: [
          // 筛选标签
          _buildFilterTabs(),
          
          // 场次列表
          Expanded(
            child: _isLoading
                ? const Center(child: CircularProgressIndicator())
                : _error != null
                    ? _buildErrorView()
                    : _sessions.isEmpty
                        ? _buildEmptyView()
                        : _buildSessionList(),
          ),
        ],
      ),
    );
  }

  Widget _buildFilterTabs() {
    final filters = [
      {'key': 'all', 'label': '全部'},
      {'key': 'running', 'label': '进行中'},
      {'key': 'draft', 'label': '未开始'},
      {'key': 'finished', 'label': '已结束'},
    ];

    return Container(
      padding: const EdgeInsets.all(12),
      child: Row(
        children: filters.map((filter) {
          final isSelected = _currentFilter == filter['key'];
          return Padding(
            padding: const EdgeInsets.only(right: 8),
            child: ChoiceChip(
              label: Text(filter['label']!),
              selected: isSelected,
              onSelected: (_) => _onFilterChanged(filter['key']!),
              selectedColor: AppColors.primary,
              labelStyle: TextStyle(
                color: isSelected ? Colors.white : Colors.black87,
              ),
            ),
          );
        }).toList(),
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
            onPressed: _loadSessions,
            child: const Text('重试'),
          ),
        ],
      ),
    );
  }

  Widget _buildEmptyView() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(Icons.event_busy, size: 80, color: Colors.grey[400]),
          const SizedBox(height: 16),
          Text(
            '暂无比赛场次',
            style: TextStyle(fontSize: 18, color: Colors.grey[600]),
          ),
          const SizedBox(height: 8),
          Text(
            '下拉刷新试试',
            style: TextStyle(fontSize: 14, color: Colors.grey[400]),
          ),
        ],
      ),
    );
  }

  Widget _buildSessionList() {
    return RefreshIndicator(
      onRefresh: _loadSessions,
      child: ListView.builder(
        padding: const EdgeInsets.all(12),
        itemCount: _sessions.length,
        itemBuilder: (context, index) {
          final session = _sessions[index];
          return _buildSessionCard(session);
        },
      ),
    );
  }

  Widget _buildSessionCard(Map<String, dynamic> session) {
    final status = session['status'] as String? ?? 'draft';
    final statusColor = _getStatusColor(status);
    final canJoin = status == 'running' || status == 'draft';

    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
        side: BorderSide(
          color: statusColor.withOpacity(0.3),
          width: 1,
        ),
      ),
      child: InkWell(
        borderRadius: BorderRadius.circular(12),
        onTap: canJoin ? () => _joinSession(session) : null,
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  // 状态图标
                  Container(
                    width: 40,
                    height: 40,
                    decoration: BoxDecoration(
                      color: statusColor.withOpacity(0.1),
                      shape: BoxShape.circle,
                    ),
                    child: Icon(
                      _getStatusIcon(status),
                      color: statusColor,
                      size: 24,
                    ),
                  ),
                  const SizedBox(width: 12),
                  
                  // 场次名称
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          session['name'] ?? '未命名场次',
                          style: const TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        const SizedBox(height: 4),
                        Text(
                          '${session['questionCount'] ?? 0} 道题目',
                          style: TextStyle(
                            fontSize: 12,
                            color: Colors.grey[600],
                          ),
                        ),
                      ],
                    ),
                  ),
                  
                  // 状态标签
                  Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 12,
                      vertical: 4,
                    ),
                    decoration: BoxDecoration(
                      color: statusColor.withOpacity(0.1),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Text(
                      _getStatusText(status),
                      style: TextStyle(
                        fontSize: 12,
                        color: statusColor,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                  ),
                ],
              ),
              
              if (session['description'] != null) ...[
                const SizedBox(height: 12),
                Text(
                  session['description'],
                  style: TextStyle(
                    fontSize: 14,
                    color: Colors.grey[600],
                  ),
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                ),
              ],
              
              const SizedBox(height: 12),
              
              // 底部信息
              Row(
                children: [
                  // 时间
                  if (session['startTime'] != null) ...[
                    Icon(Icons.schedule, size: 14, color: Colors.grey[400]),
                    const SizedBox(width: 4),
                    Text(
                      _formatTime(session['startTime']),
                      style: TextStyle(fontSize: 12, color: Colors.grey[500]),
                    ),
                    const SizedBox(width: 16),
                  ],
                  
                  // 参与人数
                  Icon(Icons.people, size: 14, color: Colors.grey[400]),
                  const SizedBox(width: 4),
                  Text(
                    '${session['participantCount'] ?? 0} 人参与',
                    style: TextStyle(fontSize: 12, color: Colors.grey[500]),
                  ),
                  
                  const Spacer(),
                  
                  // 加入按钮
                  if (canJoin)
                    TextButton(
                      onPressed: () => _joinSession(session),
                      style: TextButton.styleFrom(
                        foregroundColor: AppColors.primary,
                      ),
                      child: Row(
                        children: [
                          const Text('进入'),
                          const SizedBox(width: 4),
                          Icon(Icons.arrow_forward_ios, size: 14),
                        ],
                      ),
                    ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  String _formatTime(String? timeStr) {
    if (timeStr == null) return '';
    try {
      final time = DateTime.parse(timeStr);
      return '${time.month}/${time.day} ${time.hour}:${time.minute.toString().padLeft(2, '0')}';
    } catch (e) {
      return timeStr;
    }
  }

  void _joinSession(Map<String, dynamic> session) {
    final sessionId = session['id'] as int;
    final status = session['status'] as String? ?? 'draft';
    
    if (status == 'running') {
      // 进行中的场次，进入抢答页面
      context.go('/buzz/$sessionId');
    } else {
      // 未开始的场次，进入等待页面
      context.go('/quiz/$sessionId');
    }
  }
}
