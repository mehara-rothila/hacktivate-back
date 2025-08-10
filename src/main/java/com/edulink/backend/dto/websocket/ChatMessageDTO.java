// src/main/java/com/edulink/backend/dto/websocket/ChatMessageDTO.java
package com.edulink.backend.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    
    private String conversationId;
    private String senderId;
    private String content;
    private MessageType type;
    
    public enum MessageType {
        CHAT, JOIN, LEAVE
    }
}