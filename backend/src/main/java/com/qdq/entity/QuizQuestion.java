package com.qdq.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 题目实体类
 */
@Data
@TableName(value = "quiz_question", autoResultMap = true)
public class QuizQuestion {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分类ID */
    private Long categoryId;

    /** 题型(1:单选 2:多选 3:判断 4:填空 5:主观) */
    private Integer type;

    /** 题目标题 */
    private String title;

    /** 题目内容(富文本) */
    private String content;

    /** 选项(JSON数组) */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> options;

    /** 答案(JSON) */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object answer;

    /** 答案解析 */
    private String analysis;

    /** 分值 */
    private Integer score;

    /** 难度(1:简单 2:中等 3:困难) */
    private Integer difficulty;

    /** 标签(JSON数组) */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    /** 附件(图片/音频/视频) */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, String>> attachments;

    /** 状态(0:草稿 1:已发布 2:待审核) */
    private Integer status;

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
    
    /** 分类名称 */
    @TableField(exist = false)
    private String categoryName;

    /** 创建人姓名 */
    @TableField(exist = false)
    private String createdByName;
}
