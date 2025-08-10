// File Path: src/main/java/com/edulink/backend/controller/AuthController.java
package com.edulink.backend.controller;

import com.edulink.backend.dto.request.LoginRequest;
import com.edulink.backend.dto.request.RegisterRequest;
import com.edulink.backend.dto.response.ApiResponse;
import com.edulink.backend.dto.response.AuthResponse;
import com.edulink.backend.dto.response.UserProfileResponse;
import com.edulink.backend.model.entity.User;
import com.edulink.backend.service.UserService;
import com.edulink.backend.service.FileStorageService;
import com.edulink.backend.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final FileStorageService fileStorageService;

    /**
     * Register a new student
     */
    @PostMapping("/register/student")
    public ResponseEntity<ApiResponse<AuthResponse>> registerStudent(@Valid @RequestBody RegisterRequest request) {
        try {
            log.info("Student registration attempt for email: {}", request.getEmail());
            
            // Set role to STUDENT
            request.setRole("STUDENT");
            
            // Register user
            User user = userService.registerUser(request);
            
            // Create response
            AuthResponse authResponse = AuthResponse.registered(user, "Student account created successfully");
            
            log.info("Student registration successful for: {}", user.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(authResponse, "Student registered successfully"));
                    
        } catch (Exception e) {
            log.error("Student registration failed for email: {}", request.getEmail(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Registration failed", e.getMessage()));
        }
    }

    /**
     * Register a new lecturer
     */
    @PostMapping("/register/lecturer")
    public ResponseEntity<ApiResponse<AuthResponse>> registerLecturer(@Valid @RequestBody RegisterRequest request) {
        try {
            log.info("Lecturer registration attempt for email: {}", request.getEmail());
            
            // Set role to LECTURER
            request.setRole("LECTURER");
            
            // Register user
            User user = userService.registerUser(request);
            
            // Create response
            AuthResponse authResponse = AuthResponse.registered(user, "Lecturer account created successfully");
            
            log.info("Lecturer registration successful for: {}", user.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(authResponse, "Lecturer registered successfully"));
                    
        } catch (Exception e) {
            log.error("Lecturer registration failed for email: {}", request.getEmail(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Registration failed", e.getMessage()));
        }
    }

    /**
     * User login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            log.info("Login attempt for email: {}", request.getEmail());
            
            // Authenticate user
            User user = userService.authenticateUser(request.getEmail(), request.getPassword());
            
            // Generate tokens
            String accessToken = jwtUtil.generateToken(
                user.getId(), 
                user.getEmail(), 
                user.getRole().toString()
            );
            
            String refreshToken = jwtUtil.generateRefreshToken(
                user.getId(), 
                user.getEmail(), 
                user.getRole().toString()
            );
            
            // Calculate expiration time in seconds
            Long expiresIn = jwtUtil.getExpirationTime() / 1000;
            
            // Create response
            AuthResponse authResponse = AuthResponse.success(
                accessToken, 
                refreshToken, 
                expiresIn, 
                user, 
                "Login successful"
            );
            
            log.info("Login successful for: {}", user.getEmail());
            return ResponseEntity.ok(ApiResponse.success(authResponse, "Login successful"));
            
        } catch (Exception e) {
            log.error("Login failed for email: {}", request.getEmail(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Login failed", e.getMessage()));
        }
    }

    /**
     * Get current user profile
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            log.info("Profile request for user: {}", userEmail);
            
            User user = userService.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            UserProfileResponse profileResponse = UserProfileResponse.fromUser(user);
            
            return ResponseEntity.ok(ApiResponse.success(profileResponse, "Profile retrieved successfully"));
            
        } catch (Exception e) {
            log.error("Failed to get profile for user: {}", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve profile", e.getMessage()));
        }
    }

    /**
     * Update user profile
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @RequestBody User.UserProfile profileUpdate, 
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            log.info("Profile update request for user: {}", userEmail);
            
            User user = userService.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            User updatedUser = userService.updateUserProfile(user.getId(), profileUpdate);
            UserProfileResponse profileResponse = UserProfileResponse.fromUser(updatedUser);
            
            return ResponseEntity.ok(ApiResponse.success(profileResponse, "Profile updated successfully"));
            
        } catch (Exception e) {
            log.error("Failed to update profile for user: {}", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to update profile", e.getMessage()));
        }
    }

    /**
     * Upload or update profile picture
     */
    @PostMapping("/profile/picture")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadProfilePicture(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            log.info("Profile picture upload request for user: {}", userEmail);
            
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Please select a file to upload"));
            }
            
            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Only image files are allowed"));
            }
            
            // Validate file size (5MB max)
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("File size must be less than 5MB"));
            }
            
            User user = userService.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Delete old profile picture if exists
            if (user.getProfile() != null && user.getProfile().getAvatar() != null) {
                try {
                    fileStorageService.deleteFile(user.getProfile().getAvatar());
                } catch (Exception e) {
                    log.warn("Could not delete old profile picture: {}", e.getMessage());
                }
            }
            
            // Store new file
            String filename = fileStorageService.storeFile(file);
            
            // Update user profile
            User.UserProfile profile = user.getProfile();
            if (profile == null) {
                profile = new User.UserProfile();
            }
            profile.setAvatar(filename);
            
            userService.updateUserProfile(user.getId(), profile);
            
            // Create response with file URL
            Map<String, String> response = new HashMap<>();
            response.put("filename", filename);
            response.put("url", "/api/files/" + filename);
            response.put("message", "Profile picture uploaded successfully");
            
            log.info("Profile picture uploaded successfully for user: {}", userEmail);
            return ResponseEntity.ok(ApiResponse.success(response, "Profile picture uploaded successfully"));
            
        } catch (Exception e) {
            log.error("Profile picture upload failed for user: {}", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Profile picture upload failed", e.getMessage()));
        }
    }

    /**
     * Delete profile picture
     */
    @DeleteMapping("/profile/picture")
    public ResponseEntity<ApiResponse<String>> deleteProfilePicture(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            log.info("Profile picture delete request for user: {}", userEmail);
            
            User user = userService.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (user.getProfile() == null || user.getProfile().getAvatar() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("No profile picture to delete"));
            }
            
            // Delete file from storage
            try {
                fileStorageService.deleteFile(user.getProfile().getAvatar());
            } catch (Exception e) {
                log.warn("Could not delete profile picture file: {}", e.getMessage());
            }
            
            // Update user profile
            User.UserProfile profile = user.getProfile();
            profile.setAvatar(null);
            userService.updateUserProfile(user.getId(), profile);
            
            log.info("Profile picture deleted successfully for user: {}", userEmail);
            return ResponseEntity.ok(ApiResponse.success("Profile picture deleted successfully"));
            
        } catch (Exception e) {
            log.error("Profile picture delete failed for user: {}", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Profile picture delete failed", e.getMessage()));
        }
    }

    /**
     * Get profile picture
     */
    @GetMapping("/profile/picture")
    public ResponseEntity<Map<String, String>> getProfilePicture(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userService.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Map<String, String> response = new HashMap<>();
            if (user.getProfile() != null && user.getProfile().getAvatar() != null) {
                response.put("filename", user.getProfile().getAvatar());
                response.put("url", "/api/files/" + user.getProfile().getAvatar());
            } else {
                response.put("filename", null);
                response.put("url", null);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to get profile picture for user: {}", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Refresh access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Refresh token is required"));
            }
            
            log.info("Token refresh attempt");
            
            // Validate refresh token
            if (!jwtUtil.isTokenValid(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid refresh token"));
            }
            
            // Extract user information from refresh token
            String userEmail = jwtUtil.extractUsername(refreshToken);
            String userId = jwtUtil.extractUserId(refreshToken);
            String role = jwtUtil.extractRole(refreshToken);
            
            // Generate new access token
            String newAccessToken = jwtUtil.generateToken(userId, userEmail, role);
            Long expiresIn = jwtUtil.getExpirationTime() / 1000;
            
            // Get user details
            User user = userService.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Create response
            AuthResponse authResponse = AuthResponse.success(
                newAccessToken, 
                refreshToken, // Keep the same refresh token
                expiresIn, 
                user, 
                "Token refreshed successfully"
            );
            
            log.info("Token refresh successful for user: {}", userEmail);
            return ResponseEntity.ok(ApiResponse.success(authResponse, "Token refreshed successfully"));
            
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Token refresh failed", e.getMessage()));
        }
    }

    /**
     * Change password
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @RequestBody Map<String, String> request, 
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");
            
            if (currentPassword == null || newPassword == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Current password and new password are required"));
            }
            
            log.info("Password change request for user: {}", userEmail);
            
            User user = userService.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            userService.changePassword(user.getId(), currentPassword, newPassword);
            
            return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
            
        } catch (Exception e) {
            log.error("Password change failed for user: {}", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Password change failed", e.getMessage()));
        }
    }

    /**
     * Logout (client-side token invalidation)
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        try {
            // Clear security context
            SecurityContextHolder.clearContext();
            
            log.info("User logout successful");
            return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
            
        } catch (Exception e) {
            log.error("Logout failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Logout failed", e.getMessage()));
        }
    }

    /**
     * Check if email exists (for registration validation)
     */
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailExists(@RequestParam String email) {
        try {
            boolean exists = userService.emailExists(email);
            return ResponseEntity.ok(ApiResponse.success(exists, "Email check completed"));
            
        } catch (Exception e) {
            log.error("Email check failed for: {}", email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Email check failed", e.getMessage()));
        }
    }

    /**
     * Get authentication status
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAuthStatus(Authentication authentication) {
        try {
            Map<String, Object> status = new HashMap<>();
            
            if (authentication != null && authentication.isAuthenticated()) {
                String userEmail = authentication.getName();
                User user = userService.findByEmail(userEmail).orElse(null);
                
                status.put("authenticated", true);
                status.put("user", user != null ? AuthResponse.UserInfo.fromUser(user) : null);
            } else {
                status.put("authenticated", false);
                status.put("user", null);
            }
            
            status.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(ApiResponse.success(status, "Authentication status retrieved"));
            
        } catch (Exception e) {
            log.error("Failed to get auth status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve authentication status", e.getMessage()));
        }
    }
}