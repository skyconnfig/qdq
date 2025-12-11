package com.qdq.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qdq.common.PageRequest;
import com.qdq.common.R;
import com.qdq.dto.UserRequest;
import com.qdq.entity.SysUser;
import com.qdq.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 分页查询用户
     */
    @GetMapping
    public R<Page<SysUser>> page(PageRequest pageRequest,
                                  @RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) Integer status) {
        Page<SysUser> page = userService.page(pageRequest, keyword, status);
        // 隐藏密码
        page.getRecords().forEach(user -> user.setPasswordHash(null));
        return R.ok(page);
    }

    /**
     * 根据ID查询用户
     */
    @GetMapping("/{id}")
    public R<SysUser> getById(@PathVariable Long id) {
        SysUser user = userService.getById(id);
        if (user != null) {
            user.setPasswordHash(null);
        }
        return R.ok(user);
    }

    /**
     * 创建用户
     */
    @PostMapping
    @SaCheckRole("SUPER_ADMIN")
    public R<SysUser> create(@Valid @RequestBody UserRequest request) {
        SysUser user = userService.create(request);
        user.setPasswordHash(null);
        return R.ok("创建成功", user);
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    @SaCheckRole("SUPER_ADMIN")
    public R<SysUser> update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        SysUser user = userService.update(id, request);
        user.setPasswordHash(null);
        return R.ok("更新成功", user);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @SaCheckRole("SUPER_ADMIN")
    public R<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return R.ok("删除成功", null);
    }

    /**
     * 批量删除用户
     */
    @DeleteMapping("/batch")
    @SaCheckRole("SUPER_ADMIN")
    public R<Void> deleteBatch(@RequestBody List<Long> ids) {
        userService.deleteBatch(ids);
        return R.ok("批量删除成功", null);
    }

    /**
     * 重置密码
     */
    @PostMapping("/{id}/reset-password")
    @SaCheckRole("SUPER_ADMIN")
    public R<Void> resetPassword(@PathVariable Long id, @RequestParam String newPassword) {
        userService.resetPassword(id, newPassword);
        return R.ok("密码重置成功", null);
    }

    /**
     * 更新用户状态
     */
    @PostMapping("/{id}/status")
    @SaCheckRole("SUPER_ADMIN")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return R.ok("状态更新成功", null);
    }
}
