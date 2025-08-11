
// src/main/java/com/edulink/backend/dto/request/AppointmentStatusUpdateRequest.java
package com.edulink.backend.dto.request;

import com.edulink.backend.model.entity.Appointment;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentStatusUpdateRequest {
    
    @NotNull(message = "Status is required")
    private Appointment.AppointmentStatus status;
    
    private String notes; // Optional notes when updating status
}
