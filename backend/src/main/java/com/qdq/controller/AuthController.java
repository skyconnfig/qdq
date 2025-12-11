package com.qdq.controller;

import com.qdq.common.R;
import com.qdq.dto.LoginRequest;
import com.qdq.dto.LoginResponse;
import com.qdq.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return R.ok("登录成功", response);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public R<Void> logout() {
        authService.logout();
        return R.ok("登出成功", null);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public R<LoginResponse> getCurrentUser() {
        LoginResponse response = authService.getCurrentUser();
        return R.ok(response);
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public R<Void> changePassword(@RequestParam String oldPassword, @RequestParam String newPassword) {
        authService.changePassword(oldPassword, newPassword);
        return R.ok("密码修改成功，请重新登录", null);
    }
}
