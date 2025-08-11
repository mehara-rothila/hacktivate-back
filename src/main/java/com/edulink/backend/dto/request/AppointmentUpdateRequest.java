// src/main/java/com/edulink/backend/dto/request/AppointmentUpdateRequest.java
package com.edulink.backend.dto.request;

import com.edulink.backend.model.entity.Appointment;
import jakarta.validation.constraints.Positive;
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
public class AppointmentUpdateRequest {
    
    private String subject;
    private String description;
    private LocalDateTime scheduledAt;
    
    @Positive(message = "Duration must be positive")
    private Integer durationMinutes;
    
    private String location;
    private Appointment.AppointmentType type;
    
    // Meeting details
    private String meetingLink;
    private String meetingPassword;
    
    // Optional fields
    private String courseId;
    private List<String> attachmentIds;
    
    // Notes (typically only lecturers can update this)
    private String notes;
    
    // Recurring appointment updates
    private Boolean isRecurring;
    private Appointment.RecurringPattern recurringPattern;
    private LocalDateTime recurringEndDate;
}