// File Path: src/main/java/com/edulink/backend/service/QueryService.java
package com.edulink.backend.service;

import com.edulink.backend.dto.request.QueryRequest;
import com.edulink.backend.dto.request.QueryMessageRequest;
import com.edulink.backend.dto.request.QueryStatusUpdateRequest;
import com.edulink.backend.dto.response.QueryResponse;
import com.edulink.backend.dto.response.QueryStatsResponse;
import com.edulink.backend.model.entity.Query;
import com.edulink.backend.model.entity.Query.*;
import com.edulink.backend.model.entity.User;
import com.edulink.backend.repository.QueryRepository;
import com.edulink.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueryService {

    private final QueryRepository queryRepository;
    private final UserRepository userRepository;

    /**
     * Create a new query (student only)
     */
    @Transactional
    public QueryResponse createQuery(String studentId, QueryRequest request) {
        log.info("Creating new query for student: {}", studentId);
        
        // Validate student exists
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        if (student.getRole() != User.UserRole.STUDENT) {
            throw new RuntimeException("Only students can create queries");
        }
        
        // Find lecturer if specified, otherwise assign to course coordinator or default
        User lecturer = null;
        if (request.getLecturerId() != null && !request.getLecturerId().trim().isEmpty()) {
            lecturer = userRepository.findById(request.getLecturerId())
                    .orElseThrow(() -> new RuntimeException("Specified lecturer not found"));
        } else {
            // TODO: Implement logic to find appropriate lecturer based on course
            // For now, find any lecturer in the same department
            lecturer = findLecturerForCourse(request.getCourse(), student.getProfile().getDepartment());
        }
        
        if (lecturer == null) {
            throw new RuntimeException("No lecturer available to handle this query");
        }
        
        // Create query
        Query query = new Query(
                studentId,
                lecturer.getId(),
                request.getTitle(),
                request.getDescription(),
                request.getCategory(),
                request.getPriority(),
                request.getCourse()
        );
        
        // Add initial message from student
        String studentName = getFullName(student);
        QueryMessage initialMessage = new QueryMessage(
                studentId,
                "STUDENT",
                studentName,
                request.getDescription()
        );
        query.addMessage(initialMessage);
        
        // Save query
        Query savedQuery = queryRepository.save(query);
        
        log.info("Query created successfully with ID: {}", savedQuery.getId());
        
        return QueryResponse.fromQuery(savedQuery, student, lecturer);
    }

    /**
     * Get query by ID
     */
    public QueryResponse getQueryById(String queryId, String userId) {
        log.info("Fetching query: {} for user: {}", queryId, userId);
        
        Query query = queryRepository.findById(queryId)
                .orElseThrow(() -> new RuntimeException("Query not found"));
        
        // Check if user has permission to view this query
        if (!query.getStudentId().equals(userId) && !query.getLecturerId().equals(userId)) {
            throw new RuntimeException("Access denied to this query");
        }
        
        // Get user details
        User student = userRepository.findById(query.getStudentId()).orElse(null);
        User lecturer = userRepository.findById(query.getLecturerId()).orElse(null);
        
        // Mark as read by the requesting user
        if (query.getStudentId().equals(userId) && !query.isReadByStudent()) {
            query.setReadByStudent(true);
            queryRepository.save(query);
        } else if (query.getLecturerId().equals(userId) && !query.isReadByLecturer()) {
            query.setReadByLecturer(true);
            queryRepository.save(query);
        }
        
        return QueryResponse.fromQueryDetailed(query, student, lecturer);
    }

    /**
     * Get queries for student
     */
    public List<QueryResponse> getQueriesForStudent(String studentId, QueryFilters filters) {
        log.info("Fetching queries for student: {}", studentId);
        
        List<Query> queries;
        
        if (filters.hasFilters()) {
            queries = queryRepository.findByStudentIdWithFilters(
                    studentId,
                    filters.getStatus(),
                    filters.getCategory(),
                    filters.getPriority(),
                    filters.getCourse()
            );
        } else {
            queries = queryRepository.findByStudentIdOrderBySubmittedAtDesc(studentId);
        }
        
        // Apply search filter if present
        if (filters.getSearchTerm() != null && !filters.getSearchTerm().trim().isEmpty()) {
            queries = queryRepository.findByStudentIdAndTitleOrDescriptionContaining(
                    studentId, filters.getSearchTerm());
        }
        
        // Get users for response
        User student = userRepository.findById(studentId).orElse(null);
        
        return queries.stream()
                .map(query -> {
                    User lecturer = userRepository.findById(query.getLecturerId()).orElse(null);
                    return QueryResponse.fromQuery(query, student, lecturer);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get queries for lecturer
     */
    public List<QueryResponse> getQueriesForLecturer(String lecturerId, QueryFilters filters) {
        log.info("Fetching queries for lecturer: {}", lecturerId);
        
        List<Query> queries;
        
        if (filters.hasFilters()) {
            queries = queryRepository.findByLecturerIdWithFilters(
                    lecturerId,
                    filters.getStatus(),
                    filters.getCategory(),
                    filters.getPriority(),
                    filters.getCourse()
            );
        } else {
            queries = queryRepository.findByLecturerIdOrderBySubmittedAtDesc(lecturerId);
        }
        
        // Apply search filter if present
        if (filters.getSearchTerm() != null && !filters.getSearchTerm().trim().isEmpty()) {
            queries = queryRepository.findByLecturerIdAndTitleOrDescriptionContaining(
                    lecturerId, filters.getSearchTerm());
        }
        
        // Apply unread filter if requested
        if (filters.isUnreadOnly()) {
            queries = queries.stream()
                    .filter(query -> !query.isReadByLecturer())
                    .collect(Collectors.toList());
        }
        
        // Sort by priority and read status
        queries = queries.stream()
                .sorted((q1, q2) -> {
                    // Unread first
                    if (!q1.isReadByLecturer() && q2.isReadByLecturer()) return -1;
                    if (q1.isReadByLecturer() && !q2.isReadByLecturer()) return 1;
                    
                    // Then by priority
                    int priorityCompare = getPriorityOrder(q2.getPriority()) - getPriorityOrder(q1.getPriority());
                    if (priorityCompare != 0) return priorityCompare;
                    
                    // Finally by submission date (newest first)
                    return q2.getSubmittedAt().compareTo(q1.getSubmittedAt());
                })
                .collect(Collectors.toList());
        
        // Get lecturer info
        User lecturer = userRepository.findById(lecturerId).orElse(null);
        
        return queries.stream()
                .map(query -> {
                    User student = userRepository.findById(query.getStudentId()).orElse(null);
                    return QueryResponse.fromQuery(query, student, lecturer);
                })
                .collect(Collectors.toList());
    }

    /**
     * Add message to query
     */
    @Transactional
    public QueryResponse addMessageToQuery(String queryId, String userId, QueryMessageRequest request) {
        log.info("Adding message to query: {} by user: {}", queryId, userId);
        
        Query query = queryRepository.findById(queryId)
                .orElseThrow(() -> new RuntimeException("Query not found"));
        
        // Check if user has permission
        if (!query.getStudentId().equals(userId) && !query.getLecturerId().equals(userId)) {
            throw new RuntimeException("Access denied to this query");
        }
        
        // Check if query is closed
        if (query.getStatus() == QueryStatus.CLOSED) {
            throw new RuntimeException("Cannot add messages to a closed query");
        }
        
        // Get user info
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String senderType = user.getId().equals(query.getStudentId()) ? "STUDENT" : "LECTURER";
        String senderName = getFullName(user);
        
        // Create message
        QueryMessage message = new QueryMessage(userId, senderType, senderName, request.getContent());
        query.addMessage(message);
        
        // Update status if lecturer is responding
        if (senderType.equals("LECTURER") && query.getStatus() == QueryStatus.PENDING) {
            query.addStatusHistory(QueryStatus.IN_PROGRESS, senderName, "Lecturer responded");
        }
        
        // Reset read status for the other party
        if (senderType.equals("STUDENT")) {
            query.setReadByLecturer(false);
        } else {
            query.setReadByStudent(false);
        }
        
        // Save query
        Query savedQuery = queryRepository.save(query);
        
        // Get user details for response
        User student = userRepository.findById(query.getStudentId()).orElse(null);
        User lecturer = userRepository.findById(query.getLecturerId()).orElse(null);
        
        return QueryResponse.fromQueryDetailed(savedQuery, student, lecturer);
    }

    /**
     * Update query status (lecturer only)
     */
    @Transactional
    public QueryResponse updateQueryStatus(String queryId, String lecturerId, QueryStatusUpdateRequest request) {
        log.info("Updating query status: {} by lecturer: {}", queryId, lecturerId);
        
        Query query = queryRepository.findById(queryId)
                .orElseThrow(() -> new RuntimeException("Query not found"));
        
        // Check if lecturer has permission
        if (!query.getLecturerId().equals(lecturerId)) {
            throw new RuntimeException("Only the assigned lecturer can update query status");
        }
        
        // Get lecturer info
        User lecturer = userRepository.findById(lecturerId)
                .orElseThrow(() -> new RuntimeException("Lecturer not found"));
        
        String lecturerName = getFullName(lecturer);
        
        // Update status
        query.addStatusHistory(request.getStatus(), lecturerName, request.getNote());
        
        // Set auto-close for resolved queries
        if (request.getStatus() == QueryStatus.RESOLVED) {
            query.setAutoCloseAt(LocalDateTime.now().plusDays(7)); // Auto-close after 7 days
        }
        
        // Reset read status for student
        query.setReadByStudent(false);
        
        // Save query
        Query savedQuery = queryRepository.save(query);
        
        // Get user details for response
        User student = userRepository.findById(query.getStudentId()).orElse(null);
        
        return QueryResponse.fromQueryDetailed(savedQuery, student, lecturer);
    }

    /**
     * Get query statistics for lecturer
     */
    public QueryStatsResponse getQueryStatsForLecturer(String lecturerId) {
        log.info("Getting query statistics for lecturer: {}", lecturerId);
        
        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
        LocalDateTime monthAgo = LocalDateTime.now().minusMonths(1);
        
        return QueryStatsResponse.builder()
                .totalQueries(queryRepository.countByLecturerId(lecturerId))
                .pendingQueries(queryRepository.countByLecturerIdAndStatus(lecturerId, QueryStatus.PENDING))
                .inProgressQueries(queryRepository.countByLecturerIdAndStatus(lecturerId, QueryStatus.IN_PROGRESS))
                .resolvedQueries(queryRepository.countByLecturerIdAndStatus(lecturerId, QueryStatus.RESOLVED))
                .unreadQueries(queryRepository.countByLecturerIdAndReadByLecturerFalse(lecturerId))
                .highPriorityQueries(queryRepository.countByLecturerIdAndPriority(lecturerId, QueryPriority.HIGH))
                .queriesThisWeek(queryRepository.findByLecturerIdAndSubmittedAtAfterOrderBySubmittedAtDesc(lecturerId, weekAgo).size())
                .queriesThisMonth(queryRepository.findByLecturerIdAndSubmittedAtAfterOrderBySubmittedAtDesc(lecturerId, monthAgo).size())
                .build();
    }

    /**
     * Get query statistics for student
     */
    public QueryStatsResponse getQueryStatsForStudent(String studentId) {
        log.info("Getting query statistics for student: {}", studentId);
        
        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
        LocalDateTime monthAgo = LocalDateTime.now().minusMonths(1);
        
        return QueryStatsResponse.builder()
                .totalQueries(queryRepository.countByStudentId(studentId))
                .pendingQueries(queryRepository.countByStudentIdAndStatus(studentId, QueryStatus.PENDING))
                .inProgressQueries(queryRepository.countByStudentIdAndStatus(studentId, QueryStatus.IN_PROGRESS))
                .resolvedQueries(queryRepository.countByStudentIdAndStatus(studentId, QueryStatus.RESOLVED))
                .unreadQueries(queryRepository.countByStudentIdAndReadByStudentFalse(studentId))
                .queriesThisWeek(queryRepository.findByStudentIdAndSubmittedAtAfterOrderBySubmittedAtDesc(studentId, weekAgo).size())
                .queriesThisMonth(queryRepository.findByStudentIdAndSubmittedAtAfterOrderBySubmittedAtDesc(studentId, monthAgo).size())
                .build();
    }

    /**
     * Mark query as read
     */
    @Transactional
    public void markQueryAsRead(String queryId, String userId) {
        log.info("Marking query as read: {} by user: {}", queryId, userId);
        
        Query query = queryRepository.findById(queryId)
                .orElseThrow(() -> new RuntimeException("Query not found"));
        
        if (query.getStudentId().equals(userId)) {
            query.setReadByStudent(true);
        } else if (query.getLecturerId().equals(userId)) {
            query.setReadByLecturer(true);
        } else {
            throw new RuntimeException("Access denied to this query");
        }
        
        queryRepository.save(query);
    }

    /**
     * Mark all queries as read for lecturer
     */
    @Transactional
    public void markAllQueriesAsReadForLecturer(String lecturerId) {
        log.info("Marking all queries as read for lecturer: {}", lecturerId);
        
        List<Query> unreadQueries = queryRepository.findByLecturerIdAndReadByLecturerFalseOrderBySubmittedAtDesc(lecturerId);
        
        unreadQueries.forEach(query -> query.setReadByLecturer(true));
        
        queryRepository.saveAll(unreadQueries);
    }

    /**
     * Delete query (soft delete by changing status)
     */
    @Transactional
    public void deleteQuery(String queryId, String lecturerId) {
        log.info("Deleting query: {} by lecturer: {}", queryId, lecturerId);
        
        Query query = queryRepository.findById(queryId)
                .orElseThrow(() -> new RuntimeException("Query not found"));
        
        if (!query.getLecturerId().equals(lecturerId)) {
            throw new RuntimeException("Only the assigned lecturer can delete this query");
        }
        
        // For now, we'll actually delete. In production, consider soft delete
        queryRepository.delete(query);
    }

    // Helper methods
    private User findLecturerForCourse(String course, String department) {
        // TODO: Implement logic to find appropriate lecturer based on course and department
        // For now, return any lecturer in the department
        return userRepository.findByRoleAndProfile_Department(User.UserRole.LECTURER, department)
                .stream()
                .findFirst()
                .orElse(null);
    }

    private String getFullName(User user) {
        if (user.getProfile() != null) {
            String firstName = user.getProfile().getFirstName();
            String lastName = user.getProfile().getLastName();
            if (firstName != null && lastName != null) {
                return (firstName + " " + lastName).trim();
            }
        }
        return user.getEmail();
    }

    private int getPriorityOrder(QueryPriority priority) {
        switch (priority) {
            case HIGH: return 3;
            case MEDIUM: return 2;
            case LOW: return 1;
            default: return 0;
        }
    }

    // Filter class for query filtering
    public static class QueryFilters {
        private QueryStatus status;
        private QueryCategory category;
        private QueryPriority priority;
        private String course;
        private String searchTerm;
        private boolean unreadOnly;

        // Constructors, getters, and setters
        public QueryFilters() {}

        public QueryFilters(QueryStatus status, QueryCategory category, QueryPriority priority, 
                           String course, String searchTerm, boolean unreadOnly) {
            this.status = status;
            this.category = category;
            this.priority = priority;
            this.course = course;
            this.searchTerm = searchTerm;
            this.unreadOnly = unreadOnly;
        }

        public boolean hasFilters() {
            return status != null || category != null || priority != null || 
                   (course != null && !course.trim().isEmpty());
        }

        // Getters and setters
        public QueryStatus getStatus() { return status; }
        public void setStatus(QueryStatus status) { this.status = status; }

        public QueryCategory getCategory() { return category; }
        public void setCategory(QueryCategory category) { this.category = category; }

        public QueryPriority getPriority() { return priority; }
        public void setPriority(QueryPriority priority) { this.priority = priority; }

        public String getCourse() { return course; }
        public void setCourse(String course) { this.course = course; }

        public String getSearchTerm() { return searchTerm; }
        public void setSearchTerm(String searchTerm) { this.searchTerm = searchTerm; }

        public boolean isUnreadOnly() { return unreadOnly; }
        public void setUnreadOnly(boolean unreadOnly) { this.unreadOnly = unreadOnly; }
    }
}