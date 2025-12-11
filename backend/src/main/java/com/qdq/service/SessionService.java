package com.qdq.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qdq.common.PageRequest;
import com.qdq.dto.SessionRequest;
import com.qdq.entity.QuizQuestion;
import com.qdq.entity.QuizSession;
import com.qdq.exception.BusinessException;
import com.qdq.mapper.QuizSessionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 比赛场次服务
 */
@Slf4j
@Service
public class SessionService extends ServiceImpl<QuizSessionMapper, QuizSession> {

    private final QuestionService questionService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String SESSION_CACHE_KEY = "quiz:session:";
    private static final String SESSION_STATE_KEY = "quiz:session:state:";

    public SessionService(QuestionService questionService, RedisTemplate<String, Object> redisTemplate) {
        this.questionService = questionService;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 分页查询场次
     */
    public Page<QuizSession> page(PageRequest pageRequest, Integer status, String keyword) {
        Page<QuizSession> page = new Page<>(pageRequest.getPage(), pageRequest.getPageSize());
        
        LambdaQueryWrapper<QuizSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, QuizSession::getStatus, status);
        wrapper.like(StrUtil.isNotBlank(keyword), QuizSession::getName, keyword);
        wrapper.orderByDesc(QuizSession::getCreatedAt);
        
        Page<QuizSession> result = this.page(page, wrapper);
        
        // 填充额外信息
        result.getRecords().forEach(session -> {
            if (session.getQuestionIds() != null) {
                session.setQuestionCount(session.getQuestionIds().size());
            }
        });
        
        return result;
    }

    /**
     * 获取场次详情
     */
    public QuizSession getDetail(Long id) {
        QuizSession session = this.getById(id);
        if (session == null) {
            throw new BusinessException("场次不存在");
        }
        
        // 填充当前题目信息
        if (session.getQuestionIds() != null && session.getCurrentQuestionIndex() >= 0 
            && session.getCurrentQuestionIndex() < session.getQuestionIds().size()) {
            Long questionId = session.getQuestionIds().get(session.getCurrentQuestionIndex());
            QuizQuestion question = questionService.getById(questionId);
            session.setCurrentQuestion(question);
        }
        
        return session;
    }

    /**
     * 创建场次
     */
    @Transactional(rollbackFor = Exception.class)
    public QuizSession create(SessionRequest request) {
        QuizSession session = new QuizSession();
        BeanUtil.copyProperties(request, session, "id");
        session.setStatus(0); // 草稿状态
        session.setCurrentQuestionIndex(-1);
        session.setCreatedBy(StpUtil.getLoginIdAsLong());
        session.setUpdatedBy(StpUtil.getLoginIdAsLong());
        
        // 设置默认配置
        if (session.getConfig() == null) {
            Map<String, Object> defaultConfig = new HashMap<>();
            defaultConfig.put("questionTimeLimit", 30); // 默认答题时限30秒
            defaultConfig.put("buzzEnabled", true); // 启用抢答
            defaultConfig.put("autoJudge", true); // 自动判分
            defaultConfig.put("scoreCorrect", 10); // 答对得分
            defaultConfig.put("scoreWrong", 0); // 答错得分
            session.setConfig(defaultConfig);
        }
        
        this.save(session);
        return session;
    }

    /**
     * 更新场次
     */
    @Transactional(rollbackFor = Exception.class)
    public QuizSession update(Long id, SessionRequest request) {
        QuizSession session = this.getById(id);
        if (session == null) {
            throw new BusinessException("场次不存在");
        }
        
        // 进行中的比赛不能修改
        if (session.getStatus() == 2) {
            throw new BusinessException("进行中的比赛不能修改");
        }
        
        BeanUtil.copyProperties(request, session, "id", "status", "createdBy", "createdAt");
        session.setUpdatedBy(StpUtil.getLoginIdAsLong());
        this.updateById(session);
        
        // 清除缓存
        clearSessionCache(id);
        
        return session;
    }

    /**
     * 删除场次
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        QuizSession session = this.getById(id);
        if (session == null) {
            throw new BusinessException("场次不存在");
        }
        
        // 进行中的比赛不能删除
        if (session.getStatus() == 2) {
            throw new BusinessException("进行中的比赛不能删除");
        }
        
        this.removeById(id);
        clearSessionCache(id);
    }

    /**
     * 开始比赛
     */
    @Transactional(rollbackFor = Exception.class)
    public QuizSession start(Long id) {
        QuizSession session = this.getById(id);
        if (session == null) {
            throw new BusinessException("场次不存在");
        }
        
        if (session.getStatus() == 2) {
            throw new BusinessException("比赛已在进行中");
        }
        
        if (session.getStatus() == 4) {
            throw new BusinessException("比赛已结束");
        }
        
        if (session.getQuestionIds() == null || session.getQuestionIds().isEmpty()) {
            throw new BusinessException("请先添加题目");
        }
        
        session.setStatus(2); // 进行中
        session.setCurrentQuestionIndex(-1);
        session.setStartTime(LocalDateTime.now());
        this.updateById(session);
        
        // 缓存场次状态到Redis
        cacheSessionState(session);
        
        log.info("比赛开始: {}", session.getName());
        return session;
    }

    /**
     * 暂停比赛
     */
    @Transactional(rollbackFor = Exception.class)
    public QuizSession pause(Long id) {
        QuizSession session = this.getById(id);
        if (session == null) {
            throw new BusinessException("场次不存在");
        }
        
        if (session.getStatus() != 2) {
            throw new BusinessException("只有进行中的比赛才能暂停");
        }
        
        session.setStatus(3); // 暂停
        this.updateById(session);
        
        cacheSessionState(session);
        
        log.info("比赛暂停: {}", session.getName());
        return session;
    }

    /**
     * 恢复比赛
     */
    @Transactional(rollbackFor = Exception.class)
    public QuizSession resume(Long id) {
        QuizSession session = this.getById(id);
        if (session == null) {
            throw new BusinessException("场次不存在");
        }
        
        if (session.getStatus() != 3) {
            throw new BusinessException("只有暂停的比赛才能恢复");
        }
        
        session.setStatus(2); // 进行中
        this.updateById(session);
        
        cacheSessionState(session);
        
        log.info("比赛恢复: {}", session.getName());
        return session;
    }

    /**
     * 结束比赛
     */
    @Transactional(rollbackFor = Exception.class)
    public QuizSession finish(Long id) {
        QuizSession session = this.getById(id);
        if (session == null) {
            throw new BusinessException("场次不存在");
        }
        
        session.setStatus(4); // 已结束
        session.setEndTime(LocalDateTime.now());
        this.updateById(session);
        
        clearSessionCache(id);
        
        log.info("比赛结束: {}", session.getName());
        return session;
    }

    /**
     * 下一题
     */
    @Transactional(rollbackFor = Exception.class)
    public QuizQuestion nextQuestion(Long id) {
        QuizSession session = this.getById(id);
        if (session == null) {
            throw new BusinessException("场次不存在");
        }
        
        if (session.getStatus() != 2) {
            throw new BusinessException("比赛未在进行中");
        }
        
        List<Long> questionIds = session.getQuestionIds();
        if (questionIds == null || questionIds.isEmpty()) {
            throw new BusinessException("没有题目");
        }
        
        int nextIndex = session.getCurrentQuestionIndex() + 1;
        if (nextIndex >= questionIds.size()) {
            throw new BusinessException("已经是最后一题");
        }
        
        session.setCurrentQuestionIndex(nextIndex);
        this.updateById(session);
        
        // 获取题目信息
        Long questionId = questionIds.get(nextIndex);
        QuizQuestion question = questionService.getById(questionId);
        
        // 缓存当前题目状态
        cacheCurrentQuestion(id, question);
        
        log.info("推送下一题: 场次={}, 题目索引={}", session.getName(), nextIndex);
        return question;
    }

    /**
     * 获取当前题目
     */
    public QuizQuestion getCurrentQuestion(Long sessionId) {
        QuizSession session = this.getById(sessionId);
        if (session == null) {
            throw new BusinessException("场次不存在");
        }
        
        if (session.getCurrentQuestionIndex() < 0) {
            return null;
        }
        
        List<Long> questionIds = session.getQuestionIds();
        if (questionIds == null || session.getCurrentQuestionIndex() >= questionIds.size()) {
            return null;
        }
        
        Long questionId = questionIds.get(session.getCurrentQuestionIndex());
        return questionService.getById(questionId);
    }

    /**
     * 缓存场次状态
     */
    private void cacheSessionState(QuizSession session) {
        String key = SESSION_STATE_KEY + session.getId();
        Map<String, Object> state = new HashMap<>();
        state.put("id", session.getId());
        state.put("status", session.getStatus());
        state.put("currentQuestionIndex", session.getCurrentQuestionIndex());
        state.put("questionCount", session.getQuestionIds() != null ? session.getQuestionIds().size() : 0);
        redisTemplate.opsForHash().putAll(key, state);
        redisTemplate.expire(key, 24, TimeUnit.HOURS);
    }

    /**
     * 缓存当前题目
     */
    private void cacheCurrentQuestion(Long sessionId, QuizQuestion question) {
        String key = SESSION_CACHE_KEY + sessionId + ":current_question";
        redisTemplate.opsForValue().set(key, question, 1, TimeUnit.HOURS);
    }

    /**
     * 清除场次缓存
     */
    private void clearSessionCache(Long sessionId) {
        redisTemplate.delete(SESSION_STATE_KEY + sessionId);
        redisTemplate.delete(SESSION_CACHE_KEY + sessionId + ":current_question");
    }
}
