package com.qdq.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.qdq.dto.QuestionImportDTO;
import com.qdq.entity.QuizQuestion;
import com.qdq.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 题目导入导出服务
 */
@Slf4j
@Service
public class ImportExportService {

    private final QuestionService questionService;
    private final QuizBankService bankService;

    public ImportExportService(QuestionService questionService, QuizBankService bankService) {
        this.questionService = questionService;
        this.bankService = bankService;
    }

    /**
     * 导入题目(从Excel文件)
     */
    @Transactional(rollbackFor = Exception.class)
    public List<QuizQuestion> importQuestions(MultipartFile file, Long bankId) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        List<QuizQuestion> questions = new ArrayList<>();
        try {
            List<QuestionImportDTO> dtoList = EasyExcel.read(file.getInputStream(), QuestionImportDTO.class, null)
                    .sheet()
                    .doReadSync();

            // 验证题库
            if (bankId != null) {
                bankService.getDetail(bankId);
            }

            for (QuestionImportDTO dto : dtoList) {
                if (StrUtil.isBlank(dto.getTitle())) {
                    continue; // 跳过标题为空的行
                }

                QuizQuestion question = new QuizQuestion();
                question.setTitle(dto.getTitle());
                question.setContent(dto.getContent());
                question.setScore(dto.getScore() != null ? dto.getScore() : 10);
                question.setAnalysis(dto.getAnalysis());
                question.setBankId(bankId);
                question.setStatus(1); // 默认已发布
                question.setIsDisabled(0); // 默认未禁用
                question.setCreatedBy(StpUtil.getLoginIdAsLong());
                question.setUpdatedBy(StpUtil.getLoginIdAsLong());

                // 处理题型
                if (StrUtil.isNotBlank(dto.getType())) {
                    question.setType(parseQuestionType(dto.getType()));
                }

                // 处理难度
                if (StrUtil.isNotBlank(dto.getDifficulty())) {
                    question.setDifficulty(parseDifficulty(dto.getDifficulty()));
                }

                // 处理选项(JSON字符串转List)
                if (StrUtil.isNotBlank(dto.getOptions())) {
                    question.setOptions(parseOptions(dto.getOptions()));
                }

                // 处理答案
                if (StrUtil.isNotBlank(dto.getAnswer())) {
                    question.setAnswer(dto.getAnswer());
                }

                // 处理标签
                if (StrUtil.isNotBlank(dto.getTags())) {
                    question.setTags(Arrays.asList(dto.getTags().split(",")));
                }

                questionService.save(question);
                questions.add(question);
            }

            // 更新题库中的题目数量
            if (bankId != null) {
                bankService.updateQuestionCount(bankId);
            }

            log.info("导入题目成功: {}道", questions.size());
            return questions;

        } catch (IOException e) {
            log.error("导入题目失败", e);
            throw new BusinessException("导入失败: " + e.getMessage());
        }
    }

    /**
     * 导出题目模板
     */
    public void exportTemplate(String filePath) {
        List<QuestionImportDTO> templateList = createTemplate();
        EasyExcel.write(filePath, QuestionImportDTO.class)
                .sheet("题目模板")
                .doWrite(templateList);
        log.info("导出题目模板成功: {}", filePath);
    }

    /**
     * 创建题目模板
     */
    private List<QuestionImportDTO> createTemplate() {
        List<QuestionImportDTO> list = new ArrayList<>();
        
        // 添加示例行
        QuestionImportDTO example = new QuestionImportDTO();
        example.setTitle("示例题目");
        example.setType("单选");
        example.setCategory("综合知识");
        example.setDifficulty("中等");
        example.setScore(10);
        example.setContent("这是一个示例题目");
        example.setOptions("A:选项1,B:选项2,C:选项3,D:选项4");
        example.setAnswer("A");
        example.setAnalysis("答案解析内容");
        example.setTags("标签1,标签2");
        
        list.add(example);
        return list;
    }

    /**
     * 解析题型
     */
    private Integer parseQuestionType(String typeStr) {
        return switch (typeStr.trim()) {
            case "单选" -> 1;
            case "多选" -> 2;
            case "判断" -> 3;
            case "填空" -> 4;
            case "主观" -> 5;
            case "音频" -> 6;
            case "视频" -> 7;
            default -> 1;
        };
    }

    /**
     * 解析难度
     */
    private Integer parseDifficulty(String diffStr) {
        return switch (diffStr.trim()) {
            case "简单" -> 1;
            case "中等" -> 2;
            case "困难" -> 3;
            default -> 2;
        };
    }

    /**
     * 解析选项 (格式: A:选项1,B:选项2)
     */
    private List<Map<String, Object>> parseOptions(String optionsStr) {
        List<Map<String, Object>> optionsList = new ArrayList<>();
        String[] options = optionsStr.split(",");
        
        for (String option : options) {
            String[] parts = option.split(":");
            if (parts.length == 2) {
                Map<String, Object> optionMap = new HashMap<>();
                optionMap.put("label", parts[0].trim());
                optionMap.put("value", parts[1].trim());
                optionsList.add(optionMap);
            }
        }
        
        return optionsList.isEmpty() ? null : optionsList;
    }
}
