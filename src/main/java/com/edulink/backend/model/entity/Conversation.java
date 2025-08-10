// src/main/java/com/edulink/backend/model/entity/Conversation.java
package com.edulink.backend.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "conversations")
public class Conversation {
    
    @Id
    private String id;
    
    private Set<String> participantIds; // User IDs involved in conversation
    private String subject;
    private String courseId; // Optional - if conversation is related to a course
    private Status status;
    private Priority priority;
    
    // Last message info for quick access
    private String lastMessageContent;
    private LocalDateTime lastMessageAt;
    private String lastMessageSenderId;
    
    @Builder.Default
    private List<Message> messages = new ArrayList<>();
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Enums
    public enum Status {
        ACTIVE, RESOLVED, ARCHIVED
    }
    
    public enum Priority {
        LOW, MEDIUM, HIGH
    }
    
    // Inner Message class - matches frontend expectations
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String id;
        private String senderId;
        private String content;
        private LocalDateTime timestamp;
        private boolean isRead;
        private List<Attachment> attachments;
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Attachment {
            private String resourceId;
            private String originalFilename;
        }
    }
}