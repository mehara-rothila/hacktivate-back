// src/main/java/com/edulink/backend/controller/AnnouncementController.java
package com.edulink.backend.controller;

import com.edulink.backend.dto.request.AnnouncementRequest;
import com.edulink.backend.dto.response.AnnouncementResponse;
import com.edulink.backend.dto.response.ApiResponse;
import com.edulink.backend.model.entity.Announcement;
import com.edulink.backend.model.entity.Course;
import com.edulink.backend.model.entity.User;
import com.edulink.backend.repository.CourseRepository;
import com.edulink.backend.service.AnnouncementService;
import com.edulink.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
@Slf4j
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final UserService userService;
    private final CourseRepository courseRepository;

    /**
     * CREATE - Create a new announcement (Lecturers and Admins only)
     */
    @PostMapping
    @PreAuthorize("hasRole('LECTURER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> createAnnouncement(
            @Valid @RequestBody AnnouncementRequest request) {
        try {
            User currentUser = userService.getCurrentUser();
            
            // Build announcement entity
            Announcement announcement = Announcement.builder()
                    .title(request.getTitle())
                    .content(request.getContent())
                    .type(request.getType())
                    .priority(request.getPriority())
                    .targetAudience(request.getTargetAudience())
                    .courseId(request.getCourseId())
                    .yearLevel(request.getYearLevel())
                    .targetUserIds(request.getTargetUserIds())
                    .authorId(currentUser.getId())
                    .pinned(request.isPinned())
                    .expiresAt(request.getExpiresAt())
                    .scheduledFor(request.getScheduledFor())
                    .build();

            // Set initial status
            if (request.getScheduledFor() != null && request.getScheduledFor().isAfter(LocalDateTime.now())) {
                announcement.setStatus(Announcement.AnnouncementStatus.SCHEDULED);
            } else if (request.isPublishImmediately()) {
                announcement.setStatus(Announcement.AnnouncementStatus.PUBLISHED);
                announcement.setPublishedAt(LocalDateTime.now());
            } else {
                announcement.setStatus(Announcement.AnnouncementStatus.DRAFT);
            }

            Announcement savedAnnouncement = announcementService.createAnnouncement(announcement);
            AnnouncementResponse response = mapToAnnouncementResponse(savedAnnouncement, currentUser.getId());

            log.info("📢 Announcement created: {} by user: {}", savedAnnouncement.getTitle(), currentUser.getId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response, "Announcement created successfully"));

        } catch (Exception e) {
            log.error("❌ Error creating announcement: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create announcement: " + e.getMessage()));
        }
    }

    /**
     * READ - Get announcements for current user
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<AnnouncementResponse>>> getMyAnnouncements(
            @RequestParam(defaultValue = "all") String filter) {
        try {
            User currentUser = userService.getCurrentUser();
            List<Announcement> announcements;

            switch (filter.toLowerCase()) {
                case "unread":
                    announcements = announcementService.getAnnouncementsForUser(currentUser.getId())
                            .stream()
                            .filter(a -> !a.isReadBy(currentUser.getId()))
                            .collect(Collectors.toList());
                    break;
                case "pinned":
                    announcements = announcementService.getAnnouncementsForUser(currentUser.getId())
                            .stream()
                            .filter(Announcement::isPinned)
                            .collect(Collectors.toList());
                    break;
                default:
                    announcements = announcementService.getAnnouncementsForUser(currentUser.getId());
            }

            List<AnnouncementResponse> responses = announcements.stream()
                    .map(a -> mapToAnnouncementResponse(a, currentUser.getId()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(responses, "Announcements retrieved successfully"));

        } catch (Exception e) {
            log.error("❌ Error retrieving announcements: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve announcements: " + e.getMessage()));
        }
    }

    /**
     * READ - Get announcements created by current user (for lecturers)
     */
    @GetMapping("/my-created")
    @PreAuthorize("hasRole('LECTURER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AnnouncementResponse>>> getMyCreatedAnnouncements() {
        try {
            User currentUser = userService.getCurrentUser();
            List<Announcement> announcements = announcementService.getAnnouncementsByAuthor(currentUser.getId());

            List<AnnouncementResponse> responses = announcements.stream()
                    .map(a -> mapToAnnouncementResponse(a, currentUser.getId()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(responses, "Created announcements retrieved successfully"));

        } catch (Exception e) {
            log.error("❌ Error retrieving created announcements: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve created announcements: " + e.getMessage()));
        }
    }

    /**
     * UPDATE - Mark announcement as read
     */
    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> markAsRead(@PathVariable String id) {
        try {
            User currentUser = userService.getCurrentUser();
            announcementService.markAsRead(id, currentUser.getId());

            log.debug("👁️ Announcement {} marked as read by user: {}", id, currentUser.getId());

            return ResponseEntity.ok(ApiResponse.success("Announcement marked as read"));

        } catch (Exception e) {
            log.error("❌ Error marking announcement as read: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to mark announcement as read: " + e.getMessage()));
        }
    }

    /**
     * UPDATE - Publish a draft announcement
     */
    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('LECTURER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> publishAnnouncement(@PathVariable String id) {
        try {
            User currentUser = userService.getCurrentUser();
            Announcement publishedAnnouncement = announcementService.publishAnnouncement(id);
            AnnouncementResponse response = mapToAnnouncementResponse(publishedAnnouncement, currentUser.getId());

            log.info("📣 Announcement published: {} by user: {}", publishedAnnouncement.getTitle(), currentUser.getId());

            return ResponseEntity.ok(ApiResponse.success(response, "Announcement published successfully"));

        } catch (Exception e) {
            log.error("❌ Error publishing announcement: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to publish announcement: " + e.getMessage()));
        }
    }

    /**
     * DELETE - Delete announcement
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('LECTURER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteAnnouncement(@PathVariable String id) {
        try {
            User currentUser = userService.getCurrentUser();
            announcementService.deleteAnnouncement(id);

            log.info("🗑️ Announcement {} deleted by user: {}", id, currentUser.getId());

            return ResponseEntity.ok(ApiResponse.success("Announcement deleted successfully"));

        } catch (Exception e) {
            log.error("❌ Error deleting announcement: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete announcement: " + e.getMessage()));
        }
    }

    // =================== HELPER METHODS ===================

    /**
     * Map Announcement entity to AnnouncementResponse DTO
     */
    private AnnouncementResponse mapToAnnouncementResponse(Announcement announcement, String currentUserId) {
        // Get author info
        String authorName = "Unknown";
        String authorAvatar = null;
        try {
            Optional<User> author = userService.findById(announcement.getAuthorId());
            if (author.isPresent()) {
                authorName = author.get().getFullName();
                authorAvatar = author.get().getAvatarUrl();
            }
        } catch (Exception e) {
            log.warn("Could not fetch author info for announcement {}: {}", announcement.getId(), e.getMessage());
        }

        // Get course info if applicable
        String courseName = null;
        if (announcement.getCourseId() != null) {
            try {
                Optional<Course> course = courseRepository.findById(announcement.getCourseId());
                if (course.isPresent()) {
                    courseName = course.get().getName();
                }
            } catch (Exception e) {
                log.warn("Could not fetch course info for announcement {}: {}", announcement.getId(), e.getMessage());
            }
        }

        // Map attachments
        List<AnnouncementResponse.AttachmentInfo> attachments = announcement.getAttachments() != null
                ? announcement.getAttachments().stream()
                    .map(att -> AnnouncementResponse.AttachmentInfo.builder()
                            .filename(att.getFilename())
                            .originalFilename(att.getOriginalFilename())
                            .contentType(att.getContentType())
                            .size(att.getSize())
                            .downloadUrl("/api/files/" + att.getFilename())
                            .build())
                    .collect(Collectors.toList())
                : null;

        return AnnouncementResponse.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .content(announcement.getContent())
                .type(announcement.getType())
                .priority(announcement.getPriority())
                .targetAudience(announcement.getTargetAudience())
                .courseId(announcement.getCourseId())
                .courseName(courseName)
                .yearLevel(announcement.getYearLevel())
                .authorId(announcement.getAuthorId())
                .authorName(authorName)
                .authorAvatar(authorAvatar)
                .status(announcement.getStatus())
                .pinned(announcement.isPinned())
                .isRead(announcement.isReadBy(currentUserId))
                .attachments(attachments)
                .readCount(announcement.getReadCount())
                .deliveryCount(announcement.getDeliveryCount())
                .createdAt(announcement.getCreatedAt())
                .updatedAt(announcement.getUpdatedAt())
                .publishedAt(announcement.getPublishedAt())
                .expiresAt(announcement.getExpiresAt())
                .scheduledFor(announcement.getScheduledFor())
                .build();
    }
}