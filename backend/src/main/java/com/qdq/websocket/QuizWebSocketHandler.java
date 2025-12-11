package com.qdq.websocket;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qdq.service.BuzzService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * 抢答系统WebSocket处理器
 */
@Slf4j
@Component
public class QuizWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketMessageService messageService;
    private final BuzzService buzzService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public QuizWebSocketHandler(WebSocketMessageService messageService, BuzzService buzzService) {
        this.messageService = messageService;
        this.buzzService = buzzService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        messageService.registerSession(session);
        
        // 发送连接成功消息
        Map<String, Object> response = new HashMap<>();
        response.put("event", "connected");
        response.put("data", Map.of("sessionId", session.getId()));
        response.put("timestamp", System.currentTimeMillis());
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("收到WebSocket消息: {}", payload);
        
        try {
            Map<String, Object> msg = objectMapper.readValue(payload, 
                    new TypeReference<Map<String, Object>>() {});
            
            String event = (String) msg.get("event");
            Map<String, Object> data = (Map<String, Object>) msg.getOrDefault("data", new HashMap<>());
            
            switch (event) {
                case "join_session" -> handleJoinSession(session, data);
                case "leave_session" -> handleLeaveSession(session, data);
                case "client_buzz" -> handleBuzz(session, data);
                case "submit_answer" -> handleSubmitAnswer(session, data);
                case "ping" -> handlePing(session);
                default -> sendError(session, "未知事件类型: " + event);
            }
        } catch (Exception e) {
            log.error("处理WebSocket消息失败", e);
            sendError(session, "消息处理失败: " + e.getMessage());
        }
    }

    /**
     * 处理加入场次
     */
    private void handleJoinSession(WebSocketSession session, Map<String, Object> data) throws Exception {
        Long sessionId = getLong(data, "sessionId");
        Long teamId = getLong(data, "teamId");
        String token = (String) data.get("token");
        
        if (sessionId == null) {
            sendError(session, "场次ID不能为空");
            return;
        }
        
        // 验证token（可选）
        Long userId = null;
        if (token != null) {
            try {
                Object loginId = StpUtil.getLoginIdByToken(token);
                if (loginId != null) {
                    userId = Long.parseLong(loginId.toString());
                }
            } catch (Exception e) {
                log.warn("Token验证失败: {}", e.getMessage());
            }
        }
        
        // 订阅场次
        messageService.subscribeSession(session.getId(), sessionId, userId);
        
        // 发送加入成功消息
        Map<String, Object> response = new HashMap<>();
        response.put("event", "join_success");
        response.put("data", Map.of(
                "sessionId", sessionId,
                "userId", userId,
                "teamId", teamId
        ));
        response.put("timestamp", System.currentTimeMillis());
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        
        log.info("用户加入场次: sessionId={}, userId={}, teamId={}", sessionId, userId, teamId);
    }

    /**
     * 处理离开场次
     */
    private void handleLeaveSession(WebSocketSession session, Map<String, Object> data) throws Exception {
        Long sessionId = getLong(data, "sessionId");
        if (sessionId != null) {
            messageService.unsubscribeSession(session.getId(), sessionId);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("event", "leave_success");
        response.put("timestamp", System.currentTimeMillis());
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    /**
     * 处理抢答
     */
    private void handleBuzz(WebSocketSession session, Map<String, Object> data) throws Exception {
        Long sessionId = getLong(data, "sessionId");
        Long questionId = getLong(data, "questionId");
        Long userId = getLong(data, "userId");
        Long teamId = getLong(data, "teamId");
        
        if (sessionId == null || questionId == null) {
            sendError(session, "场次ID和题目ID不能为空");
            return;
        }
        
        if (userId == null && teamId == null) {
            sendError(session, "用户ID或队伍ID不能为空");
            return;
        }
        
        try {
            Map<String, Object> result = buzzService.buzz(sessionId, questionId, userId, teamId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("event", "buzz_response");
            response.put("data", result);
            response.put("timestamp", System.currentTimeMillis());
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        } catch (Exception e) {
            sendError(session, e.getMessage());
        }
    }

    /**
     * 处理提交答案
     */
    private void handleSubmitAnswer(WebSocketSession session, Map<String, Object> data) throws Exception {
        // TODO: 实现答案提交逻辑
        Map<String, Object> response = new HashMap<>();
        response.put("event", "answer_received");
        response.put("data", Map.of("status", "processing"));
        response.put("timestamp", System.currentTimeMillis());
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    /**
     * 处理心跳
     */
    private void handlePing(WebSocketSession session) throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("event", "pong");
        response.put("timestamp", System.currentTimeMillis());
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    /**
     * 发送错误消息
     */
    private void sendError(WebSocketSession session, String message) throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("event", "error");
        response.put("data", Map.of("message", message));
        response.put("timestamp", System.currentTimeMillis());
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        messageService.removeSession(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误: sessionId={}", session.getId(), exception);
        messageService.removeSession(session);
    }

    /**
     * 从Map中获取Long值
     */
    private Long getLong(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
