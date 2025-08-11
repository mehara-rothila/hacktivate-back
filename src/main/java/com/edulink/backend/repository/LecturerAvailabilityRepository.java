// src/main/java/com/edulink/backend/repository/LecturerAvailabilityRepository.java
package com.edulink.backend.repository;

import com.edulink.backend.model.entity.LecturerAvailability;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LecturerAvailabilityRepository extends MongoRepository<LecturerAvailability, String> {

    /**
     * Find all availability slots for a specific lecturer
     */
    List<LecturerAvailability> findByLecturerIdOrderByDateAscStartTimeAsc(String lecturerId);

    /**
     * Find active availability slots for a lecturer
     */
    List<LecturerAvailability> findByLecturerIdAndIsActiveTrueOrderByDateAscStartTimeAsc(String lecturerId);

    /**
     * Find availability slots for a lecturer on a specific date
     */
    @Query("{ 'lecturerId': ?0, 'isActive': true, $or: [ " +
           "{ 'isRecurring': false, 'date': ?1 }, " +
           "{ 'isRecurring': true, 'dayOfWeek': ?2, $or: [ " +
           "{ 'recurringStartDate': { $lte: ?1 }, 'recurringEndDate': { $gte: ?1 } }, " +
           "{ 'recurringStartDate': { $lte: ?1 }, 'recurringEndDate': null } ] } ] }")
    List<LecturerAvailability> findByLecturerIdAndDate(String lecturerId, LocalDate date, String dayOfWeek);

    /**
     * Find recurring availability slots for a lecturer
     */
    List<LecturerAvailability> findByLecturerIdAndIsRecurringTrueAndIsActiveTrueOrderByDayOfWeekAscStartTimeAsc(String lecturerId);

    /**
     * Find specific date availability slots for a lecturer
     */
    List<LecturerAvailability> findByLecturerIdAndIsRecurringFalseAndIsActiveTrueAndDateGreaterThanEqualOrderByDateAscStartTimeAsc(String lecturerId, LocalDate fromDate);

    /**
     * Find availability slots by type for a lecturer
     */
    List<LecturerAvailability> findByLecturerIdAndTypeAndIsActiveTrueOrderByDateAscStartTimeAsc(String lecturerId, LecturerAvailability.AvailabilityType type);

    /**
     * Find conflicting availability slots
     */
    @Query("{ 'lecturerId': ?0, 'isActive': true, $or: [ " +
           "{ 'isRecurring': false, 'date': ?1 }, " +
           "{ 'isRecurring': true, 'dayOfWeek': ?2 } ], " +
           "$or: [ " +
           "{ 'startTime': { $lt: ?4 }, 'endTime': { $gt: ?3 } }, " +
           "{ 'startTime': { $gte: ?3, $lt: ?4 } } ] }")
    List<LecturerAvailability> findConflictingSlots(String lecturerId, LocalDate date, String dayOfWeek, 
                                                   String startTimeStr, String endTimeStr);

    /**
     * Count active availability slots for a lecturer
     */
    long countByLecturerIdAndIsActiveTrue(String lecturerId);

    /**
     * Delete all availability slots for a lecturer (for cleanup)
     */
    void deleteByLecturerId(String lecturerId);

    /**
     * Find availability slots that need cleanup (past one-time slots)
     */
    @Query("{ 'isRecurring': false, 'isActive': true, 'date': { $lt: ?0 } }")
    List<LecturerAvailability> findPastOneTimeSlots(LocalDate cutoffDate);
}