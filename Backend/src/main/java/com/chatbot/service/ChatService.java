package com.chatbot.service;

import com.chatbot.dto.ChatDTOs.*;
import com.chatbot.model.Message;
import com.chatbot.model.Project;
import com.chatbot.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
public class ChatService {

    private final ProjectService projectService;
    private final MessageRepository messageRepository;
    private final WebClient webClient;

    // ✅ Constructor injection (CORRECT WAY)
    public ChatService(
            ProjectService projectService,
            MessageRepository messageRepository,
            @Value("${openai.api.key}") String openaiApiKey
    ) {
        this.projectService = projectService;
        this.messageRepository = messageRepository;

        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + openaiApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    // ================= SEND MESSAGE =================

    public ChatResponse sendMessage(String projectId, ChatRequest request, String userId) {

        Project project = projectService.getProject(projectId, userId);

        // Save user message
        Message userMessage = new Message(projectId, "user", request.getMessage());
        messageRepository.save(userMessage);

        List<Message> history =
                messageRepository.findByProjectIdOrderByTimestampAsc(projectId);

        List<Map<String, String>> messages = new ArrayList<>();

        // System prompt
        if (project.getSystemPrompt() != null && !project.getSystemPrompt().isEmpty()) {
            messages.add(Map.of(
                    "role", "system",
                    "content", project.getSystemPrompt()
            ));
        }

        // Last 10 messages
        int startIndex = Math.max(0, history.size() - 10);
        for (int i = startIndex; i < history.size(); i++) {
            Message msg = history.get(i);
            messages.add(Map.of(
                    "role", msg.getRole(),
                    "content", msg.getContent()
            ));
        }

        String aiResponse = callOpenAI(messages, project.getModel());

        Message assistantMessage =
                messageRepository.save(new Message(projectId, "assistant", aiResponse));

        return new ChatResponse(aiResponse, assistantMessage.getId());
    }

    // ================= OPENAI CALL =================

    private String callOpenAI(List<Map<String, String>> messages, String model) {

        try {
            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", messages,
                    "max_tokens", 1000,
                    "temperature", 0.7
            );

            Map response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            List<Map<String, Object>> choices =
                    (List<Map<String, Object>>) response.get("choices");

            Map<String, Object> message =
                    (Map<String, Object>) choices.get(0).get("message");

            return message.get("content").toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "⚠️ AI service is currently unavailable. Please try again later.";
        }
    }

    // ================= HISTORY =================

    public List<Message> getChatHistory(String projectId, String userId) {
        projectService.getProject(projectId, userId);
        return messageRepository.findByProjectIdOrderByTimestampAsc(projectId);
    }

    public void clearChatHistory(String projectId, String userId) {
        projectService.getProject(projectId, userId);
        messageRepository.deleteByProjectId(projectId);
    }
}
