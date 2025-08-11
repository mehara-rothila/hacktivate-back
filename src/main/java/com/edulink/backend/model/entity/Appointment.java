// src/main/java/com/edulink/backend/model/entity/Appointment.java
package com.edulink.backend.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "appointments")
public class Appointment {
    
    @Id
    private String id;
    
    // Participants
    private String studentId;
    private String lecturerId;
    
    // Appointment Details
    private String subject;
    private String description;
    private LocalDateTime scheduledAt; // Date and time of appointment
    private Integer durationMinutes; // Duration in minutes
    private String location; // Physical location or "Online"
    private AppointmentType type;
    private AppointmentStatus status;
    
    // Meeting Details
    private String meetingLink; // For online meetings
    private String meetingPassword; // For online meetings
    
    // Metadata
    private String courseId; // Optional - if related to specific course
    private String notes; // Lecturer's notes about the appointment
    private LocalDateTime bookedAt; // When the appointment was originally booked
    private LocalDateTime lastModifiedAt;
    private String lastModifiedBy; // User ID who last modified
    
    // Recurring appointments support
    private boolean isRecurring;
    private RecurringPattern recurringPattern;
    private LocalDateTime recurringEndDate;
    private String parentAppointmentId; // For recurring appointment instances
    
    // Attachments/Resources
    private List<String> attachmentIds; // References to uploaded files
    
    // Timestamps
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    // Enums
    public enum AppointmentType {
        OFFICE_HOURS("Office Hours"),
        CONSULTATION("Consultation"), 
        PROJECT_DISCUSSION("Project Discussion"),
        EXAM_REVIEW("Exam Review"),
        THESIS_GUIDANCE("Thesis Guidance"),
        ACADEMIC_ADVISING("Academic Advising"),
        OTHER("Other");
        
        private final String displayName;
        
        AppointmentType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum AppointmentStatus {
        PENDING("Pending Confirmation"),
        CONFIRMED("Confirmed"),
        CANCELLED("Cancelled"),
        COMPLETED("Completed"),
        NO_SHOW("No Show"),
        RESCHEDULED("Rescheduled");
        
        private final String displayName;
        
        AppointmentStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum RecurringPattern {
        WEEKLY("Weekly"),
        BIWEEKLY("Bi-weekly"),
        MONTHLY("Monthly");
        
        private final String displayName;
        
        RecurringPattern(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Helper methods
    public boolean isPending() {
        return status == AppointmentStatus.PENDING;
    }
    
    public boolean isConfirmed() {
        return status == AppointmentStatus.CONFIRMED;
    }
    
    public boolean isCancelled() {
        return status == AppointmentStatus.CANCELLED;
    }
    
    public boolean isCompleted() {
        return status == AppointmentStatus.COMPLETED;
    }
    
    public boolean isUpcoming() {
        return (status == AppointmentStatus.CONFIRMED || status == AppointmentStatus.PENDING) 
               && scheduledAt.isAfter(LocalDateTime.now());
    }
    
    public boolean isPast() {
        return scheduledAt.isBefore(LocalDateTime.now());
    }
    
    public boolean isToday() {
        LocalDateTime now = LocalDateTime.now();
        return scheduledAt.toLocalDate().equals(now.toLocalDate());
    }
    
    public LocalDateTime getEndTime() {
        return scheduledAt.plusMinutes(durationMinutes != null ? durationMinutes : 30);
    }
    
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}