package com.qdq.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.qdq.common.R;
import com.qdq.dto.LeaderboardRequest;
import com.qdq.entity.LeaderboardConfig;
import com.qdq.service.LeaderboardService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 排行版管理控制器
 */
@RestController
@RequestMapping("/api/leaderboards")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    /**
     * 获取或创建排行版配置
     */
    @PostMapping
    @SaCheckRole({"SUPER_ADMIN", "HOST"})
    public R<LeaderboardConfig> saveOrUpdate(@Valid @RequestBody LeaderboardRequest request) {
        LeaderboardConfig config = leaderboardService.saveOrUpdate(request);
        return R.ok("保存成功", config);
    }

    /**
     * 获取排行版配置
     */
    @GetMapping("/session/{sessionId}")
    public R<LeaderboardConfig> getBySessionId(@PathVariable Long sessionId) {
        LeaderboardConfig config = leaderboardService.getBySessionId(sessionId);
        return R.ok(config);
    }

    /**
     * 获取实时排行版数据
     */
    @GetMapping("/session/{sessionId}/data")
    public R<List<Map<String, Object>>> getRealTimeLeaderboard(@PathVariable Long sessionId) {
        List<Map<String, Object>> leaderboard = leaderboardService.getRealTimeLeaderboard(sessionId);
        return R.ok(leaderboard);
    }

    /**
     * 更新排行版名称
     */
    @PutMapping("/session/{sessionId}/name")
    @SaCheckRole({"SUPER_ADMIN", "HOST"})
    public R<Void> updateLeaderboardName(@PathVariable Long sessionId, @RequestParam String leaderboardName) {
        leaderboardService.updateLeaderboardName(sessionId, leaderboardName);
        return R.ok("更新成功", null);
    }

    /**
     * 更新排序方式
     */
    @PutMapping("/session/{sessionId}/sort-type")
    @SaCheckRole({"SUPER_ADMIN", "HOST"})
    public R<Void> updateSortType(@PathVariable Long sessionId, @RequestParam Integer sortType) {
        leaderboardService.updateSortType(sessionId, sortType);
        return R.ok("更新成功", null);
    }

    /**
     * 删除排行版配置
     */
    @DeleteMapping("/session/{sessionId}")
    @SaCheckRole({"SUPER_ADMIN", "HOST"})
    public R<Void> deleteBySessionId(@PathVariable Long sessionId) {
        leaderboardService.deleteBySessionId(sessionId);
        return R.ok("删除成功", null);
    }
}
