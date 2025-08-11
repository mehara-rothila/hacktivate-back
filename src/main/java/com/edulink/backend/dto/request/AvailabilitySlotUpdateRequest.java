
// src/main/java/com/edulink/backend/dto/request/AvailabilitySlotUpdateRequest.java
package com.edulink.backend.dto.request;

import com.edulink.backend.model.entity.Appointment;
import com.edulink.backend.model.entity.LecturerAvailability;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilitySlotUpdateRequest {
    
    private LocalDate date;
    private LecturerAvailability.DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer slotDurationMinutes;
    private String location;
    private Appointment.AppointmentType allowedType;
    private LecturerAvailability.AvailabilityType type;
    private String description;
    private Boolean isActive;
    private LocalDate recurringStartDate;
    private LocalDate recurringEndDate;
}
