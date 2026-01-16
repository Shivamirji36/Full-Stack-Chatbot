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

import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    private final ProjectService projectService;
    private final MessageRepository messageRepository;
    private final WebClient webClient;

    // 🔥 HF model (stable & good)
    private static final String MODEL =
            "mistralai/Mistral-7B-Instruct-v0.3";

    // ✅ Constructor injection (Render-safe)
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

    // ================= SEND MESSAGE =================

    public ChatResponse sendMessage(
            String projectId,
            ChatRequest request,
            String userId
    ) {

        Project project = projectService.getProject(projectId, userId);

        // Save user message
        messageRepository.save(
                new Message(projectId, "user", request.getMessage())
        );

        List<Message> history =
                messageRepository.findByProjectIdOrderByTimestampAsc(projectId);

        String prompt = buildPrompt(project, history);

        String aiResponse = callHuggingFace(prompt);

        Message assistantMessage =
                messageRepository.save(
                        new Message(projectId, "assistant", aiResponse)
                );

        return new ChatResponse(aiResponse, assistantMessage.getId());
    }

    // ================= PROMPT BUILDER =================

    private String buildPrompt(Project project, List<Message> history) {

        StringBuilder prompt = new StringBuilder();

        if (project.getSystemPrompt() != null &&
                !project.getSystemPrompt().isBlank()) {
            prompt.append("System: ")
                    .append(project.getSystemPrompt())
                    .append("\n\n");
        }

        int start = Math.max(0, history.size() - 6);
        for (int i = start; i < history.size(); i++) {
            Message msg = history.get(i);
            prompt.append(capitalize(msg.getRole()))
                    .append(": ")
                    .append(msg.getContent())
                    .append("\n");
        }

        prompt.append("Assistant:");
        return prompt.toString();
    }

    private String capitalize(String role) {
        if (role == null || role.isEmpty()) return role;
        return role.substring(0, 1).toUpperCase() + role.substring(1);
    }

    // ================= HF API CALL =================

    @SuppressWarnings("unchecked")
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

            Object rawResponse = webClient.post()
                    .uri("/models/" + MODEL)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();

            // 🔥 HF cold-start or error payload
            if (rawResponse instanceof Map) {
                Map<String, Object> error =
                        (Map<String, Object>) rawResponse;

                if (error.containsKey("error")) {
                    return "⚠️ AI model is warming up. Please try again in a few seconds.";
                }
            }

            List<Map<String, Object>> result =
                    (List<Map<String, Object>>) rawResponse;

            if (result.isEmpty() ||
                    !result.get(0).containsKey("generated_text")) {
                return "⚠️ AI response unavailable.";
            }

            return result.get(0)
                    .get("generated_text")
                    .toString()
                    .trim();

        } catch (Exception e) {
            e.printStackTrace();
            return "⚠️ Hugging Face AI service unavailable. Please try again.";
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
