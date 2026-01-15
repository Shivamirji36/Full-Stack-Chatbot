package com.chatbot.service;

import com.chatbot.dto.ChatDTOs.*;
import com.chatbot.model.Message;
import com.chatbot.model.Project;
import com.chatbot.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    @Autowired
    private ProjectService projectService;

    public ProjectService getProjectService() {
        return projectService;
    }

    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }

    public WebClient getWebClient() {
        return webClient;
    }

    public MessageRepository getMessageRepository() {
        return messageRepository;
    }

    public void setMessageRepository(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public String getOpenaiApiKey() {
        return openaiApiKey;
    }

    public void setOpenaiApiKey(String openaiApiKey) {
        this.openaiApiKey = openaiApiKey;
    }

    @Autowired
    private MessageRepository messageRepository;

    @Value("${openai.api.key}")
    private String openaiApiKey;

    private final WebClient webClient;

    public ChatService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .build();
    }

    public ChatResponse sendMessage(String projectId, ChatRequest request, String userId) {
        Project project = projectService.getProject(projectId, userId);

        Message userMessage = new Message(projectId, "user", request.getMessage());
        messageRepository.save(userMessage);

        List<Message> history = messageRepository.findByProjectIdOrderByTimestampAsc(projectId);

        List<Map<String, String>> messages = new ArrayList<>();

        if (project.getSystemPrompt() != null && !project.getSystemPrompt().isEmpty()) {
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", project.getSystemPrompt());
            messages.add(systemMessage);
        }

        int startIndex = Math.max(0, history.size() - 10);
        for (int i = startIndex; i < history.size(); i++) {
            Message msg = history.get(i);
            Map<String, String> message = new HashMap<>();
            message.put("role", msg.getRole());
            message.put("content", msg.getContent());
            messages.add(message);
        }

        String aiResponse = callOpenAI(messages, project.getModel());

        Message assistantMessage = new Message(projectId, "assistant", aiResponse);
        assistantMessage = messageRepository.save(assistantMessage);

        return new ChatResponse(aiResponse, assistantMessage.getId());
    }

    private String callOpenAI(List<Map<String, String>> messages, String model) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 1000);
            requestBody.put("temperature", 0.7);

            Map<String, Object> response = webClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + openaiApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            Map<String, Object> choice = choices.get(0);
            Map<String, String> message = (Map<String, String>) choice.get("message");
            return message.get("content");

        } catch (Exception e) {
            return "I apologize, but I'm having trouble connecting to the AI service right now. Please try again later.";
        }
    }

    public List<Message> getChatHistory(String projectId, String userId) {
        projectService.getProject(projectId, userId);
        return messageRepository.findByProjectIdOrderByTimestampAsc(projectId);
    }

    public void clearChatHistory(String projectId, String userId) {
        projectService.getProject(projectId, userId);
        messageRepository.deleteByProjectId(projectId);
    }
}