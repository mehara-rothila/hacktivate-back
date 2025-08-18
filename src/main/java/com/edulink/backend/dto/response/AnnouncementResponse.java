// src/main/java/com/edulink/backend/dto/response/AnnouncementResponse.java
package com.edulink.backend.dto.response;

import com.edulink.backend.model.entity.Announcement;
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
public class AnnouncementResponse {

    private String id;
    private String title;
    private String content;
    private Announcement.AnnouncementType type;
    private Announcement.Priority priority;
    private Announcement.TargetAudience targetAudience;
    
    // Targeting info
    private String courseId;
    private String courseName; // Resolved course name
    private String yearLevel;
    
    // Author info
    private String authorId;
    private String authorName;
    private String authorAvatar;
    
    // Status and metadata
    private Announcement.AnnouncementStatus status;
    private boolean pinned;
    private boolean isRead; // For the current user
    
    // Attachments
    private List<AttachmentInfo> attachments;
    
    // Statistics
    private int readCount;
    private int deliveryCount;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime scheduledFor;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttachmentInfo {
        private String filename;
        private String originalFilename;
        private String contentType;
        private Long size;
        private String downloadUrl;
    }
}