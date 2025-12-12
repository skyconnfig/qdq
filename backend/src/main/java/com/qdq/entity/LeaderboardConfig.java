package com.qdq.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 排行版配置实体类
 */
@Data
@TableName(value = "quiz_leaderboard_config", autoResultMap = true)
public class LeaderboardConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 场次ID */
    private Long sessionId;

    /** 排行版名称 */
    private String leaderboardName;

    /** 显示字段配置(JSON) */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> displayFields;

    /** 排序方式(1:得分降序 2:答题数降序) */
    private Integer sortType;

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
}
