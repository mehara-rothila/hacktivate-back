package com.edulink.backend.controller;

import com.edulink.backend.dto.response.ApiResponse;
import com.edulink.backend.dto.response.UserProfileResponse;
import com.edulink.backend.service.AdminService;
import com.edulink.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;

    // =================== DASHBOARD STATISTICS ===================

    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats() {
        try {
            Map<String, Object> stats = adminService.getDashboardStatistics();
            return ResponseEntity.ok(ApiResponse.success(stats, "Dashboard statistics retrieved successfully"));
        } catch (Exception e) {
            log.error("Error fetching dashboard statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch dashboard statistics", e.getMessage()));
        }
    }

    // =================== USER MANAGEMENT ===================

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> getAllUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String search) {
        try {
            List<UserProfileResponse> users = adminService.getAllUsers(role, department, search);
            return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
        } catch (Exception e) {
            log.error("Error fetching users: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch users", e.getMessage()));
        }
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserById(@PathVariable String userId) {
        try {
            UserProfileResponse user = adminService.getUserById(userId);
            return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully"));
        } catch (Exception e) {
            log.error("Error fetching user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch user", e.getMessage()));
        }
    }

    @PutMapping("/users/{userId}/activate")
    public ResponseEntity<ApiResponse<String>> activateUser(@PathVariable String userId) {
        try {
            adminService.activateUser(userId);
            return ResponseEntity.ok(ApiResponse.success("User activated successfully", "User has been activated"));
        } catch (Exception e) {
            log.error("Error activating user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to activate user", e.getMessage()));
        }
    }

    @PutMapping("/users/{userId}/deactivate")
    public ResponseEntity<ApiResponse<String>> deactivateUser(@PathVariable String userId) {
        try {
            adminService.deactivateUser(userId);
            return ResponseEntity.ok(ApiResponse.success("User deactivated successfully", "User has been deactivated"));
        } catch (Exception e) {
            log.error("Error deactivating user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to deactivate user", e.getMessage()));
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable String userId) {
        try {
            adminService.deleteUser(userId);
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully", "User has been permanently deleted"));
        } catch (Exception e) {
            log.error("Error deleting user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to delete user", e.getMessage()));
        }
    }

    @PutMapping("/users/{userId}/reset-password")
    public ResponseEntity<ApiResponse<String>> resetUserPassword(@PathVariable String userId) {
        try {
            String newPassword = adminService.resetUserPassword(userId);
            return ResponseEntity.ok(ApiResponse.success("Password reset successfully", 
                    "New temporary password: " + newPassword));
        } catch (Exception e) {
            log.error("Error resetting password for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to reset password", e.getMessage()));
        }
    }

    // =================== SYSTEM MANAGEMENT ===================

    @GetMapping("/system/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemHealth() {
        try {
            Map<String, Object> health = adminService.getSystemHealth();
            return ResponseEntity.ok(ApiResponse.success(health, "System health retrieved successfully"));
        } catch (Exception e) {
            log.error("Error fetching system health: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch system health", e.getMessage()));
        }
    }

    @GetMapping("/departments")
    public ResponseEntity<ApiResponse<List<String>>> getAllDepartments() {
        try {
            List<String> departments = userService.getAllDepartments();
            return ResponseEntity.ok(ApiResponse.success(departments, "Departments retrieved successfully"));
        } catch (Exception e) {
            log.error("Error fetching departments: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch departments", e.getMessage()));
        }
    }

    // =================== REPORTS ===================

    @GetMapping("/reports/user-activity")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserActivityReport(
            @RequestParam(required = false, defaultValue = "30") int days) {
        try {
            Map<String, Object> report = adminService.getUserActivityReport(days);
            return ResponseEntity.ok(ApiResponse.success(report, "User activity report generated successfully"));
        } catch (Exception e) {
            log.error("Error generating user activity report: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to generate user activity report", e.getMessage()));
        }
    }

    @GetMapping("/reports/registration-trends")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRegistrationTrends(
            @RequestParam(required = false, defaultValue = "90") int days) {
        try {
            Map<String, Object> trends = adminService.getRegistrationTrends(days);
            return ResponseEntity.ok(ApiResponse.success(trends, "Registration trends retrieved successfully"));
        } catch (Exception e) {
            log.error("Error fetching registration trends: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch registration trends", e.getMessage()));
        }
    }
}
