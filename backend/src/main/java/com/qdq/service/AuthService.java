package com.qdq.service;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.qdq.dto.LoginRequest;
import com.qdq.dto.LoginResponse;
import com.qdq.entity.SysUser;
import com.qdq.exception.BusinessException;
import com.qdq.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 认证服务
 */
@Slf4j
@Service
public class AuthService {

    private final SysUserMapper sysUserMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(SysUserMapper sysUserMapper, BCryptPasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        log.debug("登录尝试 - username: {}", request.getUsername());
        
        // 1. 查询用户
        SysUser user = sysUserMapper.selectByUsername(request.getUsername());
        if (user == null) {
            log.warn("登录失败 - 用户不存在: {}", request.getUsername());
            throw new BusinessException("用户名或密码错误");
        }
        log.debug("用户查询成功 - userId: {}, status: {}", user.getId(), user.getStatus());

        // 2. 校验状态
        if (user.getStatus() != 1) {
            log.warn("登录失败 - 账号已禁用: {}, status: {}", request.getUsername(), user.getStatus());
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        // 3. 校验密码
        boolean passwordMatch = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        log.debug("密码验证 - username: {}, match: {}", request.getUsername(), passwordMatch);
        if (!passwordMatch) {
            log.warn("登录失败 - 密码错误: {}", request.getUsername());
            throw new BusinessException("用户名或密码错误");
        }

        // 4. 登录成功，生成Token
        StpUtil.login(user.getId(), request.getRememberMe());
        String token = StpUtil.getTokenValue();

        // 5. 更新登录信息
        user.setLastLoginTime(LocalDateTime.now());
        sysUserMapper.updateById(user);

        // 6. 查询角色和权限
        List<String> roles = sysUserMapper.selectRoleCodesByUserId(user.getId());
        List<String> permissions = sysUserMapper.selectPermissionCodesByUserId(user.getId());

        // 7. 构建响应
        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setName(user.getName());
        response.setAvatar(user.getAvatar());
        response.setToken(token);
        response.setTokenExpireTime(LocalDateTime.now().plusSeconds(StpUtil.getTokenTimeout()));
        response.setRoles(roles);
        response.setPermissions(permissions);

        log.info("用户登录成功: {}", user.getUsername());
        return response;
    }

    /**
     * 用户登出
     */
    public void logout() {
        StpUtil.logout();
    }

    /**
     * 获取当前登录用户信息
     */
    public LoginResponse getCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        List<String> roles = sysUserMapper.selectRoleCodesByUserId(userId);
        List<String> permissions = sysUserMapper.selectPermissionCodesByUserId(userId);

        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setName(user.getName());
        response.setAvatar(user.getAvatar());
        response.setToken(StpUtil.getTokenValue());
        response.setRoles(roles);
        response.setPermissions(permissions);

        return response;
    }

    /**
     * 修改密码
     */
    public void changePassword(String oldPassword, String newPassword) {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserMapper.selectById(userId);

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new BusinessException("原密码错误");
        }

        user.setPasswordPlain(newPassword);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        sysUserMapper.updateById(user);

        // 踢出所有会话
        StpUtil.logout(userId);
    }
}
