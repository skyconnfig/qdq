package com.qdq.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qdq.common.PageRequest;
import com.qdq.common.R;
import com.qdq.dto.QuestionRequest;
import com.qdq.entity.QuizQuestion;
import com.qdq.service.QuestionService;
import com.qdq.service.ImportExportService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 题目管理控制器
 */
@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;
    private final ImportExportService importExportService;

    public QuestionController(QuestionService questionService, ImportExportService importExportService) {
        this.questionService = questionService;
        this.importExportService = importExportService;
    }

    /**
     * 分页查询题目
     */
    @GetMapping
    public R<Page<QuizQuestion>> page(PageRequest pageRequest,
                                       @RequestParam(required = false) Long categoryId,
                                       @RequestParam(required = false) Integer type,
                                       @RequestParam(required = false) Integer difficulty,
                                       @RequestParam(required = false) Integer status,
                                       @RequestParam(required = false) String keyword) {
        Page<QuizQuestion> page = questionService.page(pageRequest, categoryId, type, difficulty, status, keyword);
        return R.ok(page);
    }

    /**
     * 获取题目详情
     */
    @GetMapping("/{id}")
    public R<QuizQuestion> getById(@PathVariable Long id) {
        QuizQuestion question = questionService.getDetail(id);
        return R.ok(question);
    }

    /**
     * 创建题目
     */
    @PostMapping
    @SaCheckRole({"SUPER_ADMIN", "HOST"})
    public R<QuizQuestion> create(@Valid @RequestBody QuestionRequest request) {
        QuizQuestion question = questionService.create(request);
        return R.ok("创建成功", question);
    }

    /**
     * 更新题目
     */
    @PutMapping("/{id}")
    @SaCheckRole({"SUPER_ADMIN", "HOST"})
    public R<QuizQuestion> update(@PathVariable Long id, @Valid @RequestBody QuestionRequest request) {
        QuizQuestion question = questionService.update(id, request);
        return R.ok("更新成功", question);
    }

    /**
     * 删除题目
     */
    @DeleteMapping("/{id}")
    @SaCheckRole({"SUPER_ADMIN", "HOST"})
    public R<Void> delete(@PathVariable Long id) {
        questionService.delete(id);
        return R.ok("删除成功", null);
    }

    /**
     * 批量删除题目
     */
    @DeleteMapping("/batch")
    @SaCheckRole({"SUPER_ADMIN", "HOST"})
    public R<Void> deleteBatch(@RequestBody List<Long> ids) {
        questionService.deleteBatch(ids);
        return R.ok("批量删除成功", null);
    }

    /**
     * 更新题目状态
     */
    @PostMapping("/{id}/status")
    @SaCheckRole({"SUPER_ADMIN", "HOST"})
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        questionService.updateStatus(id, status);
        return R.ok("状态更新成功", null);
    }

    /**
     * 上传题目模板
     */
    @GetMapping("/template")
    public R<Void> getTemplate() {
        // 上传模板文件
        String filePath = "./uploads/question_template.xlsx";
        importExportService.exportTemplate(filePath);
        return R.ok("模板下载成功", null);
    }

    /**
     * 导入题目
     */
    @PostMapping("/import")
    @SaCheckRole({"SUPER_ADMIN", "HOST"})
    public R<List<QuizQuestion>> importQuestions(@RequestParam("file") MultipartFile file,
                                                  @RequestParam(required = false) Long bankId) {
        List<QuizQuestion> questions = importExportService.importQuestions(file, bankId);
        return R.ok("导入成功", questions);
    }

    /**
     * 禁用题目
     */
    @PostMapping("/{id}/disable")
    @SaCheckRole({"SUPER_ADMIN", "HOST"})
    public R<Void> disableQuestion(@PathVariable Long id) {
        questionService.disable(id);
        return R.ok("禁用成功", null);
    }

    /**
     * 启用题目
     */
    @PostMapping("/{id}/enable")
    @SaCheckRole({"SUPER_ADMIN", "HOST"})
    public R<Void> enableQuestion(@PathVariable Long id) {
        questionService.enable(id);
        return R.ok("启用成功", null);
    }

    /**
     * 批量禁用题目
     */
    @PostMapping("/batch/disable")
    @SaCheckRole({"SUPER_ADMIN", "HOST"})
    public R<Void> disableBatch(@RequestBody List<Long> ids) {
        questionService.disableBatch(ids);
        return R.ok("批量禁用成功", null);
    }

    /**
     * 批量启用题目
     */
    @PostMapping("/batch/enable")
    @SaCheckRole({"SUPER_ADMIN", "HOST"})
    public R<Void> enableBatch(@RequestBody List<Long> ids) {
        questionService.enableBatch(ids);
        return R.ok("批量启用成功", null);
    }

    /**
     * 随机题目
     */
    @GetMapping("/random")
    public R<List<QuizQuestion>> getRandomQuestions(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(defaultValue = "10") Integer count) {
        List<QuizQuestion> questions = questionService.getRandomQuestions(categoryId, type, difficulty, count);
        return R.ok(questions);
    }
}
