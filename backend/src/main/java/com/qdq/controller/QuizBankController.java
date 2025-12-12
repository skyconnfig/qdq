package com.qdq.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qdq.common.PageRequest;
import com.qdq.common.R;
import com.qdq.dto.BankRequest;
import com.qdq.entity.QuizBank;
import com.qdq.service.QuizBankService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 题库管理控制器
 */
@RestController
@RequestMapping("/api/banks")
public class QuizBankController {

    private final QuizBankService bankService;

    public QuizBankController(QuizBankService bankService) {
        this.bankService = bankService;
    }

    /**
     * 分页查询题库
     */
    @GetMapping
    public R<Page<QuizBank>> page(PageRequest pageRequest,
                                   @RequestParam(required = false) Integer status,
                                   @RequestParam(required = false) Integer isDisabled,
                                   @RequestParam(required = false) String keyword) {
        Page<QuizBank> page = bankService.page(pageRequest, status, isDisabled, keyword);
        return R.ok(page);
    }

    /**
     * 获取题库详情
     */
    @GetMapping("/{id}")
    public R<QuizBank> getById(@PathVariable Long id) {
        QuizBank bank = bankService.getDetail(id);
        return R.ok(bank);
    }

    /**
     * 创建题库
     */
    @PostMapping
    @SaCheckRole({"SUPER_ADMIN", "HOST"})
    public R<QuizBank> create(@Valid @RequestBody BankRequest request) {
        QuizBank bank = bankService.create(request);
        return R.ok("创建成功", bank);
    }

    /**
     * 更新题库
     */
    @PutMapping("/{id}")
    @SaCheckRole({"SUPER_ADMIN", "HOST"})
    public R<QuizBank> update(@PathVariable Long id, @Valid @RequestBody BankRequest request) {
        QuizBank bank = bankService.update(id, request);
        return R.ok("更新成功", bank);
    }

    /**
     * 删除题库
     */
    @DeleteMapping("/{id}")
    @SaCheckRole({"SUPER_ADMIN", "HOST"})
    public R<Void> delete(@PathVariable Long id) {
        bankService.delete(id);
        return R.ok("删除成功", null);
    }

    /**
     * 批量删除题库
     */
    @DeleteMapping("/batch")
    @SaCheckRole({"SUPER_ADMIN", "HOST"})
    public R<Void> deleteBatch(@RequestBody List<Long> ids) {
        bankService.deleteBatch(ids);
        return R.ok("批量删除成功", null);
    }

    /**
     * 禁用题库
     */
    @PostMapping("/{id}/disable")
    @SaCheckRole({"SUPER_ADMIN", "HOST"})
    public R<Void> disable(@PathVariable Long id) {
        bankService.disable(id);
        return R.ok("禁用成功", null);
    }

    /**
     * 启用题库
     */
    @PostMapping("/{id}/enable")
    @SaCheckRole({"SUPER_ADMIN", "HOST"})
    public R<Void> enable(@PathVariable Long id) {
        bankService.enable(id);
        return R.ok("启用成功", null);
    }

    /**
     * 批量禁用题库
     */
    @PostMapping("/batch/disable")
    @SaCheckRole({"SUPER_ADMIN", "HOST"})
    public R<Void> disableBatch(@RequestBody List<Long> ids) {
        bankService.disableBatch(ids);
        return R.ok("批量禁用成功", null);
    }

    /**
     * 批量启用题库
     */
    @PostMapping("/batch/enable")
    @SaCheckRole({"SUPER_ADMIN", "HOST"})
    public R<Void> enableBatch(@RequestBody List<Long> ids) {
        bankService.enableBatch(ids);
        return R.ok("批量启用成功", null);
    }
}
