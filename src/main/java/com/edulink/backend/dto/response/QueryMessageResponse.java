// File Path: src/main/java/com/edulink/backend/dto/response/QueryMessageResponse.java
package com.edulink.backend.dto.response;

import com.edulink.backend.model.entity.Query.QueryMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryMessageResponse {
    private String id;
    private String sender;
    private String senderType;
    private String senderName;
    private String content;
    private LocalDateTime timestamp;
    private boolean isRead;
    private LocalDateTime readAt;

    public static QueryMessageResponse fromQueryMessage(QueryMessage message) {
        if (message == null) return null;
        
        return QueryMessageResponse.builder()
                .id(message.getId())
                .sender(message.getSender())
                .senderType(message.getSenderType())
                .senderName(message.getSenderName())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .isRead(message.isRead())
                .readAt(message.getReadAt())
                .build();
    }
}