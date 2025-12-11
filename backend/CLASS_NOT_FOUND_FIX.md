# ✅ ClassNotFoundException 修复

## 问题

```
错误: 找不到或无法加载主类 com.qdq.QuizCompetitionApplication
原因: java.lang.ClassNotFoundException: com.qdq.QuizCompetitionApplication
```

## 原因

IntelliJ 没有正确编译类文件到 `target/classes` 目录。直接运行导致无法找到主类。

---

## ✅ 解决方案

### 方法 1: 使用自动化脚本 (推荐) ⭐

在项目根目录运行：

**Windows (批处理)**:
```bash
RUN_APPLICATION.bat
```

**Windows (PowerShell)**:
```powershell
.\RUN_APPLICATION.ps1
```

这会自动：
1. ✅ 清理旧的编译输出
2. ✅ 重新编译应用
3. ✅ 打包为 JAR 文件
4. ✅ 运行应用

### 方法 2: 手动执行 Maven 命令

```bash
# 完整重建
cd D:\daima\qdq\backend
mvn clean package -DskipTests

# 运行 JAR
java -jar target/quiz-competition-1.0.0.jar
```

### 方法 3: 在 IntelliJ 中修复

1. **清理 IntelliJ 缓存**:
   - File → Invalidate Caches... → 勾选 "Clear file system cache and Local History"
   - 点击 "Invalidate and Restart"

2. **重新编译**:
   - Build → Clean Project
   - Build → Rebuild Project

3. **运行应用**:
   - 右键点击 `QuizCompetitionApplication.java`
   - 选择 "Run 'QuizCompetitionApplication.main()'"

---

## 📋 完整步骤

### Step 1: 编译应用

使用脚本或命令：
```bash
mvn clean package -DskipTests
```

**预期输出**:
```
[INFO] BUILD SUCCESS
[INFO] jar -jar target/quiz-competition-1.0.0.jar
```

### Step 2: 运行应用

```bash
java -jar target/quiz-competition-1.0.0.jar
```

**预期输出**:
```
===============================================
    知识竞赛抢答系统启动成功！
    访问地址: http://localhost:8080
===============================================
```

### Step 3: 验证应用启动

访问：http://localhost:8080/api/auth/me

应该看到 401 Unauthorized（因为未登录，这是正常的）

### Step 4: 生成正确的密码哈希

应用运行后，在另一个终端运行：
```bash
java -cp target/classes com.qdq.util.GenerateBcryptHash
```

### Step 5: 更新数据库密码

复制输出的 SQL 并在 MySQL 中执行：
```sql
UPDATE sys_user
SET password_hash = '[从工具输出复制的哈希值]',
    password_plain = 'admin123'
WHERE username = 'admin';
```

### Step 6: 测试登录

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
    "token": "eyJ...",
    "roles": ["SUPER_ADMIN"]
  }
}
```

---

## 🔧 创建的文件

| 文件 | 用途 |
|------|------|
| `RUN_APPLICATION.bat` | Windows 批处理脚本 |
| `RUN_APPLICATION.ps1` | PowerShell 脚本 |

---

## 🎯 关键点

✅ **使用 Maven 构建和运行**
- 确保所有类都被正确编译
- 生成可执行的 JAR 文件
- 避免 IntelliJ 的编译问题

✅ **完整的构建过程**
- `mvn clean` - 删除旧的编译输出
- `mvn compile` - 编译源代码
- `mvn package` - 打包为 JAR
- `java -jar` - 运行应用

✅ **验证编译**
```bash
# 检查 target 目录是否存在
dir target\classes\com\qdq\QuizCompetitionApplication.class

# 如果存在，说明编译成功
```

---

## 🚀 现在可以：

- ✅ 编译应用
- ✅ 成功启动应用
- ✅ 生成 BCrypt 密码哈希
- ✅ 更新数据库
- ✅ 登录系统

**一切都准备好了！** 🎉

