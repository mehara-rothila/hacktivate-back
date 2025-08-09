// File Path: src/main/java/com/edulink/backend/controller/QueryController.java
package com.edulink.backend.controller;

import com.edulink.backend.dto.request.QueryRequest;
import com.edulink.backend.dto.request.QueryMessageRequest;
import com.edulink.backend.dto.request.QueryStatusUpdateRequest;
import com.edulink.backend.dto.response.ApiResponse;
import com.edulink.backend.dto.response.QueryResponse;
import com.edulink.backend.dto.response.QueryStatsResponse;
import com.edulink.backend.model.entity.Query.*;
import com.edulink.backend.service.QueryService;
import com.edulink.backend.service.QueryService.QueryFilters;
import com.edulink.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/queries")
@RequiredArgsConstructor
@Slf4j
public class QueryController {

    private final QueryService queryService;
    private final UserService userService;

    /**
     * Create a new query (students only)
     * POST /api/queries
     */
    @PostMapping
    public ResponseEntity<ApiResponse<QueryResponse>> createQuery(
            @Valid @RequestBody QueryRequest request,
            Authentication authentication) {
        try {
            String studentId = getUserIdFromAuthentication(authentication);
            log.info("Creating query for student: {}", studentId);
            
            QueryResponse response = queryService.createQuery(studentId, request);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response, "Query created successfully"));
                    
        } catch (Exception e) {
            log.error("Failed to create query for user: {}", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to create query", e.getMessage()));
        }
    }

    /**
     * Get all queries for current user (with filtering)
     * GET /api/queries
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<QueryResponse>>> getQueries(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "priority", required = false) String priority,
            @RequestParam(value = "course", required = false) String course,
            @RequestParam(value = "search", required = false) String searchTerm,
            @RequestParam(value = "unreadOnly", required = false, defaultValue = "false") boolean unreadOnly,
            Authentication authentication) {
        try {
            String userId = getUserIdFromAuthentication(authentication);
            String userRole = getUserRoleFromAuthentication(authentication);
            
            log.info("Fetching queries for user: {} with role: {}", userId, userRole);
            
            // Create filters
            QueryFilters filters = new QueryFilters(
                    parseQueryStatus(status),
                    parseQueryCategory(category),
                    parseQueryPriority(priority),
                    course,
                    searchTerm,
                    unreadOnly
            );
            
            List<QueryResponse> queries;
            
            if ("LECTURER".equals(userRole)) {
                queries = queryService.getQueriesForLecturer(userId, filters);
            } else if ("STUDENT".equals(userRole)) {
                queries = queryService.getQueriesForStudent(userId, filters);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("Access denied", "Invalid user role"));
            }
            
            return ResponseEntity.ok(ApiResponse.success(queries, "Queries retrieved successfully"));
            
        } catch (Exception e) {
            log.error("Failed to get queries for user: {}", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve queries", e.getMessage()));
        }
    }

    /**
     * Get specific query by ID
     * GET /api/queries/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QueryResponse>> getQueryById(
            @PathVariable String id,
            Authentication authentication) {
        try {
            String userId = getUserIdFromAuthentication(authentication);
            log.info("Fetching query: {} for user: {}", id, userId);
            
            QueryResponse query = queryService.getQueryById(id, userId);
            
            return ResponseEntity.ok(ApiResponse.success(query, "Query retrieved successfully"));
            
        } catch (Exception e) {
            log.error("Failed to get query: {} for user: {}", id, authentication.getName(), e);
            
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Query not found", e.getMessage()));
            } else if (e.getMessage().contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("Access denied", e.getMessage()));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to retrieve query", e.getMessage()));
            }
        }
    }

    /**
     * Add message to query
     * POST /api/queries/{id}/messages
     */
    @PostMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<QueryResponse>> addMessageToQuery(
            @PathVariable String id,
            @Valid @RequestBody QueryMessageRequest request,
            Authentication authentication) {
        try {
            String userId = getUserIdFromAuthentication(authentication);
            log.info("Adding message to query: {} by user: {}", id, userId);
            
            QueryResponse response = queryService.addMessageToQuery(id, userId, request);
            
            return ResponseEntity.ok(ApiResponse.success(response, "Message added successfully"));
            
        } catch (Exception e) {
            log.error("Failed to add message to query: {} for user: {}", id, authentication.getName(), e);
            
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Query not found", e.getMessage()));
            } else if (e.getMessage().contains("Access denied") || e.getMessage().contains("closed")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("Operation not allowed", e.getMessage()));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Failed to add message", e.getMessage()));
            }
        }
    }

    /**
     * Update query status (lecturers only)
     * PUT /api/queries/{id}/status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<QueryResponse>> updateQueryStatus(
            @PathVariable String id,
            @Valid @RequestBody QueryStatusUpdateRequest request,
            Authentication authentication) {
        try {
            String lecturerId = getUserIdFromAuthentication(authentication);
            String userRole = getUserRoleFromAuthentication(authentication);
            
            if (!"LECTURER".equals(userRole)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("Access denied", "Only lecturers can update query status"));
            }
            
            log.info("Updating status for query: {} by lecturer: {}", id, lecturerId);
            
            QueryResponse response = queryService.updateQueryStatus(id, lecturerId, request);
            
            return ResponseEntity.ok(ApiResponse.success(response, "Query status updated successfully"));
            
        } catch (Exception e) {
            log.error("Failed to update query status: {} for lecturer: {}", id, authentication.getName(), e);
            
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Query not found", e.getMessage()));
            } else if (e.getMessage().contains("Only the assigned lecturer")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("Access denied", e.getMessage()));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Failed to update status", e.getMessage()));
            }
        }
    }

    /**
     * Mark query as read
     * PUT /api/queries/{id}/mark-read
     */
    @PutMapping("/{id}/mark-read")
    public ResponseEntity<ApiResponse<String>> markQueryAsRead(
            @PathVariable String id,
            Authentication authentication) {
        try {
            String userId = getUserIdFromAuthentication(authentication);
            log.info("Marking query as read: {} by user: {}", id, userId);
            
            queryService.markQueryAsRead(id, userId);
            
            return ResponseEntity.ok(ApiResponse.success("Query marked as read"));
            
        } catch (Exception e) {
            log.error("Failed to mark query as read: {} for user: {}", id, authentication.getName(), e);
            
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Query not found", e.getMessage()));
            } else if (e.getMessage().contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("Access denied", e.getMessage()));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to mark as read", e.getMessage()));
            }
        }
    }

    /**
     * Mark all queries as read (lecturers only)
     * PUT /api/queries/mark-all-read
     */
    @PutMapping("/mark-all-read")
    public ResponseEntity<ApiResponse<String>> markAllQueriesAsRead(Authentication authentication) {
        try {
            String lecturerId = getUserIdFromAuthentication(authentication);
            String userRole = getUserRoleFromAuthentication(authentication);
            
            if (!"LECTURER".equals(userRole)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("Access denied", "Only lecturers can mark all queries as read"));
            }
            
            log.info("Marking all queries as read for lecturer: {}", lecturerId);
            
            queryService.markAllQueriesAsReadForLecturer(lecturerId);
            
            return ResponseEntity.ok(ApiResponse.success("All queries marked as read"));
            
        } catch (Exception e) {
            log.error("Failed to mark all queries as read for lecturer: {}", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to mark all as read", e.getMessage()));
        }
    }

    /**
     * Get query statistics
     * GET /api/queries/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<QueryStatsResponse>> getQueryStats(Authentication authentication) {
        try {
            String userId = getUserIdFromAuthentication(authentication);
            String userRole = getUserRoleFromAuthentication(authentication);
            
            log.info("Getting query statistics for user: {} with role: {}", userId, userRole);
            
            QueryStatsResponse stats;
            
            if ("LECTURER".equals(userRole)) {
                stats = queryService.getQueryStatsForLecturer(userId);
            } else if ("STUDENT".equals(userRole)) {
                stats = queryService.getQueryStatsForStudent(userId);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("Access denied", "Invalid user role"));
            }
            
            return ResponseEntity.ok(ApiResponse.success(stats, "Statistics retrieved successfully"));
            
        } catch (Exception e) {
            log.error("Failed to get query statistics for user: {}", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve statistics", e.getMessage()));
        }
    }

    /**
     * Delete query (lecturers only)
     * DELETE /api/queries/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteQuery(
            @PathVariable String id,
            Authentication authentication) {
        try {
            String lecturerId = getUserIdFromAuthentication(authentication);
            String userRole = getUserRoleFromAuthentication(authentication);
            
            if (!"LECTURER".equals(userRole)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("Access denied", "Only lecturers can delete queries"));
            }
            
            log.info("Deleting query: {} by lecturer: {}", id, lecturerId);
            
            queryService.deleteQuery(id, lecturerId);
            
            return ResponseEntity.ok(ApiResponse.success("Query deleted successfully"));
            
        } catch (Exception e) {
            log.error("Failed to delete query: {} for lecturer: {}", id, authentication.getName(), e);
            
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Query not found", e.getMessage()));
            } else if (e.getMessage().contains("Only the assigned lecturer")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("Access denied", e.getMessage()));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to delete query", e.getMessage()));
            }
        }
    }

    // Helper methods
    private String getUserIdFromAuthentication(Authentication authentication) {
        // authentication.getName() returns email, but we need userId
        // We need to look up the user by email to get the ID
        try {
            String userEmail = authentication.getName();
            return userService.findByEmail(userEmail)
                    .map(user -> user.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract user ID from authentication", e);
        }
    }

    private String getUserRoleFromAuthentication(Authentication authentication) {
        // Extract role from authentication authorities
        String authority = authentication.getAuthorities().iterator().next().getAuthority();
        // Remove ROLE_ prefix if present
        return authority.startsWith("ROLE_") ? authority.substring(5) : authority;
    }

    private QueryStatus parseQueryStatus(String status) {
        if (status == null || status.trim().isEmpty() || "All".equalsIgnoreCase(status)) {
            return null;
        }
        try {
            // Convert from frontend format (e.g., "In Progress") to enum format
            String enumValue = status.toUpperCase().replace(" ", "_").replace("-", "_");
            return QueryStatus.valueOf(enumValue);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid query status: {}", status);
            return null;
        }
    }

    private QueryCategory parseQueryCategory(String category) {
        if (category == null || category.trim().isEmpty() || "All".equalsIgnoreCase(category)) {
            return null;
        }
        try {
            String enumValue = category.toUpperCase().replace(" ", "_").replace("-", "_");
            return QueryCategory.valueOf(enumValue);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid query category: {}", category);
            return null;
        }
    }

    private QueryPriority parseQueryPriority(String priority) {
        if (priority == null || priority.trim().isEmpty() || "All".equalsIgnoreCase(priority)) {
            return null;
        }
        try {
            return QueryPriority.valueOf(priority.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid query priority: {}", priority);
            return null;
        }
    }
}