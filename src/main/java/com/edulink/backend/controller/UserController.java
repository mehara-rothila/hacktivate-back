// src/main/java/com/edulink/backend/controller/UserController.java
package com.edulink.backend.controller;

import com.edulink.backend.dto.response.ApiResponse;
import com.edulink.backend.dto.response.UserProfileResponse;
import com.edulink.backend.dto.response.LecturerResponse;
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

    // =================== GET ALL LECTURERS ===================
    @GetMapping("/lecturers")
    public ResponseEntity<ApiResponse<List<LecturerResponse>>> getAllLecturers() {
        try {
            log.info("Getting all lecturers");
            
            List<User> lecturers = userService.getAllLecturers();
            List<LecturerResponse> lecturerResponses = lecturers.stream()
                .filter(User::isActive) // Only active lecturers
                .map(this::mapToLecturerResponse)
                .collect(Collectors.toList());

            log.info("Found {} active lecturers", lecturerResponses.size());

            return ResponseEntity.ok(
                ApiResponse.<List<LecturerResponse>>builder()
                    .success(true)
                    .message("Lecturers retrieved successfully")
                    .data(lecturerResponses)
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        } catch (Exception e) {
            log.error("Error getting lecturers", e);
            return ResponseEntity.badRequest().body(
                ApiResponse.<List<LecturerResponse>>builder()
                    .success(false)
                    .message("Failed to get lecturers: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        }
    }

    // =================== GET ALL STUDENTS ===================
    @GetMapping("/students")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> getAllStudents() {
        try {
            log.info("Getting all students");
            
            List<User> students = userService.getAllStudents();
            List<UserProfileResponse> studentResponses = students.stream()
                .filter(User::isActive) // Only active students
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
            log.error("Error getting students", e);
            return ResponseEntity.badRequest().body(
                ApiResponse.<List<UserProfileResponse>>builder()
                    .success(false)
                    .message("Failed to get students: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        }
    }

    // =================== GET LECTURER BY ID ===================
    @GetMapping("/lecturers/{lecturerId}")
    public ResponseEntity<ApiResponse<LecturerResponse>> getLecturerById(@PathVariable String lecturerId) {
        try {
            log.info("Getting lecturer by ID: {}", lecturerId);
            
            User lecturer = userService.findById(lecturerId)
                .orElseThrow(() -> new RuntimeException("Lecturer not found: " + lecturerId));

            if (lecturer.getRole() != User.UserRole.LECTURER) {
                throw new RuntimeException("User is not a lecturer: " + lecturerId);
            }

            if (!lecturer.isActive()) {
                throw new RuntimeException("Lecturer is not active: " + lecturerId);
            }

            LecturerResponse lecturerResponse = mapToLecturerResponse(lecturer);

            return ResponseEntity.ok(
                ApiResponse.<LecturerResponse>builder()
                    .success(true)
                    .message("Lecturer retrieved successfully")
                    .data(lecturerResponse)
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        } catch (Exception e) {
            log.error("Error getting lecturer {}", lecturerId, e);
            return ResponseEntity.badRequest().body(
                ApiResponse.<LecturerResponse>builder()
                    .success(false)
                    .message("Failed to get lecturer: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        }
    }

    // =================== GET USERS BY DEPARTMENT ===================
    @GetMapping("/department/{department}")
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> getUsersByDepartment(@PathVariable String department) {
        try {
            log.info("Getting users by department: {}", department);
            
            List<User> users = userService.getUsersByDepartment(department);
            List<UserProfileResponse> userResponses = users.stream()
                .filter(User::isActive) // Only active users
                .map(UserService::mapToUserProfileResponse)
                .collect(Collectors.toList());

            log.info("Found {} active users in department {}", userResponses.size(), department);

            return ResponseEntity.ok(
                ApiResponse.<List<UserProfileResponse>>builder()
                    .success(true)
                    .message("Users retrieved successfully")
                    .data(userResponses)
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        } catch (Exception e) {
            log.error("Error getting users by department {}", department, e);
            return ResponseEntity.badRequest().body(
                ApiResponse.<List<UserProfileResponse>>builder()
                    .success(false)
                    .message("Failed to get users: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        }
    }

    // =================== SEARCH LECTURERS ===================
    @GetMapping("/lecturers/search")
    public ResponseEntity<ApiResponse<List<LecturerResponse>>> searchLecturers(@RequestParam(required = false) String department) {
        try {
            log.info("Searching lecturers with department filter: {}", department);
            
            List<User> lecturers = userService.getAllLecturers();
            List<LecturerResponse> lecturerResponses = lecturers.stream()
                .filter(User::isActive) // Only active lecturers
                .filter(lecturer -> department == null || 
                       (lecturer.getProfile() != null && 
                        lecturer.getProfile().getDepartment() != null &&
                        lecturer.getProfile().getDepartment().equalsIgnoreCase(department)))
                .map(this::mapToLecturerResponse)
                .collect(Collectors.toList());

            log.info("Found {} lecturers matching search criteria", lecturerResponses.size());

            return ResponseEntity.ok(
                ApiResponse.<List<LecturerResponse>>builder()
                    .success(true)
                    .message("Lecturers search completed successfully")
                    .data(lecturerResponses)
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        } catch (Exception e) {
            log.error("Error searching lecturers", e);
            return ResponseEntity.badRequest().body(
                ApiResponse.<List<LecturerResponse>>builder()
                    .success(false)
                    .message("Failed to search lecturers: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        }
    }

    // =================== HELPER METHODS ===================
    private LecturerResponse mapToLecturerResponse(User lecturer) {
        if (lecturer == null || lecturer.getProfile() == null) {
            return null;
        }

        User.UserProfile profile = lecturer.getProfile();

        // Combine first and last name for the "name" field
        String fullName = (profile.getFirstName() != null ? profile.getFirstName() : "") + 
                         " " + 
                         (profile.getLastName() != null ? profile.getLastName() : "");
        fullName = fullName.trim();

        return LecturerResponse.builder()
            .id(lecturer.getId())
            .name(fullName)
            .title(profile.getTitle())
            .email(lecturer.getEmail())
            .phone(profile.getPhone())
            .avatar(profile.getAvatar())
            .department(profile.getDepartment())
            .specialization(List.of()) // TODO: Add specialization field to User entity
            .courses(List.of()) // TODO: Load actual courses from CourseService
            .officeLocation(profile.getOffice() != null ? profile.getOffice() : profile.getOfficeAddress())
            .officeHours(profile.getOfficeHours() != null ? 
                        List.of(profile.getOfficeHours().split(",")) : 
                        List.of())
            .biography(profile.getBio())
            .researchInterests(List.of()) // TODO: Add research interests to User entity
            .publications(0) // TODO: Add publications count to User entity
            .yearsExperience(profile.getExperience() != null ? 
                           parseYearsExperience(profile.getExperience()) : 0)
            .rating(4.5) // TODO: Calculate actual rating from reviews
            .responseTime("Usually responds within 24 hours") // TODO: Calculate from actual data
            .availability("Available") // TODO: Check actual availability
            .lastActive(lecturer.getLastLogin() != null ? 
                       lecturer.getLastLogin().toString() : "Never")
            .preferredContactMethod("Email") // TODO: Add to User entity
            .build();
    }

    private Integer parseYearsExperience(String experience) {
        try {
            // Try to extract number from experience string like "5 years" or "5"
            String numberStr = experience.replaceAll("[^0-9]", "");
            return numberStr.isEmpty() ? 0 : Integer.parseInt(numberStr);
        } catch (Exception e) {
            return 0;
        }
    }
}