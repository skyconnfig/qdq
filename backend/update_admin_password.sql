-- 更新现有的admin用户密码
-- 密码: admin123
-- Spring Security BCrypt hash (cost 12): $2a$12$gSvqqUPYvJEFO.lkV4dPze6B.bD6qJEhOlpAiNzU6cWVAGPuqz5uC
-- 注: 每个BCrypt哈希都是唯一的(包含随机盐值)，但都能验证同一个密码

UPDATE sys_user 
SET password_hash = '$2a$12$gSvqqUPYvJEFO.lkV4dPze6B.bD6qJEhOlpAiNzU6cWVAGPuqz5uC',
    password_plain = 'admin123'
WHERE username = 'admin';

-- 验证更新
SELECT id, username, password_plain, password_hash, status FROM sys_user WHERE username = 'admin';
