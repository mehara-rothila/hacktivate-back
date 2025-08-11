
// src/main/java/com/edulink/backend/dto/response/GeneratedTimeSlotResponse.java
package com.edulink.backend.dto.response;

import com.edulink.backend.model.entity.Appointment;
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
public class GeneratedTimeSlotResponse {
    
    private String slotId; // Unique identifier for this specific time slot
    private String availabilityId; // ID of the parent availability slot
    
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Integer durationMinutes;
    
    private String location;
    private Appointment.AppointmentType type;
    private boolean isAvailable; // Whether this slot is currently bookable
    private boolean isBooked; // Whether this slot has an appointment
    
    // If booked, appointment details
    private String appointmentId;
    private String appointmentStatus;
    private String studentName;
    
    // Lecturer info
    private UserProfileResponse lecturer;
}