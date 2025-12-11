package com.qdq.service;

import com.qdq.entity.QuizBuzzLog;
import com.qdq.entity.QuizSession;
import com.qdq.exception.BusinessException;
import com.qdq.mapper.QuizBuzzLogMapper;
import com.qdq.websocket.WebSocketMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 抢答服务
 * 使用Redis Sorted Set实现低延迟(<100ms)的抢答判定
 */
@Slf4j
@Service
public class BuzzService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final QuizBuzzLogMapper buzzLogMapper;
    private final SessionService sessionService;
    private final WebSocketMessageService wsMessageService;

    private static final String BUZZ_QUEUE_KEY = "buzz:queue:";
    private static final String BUZZ_LOCK_KEY = "buzz:lock:";
    private static final String BUZZ_RESULT_KEY = "buzz:result:";
    private static final long BUZZ_TIMEOUT_MS = 100; // 抢答判定窗口100ms

    public BuzzService(RedisTemplate<String, Object> redisTemplate,
                       QuizBuzzLogMapper buzzLogMapper,
                       SessionService sessionService,
                       WebSocketMessageService wsMessageService) {
        this.redisTemplate = redisTemplate;
        this.buzzLogMapper = buzzLogMapper;
        this.sessionService = sessionService;
        this.wsMessageService = wsMessageService;
    }

    /**
     * 处理抢答请求
     * 
     * @param sessionId 场次ID
     * @param questionId 题目ID
     * @param userId 用户ID
     * @param teamId 队伍ID（团队赛）
     * @return 抢答结果
     */
    public Map<String, Object> buzz(Long sessionId, Long questionId, Long userId, Long teamId) {
        long serverTime = System.currentTimeMillis();
        
        // 1. 验证场次状态
        QuizSession session = sessionService.getById(sessionId);
        if (session == null) {
            throw new BusinessException("场次不存在");
        }
        if (session.getStatus() != 2) {
            throw new BusinessException("比赛未在进行中");
        }
        
        // 2. 验证当前题目
        if (session.getCurrentQuestionIndex() < 0) {
            throw new BusinessException("当前没有进行中的题目");
        }
        Long currentQuestionId = session.getQuestionIds().get(session.getCurrentQuestionIndex());
        if (!currentQuestionId.equals(questionId)) {
            throw new BusinessException("题目已过期");
        }
        
        String queueKey = BUZZ_QUEUE_KEY + sessionId + ":" + questionId;
        String lockKey = BUZZ_LOCK_KEY + sessionId + ":" + questionId;
        
        // 3. 检查是否已经有抢答结果（锁定状态）
        Boolean locked = redisTemplate.hasKey(lockKey);
        if (Boolean.TRUE.equals(locked)) {
            // 已经有人抢答成功，返回失败
            return buildBuzzResult(false, "抢答失败，已有人抢答成功", null, serverTime);
        }
        
        // 4. 记录抢答时间到Redis Sorted Set
        String memberId = (teamId != null ? "team:" + teamId : "user:" + userId);
        redisTemplate.opsForZSet().add(queueKey, memberId, serverTime);
        redisTemplate.expire(queueKey, 5, TimeUnit.MINUTES);
        
        log.info("收到抢答请求: sessionId={}, questionId={}, userId={}, teamId={}, serverTime={}", 
                sessionId, questionId, userId, teamId, serverTime);
        
        // 5. 等待抢答窗口结束后处理结果
        // 这里采用延迟处理策略，由定时任务或第一个请求触发判定
        return buildBuzzResult(true, "抢答已提交", memberId, serverTime);
    }

    /**
     * 处理抢答结果（在抢答窗口结束后调用）
     */
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> processBuzzResult(Long sessionId, Long questionId) {
        String queueKey = BUZZ_QUEUE_KEY + sessionId + ":" + questionId;
        String lockKey = BUZZ_LOCK_KEY + sessionId + ":" + questionId;
        String resultKey = BUZZ_RESULT_KEY + sessionId + ":" + questionId;
        
        // 检查是否已处理
        Boolean hasResult = redisTemplate.hasKey(resultKey);
        if (Boolean.TRUE.equals(hasResult)) {
            // 返回已有结果
            Object cached = redisTemplate.opsForValue().get(resultKey);
            if (cached instanceof List) {
                return (List<Map<String, Object>>) cached;
            }
            return Collections.emptyList();
        }
        
        // 获取所有抢答记录，按时间排序
        Set<ZSetOperations.TypedTuple<Object>> buzzSet = 
                redisTemplate.opsForZSet().rangeWithScores(queueKey, 0, -1);
        
        if (buzzSet == null || buzzSet.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 设置锁定，防止后续抢答
        redisTemplate.opsForValue().set(lockKey, "locked", 5, TimeUnit.MINUTES);
        
        List<Map<String, Object>> results = new ArrayList<>();
        int rank = 1;
        Long firstMemberServerTime = null;
        
        for (ZSetOperations.TypedTuple<Object> tuple : buzzSet) {
            String memberId = tuple.getValue().toString();
            Double score = tuple.getScore();
            
            if (firstMemberServerTime == null) {
                firstMemberServerTime = score.longValue();
            }
            
            // 只接受在窗口期内的抢答
            if (score - firstMemberServerTime > BUZZ_TIMEOUT_MS) {
                break;
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("rank", rank);
            result.put("memberId", memberId);
            result.put("serverTime", score.longValue());
            result.put("isFirst", rank == 1);
            results.add(result);
            
            // 保存到数据库
            saveBuzzLog(sessionId, questionId, memberId, score.longValue(), rank == 1, rank);
            
            rank++;
        }
        
        // 缓存结果
        redisTemplate.opsForValue().set(resultKey, results, 30, TimeUnit.MINUTES);
        
        // 广播抢答结果
        wsMessageService.broadcastBuzzResult(sessionId, questionId, results);
        
        log.info("抢答判定完成: sessionId={}, questionId={}, 有效抢答数={}", 
                sessionId, questionId, results.size());
        
        return results;
    }

    /**
     * 开放抢答（新题目时调用）
     */
    public void openBuzz(Long sessionId, Long questionId) {
        String queueKey = BUZZ_QUEUE_KEY + sessionId + ":" + questionId;
        String lockKey = BUZZ_LOCK_KEY + sessionId + ":" + questionId;
        String resultKey = BUZZ_RESULT_KEY + sessionId + ":" + questionId;
        
        // 清除之前的数据
        redisTemplate.delete(queueKey);
        redisTemplate.delete(lockKey);
        redisTemplate.delete(resultKey);
        
        log.info("开放抢答: sessionId={}, questionId={}", sessionId, questionId);
    }

    /**
     * 关闭抢答
     */
    public void closeBuzz(Long sessionId, Long questionId) {
        String lockKey = BUZZ_LOCK_KEY + sessionId + ":" + questionId;
        redisTemplate.opsForValue().set(lockKey, "closed", 30, TimeUnit.MINUTES);
        log.info("关闭抢答: sessionId={}, questionId={}", sessionId, questionId);
    }

    /**
     * 获取抢答结果
     */
    public List<Map<String, Object>> getBuzzResult(Long sessionId, Long questionId) {
        String resultKey = BUZZ_RESULT_KEY + sessionId + ":" + questionId;
        Object cached = redisTemplate.opsForValue().get(resultKey);
        if (cached instanceof List) {
            return (List<Map<String, Object>>) cached;
        }
        return Collections.emptyList();
    }

    /**
     * 保存抢答记录到数据库
     */
    private void saveBuzzLog(Long sessionId, Long questionId, String memberId, 
                              Long serverTime, boolean isFirst, int rank) {
        QuizBuzzLog buzzLog = new QuizBuzzLog();
        buzzLog.setSessionId(sessionId);
        buzzLog.setQuestionId(questionId);
        
        if (memberId.startsWith("team:")) {
            buzzLog.setTeamId(Long.parseLong(memberId.substring(5)));
        } else if (memberId.startsWith("user:")) {
            buzzLog.setUserId(Long.parseLong(memberId.substring(5)));
        }
        
        buzzLog.setBuzzTime(LocalDateTime.now());
        buzzLog.setServerTime(serverTime);
        buzzLog.setIsFirst(isFirst ? 1 : 0);
        buzzLog.setRank(rank);
        buzzLog.setProcessed(1);
        
        buzzLogMapper.insert(buzzLog);
    }

    /**
     * 构建抢答结果
     */
    private Map<String, Object> buildBuzzResult(boolean success, String message, 
                                                  String memberId, long serverTime) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", message);
        result.put("memberId", memberId);
        result.put("serverTime", serverTime);
        return result;
    }
}
