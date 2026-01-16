package com.chatbot.service;

import com.chatbot.dto.ChatDTOs.ChatRequest;
import com.chatbot.dto.ChatDTOs.ChatResponse;
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

    // ✅ Constructor injection (correct + safe)
    public ChatService(
            ProjectService projectService,
            MessageRepository messageRepository,
            @Value("${huggingface.api.key}") String huggingFaceApiKey
    ) {
        this.projectService = projectService;
        this.messageRepository = messageRepository;

        this.webClient = WebClient.builder()
                .baseUrl("https://api-inference.huggingface.co")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + huggingFaceApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }


    public ChatResponse sendMessage(String projectId, ChatRequest request, String userId) {

        Project project = projectService.getProject(projectId, userId);

        // Save user message
        messageRepository.save(
                new Message(projectId, "user", request.getMessage())
        );

        List<Message> history =
                messageRepository.findByProjectIdOrderByTimestampAsc(projectId);

        // Build prompt (HF models are prompt-based)
        String prompt = buildPrompt(project, history);

        String aiResponse = callHuggingFace(prompt);

        Message assistantMessage =
                messageRepository.save(new Message(projectId, "assistant", aiResponse));

        return new ChatResponse(aiResponse, assistantMessage.getId());
    }


    private String buildPrompt(Project project, List<Message> history) {

        StringBuilder prompt = new StringBuilder();

        if (project.getSystemPrompt() != null && !project.getSystemPrompt().isBlank()) {
            prompt.append("System: ")
                    .append(project.getSystemPrompt())
                    .append("\n\n");
        }

        int start = Math.max(0, history.size() - 6);
        for (int i = start; i < history.size(); i++) {
            Message msg = history.get(i);
            prompt.append(msg.getRole())
                    .append(": ")
                    .append(msg.getContent())
                    .append("\n");
        }

        prompt.append("assistant:");
        return prompt.toString();
    }

    private String callHuggingFace(String prompt) {

        try {
            Map<String, Object> requestBody = Map.of(
                    "inputs", prompt,
                    "parameters", Map.of(
                            "max_new_tokens", 300,
                            "temperature", 0.7,
                            "return_full_text", false
                    )
            );

            Object response = webClient.post()
                    // ✅ MISTRAL v0.3
                    .uri("/models/mistralai/Mistral-7B-Instruct-v0.3")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();

            // HF returns: [ { "generated_text": "..." } ]
            List<Map<String, Object>> result =
                    (List<Map<String, Object>>) response;

            return result.get(0)
                    .get("generated_text")
                    .toString()
                    .trim();

        } catch (Exception e) {
            e.printStackTrace();
            return "⚠️ Hugging Face AI service unavailable. Please try again.";
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
