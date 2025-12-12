package com.qdq.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 题库请求DTO
 */
@Data
public class BankRequest {

    @NotBlank(message = "题库名称不能为空")
    private String name;

    private String description;

    private Integer status;

    private Integer isDisabled;
}
