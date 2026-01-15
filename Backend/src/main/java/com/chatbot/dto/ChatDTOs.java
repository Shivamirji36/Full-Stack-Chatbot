package com.chatbot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ChatDTOs {

    @Data
    @NoArgsConstructor
    public static class ChatRequest {
        @NotBlank(message = "Message is required")
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    @Data
    public static class ChatResponse {
        private String response;
        private String messageId;

        public ChatResponse() {}

        // ✅ REQUIRED all-args constructor
        public ChatResponse(String response, String messageId) {
            this.response = response;
            this.messageId = messageId;
        }

        public String getMessageId() {
            return messageId;
        }

        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }

        public String getResponse() {
            return response;
        }

        public void setResponse(String response) {
            this.response = response;
        }
    }
}