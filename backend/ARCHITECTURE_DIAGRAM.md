# 密码流程架构图

## 1. 系统架构概览

```
┌─────────────────────────────────────────────────────────────────┐
│                         前端应用                                 │
│                                                                  │
│  用户输入密码: "admin123"                                       │
│          ↓                                                      │
│  发送请求: POST /api/auth/login                                │
│  请求体: {"username": "admin", "password": "admin123"}         │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                         后端应用                                 │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ AuthController.login()                                   │  │
│  │  └─→ AuthService.login(LoginRequest)                     │  │
│  │       ├─ Step 1: sysUserMapper.selectByUsername()        │  │
│  │       │            查询用户，获取 password_hash           │  │
│  │       │                                                   │  │
│  │       ├─ Step 2: 检查 user.status == 1                   │  │
│  │       │                                                   │  │
│  │       ├─ Step 3: passwordEncoder.matches(               │  │
│  │       │             "admin123",                          │  │
│  │       │             "$2a$12$..."                         │  │
│  │       │          )                                        │  │
│  │       │          ✓ true → 继续                            │  │
│  │       │          ✗ false → 异常                           │  │
│  │       │                                                   │  │
│  │       ├─ Step 4: StpUtil.login() 生成 Token              │  │
│  │       │                                                   │  │
│  │       ├─ Step 5: 更新 lastLoginTime                      │  │
│  │       │                                                   │  │
│  │       ├─ Step 6: 查询用户角色和权限                       │  │
│  │       │                                                   │  │
│  │       └─ Step 7: 返回 LoginResponse                       │  │
│  │                  {token, userId, roles, ...}             │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                       关键配置                                   │
│                                                                  │
│  WebMvcConfig.passwordEncoder()                                │
│  └─→ new BCryptPasswordEncoder(12)                             │
│      Cost Factor = 12 (安全强度)                               │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                       MySQL 数据库                               │
│                                                                  │
│  sys_user 表:                                                   │
│  ┌────────────────────────────────────────────────────────┐    │
│  │ id | username | password_hash      | password_plain     │    │
│  ├────────────────────────────────────────────────────────┤    │
│  │ 1  | admin    | $2a$12$R3h2c... | admin123           │    │
│  └────────────────────────────────────────────────────────┘    │
│                                                                  │
│  记录关键字段:                                                 │
│  - password_hash: BCrypt哈希值 (60字符)                        │
│  - password_plain: 明文密码 (用于显示)                         │
│  - status: 1 (启用) 或 0 (禁用)                               │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                       返回响应                                   │
│                                                                  │
│  成功: HTTP 200 OK                                              │
│  {                                                              │
│    "code": 0,                                                   │
│    "message": "登录成功",                                       │
│    "data": {                                                    │
│      "userId": 1,                                               │
│      "username": "admin",                                        │
│      "token": "eyJ...",                                          │
│      "roles": ["SUPER_ADMIN"],                                   │
│      ...                                                        │
│    }                                                            │
│  }                                                              │
│                                                                  │
│  失败: HTTP 200 (返回错误信息)                                   │
│  {                                                              │
│    "code": -1,                                                  │
│    "message": "业务异常: 用户名或密码错误",                    │
│    "data": null                                                 │
│  }                                                              │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. 密码加密流程

```
用户设置密码
    ↓
输入明文密码: "admin123"
    ↓
UserService.create() / resetPassword()
    ├─ passwordPlain = "admin123"
    └─ passwordHash = passwordEncoder.encode("admin123")
    
BCryptPasswordEncoder.encode("admin123")
    ↓
随机生成 salt (成本因子=12)
    ↓
使用 BCrypt 算法加密
    ↓
生成哈希值: $2a$12$R3h2cIPjj0yramPvVS9H2OPST9/PgBkqquzi.Ss7KIUgO2PKh2Gau
    ↓
存储到数据库
    ├─ password_hash 字段 (用于验证)
    └─ password_plain 字段 (用于显示)
```

---

## 3. 密码验证流程

```
用户登录
    ↓
输入密码: "admin123"
    ↓
POST /api/auth/login
{
  "username": "admin",
  "password": "admin123"
}
    ↓
AuthService.login()
    ├─ 从数据库查询 password_hash
    │  获取: $2a$12$R3h2cIPjj0yramPvVS9H2OPST9/PgBkqquzi.Ss7KIUgO2PKh2Gau
    │
    └─ passwordEncoder.matches(输入密码, 存储的哈希)
       
BCryptPasswordEncoder.matches("admin123", "$2a$12$...")
    ↓
使用存储的 salt 重新计算哈希
    ↓
比较两个哈希值
    ↓
✓ 相同 → 返回 true → 登录成功
✗ 不同 → 返回 false → 抛出异常
```

---

## 4. 关键代码位置

```
项目结构
└── src/main/java/com/qdq/
    ├── config/
    │   └── WebMvcConfig.java
    │       └── @Bean passwordEncoder()
    │
    ├── controller/
    │   └── AuthController.java
    │       └── @PostMapping("/login")
    │
    ├── service/
    │   ├── AuthService.java
    │   │   └── login(LoginRequest)
    │   │       └── passwordEncoder.matches()
    │   │
    │   └── UserService.java
    │       ├── create(UserRequest)
    │       │   └── passwordEncoder.encode()
    │       │
    │       ├── update(Long id, UserRequest)
    │       │   └── passwordEncoder.encode()
    │       │
    │       └── resetPassword(Long id, String)
    │           └── passwordEncoder.encode()
    │
    ├── dto/
    │   ├── LoginRequest.java
    │   │   ├── username: String
    │   │   └── password: String (明文)
    │   │
    │   └── UserRequest.java
    │       └── password: String (明文)
    │
    ├── entity/
    │   └── SysUser.java
    │       ├── passwordHash: String (BCrypt哈希)
    │       └── passwordPlain: String (明文显示)
    │
    └── util/
        └── PasswordTestUtil.java
            └── main() 测试密码流程

└── src/main/resources/
    └── db/
        ├── schema.sql
        │   └── INSERT INTO sys_user ... password_hash='$2a$12$...'
        │
        └── application.yml
            └── logging.level

└── 根目录
    ├── update_admin_password.sql
    ├── PASSWORD_DEBUG_GUIDE.md
    ├── PASSWORD_FLOW_CHECKLIST.md
    └── QUICK_FIX_REFERENCE.txt
```

---

## 5. 密码对比关系图

```
BCrypt 密码对比算法:

第一次运行 (创建用户):
  input:      "admin123"
              + salt (随机生成, 16字节)
              + cost=12 (迭代次数: 2^12=4096)
                ↓
              BCrypt.hashpw()
                ↓
            Output: $2a$12$R3h2cIPjj0yramPvVS9H2...
                ↓
            存储到数据库

第二次运行 (登录验证):
  input:      "admin123"
              + hash from DB: $2a$12$R3h2cIPjj0yramPvVS9H2...
                ↓
              BCrypt.checkpw() / matches()
                ↓
              提取 salt 和 cost 参数
                ↓
              重新计算哈希值
                ↓
              比较: 新哈希 == 数据库哈希 ?
                ↓
            ✓ YES → true  (登录成功)
            ✗ NO  → false (登录失败)

关键特点:
- Salt 存储在哈希值中 ($2a$12$salt$hash)
- 每次哈希可能不同，但验证总是相同
- Cost=12 意味着非常安全 (4096次迭代)
- 无法反向解密 (单向函数)
```

---

## 6. 错误流程

```
异常处理流程:

登录请求
    ↓
AuthService.login()
    │
    ├─ 用户不存在
    │   └─→ throw BusinessException("用户名或密码错误")
    │       ↓
    │   GlobalExceptionHandler.handlerBusinessException()
    │       ↓
    │   返回: {"code": -1, "message": "业务异常: 用户名或密码错误"}
    │
    ├─ 用户被禁用 (status != 1)
    │   └─→ throw BusinessException("账号已被禁用，请联系管理员")
    │       ↓
    │   返回: {"code": -1, "message": "业务异常: 账号已被禁用..."}
    │
    └─ 密码错误
        └─→ !passwordEncoder.matches() 返回 false
            ↓
        throw BusinessException("用户名或密码错误")
            ↓
        返回: {"code": -1, "message": "业务异常: 用户名或密码错误"}
```

---

## 7. 依赖注入关系

```
Spring 容器启动:
    ↓
WebMvcConfig
    ├─ @Bean passwordEncoder()
    │   └─→ BCryptPasswordEncoder(12) 实例
    │
    ├─ 注入到 AuthService
    │   └─→ AuthService(sysUserMapper, passwordEncoder)
    │
    └─ 注入到 UserService
        └─→ UserService(passwordEncoder)

运行时:
    AuthService.login()
        └─→ this.passwordEncoder.matches()
    
    UserService.create()
        └─→ this.passwordEncoder.encode()
```

---

## 8. 数据流向

```
用户层
  │
  ├─→ 前端 UI (输入密码)
  │     ↓
  ├─→ HTTP 请求 (HTTPS 加密传输)
  │     ↓
  ├─→ AuthController
  │     ↓
  ├─→ AuthService
  │     ├─ DatabaseLayer (查询用户)
  │     │   └─ SysUserMapper.selectByUsername()
  │     │       └─ MySQL (password_hash)
  │     │
  │     └─ PasswordValidation
  │         └─ BCryptPasswordEncoder.matches()
  │             ├─ Input: 用户输入的明文密码
  │             └─ Hash: 数据库中的BCrypt哈希
  │
  ├─→ Token 生成 (Sa-Token)
  │     ↓
  ├─→ HTTP 响应 (返回 Token)
  │     ↓
  └─→ 前端保存 Token (localStorage/sessionStorage)
```

---

## 9. 测试流程

```
PasswordTestUtil.main()
    ↓
步骤 1: 创建 BCryptPasswordEncoder(12)
    ↓
步骤 2: 加密密码
    input: "admin123"
    output: $2a$12$... (随机生成)
    ↓
步骤 3: 验证加密的密码
    matches("admin123", output) → true ✓
    ↓
步骤 4: 验证错误密码
    matches("wrongpassword", output) → false ✓
    ↓
步骤 5: 验证数据库中的哈希值
    matches("admin123", "$2a$12$R3h2cIPjj0yramPvVS9H2OPST9/PgBkqquzi.Ss7KIUgO2PKh2Gau")
    → true ✓ (说明数据库哈希值正确)
    ↓
步骤 6: 完整流程演示
    模拟登录过程 → 输出结果
    ↓
最终: 如果所有测试都通过，输出"✓ 登录成功"
```

---

## 10. 文件修改总结

```
修改项目:

✓ pom.xml
  旧: <dependency>org.mindrot:jbcrypt</dependency>
  新: <dependency>org.springframework.security:spring-security-crypto</dependency>

✓ WebMvcConfig.java
  增: @Bean passwordEncoder() { return new BCryptPasswordEncoder(12); }

✓ AuthService.java
  改: import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
  改: private final BCryptPasswordEncoder passwordEncoder;
  改: 构造器添加 passwordEncoder 参数
  改: passwordEncoder.matches() 替代旧的 BCrypt.checkpw()
  增: 详细的日志记录

✓ UserService.java
  改: import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
  改: private final BCryptPasswordEncoder passwordEncoder;
  改: 构造器添加 passwordEncoder 参数
  改: passwordEncoder.encode() 替代旧的 BCrypt.hashpw()

✓ schema.sql
  改: admin 用户的 password_hash 更新为有效的BCrypt哈希值

+ PasswordTestUtil.java (新建)
  用途: 测试密码加密和验证流程

+ PASSWORD_DEBUG_GUIDE.md (新建)
  用途: 详细的故障排查指南

+ PASSWORD_FLOW_CHECKLIST.md (新建)
  用途: 完整的检查清单

+ QUICK_FIX_REFERENCE.txt (新建)
  用途: 快速参考指南
```

