# 密码流程诊断指南

## 问题排查重点

### 1. 前端密码输入检查

**检查点：**
- ✓ 密码是否以明文形式发送到后端（正确做法）
- ✓ 密码是否被浏览器自动修改或加工
- ✓ 密码是否包含隐藏的空格或特殊字符

**前端API调用应该是：**
```json
POST /api/auth/login
{
  "username": "admin",
  "password": "admin123",
  "rememberMe": false
}
```

**注意：** 密码应该以原始明文形式发送。BCrypt加密在后端完成。

---

### 2. 后端加密比对逻辑检查

#### AuthService.java - 登录验证逻辑
**位置：** `src/main/java/com/qdq/service/AuthService.java`

**关键代码：**
```java
// 3. 校验密码
if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
    throw new BusinessException("用户名或密码错误");
}
```

**验证清单：**
- ✓ 使用 `BCryptPasswordEncoder` 的 `matches()` 方法
- ✓ 第一个参数是用户输入的明文密码
- ✓ 第二个参数是数据库中存储的哈希值

#### UserService.java - 密码创建和修改逻辑
**位置：** `src/main/java/com/qdq/service/UserService.java`

**密码创建：**
```java
user.setPasswordPlain(request.getPassword());
user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
```

**密码重置：**
```java
user.setPasswordPlain(newPassword);
user.setPasswordHash(passwordEncoder.encode(newPassword));
```

**验证清单：**
- ✓ 使用 `passwordEncoder.encode()` 生成哈希
- ✓ 同时设置 `passwordPlain` 和 `passwordHash`
- ✓ 所有密码操作都使用同一个 BCryptPasswordEncoder 实例

---

### 3. 数据库中的哈希值检查

#### 当前配置
- **BCrypt Cost Factor：** 12
- **哈希格式：** `$2a$12$...` (60字符)
- **算法版本：** BCrypt $2a$

#### 初始化管理员用户
**schema.sql 中的哈希值：**
```sql
INSERT INTO sys_user (username, password_hash, password_plain, name, status) VALUES
('admin', '$2a$12$R3h2cIPjj0yramPvVS9H2OPST9/PgBkqquzi.Ss7KIUgO2PKh2Gau', 'admin123', '系统管理员', 1);
```

**验证方法：**
1. 连接到MySQL数据库
2. 执行查询：
```sql
SELECT username, password_plain, password_hash FROM sys_user WHERE username = 'admin';
```

3. 确认：
   - `username`: admin
   - `password_plain`: admin123
   - `password_hash`: $2a$12$R3h2cIPjj0yramPvVS9H2OPST9/PgBkqquzi.Ss7KIUgO2PKh2Gau

#### 哈希值验证关系
```
明文密码: admin123
↓
BCryptPasswordEncoder(12).encode("admin123")
↓
哈希值: $2a$12$R3h2cIPjj0yramPvVS9H2OPST9/PgBkqquzi.Ss7KIUgO2PKh2Gau
↓
登录时验证: passwordEncoder.matches("admin123", 哈希值)
↓
结果: true (登录成功)
```

---

## 完整登录流程

### 流程图
```
前端输入
  ↓
发送请求: POST /api/auth/login
  {
    "username": "admin",
    "password": "admin123"
  }
  ↓
AuthController.login()
  ↓
AuthService.login()
  ├─ Step 1: 根据username查询用户
  │  sysUserMapper.selectByUsername("admin")
  │  返回: SysUser (包含password_hash)
  │
  ├─ Step 2: 检查用户状态
  │  if (user.getStatus() != 1) throw Exception
  │
  ├─ Step 3: 密码验证
  │  passwordEncoder.matches(
  │    "admin123",  // 用户输入
  │    "$2a$12$..."  // 数据库中的hash
  │  )
  │  ↓
  │  ✓ 返回 true → 继续
  │  ✗ 返回 false → 抛出异常
  │
  ├─ Step 4: 生成Token
  │  StpUtil.login(userId, rememberMe)
  │
  └─ Step 5: 返回响应
     LoginResponse {
       userId, username, token, roles, ...
     }
  ↓
前端保存Token，登录成功
```

---

## 故障排查步骤

### 1. 确认数据库状态
```bash
# 连接到数据库
mysql -u root -p quiz_competition

# 查询admin用户信息
SELECT id, username, password_plain, password_hash, status FROM sys_user WHERE username='admin';

# 如果没有找到，需要执行初始化脚本：
# mysql -u root -p quiz_competition < schema.sql
```

### 2. 测试密码加密
```bash
# 运行测试工具
cd D:\daima\qdq\backend
# 编译
mvn clean compile

# 运行测试类
java -cp target/classes com.qdq.util.PasswordTestUtil
```

**预期输出：**
```
=== 完整登录流程演示 ===
Step 1: 用户输入 - username: admin, password: admin123
Step 2: 数据库查询到哈希: $2a$12$R3h2cIPjj0yramPvVS9H2OPST9/PgBkqquzi.Ss7KIUgO2PKh2Gau
Step 3: 密码验证 - encoder.matches('admin123', '$2a$12$...')
Step 4: 验证结果: true

✓ 登录成功
```

### 3. 测试登录API
```bash
# 使用 curl 测试
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123",
    "rememberMe": false
  }'

# 预期响应：
# {
#   "code": 0,
#   "message": "登录成功",
#   "data": {
#     "userId": 1,
#     "username": "admin",
#     "name": "系统管理员",
#     "token": "...",
#     "tokenExpireTime": "...",
#     "roles": ["SUPER_ADMIN"],
#     "permissions": [...]
#   }
# }
```

### 4. 查看应用日志
**日志位置：** 
- 开发环境：控制台输出
- 文件日志：检查 `logs/` 目录

**关键日志信息：**
```
✓ 用户登录成功: admin
✗ 业务异常: 用户名或密码错误
✗ 业务异常: 账号已被禁用，请联系管理员
```

---

## 常见问题

### 问题1：明文密码和数据库哈希值不匹配
**原因：** 
- 新创建的用户密码哈希值与初始化脚本中的不一致
- 密码被多次编码

**解决方案：**
```sql
-- 重置admin密码
UPDATE sys_user 
SET password_hash = '$2a$12$R3h2cIPjj0yramPvVS9H2OPST9/PgBkqquzi.Ss7KIUgO2PKh2Gau',
    password_plain = 'admin123'
WHERE username = 'admin';
```

### 问题2：密码在创建时被正确加密，但登录时失败
**可能原因：**
- BCryptPasswordEncoder 配置不一致（cost factor 不同）
- 密码被修改但 `password_hash` 没有更新
- 用户状态被禁用 (`status = 0`)

**解决方案：**
1. 检查 `WebMvcConfig.java` 中 `passwordEncoder()` 方法
2. 确保 `return new BCryptPasswordEncoder(12)`
3. 检查用户状态: `SELECT status FROM sys_user WHERE username='admin'`

### 问题3：密码包含特殊字符无法登录
**可能原因：**
- 前端未正确编码特殊字符
- 密码中包含不可见字符（空格、制表符等）

**解决方案：**
1. 使用简单密码测试（如 `admin123`）
2. 查看应用日志中接收到的密码内容
3. 在后端添加密码长度和内容验证日志

---

## 配置清单

### ✓ 必须检查的配置

**1. WebMvcConfig.java**
```java
@Bean
public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);  // Cost factor 必须是 12
}
```

**2. AuthService.java**
```java
@Service
public class AuthService {
    private final BCryptPasswordEncoder passwordEncoder;  // 依赖注入
    
    public AuthService(SysUserMapper sysUserMapper, BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;  // 初始化
    }
    
    public LoginResponse login(LoginRequest request) {
        // 使用 passwordEncoder.matches()
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("用户名或密码错误");
        }
    }
}
```

**3. UserService.java**
```java
@Service
public class UserService extends ServiceImpl<SysUserMapper, SysUser> {
    private final BCryptPasswordEncoder passwordEncoder;  // 依赖注入
    
    public UserService(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;  // 初始化
    }
    
    public SysUser create(UserRequest request) {
        // 使用 passwordEncoder.encode()
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    }
}
```

**4. schema.sql 初始化数据**
```sql
INSERT INTO sys_user (username, password_hash, password_plain, name, status) VALUES
('admin', '$2a$12$R3h2cIPjj0yramPvVS9H2OPST9/PgBkqquzi.Ss7KIUgO2PKh2Gau', 'admin123', '系统管理员', 1);
```

---

## 验证清单

完成以下检查确保密码流程正确：

- [ ] BCryptPasswordEncoder bean 已在 WebMvcConfig 中创建（cost=12）
- [ ] AuthService 和 UserService 都注入了 passwordEncoder
- [ ] 所有密码验证使用 `passwordEncoder.matches()`
- [ ] 所有密码加密使用 `passwordEncoder.encode()`
- [ ] 数据库 admin 用户的哈希值是 `$2a$12$R3h2cIPjj0yramPvVS9H2OPST9/PgBkqquzi.Ss7KIUgO2PKh2Gau`
- [ ] admin 用户的 `status` 是 1（启用）
- [ ] 应用成功编译并启动
- [ ] 登录 API 返回 200 OK（不是 400/500 错误）
- [ ] 登录成功返回 token
- [ ] 登录失败返回"用户名或密码错误"

---

## 相关文件

| 文件 | 用途 | 位置 |
|------|------|------|
| PasswordTestUtil.java | 密码加密/验证测试工具 | `src/main/java/com/qdq/util/` |
| WebMvcConfig.java | BCryptPasswordEncoder bean 配置 | `src/main/java/com/qdq/config/` |
| AuthService.java | 登录验证逻辑 | `src/main/java/com/qdq/service/` |
| UserService.java | 用户创建、密码修改逻辑 | `src/main/java/com/qdq/service/` |
| schema.sql | 数据库初始化脚本 | `src/main/resources/db/` |
| update_admin_password.sql | 更新现有admin密码 | `src/main/resources/db/` |

