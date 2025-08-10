// src/main/java/com/edulink/backend/model/entity/Message.java
package com.edulink.backend.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    
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