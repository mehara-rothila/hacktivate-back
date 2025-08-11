// src/main/java/com/edulink/backend/controller/AppointmentController.java
package com.edulink.backend.controller;

import com.edulink.backend.dto.request.AppointmentRequest;
import com.edulink.backend.dto.request.AppointmentStatusUpdateRequest;
import com.edulink.backend.dto.request.AppointmentUpdateRequest;
import com.edulink.backend.dto.response.ApiResponse;
import com.edulink.backend.dto.response.AppointmentResponse;
import com.edulink.backend.dto.response.TimeSlotResponse;
import com.edulink.backend.dto.response.UserProfileResponse;
import com.edulink.backend.model.entity.Appointment;
import com.edulink.backend.model.entity.Course;
import com.edulink.backend.model.entity.User;
import com.edulink.backend.repository.AppointmentRepository;
import com.edulink.backend.repository.CourseRepository;
import com.edulink.backend.repository.UserRepository;
import com.edulink.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Slf4j
public class AppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final UserService userService;

    // =================== CREATE APPOINTMENT ===================
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AppointmentResponse>> createAppointment(@Valid @RequestBody AppointmentRequest request) {
        User currentUser = userService.getCurrentUser();
        
        // Validate participants
        String studentId, lecturerId;
        if (currentUser.getRole() == User.UserRole.STUDENT) {
            studentId = currentUser.getId();
            lecturerId = request.getLecturerId();
            if (lecturerId == null) {
                throw new RuntimeException("Lecturer ID is required when student creates appointment");
            }
        } else if (currentUser.getRole() == User.UserRole.LECTURER) {
            lecturerId = currentUser.getId();
            studentId = request.getStudentId();
            if (studentId == null) {
                throw new RuntimeException("Student ID is required when lecturer creates appointment");
            }
        } else {
            throw new SecurityException("Only students and lecturers can create appointments");
        }

        // Validate participants exist
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        User lecturer = userRepository.findById(lecturerId)
                .orElseThrow(() -> new RuntimeException("Lecturer not found"));

        if (student.getRole() != User.UserRole.STUDENT) {
            throw new RuntimeException("Specified user is not a student");
        }
        if (lecturer.getRole() != User.UserRole.LECTURER) {
            throw new RuntimeException("Specified user is not a lecturer");
        }

        // Check for scheduling conflicts
        LocalDateTime endTime = request.getScheduledAt().plusMinutes(request.getDurationMinutes());
        
        List<Appointment> lecturerConflicts = appointmentRepository.findConflictingAppointmentsForLecturer(
                lecturerId, request.getScheduledAt(), endTime);
        if (!lecturerConflicts.isEmpty()) {
            throw new RuntimeException("Lecturer has a conflicting appointment at this time");
        }

        List<Appointment> studentConflicts = appointmentRepository.findConflictingAppointmentsForStudent(
                studentId, request.getScheduledAt(), endTime);
        if (!studentConflicts.isEmpty()) {
            throw new RuntimeException("Student has a conflicting appointment at this time");
        }

        // Create appointment
        Appointment appointment = Appointment.builder()
                .studentId(studentId)
                .lecturerId(lecturerId)
                .subject(request.getSubject())
                .description(request.getDescription())
                .scheduledAt(request.getScheduledAt())
                .durationMinutes(request.getDurationMinutes())
                .location(request.getLocation())
                .type(request.getType())
                .status(Appointment.AppointmentStatus.PENDING)
                .courseId(request.getCourseId())
                .meetingLink(request.getMeetingLink())
                .meetingPassword(request.getMeetingPassword())
                .attachmentIds(request.getAttachmentIds())
                .isRecurring(request.isRecurring())
                .recurringPattern(request.getRecurringPattern())
                .recurringEndDate(request.getRecurringEndDate())
                .bookedAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .lastModifiedBy(currentUser.getId())
                .build();

        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Create recurring instances if needed
        if (request.isRecurring() && request.getRecurringEndDate() != null) {
            createRecurringInstances(savedAppointment, request);
        }

        return new ResponseEntity<>(
                ApiResponse.<AppointmentResponse>builder()
                        .success(true)
                        .message("Appointment created successfully")
                        .data(mapToAppointmentResponse(savedAppointment))
                        .build(),
                HttpStatus.CREATED
        );
    }

    // =================== GET APPOINTMENTS ===================
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getMyAppointments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String courseId) {
        
        User currentUser = userService.getCurrentUser();
        
        Appointment.AppointmentStatus statusFilter = null;
        if (status != null) {
            try {
                statusFilter = Appointment.AppointmentStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid status: " + status);
            }
        }
        
        Appointment.AppointmentType typeFilter = null;
        if (type != null) {
            try {
                typeFilter = Appointment.AppointmentType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid type: " + type);
            }
        }

        List<Appointment> appointments = appointmentRepository.findAppointmentsByFilters(
                currentUser.getId(), statusFilter, typeFilter, courseId);

        List<AppointmentResponse> appointmentResponses = appointments.stream()
                .map(this::mapToAppointmentResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                ApiResponse.<List<AppointmentResponse>>builder()
                        .success(true)
                        .message("Appointments retrieved successfully")
                        .data(appointmentResponses)
                        .build()
        );
    }

    // =================== GET SPECIFIC APPOINTMENT ===================
    @GetMapping("/{appointmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointment(@PathVariable String appointmentId) {
        User currentUser = userService.getCurrentUser();
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Check if user is participant
        if (!appointment.getStudentId().equals(currentUser.getId()) && 
            !appointment.getLecturerId().equals(currentUser.getId())) {
            throw new SecurityException("User is not a participant in this appointment");
        }

        return ResponseEntity.ok(
                ApiResponse.<AppointmentResponse>builder()
                        .success(true)
                        .message("Appointment retrieved successfully")
                        .data(mapToAppointmentResponse(appointment))
                        .build()
        );
    }

    // =================== UPDATE APPOINTMENT ===================
    @PutMapping("/{appointmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateAppointment(
            @PathVariable String appointmentId,
            @Valid @RequestBody AppointmentUpdateRequest request) {
        
        User currentUser = userService.getCurrentUser();
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Check permissions - only participants can update
        if (!appointment.getStudentId().equals(currentUser.getId()) && 
            !appointment.getLecturerId().equals(currentUser.getId())) {
            throw new SecurityException("User is not a participant in this appointment");
        }

        // Students can only update certain fields, lecturers can update more
        boolean isLecturer = appointment.getLecturerId().equals(currentUser.getId());

        // Update allowed fields
        if (request.getSubject() != null) {
            appointment.setSubject(request.getSubject());
        }
        if (request.getDescription() != null) {
            appointment.setDescription(request.getDescription());
        }
        if (request.getScheduledAt() != null) {
            // Check for conflicts if time is being changed
            LocalDateTime newEndTime = request.getScheduledAt().plusMinutes(
                    request.getDurationMinutes() != null ? request.getDurationMinutes() : appointment.getDurationMinutes());
            
            // Check lecturer conflicts
            List<Appointment> lecturerConflicts = appointmentRepository.findConflictingAppointmentsForLecturer(
                    appointment.getLecturerId(), request.getScheduledAt(), newEndTime)
                    .stream().filter(a -> !a.getId().equals(appointmentId))
                    .collect(Collectors.toList());
            if (!lecturerConflicts.isEmpty()) {
                throw new RuntimeException("Lecturer has a conflicting appointment at this time");
            }

            // Check student conflicts
            List<Appointment> studentConflicts = appointmentRepository.findConflictingAppointmentsForStudent(
                    appointment.getStudentId(), request.getScheduledAt(), newEndTime)
                    .stream().filter(a -> !a.getId().equals(appointmentId))
                    .collect(Collectors.toList());
            if (!studentConflicts.isEmpty()) {
                throw new RuntimeException("Student has a conflicting appointment at this time");
            }

            appointment.setScheduledAt(request.getScheduledAt());
        }
        if (request.getDurationMinutes() != null) {
            appointment.setDurationMinutes(request.getDurationMinutes());
        }
        if (request.getLocation() != null) {
            appointment.setLocation(request.getLocation());
        }
        if (request.getType() != null) {
            appointment.setType(request.getType());
        }
        if (request.getMeetingLink() != null) {
            appointment.setMeetingLink(request.getMeetingLink());
        }
        if (request.getMeetingPassword() != null) {
            appointment.setMeetingPassword(request.getMeetingPassword());
        }
        if (request.getAttachmentIds() != null) {
            appointment.setAttachmentIds(request.getAttachmentIds());
        }

        // Only lecturers can update notes
        if (isLecturer && request.getNotes() != null) {
            appointment.setNotes(request.getNotes());
        }

        appointment.setLastModifiedAt(LocalDateTime.now());
        appointment.setLastModifiedBy(currentUser.getId());

        Appointment updatedAppointment = appointmentRepository.save(appointment);

        return ResponseEntity.ok(
                ApiResponse.<AppointmentResponse>builder()
                        .success(true)
                        .message("Appointment updated successfully")
                        .data(mapToAppointmentResponse(updatedAppointment))
                        .build()
        );
    }

    // =================== UPDATE APPOINTMENT STATUS ===================
    @PutMapping("/{appointmentId}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateAppointmentStatus(
            @PathVariable String appointmentId,
            @Valid @RequestBody AppointmentStatusUpdateRequest request) {
        
        User currentUser = userService.getCurrentUser();
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Check permissions
        if (!appointment.getStudentId().equals(currentUser.getId()) && 
            !appointment.getLecturerId().equals(currentUser.getId())) {
            throw new SecurityException("User is not a participant in this appointment");
        }

        // Business rules for status updates
        boolean isLecturer = appointment.getLecturerId().equals(currentUser.getId());
        Appointment.AppointmentStatus newStatus = request.getStatus();

        // Validate status transitions
        switch (newStatus) {
            case CONFIRMED:
                if (!isLecturer) {
                    throw new SecurityException("Only lecturers can confirm appointments");
                }
                if (appointment.getStatus() != Appointment.AppointmentStatus.PENDING) {
                    throw new RuntimeException("Only pending appointments can be confirmed");
                }
                break;
            case CANCELLED:
                // Both can cancel, but different rules
                if (appointment.getStatus() == Appointment.AppointmentStatus.COMPLETED) {
                    throw new RuntimeException("Cannot cancel completed appointments");
                }
                break;
            case COMPLETED:
                if (!isLecturer) {
                    throw new SecurityException("Only lecturers can mark appointments as completed");
                }
                if (appointment.getStatus() != Appointment.AppointmentStatus.CONFIRMED) {
                    throw new RuntimeException("Only confirmed appointments can be marked as completed");
                }
                break;
            case NO_SHOW:
                if (!isLecturer) {
                    throw new SecurityException("Only lecturers can mark appointments as no-show");
                }
                break;
        }

        appointment.setStatus(newStatus);
        if (request.getNotes() != null && isLecturer) {
            appointment.setNotes(request.getNotes());
        }
        appointment.setLastModifiedAt(LocalDateTime.now());
        appointment.setLastModifiedBy(currentUser.getId());

        Appointment updatedAppointment = appointmentRepository.save(appointment);

        return ResponseEntity.ok(
                ApiResponse.<AppointmentResponse>builder()
                        .success(true)
                        .message("Appointment status updated successfully")
                        .data(mapToAppointmentResponse(updatedAppointment))
                        .build()
        );
    }

    // =================== DELETE APPOINTMENT ===================
    @DeleteMapping("/{appointmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteAppointment(@PathVariable String appointmentId) {
        User currentUser = userService.getCurrentUser();
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Check permissions
        if (!appointment.getStudentId().equals(currentUser.getId()) && 
            !appointment.getLecturerId().equals(currentUser.getId())) {
            throw new SecurityException("User is not a participant in this appointment");
        }

        // Business rule: Only pending appointments can be deleted
        if (appointment.getStatus() != Appointment.AppointmentStatus.PENDING) {
            throw new RuntimeException("Only pending appointments can be deleted");
        }

        appointmentRepository.delete(appointment);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Appointment deleted successfully")
                        .build()
        );
    }

    // =================== GET AVAILABLE TIME SLOTS ===================
    @GetMapping("/available-slots")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<TimeSlotResponse>>> getAvailableTimeSlots(
            @RequestParam String lecturerId,
            @RequestParam String date, // Format: YYYY-MM-DD
            @RequestParam(defaultValue = "30") Integer durationMinutes) {
        
        // Validate lecturer exists
        User lecturer = userRepository.findById(lecturerId)
                .orElseThrow(() -> new RuntimeException("Lecturer not found"));
        
        if (lecturer.getRole() != User.UserRole.LECTURER) {
            throw new RuntimeException("Specified user is not a lecturer");
        }

        LocalDate requestedDate = LocalDate.parse(date);
        LocalDateTime startOfDay = requestedDate.atStartOfDay();
        LocalDateTime endOfDay = requestedDate.atTime(23, 59, 59);

        // Get lecturer's existing appointments for the day
        List<Appointment> existingAppointments = appointmentRepository.findByUserIdAndScheduledAtBetween(
                lecturerId, startOfDay, endOfDay)
                .stream()
                .filter(apt -> apt.getStatus() == Appointment.AppointmentStatus.CONFIRMED || 
                              apt.getStatus() == Appointment.AppointmentStatus.PENDING)
                .collect(Collectors.toList());

        // Generate available time slots (this is a simplified version)
        // In a real implementation, you'd want to store lecturer's availability preferences
        List<TimeSlotResponse> availableSlots = generateAvailableTimeSlots(
                lecturer, requestedDate, existingAppointments, durationMinutes);

        return ResponseEntity.ok(
                ApiResponse.<List<TimeSlotResponse>>builder()
                        .success(true)
                        .message("Available time slots retrieved successfully")
                        .data(availableSlots)
                        .build()
        );
    }

    // =================== GET DASHBOARD STATS ===================
    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Object>> getAppointmentStats() {
        User currentUser = userService.getCurrentUser();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.toLocalDate().atTime(23, 59, 59);

        Object stats;
        if (currentUser.getRole() == User.UserRole.LECTURER) {
            long todayCount = appointmentRepository.findTodayAppointmentsByLecturerId(
                    currentUser.getId(), startOfDay, endOfDay).size();
            long upcomingCount = appointmentRepository.findUpcomingAppointmentsByLecturerId(
                    currentUser.getId(), now).size();
            long pendingCount = appointmentRepository.countByLecturerIdAndStatus(
                    currentUser.getId(), Appointment.AppointmentStatus.PENDING);

            stats = new Object() {
                public final long today = todayCount;
                public final long upcoming = upcomingCount;
                public final long pending = pendingCount;
            };
        } else {
            long todayCount = appointmentRepository.findTodayAppointmentsByStudentId(
                    currentUser.getId(), startOfDay, endOfDay).size();
            long upcomingCount = appointmentRepository.findUpcomingAppointmentsByStudentId(
                    currentUser.getId(), now).size();
            long pendingCount = appointmentRepository.countByStudentIdAndStatus(
                    currentUser.getId(), Appointment.AppointmentStatus.PENDING);

            stats = new Object() {
                public final long today = todayCount;
                public final long upcoming = upcomingCount;
                public final long pending = pendingCount;
            };
        }

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Appointment statistics retrieved successfully")
                        .data(stats)
                        .build()
        );
    }

    // =================== HELPER METHODS ===================
    private AppointmentResponse mapToAppointmentResponse(Appointment appointment) {
        User student = userRepository.findById(appointment.getStudentId()).orElse(null);
        User lecturer = userRepository.findById(appointment.getLecturerId()).orElse(null);
        
        String courseName = null;
        if (appointment.getCourseId() != null) {
            courseName = courseRepository.findById(appointment.getCourseId())
                    .map(Course::getName)
                    .orElse(null);
        }

        return AppointmentResponse.builder()
                .id(appointment.getId())
                .subject(appointment.getSubject())
                .description(appointment.getDescription())
                .scheduledAt(appointment.getScheduledAt())
                .durationMinutes(appointment.getDurationMinutes())
                .location(appointment.getLocation())
                .type(appointment.getType())
                .status(appointment.getStatus())
                .meetingLink(appointment.getMeetingLink())
                .notes(appointment.getNotes())
                .bookedAt(appointment.getBookedAt())
                .lastModifiedAt(appointment.getLastModifiedAt())
                .lastModifiedBy(appointment.getLastModifiedBy())
                .student(student != null ? UserService.mapToUserProfileResponse(student) : null)
                .lecturer(lecturer != null ? UserService.mapToUserProfileResponse(lecturer) : null)
                .courseId(appointment.getCourseId())
                .courseName(courseName)
                .isRecurring(appointment.isRecurring())
                .recurringPattern(appointment.getRecurringPattern())
                .recurringEndDate(appointment.getRecurringEndDate())
                .parentAppointmentId(appointment.getParentAppointmentId())
                .attachmentIds(appointment.getAttachmentIds())
                .endTime(appointment.getEndTime())
                .isUpcoming(appointment.isUpcoming())
                .isPast(appointment.isPast())
                .isToday(appointment.isToday())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }

    private void createRecurringInstances(Appointment parentAppointment, AppointmentRequest request) {
        // Implementation for creating recurring appointment instances
        // This would create individual appointment records for each occurrence
        // Simplified version - you'd want to make this more robust
        LocalDateTime current = parentAppointment.getScheduledAt();
        LocalDateTime endDate = request.getRecurringEndDate();
        
        while (current.isBefore(endDate)) {
            if (request.getRecurringPattern() == Appointment.RecurringPattern.WEEKLY) {
                current = current.plusWeeks(1);
            } else if (request.getRecurringPattern() == Appointment.RecurringPattern.BIWEEKLY) {
                current = current.plusWeeks(2);
            } else if (request.getRecurringPattern() == Appointment.RecurringPattern.MONTHLY) {
                current = current.plusMonths(1);
            }
            
            if (current.isBefore(endDate)) {
                Appointment recurringInstance = Appointment.builder()
                        .studentId(parentAppointment.getStudentId())
                        .lecturerId(parentAppointment.getLecturerId())
                        .subject(parentAppointment.getSubject())
                        .description(parentAppointment.getDescription())
                        .scheduledAt(current)
                        .durationMinutes(parentAppointment.getDurationMinutes())
                        .location(parentAppointment.getLocation())
                        .type(parentAppointment.getType())
                        .status(Appointment.AppointmentStatus.PENDING)
                        .courseId(parentAppointment.getCourseId())
                        .meetingLink(parentAppointment.getMeetingLink())
                        .meetingPassword(parentAppointment.getMeetingPassword())
                        .isRecurring(false) // Individual instances are not recurring
                        .parentAppointmentId(parentAppointment.getId())
                        .bookedAt(LocalDateTime.now())
                        .lastModifiedAt(LocalDateTime.now())
                        .lastModifiedBy(parentAppointment.getLastModifiedBy())
                        .build();
                
                appointmentRepository.save(recurringInstance);
            }
        }
    }

    private List<TimeSlotResponse> generateAvailableTimeSlots(User lecturer, LocalDate date, 
                                                            List<Appointment> existingAppointments, 
                                                            Integer durationMinutes) {
        // Simplified implementation - generate time slots from 9 AM to 5 PM
        // In a real application, you'd want to store lecturer's availability preferences
        List<TimeSlotResponse> slots = new java.util.ArrayList<>();
        
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(17, 0);
        
        LocalTime current = startTime;
        while (current.plusMinutes(durationMinutes).isBefore(endTime) || 
               current.plusMinutes(durationMinutes).equals(endTime)) {
            
            LocalDateTime slotStart = date.atTime(current);
            LocalDateTime slotEnd = slotStart.plusMinutes(durationMinutes);
            
            // Check if this slot conflicts with existing appointments
            boolean isAvailable = existingAppointments.stream()
                    .noneMatch(apt -> {
                        LocalDateTime aptStart = apt.getScheduledAt();
                        LocalDateTime aptEnd = apt.getEndTime();
                        return (slotStart.isBefore(aptEnd) && slotEnd.isAfter(aptStart));
                    });
            
            if (isAvailable) {
                slots.add(TimeSlotResponse.builder()
                        .id(date + "-" + current.toString())
                        .startTime(slotStart)
                        .endTime(slotEnd)
                        .durationMinutes(durationMinutes)
                        .location("Office") // Default location
                        .type(Appointment.AppointmentType.OFFICE_HOURS)
                        .isAvailable(true)
                        .isRecurring(false)
                        .lecturer(UserService.mapToUserProfileResponse(lecturer))
                        .build());
            }
            
            current = current.plusMinutes(30); // 30-minute intervals
        }
        
        return slots;
    }
}