// src/main/java/com/edulink/backend/dto/request/AvailabilitySlotRequest.java
package com.edulink.backend.dto.request;

import com.edulink.backend.model.entity.Appointment;
import com.edulink.backend.model.entity.LecturerAvailability;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class AvailabilitySlotRequest {
    
    // For specific date slots
    private LocalDate date;
    
    // For recurring slots
    private LecturerAvailability.DayOfWeek dayOfWeek;
    private boolean isRecurring;
    private LocalDate recurringStartDate;
    private LocalDate recurringEndDate;
    
    @NotNull(message = "Start time is required")
    private LocalTime startTime;
    
    @NotNull(message = "End time is required") 
    private LocalTime endTime;
    
    @Positive(message = "Slot duration must be positive")
    private Integer slotDurationMinutes;
    
    @NotNull(message = "Location is required")
    private String location;
    
    private Appointment.AppointmentType allowedType;
    private LecturerAvailability.AvailabilityType type;
    private String description;
    
    @Builder.Default
    private boolean isActive = true;
}

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

// src/main/java/com/edulink/backend/dto/response/AvailabilitySlotResponse.java
package com.edulink.backend.dto.response;

import com.edulink.backend.model.entity.Appointment;
import com.edulink.backend.model.entity.LecturerAvailability;
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
    
    // Settings
    private boolean isActive;
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