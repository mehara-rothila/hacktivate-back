// src/main/java/com/edulink/backend/dto/response/ConversationDTO.java
package com.edulink.backend.dto.response;

import com.edulink.backend.model.entity.Conversation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {
    
    private String id;
    private UserProfileResponse otherParticipant;
    private String subject;
    private String courseId;
    private Conversation.Status status;
    private Conversation.Priority priority;
    private String lastMessageContent;
    private LocalDateTime lastMessageAt;
    private String lastMessageSenderId;
    private long unreadCount;
}