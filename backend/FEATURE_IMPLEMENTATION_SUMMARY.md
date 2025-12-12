# 题库管理系统功能实现总结

## 实现的功能清单

### 1. 题库管理功能 ✅

#### 1.1 题库基础操作
- **创建题库** - 支持创建新的题库
- **查询题库** - 分页查询、模糊搜索
- **更新题库** - 修改题库信息
- **删除题库** - 单个或批量删除
- **禁用/启用题库** - 支持禁用整个题库
- **批量禁用/启用** - 批量操作题库状态

**相关文件:**
- Entity: `QuizBank.java`
- Mapper: `QuizBankMapper.java`
- Service: `QuizBankService.java`
- Controller: `QuizBankController.java`
- DTO: `BankRequest.java`

**API Endpoints:**
```
GET    /api/banks                    - 分页查询题库
GET    /api/banks/{id}               - 获取题库详情
POST   /api/banks                    - 创建题库
PUT    /api/banks/{id}               - 更新题库
DELETE /api/banks/{id}               - 删除题库
DELETE /api/banks/batch              - 批量删除题库
POST   /api/banks/{id}/disable       - 禁用题库
POST   /api/banks/{id}/enable        - 启用题库
POST   /api/banks/batch/disable      - 批量禁用题库
POST   /api/banks/batch/enable       - 批量启用题库
```

---

### 2. 题目导入导出功能 ✅

#### 2.1 题目导入
- **Excel导入** - 支持从Excel文件导入题目
- **批量导入** - 自动处理多个题目
- **自动解析** - 自动识别题型、难度、选项等
- **关联题库** - 导入的题目自动关联到指定题库

#### 2.2 题目导出
- **模板下载** - 下载题目导入模板
- **格式规范** - 统一的Excel格式

#### 2.3 题目禁用/启用
- **单个禁用** - 禁用单个题目
- **批量禁用** - 批量禁用题目
- **单个启用** - 启用单个题目
- **批量启用** - 批量启用题目

**相关文件:**
- Service: `ImportExportService.java`
- DTO: `QuestionImportDTO.java`

**API Endpoints:**
```
GET    /api/questions/template              - 下载题目模板
POST   /api/questions/import                - 导入题目
POST   /api/questions/{id}/disable          - 禁用题目
POST   /api/questions/{id}/enable           - 启用题目
POST   /api/questions/batch/disable         - 批量禁用
POST   /api/questions/batch/enable          - 批量启用
DELETE /api/questions/{id}                  - 删除题目
DELETE /api/questions/batch                 - 批量删除题目
```

---

### 3. 题库高级管理 ✅

#### 3.1 题库状态管理
- 支持启用/禁用整个题库
- 支持批量操作
- 禁用题库自动更新题目状态

#### 3.2 题库统计
- 自动统计题库中的题目数量
- 提供题库基本统计信息

---

### 4. 音频/视频文件支持 ✅

#### 4.1 本地文件存储
- **音频上传** - 支持mp3、wav等音频格式
- **视频上传** - 支持mp4、avi等视频格式
- **图片上传** - 支持jpg、png等图片格式
- **文件验证** - 文件类型和大小验证
- **文件删除** - 支持文件删除

#### 4.2 新题目类型
- 类型6: 音频题目
- 类型7: 视频题目

**相关文件:**
- Entity: `SysFile.java`
- Mapper: `SysFileMapper.java`
- Service: `FileStorageService.java`
- Controller: `FileController.java`

**API Endpoints:**
```
POST   /api/files/audio/upload       - 上传音频文件
POST   /api/files/video/upload       - 上传视频文件
POST   /api/files/image/upload       - 上传图片文件
GET    /api/files/{fileId}/download  - 下载文件
DELETE /api/files/{fileId}           - 删除文件
GET    /api/files/{fileId}           - 获取文件信息
```

---

### 5. 排行版管理 ✅

#### 5.1 排行版配置
- **创建排行版** - 为每场比赛创建排行版配置
- **自定义名称** - 管理员可修改排行版名称
- **自定义字段** - 可选择显示的字段
- **排序方式** - 支持按得分或答题数排序
- **实时更新** - 排行版实时更新并同步到用户端

#### 5.2 排行版数据
- 显示参赛者排名、得分、正确题数等
- 按配置的排序方式自动排序
- 实时计算排名

**相关文件:**
- Entity: `LeaderboardConfig.java`
- Mapper: `LeaderboardConfigMapper.java`
- Service: `LeaderboardService.java`
- Controller: `LeaderboardController.java`
- DTO: `LeaderboardRequest.java`

**API Endpoints:**
```
POST   /api/leaderboards                         - 创建/更新排行版
GET    /api/leaderboards/session/{sessionId}    - 获取排行版配置
GET    /api/leaderboards/session/{sessionId}/data - 获取实时排行版数据
PUT    /api/leaderboards/session/{sessionId}/name - 更新排行版名称
PUT    /api/leaderboards/session/{sessionId}/sort-type - 更新排序方式
DELETE /api/leaderboards/session/{sessionId}    - 删除排行版配置
```

---

### 6. 比赛倒计时功能 ✅

#### 6.1 开始前倒计时
- **自定义倒计时** - 支持自定义开始前倒计时秒数(默认10秒)
- **实时广播** - 倒计时信息实时广播到选手端
- **自动开始** - 倒计时结束自动开始答题
- **配置管理** - 系统配置中管理倒计时时长

#### 6.2 倒计时状态管理
- 缓存倒计时信息到Redis
- 支持查询当前倒计时状态

**相关方法:**
- `SessionService.startWithCountdown()` - 带倒计时的开始比赛
- `SessionService.getCountdownSeconds()` - 获取倒计时秒数
- `WebSocketMessageService.broadcastCountdown()` - 广播倒计时

---

### 7. 答题进度实时展示 ✅

#### 7.1 进度追踪
- **实时追踪** - 追踪每个选手/队伍的答题进度
- **进度统计** - 统计已答题数、正确数、错误数等
- **实时更新** - 答题进度实时更新到选手端

#### 7.2 进度数据结构
```json
{
  "userId": 1,
  "answeredCount": 5,
  "timestamp": 1702300000000
}
```

**相关方法:**
- `SessionService.updateAnswerProgress()` - 更新答题进度
- `SessionService.getAnswerProgressMap()` - 获取整体进度
- `WebSocketMessageService.broadcastAnswerProgress()` - 广播答题进度

---

### 8. 数据库扩展 ✅

#### 8.1 新增表
1. **quiz_bank** - 题库表
   - 题库信息、状态、禁用标记

2. **quiz_leaderboard_config** - 排行版配置表
   - 排行版名称、显示字段、排序方式

3. **sys_file** - 文件记录表
   - 文件元数据、存储路径、上传人

#### 8.2 修改的表
1. **quiz_question** - 题目表
   - 新增字段: `bank_id`(题库ID)、`is_disabled`(禁用标记)
   - 修改字段: `type`(支持音频和视频类型)

2. **quiz_session** - 比赛场次表
   - 新增字段: `countdown_seconds`(倒计时秒数)

3. **quiz_session_participant** - 参赛者表
   - 新增字段: `answered_count`(已答题数)

---

### 9. WebSocket实时通信 ✅

#### 9.1 新增事件类型
- `leaderboard_update` - 排行版更新
- `answer_progress_update` - 答题进度更新
- `leaderboard_config_update` - 排行版配置更新

#### 9.2 广播方法
- `broadcastLeaderboard()` - 广播排行版数据
- `broadcastAnswerProgress()` - 广播答题进度
- `broadcastLeaderboardConfig()` - 广播排行版配置更新

---

## 完整的功能流程

### 题库管理流程
1. 管理员创建题库
2. 通过导入或逐个创建题目
3. 禁用/启用题库或单个题目
4. 组织题库进行比赛

### 比赛流程
1. 创建比赛场次，关联题库中的题目
2. 为场次创建排行版配置(可选)
3. 初始化倒计时(默认10秒)
4. 选手连接，进行倒计时
5. 倒计时结束，自动开始答题
6. 实时显示答题进度和排行版
7. 比赛结束，查看最终排行版

---

## 数据库初始化

系统初始化时自动创建:
- 默认题库 - "默认题库"
- 系统配置 - 倒计时秒数、文件上传路径等

---

## 关键特性

✅ **模块化设计** - 各个功能独立且可扩展
✅ **实时同步** - WebSocket实时广播数据
✅ **事务支持** - 关键操作支持事务控制
✅ **错误处理** - 统一的异常处理
✅ **权限控制** - 基于角色的访问控制
✅ **缓存优化** - 使用Redis缓存重要数据
✅ **批量操作** - 支持批量删除、禁用等操作

---

## 技术栈

- **框架**: Spring Boot 3.2.0
- **ORM**: MyBatis-Plus 3.5.5
- **缓存**: Redis
- **WebSocket**: Spring WebSocket
- **文件处理**: EasyExcel 3.3.3
- **权限认证**: Sa-Token 1.37.0
- **工具库**: Hutool 5.8.24

---

## 配置示例

### application.yml
```yaml
quiz:
  upload:
    path: ./uploads
  countdown:
    default: 10  # 默认倒计时10秒
```

### 数据库配置
所有新增表自动创建，见 `schema.sql`

---

## 使用示例

### 导入题目
```bash
POST /api/questions/import
Content-Type: multipart/form-data

file: [Excel文件]
bankId: 1  # 关联题库ID
```

### 启用排行版
```bash
POST /api/leaderboards
Content-Type: application/json

{
  "sessionId": 1,
  "leaderboardName": "实时排行版",
  "sortType": 1
}
```

### 获取排行版数据
```bash
GET /api/leaderboards/session/1/data
```

---

## 注意事项

1. **文件存储** - 文件保存在 `./uploads` 目录，需确保目录可写
2. **倒计时** - 默认10秒，可通过SessionService.startWithCountdown()自定义
3. **排行版** - 每个Session最多一个排行版配置
4. **权限** - 大部分管理操作需要SUPER_ADMIN或HOST角色

---

## 后续优化建议

1. 支持更多文件格式和处理方式
2. 排行版支持更复杂的统计和过滤
3. 支持题库版本管理
4. 支持题目难度和分值的自动调整
5. 提供更详细的答题统计报告

