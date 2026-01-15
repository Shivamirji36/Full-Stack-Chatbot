package com.chatbot.controller;

import com.chatbot.dto.ChatDTOs.*;
import com.chatbot.model.Message;
import com.chatbot.security.JwtUtil;
import com.chatbot.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    public ChatService getChatService() {
        return chatService;
    }

    public void setChatService(ChatService chatService) {
        this.chatService = chatService;
    }

    public JwtUtil getJwtUtil() {
        return jwtUtil;
    }

    public void setJwtUtil(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/{projectId}")
    public ResponseEntity<?> sendMessage(
            @PathVariable String projectId,
            @Valid @RequestBody ChatRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = getUserIdFromToken(token);
            ChatResponse response = chatService.sendMessage(projectId, request, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorMap(e.getMessage()));
        }
    }

    @GetMapping("/{projectId}/history")
    public ResponseEntity<?> getChatHistory(
            @PathVariable String projectId,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = getUserIdFromToken(token);
            List<Message> history = chatService.getChatHistory(projectId, userId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorMap(e.getMessage()));
        }
    }

    @DeleteMapping("/{projectId}/history")
    public ResponseEntity<?> clearChatHistory(
            @PathVariable String projectId,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = getUserIdFromToken(token);
            chatService.clearChatHistory(projectId, userId);
            return ResponseEntity.ok(createSuccessMap("Chat history cleared"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorMap(e.getMessage()));
        }
    }

    private String getUserIdFromToken(String token) {
        String jwt = token.substring(7);
        return jwtUtil.getUserIdFromToken(jwt);
    }

    private Map<String, String> createErrorMap(String message) {
        Map<String, String> map = new HashMap<>();
        map.put("error", message);
        return map;
    }

    private Map<String, String> createSuccessMap(String message) {
        Map<String, String> map = new HashMap<>();
        map.put("message", message);
        return map;
    }
}