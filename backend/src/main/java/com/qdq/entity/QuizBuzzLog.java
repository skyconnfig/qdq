package com.qdq.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 抢答记录实体类
 */
@Data
@TableName("quiz_buzz_log")
public class QuizBuzzLog {

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

    /** 抢答时间(毫秒精度) */
    private LocalDateTime buzzTime;

    /** 服务器时间戳(毫秒) */
    private Long serverTime;

    /** 是否第一个(0:否 1:是) */
    private Integer isFirst;

    /** 抢答排名 */
    private Integer rank;

    /** 是否已处理 */
    private Integer processed;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // ============ 非数据库字段 ============
    
    /** 用户名 */
    @TableField(exist = false)
    private String username;

    /** 用户姓名 */
    @TableField(exist = false)
    private String userName;

    /** 队伍名称 */
    @TableField(exist = false)
    private String teamName;
}
