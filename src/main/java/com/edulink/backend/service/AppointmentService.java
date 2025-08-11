// src/main/java/com/edulink/backend/service/AppointmentService.java
package com.edulink.backend.service;

import com.edulink.backend.dto.request.AppointmentRequest;
import com.edulink.backend.dto.request.AppointmentStatusUpdateRequest;
import com.edulink.backend.dto.request.AppointmentUpdateRequest;
import com.edulink.backend.dto.response.AppointmentResponse;
import com.edulink.backend.dto.response.TimeSlotResponse;
import com.edulink.backend.model.entity.Appointment;
import com.edulink.backend.model.entity.Course;
import com.edulink.backend.model.entity.User;
import com.edulink.backend.repository.AppointmentRepository;
import com.edulink.backend.repository.CourseRepository;
import com.edulink.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    // =================== CREATE APPOINTMENT ===================
    public AppointmentResponse createAppointment(AppointmentRequest request, String currentUserId) {
        log.info("Creating appointment for user: {}", currentUserId);

        // Get current user and determine roles
        User currentUser = getUserById(currentUserId);
        String studentId, lecturerId;

        if (currentUser.getRole() == User.UserRole.STUDENT) {
            studentId = currentUserId;
            lecturerId = request.getLecturerId();
            if (lecturerId == null) {
                throw new IllegalArgumentException("Lecturer ID is required when student creates appointment");
            }
        } else if (currentUser.getRole() == User.UserRole.LECTURER) {
            lecturerId = currentUserId;
            studentId = request.getStudentId();
            if (studentId == null) {
                throw new IllegalArgumentException("Student ID is required when lecturer creates appointment");
            }
        } else {
            throw new SecurityException("Only students and lecturers can create appointments");
        }

        // Validate participants
        User student = validateStudent(studentId);
        User lecturer = validateLecturer(lecturerId);

        // Validate scheduling constraints
        validateAppointmentTime(request.getScheduledAt(), request.getDurationMinutes());
        validateNoConflicts(studentId, lecturerId, request.getScheduledAt(), request.getDurationMinutes(), null);

        // Validate course if specified
        if (request.getCourseId() != null) {
            validateCourseAccess(request.getCourseId(), studentId, lecturerId);
        }

        // Create appointment
        Appointment appointment = buildAppointment(request, studentId, lecturerId, currentUserId);
        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Create recurring instances if needed
        if (request.isRecurring() && request.getRecurringEndDate() != null) {
            createRecurringInstances(savedAppointment, request, currentUserId);
        }

        log.info("Appointment created successfully with ID: {}", savedAppointment.getId());
        return mapToAppointmentResponse(savedAppointment);
    }

    // =================== GET APPOINTMENTS ===================
    public List<AppointmentResponse> getAppointmentsByUser(String userId, 
                                                          Appointment.AppointmentStatus status,
                                                          Appointment.AppointmentType type,
                                                          String courseId) {
        log.info("Getting appointments for user: {} with filters - status: {}, type: {}, courseId: {}", 
                userId, status, type, courseId);

        List<Appointment> appointments = appointmentRepository.findAppointmentsByFilters(userId, status, type, courseId);
        return appointments.stream()
                .map(this::mapToAppointmentResponse)
                .collect(Collectors.toList());
    }

    public AppointmentResponse getAppointmentById(String appointmentId, String currentUserId) {
        log.info("Getting appointment {} for user: {}", appointmentId, currentUserId);

        Appointment appointment = getAppointmentEntityById(appointmentId);
        validateParticipant(appointment, currentUserId);
        return mapToAppointmentResponse(appointment);
    }

    public List<AppointmentResponse> getUpcomingAppointments(String userId) {
        LocalDateTime now = LocalDateTime.now();
        List<Appointment> appointments;
        
        User user = getUserById(userId);
        if (user.getRole() == User.UserRole.LECTURER) {
            appointments = appointmentRepository.findUpcomingAppointmentsByLecturerId(userId, now);
        } else {
            appointments = appointmentRepository.findUpcomingAppointmentsByStudentId(userId, now);
        }

        return appointments.stream()
                .map(this::mapToAppointmentResponse)
                .collect(Collectors.toList());
    }

    public List<AppointmentResponse> getTodayAppointments(String userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.toLocalDate().atTime(23, 59, 59);
        
        List<Appointment> appointments;
        User user = getUserById(userId);
        if (user.getRole() == User.UserRole.LECTURER) {
            appointments = appointmentRepository.findTodayAppointmentsByLecturerId(userId, startOfDay, endOfDay);
        } else {
            appointments = appointmentRepository.findTodayAppointmentsByStudentId(userId, startOfDay, endOfDay);
        }

        return appointments.stream()
                .map(this::mapToAppointmentResponse)
                .collect(Collectors.toList());
    }

    // =================== UPDATE APPOINTMENT ===================
    public AppointmentResponse updateAppointment(String appointmentId, AppointmentUpdateRequest request, String currentUserId) {
        log.info("Updating appointment {} by user: {}", appointmentId, currentUserId);

        Appointment appointment = getAppointmentEntityById(appointmentId);
        validateParticipant(appointment, currentUserId);

        boolean isLecturer = appointment.getLecturerId().equals(currentUserId);

        // Update allowed fields
        if (request.getSubject() != null) {
            appointment.setSubject(request.getSubject());
        }
        
        if (request.getDescription() != null) {
            appointment.setDescription(request.getDescription());
        }

        // Handle time changes with conflict validation
        if (request.getScheduledAt() != null || request.getDurationMinutes() != null) {
            LocalDateTime newScheduledAt = request.getScheduledAt() != null ? 
                    request.getScheduledAt() : appointment.getScheduledAt();
            Integer newDuration = request.getDurationMinutes() != null ? 
                    request.getDurationMinutes() : appointment.getDurationMinutes();

            validateAppointmentTime(newScheduledAt, newDuration);
            validateNoConflicts(appointment.getStudentId(), appointment.getLecturerId(), 
                              newScheduledAt, newDuration, appointmentId);

            appointment.setScheduledAt(newScheduledAt);
            appointment.setDurationMinutes(newDuration);
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
        appointment.setLastModifiedBy(currentUserId);

        Appointment updatedAppointment = appointmentRepository.save(appointment);
        log.info("Appointment {} updated successfully", appointmentId);
        
        return mapToAppointmentResponse(updatedAppointment);
    }

    // =================== UPDATE APPOINTMENT STATUS ===================
    public AppointmentResponse updateAppointmentStatus(String appointmentId, 
                                                      AppointmentStatusUpdateRequest request, 
                                                      String currentUserId) {
        log.info("Updating appointment {} status to {} by user: {}", appointmentId, request.getStatus(), currentUserId);

        Appointment appointment = getAppointmentEntityById(appointmentId);
        validateParticipant(appointment, currentUserId);

        boolean isLecturer = appointment.getLecturerId().equals(currentUserId);
        validateStatusTransition(appointment, request.getStatus(), isLecturer);

        appointment.setStatus(request.getStatus());
        
        if (request.getNotes() != null && isLecturer) {
            appointment.setNotes(request.getNotes());
        }
        
        appointment.setLastModifiedAt(LocalDateTime.now());
        appointment.setLastModifiedBy(currentUserId);

        Appointment updatedAppointment = appointmentRepository.save(appointment);
        log.info("Appointment {} status updated to {}", appointmentId, request.getStatus());
        
        return mapToAppointmentResponse(updatedAppointment);
    }

    // =================== DELETE APPOINTMENT ===================
    public void deleteAppointment(String appointmentId, String currentUserId) {
        log.info("Deleting appointment {} by user: {}", appointmentId, currentUserId);

        Appointment appointment = getAppointmentEntityById(appointmentId);
        validateParticipant(appointment, currentUserId);

        if (appointment.getStatus() != Appointment.AppointmentStatus.PENDING) {
            throw new IllegalStateException("Only pending appointments can be deleted");
        }

        // Delete related recurring instances if this is a parent appointment
        if (appointment.isRecurring() && appointment.getParentAppointmentId() == null) {
            List<Appointment> recurringInstances = appointmentRepository.findByParentAppointmentIdOrderByScheduledAtAsc(appointmentId);
            appointmentRepository.deleteAll(recurringInstances);
            log.info("Deleted {} recurring instances for appointment {}", recurringInstances.size(), appointmentId);
        }

        appointmentRepository.delete(appointment);
        log.info("Appointment {} deleted successfully", appointmentId);
    }

    // =================== AVAILABLE TIME SLOTS ===================
    public List<TimeSlotResponse> getAvailableTimeSlots(String lecturerId, LocalDate date, Integer durationMinutes) {
        log.info("Getting available time slots for lecturer {} on date {} with duration {}", lecturerId, date, durationMinutes);

        User lecturer = validateLecturer(lecturerId);
        
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        // Get existing appointments for the day
        List<Appointment> existingAppointments = appointmentRepository.findByUserIdAndScheduledAtBetween(
                lecturerId, startOfDay, endOfDay)
                .stream()
                .filter(apt -> apt.getStatus() == Appointment.AppointmentStatus.CONFIRMED || 
                              apt.getStatus() == Appointment.AppointmentStatus.PENDING)
                .collect(Collectors.toList());

        // Generate available time slots
        List<TimeSlotResponse> availableSlots = generateAvailableTimeSlots(
                lecturer, date, existingAppointments, durationMinutes);

        log.info("Found {} available time slots for lecturer {} on {}", availableSlots.size(), lecturerId, date);
        return availableSlots;
    }

    // =================== APPOINTMENT STATISTICS ===================
    public AppointmentStatistics getAppointmentStatistics(String userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.toLocalDate().atTime(23, 59, 59);

        User user = getUserById(userId);
        
        if (user.getRole() == User.UserRole.LECTURER) {
            long todayCount = appointmentRepository.findTodayAppointmentsByLecturerId(userId, startOfDay, endOfDay).size();
            long upcomingCount = appointmentRepository.findUpcomingAppointmentsByLecturerId(userId, now).size();
            long pendingCount = appointmentRepository.countByLecturerIdAndStatus(userId, Appointment.AppointmentStatus.PENDING);
            long totalCount = appointmentRepository.findByLecturerIdOrderByScheduledAtDesc(userId).size();

            return new AppointmentStatistics(todayCount, upcomingCount, pendingCount, totalCount);
        } else {
            long todayCount = appointmentRepository.findTodayAppointmentsByStudentId(userId, startOfDay, endOfDay).size();
            long upcomingCount = appointmentRepository.findUpcomingAppointmentsByStudentId(userId, now).size();
            long pendingCount = appointmentRepository.countByStudentIdAndStatus(userId, Appointment.AppointmentStatus.PENDING);
            long totalCount = appointmentRepository.findByStudentIdOrderByScheduledAtDesc(userId).size();

            return new AppointmentStatistics(todayCount, upcomingCount, pendingCount, totalCount);
        }
    }

    // =================== RECURRING APPOINTMENTS ===================
    public void createRecurringInstances(Appointment parentAppointment, AppointmentRequest request, String currentUserId) {
        log.info("Creating recurring instances for appointment {}", parentAppointment.getId());

        LocalDateTime current = parentAppointment.getScheduledAt();
        LocalDateTime endDate = request.getRecurringEndDate();
        List<Appointment> recurringInstances = new ArrayList<>();

        while (current.isBefore(endDate)) {
            // Calculate next occurrence
            if (request.getRecurringPattern() == Appointment.RecurringPattern.WEEKLY) {
                current = current.plusWeeks(1);
            } else if (request.getRecurringPattern() == Appointment.RecurringPattern.BIWEEKLY) {
                current = current.plusWeeks(2);
            } else if (request.getRecurringPattern() == Appointment.RecurringPattern.MONTHLY) {
                current = current.plusMonths(1);
            }

            if (current.isBefore(endDate)) {
                // Check for conflicts before creating
                try {
                    validateNoConflicts(parentAppointment.getStudentId(), parentAppointment.getLecturerId(), 
                                      current, parentAppointment.getDurationMinutes(), null);

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
                            .isRecurring(false)
                            .parentAppointmentId(parentAppointment.getId())
                            .bookedAt(LocalDateTime.now())
                            .lastModifiedAt(LocalDateTime.now())
                            .lastModifiedBy(currentUserId)
                            .build();

                    recurringInstances.add(recurringInstance);
                } catch (Exception e) {
                    log.warn("Skipping recurring instance at {} due to conflict: {}", current, e.getMessage());
                }
            }
        }

        if (!recurringInstances.isEmpty()) {
            appointmentRepository.saveAll(recurringInstances);
            log.info("Created {} recurring instances for appointment {}", recurringInstances.size(), parentAppointment.getId());
        }
    }

    // =================== HELPER METHODS ===================
    private Appointment buildAppointment(AppointmentRequest request, String studentId, String lecturerId, String currentUserId) {
        return Appointment.builder()
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
                .lastModifiedBy(currentUserId)
                .build();
    }

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

    private List<TimeSlotResponse> generateAvailableTimeSlots(User lecturer, LocalDate date, 
                                                            List<Appointment> existingAppointments, 
                                                            Integer durationMinutes) {
        List<TimeSlotResponse> slots = new ArrayList<>();
        
        // Generate slots from 9 AM to 5 PM with 30-minute intervals
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
                        .location("Office") // Default location - could be configurable
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

    // =================== VALIDATION METHODS ===================
    private User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    private User validateStudent(String studentId) {
        User student = getUserById(studentId);
        if (student.getRole() != User.UserRole.STUDENT) {
            throw new IllegalArgumentException("Specified user is not a student: " + studentId);
        }
        return student;
    }

    private User validateLecturer(String lecturerId) {
        User lecturer = getUserById(lecturerId);
        if (lecturer.getRole() != User.UserRole.LECTURER) {
            throw new IllegalArgumentException("Specified user is not a lecturer: " + lecturerId);
        }
        return lecturer;
    }

    private Appointment getAppointmentEntityById(String appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found: " + appointmentId));
    }

    private void validateParticipant(Appointment appointment, String userId) {
        if (!appointment.getStudentId().equals(userId) && !appointment.getLecturerId().equals(userId)) {
            throw new SecurityException("User is not a participant in this appointment");
        }
    }

    private void validateAppointmentTime(LocalDateTime scheduledAt, Integer durationMinutes) {
        if (scheduledAt.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot schedule appointments in the past");
        }
        
        if (durationMinutes <= 0 || durationMinutes > 480) { // Max 8 hours
            throw new IllegalArgumentException("Duration must be between 1 and 480 minutes");
        }
    }

    private void validateNoConflicts(String studentId, String lecturerId, LocalDateTime scheduledAt, 
                                   Integer durationMinutes, String excludeAppointmentId) {
        LocalDateTime endTime = scheduledAt.plusMinutes(durationMinutes);
        
        // Check lecturer conflicts
        List<Appointment> lecturerConflicts = appointmentRepository.findConflictingAppointmentsForLecturer(
                lecturerId, scheduledAt, endTime);
        if (excludeAppointmentId != null) {
            lecturerConflicts = lecturerConflicts.stream()
                    .filter(apt -> !apt.getId().equals(excludeAppointmentId))
                    .collect(Collectors.toList());
        }
        if (!lecturerConflicts.isEmpty()) {
            throw new IllegalStateException("Lecturer has a conflicting appointment at this time");
        }

        // Check student conflicts
        List<Appointment> studentConflicts = appointmentRepository.findConflictingAppointmentsForStudent(
                studentId, scheduledAt, endTime);
        if (excludeAppointmentId != null) {
            studentConflicts = studentConflicts.stream()
                    .filter(apt -> !apt.getId().equals(excludeAppointmentId))
                    .collect(Collectors.toList());
        }
        if (!studentConflicts.isEmpty()) {
            throw new IllegalStateException("Student has a conflicting appointment at this time");
        }
    }

    private void validateCourseAccess(String courseId, String studentId, String lecturerId) {
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isPresent()) {
            Course course = courseOpt.get();
            // Validate that lecturer teaches this course and student is enrolled
            boolean lecturerValid = course.getLecturerId().equals(lecturerId);
            boolean studentValid = course.getEnrollment().getStudentIds().contains(studentId);
            
            if (!lecturerValid || !studentValid) {
                throw new IllegalArgumentException("Invalid course access for appointment participants");
            }
        }
    }

    private void validateStatusTransition(Appointment appointment, Appointment.AppointmentStatus newStatus, boolean isLecturer) {
        switch (newStatus) {
            case CONFIRMED:
                if (!isLecturer) {
                    throw new SecurityException("Only lecturers can confirm appointments");
                }
                if (appointment.getStatus() != Appointment.AppointmentStatus.PENDING) {
                    throw new IllegalStateException("Only pending appointments can be confirmed");
                }
                break;
            case CANCELLED:
                if (appointment.getStatus() == Appointment.AppointmentStatus.COMPLETED) {
                    throw new IllegalStateException("Cannot cancel completed appointments");
                }
                break;
            case COMPLETED:
                if (!isLecturer) {
                    throw new SecurityException("Only lecturers can mark appointments as completed");
                }
                if (appointment.getStatus() != Appointment.AppointmentStatus.CONFIRMED) {
                    throw new IllegalStateException("Only confirmed appointments can be marked as completed");
                }
                break;
            case NO_SHOW:
                if (!isLecturer) {
                    throw new SecurityException("Only lecturers can mark appointments as no-show");
                }
                break;
            case RESCHEDULED:
                // Both can reschedule, but typically handled through update, not status change
                break;
        }
    }

    // =================== STATISTICS CLASS ===================
    public static class AppointmentStatistics {
        public final long today;
        public final long upcoming;
        public final long pending;
        public final long total;

        public AppointmentStatistics(long today, long upcoming, long pending, long total) {
            this.today = today;
            this.upcoming = upcoming;
            this.pending = pending;
            this.total = total;
        }
    }
}