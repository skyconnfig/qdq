package com.qdq.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 用户请求DTO
 */
@Data
public class UserRequest {

    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 64, message = "用户名长度必须在3-64个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    @Size(min = 6, max = 32, message = "密码长度必须在6-32个字符之间")
    private String password;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 100, message = "姓名长度不能超过100个字符")
    private String name;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String avatar;

    @Min(value = 0, message = "性别值无效")
    @Max(value = 2, message = "性别值无效")
    private Integer gender;

    private Integer status;

    /** 角色ID列表 */
    private java.util.List<Long> roleIds;
}
