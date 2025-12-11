-- ============================
-- 知识竞赛抢答系统 - 数据库初始化脚本
-- ============================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS quiz_competition 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE quiz_competition;

-- ============================
-- 1. 用户与权限模块
-- ============================

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    description VARCHAR(255) COMMENT '角色描述',
    status TINYINT DEFAULT 1 COMMENT '状态(0:禁用 1:启用)',
    sort INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父权限ID',
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    permission_code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
    type TINYINT DEFAULT 1 COMMENT '类型(1:菜单 2:按钮 3:接口)',
    path VARCHAR(255) COMMENT '路由路径',
    icon VARCHAR(100) COMMENT '图标',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    UNIQUE KEY uk_role_permission (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(64) NOT NULL UNIQUE COMMENT '用户名',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    password_plain VARCHAR(100) COMMENT '密码明文(用于查看)',
    name VARCHAR(100) COMMENT '姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    avatar VARCHAR(255) COMMENT '头像URL',
    gender TINYINT DEFAULT 0 COMMENT '性别(0:未知 1:男 2:女)',
    status TINYINT DEFAULT 1 COMMENT '状态(0:禁用 1:启用)',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ============================
-- 2. 题库模块
-- ============================

-- 题目分类表
CREATE TABLE IF NOT EXISTS quiz_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    description VARCHAR(255) COMMENT '描述',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目分类表';

-- 题目表
CREATE TABLE IF NOT EXISTS quiz_question (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '题目ID',
    category_id BIGINT COMMENT '分类ID',
    type TINYINT NOT NULL COMMENT '题型(1:单选 2:多选 3:判断 4:填空 5:主观)',
    title VARCHAR(500) NOT NULL COMMENT '题目标题',
    content TEXT COMMENT '题目内容(富文本)',
    options JSON COMMENT '选项(JSON数组)',
    answer JSON COMMENT '答案(JSON)',
    analysis TEXT COMMENT '答案解析',
    score INT DEFAULT 10 COMMENT '分值',
    difficulty TINYINT DEFAULT 2 COMMENT '难度(1:简单 2:中等 3:困难)',
    tags JSON COMMENT '标签(JSON数组)',
    attachments JSON COMMENT '附件(图片/音频/视频)',
    status TINYINT DEFAULT 1 COMMENT '状态(0:草稿 1:已发布 2:待审核)',
    created_by BIGINT COMMENT '创建人',
    updated_by BIGINT COMMENT '更新人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_category (category_id),
    INDEX idx_type (type),
    INDEX idx_difficulty (difficulty),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目表';

-- ============================
-- 3. 比赛场次模块
-- ============================

-- 队伍/团队表
CREATE TABLE IF NOT EXISTS quiz_team (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '队伍ID',
    name VARCHAR(100) NOT NULL COMMENT '队伍名称',
    logo VARCHAR(255) COMMENT '队伍Logo',
    description VARCHAR(500) COMMENT '队伍描述',
    captain_id BIGINT COMMENT '队长用户ID',
    status TINYINT DEFAULT 1 COMMENT '状态',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='队伍表';

-- 队伍成员表
CREATE TABLE IF NOT EXISTS quiz_team_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    team_id BIGINT NOT NULL COMMENT '队伍ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role TINYINT DEFAULT 0 COMMENT '角色(0:成员 1:队长)',
    joined_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    UNIQUE KEY uk_team_user (team_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='队伍成员表';

-- 比赛场次表
CREATE TABLE IF NOT EXISTS quiz_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '场次ID',
    name VARCHAR(200) NOT NULL COMMENT '场次名称',
    description TEXT COMMENT '场次描述',
    status TINYINT DEFAULT 0 COMMENT '状态(0:草稿 1:待开始 2:进行中 3:暂停 4:已结束)',
    mode TINYINT DEFAULT 1 COMMENT '模式(1:个人赛 2:团队赛)',
    config JSON COMMENT '场次配置(JSON)',
    question_ids JSON COMMENT '题目ID列表(JSON数组)',
    current_question_index INT DEFAULT -1 COMMENT '当前题目索引',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    scheduled_start DATETIME COMMENT '计划开始时间',
    created_by BIGINT COMMENT '创建人',
    updated_by BIGINT COMMENT '更新人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_status (status),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='比赛场次表';

-- 场次参赛者表
CREATE TABLE IF NOT EXISTS quiz_session_participant (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL COMMENT '场次ID',
    user_id BIGINT COMMENT '用户ID(个人赛)',
    team_id BIGINT COMMENT '队伍ID(团队赛)',
    total_score INT DEFAULT 0 COMMENT '总得分',
    correct_count INT DEFAULT 0 COMMENT '正确题数',
    wrong_count INT DEFAULT 0 COMMENT '错误题数',
    buzz_count INT DEFAULT 0 COMMENT '抢答次数',
    buzz_success_count INT DEFAULT 0 COMMENT '抢答成功次数',
    rank INT COMMENT '排名',
    joined_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    INDEX idx_session (session_id),
    INDEX idx_score (total_score DESC),
    CONSTRAINT fk_session_id FOREIGN KEY (session_id) REFERENCES quiz_session(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场次参赛者表';

-- ============================
-- 4. 抢答与答题记录
-- ============================

-- 抢答记录表
CREATE TABLE IF NOT EXISTS quiz_buzz_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    session_id BIGINT NOT NULL COMMENT '场次ID',
    question_id BIGINT NOT NULL COMMENT '题目ID',
    user_id BIGINT COMMENT '用户ID',
    team_id BIGINT COMMENT '队伍ID',
    buzz_time DATETIME(3) NOT NULL COMMENT '抢答时间(毫秒精度)',
    server_time BIGINT NOT NULL COMMENT '服务器时间戳(毫秒)',
    is_first TINYINT DEFAULT 0 COMMENT '是否第一个(0:否 1:是)',
    rank INT COMMENT '抢答排名',
    processed TINYINT DEFAULT 0 COMMENT '是否已处理',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_session_question (session_id, question_id),
    INDEX idx_buzz_time (buzz_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抢答记录表';

-- 答题记录表
CREATE TABLE IF NOT EXISTS quiz_answer_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    session_id BIGINT NOT NULL COMMENT '场次ID',
    question_id BIGINT NOT NULL COMMENT '题目ID',
    user_id BIGINT COMMENT '用户ID',
    team_id BIGINT COMMENT '队伍ID',
    answer JSON COMMENT '提交的答案',
    is_correct TINYINT COMMENT '是否正确(0:错误 1:正确 NULL:待评判)',
    score INT DEFAULT 0 COMMENT '得分',
    time_spent INT COMMENT '答题用时(秒)',
    judge_user_id BIGINT COMMENT '评判人ID(主观题)',
    judge_comment VARCHAR(500) COMMENT '评判备注',
    submit_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    judge_time DATETIME COMMENT '评判时间',
    INDEX idx_session_question (session_id, question_id),
    INDEX idx_user (user_id),
    INDEX idx_team (team_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='答题记录表';

-- ============================
-- 5. 系统通知
-- ============================

-- 通知表
CREATE TABLE IF NOT EXISTS sys_notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '通知ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT COMMENT '内容',
    type TINYINT DEFAULT 1 COMMENT '类型(1:系统通知 2:比赛通知)',
    target_type TINYINT DEFAULT 0 COMMENT '目标类型(0:全部 1:指定用户)',
    target_users JSON COMMENT '目标用户ID列表',
    created_by BIGINT COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- 用户通知状态表
CREATE TABLE IF NOT EXISTS sys_user_notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    notification_id BIGINT NOT NULL COMMENT '通知ID',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读',
    read_time DATETIME COMMENT '阅读时间',
    UNIQUE KEY uk_user_notification (user_id, notification_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户通知状态表';

-- ============================
-- 6. 系统配置
-- ============================

-- 系统配置表
CREATE TABLE IF NOT EXISTS sys_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_type VARCHAR(50) COMMENT '配置类型',
    description VARCHAR(255) COMMENT '描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(64) COMMENT '用户名',
    module VARCHAR(50) COMMENT '操作模块',
    operation VARCHAR(100) COMMENT '操作内容',
    method VARCHAR(200) COMMENT '请求方法',
    request_url VARCHAR(255) COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数',
    response_data TEXT COMMENT '响应数据',
    ip VARCHAR(50) COMMENT 'IP地址',
    status TINYINT DEFAULT 1 COMMENT '状态(0:失败 1:成功)',
    error_msg TEXT COMMENT '错误信息',
    time_cost BIGINT COMMENT '耗时(毫秒)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ============================
-- 初始化数据
-- ============================

-- 初始化角色
INSERT INTO sys_role (role_name, role_code, description, sort) VALUES
('超级管理员', 'SUPER_ADMIN', '系统超级管理员，拥有所有权限', 1),
('主持人', 'HOST', '比赛主持人，控制比赛流程', 2),
('评委', 'JUDGE', '比赛评委，评判主观题', 3),
('选手', 'PLAYER', '参赛选手', 4),
('观众', 'VIEWER', '观看比赛', 5);

-- 初始化超级管理员用户 (密码: admin123, Spring Security BCrypt hash with cost 12)
INSERT INTO sys_user (username, password_hash, password_plain, name, status) VALUES
('admin', '$2a$12$gSvqqUPYvJEFO.lkV4dPze6B.bD6qJEhOlpAiNzU6cWVAGPuqz5uC', 'admin123', '系统管理员', 1);

-- 关联超级管理员角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 初始化系统配置
INSERT INTO sys_config (config_key, config_value, config_type, description) VALUES
('buzz_timeout_ms', '100', 'number', '抢答超时时间(毫秒)'),
('question_time_limit', '30', 'number', '默认答题时限(秒)'),
('score_correct', '10', 'number', '答对得分'),
('score_wrong', '0', 'number', '答错得分'),
('score_timeout', '0', 'number', '超时得分'),
('enable_buzz_sound', 'true', 'boolean', '启用抢答音效'),
('max_team_members', '5', 'number', '队伍最大人数');

-- 初始化题目分类
INSERT INTO quiz_category (name, description, sort) VALUES
('综合知识', '综合类知识题目', 1),
('科学技术', '科学技术类题目', 2),
('历史文化', '历史文化类题目', 3),
('时事政治', '时事政治类题目', 4),
('文学艺术', '文学艺术类题目', 5);
