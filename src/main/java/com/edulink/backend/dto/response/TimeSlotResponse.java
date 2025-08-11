// src/main/java/com/edulink/backend/dto/response/TimeSlotResponse.java
package com.edulink.backend.dto.response;

import com.edulink.backend.model.entity.Appointment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotResponse {
    
    private String id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
    private String location;
    private Appointment.AppointmentType type;
    private boolean isAvailable;
    private boolean isRecurring;
    private Appointment.RecurringPattern recurringPattern;
    private UserProfileResponse lecturer;
}