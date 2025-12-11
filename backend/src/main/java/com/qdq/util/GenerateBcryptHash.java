package com.qdq.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 生成正确的BCrypt密码哈希
 * 这个工具在应用运行时执行，确保生成的哈希能被应用验证
 */
public class GenerateBcryptHash {
    
    public static void main(String[] args) {
        // 创建与应用中相同配置的BCryptPasswordEncoder
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        
        String password = "admin123";
        
        // 生成哈希（包含随机盐值）
        String hash = encoder.encode(password);
        
        System.out.println("========================================");
        System.out.println("BCrypt 密码哈希生成工具");
        System.out.println("========================================");
        System.out.println();
        System.out.println("密码: " + password);
        System.out.println("生成的哈希: " + hash);
        System.out.println("哈希长度: " + hash.length());
        System.out.println();
        
        // 验证生成的哈希
        boolean matches = encoder.matches(password, hash);
        System.out.println("验证结果: " + (matches ? "✓ 成功" : "✗ 失败"));
        System.out.println();
        
        // 生成SQL语句
        System.out.println("========================================");
        System.out.println("执行以下SQL更新数据库:");
        System.out.println("========================================");
        System.out.println();
        System.out.println("UPDATE sys_user");
        System.out.println("SET password_hash = '" + hash + "',");
        System.out.println("    password_plain = '" + password + "'");
        System.out.println("WHERE username = 'admin';");
        System.out.println();
        
        // 验证查询
        System.out.println("========================================");
        System.out.println("验证SQL (执行更新后):");
        System.out.println("========================================");
        System.out.println();
        System.out.println("SELECT id, username, password_plain, password_hash, status");
        System.out.println("FROM sys_user");
        System.out.println("WHERE username = 'admin';");
        System.out.println();
        System.out.println("预期结果:");
        System.out.println("- password_hash: " + hash);
        System.out.println("- password_plain: " + password);
        System.out.println("- status: 1");
        System.out.println();
    }
}
