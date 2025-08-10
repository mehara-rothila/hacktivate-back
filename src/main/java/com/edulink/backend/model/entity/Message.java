// src/main/java/com/edulink/backend/model/entity/Message.java
package com.edulink.backend.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    private String id; // Unique ID for each message
    private String senderId;
    private String content;
    private LocalDateTime timestamp;
    private boolean isRead;
    private List<Attachment> attachments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Attachment {
        private String resourceId; // Link to the Resource entity
        private String originalFilename;
    }
}