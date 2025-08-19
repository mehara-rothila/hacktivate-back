package com.edulink.backend.service;

import com.edulink.backend.dto.response.UserProfileResponse;
import com.edulink.backend.model.entity.User;
import com.edulink.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordService passwordService;

    // =================== DASHBOARD STATISTICS ===================

    public Map<String, Object> getDashboardStatistics() {
        log.info("Generating dashboard statistics");

        Map<String, Object> stats = new HashMap<>();

        // User counts by role
        long totalUsers = userRepository.count();
        long totalStudents = userRepository.countByRole(User.UserRole.STUDENT);
        long totalLecturers = userRepository.countByRole(User.UserRole.LECTURER);
        long totalAdmins = userRepository.countByRole(User.UserRole.ADMIN);

        // Active user counts
        long activeUsers = userRepository.findByIsActiveTrue().size();
        long activeStudents = userRepository.countByRoleAndIsActiveTrue(User.UserRole.STUDENT);
        long activeLecturers = userRepository.countByRoleAndIsActiveTrue(User.UserRole.LECTURER);

        stats.put("totalUsers", totalUsers);
        stats.put("totalStudents", totalStudents);
        stats.put("totalLecturers", totalLecturers);
        stats.put("totalAdmins", totalAdmins);
        stats.put("activeUsers", activeUsers);
        stats.put("activeStudents", activeStudents);
        stats.put("activeLecturers", activeLecturers);

        // Recent registrations (last 7 days)
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<User> recentUsers = userRepository.findAll().stream()
                .filter(user -> user.getCreatedAt().isAfter(weekAgo))
                .collect(Collectors.toList());
        
        stats.put("recentRegistrations", recentUsers.size());

        // Department statistics
        Map<String, Long> departmentStats = userRepository.findAll().stream()
                .filter(user -> user.getProfile() != null && user.getProfile().getDepartment() != null)
                .collect(Collectors.groupingBy(
                        user -> user.getProfile().getDepartment(),
                        Collectors.counting()
                ));
        
        stats.put("departmentStats", departmentStats);

        // Users with recent activity (last login within 30 days)
        LocalDateTime monthAgo = LocalDateTime.now().minusDays(30);
        long recentlyActiveUsers = userRepository.findAll().stream()
                .filter(user -> user.getLastLogin() != null && user.getLastLogin().isAfter(monthAgo))
                .count();
        
        stats.put("recentlyActiveUsers", recentlyActiveUsers);

        log.info("Dashboard statistics generated successfully");
        return stats;
    }

    // =================== USER MANAGEMENT ===================

    public List<UserProfileResponse> getAllUsers(String role, String department, String search) {
        log.info("Fetching users with filters - role: {}, department: {}, search: {}", role, department, search);

        List<User> users = userRepository.findAll();

        // Filter by role if specified
        if (role != null && !role.trim().isEmpty()) {
            try {
                User.UserRole userRole = User.UserRole.valueOf(role.toUpperCase());
                users = users.stream()
                        .filter(user -> user.getRole() == userRole)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid role filter: {}", role);
            }
        }

        // Filter by department if specified
        if (department != null && !department.trim().isEmpty()) {
            users = users.stream()
                    .filter(user -> user.getProfile() != null && 
                            user.getProfile().getDepartment() != null &&
                            user.getProfile().getDepartment().equalsIgnoreCase(department))
                    .collect(Collectors.toList());
        }

        // Filter by search query if specified
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase().trim();
            users = users.stream()
                    .filter(user -> {
                        if (user.getEmail().toLowerCase().contains(searchLower)) {
                            return true;
                        }
                        if (user.getProfile() != null) {
                            String firstName = user.getProfile().getFirstName();
                            String lastName = user.getProfile().getLastName();
                            String userDepartment = user.getProfile().getDepartment();
                            
                            return (firstName != null && firstName.toLowerCase().contains(searchLower)) ||
                                   (lastName != null && lastName.toLowerCase().contains(searchLower)) ||
                                   (userDepartment != null && userDepartment.toLowerCase().contains(searchLower));
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
        }

        // Convert to response DTOs
        List<UserProfileResponse> responses = users.stream()
                .map(UserService::mapToUserProfileResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.info("Retrieved {} users with applied filters", responses.size());
        return responses;
    }

    public UserProfileResponse getUserById(String userId) {
        log.info("Fetching user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        UserProfileResponse response = UserService.mapToUserProfileResponse(user);
        if (response == null) {
            throw new RuntimeException("Failed to map user data for ID: " + userId);
        }

        log.info("User retrieved successfully: {}", user.getEmail());
        return response;
    }

    public void activateUser(String userId) {
        log.info("Activating user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        if (user.isActive()) {
            throw new RuntimeException("User is already active");
        }

        user.setIsActive(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("User activated successfully: {}", user.getEmail());
    }

    public void deactivateUser(String userId) {
        log.info("Deactivating user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        if (!user.isActive()) {
            throw new RuntimeException("User is already inactive");
        }

        // Prevent deactivating the last admin
        if (user.getRole() == User.UserRole.ADMIN) {
            long activeAdminCount = userRepository.countByRoleAndIsActiveTrue(User.UserRole.ADMIN);
            if (activeAdminCount <= 1) {
                throw new RuntimeException("Cannot deactivate the last active admin user");
            }
        }

        user.setIsActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("User deactivated successfully: {}", user.getEmail());
    }

    public void deleteUser(String userId) {
        log.info("Deleting user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Prevent deleting the last admin
        if (user.getRole() == User.UserRole.ADMIN) {
            long adminCount = userRepository.countByRole(User.UserRole.ADMIN);
            if (adminCount <= 1) {
                throw new RuntimeException("Cannot delete the last admin user");
            }
        }

        userRepository.deleteById(userId);
        log.info("User deleted successfully: {}", user.getEmail());
    }

    public String resetUserPassword(String userId) {
        log.info("Resetting password for user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Generate a temporary password
        String tempPassword = generateTemporaryPassword();
        String hashedPassword = passwordService.hashPassword(tempPassword);

        user.setPassword(hashedPassword);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Password reset successfully for user: {}", user.getEmail());
        return tempPassword;
    }

    // =================== SYSTEM MANAGEMENT ===================

    public Map<String, Object> getSystemHealth() {
        log.info("Checking system health");

        Map<String, Object> health = new HashMap<>();

        try {
            // Database connectivity test
            long userCount = userRepository.count();
            health.put("database", "healthy");
            health.put("totalUsersInDb", userCount);
        } catch (Exception e) {
            health.put("database", "unhealthy");
            health.put("databaseError", e.getMessage());
        }

        // Memory usage
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        Map<String, Object> memoryInfo = new HashMap<>();
        memoryInfo.put("maxMemoryMB", maxMemory / (1024 * 1024));
        memoryInfo.put("totalMemoryMB", totalMemory / (1024 * 1024));
        memoryInfo.put("usedMemoryMB", usedMemory / (1024 * 1024));
        memoryInfo.put("freeMemoryMB", freeMemory / (1024 * 1024));
        memoryInfo.put("memoryUsagePercent", (usedMemory * 100) / maxMemory);

        health.put("memory", memoryInfo);
        health.put("status", "healthy");
        health.put("timestamp", LocalDateTime.now());

        log.info("System health check completed");
        return health;
    }

    // =================== REPORTS ===================

    public Map<String, Object> getUserActivityReport(int days) {
        log.info("Generating user activity report for last {} days", days);

        LocalDateTime since = LocalDateTime.now().minusDays(days);
        Map<String, Object> report = new HashMap<>();

        // Users with recent activity
        List<User> recentlyActiveUsers = userRepository.findAll().stream()
                .filter(user -> user.getLastLogin() != null && user.getLastLogin().isAfter(since))
                .collect(Collectors.toList());

        // Activity by role
        Map<String, Long> activityByRole = recentlyActiveUsers.stream()
                .collect(Collectors.groupingBy(
                        user -> user.getRole().name(),
                        Collectors.counting()
                ));

        // Daily activity breakdown
        Map<String, Long> dailyActivity = recentlyActiveUsers.stream()
                .collect(Collectors.groupingBy(
                        user -> user.getLastLogin().toLocalDate().toString(),
                        Collectors.counting()
                ));

        report.put("totalActiveUsers", recentlyActiveUsers.size());
        report.put("activityByRole", activityByRole);
        report.put("dailyActivity", dailyActivity);
        report.put("reportPeriodDays", days);
        report.put("generatedAt", LocalDateTime.now());

        log.info("User activity report generated for {} active users", recentlyActiveUsers.size());
        return report;
    }

    public Map<String, Object> getRegistrationTrends(int days) {
        log.info("Generating registration trends for last {} days", days);

        LocalDateTime since = LocalDateTime.now().minusDays(days);
        Map<String, Object> trends = new HashMap<>();

        // Recent registrations
        List<User> recentRegistrations = userRepository.findAll().stream()
                .filter(user -> user.getCreatedAt().isAfter(since))
                .collect(Collectors.toList());

        // Registrations by role
        Map<String, Long> registrationsByRole = recentRegistrations.stream()
                .collect(Collectors.groupingBy(
                        user -> user.getRole().name(),
                        Collectors.counting()
                ));

        // Daily registration breakdown
        Map<String, Long> dailyRegistrations = recentRegistrations.stream()
                .collect(Collectors.groupingBy(
                        user -> user.getCreatedAt().toLocalDate().toString(),
                        Collectors.counting()
                ));

        // Department breakdown
        Map<String, Long> registrationsByDepartment = recentRegistrations.stream()
                .filter(user -> user.getProfile() != null && user.getProfile().getDepartment() != null)
                .collect(Collectors.groupingBy(
                        user -> user.getProfile().getDepartment(),
                        Collectors.counting()
                ));

        trends.put("totalRegistrations", recentRegistrations.size());
        trends.put("registrationsByRole", registrationsByRole);
        trends.put("dailyRegistrations", dailyRegistrations);
        trends.put("registrationsByDepartment", registrationsByDepartment);
        trends.put("reportPeriodDays", days);
        trends.put("generatedAt", LocalDateTime.now());

        log.info("Registration trends generated for {} new registrations", recentRegistrations.size());
        return trends;
    }

    // =================== UTILITY METHODS ===================

    private String generateTemporaryPassword() {
        // Generate a random 8-character temporary password
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }
}
