// src/main/java/com/edulink/backend/repository/AnnouncementRepository.java
package com.edulink.backend.repository;

import com.edulink.backend.model.entity.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnnouncementRepository extends MongoRepository<Announcement, String> {

    // Find by author
    List<Announcement> findByAuthorIdOrderByCreatedAtDesc(String authorId);
    Page<Announcement> findByAuthorIdOrderByCreatedAtDesc(String authorId, Pageable pageable);

    // Find by status
    List<Announcement> findByStatusOrderByCreatedAtDesc(Announcement.AnnouncementStatus status);
    
    // Find published announcements
    List<Announcement> findByStatusAndPinnedOrderByCreatedAtDesc(Announcement.AnnouncementStatus status, boolean pinned);

    // Find active announcements (published and not expired)
    @Query("{ 'status': 'PUBLISHED', $or: [ { 'expiresAt': null }, { 'expiresAt': { $gt: ?0 } } ] }")
    List<Announcement> findActiveAnnouncements(LocalDateTime now);

    // Find by target audience
    List<Announcement> findByTargetAudienceAndStatusOrderByCreatedAtDesc(
        Announcement.TargetAudience targetAudience, 
        Announcement.AnnouncementStatus status
    );

    // Find course-specific announcements
    List<Announcement> findByCourseIdAndStatusOrderByCreatedAtDesc(String courseId, Announcement.AnnouncementStatus status);

    // Find by year level
    List<Announcement> findByYearLevelAndStatusOrderByCreatedAtDesc(String yearLevel, Announcement.AnnouncementStatus status);

    // Find by type
    List<Announcement> findByTypeAndStatusOrderByCreatedAtDesc(
        Announcement.AnnouncementType type, 
        Announcement.AnnouncementStatus status
    );

    // Find by priority
    List<Announcement> findByPriorityAndStatusOrderByCreatedAtDesc(
        Announcement.Priority priority, 
        Announcement.AnnouncementStatus status
    );

    // Find expired announcements that need status update
    @Query("{ 'status': 'PUBLISHED', 'expiresAt': { $lt: ?0 } }")
    List<Announcement> findExpiredAnnouncements(LocalDateTime now);

    // Find scheduled announcements ready to publish
    @Query("{ 'status': 'SCHEDULED', 'scheduledFor': { $lte: ?0 } }")
    List<Announcement> findScheduledAnnouncementsReadyToPublish(LocalDateTime now);

    // Search announcements
    @Query("{ $and: [ { 'status': ?2 }, { $or: [ { 'title': { $regex: ?0, $options: 'i' } }, { 'content': { $regex: ?1, $options: 'i' } } ] } ] }")
    List<Announcement> searchAnnouncements(String titleRegex, String contentRegex, Announcement.AnnouncementStatus status);
}