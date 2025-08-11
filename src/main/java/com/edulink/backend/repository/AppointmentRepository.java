// src/main/java/com/edulink/backend/repository/AppointmentRepository.java
package com.edulink.backend.repository;

import com.edulink.backend.model.entity.Appointment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends MongoRepository<Appointment, String> {

    /**
     * Find all appointments for a specific student
     */
    List<Appointment> findByStudentIdOrderByScheduledAtDesc(String studentId);

    /**
     * Find all appointments for a specific lecturer
     */
    List<Appointment> findByLecturerIdOrderByScheduledAtDesc(String lecturerId);

    /**
     * Find appointments by student ID and status
     */
    List<Appointment> findByStudentIdAndStatusOrderByScheduledAtDesc(String studentId, Appointment.AppointmentStatus status);

    /**
     * Find appointments by lecturer ID and status
     */
    List<Appointment> findByLecturerIdAndStatusOrderByScheduledAtDesc(String lecturerId, Appointment.AppointmentStatus status);

    /**
     * Find appointments between two dates for a specific user (student or lecturer)
     */
    @Query("{ $or: [ { 'studentId': ?0 }, { 'lecturerId': ?0 } ], 'scheduledAt': { $gte: ?1, $lte: ?2 } }")
    List<Appointment> findByUserIdAndScheduledAtBetween(String userId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find upcoming appointments for a specific student
     */
    @Query("{ 'studentId': ?0, 'scheduledAt': { $gte: ?1 }, 'status': { $in: ['PENDING', 'CONFIRMED'] } }")
    List<Appointment> findUpcomingAppointmentsByStudentId(String studentId, LocalDateTime now);

    /**
     * Find upcoming appointments for a specific lecturer
     */
    @Query("{ 'lecturerId': ?0, 'scheduledAt': { $gte: ?1 }, 'status': { $in: ['PENDING', 'CONFIRMED'] } }")
    List<Appointment> findUpcomingAppointmentsByLecturerId(String lecturerId, LocalDateTime now);

    /**
     * Find today's appointments for a specific lecturer
     */
    @Query("{ 'lecturerId': ?0, 'scheduledAt': { $gte: ?1, $lt: ?2 }, 'status': { $in: ['PENDING', 'CONFIRMED'] } }")
    List<Appointment> findTodayAppointmentsByLecturerId(String lecturerId, LocalDateTime startOfDay, LocalDateTime endOfDay);

    /**
     * Find today's appointments for a specific student
     */
    @Query("{ 'studentId': ?0, 'scheduledAt': { $gte: ?1, $lt: ?2 }, 'status': { $in: ['PENDING', 'CONFIRMED'] } }")
    List<Appointment> findTodayAppointmentsByStudentId(String studentId, LocalDateTime startOfDay, LocalDateTime endOfDay);

    /**
     * Find appointments by lecturer ID and type
     */
    List<Appointment> findByLecturerIdAndTypeOrderByScheduledAtDesc(String lecturerId, Appointment.AppointmentType type);

    /**
     * Find appointments by course ID
     */
    List<Appointment> findByCourseIdOrderByScheduledAtDesc(String courseId);

    /**
     * FIXED: Find conflicting appointments for a lecturer in a time range
     * Using simple Spring Data method instead of complex query
     */
    default List<Appointment> findConflictingAppointmentsForLecturer(String lecturerId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Appointment> lecturerAppointments = findByLecturerIdOrderByScheduledAtDesc(lecturerId);
        
        return lecturerAppointments.stream()
            .filter(apt -> apt.getStatus() == Appointment.AppointmentStatus.PENDING || 
                          apt.getStatus() == Appointment.AppointmentStatus.CONFIRMED)
            .filter(apt -> {
                LocalDateTime aptStart = apt.getScheduledAt();
                LocalDateTime aptEnd = apt.getScheduledAt().plusMinutes(apt.getDurationMinutes());
                // Check if appointments overlap
                return (startTime.isBefore(aptEnd) && endTime.isAfter(aptStart));
            })
            .toList();
    }

    /**
     * FIXED: Find conflicting appointments for a student in a time range
     * Using simple Spring Data method instead of complex query
     */
    default List<Appointment> findConflictingAppointmentsForStudent(String studentId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Appointment> studentAppointments = findByStudentIdOrderByScheduledAtDesc(studentId);
        
        return studentAppointments.stream()
            .filter(apt -> apt.getStatus() == Appointment.AppointmentStatus.PENDING || 
                          apt.getStatus() == Appointment.AppointmentStatus.CONFIRMED)
            .filter(apt -> {
                LocalDateTime aptStart = apt.getScheduledAt();
                LocalDateTime aptEnd = apt.getScheduledAt().plusMinutes(apt.getDurationMinutes());
                // Check if appointments overlap
                return (startTime.isBefore(aptEnd) && endTime.isAfter(aptStart));
            })
            .toList();
    }

    /**
     * Count pending appointments for a lecturer
     */
    long countByLecturerIdAndStatus(String lecturerId, Appointment.AppointmentStatus status);

    /**
     * Count pending appointments for a student
     */
    long countByStudentIdAndStatus(String studentId, Appointment.AppointmentStatus status);

    /**
     * Find appointments by parent appointment ID (for recurring appointments)
     */
    List<Appointment> findByParentAppointmentIdOrderByScheduledAtAsc(String parentAppointmentId);

    /**
     * Find recurring appointments
     */
    List<Appointment> findByIsRecurringTrueAndParentAppointmentIdIsNull();

    /**
     * Find appointments that need to be completed (past scheduled time but still confirmed)
     */
    @Query("{ 'status': 'CONFIRMED', 'scheduledAt': { $lt: ?0 } }")
    List<Appointment> findAppointmentsPendingCompletion(LocalDateTime currentTime);

    /**
     * FIXED: Find appointments by multiple filters using simpler approach
     */
    default List<Appointment> findAppointmentsByFilters(String userId, Appointment.AppointmentStatus status, 
                                                       Appointment.AppointmentType type, String courseId) {
        // Get all appointments for user first
        List<Appointment> appointments = findByUserIdOrderByScheduledAtDesc(userId);
        
        return appointments.stream()
            .filter(apt -> status == null || apt.getStatus() == status)
            .filter(apt -> type == null || apt.getType() == type)
            .filter(apt -> courseId == null || (apt.getCourseId() != null && apt.getCourseId().equals(courseId)))
            .toList();
    }
    
    /**
     * Helper method to find all appointments for a user (student or lecturer)
     */
    @Query("{ $or: [ { 'studentId': ?0 }, { 'lecturerId': ?0 } ] }")
    List<Appointment> findByUserIdOrderByScheduledAtDesc(String userId);
}