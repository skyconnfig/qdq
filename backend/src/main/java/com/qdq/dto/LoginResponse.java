package com.qdq.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录响应DTO
 */
@Data
public class LoginResponse {

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 姓名 */
    private String name;

    /** 头像 */
    private String avatar;

    /** Token */
    private String token;

    /** Token过期时间 */
    private LocalDateTime tokenExpireTime;

    /** 角色列表 */
    private List<String> roles;

    /** 权限列表 */
    private List<String> permissions;
}
