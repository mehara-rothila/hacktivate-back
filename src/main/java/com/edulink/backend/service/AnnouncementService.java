// src/main/java/com/edulink/backend/service/AnnouncementService.java
package com.edulink.backend.service;

import com.edulink.backend.model.entity.Announcement;
import com.edulink.backend.model.entity.Course;
import com.edulink.backend.model.entity.User;
import com.edulink.backend.repository.AnnouncementRepository;
import com.edulink.backend.repository.CourseRepository;
import com.edulink.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;

    /**
     * Create a new announcement
     */
    public Announcement createAnnouncement(Announcement announcement) {
        announcement.setCreatedAt(LocalDateTime.now());
        announcement.setUpdatedAt(LocalDateTime.now());
        
        // Auto-publish if not scheduled
        if (announcement.getScheduledFor() == null && announcement.getStatus() == null) {
            announcement.setStatus(Announcement.AnnouncementStatus.PUBLISHED);
            announcement.setPublishedAt(LocalDateTime.now());
        }
        
        Announcement savedAnnouncement = announcementRepository.save(announcement);
        
        // Broadcast real-time if published
        if (savedAnnouncement.getStatus() == Announcement.AnnouncementStatus.PUBLISHED) {
            broadcastAnnouncementAsync(savedAnnouncement);
        }
        
        return savedAnnouncement;
    }

    /**
     * Update an existing announcement
     */
    public Announcement updateAnnouncement(String id, Announcement updateData) {
        Announcement existing = announcementRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Announcement not found"));
        
        // Update fields
        if (updateData.getTitle() != null) existing.setTitle(updateData.getTitle());
        if (updateData.getContent() != null) existing.setContent(updateData.getContent());
        if (updateData.getType() != null) existing.setType(updateData.getType());
        if (updateData.getPriority() != null) existing.setPriority(updateData.getPriority());
        if (updateData.getTargetAudience() != null) existing.setTargetAudience(updateData.getTargetAudience());
        if (updateData.getCourseId() != null) existing.setCourseId(updateData.getCourseId());
        if (updateData.getYearLevel() != null) existing.setYearLevel(updateData.getYearLevel());
        if (updateData.getExpiresAt() != null) existing.setExpiresAt(updateData.getExpiresAt());
        if (updateData.getScheduledFor() != null) existing.setScheduledFor(updateData.getScheduledFor());
        
        existing.setPinned(updateData.isPinned());
        existing.setUpdatedAt(LocalDateTime.now());
        
        return announcementRepository.save(existing);
    }

    /**
     * Publish a draft announcement
     */
    public Announcement publishAnnouncement(String id) {
        Announcement announcement = announcementRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Announcement not found"));
        
        announcement.setStatus(Announcement.AnnouncementStatus.PUBLISHED);
        announcement.setPublishedAt(LocalDateTime.now());
        announcement.setUpdatedAt(LocalDateTime.now());
        
        Announcement published = announcementRepository.save(announcement);
        broadcastAnnouncementAsync(published);
        
        return published;
    }

    /**
     * Get announcements for a specific user based on their role and enrollments
     */
    public List<Announcement> getAnnouncementsForUser(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Announcement> announcements = new ArrayList<>();
        
        // Get all active announcements
        List<Announcement> activeAnnouncements = announcementRepository.findActiveAnnouncements(LocalDateTime.now());
        
        for (Announcement announcement : activeAnnouncements) {
            if (isAnnouncementTargetedToUser(announcement, user)) {
                announcements.add(announcement);
            }
        }
        
        // Sort: pinned first, then by creation date
        announcements.sort((a, b) -> {
            if (a.isPinned() && !b.isPinned()) return -1;
            if (!a.isPinned() && b.isPinned()) return 1;
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        });
        
        return announcements;
    }

    /**
     * Get announcements created by a lecturer
     */
    public List<Announcement> getAnnouncementsByAuthor(String authorId) {
        return announcementRepository.findByAuthorIdOrderByCreatedAtDesc(authorId);
    }

    /**
     * Get announcements by author with pagination
     */
    public Page<Announcement> getAnnouncementsByAuthor(String authorId, Pageable pageable) {
        return announcementRepository.findByAuthorIdOrderByCreatedAtDesc(authorId, pageable);
    }

    /**
     * Mark announcement as read by user
     */
    public void markAsRead(String announcementId, String userId) {
        Announcement announcement = announcementRepository.findById(announcementId)
            .orElseThrow(() -> new RuntimeException("Announcement not found"));
        
        announcement.markAsReadBy(userId);
        announcementRepository.save(announcement);
    }

    /**
     * Delete announcement
     */
    public void deleteAnnouncement(String id) {
        announcementRepository.deleteById(id);
    }

    /**
     * Auto-expire announcements (called by scheduler)
     */
    public void autoExpireAnnouncements() {
        List<Announcement> expiredAnnouncements = announcementRepository.findExpiredAnnouncements(LocalDateTime.now());
        
        for (Announcement announcement : expiredAnnouncements) {
            announcement.setStatus(Announcement.AnnouncementStatus.EXPIRED);
            announcement.setUpdatedAt(LocalDateTime.now());
        }
        
        if (!expiredAnnouncements.isEmpty()) {
            announcementRepository.saveAll(expiredAnnouncements);
            log.info("Auto-expired {} announcements", expiredAnnouncements.size());
        }
    }

    /**
     * Publish scheduled announcements (called by scheduler)
     */
    public void publishScheduledAnnouncements() {
        List<Announcement> readyToPublish = announcementRepository.findScheduledAnnouncementsReadyToPublish(LocalDateTime.now());
        
        for (Announcement announcement : readyToPublish) {
            announcement.setStatus(Announcement.AnnouncementStatus.PUBLISHED);
            announcement.setPublishedAt(LocalDateTime.now());
            announcement.setUpdatedAt(LocalDateTime.now());
            
            // Broadcast immediately when published
            broadcastAnnouncementAsync(announcement);
        }
        
        if (!readyToPublish.isEmpty()) {
            announcementRepository.saveAll(readyToPublish);
            log.info("Published {} scheduled announcements", readyToPublish.size());
        }
    }

    /**
     * Check if announcement is targeted to specific user
     */
    private boolean isAnnouncementTargetedToUser(Announcement announcement, User user) {
        switch (announcement.getTargetAudience()) {
            case ALL:
                return true;
                
            case STUDENTS:
                return user.getRole() == User.UserRole.STUDENT;
                
            case LECTURERS:
                return user.getRole() == User.UserRole.LECTURER;
                
            case COURSE_SPECIFIC:
                if (announcement.getCourseId() == null) return false;
                return isUserEnrolledInCourse(user, announcement.getCourseId());
                
            case YEAR_SPECIFIC:
                if (announcement.getYearLevel() == null) return false;
                return announcement.getYearLevel().equals(user.getProfile().getYear());
                
            case CUSTOM:
                return announcement.getTargetUserIds() != null && 
                       announcement.getTargetUserIds().contains(user.getId());
                
            default:
                return false;
        }
    }

    /**
     * Check if user is enrolled in course
     */
    private boolean isUserEnrolledInCourse(User user, String courseId) {
        if (user.getRole() == User.UserRole.LECTURER) {
            // Lecturer can see announcements for courses they teach
            Optional<Course> course = courseRepository.findById(courseId);
            return course.isPresent() && course.get().getLecturerId().equals(user.getId());
        }
        
        if (user.getRole() == User.UserRole.STUDENT) {
            // Student can see announcements for courses they're enrolled in
            Optional<Course> course = courseRepository.findById(courseId);
            return course.isPresent() && 
                   course.get().getEnrollment() != null &&
                   course.get().getEnrollment().getStudentIds() != null &&
                   course.get().getEnrollment().getStudentIds().contains(user.getId());
        }
        
        return false;
    }

    /**
     * Broadcast announcement to targeted users via WebSocket
     */
    @Async
    public void broadcastAnnouncementAsync(Announcement announcement) {
        try {
            log.info("📢 Broadcasting announcement: {} to target audience: {}", 
                announcement.getTitle(), announcement.getTargetAudience());
            
            Set<String> targetUserIds = getTargetUserIds(announcement);
            
            // Broadcast to each target user
            for (String userId : targetUserIds) {
                messagingTemplate.convertAndSendToUser(
                    userId, 
                    "/queue/announcements", 
                    announcement
                );
                
                // Mark as delivered
                announcement.markAsDeliveredTo(userId);
            }
            
            // Also broadcast to general announcement channel
            messagingTemplate.convertAndSend("/topic/announcements", announcement);
            
            // Save delivery tracking
            announcementRepository.save(announcement);
            
            log.info("✅ Announcement broadcast completed. Delivered to {} users", targetUserIds.size());
            
        } catch (Exception e) {
            log.error("❌ Error broadcasting announcement: {}", e.getMessage(), e);
        }
    }

    /**
     * Get target user IDs for an announcement
     */
    private Set<String> getTargetUserIds(Announcement announcement) {
        Set<String> targetUserIds = new HashSet<>();
        
        switch (announcement.getTargetAudience()) {
            case ALL:
                List<User> allUsers = userRepository.findByIsActiveTrue(); // Only active users
                targetUserIds.addAll(allUsers.stream().map(User::getId).collect(Collectors.toSet()));
                break;
                
            case STUDENTS:
                List<User> students = userRepository.findActiveStudents(); // Use your convenience method
                targetUserIds.addAll(students.stream().map(User::getId).collect(Collectors.toSet()));
                break;
                
            case LECTURERS:
                List<User> lecturers = userRepository.findActiveLecturers(); // Use your convenience method
                targetUserIds.addAll(lecturers.stream().map(User::getId).collect(Collectors.toSet()));
                break;
                
            case COURSE_SPECIFIC:
                if (announcement.getCourseId() != null) {
                    Optional<Course> course = courseRepository.findById(announcement.getCourseId());
                    if (course.isPresent() && course.get().getEnrollment() != null) {
                        Set<String> enrolledStudents = course.get().getEnrollment().getStudentIds();
                        if (enrolledStudents != null) {
                            // Filter to only include active students
                            List<User> activeEnrolledStudents = userRepository.findAllById(enrolledStudents)
                                .stream()
                                .filter(User::isActive)
                                .collect(Collectors.toList());
                            targetUserIds.addAll(activeEnrolledStudents.stream().map(User::getId).collect(Collectors.toSet()));
                        }
                        // Also include the lecturer if active
                        if (course.get().getLecturerId() != null) {
                            userRepository.findById(course.get().getLecturerId())
                                .filter(User::isActive)
                                .ifPresent(lecturer -> targetUserIds.add(lecturer.getId()));
                        }
                    }
                }
                break;
                
            case YEAR_SPECIFIC:
                if (announcement.getYearLevel() != null) {
                    // Use your existing method - much more efficient!
                    List<User> studentsInYear = userRepository.findStudentsByYear(announcement.getYearLevel());
                    targetUserIds.addAll(
                        studentsInYear.stream()
                            .filter(User::isActive) // Only active students
                            .map(User::getId)
                            .collect(Collectors.toSet())
                    );
                }
                break;
                
            case DEPARTMENT:
                if (announcement.getCourseId() != null) { // Reuse courseId field for department
                    List<User> usersInDepartment = userRepository.findByDepartmentAndIsActiveTrue(announcement.getCourseId());
                    targetUserIds.addAll(usersInDepartment.stream().map(User::getId).collect(Collectors.toSet()));
                }
                break;
                
            case CUSTOM:
                if (announcement.getTargetUserIds() != null) {
                    // Filter to only include active users
                    List<User> customUsers = userRepository.findAllById(announcement.getTargetUserIds())
                        .stream()
                        .filter(User::isActive)
                        .collect(Collectors.toList());
                    targetUserIds.addAll(customUsers.stream().map(User::getId).collect(Collectors.toSet()));
                }
                break;
        }
        
        return targetUserIds;
    }

    /**
     * Search announcements
     */
    public List<Announcement> searchAnnouncements(String query, Announcement.AnnouncementStatus status) {
        if (query == null || query.trim().isEmpty()) {
            return announcementRepository.findByStatusOrderByCreatedAtDesc(status);
        }
        
        String searchRegex = ".*" + query.trim() + ".*";
        return announcementRepository.searchAnnouncements(searchRegex, searchRegex, status);
    }
}