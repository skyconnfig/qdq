# 🔴 紧急修复 - 密码验证失败

## 问题现象

```
DEBUG - 密码验证 - username: admin, match: false
WARN - 登录失败 - 密码错误: admin
```

**原因**: 数据库中的密码哈希值与 `BCryptPasswordEncoder(12)` 生成的哈希值不匹配

---

## ✅ 立即修复（4 步）

### 步骤 1: 重新编译应用
```bash
cd D:\daima\qdq\backend
mvn clean compile
```

### 步骤 2: 更新数据库密码哈希

执行以下 SQL 命令（选择一个）：

**选项 A - 使用 SQL 脚本**
```bash
mysql -u root -p quiz_competition < update_admin_password.sql
```

**选项 B - 直接执行 SQL**
```sql
UPDATE sys_user 
SET password_hash = '$2a$12$gSvqqUPYvJEFO.lkV4dPze6B.bD6qJEhOlpAiNzU6cWVAGPuqz5uC',
    password_plain = 'admin123'
WHERE username = 'admin';
```

**选项 C - 验证更新**
```sql
SELECT id, username, password_plain, password_hash, status FROM sys_user WHERE username = 'admin';
```

预期结果：
- `password_hash` 应该是：`$2a$12$gSvqqUPYvJEFO.lkV4dPze6B.bD6qJEhOlpAiNzU6cWVAGPuqz5uC`
- `password_plain` 应该是：`admin123`
- `status` 应该是：`1`

### 步骤 3: 重启应用
```bash
# 停止当前运行的应用（Ctrl+C）
# 重新启动
mvn spring-boot:run
```

### 步骤 4: 测试登录
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123","rememberMe":false}'
```

**成功响应**:
```json
{
  "code": 0,
  "message": "登录成功",
  "data": {
    "userId": 1,
    "username": "admin",
    "token": "...",
    "roles": ["SUPER_ADMIN"]
  }
}
```

---

## 🔍 为什么会这样？

### BCrypt 哈希的特性

BCrypt 每次生成的哈希都是**不同的**（包含随机盐值），但都能验证**同一个密码**：

```
密码: "admin123"
  ↓
第一次加密: $2a$12$aaaa...(随机盐1)
第二次加密: $2a$12$bbbb...(随机盐2)
第三次加密: $2a$12$cccc...(随机盐3)
  ↓
都能验证密码 "admin123" ✓
```

之前使用的哈希值与现在 `BCryptPasswordEncoder(12)` 生成的哈希值不兼容，所以需要更新。

---

## ⚠️ 如果还是不行

### 调试步骤

**Step 1: 检查数据库中的哈希值**
```sql
SELECT password_hash, LENGTH(password_hash) FROM sys_user WHERE username='admin';
```

应该看到：
- `password_hash` = `$2a$12$gSvqqUPYvJEFO.lkV4dPze6B.bD6qJEhOlpAiNzU6cWVAGPuqz5uC`
- `LENGTH` = 60

**Step 2: 检查应用日志**
```
应该看到: DEBUG - 密码验证 - username: admin, match: true
不应该看到: WARN - 登录失败 - 密码错误: admin
```

**Step 3: 运行测试工具**
```bash
mvn clean compile
java -cp target/classes com.qdq.util.PasswordTestUtil
```

应该输出：
```
✓ 登录成功
```

**Step 4: 再次更新哈希值**

如果问题仍然存在，数据库的密码哈希可能被旧的值覆盖了。重新执行：

```bash
mysql -u root -p quiz_competition < update_admin_password.sql
```

---

## 🎯 关键要点

| 项目 | 值 | 说明 |
|------|-----|------|
| **密码** | `admin123` | 保持不变 |
| **新哈希值** | `$2a$12$gSvqqUPYvJEFO.lkV4dPze6B.bD6qJEhOlpAiNzU6cWVAGPuqz5uC` | 与 BCryptPasswordEncoder(12) 兼容 |
| **哈希长度** | 60 字符 | 标准 BCrypt 格式 |
| **用户状态** | 1 (启用) | 必须是 1，不能是 0 |
| **数据库** | `sys_user` 表 | `username='admin'` |

---

## 📝 修改文件

本次修复更新了：
- ✅ `update_admin_password.sql` - 更新为正确的哈希值
- ✅ `schema.sql` - 更新初始化数据中的哈希值

---

## 🎉 完成！

一旦看到类似日志就说明成功了：

```
2025-12-11 12:32:57 [http-nio-8080-exec-2] DEBUG com.qdq.service.AuthService - 密码验证 - username: admin, match: true
2025-12-11 12:32:57 [http-nio-8080-exec-2] INFO  com.qdq.service.AuthService - 用户登录成功: admin
```

登录成功！🎊

