// src/main/java/com/edulink/backend/dto/response/LostFoundItemResponse.java
package com.edulink.backend.dto.response;

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
public class LostFoundItemResponse {
    
    private String id;
    private String title;
    private String description;
    private String type; // "LOST" or "FOUND"
    private String status; // "OPEN", "RESOLVED", "ARCHIVED"
    
    // User who posted
    private String userId;
    private String userName;
    private String userAvatar;
    
    // Location and contact
    private String location;
    private LocalDateTime lostFoundDateTime; // When the item was actually lost or found
    private String contactInfo;
    
    // Image attachment
    private ImageAttachmentResponse image;
    
    // Resolution info
    private String resolvedBy;
    private String resolvedByName;
    private LocalDateTime resolvedAt;
    
    // Metadata
    private int commentCount;
    private int claimCount;
    private boolean hasUserCommented; // For current user
    private boolean hasUserClaimed; // For current user
    private boolean canEdit; // For current user
    private boolean canDelete; // For current user
    private boolean canResolve; // For current user
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Comments (optional - loaded separately or with details)
    private List<LostFoundCommentResponse> comments;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageAttachmentResponse {
        private String filename;
        private String originalFilename;
        private String contentType;
        private Long size;
        private String downloadUrl;
    }
}
