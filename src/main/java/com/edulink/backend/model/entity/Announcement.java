// src/main/java/com/edulink/backend/model/entity/Announcement.java
package com.edulink.backend.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Document(collection = "announcements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Announcement {

    @Id
    private String id;

    @Indexed
    private String title;

    private String content;

    @Indexed
    private AnnouncementType type;

    @Indexed
    private Priority priority;

    @Indexed
    private TargetAudience targetAudience;

    // Targeting fields
    private String courseId;        // For course-specific announcements
    private String yearLevel;       // For year-specific announcements
    private Set<String> targetUserIds; // For specific user targeting

    @Indexed
    private String authorId;        // User ID of the announcement creator

    @Indexed
    private AnnouncementStatus status;

    private boolean pinned;

    @Builder.Default
    private List<Attachment> attachments = new ArrayList<>();

    // Delivery tracking
    @Builder.Default
    private Set<String> deliveredToUserIds = new HashSet<>();
    
    @Builder.Default
    private Set<String> readByUserIds = new HashSet<>();

    // Scheduling
    private LocalDateTime publishedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime scheduledFor; // For future scheduling

    // Metadata
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // --- Enums ---
    public enum AnnouncementType {
        GENERAL,    // General university announcements
        URGENT,     // Urgent/emergency announcements
        COURSE,     // Course-specific announcements
        SCHEDULE,   // Schedule changes, office hours
        SYSTEM,     // System maintenance, platform updates
        EVENT,      // Events and activities
        ACADEMIC,   // Academic deadlines, exam schedules
        ADMINISTRATIVE // Administrative notices
    }

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public enum TargetAudience {
        ALL,            // All users
        STUDENTS,       // All students
        LECTURERS,      // All lecturers
        COURSE_SPECIFIC, // Students enrolled in specific course
        YEAR_SPECIFIC,   // Students in specific year level
        DEPARTMENT,     // Users in specific department
        CUSTOM          // Custom user selection
    }

    public enum AnnouncementStatus {
        DRAFT,
        SCHEDULED,
        PUBLISHED,
        EXPIRED,
        ARCHIVED
    }

    // --- Embedded Classes ---
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Attachment {
        private String filename;
        private String originalFilename;
        private String contentType;
        private Long size;
        private String uploadedBy;
        private LocalDateTime uploadedAt;
    }

    // --- Helper Methods ---

    /**
     * Check if announcement is currently active
     */
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return status == AnnouncementStatus.PUBLISHED &&
               (publishedAt == null || publishedAt.isBefore(now) || publishedAt.isEqual(now)) &&
               (expiresAt == null || expiresAt.isAfter(now));
    }

    /**
     * Check if announcement has expired
     */
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    /**
     * Check if announcement is scheduled for future
     */
    public boolean isScheduled() {
        return status == AnnouncementStatus.SCHEDULED ||
               (scheduledFor != null && scheduledFor.isAfter(LocalDateTime.now()));
    }

    /**
     * Get read count
     */
    public int getReadCount() {
        return readByUserIds != null ? readByUserIds.size() : 0;
    }

    /**
     * Get delivery count
     */
    public int getDeliveryCount() {
        return deliveredToUserIds != null ? deliveredToUserIds.size() : 0;
    }

    /**
     * Mark as read by user
     */
    public void markAsReadBy(String userId) {
        if (readByUserIds == null) {
            readByUserIds = new HashSet<>();
        }
        readByUserIds.add(userId);
    }

    /**
     * Mark as delivered to user
     */
    public void markAsDeliveredTo(String userId) {
        if (deliveredToUserIds == null) {
            deliveredToUserIds = new HashSet<>();
        }
        deliveredToUserIds.add(userId);
    }

    /**
     * Check if user has read this announcement
     */
    public boolean isReadBy(String userId) {
        return readByUserIds != null && readByUserIds.contains(userId);
    }

    /**
     * Check if announcement was delivered to user
     */
    public boolean isDeliveredTo(String userId) {
        return deliveredToUserIds != null && deliveredToUserIds.contains(userId);
    }

    /**
     * Auto-expire if past expiry date
     */
    public void autoExpireIfNeeded() {
        if (isExpired() && status == AnnouncementStatus.PUBLISHED) {
            this.status = AnnouncementStatus.EXPIRED;
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Get priority color for UI
     */
    public String getPriorityColor() {
        switch (priority) {
            case CRITICAL: return "#dc2626"; // red-600
            case HIGH: return "#ea580c";     // orange-600
            case MEDIUM: return "#ca8a04";   // yellow-600
            case LOW: return "#16a34a";      // green-600
            default: return "#6b7280";       // gray-500
        }
    }

    /**
     * Get type color for UI
     */
    public String getTypeColor() {
        switch (type) {
            case URGENT: return "#dc2626";      // red-600
            case COURSE: return "#2563eb";      // blue-600
            case SCHEDULE: return "#7c3aed";    // violet-600
            case SYSTEM: return "#4b5563";      // gray-600
            case EVENT: return "#059669";       // emerald-600
            case ACADEMIC: return "#0891b2";    // cyan-600
            case ADMINISTRATIVE: return "#be185d"; // pink-600
            default: return "#16a34a";          // green-600 (GENERAL)
        }
    }
}