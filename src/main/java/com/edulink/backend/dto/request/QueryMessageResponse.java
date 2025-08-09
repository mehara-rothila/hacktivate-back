
// File Path: src/main/java/com/edulink/backend/dto/response/QueryMessageResponse.java
package com.edulink.backend.dto.response;

import com.edulink.backend.model.entity.Query.QueryMessage;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.format.DateTimeFormatter;

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
    private String timestamp;
    private boolean isRead;
    private String readAt;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static QueryMessageResponse fromQueryMessage(QueryMessage message) {
        return QueryMessageResponse.builder()
                .id(message.getId())
                .sender(message.getSender())
                .senderType(message.getSenderType())
                .senderName(message.getSenderName())
                .content(message.getContent())
                .timestamp(message.getTimestamp().format(FORMATTER))
                .isRead(message.isRead())
                .readAt(message.getReadAt() != null ? message.getReadAt().format(FORMATTER) : null)
                .build();
    }
}
