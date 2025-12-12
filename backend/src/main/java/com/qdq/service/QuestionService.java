package com.qdq.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qdq.common.PageRequest;
import com.qdq.dto.QuestionRequest;
import com.qdq.entity.QuizQuestion;
import com.qdq.exception.BusinessException;
import com.qdq.mapper.QuizQuestionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 题目服务
 */
@Service
public class QuestionService extends ServiceImpl<QuizQuestionMapper, QuizQuestion> {

    /**
     * 分页查询题目
     */
    public Page<QuizQuestion> page(PageRequest pageRequest, Long categoryId, Integer type, 
                                    Integer difficulty, Integer status, String keyword) {
        Page<QuizQuestion> page = new Page<>(pageRequest.getPage(), pageRequest.getPageSize());
        
        LambdaQueryWrapper<QuizQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(categoryId != null, QuizQuestion::getCategoryId, categoryId);
        wrapper.eq(type != null, QuizQuestion::getType, type);
        wrapper.eq(difficulty != null, QuizQuestion::getDifficulty, difficulty);
        wrapper.eq(status != null, QuizQuestion::getStatus, status);
        wrapper.like(StrUtil.isNotBlank(keyword), QuizQuestion::getTitle, keyword);
        wrapper.orderByDesc(QuizQuestion::getCreatedAt);
        
        return this.page(page, wrapper);
    }

    /**
     * 获取题目详情
     */
    public QuizQuestion getDetail(Long id) {
        QuizQuestion question = this.getById(id);
        if (question == null) {
            throw new BusinessException("题目不存在");
        }
        return question;
    }

    /**
     * 创建题目
     */
    @Transactional(rollbackFor = Exception.class)
    public QuizQuestion create(QuestionRequest request) {
        QuizQuestion question = new QuizQuestion();
        BeanUtil.copyProperties(request, question, "id");
        question.setCreatedBy(StpUtil.getLoginIdAsLong());
        question.setUpdatedBy(StpUtil.getLoginIdAsLong());
        this.save(question);
        return question;
    }

    /**
     * 更新题目
     */
    @Transactional(rollbackFor = Exception.class)
    public QuizQuestion update(Long id, QuestionRequest request) {
        QuizQuestion question = this.getById(id);
        if (question == null) {
            throw new BusinessException("题目不存在");
        }
        
        BeanUtil.copyProperties(request, question, "id", "createdBy", "createdAt");
        question.setUpdatedBy(StpUtil.getLoginIdAsLong());
        this.updateById(question);
        return question;
    }

    /**
     * 删除题目
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        QuizQuestion question = this.getById(id);
        if (question == null) {
            throw new BusinessException("题目不存在");
        }
        this.removeById(id);
    }

    /**
     * 批量删除题目
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<Long> ids) {
        this.removeByIds(ids);
    }

    /**
     * 更新题目状态
     */
    public void updateStatus(Long id, Integer status) {
        QuizQuestion question = this.getById(id);
        if (question == null) {
            throw new BusinessException("题目不存在");
        }
        question.setStatus(status);
        this.updateById(question);
    }

    /**
     * 批量更新状态
     */
    public void updateStatusBatch(List<Long> ids, Integer status) {
        for (Long id : ids) {
            updateStatus(id, status);
        }
    }

    /**
     * 根据ID列表获取题目
     */
    public List<QuizQuestion> getByIds(List<Long> ids) {
        return this.listByIds(ids);
    }

    /**
     * 是否是不该显示的题目(禁用的题目)
     */
    public void disable(Long id) {
        QuizQuestion question = this.getById(id);
        if (question == null) {
            throw new BusinessException("题目不存在");
        }
        question.setIsDisabled(1);
        this.updateById(question);
    }

    /**
     * 整体次数、错误次数、答题用时等
     */
    public void enable(Long id) {
        QuizQuestion question = this.getById(id);
        if (question == null) {
            throw new BusinessException("题目不存在");
        }
        question.setIsDisabled(0);
        this.updateById(question);
    }

    /**
     * 批量禁用题目
     */
    @Transactional(rollbackFor = Exception.class)
    public void disableBatch(List<Long> ids) {
        for (Long id : ids) {
            disable(id);
        }
    }

    /**
     * 批量启用题目
     */
    @Transactional(rollbackFor = Exception.class)
    public void enableBatch(List<Long> ids) {
        for (Long id : ids) {
            enable(id);
        }
    }

    /**
     * 获取随机题目
     */
    public List<QuizQuestion> getRandomQuestions(Long categoryId, Integer type, 
                                                   Integer difficulty, Integer count) {
        LambdaQueryWrapper<QuizQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(categoryId != null, QuizQuestion::getCategoryId, categoryId);
        wrapper.eq(type != null, QuizQuestion::getType, type);
        wrapper.eq(difficulty != null, QuizQuestion::getDifficulty, difficulty);
        wrapper.eq(QuizQuestion::getStatus, 1); // 只获取已发布的题目
        wrapper.last("ORDER BY RAND() LIMIT " + count);
        return this.list(wrapper);
    }
}
