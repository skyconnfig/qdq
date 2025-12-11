/// 用户模型
class User {
  final int id;
  final String username;
  final String? name;
  final String? avatar;
  final String? role;
  final int? status;
  final String? createdAt;

  User({
    required this.id,
    required this.username,
    this.name,
    this.avatar,
    this.role,
    this.status,
    this.createdAt,
  });

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      id: json['id'] ?? 0,
      username: json['username'] ?? '',
      name: json['name'],
      avatar: json['avatar'],
      role: json['role'],
      status: json['status'],
      createdAt: json['createdAt'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'username': username,
      'name': name,
      'avatar': avatar,
      'role': role,
      'status': status,
      'createdAt': createdAt,
    };
  }
}

/// 题目类型枚举
enum QuestionType {
  single(1, '单选题'),
  multiple(2, '多选题'),
  judge(3, '判断题'),
  blank(4, '填空题'),
  subjective(5, '主观题');

  final int value;
  final String label;
  const QuestionType(this.value, this.label);

  static QuestionType fromValue(int value) {
    return QuestionType.values.firstWhere(
      (e) => e.value == value,
      orElse: () => QuestionType.single,
    );
  }
}

/// 题目模型
class Question {
  final int id;
  final QuestionType type;
  final String title;
  final String? content;
  final List<dynamic>? options;
  final dynamic answer;
  final int score;
  final int? timeLimit;
  final List<String>? attachments;
  final List<String>? tags;
  final int? difficulty;

  Question({
    required this.id,
    required this.type,
    required this.title,
    this.content,
    this.options,
    this.answer,
    this.score = 10,
    this.timeLimit,
    this.attachments,
    this.tags,
    this.difficulty,
  });

  factory Question.fromJson(Map<String, dynamic> json) {
    return Question(
      id: json['id'] ?? 0,
      type: QuestionType.fromValue(json['type'] ?? 1),
      title: json['title'] ?? '',
      content: json['content'],
      options: json['options'],
      answer: json['answer'],
      score: json['score'] ?? 10,
      timeLimit: json['timeLimit'],
      attachments: (json['attachments'] as List<dynamic>?)?.cast<String>(),
      tags: (json['tags'] as List<dynamic>?)?.cast<String>(),
      difficulty: json['difficulty'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'type': type.value,
      'title': title,
      'content': content,
      'options': options,
      'answer': answer,
      'score': score,
      'timeLimit': timeLimit,
      'attachments': attachments,
      'tags': tags,
      'difficulty': difficulty,
    };
  }
}

/// 场次状态枚举
enum SessionStatus {
  draft('draft', '草稿'),
  running('running', '进行中'),
  paused('paused', '已暂停'),
  finished('finished', '已结束');

  final String value;
  final String label;
  const SessionStatus(this.value, this.label);

  static SessionStatus fromValue(String value) {
    return SessionStatus.values.firstWhere(
      (e) => e.value == value,
      orElse: () => SessionStatus.draft,
    );
  }
}

/// 场次模型
class QuizSession {
  final int id;
  final String name;
  final String? description;
  final SessionStatus status;
  final Map<String, dynamic>? config;
  final String? startTime;
  final String? endTime;
  final int? questionCount;
  final int? participantCount;
  final int? createdBy;

  QuizSession({
    required this.id,
    required this.name,
    this.description,
    this.status = SessionStatus.draft,
    this.config,
    this.startTime,
    this.endTime,
    this.questionCount,
    this.participantCount,
    this.createdBy,
  });

  factory QuizSession.fromJson(Map<String, dynamic> json) {
    return QuizSession(
      id: json['id'] ?? 0,
      name: json['name'] ?? '',
      description: json['description'],
      status: SessionStatus.fromValue(json['status'] ?? 'draft'),
      config: json['config'],
      startTime: json['startTime'],
      endTime: json['endTime'],
      questionCount: json['questionCount'],
      participantCount: json['participantCount'],
      createdBy: json['createdBy'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'description': description,
      'status': status.value,
      'config': config,
      'startTime': startTime,
      'endTime': endTime,
      'questionCount': questionCount,
      'participantCount': participantCount,
      'createdBy': createdBy,
    };
  }
}

/// 队伍模型
class Team {
  final int id;
  final String name;
  final int? sessionId;
  final int score;
  final int correctCount;
  final int totalCount;
  final int? rank;

  Team({
    required this.id,
    required this.name,
    this.sessionId,
    this.score = 0,
    this.correctCount = 0,
    this.totalCount = 0,
    this.rank,
  });

  factory Team.fromJson(Map<String, dynamic> json) {
    return Team(
      id: json['id'] ?? 0,
      name: json['name'] ?? '',
      sessionId: json['sessionId'],
      score: json['score'] ?? 0,
      correctCount: json['correctCount'] ?? 0,
      totalCount: json['totalCount'] ?? 0,
      rank: json['rank'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'sessionId': sessionId,
      'score': score,
      'correctCount': correctCount,
      'totalCount': totalCount,
      'rank': rank,
    };
  }
}

/// 抢答记录模型
class BuzzLog {
  final int id;
  final int sessionId;
  final int questionId;
  final int teamId;
  final String? teamName;
  final int buzzTime;
  final bool isFirst;
  final bool processed;

  BuzzLog({
    required this.id,
    required this.sessionId,
    required this.questionId,
    required this.teamId,
    this.teamName,
    required this.buzzTime,
    this.isFirst = false,
    this.processed = false,
  });

  factory BuzzLog.fromJson(Map<String, dynamic> json) {
    return BuzzLog(
      id: json['id'] ?? 0,
      sessionId: json['sessionId'] ?? 0,
      questionId: json['questionId'] ?? 0,
      teamId: json['teamId'] ?? 0,
      teamName: json['teamName'],
      buzzTime: json['buzzTime'] ?? 0,
      isFirst: json['isFirst'] ?? false,
      processed: json['processed'] ?? false,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'sessionId': sessionId,
      'questionId': questionId,
      'teamId': teamId,
      'teamName': teamName,
      'buzzTime': buzzTime,
      'isFirst': isFirst,
      'processed': processed,
    };
  }
}

/// 答题记录模型
class AnswerLog {
  final int id;
  final int sessionId;
  final int questionId;
  final int teamId;
  final int? userId;
  final dynamic answer;
  final int score;
  final bool correct;
  final String? submitTime;

  AnswerLog({
    required this.id,
    required this.sessionId,
    required this.questionId,
    required this.teamId,
    this.userId,
    this.answer,
    this.score = 0,
    this.correct = false,
    this.submitTime,
  });

  factory AnswerLog.fromJson(Map<String, dynamic> json) {
    return AnswerLog(
      id: json['id'] ?? 0,
      sessionId: json['sessionId'] ?? 0,
      questionId: json['questionId'] ?? 0,
      teamId: json['teamId'] ?? 0,
      userId: json['userId'],
      answer: json['answer'],
      score: json['score'] ?? 0,
      correct: json['correct'] ?? false,
      submitTime: json['submitTime'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'sessionId': sessionId,
      'questionId': questionId,
      'teamId': teamId,
      'userId': userId,
      'answer': answer,
      'score': score,
      'correct': correct,
      'submitTime': submitTime,
    };
  }
}
