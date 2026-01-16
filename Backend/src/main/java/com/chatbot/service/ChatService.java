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
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    private final ProjectService projectService;
    private final MessageRepository messageRepository;
    private final WebClient webClient;

    // 🚀 Groq official fast model
    private static final String MODEL = "mixtral-8x7b-32768";

    public ChatService(
            ProjectService projectService,
            MessageRepository messageRepository,
            @Value("${groq.api.key:}") String groqApiKey
    ) {
        if (groqApiKey == null || groqApiKey.isBlank()) {
            throw new IllegalStateException(
                    "❌ GROQ_API_KEY is not set in Render environment variables"
            );
        }

        this.projectService = projectService;
        this.messageRepository = messageRepository;

        this.webClient = WebClient.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + groqApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /* ===========================
       SEND MESSAGE
    ============================ */
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

        List<Map<String, String>> messages =
                buildMessages(project, history);

        String aiResponse = callGroq(messages);

        Message assistantMessage =
                messageRepository.save(
                        new Message(projectId, "assistant", aiResponse)
                );

        return new ChatResponse(aiResponse, assistantMessage.getId());
    }

    /* ===========================
       MESSAGE BUILDER
    ============================ */
    private List<Map<String, String>> buildMessages(
            Project project,
            List<Message> history
    ) {
        List<Map<String, String>> messages = new ArrayList<>();

        if (project.getSystemPrompt() != null &&
                !project.getSystemPrompt().isBlank()) {
            messages.add(Map.of(
                    "role", "system",
                    "content", project.getSystemPrompt()
            ));
        }

        int start = Math.max(0, history.size() - 10);
        for (int i = start; i < history.size(); i++) {
            Message msg = history.get(i);
            messages.add(Map.of(
                    "role", msg.getRole(),
                    "content", msg.getContent()
            ));
        }

        return messages;
    }


    @SuppressWarnings("unchecked")
    private String callGroq(List<Map<String, String>> messages) {

        try {
            Map<String, Object> body = Map.of(
                    "model", MODEL,
                    "messages", messages,
                    "temperature", 0.7,
                    "max_tokens", 512
            );

            Map<String, Object> response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .map(errorBody -> {
                                        System.err.println("🔥 GROQ ERROR RESPONSE:");
                                        System.err.println(errorBody);
                                        return new RuntimeException(errorBody);
                                    })
                    )
                    .bodyToMono(Map.class)
                    .block();

            // 🔍 Handle Groq error JSON
            if (response.containsKey("error")) {
                Map<String, Object> error =
                        (Map<String, Object>) response.get("error");

                return "⚠️ Groq error: " + error.get("message");
            }

            List<Map<String, Object>> choices =
                    (List<Map<String, Object>>) response.get("choices");

            if (choices == null || choices.isEmpty()) {
                return "⚠️ Groq returned no choices.";
            }

            Map<String, Object> message =
                    (Map<String, Object>) choices.get(0).get("message");

            return message.get("content").toString().trim();

        } catch (Exception e) {
            System.err.println("🔥 GROQ CALL FAILED");
            e.printStackTrace();
            return "⚠️ AI service unavailable. Please try again.";
        }
    }


    /* ===========================
       HISTORY
    ============================ */
    public List<Message> getChatHistory(String projectId, String userId) {
        projectService.getProject(projectId, userId);
        return messageRepository.findByProjectIdOrderByTimestampAsc(projectId);
    }

    public void clearChatHistory(String projectId, String userId) {
        projectService.getProject(projectId, userId);
        messageRepository.deleteByProjectId(projectId);
    }
}
