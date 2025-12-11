package com.qdq.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 答题记录实体类
 */
@Data
@TableName(value = "quiz_answer_log", autoResultMap = true)
public class QuizAnswerLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 场次ID */
    private Long sessionId;

    /** 题目ID */
    private Long questionId;

    /** 用户ID */
    private Long userId;

    /** 队伍ID */
    private Long teamId;

    /** 提交的答案 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object answer;

    /** 是否正确(0:错误 1:正确 NULL:待评判) */
    private Integer isCorrect;

    /** 得分 */
    private Integer score;

    /** 答题用时(秒) */
    private Integer timeSpent;

    /** 评判人ID(主观题) */
    private Long judgeUserId;

    /** 评判备注 */
    private String judgeComment;

    /** 提交时间 */
    private LocalDateTime submitTime;

    /** 评判时间 */
    private LocalDateTime judgeTime;

    // ============ 非数据库字段 ============
    
    /** 用户姓名 */
    @TableField(exist = false)
    private String userName;

    /** 队伍名称 */
    @TableField(exist = false)
    private String teamName;

    /** 题目标题 */
    @TableField(exist = false)
    private String questionTitle;
}
