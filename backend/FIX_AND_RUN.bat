@echo off
REM 编译并运行哈希生成工具

echo ========================================
echo 密码哈希修复脚本
echo ========================================
echo.

REM Step 1: Clean compile
echo Step 1: 清理并编译应用...
mvn clean compile -DskipTests
if %ERRORLEVEL% neq 0 (
    echo 编译失败！
    pause
    exit /b 1
)

echo.
echo ========================================
echo Step 2: 运行哈希生成工具...
echo ========================================
echo.

REM Step 2: Run the hash generator
java -cp target/classes com.qdq.util.GenerateBcryptHash

echo.
echo ========================================
echo 完成！
echo ========================================
echo.
echo 下一步：
echo 1. 复制上面输出的 SQL UPDATE 语句
echo 2. 在 MySQL 中执行该语句
echo 3. 重启应用：mvn spring-boot:run
echo 4. 使用 admin/admin123 登录
echo.
pause
