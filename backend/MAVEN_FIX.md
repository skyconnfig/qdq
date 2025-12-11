# ✅ Maven 编译错误修复

## 问题

```
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.11.0:compile
[ERROR] Fatal error compiling: 无效的标 记: --release
```

## 原因

Maven 编译器插件没有正确配置 Java 17 的编译选项。

## 解决方案

### ✅ 已修复

已更新 `pom.xml` 中的 `<build>` 部分，添加了正确的 Maven Compiler Plugin 配置：

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.11.0</version>
    <configuration>
        <source>17</source>
        <target>17</target>
        <encoding>UTF-8</encoding>
    </configuration>
</plugin>
```

---

## 🚀 现在可以编译了

### 方法 1: 使用自动化脚本 (推荐)

**Windows (批处理)**:
```bash
FIX_AND_RUN.bat
```

**Windows (PowerShell)**:
```powershell
.\FIX_AND_RUN.ps1
```

这会自动：
1. ✅ 编译应用
2. ✅ 运行哈希生成工具
3. ✅ 显示 SQL 语句供你复制

### 方法 2: 手动执行命令

```bash
# Step 1: 编译
mvn clean compile -DskipTests

# Step 2: 运行哈希生成工具
java -cp target/classes com.qdq.util.GenerateBcryptHash
```

---

## 📋 完整步骤

### Step 1: 编译应用
```bash
cd D:\daima\qdq\backend
mvn clean compile -DskipTests
```

**预期输出**:
```
[INFO] BUILD SUCCESS
```

### Step 2: 运行哈希生成工具
```bash
java -cp target/classes com.qdq.util.GenerateBcryptHash
```

**预期输出**:
```
========================================
BCrypt 密码哈希生成工具
========================================

密码: admin123
生成的哈希: $2a$12$XXXX...YYYY
哈希长度: 60

验证结果: ✓ 成功

========================================
执行以下SQL更新数据库:
========================================

UPDATE sys_user
SET password_hash = '$2a$12$XXXX...YYYY',
    password_plain = 'admin123'
WHERE username = 'admin';
```

### Step 3: 复制 SQL 并在 MySQL 中执行

```bash
mysql -u root -p quiz_competition
```

然后粘贴并执行 SQL 语句：
```sql
UPDATE sys_user
SET password_hash = '$2a$12$XXXX...YYYY',
    password_plain = 'admin123'
WHERE username = 'admin';
```

### Step 4: 验证更新

```sql
SELECT id, username, password_plain, password_hash, status
FROM sys_user
WHERE username = 'admin';
```

应该看到：
- `password_hash`: 与上面 SQL 中的哈希值相同
- `password_plain`: admin123
- `status`: 1

### Step 5: 重启应用

```bash
mvn spring-boot:run
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

## 🔧 修改的文件

| 文件 | 改动 |
|------|------|
| `pom.xml` | ✅ 添加了 Maven Compiler Plugin 配置 |
| `GenerateBcryptHash.java` | ✅ 创建了哈希生成工具 |
| `FIX_AND_RUN.bat` | ✅ 创建了 Windows 批处理脚本 |
| `FIX_AND_RUN.ps1` | ✅ 创建了 PowerShell 脚本 |

---

## ✨ 关键配置

```xml
<source>17</source>      <!-- 源代码版本 -->
<target>17</target>      <!-- 目标 JVM 版本 -->
<encoding>UTF-8</encoding> <!-- 编码 -->
```

这告诉 Maven 使用 Java 17 编译，不需要使用 `--release` 标记。

---

## 🎯 现在你可以：

- ✅ 编译应用
- ✅ 生成正确的 BCrypt 密码哈希
- ✅ 更新数据库
- ✅ 成功登录

**一切都准备好了！** 🎉

