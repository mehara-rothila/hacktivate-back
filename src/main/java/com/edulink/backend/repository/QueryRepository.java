// File Path: src/main/java/com/edulink/backend/repository/QueryRepository.java
package com.edulink.backend.repository;

import com.edulink.backend.model.entity.Query;
import com.edulink.backend.model.entity.Query.QueryCategory;
import com.edulink.backend.model.entity.Query.QueryPriority;
import com.edulink.backend.model.entity.Query.QueryStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QueryRepository extends MongoRepository<Query, String> {

    // Student queries
    List<Query> findByStudentIdOrderBySubmittedAtDesc(String studentId);
    List<Query> findByStudentIdAndStatusOrderBySubmittedAtDesc(String studentId, QueryStatus status);
    List<Query> findByStudentIdAndSubmittedAtAfterOrderBySubmittedAtDesc(String studentId, LocalDateTime date);

    // Lecturer queries
    List<Query> findByLecturerIdOrderBySubmittedAtDesc(String lecturerId);
    List<Query> findByLecturerIdAndStatusOrderBySubmittedAtDesc(String lecturerId, QueryStatus status);
    List<Query> findByLecturerIdAndReadByLecturerFalseOrderBySubmittedAtDesc(String lecturerId);
    List<Query> findByLecturerIdAndSubmittedAtAfterOrderBySubmittedAtDesc(String lecturerId, LocalDateTime date);

    // Search methods (simplified)
    List<Query> findByStudentIdAndTitleContainingIgnoreCaseOrderBySubmittedAtDesc(String studentId, String title);
    List<Query> findByLecturerIdAndTitleContainingIgnoreCaseOrderBySubmittedAtDesc(String lecturerId, String title);

    // Count queries
    long countByStudentId(String studentId);
    long countByLecturerId(String lecturerId);
    long countByStudentIdAndStatus(String studentId, QueryStatus status);
    long countByLecturerIdAndStatus(String lecturerId, QueryStatus status);
    long countByStudentIdAndReadByStudentFalse(String studentId);
    long countByLecturerIdAndReadByLecturerFalse(String lecturerId);
    long countByLecturerIdAndPriority(String lecturerId, QueryPriority priority);

    // Additional methods for filters
    List<Query> findByStudentIdAndCategoryOrderBySubmittedAtDesc(String studentId, QueryCategory category);
    List<Query> findByStudentIdAndPriorityOrderBySubmittedAtDesc(String studentId, QueryPriority priority);
    List<Query> findByLecturerIdAndCategoryOrderBySubmittedAtDesc(String lecturerId, QueryCategory category);
    List<Query> findByLecturerIdAndPriorityOrderBySubmittedAtDesc(String lecturerId, QueryPriority priority);
}