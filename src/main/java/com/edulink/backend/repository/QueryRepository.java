// File Path: src/main/java/com/edulink/backend/repository/QueryRepository.java
package com.edulink.backend.repository;

// Keep your entity import
import com.edulink.backend.model.entity.Query;
import com.edulink.backend.model.entity.Query.QueryCategory;
import com.edulink.backend.model.entity.Query.QueryPriority;
import com.edulink.backend.model.entity.Query.QueryStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
// DO NOT import org.springframework.data.mongodb.repository.Query here
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

    // --- FIX: Using fully qualified name for the @Query annotation ---

    @org.springframework.data.mongodb.repository.Query("{" +
        "'studentId': ?0, " +
        " $and: [" +
        "   { $or: [ { ?1: null }, { 'status': ?1 } ] }," +
        "   { $or: [ { ?2: null }, { 'category': ?2 } ] }," +
        "   { $or: [ { ?3: null }, { 'priority': ?3 } ] }," +
        "   { $or: [ { ?4: null }, { 'course': ?4 } ] }" +
        " ]" +
    "}")
    List<Query> findByStudentIdWithFilters(String studentId, QueryStatus status, QueryCategory category, QueryPriority priority, String course);

    @org.springframework.data.mongodb.repository.Query("{$and: [{'studentId': ?0}, {$or: [{'title': {$regex: ?1, $options: 'i'}}, {'description': {$regex: ?1, $options: 'i'}}]}]}")
    List<Query> findByStudentIdAndTitleOrDescriptionContaining(String studentId, String searchTerm);

    @org.springframework.data.mongodb.repository.Query("{" +
        "'lecturerId': ?0, " +
        " $and: [" +
        "   { $or: [ { ?1: null }, { 'status': ?1 } ] }," +
        "   { $or: [ { ?2: null }, { 'category': ?2 } ] }," +
        "   { $or: [ { ?3: null }, { 'priority': ?3 } ] }," +
        "   { $or: [ { ?4: null }, { 'course': ?4 } ] }" +
        " ]" +
    "}")
    List<Query> findByLecturerIdWithFilters(String lecturerId, QueryStatus status, QueryCategory category, QueryPriority priority, String course);

    @org.springframework.data.mongodb.repository.Query("{$and: [{'lecturerId': ?0}, {$or: [{'title': {$regex: ?1, $options: 'i'}}, {'description': {$regex: ?1, $options: 'i'}}]}]}")
    List<Query> findByLecturerIdAndTitleOrDescriptionContaining(String lecturerId, String searchTerm);
}