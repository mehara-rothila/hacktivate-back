// src/main/java/com/edulink/backend/dto/websocket/ChatMessageDTO.java
package com.edulink.backend.dto.websocket;

import lombok.Data;

@Data
public class ChatMessageDTO {
    private String conversationId;
    private String senderId;
    private String content;
    // We can add attachments here later if needed
}