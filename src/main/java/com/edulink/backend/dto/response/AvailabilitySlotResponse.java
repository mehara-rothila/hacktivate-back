// src/main/java/com/edulink/backend/dto/response/AvailabilitySlotResponse.java
package com.edulink.backend.dto.response;

import com.edulink.backend.model.entity.Appointment;
import com.edulink.backend.model.entity.LecturerAvailability;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilitySlotResponse {
    
    private String id;
    private String lecturerId;
    
    // Date/time information
    private LocalDate date;
    private LecturerAvailability.DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer slotDurationMinutes;
    
    // Slot details
    private String location;
    private Appointment.AppointmentType allowedType;
    private LecturerAvailability.AvailabilityType type;
    private String description;
    
    // Settings - FIXED: Force correct JSON field names
    @JsonProperty("isActive")
    private boolean isActive;
    
    @JsonProperty("isRecurring")
    private boolean isRecurring;
    
    private LocalDate recurringStartDate;
    private LocalDate recurringEndDate;
    
    // Computed fields
    private String displayName;
    private String timeRange;
    private Integer totalSlots; // How many bookable slots this generates
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}