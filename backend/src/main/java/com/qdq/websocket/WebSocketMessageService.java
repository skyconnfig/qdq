package com.qdq.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket消息服务
 */
@Slf4j
@Service
public class WebSocketMessageService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 所有连接的会话
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    // 场次订阅关系：sessionId -> Set<WebSocketSession Id>
    private final Map<Long, Set<String>> sessionSubscriptions = new ConcurrentHashMap<>();
    
    // 用户会话映射：userId -> WebSocketSession Id
    private final Map<Long, String> userSessions = new ConcurrentHashMap<>();

    /**
     * 注册会话
     */
    public void registerSession(WebSocketSession session) {
        sessions.put(session.getId(), session);
        log.info("WebSocket连接建立: {}", session.getId());
    }

    /**
     * 移除会话
     */
    public void removeSession(WebSocketSession session) {
        sessions.remove(session.getId());
        
        // 清理订阅关系
        sessionSubscriptions.values().forEach(set -> set.remove(session.getId()));
        
        // 清理用户映射
        userSessions.values().removeIf(id -> id.equals(session.getId()));
        
        log.info("WebSocket连接断开: {}", session.getId());
    }

    /**
     * 订阅场次
     */
    public void subscribeSession(String wsSessionId, Long quizSessionId, Long userId) {
        sessionSubscriptions.computeIfAbsent(quizSessionId, k -> ConcurrentHashMap.newKeySet())
                .add(wsSessionId);
        
        if (userId != null) {
            userSessions.put(userId, wsSessionId);
        }
        
        log.info("用户订阅场次: wsSessionId={}, quizSessionId={}, userId={}", 
                wsSessionId, quizSessionId, userId);
    }

    /**
     * 取消订阅场次
     */
    public void unsubscribeSession(String wsSessionId, Long quizSessionId) {
        Set<String> subscribers = sessionSubscriptions.get(quizSessionId);
        if (subscribers != null) {
            subscribers.remove(wsSessionId);
        }
    }

    /**
     * 广播消息到场次
     */
    public void broadcastToSession(Long quizSessionId, String event, Object data) {
        Set<String> subscribers = sessionSubscriptions.get(quizSessionId);
        if (subscribers == null || subscribers.isEmpty()) {
            return;
        }
        
        Map<String, Object> message = new HashMap<>();
        message.put("event", event);
        message.put("data", data);
        message.put("timestamp", System.currentTimeMillis());
        
        String jsonMessage;
        try {
            jsonMessage = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("消息序列化失败", e);
            return;
        }
        
        TextMessage textMessage = new TextMessage(jsonMessage);
        
        for (String wsSessionId : subscribers) {
            WebSocketSession session = sessions.get(wsSessionId);
            if (session != null && session.isOpen()) {
                try {
                    session.sendMessage(textMessage);
                } catch (IOException e) {
                    log.error("发送消息失败: sessionId={}", wsSessionId, e);
                }
            }
        }
        
        log.debug("广播消息到场次: quizSessionId={}, event={}, 订阅者数={}", 
                quizSessionId, event, subscribers.size());
    }

    /**
     * 发送消息给指定用户
     */
    public void sendToUser(Long userId, String event, Object data) {
        String wsSessionId = userSessions.get(userId);
        if (wsSessionId == null) {
            return;
        }
        
        WebSocketSession session = sessions.get(wsSessionId);
        if (session == null || !session.isOpen()) {
            return;
        }
        
        Map<String, Object> message = new HashMap<>();
        message.put("event", event);
        message.put("data", data);
        message.put("timestamp", System.currentTimeMillis());
        
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(jsonMessage));
        } catch (Exception e) {
            log.error("发送消息给用户失败: userId={}", userId, e);
        }
    }

    /**
     * 广播抢答结果
     */
    public void broadcastBuzzResult(Long quizSessionId, Long questionId, List<Map<String, Object>> results) {
        Map<String, Object> data = new HashMap<>();
        data.put("questionId", questionId);
        data.put("results", results);
        broadcastToSession(quizSessionId, "buzz_result", data);
    }

    /**
     * 广播题目
     */
    public void broadcastQuestion(Long quizSessionId, Object question) {
        broadcastToSession(quizSessionId, "question_push", question);
    }

    /**
     * 广播分数更新
     */
    public void broadcastScoreUpdate(Long quizSessionId, Object scores) {
        broadcastToSession(quizSessionId, "score_update", scores);
    }

    /**
     * 广播场次状态
     */
    public void broadcastSessionState(Long quizSessionId, Object state) {
        broadcastToSession(quizSessionId, "session_state", state);
    }

    /**
     * 广播倒计时
     */
    public void broadcastCountdown(Long quizSessionId, int seconds) {
        Map<String, Object> data = new HashMap<>();
        data.put("seconds", seconds);
        broadcastToSession(quizSessionId, "countdown", data);
    }

    /**
     * 获取在线人数
     */
    public int getOnlineCount() {
        return sessions.size();
    }

    /**
     * 广播排行版
     */
    public void broadcastLeaderboard(Long quizSessionId, List<Map<String, Object>> leaderboard) {
        Map<String, Object> data = new HashMap<>();
        data.put("leaderboard", leaderboard);
        broadcastToSession(quizSessionId, "leaderboard_update", data);
    }

    /**
     * 广播答题进度
     */
    public void broadcastAnswerProgress(Long quizSessionId, Map<String, Object> progressMap) {
        Map<String, Object> data = new HashMap<>();
        data.put("progress", progressMap);
        broadcastToSession(quizSessionId, "answer_progress_update", data);
    }

    /**
     * 广播排行版配置更新
     */
    public void broadcastLeaderboardConfig(Long quizSessionId, String leaderboardName) {
        Map<String, Object> data = new HashMap<>();
        data.put("leaderboardName", leaderboardName);
        broadcastToSession(quizSessionId, "leaderboard_config_update", data);
    }

    /**
     * 获取场次在线人数
     */
    public int getSessionOnlineCount(Long quizSessionId) {
        Set<String> subscribers = sessionSubscriptions.get(quizSessionId);
        return subscribers != null ? subscribers.size() : 0;
    }
}
