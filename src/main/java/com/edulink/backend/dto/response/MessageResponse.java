// ============================================================================
// dto/response/MessageResponse.java
// ============================================================================
package com.edulink.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageResponse {
    private String id;
    private String lecturer;
    private String lecturerAvatar;
    private String subject;
    private String lastMessage;
    private String lastMessageTime;
    private Integer unreadCount;
    private Boolean isRead;
    private String course;
    private String messageType;
    private String priority;
}
