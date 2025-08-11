// src/main/java/com/edulink/backend/model/entity/LecturerAvailability.java
package com.edulink.backend.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "lecturer_availability")
public class LecturerAvailability {
    
    @Id
    private String id;
    
    private String lecturerId;
    
    // Date/time specification - either specific date OR recurring day
    private LocalDate date; // For specific date slots
    private DayOfWeek dayOfWeek; // For recurring slots
    private boolean isRecurring;
    private LocalDate recurringStartDate; // When recurring pattern starts
    private LocalDate recurringEndDate; // When recurring pattern ends (null = indefinite)
    
    // Time specification
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer slotDurationMinutes; // How long each bookable slot is
    
    // Slot details
    private String location;
    private Appointment.AppointmentType allowedType; // What type of appointments are allowed
    private AvailabilityType type; // The nature of this availability
    private String description;
    
    // Status
    @Builder.Default
    private boolean isActive = true;
    
    // Timestamps
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    // Enums
    public enum DayOfWeek {
        MONDAY("Monday"),
        TUESDAY("Tuesday"),
        WEDNESDAY("Wednesday"),
        THURSDAY("Thursday"),
        FRIDAY("Friday"),
        SATURDAY("Saturday"),
        SUNDAY("Sunday");
        
        private final String displayName;
        
        DayOfWeek(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum AvailabilityType {
        OFFICE_HOURS("Office Hours"),
        OPEN("Open Consultation"),
        PROJECT_MEETINGS("Project Meetings"),
        EXAM_REVIEWS("Exam Reviews"),
        THESIS_SUPERVISION("Thesis Supervision"),
        ACADEMIC_ADVISING("Academic Advising"),
        RESEARCH_DISCUSSION("Research Discussion"),
        BLOCKED("Blocked Time"); // For blocking time when not available
        
        private final String displayName;
        
        AvailabilityType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Inner class for generated time slots
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSlot {
        private String slotId;
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
        private LocalDateTime startDateTime;
        private LocalDateTime endDateTime;
        private Integer durationMinutes;
        private String location;
        private Appointment.AppointmentType type;
        
        public String getSlotId() {
            if (slotId == null) {
                slotId = UUID.randomUUID().toString();
            }
            return slotId;
        }
    }
    
    // Helper methods
    public boolean isActive() {
        return isActive;
    }
    
    public boolean isActiveOn(LocalDate checkDate) {
        if (!isActive) {
            return false;
        }
        
        if (isRecurring) {
            // Check if the date falls on the correct day of week
            if (dayOfWeek == null) {
                return false;
            }
            
            java.time.DayOfWeek javaDayOfWeek = checkDate.getDayOfWeek();
            DayOfWeek expectedDayOfWeek = convertFromJavaDayOfWeek(javaDayOfWeek);
            
            if (dayOfWeek != expectedDayOfWeek) {
                return false;
            }
            
            // Check if within recurring date range
            if (recurringStartDate != null && checkDate.isBefore(recurringStartDate)) {
                return false;
            }
            
            if (recurringEndDate != null && checkDate.isAfter(recurringEndDate)) {
                return false;
            }
            
            return true;
        } else {
            // For specific dates, check exact match
            return date != null && date.equals(checkDate);
        }
    }
    
    public List<TimeSlot> generateTimeSlots(LocalDate forDate) {
        List<TimeSlot> slots = new ArrayList<>();
        
        if (!isActiveOn(forDate) || startTime == null || endTime == null || slotDurationMinutes == null) {
            return slots;
        }
        
        LocalTime current = startTime;
        while (current.plusMinutes(slotDurationMinutes).isBefore(endTime) || 
               current.plusMinutes(slotDurationMinutes).equals(endTime)) {
            
            LocalTime slotEndTime = current.plusMinutes(slotDurationMinutes);
            
            TimeSlot slot = TimeSlot.builder()
                .slotId(generateSlotId(forDate, current))
                .date(forDate)
                .startTime(current)
                .endTime(slotEndTime)
                .startDateTime(forDate.atTime(current))
                .endDateTime(forDate.atTime(slotEndTime))
                .durationMinutes(slotDurationMinutes)
                .location(location)
                .type(allowedType != null ? allowedType : Appointment.AppointmentType.OFFICE_HOURS)
                .build();
            
            slots.add(slot);
            current = current.plusMinutes(slotDurationMinutes);
        }
        
        return slots;
    }
    
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Helper methods
    private String generateSlotId(LocalDate date, LocalTime time) {
        return id + "-" + date.toString() + "-" + time.toString();
    }
    
    private DayOfWeek convertFromJavaDayOfWeek(java.time.DayOfWeek javaDayOfWeek) {
        switch (javaDayOfWeek) {
            case MONDAY: return DayOfWeek.MONDAY;
            case TUESDAY: return DayOfWeek.TUESDAY;
            case WEDNESDAY: return DayOfWeek.WEDNESDAY;
            case THURSDAY: return DayOfWeek.THURSDAY;
            case FRIDAY: return DayOfWeek.FRIDAY;
            case SATURDAY: return DayOfWeek.SATURDAY;
            case SUNDAY: return DayOfWeek.SUNDAY;
            default: throw new IllegalArgumentException("Unknown day of week: " + javaDayOfWeek);
        }
    }
    
    // Display helpers
    public String getDisplayName() {
        if (isRecurring && dayOfWeek != null) {
            return dayOfWeek.getDisplayName() + " " + type.getDisplayName();
        } else if (date != null) {
            return date.toString() + " " + type.getDisplayName();
        }
        return type.getDisplayName();
    }
    
    public String getTimeRange() {
        if (startTime != null && endTime != null) {
            return startTime + " - " + endTime;
        }
        return "";
    }
}