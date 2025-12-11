package com.qdq.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 题目请求DTO
 */
@Data
public class QuestionRequest {

    private Long id;

    /** 分类ID */
    private Long categoryId;

    /** 题型(1:单选 2:多选 3:判断 4:填空 5:主观) */
    @NotNull(message = "题型不能为空")
    @Min(value = 1, message = "题型值无效")
    @Max(value = 5, message = "题型值无效")
    private Integer type;

    /** 题目标题 */
    @NotBlank(message = "题目标题不能为空")
    @Size(max = 500, message = "题目标题不能超过500个字符")
    private String title;

    /** 题目内容(富文本) */
    private String content;

    /** 选项(JSON数组) */
    private List<Map<String, Object>> options;

    /** 答案(JSON) */
    private Object answer;

    /** 答案解析 */
    private String analysis;

    /** 分值 */
    @Min(value = 1, message = "分值必须大于0")
    private Integer score = 10;

    /** 难度(1:简单 2:中等 3:困难) */
    @Min(value = 1, message = "难度值无效")
    @Max(value = 3, message = "难度值无效")
    private Integer difficulty = 2;

    /** 标签(JSON数组) */
    private List<String> tags;

    /** 附件(图片/音频/视频) */
    private List<Map<String, String>> attachments;

    /** 状态(0:草稿 1:已发布 2:待审核) */
    private Integer status = 0;
}
