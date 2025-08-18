// src/main/java/com/edulink/backend/dto/websocket/AnnouncementNotificationDTO.java
package com.edulink.backend.dto.websocket;

import com.edulink.backend.model.entity.Announcement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementNotificationDTO {

    private String id;
    private String title;
    private String content;
    private Announcement.AnnouncementType type;
    private Announcement.Priority priority;
    private boolean pinned;
    
    // Author info
    private String authorName;
    private String authorAvatar;
    
    // Course info (if applicable)
    private String courseName;
    
    // Timestamps
    private LocalDateTime publishedAt;
    private LocalDateTime expiresAt;
    
    // Notification metadata
    private NotificationType notificationType;
    private String message; // User-friendly notification message
    
    public enum NotificationType {
        NEW_ANNOUNCEMENT,
        URGENT_ANNOUNCEMENT,
        ANNOUNCEMENT_UPDATE,
        ANNOUNCEMENT_REMINDER
    }

    /**
     * Create notification DTO from Announcement entity
     */
    public static AnnouncementNotificationDTO fromAnnouncement(Announcement announcement, String authorName, String courseName) {
        NotificationType notificationType = announcement.getPriority() == Announcement.Priority.CRITICAL ||
                                          announcement.getType() == Announcement.AnnouncementType.URGENT
                                          ? NotificationType.URGENT_ANNOUNCEMENT
                                          : NotificationType.NEW_ANNOUNCEMENT;
        
        String message = createNotificationMessage(announcement, authorName, courseName);
        
        return AnnouncementNotificationDTO.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .content(announcement.getContent())
                .type(announcement.getType())
                .priority(announcement.getPriority())
                .pinned(announcement.isPinned())
                .authorName(authorName)
                .courseName(courseName)
                .publishedAt(announcement.getPublishedAt())
                .expiresAt(announcement.getExpiresAt())
                .notificationType(notificationType)
                .message(message)
                .build();
    }

    private static String createNotificationMessage(Announcement announcement, String authorName, String courseName) {
        StringBuilder message = new StringBuilder();
        
        if (announcement.getPriority() == Announcement.Priority.CRITICAL) {
            message.append("🚨 CRITICAL: ");
        } else if (announcement.getType() == Announcement.AnnouncementType.URGENT) {
            message.append("⚠️ URGENT: ");
        } else {
            message.append("📢 ");
        }
        
        message.append(authorName);
        
        if (courseName != null) {
            message.append(" (").append(courseName).append(")");
        }
        
        message.append(" posted: ").append(announcement.getTitle());
        
        return message.toString();
    }
}