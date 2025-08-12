// src/main/java/com/edulink/backend/controller/UserController.java
package com.edulink.backend.controller;

import com.edulink.backend.dto.response.ApiResponse;
import com.edulink.backend.dto.response.UserProfileResponse;
import com.edulink.backend.model.entity.User;
import com.edulink.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * Get all lecturers (for student lecturer directory)
     */
    @GetMapping("/lecturers")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> getAllLecturers() {
        try {
            log.info("Getting all lecturers");
            
            List<User> lecturers = userService.getAllLecturers();
            
            List<UserProfileResponse> lecturerResponses = lecturers.stream()
                    .filter(User::isActive) // Only return active lecturers
                    .map(UserService::mapToUserProfileResponse)
                    .collect(Collectors.toList());
            
            log.info("Found {} active lecturers", lecturerResponses.size());
            
            return ResponseEntity.ok(
                ApiResponse.<List<UserProfileResponse>>builder()
                    .success(true)
                    .message("Lecturers retrieved successfully")
                    .data(lecturerResponses)
                    .timestamp(LocalDateTime.now())
                    .build()
            );
            
        } catch (Exception e) {
            log.error("Failed to get lecturers", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.<List<UserProfileResponse>>builder()
                        .success(false)
                        .message("Failed to retrieve lecturers")
                        .error(e.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
        }
    }

    /**
     * Get all students (for lecturer student directory)
     */
    @GetMapping("/students")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> getAllStudents() {
        try {
            log.info("Getting all students");
            
            List<User> students = userService.getAllStudents();
            
            List<UserProfileResponse> studentResponses = students.stream()
                    .filter(User::isActive) // Only return active students
                    .map(UserService::mapToUserProfileResponse)
                    .collect(Collectors.toList());
            
            log.info("Found {} active students", studentResponses.size());
            
            return ResponseEntity.ok(
                ApiResponse.<List<UserProfileResponse>>builder()
                    .success(true)
                    .message("Students retrieved successfully")
                    .data(studentResponses)
                    .timestamp(LocalDateTime.now())
                    .build()
            );
            
        } catch (Exception e) {
            log.error("Failed to get students", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.<List<UserProfileResponse>>builder()
                        .success(false)
                        .message("Failed to retrieve students")
                        .error(e.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
        }
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserById(@PathVariable String userId) {
        try {
            log.info("Getting user by ID: {}", userId);
            
            User user = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
            
            UserProfileResponse userResponse = UserService.mapToUserProfileResponse(user);
            
            return ResponseEntity.ok(
                ApiResponse.<UserProfileResponse>builder()
                    .success(true)
                    .message("User retrieved successfully")
                    .data(userResponse)
                    .timestamp(LocalDateTime.now())
                    .build()
            );
            
        } catch (Exception e) {
            log.error("Failed to get user by ID: {}", userId, e);
            return ResponseEntity.status(404)
                    .body(ApiResponse.<UserProfileResponse>builder()
                        .success(false)
                        .message("User not found")
                        .error(e.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
        }
    }

    /**
     * Get users by department
     */
    @GetMapping("/department")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> getUsersByDepartment(
            @RequestParam(name = "department") String department,
            @RequestParam(name = "role", required = false) String role) {
        try {
            log.info("Getting users by department: '{}', role: {}", department, role);
            
            List<User> users;
            
            if (role != null && !role.trim().isEmpty()) {
                try {
                    User.UserRole userRole = User.UserRole.valueOf(role.toUpperCase());
                    users = userService.findUsersByDepartmentAndRole(department, userRole);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.status(400)
                            .body(ApiResponse.<List<UserProfileResponse>>builder()
                                .success(false)
                                .message("Invalid role specified: " + role)
                                .timestamp(LocalDateTime.now())
                                .build());
                }
            } else {
                users = userService.getUsersByDepartment(department);
            }
            
            List<UserProfileResponse> userResponses = users.stream()
                    .filter(User::isActive) // Only return active users
                    .map(UserService::mapToUserProfileResponse)
                    .collect(Collectors.toList());
            
            log.info("Found {} users in department '{}'", userResponses.size(), department);
            
            return ResponseEntity.ok(
                ApiResponse.<List<UserProfileResponse>>builder()
                    .success(true)
                    .message("Users retrieved successfully")
                    .data(userResponses)
                    .timestamp(LocalDateTime.now())
                    .build()
            );
            
        } catch (Exception e) {
            log.error("Failed to get users by department: {}", department, e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.<List<UserProfileResponse>>builder()
                        .success(false)
                        .message("Failed to retrieve users")
                        .error(e.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
        }
    }

    /**
     * Search lecturers with optional department filter
     */
    @GetMapping("/lecturers/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> searchLecturers(
            @RequestParam(name = "department", required = false) String department) {
        try {
            log.info("Searching lecturers with department filter: {}", department);
            
            List<User> lecturers;
            
            if (department != null && !department.trim().isEmpty()) {
                lecturers = userService.findUsersByDepartmentAndRole(department, User.UserRole.LECTURER);
            } else {
                lecturers = userService.getAllLecturers();
            }
            
            List<UserProfileResponse> lecturerResponses = lecturers.stream()
                    .filter(User::isActive) // Only return active lecturers
                    .map(UserService::mapToUserProfileResponse)
                    .collect(Collectors.toList());
            
            log.info("Found {} lecturers matching search criteria", lecturerResponses.size());
            
            return ResponseEntity.ok(
                ApiResponse.<List<UserProfileResponse>>builder()
                    .success(true)
                    .message("Lecturers search completed successfully")
                    .data(lecturerResponses)
                    .timestamp(LocalDateTime.now())
                    .build()
            );
            
        } catch (Exception e) {
            log.error("Failed to search lecturers", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.<List<UserProfileResponse>>builder()
                        .success(false)
                        .message("Failed to search lecturers")
                        .error(e.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
        }
    }

    /**
     * Get all departments
     */
    @GetMapping("/departments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<String>>> getAllDepartments() {
        try {
            log.info("Getting all departments");
            
            List<String> departments = userService.getAllDepartments();
            
            log.info("Found {} departments", departments.size());
            
            return ResponseEntity.ok(
                ApiResponse.<List<String>>builder()
                    .success(true)
                    .message("Departments retrieved successfully")
                    .data(departments)
                    .timestamp(LocalDateTime.now())
                    .build()
            );
            
        } catch (Exception e) {
            log.error("Failed to get departments", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.<List<String>>builder()
                        .success(false)
                        .message("Failed to retrieve departments")
                        .error(e.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
        }
    }

    /**
     * Search all users by query string
     */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> searchUsers(
            @RequestParam(name = "q") String query,
            @RequestParam(name = "role", required = false) String role) {
        try {
            log.info("Searching users with query: '{}', role: {}", query, role);
            
            List<User> users;
            
            if (role != null && !role.trim().isEmpty()) {
                try {
                    User.UserRole userRole = User.UserRole.valueOf(role.toUpperCase());
                    users = userService.searchUsersByRole(query, userRole);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.status(400)
                            .body(ApiResponse.<List<UserProfileResponse>>builder()
                                .success(false)
                                .message("Invalid role specified: " + role)
                                .timestamp(LocalDateTime.now())
                                .build());
                }
            } else {
                users = userService.searchUsers(query);
            }
            
            List<UserProfileResponse> userResponses = users.stream()
                    .filter(User::isActive) // Only return active users
                    .map(UserService::mapToUserProfileResponse)
                    .collect(Collectors.toList());
            
            log.info("Found {} users matching search criteria", userResponses.size());
            
            return ResponseEntity.ok(
                ApiResponse.<List<UserProfileResponse>>builder()
                    .success(true)
                    .message("Search completed successfully")
                    .data(userResponses)
                    .timestamp(LocalDateTime.now())
                    .build()
            );
            
        } catch (Exception e) {
            log.error("Failed to search users", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.<List<UserProfileResponse>>builder()
                        .success(false)
                        .message("Search failed")
                        .error(e.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
        }
    }
}