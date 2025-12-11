package com.qdq.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 比赛场次请求DTO
 */
@Data
public class SessionRequest {

    private Long id;

    /** 场次名称 */
    @NotBlank(message = "场次名称不能为空")
    @Size(max = 200, message = "场次名称不能超过200个字符")
    private String name;

    /** 场次描述 */
    private String description;

    /** 模式(1:个人赛 2:团队赛) */
    @Min(value = 1, message = "模式值无效")
    @Max(value = 2, message = "模式值无效")
    private Integer mode = 1;

    /** 场次配置(JSON) */
    private Map<String, Object> config;

    /** 题目ID列表 */
    private List<Long> questionIds;

    /** 计划开始时间 */
    private LocalDateTime scheduledStart;
}
