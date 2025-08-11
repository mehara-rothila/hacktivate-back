// src/main/java/com/edulink/backend/scheduler/AppointmentScheduler.java
package com.edulink.backend.scheduler;

import com.edulink.backend.model.entity.Appointment;
import com.edulink.backend.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentScheduler {

    private final AppointmentRepository appointmentRepository;

    /**
     * Automatically mark past confirmed appointments as completed
     * Runs every hour
     */
    @Scheduled(fixedRate = 3600000) // 1 hour = 3,600,000 milliseconds
    @Transactional
    public void autoCompleteExpiredAppointments() {
        log.info("Starting auto-completion of expired appointments");
        
        try {
            // Find appointments that should be completed (2 hours past scheduled time)
            LocalDateTime cutoffTime = LocalDateTime.now().minus(2, ChronoUnit.HOURS);
            List<Appointment> expiredAppointments = appointmentRepository.findAppointmentsPendingCompletion(cutoffTime);
            
            int completedCount = 0;
            for (Appointment appointment : expiredAppointments) {
                // Only auto-complete if it's still in CONFIRMED status
                if (appointment.getStatus() == Appointment.AppointmentStatus.CONFIRMED) {
                    appointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
                    appointment.setLastModifiedAt(LocalDateTime.now());
                    appointment.setLastModifiedBy("SYSTEM_AUTO_COMPLETE");
                    appointment.setNotes(appointment.getNotes() + 
                        (appointment.getNotes() != null ? " | " : "") + 
                        "Auto-completed by system at " + LocalDateTime.now());
                    
                    appointmentRepository.save(appointment);
                    completedCount++;
                }
            }
            
            if (completedCount > 0) {
                log.info("Auto-completed {} expired appointments", completedCount);
            } else {
                log.debug("No expired appointments found to auto-complete");
            }
            
        } catch (Exception e) {
            log.error("Error during auto-completion of expired appointments", e);
        }
    }

    /**
     * Clean up very old appointments (older than 2 years) to prevent database bloat
     * Runs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2:00 AM
    @Transactional
    public void cleanupOldAppointments() {
        log.info("Starting cleanup of old appointments");
        
        try {
            // Delete appointments older than 2 years (except recurring parent appointments)
            LocalDateTime cutoffDate = LocalDateTime.now().minus(2, ChronoUnit.YEARS);
            
            // First, find old appointments that are not recurring parents
            List<Appointment> oldAppointments = appointmentRepository.findAll().stream()
                .filter(apt -> apt.getScheduledAt().isBefore(cutoffDate))
                .filter(apt -> !apt.isRecurring() || apt.getParentAppointmentId() != null) // Keep recurring parents
                .filter(apt -> apt.getStatus() == Appointment.AppointmentStatus.COMPLETED || 
                              apt.getStatus() == Appointment.AppointmentStatus.CANCELLED ||
                              apt.getStatus() == Appointment.AppointmentStatus.NO_SHOW)
                .toList();
            
            int deletedCount = oldAppointments.size();
            if (deletedCount > 0) {
                appointmentRepository.deleteAll(oldAppointments);
                log.info("Cleaned up {} old appointments (older than 2 years)", deletedCount);
            } else {
                log.debug("No old appointments found for cleanup");
            }
            
        } catch (Exception e) {
            log.error("Error during cleanup of old appointments", e);
        }
    }

    /**
     * Send appointment reminders for appointments in the next 24 hours
     * Runs every 6 hours
     */
    @Scheduled(fixedRate = 21600000) // 6 hours = 21,600,000 milliseconds
    public void sendAppointmentReminders() {
        log.info("Starting appointment reminder notifications");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime reminderWindowStart = now.plus(23, ChronoUnit.HOURS);
            LocalDateTime reminderWindowEnd = now.plus(25, ChronoUnit.HOURS);
            
            // Find confirmed appointments in the next 24 hours
            List<Appointment> upcomingAppointments = appointmentRepository.findAll().stream()
                .filter(apt -> apt.getStatus() == Appointment.AppointmentStatus.CONFIRMED)
                .filter(apt -> apt.getScheduledAt().isAfter(reminderWindowStart) && 
                              apt.getScheduledAt().isBefore(reminderWindowEnd))
                .toList();
            
            int remindersSent = 0;
            for (Appointment appointment : upcomingAppointments) {
                try {
                    // Here you would integrate with your notification system
                    // For now, we'll just log the reminder
                    log.info("Reminder: Appointment '{}' for student {} with lecturer {} at {}", 
                        appointment.getSubject(),
                        appointment.getStudentId(),
                        appointment.getLecturerId(),
                        appointment.getScheduledAt());
                    
                    // In a real implementation, you might:
                    // 1. Send email notifications
                    // 2. Send push notifications
                    // 3. Create in-app notifications
                    // 4. Send SMS reminders
                    
                    remindersSent++;
                    
                } catch (Exception e) {
                    log.error("Failed to send reminder for appointment {}", appointment.getId(), e);
                }
            }
            
            if (remindersSent > 0) {
                log.info("Sent {} appointment reminders", remindersSent);
            } else {
                log.debug("No appointment reminders to send");
            }
            
        } catch (Exception e) {
            log.error("Error during appointment reminder notifications", e);
        }
    }

    /**
     * Process recurring appointments - create next instances
     * Runs daily at 1 AM
     */
    @Scheduled(cron = "0 0 1 * * ?") // Daily at 1:00 AM
    @Transactional
    public void processRecurringAppointments() {
        log.info("Starting processing of recurring appointments");
        
        try {
            // Find recurring parent appointments
            List<Appointment> recurringParents = appointmentRepository.findByIsRecurringTrueAndParentAppointmentIdIsNull();
            
            int newInstancesCreated = 0;
            for (Appointment parent : recurringParents) {
                try {
                    // Check if we need to create new instances
                    if (shouldCreateNextRecurringInstance(parent)) {
                        createNextRecurringInstance(parent);
                        newInstancesCreated++;
                    }
                } catch (Exception e) {
                    log.error("Failed to process recurring appointment {}", parent.getId(), e);
                }
            }
            
            if (newInstancesCreated > 0) {
                log.info("Created {} new recurring appointment instances", newInstancesCreated);
            } else {
                log.debug("No new recurring appointment instances needed");
            }
            
        } catch (Exception e) {
            log.error("Error during processing of recurring appointments", e);
        }
    }

    /**
     * Update appointment statistics and metrics
     * Runs every 30 minutes
     */
    @Scheduled(fixedRate = 1800000) // 30 minutes = 1,800,000 milliseconds
    public void updateAppointmentMetrics() {
        log.debug("Updating appointment metrics");
        
        try {
            // Here you would update various metrics like:
            // - Appointment completion rates
            // - Average appointment duration
            // - Popular appointment times
            // - Lecturer utilization rates
            // - Student engagement metrics
            
            long totalAppointments = appointmentRepository.count();
            long pendingAppointments = appointmentRepository.findAll().stream()
                .mapToLong(apt -> apt.getStatus() == Appointment.AppointmentStatus.PENDING ? 1 : 0)
                .sum();
            long confirmedAppointments = appointmentRepository.findAll().stream()
                .mapToLong(apt -> apt.getStatus() == Appointment.AppointmentStatus.CONFIRMED ? 1 : 0)
                .sum();
            long completedAppointments = appointmentRepository.findAll().stream()
                .mapToLong(apt -> apt.getStatus() == Appointment.AppointmentStatus.COMPLETED ? 1 : 0)
                .sum();
            
            log.debug("Appointment metrics - Total: {}, Pending: {}, Confirmed: {}, Completed: {}", 
                totalAppointments, pendingAppointments, confirmedAppointments, completedAppointments);
            
        } catch (Exception e) {
            log.error("Error during appointment metrics update", e);
        }
    }

    /**
     * Cancel abandoned pending appointments (pending for more than 48 hours)
     * Runs daily at 3 AM
     */
    @Scheduled(cron = "0 0 3 * * ?") // Daily at 3:00 AM
    @Transactional
    public void cancelAbandonedAppointments() {
        log.info("Starting cancellation of abandoned pending appointments");
        
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minus(48, ChronoUnit.HOURS);
            
            List<Appointment> abandonedAppointments = appointmentRepository.findAll().stream()
                .filter(apt -> apt.getStatus() == Appointment.AppointmentStatus.PENDING)
                .filter(apt -> apt.getBookedAt().isBefore(cutoffTime))
                .toList();
            
            int cancelledCount = 0;
            for (Appointment appointment : abandonedAppointments) {
                appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
                appointment.setLastModifiedAt(LocalDateTime.now());
                appointment.setLastModifiedBy("SYSTEM_AUTO_CANCEL");
                appointment.setNotes(appointment.getNotes() + 
                    (appointment.getNotes() != null ? " | " : "") + 
                    "Auto-cancelled due to no lecturer response within 48 hours");
                
                appointmentRepository.save(appointment);
                cancelledCount++;
            }
            
            if (cancelledCount > 0) {
                log.info("Auto-cancelled {} abandoned pending appointments", cancelledCount);
            } else {
                log.debug("No abandoned pending appointments found");
            }
            
        } catch (Exception e) {
            log.error("Error during cancellation of abandoned appointments", e);
        }
    }

    /**
     * Generate appointment analytics reports
     * Runs weekly on Sunday at 6 AM
     */
    @Scheduled(cron = "0 0 6 * * SUN") // Weekly on Sunday at 6:00 AM
    public void generateWeeklyReports() {
        log.info("Generating weekly appointment reports");
        
        try {
            LocalDateTime weekStart = LocalDateTime.now().minus(7, ChronoUnit.DAYS);
            LocalDateTime weekEnd = LocalDateTime.now();
            
            List<Appointment> weeklyAppointments = appointmentRepository.findAll().stream()
                .filter(apt -> apt.getScheduledAt().isAfter(weekStart) && 
                              apt.getScheduledAt().isBefore(weekEnd))
                .toList();
            
            // Generate various reports
            long totalWeeklyAppointments = weeklyAppointments.size();
            long completedThisWeek = weeklyAppointments.stream()
                .mapToLong(apt -> apt.getStatus() == Appointment.AppointmentStatus.COMPLETED ? 1 : 0)
                .sum();
            long cancelledThisWeek = weeklyAppointments.stream()
                .mapToLong(apt -> apt.getStatus() == Appointment.AppointmentStatus.CANCELLED ? 1 : 0)
                .sum();
            
            double completionRate = totalWeeklyAppointments > 0 ? 
                (double) completedThisWeek / totalWeeklyAppointments * 100 : 0;
            
            log.info("Weekly Report - Total appointments: {}, Completed: {}, Cancelled: {}, Completion rate: {:.2f}%",
                totalWeeklyAppointments, completedThisWeek, cancelledThisWeek, completionRate);
            
            // Here you could:
            // 1. Store these metrics in a separate analytics table
            // 2. Send reports to administrators
            // 3. Generate charts and visualizations
            // 4. Trigger alerts if metrics are concerning
            
        } catch (Exception e) {
            log.error("Error during weekly report generation", e);
        }
    }

    // =================== HELPER METHODS ===================
    
    private boolean shouldCreateNextRecurringInstance(Appointment parent) {
        // Check if the next instance in the sequence should be created
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextInstanceTime = calculateNextRecurrenceTime(parent);
        
        // Create instances up to 30 days in advance
        LocalDateTime creationThreshold = now.plus(30, ChronoUnit.DAYS);
        
        if (nextInstanceTime.isAfter(creationThreshold)) {
            return false; // Too far in the future
        }
        
        if (parent.getRecurringEndDate() != null && nextInstanceTime.isAfter(parent.getRecurringEndDate())) {
            return false; // Past the end date
        }
        
        // Check if this instance already exists
        List<Appointment> existingInstances = appointmentRepository.findByParentAppointmentIdOrderByScheduledAtAsc(parent.getId());
        return existingInstances.stream()
            .noneMatch(instance -> instance.getScheduledAt().equals(nextInstanceTime));
    }
    
    private LocalDateTime calculateNextRecurrenceTime(Appointment parent) {
        // Get the last created instance or use parent time if no instances exist
        List<Appointment> instances = appointmentRepository.findByParentAppointmentIdOrderByScheduledAtAsc(parent.getId());
        
        LocalDateTime lastInstanceTime = instances.isEmpty() ? 
            parent.getScheduledAt() : 
            instances.get(instances.size() - 1).getScheduledAt();
        
        return switch (parent.getRecurringPattern()) {
            case WEEKLY -> lastInstanceTime.plus(1, ChronoUnit.WEEKS);
            case BIWEEKLY -> lastInstanceTime.plus(2, ChronoUnit.WEEKS);
            case MONTHLY -> lastInstanceTime.plus(1, ChronoUnit.MONTHS);
        };
    }
    
    private void createNextRecurringInstance(Appointment parent) {
        LocalDateTime nextTime = calculateNextRecurrenceTime(parent);
        
        // Check for conflicts before creating
        List<Appointment> conflicts = appointmentRepository.findConflictingAppointmentsForLecturer(
            parent.getLecturerId(), nextTime, nextTime.plus(parent.getDurationMinutes(), ChronoUnit.MINUTES));
        
        if (!conflicts.isEmpty()) {
            log.warn("Skipping recurring instance for appointment {} at {} due to conflicts", 
                parent.getId(), nextTime);
            return;
        }
        
        Appointment newInstance = Appointment.builder()
            .studentId(parent.getStudentId())
            .lecturerId(parent.getLecturerId())
            .subject(parent.getSubject())
            .description(parent.getDescription())
            .scheduledAt(nextTime)
            .durationMinutes(parent.getDurationMinutes())
            .location(parent.getLocation())
            .type(parent.getType())
            .status(Appointment.AppointmentStatus.PENDING)
            .courseId(parent.getCourseId())
            .meetingLink(parent.getMeetingLink())
            .meetingPassword(parent.getMeetingPassword())
            .isRecurring(false)
            .parentAppointmentId(parent.getId())
            .bookedAt(LocalDateTime.now())
            .lastModifiedAt(LocalDateTime.now())
            .lastModifiedBy("SYSTEM_RECURRING")
            .build();
        
        appointmentRepository.save(newInstance);
        log.info("Created new recurring instance for appointment {} at {}", parent.getId(), nextTime);
    }
}