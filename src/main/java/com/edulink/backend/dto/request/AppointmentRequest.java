// src/main/java/com/edulink/backend/dto/request/AppointmentRequest.java
package com.edulink.backend.dto.request;

import com.edulink.backend.model.entity.Appointment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class AppointmentRequest {
    
    @NotBlank(message = "Subject is required")
    private String subject;
    
    private String description;
    
    @NotNull(message = "Scheduled time is required")
    private LocalDateTime scheduledAt;
    
    @Positive(message = "Duration must be positive")
    private Integer durationMinutes;
    
    @NotBlank(message = "Location is required")
    private String location;
    
    @NotNull(message = "Appointment type is required")
    private Appointment.AppointmentType type;
    
    // For students booking with lecturers
    private String lecturerId;
    
    // For lecturers creating appointments
    private String studentId;
    
    // Optional fields
    private String courseId;
    private String meetingLink;
    private String meetingPassword;
    private List<String> attachmentIds;
    
    // Recurring appointment fields
    private boolean isRecurring;
    private Appointment.RecurringPattern recurringPattern;
    private LocalDateTime recurringEndDate;
}

