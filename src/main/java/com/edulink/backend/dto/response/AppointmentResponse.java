
// src/main/java/com/edulink/backend/dto/response/AppointmentResponse.java
package com.edulink.backend.dto.response;

import com.edulink.backend.model.entity.Appointment;
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
public class AppointmentResponse {
    
    private String id;
    private String subject;
    private String description;
    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    private String location;
    private Appointment.AppointmentType type;
    private Appointment.AppointmentStatus status;
    private String meetingLink;
    private String notes;
    private LocalDateTime bookedAt;
    private LocalDateTime lastModifiedAt;
    private String lastModifiedBy;
    
    // Participant details
    private UserProfileResponse student;
    private UserProfileResponse lecturer;
    
    // Course details if applicable
    private String courseId;
    private String courseName;
    
    // Recurring details
    private boolean isRecurring;
    private Appointment.RecurringPattern recurringPattern;
    private LocalDateTime recurringEndDate;
    private String parentAppointmentId;
    
    // Attachments
    private List<String> attachmentIds;
    
    // Calculated fields
    private LocalDateTime endTime;
    private boolean isUpcoming;
    private boolean isPast;
    private boolean isToday;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
