package com.qdq.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 场次参赛者实体类
 */
@Data
@TableName("quiz_session_participant")
public class QuizSessionParticipant {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 场次ID */
    private Long sessionId;

    /** 用户ID(个人赛) */
    private Long userId;

    /** 队伍ID(团队赛) */
    private Long teamId;

    /** 总得分 */
    private Integer totalScore;

    /** 正确题数 */
    private Integer correctCount;

    /** 错误题数 */
    private Integer wrongCount;

    /** 抢答次数 */
    private Integer buzzCount;

    /** 抢答成功次数 */
    private Integer buzzSuccessCount;

    /** 已答题数 */
    private Integer answeredCount;

    /** 排名 */
    private Integer rank;

    /** 加入时间 */
    private LocalDateTime joinedAt;

    // ============ 非数据库字段 ============
    
    /** 用户姓名 */
    @TableField(exist = false)
    private String userName;

    /** 用户名 */
    @TableField(exist = false)
    private String username;

    /** 队伍名称 */
    @TableField(exist = false)
    private String teamName;
}
