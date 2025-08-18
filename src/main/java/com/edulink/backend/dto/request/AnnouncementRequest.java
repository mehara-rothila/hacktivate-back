// src/main/java/com/edulink/backend/dto/request/AnnouncementRequest.java
package com.edulink.backend.dto.request;

import com.edulink.backend.model.entity.Announcement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    @NotNull(message = "Type is required")
    private Announcement.AnnouncementType type;

    @NotNull(message = "Priority is required")
    private Announcement.Priority priority;

    @NotNull(message = "Target audience is required")
    private Announcement.TargetAudience targetAudience;

    // Optional targeting fields
    private String courseId;
    private String yearLevel;
    private Set<String> targetUserIds;

    // Optional scheduling and expiry
    private LocalDateTime expiresAt;
    private LocalDateTime scheduledFor;

    @Builder.Default
    private boolean pinned = false;

    @Builder.Default
    private boolean publishImmediately = true;
}
