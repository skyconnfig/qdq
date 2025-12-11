package com.qdq.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qdq.common.PageRequest;
import com.qdq.common.R;
import com.qdq.dto.SessionRequest;
import com.qdq.entity.QuizQuestion;
import com.qdq.entity.QuizSession;
import com.qdq.service.BuzzService;
import com.qdq.service.SessionService;
import com.qdq.websocket.WebSocketMessageService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 比赛场次控制器
 */
@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;
    private final BuzzService buzzService;
    private final WebSocketMessageService wsMessageService;

    public SessionController(SessionService sessionService, 
                              BuzzService buzzService,
                              WebSocketMessageService wsMessageService) {
        this.sessionService = sessionService;
        this.buzzService = buzzService;
        this.wsMessageService = wsMessageService;
    }

    /**
     * 分页查询场次
     */
    @GetMapping
    public R<Page<QuizSession>> page(PageRequest pageRequest,
                                      @RequestParam(required = false) Integer status,
                                      @RequestParam(required = false) String keyword) {
        Page<QuizSession> page = sessionService.page(pageRequest, status, keyword);
        return R.ok(page);
    }

    /**
     * 获取场次详情
     */
    @GetMapping("/{id}")
    public R<QuizSession> getById(@PathVariable Long id) {
        QuizSession session = sessionService.getDetail(id);
        return R.ok(session);
    }

    /**
     * 创建场次
     */
    @PostMapping
    @SaCheckRole({"SUPER_ADMIN", "HOST"})
    public R<QuizSession> create(@Valid @RequestBody SessionRequest request) {
        QuizSession session = sessionService.create(request);
        return R.ok("创建成功", session);
    }

    /**
     * 更新场次
     */
    @PutMapping("/{id}")
    @SaCheckRole({"SUPER_ADMIN", "HOST"})
    public R<QuizSession> update(@PathVariable Long id, @Valid @RequestBody SessionRequest request) {
        QuizSession session = sessionService.update(id, request);
        return R.ok("更新成功", session);
    }

    /**
     * 删除场次
     */
    @DeleteMapping("/{id}")
    @SaCheckRole({"SUPER_ADMIN", "HOST"})
    public R<Void> delete(@PathVariable Long id) {
        sessionService.delete(id);
        return R.ok("删除成功", null);
    }

    /**
     * 开始比赛
     */
    @PostMapping("/{id}/start")
    @SaCheckRole({"SUPER_ADMIN", "HOST"})
    public R<QuizSession> start(@PathVariable Long id) {
        QuizSession session = sessionService.start(id);
        // 广播比赛开始
        wsMessageService.broadcastSessionState(id, Map.of("status", "started", "session", session));
        return R.ok("比赛已开始", session);
    }

    /**
     * 暂停比赛
     */
    @PostMapping("/{id}/pause")
    @SaCheckRole({"SUPER_ADMIN", "HOST"})
    public R<QuizSession> pause(@PathVariable Long id) {
        QuizSession session = sessionService.pause(id);
        wsMessageService.broadcastSessionState(id, Map.of("status", "paused"));
        return R.ok("比赛已暂停", session);
    }

    /**
     * 恢复比赛
     */
    @PostMapping("/{id}/resume")
    @SaCheckRole({"SUPER_ADMIN", "HOST"})
    public R<QuizSession> resume(@PathVariable Long id) {
        QuizSession session = sessionService.resume(id);
        wsMessageService.broadcastSessionState(id, Map.of("status", "resumed"));
        return R.ok("比赛已恢复", session);
    }

    /**
     * 结束比赛
     */
    @PostMapping("/{id}/finish")
    @SaCheckRole({"SUPER_ADMIN", "HOST"})
    public R<QuizSession> finish(@PathVariable Long id) {
        QuizSession session = sessionService.finish(id);
        wsMessageService.broadcastSessionState(id, Map.of("status", "finished"));
        return R.ok("比赛已结束", session);
    }

    /**
     * 下一题
     */
    @PostMapping("/{id}/next-question")
    @SaCheckRole({"SUPER_ADMIN", "HOST"})
    public R<QuizQuestion> nextQuestion(@PathVariable Long id) {
        QuizQuestion question = sessionService.nextQuestion(id);
        
        // 开放抢答
        buzzService.openBuzz(id, question.getId());
        
        // 广播题目（隐藏答案）
        QuizQuestion questionForBroadcast = new QuizQuestion();
        questionForBroadcast.setId(question.getId());
        questionForBroadcast.setType(question.getType());
        questionForBroadcast.setTitle(question.getTitle());
        questionForBroadcast.setContent(question.getContent());
        questionForBroadcast.setOptions(question.getOptions());
        questionForBroadcast.setScore(question.getScore());
        questionForBroadcast.setDifficulty(question.getDifficulty());
        questionForBroadcast.setAttachments(question.getAttachments());
        
        wsMessageService.broadcastQuestion(id, questionForBroadcast);
        
        return R.ok(question);
    }

    /**
     * 获取当前题目
     */
    @GetMapping("/{id}/current-question")
    public R<QuizQuestion> getCurrentQuestion(@PathVariable Long id) {
        QuizQuestion question = sessionService.getCurrentQuestion(id);
        return R.ok(question);
    }

    /**
     * 处理抢答结果
     */
    @PostMapping("/{id}/process-buzz/{questionId}")
    @SaCheckRole({"SUPER_ADMIN", "HOST"})
    public R<List<Map<String, Object>>> processBuzz(@PathVariable Long id, @PathVariable Long questionId) {
        List<Map<String, Object>> results = buzzService.processBuzzResult(id, questionId);
        return R.ok(results);
    }

    /**
     * 关闭抢答
     */
    @PostMapping("/{id}/close-buzz/{questionId}")
    @SaCheckRole({"SUPER_ADMIN", "HOST"})
    public R<Void> closeBuzz(@PathVariable Long id, @PathVariable Long questionId) {
        buzzService.closeBuzz(id, questionId);
        return R.ok("抢答已关闭", null);
    }

    /**
     * 获取场次在线人数
     */
    @GetMapping("/{id}/online-count")
    public R<Integer> getOnlineCount(@PathVariable Long id) {
        int count = wsMessageService.getSessionOnlineCount(id);
        return R.ok(count);
    }
}
