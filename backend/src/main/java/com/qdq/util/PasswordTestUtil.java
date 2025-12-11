package com.qdq.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码测试工具类 - 用于验证密码加密和验证的完整流程
 */
public class PasswordTestUtil {

    /**
     * 测试密码加密和验证流程
     */
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        
        String plainPassword = "admin123";
        
        // 1. 生成哈希
        String hash = encoder.encode(plainPassword);
        System.out.println("=== 密码加密测试 ===");
        System.out.println("原始密码: " + plainPassword);
        System.out.println("生成的哈希: " + hash);
        System.out.println();
        
        // 2. 验证密码
        System.out.println("=== 密码验证测试 ===");
        boolean matches = encoder.matches(plainPassword, hash);
        System.out.println("密码匹配结果: " + matches);
        System.out.println();
        
        // 3. 测试错误密码
        System.out.println("=== 错误密码验证 ===");
        boolean wrongMatches = encoder.matches("wrongpassword", hash);
        System.out.println("错误密码匹配结果: " + wrongMatches);
        System.out.println();
        
        // 4. 数据库中的哈希值验证
        String dbHash = "$2a$12$R3h2cIPjj0yramPvVS9H2OPST9/PgBkqquzi.Ss7KIUgO2PKh2Gau";
        System.out.println("=== 数据库哈希验证 ===");
        System.out.println("数据库中的哈希: " + dbHash);
        boolean dbMatches = encoder.matches(plainPassword, dbHash);
        System.out.println("与 'admin123' 匹配结果: " + dbMatches);
        System.out.println();
        
        // 5. 完整流程演示
        System.out.println("=== 完整登录流程演示 ===");
        String username = "admin";
        String inputPassword = "admin123";
        String storedHash = dbHash; // 数据库中存储的哈希值
        
        System.out.println("Step 1: 用户输入 - username: " + username + ", password: " + inputPassword);
        System.out.println("Step 2: 数据库查询到哈希: " + storedHash);
        System.out.println("Step 3: 密码验证 - encoder.matches('" + inputPassword + "', '" + storedHash + "')");
        boolean finalResult = encoder.matches(inputPassword, storedHash);
        System.out.println("Step 4: 验证结果: " + finalResult);
        System.out.println();
        
        if (finalResult) {
            System.out.println("✓ 登录成功");
        } else {
            System.out.println("✗ 登录失败");
        }
    }
}
