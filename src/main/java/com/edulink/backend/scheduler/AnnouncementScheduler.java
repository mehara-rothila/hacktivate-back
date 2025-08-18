// src/main/java/com/edulink/backend/scheduler/AnnouncementScheduler.java
package com.edulink.backend.scheduler;

import com.edulink.backend.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnnouncementScheduler {

    private final AnnouncementService announcementService;

    /**
     * Auto-expire announcements that have passed their expiry date
     * Runs every hour
     */
    @Scheduled(fixedRate = 3600000) // 1 hour = 3,600,000 milliseconds
    public void autoExpireAnnouncements() {
        try {
            log.debug("🕐 Running auto-expire announcements task...");
            announcementService.autoExpireAnnouncements();
        } catch (Exception e) {
            log.error("❌ Error in auto-expire announcements task: {}", e.getMessage(), e);
        }
    }

    /**
     * Publish scheduled announcements that are ready to be published
     * Runs every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes = 300,000 milliseconds
    public void publishScheduledAnnouncements() {
        try {
            log.debug("📅 Running publish scheduled announcements task...");
            announcementService.publishScheduledAnnouncements();
        } catch (Exception e) {
            log.error("❌ Error in publish scheduled announcements task: {}", e.getMessage(), e);
        }
    }

    /**
     * Send reminder notifications for expiring announcements
     * Runs daily at 9 AM
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void sendExpiryReminders() {
        try {
            log.info("⏰ Running expiry reminder task...");
            // TODO: Implement expiry reminder logic
            // This would find announcements expiring in the next 24-48 hours
            // and send reminder notifications to users who haven't read them yet
        } catch (Exception e) {
            log.error("❌ Error in expiry reminder task: {}", e.getMessage(), e);
        }
    }

    /**
     * Clean up old announcement read/delivery tracking data
     * Runs weekly on Sunday at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * SUN")
    public void cleanupOldTrackingData() {
        try {
            log.info("🧹 Running cleanup task for old announcement tracking data...");
            // TODO: Implement cleanup logic
            // This would remove read/delivery tracking for announcements older than X months
            // to prevent the tracking sets from growing indefinitely
        } catch (Exception e) {
            log.error("❌ Error in cleanup task: {}", e.getMessage(), e);
        }
    }
}