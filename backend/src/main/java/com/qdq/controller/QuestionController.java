package com.qdq.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qdq.common.PageRequest;
import com.qdq.common.R;
import com.qdq.dto.QuestionRequest;
import com.qdq.entity.QuizQuestion;
import com.qdq.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 题目管理控制器
 */
@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
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
     * 获取随机题目
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
