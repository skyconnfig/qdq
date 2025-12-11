# 密码哈希修复脚本 (PowerShell)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "密码哈希修复脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Clean compile
Write-Host "Step 1: 清理并编译应用..." -ForegroundColor Yellow
mvn clean compile -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "编译失败！" -ForegroundColor Red
    Read-Host "按 Enter 键退出"
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Step 2: 运行哈希生成工具..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 2: Run the hash generator
java -cp target/classes com.qdq.util.GenerateBcryptHash

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "下一步：" -ForegroundColor Yellow
Write-Host "1. 复制上面输出的 SQL UPDATE 语句" -ForegroundColor White
Write-Host "2. 在 MySQL 中执行该语句" -ForegroundColor White
Write-Host "3. 重启应用：mvn spring-boot:run" -ForegroundColor White
Write-Host "4. 使用 admin/admin123 登录" -ForegroundColor White
Write-Host ""
Read-Host "按 Enter 键退出"
