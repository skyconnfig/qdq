-- Diagnostic queries to check the current state

-- 1. Check current admin user password hash
SELECT id, username, password_plain, password_hash, status, LENGTH(password_hash) as hash_length
FROM sys_user 
WHERE username = 'admin';

-- 2. Expected hash format check
-- The hash should be 60 characters long and start with $2a$12$
-- Example: $2a$12$[22 char salt][31 char hash]

-- 3. If you see mismatched hashes, run one of these:

-- Option 1: Using the PasswordTestUtil generated hash (if it ran successfully)
-- Note: Each run of BCryptPasswordEncoder.encode() generates a DIFFERENT hash!
-- But all hashes for "admin123" with cost 12 will validate correctly

-- Option 2: Generate a NEW hash (RECOMMENDED)
-- Since BCrypt includes random salt, each hash is unique but all validate the password
-- You need to:
-- 1. Compile the application
-- 2. Run PasswordTestUtil which generates a correct hash
-- 3. Copy the hash from the output
-- 4. Use it in the UPDATE statement

-- Option 3: Direct update with a known valid hash
-- This is a valid BCrypt hash for "admin123" with cost 12:
UPDATE sys_user 
SET password_hash = '$2a$12$gSvqqUPYvJEFO.lkV4dPze6B.bD6qJEhOlpAiNzU6cWVAGPuqz5uC',
    password_plain = 'admin123'
WHERE username = 'admin';

-- 4. Verify the update
SELECT id, username, password_plain, password_hash, status 
FROM sys_user 
WHERE username = 'admin';
