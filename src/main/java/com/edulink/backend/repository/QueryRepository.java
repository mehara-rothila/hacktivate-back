// File Path: src/main/java/com/edulink/backend/repository/QueryRepository.java
package com.edulink.backend.repository;

import com.edulink.backend.model.entity.Query;
import com.edulink.backend.model.entity.Query.QueryStatus;
import com.edulink.backend.model.entity.Query.QueryCategory;
import com.edulink.backend.model.entity.Query.QueryPriority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query as MongoQuery;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QueryRepository extends MongoRepository<Query, String> {

    // Find queries by student
    List<Query> findByStudentIdOrderBySubmittedAtDesc(String studentId);
    
    Page<Query> findByStudentIdOrderBySubmittedAtDesc(String studentId, Pageable pageable);
    
    // Find queries by lecturer
    List<Query> findByLecturerIdOrderBySubmittedAtDesc(String lecturerId);
    
    Page<Query> findByLecturerIdOrderBySubmittedAtDesc(String lecturerId, Pageable pageable);
    
    // Find queries by status
    List<Query> findByStatusOrderBySubmittedAtDesc(QueryStatus status);
    
    // Find queries by lecturer and status
    List<Query> findByLecturerIdAndStatusOrderBySubmittedAtDesc(String lecturerId, QueryStatus status);
    
    Page<Query> findByLecturerIdAndStatusOrderBySubmittedAtDesc(String lecturerId, QueryStatus status, Pageable pageable);
    
    // Find queries by student and status
    List<Query> findByStudentIdAndStatusOrderBySubmittedAtDesc(String studentId, QueryStatus status);
    
    Page<Query> findByStudentIdAndStatusOrderBySubmittedAtDesc(String studentId, QueryStatus status, Pageable pageable);
    
    // Find queries by course
    List<Query> findByCourseOrderBySubmittedAtDesc(String course);
    
    Page<Query> findByCourseOrderBySubmittedAtDesc(String course, Pageable pageable);
    
    // Find queries by lecturer and course
    List<Query> findByLecturerIdAndCourseOrderBySubmittedAtDesc(String lecturerId, String course);
    
    // Find queries by category
    List<Query> findByCategoryOrderBySubmittedAtDesc(QueryCategory category);
    
    List<Query> findByLecturerIdAndCategoryOrderBySubmittedAtDesc(String lecturerId, QueryCategory category);
    
    // Find queries by priority
    List<Query> findByPriorityOrderBySubmittedAtDesc(QueryPriority priority);
    
    List<Query> findByLecturerIdAndPriorityOrderBySubmittedAtDesc(String lecturerId, QueryPriority priority);
    
    // Find unread queries
    List<Query> findByLecturerIdAndReadByLecturerFalseOrderBySubmittedAtDesc(String lecturerId);
    
    List<Query> findByStudentIdAndReadByStudentFalseOrderBySubmittedAtDesc(String studentId);
    
    // Count queries
    long countByStudentId(String studentId);
    
    long countByLecturerId(String lecturerId);
    
    long countByLecturerIdAndStatus(String lecturerId, QueryStatus status);
    
    long countByStudentIdAndStatus(String studentId, QueryStatus status);
    
    long countByLecturerIdAndReadByLecturerFalse(String lecturerId);
    
    long countByStudentIdAndReadByStudentFalse(String studentId);
    
    long countByLecturerIdAndPriority(String lecturerId, QueryPriority priority);
    
    // Find queries that need auto-closing
    List<Query> findByAutoCloseAtBefore(LocalDateTime dateTime);
    
    // Custom search queries
    @MongoQuery("{ 'lecturerId': ?0, '$or': [ " +
               "{ 'title': { $regex: ?1, $options: 'i' } }, " +
               "{ 'description': { $regex: ?1, $options: 'i' } } ] }")
    List<Query> findByLecturerIdAndTitleOrDescriptionContaining(String lecturerId, String searchTerm);
    
    @MongoQuery("{ 'studentId': ?0, '$or': [ " +
               "{ 'title': { $regex: ?1, $options: 'i' } }, " +
               "{ 'description': { $regex: ?1, $options: 'i' } } ] }")
    List<Query> findByStudentIdAndTitleOrDescriptionContaining(String studentId, String searchTerm);
    
    // Complex filtering with multiple criteria
    @MongoQuery("{ 'lecturerId': ?0, " +
               "$and: [ " +
               "{ $or: [ { 'status': { $exists: false } }, { 'status': ?1 } ] }, " +
               "{ $or: [ { 'category': { $exists: false } }, { 'category': ?2 } ] }, " +
               "{ $or: [ { 'priority': { $exists: false } }, { 'priority': ?3 } ] }, " +
               "{ $or: [ { 'course': { $exists: false } }, { 'course': ?4 } ] } ] }")
    List<Query> findByLecturerIdWithFilters(String lecturerId, QueryStatus status, 
                                           QueryCategory category, QueryPriority priority, String course);
    
    @MongoQuery("{ 'studentId': ?0, " +
               "$and: [ " +
               "{ $or: [ { 'status': { $exists: false } }, { 'status': ?1 } ] }, " +
               "{ $or: [ { 'category': { $exists: false } }, { 'category': ?2 } ] }, " +
               "{ $or: [ { 'priority': { $exists: false } }, { 'priority': ?3 } ] }, " +
               "{ $or: [ { 'course': { $exists: false } }, { 'course': ?4 } ] } ] }")
    List<Query> findByStudentIdWithFilters(String studentId, QueryStatus status, 
                                          QueryCategory category, QueryPriority priority, String course);
    
    // Recent queries (last 30 days)
    List<Query> findByLecturerIdAndSubmittedAtAfterOrderBySubmittedAtDesc(String lecturerId, LocalDateTime since);
    
    List<Query> findByStudentIdAndSubmittedAtAfterOrderBySubmittedAtDesc(String studentId, LocalDateTime since);
    
    // High priority pending queries for dashboard
    List<Query> findByLecturerIdAndStatusAndPriorityOrderBySubmittedAtDesc(String lecturerId, QueryStatus status, QueryPriority priority);
    
    // Queries modified in the last X hours (for notifications)
    List<Query> findByLecturerIdAndLastUpdatedAfterOrderByLastUpdatedDesc(String lecturerId, LocalDateTime since);
    
    List<Query> findByStudentIdAndLastUpdatedAfterOrderByLastUpdatedDesc(String studentId, LocalDateTime since);
    
    // Get all queries for a specific course that a lecturer teaches
    List<Query> findByLecturerIdAndCourseAndStatusOrderBySubmittedAtDesc(String lecturerId, String course, QueryStatus status);
    
    // Delete queries older than a certain date (for cleanup)
    void deleteBySubmittedAtBefore(LocalDateTime dateTime);
}