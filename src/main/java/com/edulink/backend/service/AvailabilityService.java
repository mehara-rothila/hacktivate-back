// src/main/java/com/edulink/backend/service/AvailabilityService.java
package com.edulink.backend.service;

import com.edulink.backend.dto.request.AvailabilitySlotRequest;
import com.edulink.backend.dto.request.AvailabilitySlotUpdateRequest;
import com.edulink.backend.dto.response.AvailabilitySlotResponse;
import com.edulink.backend.dto.response.GeneratedTimeSlotResponse;
import com.edulink.backend.model.entity.Appointment;
import com.edulink.backend.model.entity.LecturerAvailability;
import com.edulink.backend.model.entity.User;
import com.edulink.backend.repository.AppointmentRepository;
import com.edulink.backend.repository.LecturerAvailabilityRepository;
import com.edulink.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AvailabilityService {

    private final LecturerAvailabilityRepository availabilityRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    // =================== CREATE AVAILABILITY SLOT ===================
    public AvailabilitySlotResponse createAvailabilitySlot(AvailabilitySlotRequest request, String lecturerId) {
        log.info("Creating availability slot for lecturer: {}", lecturerId);

        // Validate lecturer exists
        User lecturer = validateLecturer(lecturerId);

        // Validate request
        validateAvailabilityRequest(request, lecturerId);

        // Build availability slot
        LecturerAvailability availability = buildAvailabilitySlot(request, lecturerId);

        // Save to database
        LecturerAvailability savedAvailability = availabilityRepository.save(availability);
        
        log.info("Availability slot created successfully with ID: {}", savedAvailability.getId());
        return mapToAvailabilitySlotResponse(savedAvailability);
    }

    // =================== GET LECTURER AVAILABILITY SLOTS ===================
    public List<AvailabilitySlotResponse> getLecturerAvailabilitySlots(String lecturerId, boolean activeOnly, 
                                                                      LecturerAvailability.AvailabilityType type) {
        log.info("Getting availability slots for lecturer: {} (activeOnly: {}, type: {})", lecturerId, activeOnly, type);

        List<LecturerAvailability> availabilitySlots;
        
        if (activeOnly) {
            if (type != null) {
                availabilitySlots = availabilityRepository.findByLecturerIdAndTypeAndIsActiveTrueOrderByDateAscStartTimeAsc(lecturerId, type);
            } else {
                availabilitySlots = availabilityRepository.findByLecturerIdAndIsActiveTrueOrderByDateAscStartTimeAsc(lecturerId);
            }
        } else {
            availabilitySlots = availabilityRepository.findByLecturerIdOrderByDateAscStartTimeAsc(lecturerId);
            if (type != null) {
                availabilitySlots = availabilitySlots.stream()
                    .filter(slot -> slot.getType() == type)
                    .collect(Collectors.toList());
            }
        }

        return availabilitySlots.stream()
            .map(this::mapToAvailabilitySlotResponse)
            .collect(Collectors.toList());
    }

    // =================== GET GENERATED TIME SLOTS FOR DATE ===================
    public List<GeneratedTimeSlotResponse> getGeneratedTimeSlotsForDate(String lecturerId, LocalDate date) {
        log.info("Getting generated time slots for lecturer {} on date {}", lecturerId, date);

        // Get availability slots for this date
        String dayOfWeek = date.getDayOfWeek().name();
        List<LecturerAvailability> availabilitySlots = availabilityRepository.findByLecturerIdAndDate(
            lecturerId, date, dayOfWeek);

        // Get existing appointments for this date
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        List<Appointment> existingAppointments = appointmentRepository.findByUserIdAndScheduledAtBetween(
            lecturerId, startOfDay, endOfDay)
            .stream()
            .filter(apt -> apt.getStatus() == Appointment.AppointmentStatus.CONFIRMED || 
                          apt.getStatus() == Appointment.AppointmentStatus.PENDING)
            .collect(Collectors.toList());

        // Generate time slots from availability
        List<GeneratedTimeSlotResponse> timeSlots = new ArrayList<>();
        
        for (LecturerAvailability availability : availabilitySlots) {
            if (availability.isActiveOn(date)) {
                List<LecturerAvailability.TimeSlot> generatedSlots = availability.generateTimeSlots(date);
                
                for (LecturerAvailability.TimeSlot slot : generatedSlots) {
                    // Check if slot is available (no conflicting appointments)
                    boolean isAvailable = existingAppointments.stream()
                        .noneMatch(apt -> {
                            LocalDateTime aptStart = apt.getScheduledAt();
                            LocalDateTime aptEnd = apt.getEndTime();
                            LocalDateTime slotStart = slot.getStartDateTime();
                            LocalDateTime slotEnd = slot.getEndDateTime();
                            return (slotStart.isBefore(aptEnd) && slotEnd.isAfter(aptStart));
                        });

                    // Find conflicting appointment if any
                    Optional<Appointment> conflictingAppointment = existingAppointments.stream()
                        .filter(apt -> {
                            LocalDateTime aptStart = apt.getScheduledAt();
                            LocalDateTime aptEnd = apt.getEndTime();
                            LocalDateTime slotStart = slot.getStartDateTime();
                            LocalDateTime slotEnd = slot.getEndDateTime();
                            return (slotStart.isBefore(aptEnd) && slotEnd.isAfter(aptStart));
                        })
                        .findFirst();

                    GeneratedTimeSlotResponse.GeneratedTimeSlotResponseBuilder builder = GeneratedTimeSlotResponse.builder()
                        .slotId(slot.getSlotId())
                        .availabilityId(availability.getId())
                        .date(date)
                        .startTime(slot.getStartTime())
                        .endTime(slot.getEndTime())
                        .startDateTime(slot.getStartDateTime())
                        .endDateTime(slot.getEndDateTime())
                        .durationMinutes(slot.getDurationMinutes())
                        .location(slot.getLocation())
                        .type(slot.getType())
                        .isAvailable(isAvailable)
                        .isBooked(!isAvailable);

                    if (conflictingAppointment.isPresent()) {
                        Appointment apt = conflictingAppointment.get();
                        User student = userRepository.findById(apt.getStudentId()).orElse(null);
                        builder.appointmentId(apt.getId())
                               .appointmentStatus(apt.getStatus().toString())
                               .studentName(student != null ? student.getFullName() : "Unknown Student");
                    }

                    timeSlots.add(builder.build());
                }
            }
        }

        // Sort by start time
        timeSlots.sort(Comparator.comparing(GeneratedTimeSlotResponse::getStartTime));

        log.info("Generated {} time slots for lecturer {} on {}", timeSlots.size(), lecturerId, date);
        return timeSlots;
    }

    // =================== UPDATE AVAILABILITY SLOT ===================
    public AvailabilitySlotResponse updateAvailabilitySlot(String slotId, AvailabilitySlotUpdateRequest request, 
                                                          String lecturerId) {
        log.info("Updating availability slot {} by lecturer: {}", slotId, lecturerId);

        LecturerAvailability availability = getAvailabilitySlotById(slotId, lecturerId);

        // Update fields
        if (request.getDate() != null) {
            availability.setDate(request.getDate());
            availability.setRecurring(false); // FIXED: was setIsRecurring(false)
        }
        
        if (request.getDayOfWeek() != null) {
            availability.setDayOfWeek(request.getDayOfWeek());
            availability.setRecurring(true); // FIXED: was setIsRecurring(true)
        }
        
        if (request.getStartTime() != null) {
            availability.setStartTime(request.getStartTime());
        }
        
        if (request.getEndTime() != null) {
            availability.setEndTime(request.getEndTime());
        }
        
        if (request.getSlotDurationMinutes() != null) {
            availability.setSlotDurationMinutes(request.getSlotDurationMinutes());
        }
        
        if (request.getLocation() != null) {
            availability.setLocation(request.getLocation());
        }
        
        if (request.getAllowedType() != null) {
            availability.setAllowedType(request.getAllowedType());
        }
        
        if (request.getType() != null) {
            availability.setType(request.getType());
        }
        
        if (request.getDescription() != null) {
            availability.setDescription(request.getDescription());
        }
        
        if (request.getIsActive() != null) {
            availability.setActive(request.getIsActive()); // FIXED: was setIsActive(request.getIsActive())
        }
        
        if (request.getRecurringStartDate() != null) {
            availability.setRecurringStartDate(request.getRecurringStartDate());
        }
        
        if (request.getRecurringEndDate() != null) {
            availability.setRecurringEndDate(request.getRecurringEndDate());
        }

        availability.updateTimestamp();
        LecturerAvailability updatedAvailability = availabilityRepository.save(availability);

        log.info("Availability slot {} updated successfully", slotId);
        return mapToAvailabilitySlotResponse(updatedAvailability);
    }

    // =================== TOGGLE AVAILABILITY SLOT STATUS ===================
    public AvailabilitySlotResponse toggleAvailabilitySlot(String slotId, String lecturerId) {
        log.info("Toggling availability slot {} by lecturer: {}", slotId, lecturerId);

        LecturerAvailability availability = getAvailabilitySlotById(slotId, lecturerId);
        availability.setActive(!availability.isActive()); // FIXED: was setIsActive(!availability.isActive())
        availability.updateTimestamp();

        LecturerAvailability updatedAvailability = availabilityRepository.save(availability);
        log.info("Availability slot {} toggled to {}", slotId, updatedAvailability.isActive() ? "active" : "inactive");
        
        return mapToAvailabilitySlotResponse(updatedAvailability);
    }

    // =================== DELETE AVAILABILITY SLOT ===================
    public void deleteAvailabilitySlot(String slotId, String lecturerId) {
        log.info("Deleting availability slot {} by lecturer: {}", slotId, lecturerId);

        LecturerAvailability availability = getAvailabilitySlotById(slotId, lecturerId);
        
        // Check if there are any future appointments based on this availability
        // For simplicity, we'll allow deletion but could add validation here
        
        availabilityRepository.delete(availability);
        log.info("Availability slot {} deleted successfully", slotId);
    }

    // =================== BULK OPERATIONS ===================
    public List<AvailabilitySlotResponse> createBulkAvailabilitySlots(List<AvailabilitySlotRequest> requests, 
                                                                     String lecturerId) {
        log.info("Creating {} bulk availability slots for lecturer: {}", requests.size(), lecturerId);

        validateLecturer(lecturerId);
        
        List<AvailabilitySlotResponse> responses = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        for (int i = 0; i < requests.size(); i++) {
            try {
                AvailabilitySlotRequest request = requests.get(i);
                validateAvailabilityRequest(request, lecturerId);
                
                LecturerAvailability availability = buildAvailabilitySlot(request, lecturerId);
                LecturerAvailability savedAvailability = availabilityRepository.save(availability);
                responses.add(mapToAvailabilitySlotResponse(savedAvailability));
                
            } catch (Exception e) {
                log.warn("Failed to create bulk availability slot at index {}: {}", i, e.getMessage());
                errors.add("Slot " + (i + 1) + ": " + e.getMessage());
            }
        }
        
        if (!errors.isEmpty()) {
            throw new RuntimeException("Some slots failed to create: " + String.join("; ", errors));
        }
        
        log.info("Successfully created {} bulk availability slots", responses.size());
        return responses;
    }

    public void deleteBulkAvailabilitySlots(List<String> slotIds, String lecturerId) {
        log.info("Deleting {} bulk availability slots for lecturer: {}", slotIds.size(), lecturerId);

        List<String> errors = new ArrayList<>();
        int deletedCount = 0;
        
        for (String slotId : slotIds) {
            try {
                deleteAvailabilitySlot(slotId, lecturerId);
                deletedCount++;
            } catch (Exception e) {
                log.warn("Failed to delete availability slot {}: {}", slotId, e.getMessage());
                errors.add("Slot " + slotId + ": " + e.getMessage());
            }
        }
        
        if (!errors.isEmpty()) {
            throw new RuntimeException("Some slots failed to delete: " + String.join("; ", errors));
        }
        
        log.info("Successfully deleted {} bulk availability slots", deletedCount);
    }

    // =================== AVAILABILITY STATISTICS ===================
    public Object getAvailabilityStats(String lecturerId) {
        log.info("Getting availability statistics for lecturer: {}", lecturerId);

        long totalSlots = availabilityRepository.countByLecturerIdAndIsActiveTrue(lecturerId);
        long recurringSlots = availabilityRepository.findByLecturerIdAndIsRecurringTrueAndIsActiveTrueOrderByDayOfWeekAscStartTimeAsc(lecturerId).size();
        long oneTimeSlots = availabilityRepository.findByLecturerIdAndIsRecurringFalseAndIsActiveTrueAndDateGreaterThanEqualOrderByDateAscStartTimeAsc(lecturerId, LocalDate.now()).size();

        // Calculate total bookable slots for next 7 days
        int bookableSlotsCount = 0;
        for (int i = 0; i < 7; i++) {
            LocalDate checkDate = LocalDate.now().plusDays(i);
            List<GeneratedTimeSlotResponse> daySlots = getGeneratedTimeSlotsForDate(lecturerId, checkDate);
            bookableSlotsCount += (int) daySlots.stream().filter(GeneratedTimeSlotResponse::isAvailable).count();
        }
        
        // Make it effectively final by assigning to a final variable
        final int bookableSlotsNext7Days = bookableSlotsCount;

        return new Object() {
            public final long total = totalSlots;
            public final long recurring = recurringSlots;
            public final long oneTime = oneTimeSlots;
            public final int availableNext7Days = bookableSlotsNext7Days;
        };
    }

    // =================== HELPER METHODS ===================
    private User validateLecturer(String lecturerId) {
        User lecturer = userRepository.findById(lecturerId)
                .orElseThrow(() -> new IllegalArgumentException("Lecturer not found: " + lecturerId));
        
        if (lecturer.getRole() != User.UserRole.LECTURER) {
            throw new IllegalArgumentException("User is not a lecturer: " + lecturerId);
        }
        
        return lecturer;
    }

    private void validateAvailabilityRequest(AvailabilitySlotRequest request, String lecturerId) {
        // Validate time logic
        if (request.getStartTime() != null && request.getEndTime() != null) {
            if (!request.getStartTime().isBefore(request.getEndTime())) {
                throw new IllegalArgumentException("Start time must be before end time");
            }
        }

        // Validate recurring vs specific date logic
        if (request.isRecurring()) {
            if (request.getDayOfWeek() == null) {
                throw new IllegalArgumentException("Day of week is required for recurring slots");
            }
            if (request.getDate() != null) {
                throw new IllegalArgumentException("Cannot set specific date for recurring slots");
            }
        } else {
            if (request.getDate() == null) {
                throw new IllegalArgumentException("Date is required for non-recurring slots");
            }
            if (request.getDayOfWeek() != null) {
                throw new IllegalArgumentException("Cannot set day of week for non-recurring slots");
            }
        }

        // Check for conflicts (simplified - could be more robust)
        // This would check for overlapping availability slots
    }

    private LecturerAvailability buildAvailabilitySlot(AvailabilitySlotRequest request, String lecturerId) {
        return LecturerAvailability.builder()
                .lecturerId(lecturerId)
                .date(request.getDate())
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .slotDurationMinutes(request.getSlotDurationMinutes())
                .location(request.getLocation())
                .allowedType(request.getAllowedType())
                .type(request.getType() != null ? request.getType() : LecturerAvailability.AvailabilityType.OPEN)
                .description(request.getDescription())
                .isActive(request.isActive())
                .isRecurring(request.isRecurring())
                .recurringStartDate(request.getRecurringStartDate())
                .recurringEndDate(request.getRecurringEndDate())
                .build();
    }

    private LecturerAvailability getAvailabilitySlotById(String slotId, String lecturerId) {
        LecturerAvailability availability = availabilityRepository.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Availability slot not found: " + slotId));
        
        if (!availability.getLecturerId().equals(lecturerId)) {
            throw new SecurityException("You can only modify your own availability slots");
        }
        
        return availability;
    }

    private AvailabilitySlotResponse mapToAvailabilitySlotResponse(LecturerAvailability availability) {
        // Calculate display name and total slots
        String displayName = generateDisplayName(availability);
        String timeRange = availability.getStartTime() + " - " + availability.getEndTime();
        Integer totalSlots = calculateTotalSlots(availability);

        return AvailabilitySlotResponse.builder()
                .id(availability.getId())
                .lecturerId(availability.getLecturerId())
                .date(availability.getDate())
                .dayOfWeek(availability.getDayOfWeek())
                .startTime(availability.getStartTime())
                .endTime(availability.getEndTime())
                .slotDurationMinutes(availability.getSlotDurationMinutes())
                .location(availability.getLocation())
                .allowedType(availability.getAllowedType())
                .type(availability.getType())
                .description(availability.getDescription())
                .isActive(availability.isActive())
                .isRecurring(availability.isRecurring())
                .recurringStartDate(availability.getRecurringStartDate())
                .recurringEndDate(availability.getRecurringEndDate())
                .displayName(displayName)
                .timeRange(timeRange)
                .totalSlots(totalSlots)
                .createdAt(availability.getCreatedAt())
                .updatedAt(availability.getUpdatedAt())
                .build();
    }

    private String generateDisplayName(LecturerAvailability availability) {
        if (availability.isRecurring() && availability.getDayOfWeek() != null) {
            String dayName = availability.getDayOfWeek().name().toLowerCase();
            dayName = dayName.substring(0, 1).toUpperCase() + dayName.substring(1);
            return dayName + " " + availability.getType().getDisplayName();
        } else if (availability.getDate() != null) {
            return availability.getDate().toString() + " " + availability.getType().getDisplayName();
        }
        return availability.getType().getDisplayName();
    }

    private Integer calculateTotalSlots(LecturerAvailability availability) {
        if (availability.getStartTime() == null || availability.getEndTime() == null || 
            availability.getSlotDurationMinutes() == null) {
            return 0;
        }

        LocalTime current = availability.getStartTime();
        int count = 0;
        
        while (current.plusMinutes(availability.getSlotDurationMinutes()).isBefore(availability.getEndTime()) || 
               current.plusMinutes(availability.getSlotDurationMinutes()).equals(availability.getEndTime())) {
            count++;
            current = current.plusMinutes(availability.getSlotDurationMinutes());
        }
        
        return count;
    }
}