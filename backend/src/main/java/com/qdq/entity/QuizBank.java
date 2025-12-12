package com.qdq.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 题库实体类
 */
@Data
@TableName("quiz_bank")
public class QuizBank {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 题库名称 */
    private String name;

    /** 题库描述 */
    private String description;

    /** 题目总数 */
    private Integer totalQuestions;

    /** 状态(0:禁用 1:启用) */
    private Integer status;

    /** 禁用标记(0:启用 1:禁用) */
    private Integer isDisabled;

    /** 创建人 */
    private Long createdBy;

    /** 更新人 */
    private Long updatedBy;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 删除标记 */
    @TableLogic
    private Integer deleted;
}
