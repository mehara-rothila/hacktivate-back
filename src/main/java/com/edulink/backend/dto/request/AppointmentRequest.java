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
    
    // Lecturer details
    private UserProfileResponse lecturer;
}