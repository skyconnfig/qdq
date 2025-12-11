package com.qdq.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 比赛场次实体类
 */
@Data
@TableName(value = "quiz_session", autoResultMap = true)
public class QuizSession {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 场次名称 */
    private String name;

    /** 场次描述 */
    private String description;

    /** 状态(0:草稿 1:待开始 2:进行中 3:暂停 4:已结束) */
    private Integer status;

    /** 模式(1:个人赛 2:团队赛) */
    private Integer mode;

    /** 场次配置(JSON) */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> config;

    /** 题目ID列表(JSON数组) */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> questionIds;

    /** 当前题目索引 */
    private Integer currentQuestionIndex;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 计划开始时间 */
    private LocalDateTime scheduledStart;

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

    // ============ 非数据库字段 ============
    
    /** 参赛人数/队伍数 */
    @TableField(exist = false)
    private Integer participantCount;

    /** 题目数量 */
    @TableField(exist = false)
    private Integer questionCount;

    /** 当前题目信息 */
    @TableField(exist = false)
    private QuizQuestion currentQuestion;
}
