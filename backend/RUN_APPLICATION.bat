@echo off
REM 构建并运行应用

echo ========================================
echo 知识竞赛抢答系统 - 启动脚本
echo ========================================
echo.

REM Step 1: 清理和编译
echo Step 1: 清理和编译应用...
mvn clean package -DskipTests
if %ERRORLEVEL% neq 0 (
    echo 编译失败！
    pause
    exit /b 1
)

echo.
echo ========================================
echo Step 2: 运行应用...
echo ========================================
echo.

REM Step 2: 运行 JAR 文件
java -jar target/quiz-competition-1.0.0.jar

pause
