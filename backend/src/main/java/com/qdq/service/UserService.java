package com.qdq.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qdq.common.PageRequest;
import com.qdq.dto.UserRequest;
import com.qdq.entity.SysUser;
import com.qdq.exception.BusinessException;
import com.qdq.mapper.SysUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户服务
 */
@Service
public class UserService extends ServiceImpl<SysUserMapper, SysUser> {

    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 分页查询用户
     */
    public Page<SysUser> page(PageRequest pageRequest, String keyword, Integer status) {
        Page<SysUser> page = new Page<>(pageRequest.getPage(), pageRequest.getPageSize());
        
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(keyword), SysUser::getUsername, keyword)
                .or()
                .like(StrUtil.isNotBlank(keyword), SysUser::getName, keyword)
                .or()
                .like(StrUtil.isNotBlank(keyword), SysUser::getPhone, keyword);
        wrapper.eq(status != null, SysUser::getStatus, status);
        wrapper.orderByDesc(SysUser::getCreatedAt);
        
        return this.page(page, wrapper);
    }

    /**
     * 创建用户
     */
    @Transactional(rollbackFor = Exception.class)
    public SysUser create(UserRequest request) {
        // 检查用户名是否存在
        if (existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        SysUser user = new SysUser();
        BeanUtil.copyProperties(request, user, "id", "password");
        
        // 加密密码
        if (StrUtil.isNotBlank(request.getPassword())) {
            user.setPasswordPlain(request.getPassword());
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        } else {
            // 默认密码
            user.setPasswordPlain("123456");
            user.setPasswordHash(passwordEncoder.encode("123456"));
        }
        
        user.setStatus(1);
        this.save(user);
        
        // TODO: 保存用户角色关联
        
        return user;
    }

    /**
     * 更新用户
     */
    @Transactional(rollbackFor = Exception.class)
    public SysUser update(Long id, UserRequest request) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 检查用户名是否被其他用户使用
        if (!user.getUsername().equals(request.getUsername()) && existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        BeanUtil.copyProperties(request, user, "id", "password", "passwordHash");
        
        // 如果提供了新密码，则更新
        if (StrUtil.isNotBlank(request.getPassword())) {
            user.setPasswordPlain(request.getPassword());
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        
        this.updateById(user);
        
        // TODO: 更新用户角色关联
        
        return user;
    }

    /**
     * 删除用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 不能删除管理员
        if ("admin".equals(user.getUsername())) {
            throw new BusinessException("不能删除系统管理员");
        }
        
        this.removeById(id);
    }

    /**
     * 批量删除用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<Long> ids) {
        for (Long id : ids) {
            delete(id);
        }
    }

    /**
     * 重置密码
     */
    public void resetPassword(Long id, String newPassword) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        user.setPasswordPlain(newPassword);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        this.updateById(user);
    }

    /**
     * 更新用户状态
     */
    public void updateStatus(Long id, Integer status) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        user.setStatus(status);
        this.updateById(user);
    }

    /**
     * 检查用户名是否存在
     */
    private boolean existsByUsername(String username) {
        return this.count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)) > 0;
    }
}
