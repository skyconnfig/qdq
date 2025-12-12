package com.qdq.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 题目导入DTO(用于EasyExcel)
 */
@Data
public class QuestionImportDTO {

    @ExcelProperty("题目标题")
    private String title;

    @ExcelProperty("题型")
    private String type;

    @ExcelProperty("分类")
    private String category;

    @ExcelProperty("难度")
    private String difficulty;

    @ExcelProperty("分值")
    private Integer score;

    @ExcelProperty("题目内容")
    private String content;

    @ExcelProperty("选项")
    private String options;

    @ExcelProperty("答案")
    private String answer;

    @ExcelProperty("答案解析")
    private String analysis;

    @ExcelProperty("标签")
    private String tags;
}
