# 构建并运行应用 (PowerShell)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "知识竞赛抢答系统 - 启动脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: 清理和编译
Write-Host "Step 1: 清理和编译应用..." -ForegroundColor Yellow
mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "编译失败！" -ForegroundColor Red
    Read-Host "按 Enter 键退出"
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Step 2: 运行应用..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 2: 运行 JAR 文件
java -jar target/quiz-competition-1.0.0.jar

Read-Host "按 Enter 键退出"
