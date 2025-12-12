package com.qdq.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qdq.dto.LeaderboardRequest;
import com.qdq.entity.LeaderboardConfig;
import com.qdq.entity.QuizSessionParticipant;
import com.qdq.exception.BusinessException;
import com.qdq.mapper.LeaderboardConfigMapper;
import com.qdq.mapper.QuizSessionParticipantMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 排行版服务
 */
@Service
public class LeaderboardService extends ServiceImpl<LeaderboardConfigMapper, LeaderboardConfig> {

    private final QuizSessionParticipantMapper participantMapper;

    public LeaderboardService(QuizSessionParticipantMapper participantMapper) {
        this.participantMapper = participantMapper;
    }

    /**
     * 创建或更新排行版配置
     */
    @Transactional(rollbackFor = Exception.class)
    public LeaderboardConfig saveOrUpdate(LeaderboardRequest request) {
        LeaderboardConfig config = this.getOne(new LambdaQueryWrapper<LeaderboardConfig>()
                .eq(LeaderboardConfig::getSessionId, request.getSessionId()));

        if (config == null) {
            config = new LeaderboardConfig();
            BeanUtil.copyProperties(request, config);
            config.setCreatedBy(StpUtil.getLoginIdAsLong());
            this.save(config);
        } else {
            BeanUtil.copyProperties(request, config, "id", "createdBy", "createdAt");
            config.setUpdatedBy(StpUtil.getLoginIdAsLong());
            this.updateById(config);
        }
        return config;
    }

    /**
     * 获取排行版配置
     */
    public LeaderboardConfig getBySessionId(Long sessionId) {
        LeaderboardConfig config = this.getOne(new LambdaQueryWrapper<LeaderboardConfig>()
                .eq(LeaderboardConfig::getSessionId, sessionId));
        
        if (config == null) {
            throw new BusinessException("排行版配置不存在");
        }
        return config;
    }

    /**
     * 获取实时排行版数据
     */
    public List<Map<String, Object>> getRealTimeLeaderboard(Long sessionId) {
        LeaderboardConfig config = getBySessionId(sessionId);
        
        // 获取所有参赛者
        List<QuizSessionParticipant> participants = participantMapper.selectList(
                new LambdaQueryWrapper<QuizSessionParticipant>()
                        .eq(QuizSessionParticipant::getSessionId, sessionId)
        );

        // 按配置的排序方式排序
        if (config.getSortType() != null && config.getSortType() == 2) {
            // 按答题数降序
            participants.sort(Comparator.comparingInt(QuizSessionParticipant::getAnsweredCount).reversed()
                    .thenComparingInt(QuizSessionParticipant::getTotalScore).reversed());
        } else {
            // 默认按得分降序
            participants.sort(Comparator.comparingInt(QuizSessionParticipant::getTotalScore).reversed()
                    .thenComparingInt(QuizSessionParticipant::getAnsweredCount).reversed());
        }

        // 构建排行版数据
        List<Map<String, Object>> leaderboardData = new ArrayList<>();
        for (int i = 0; i < participants.size(); i++) {
            QuizSessionParticipant participant = participants.get(i);
            participant.setRank(i + 1);
            
            Map<String, Object> item = new java.util.HashMap<>();
            item.put("rank", i + 1);
            item.put("name", participant.getUserName() != null ? participant.getUserName() : participant.getTeamName());
            item.put("totalScore", participant.getTotalScore());
            item.put("correctCount", participant.getCorrectCount());
            item.put("wrongCount", participant.getWrongCount());
            item.put("answeredCount", participant.getAnsweredCount());
            item.put("buzzCount", participant.getBuzzCount());
            item.put("buzzSuccessCount", participant.getBuzzSuccessCount());
            
            leaderboardData.add(item);
        }

        return leaderboardData;
    }

    /**
     * 更新排行版名称
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateLeaderboardName(Long sessionId, String leaderboardName) {
        LeaderboardConfig config = getBySessionId(sessionId);
        config.setLeaderboardName(leaderboardName);
        config.setUpdatedBy(StpUtil.getLoginIdAsLong());
        this.updateById(config);
    }

    /**
     * 更新排序方式
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateSortType(Long sessionId, Integer sortType) {
        LeaderboardConfig config = getBySessionId(sessionId);
        config.setSortType(sortType);
        config.setUpdatedBy(StpUtil.getLoginIdAsLong());
        this.updateById(config);
    }

    /**
     * 删除排行版配置
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteBySessionId(Long sessionId) {
        this.remove(new LambdaQueryWrapper<LeaderboardConfig>()
                .eq(LeaderboardConfig::getSessionId, sessionId));
    }
}
