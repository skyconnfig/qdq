package com.qdq.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qdq.common.PageRequest;
import com.qdq.dto.BankRequest;
import com.qdq.entity.QuizBank;
import com.qdq.entity.QuizQuestion;
import com.qdq.exception.BusinessException;
import com.qdq.mapper.QuizBankMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 题库服务
 */
@Service
public class QuizBankService extends ServiceImpl<QuizBankMapper, QuizBank> {

    private final QuestionService questionService;

    public QuizBankService(QuestionService questionService) {
        this.questionService = questionService;
    }

    /**
     * 分页查询题库
     */
    public Page<QuizBank> page(PageRequest pageRequest, Integer status, Integer isDisabled, String keyword) {
        Page<QuizBank> page = new Page<>(pageRequest.getPage(), pageRequest.getPageSize());
        
        LambdaQueryWrapper<QuizBank> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, QuizBank::getStatus, status);
        wrapper.eq(isDisabled != null, QuizBank::getIsDisabled, isDisabled);
        wrapper.like(StrUtil.isNotBlank(keyword), QuizBank::getName, keyword);
        wrapper.orderByDesc(QuizBank::getCreatedAt);
        
        return this.page(page, wrapper);
    }

    /**
     * 获取题库详情
     */
    public QuizBank getDetail(Long id) {
        QuizBank bank = this.getById(id);
        if (bank == null) {
            throw new BusinessException("题库不存在");
        }
        return bank;
    }

    /**
     * 创建题库
     */
    @Transactional(rollbackFor = Exception.class)
    public QuizBank create(BankRequest request) {
        QuizBank bank = new QuizBank();
        BeanUtil.copyProperties(request, bank, "id");
        bank.setCreatedBy(StpUtil.getLoginIdAsLong());
        bank.setUpdatedBy(StpUtil.getLoginIdAsLong());
        bank.setTotalQuestions(0);
        bank.setStatus(1); // 默认启用
        bank.setIsDisabled(0); // 默认未禁用
        this.save(bank);
        return bank;
    }

    /**
     * 更新题库
     */
    @Transactional(rollbackFor = Exception.class)
    public QuizBank update(Long id, BankRequest request) {
        QuizBank bank = this.getById(id);
        if (bank == null) {
            throw new BusinessException("题库不存在");
        }
        
        BeanUtil.copyProperties(request, bank, "id", "totalQuestions", "createdBy", "createdAt");
        bank.setUpdatedBy(StpUtil.getLoginIdAsLong());
        this.updateById(bank);
        return bank;
    }

    /**
     * 删除题库
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        QuizBank bank = this.getById(id);
        if (bank == null) {
            throw new BusinessException("题库不存在");
        }
        
        // 删除题库下的所有题目
        LambdaQueryWrapper<QuizQuestion> questionWrapper = new LambdaQueryWrapper<>();
        questionWrapper.eq(QuizQuestion::getBankId, id);
        questionService.remove(questionWrapper);
        
        // 删除题库
        this.removeById(id);
    }

    /**
     * 批量删除题库
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<Long> ids) {
        for (Long id : ids) {
            delete(id);
        }
    }

    /**
     * 禁用题库
     */
    @Transactional(rollbackFor = Exception.class)
    public void disable(Long id) {
        QuizBank bank = this.getById(id);
        if (bank == null) {
            throw new BusinessException("题库不存在");
        }
        bank.setIsDisabled(1);
        bank.setUpdatedBy(StpUtil.getLoginIdAsLong());
        this.updateById(bank);
    }

    /**
     * 启用题库
     */
    @Transactional(rollbackFor = Exception.class)
    public void enable(Long id) {
        QuizBank bank = this.getById(id);
        if (bank == null) {
            throw new BusinessException("题库不存在");
        }
        bank.setIsDisabled(0);
        bank.setUpdatedBy(StpUtil.getLoginIdAsLong());
        this.updateById(bank);
    }

    /**
     * 批量禁用题库
     */
    @Transactional(rollbackFor = Exception.class)
    public void disableBatch(List<Long> ids) {
        for (Long id : ids) {
            disable(id);
        }
    }

    /**
     * 批量启用题库
     */
    @Transactional(rollbackFor = Exception.class)
    public void enableBatch(List<Long> ids) {
        for (Long id : ids) {
            enable(id);
        }
    }

    /**
     * 更新题库中的题目数量
     */
    public void updateQuestionCount(Long bankId) {
        LambdaQueryWrapper<QuizQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuizQuestion::getBankId, bankId);
        long count = questionService.count(wrapper);
        
        QuizBank bank = this.getById(bankId);
        if (bank != null) {
            bank.setTotalQuestions((int) count);
            this.updateById(bank);
        }
    }
}
