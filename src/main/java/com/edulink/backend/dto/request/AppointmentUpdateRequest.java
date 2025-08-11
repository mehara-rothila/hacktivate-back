
// src/main/java/com/edulink/backend/dto/request/AppointmentUpdateRequest.java
package com.edulink.backend.dto.request;

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
public class AppointmentUpdateRequest {
    
    private String subject;
    private String description;
    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    private String location;
    private Appointment.AppointmentType type;
    private String meetingLink;
    private String meetingPassword;
    private String notes;
    private List<String> attachmentIds;
}