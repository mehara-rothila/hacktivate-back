

// src/main/java/com/edulink/backend/dto/response/LostFoundCommentResponse.java
package com.edulink.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LostFoundCommentResponse {
    
    private String id;
    private String lostFoundItemId;
    
    // User who commented
    private String userId;
    private String userName;
    private String userAvatar;
    
    // Comment content
    private String content;
    private String type; // "COMMENT", "CLAIM", "INFO"
    private String contactInfo;
    
    // Permissions for current user
    private boolean canEdit;
    private boolean canDelete;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}