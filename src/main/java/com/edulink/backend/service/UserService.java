// File Path: src/main/java/com/edulink/backend/service/UserService.java
package com.edulink.backend.service;

import com.edulink.backend.dto.request.RegisterRequest;
import com.edulink.backend.model.entity.User;
import com.edulink.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    /**
     * Register a new user (student or lecturer)
     */
    public User registerUser(RegisterRequest request) {
        log.info("Attempting to register user with email: {}", request.getEmail());
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        // Validate password
        String passwordError = passwordService.getPasswordValidationError(request.getPassword());
        if (passwordError != null) {
            throw new RuntimeException("Invalid password: " + passwordError);
        }

        // Create new user
        User user = new User();
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setPassword(passwordService.hashPassword(request.getPassword()));
        user.setRole(User.UserRole.valueOf(request.getRole().toUpperCase()));
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // Create user profile
        User.UserProfile profile = new User.UserProfile();
        profile.setFirstName(request.getFirstName().trim());
        profile.setLastName(request.getLastName().trim());
        profile.setDepartment(request.getDepartment().trim());

        // Set role-specific fields
        if (user.getRole() == User.UserRole.STUDENT) {
            profile.setStudentId(request.getStudentId());
            profile.setYear(request.getYear());
            profile.setMajor(request.getMajor());
        } else if (user.getRole() == User.UserRole.LECTURER) {
            profile.setEmployeeId(request.getEmployeeId());
            profile.setOffice(request.getOffice());
            profile.setPhone(request.getPhone());
        }

        // Set optional common fields
        if (request.getAvatar() != null && !request.getAvatar().trim().isEmpty()) {
            profile.setAvatar(request.getAvatar().trim());
        }

        user.setProfile(profile);

        // Save user
        User savedUser = userRepository.save(user);
        log.info("Successfully registered user: {} with ID: {}", savedUser.getEmail(), savedUser.getId());

        return savedUser;
    }

    /**
     * Authenticate user login
     */
    public User authenticateUser(String email, String password) {
        log.info("Attempting to authenticate user: {}", email);
        
        // Find user by email
        Optional<User> userOptional = userRepository.findByEmail(email.toLowerCase().trim());
        if (userOptional.isEmpty()) {
            log.warn("Authentication failed: User not found with email: {}", email);
            throw new RuntimeException("Invalid email or password");
        }

        User user = userOptional.get();

        // Check if user is active
        if (!user.isActive()) {
            log.warn("Authentication failed: User account is inactive: {}", email);
            throw new RuntimeException("Account is inactive. Please contact administrator.");
        }

        // Verify password
        if (!passwordService.verifyPassword(password, user.getPassword())) {
            log.warn("Authentication failed: Invalid password for user: {}", email);
            throw new RuntimeException("Invalid email or password");
        }

        // Update last login
        user.setLastLogin(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Successfully authenticated user: {}", email);
        return user;
    }

    /**
     * Find user by email
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase().trim());
    }

    /**
     * Find user by ID
     */
    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    /**
     * Get user profile by ID
     */
    public User getUserProfile(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    /**
     * Update user profile
     */
    public User updateUserProfile(String userId, User.UserProfile profileUpdate) {
        log.info("Updating profile for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Update profile fields if provided
        User.UserProfile currentProfile = user.getProfile();
        if (currentProfile == null) {
            currentProfile = new User.UserProfile();
        }

        if (profileUpdate.getFirstName() != null) {
            currentProfile.setFirstName(profileUpdate.getFirstName().trim());
        }
        if (profileUpdate.getLastName() != null) {
            currentProfile.setLastName(profileUpdate.getLastName().trim());
        }
        if (profileUpdate.getDepartment() != null) {
            currentProfile.setDepartment(profileUpdate.getDepartment().trim());
        }
        if (profileUpdate.getPhone() != null) {
            currentProfile.setPhone(profileUpdate.getPhone().trim());
        }
        if (profileUpdate.getOffice() != null) {
            currentProfile.setOffice(profileUpdate.getOffice().trim());
        }
        if (profileUpdate.getAvatar() != null) {
            currentProfile.setAvatar(profileUpdate.getAvatar().trim());
        }

        // Role-specific updates
        if (user.getRole() == User.UserRole.STUDENT) {
            if (profileUpdate.getYear() != null) {
                currentProfile.setYear(profileUpdate.getYear());
            }
            if (profileUpdate.getMajor() != null) {
                currentProfile.setMajor(profileUpdate.getMajor().trim());
            }
        }

        user.setProfile(currentProfile);
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        log.info("Successfully updated profile for user: {}", user.getEmail());

        return updatedUser;
    }

    /**
     * Change user password
     */
    public void changePassword(String userId, String currentPassword, String newPassword) {
        log.info("Attempting to change password for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Verify current password
        if (!passwordService.verifyPassword(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Validate new password
        String passwordError = passwordService.getPasswordValidationError(newPassword);
        if (passwordError != null) {
            throw new RuntimeException("Invalid new password: " + passwordError);
        }

        // Update password
        user.setPassword(passwordService.hashPassword(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Successfully changed password for user: {}", user.getEmail());
    }

    /**
     * Get all students
     */
    public List<User> getAllStudents() {
        return userRepository.findByRole(User.UserRole.STUDENT);
    }

    /**
     * Get all lecturers
     */
    public List<User> getAllLecturers() {
        return userRepository.findByRole(User.UserRole.LECTURER);
    }

    /**
     * Get users by department
     */
    public List<User> getUsersByDepartment(String department) {
        return userRepository.findByDepartment(department);
    }

    /**
     * Deactivate user account
     */
    public void deactivateUser(String userId) {
        log.info("Deactivating user with ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        user.setIsActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Successfully deactivated user: {}", user.getEmail());
    }

    /**
     * Activate user account
     */
    public void activateUser(String userId) {
        log.info("Activating user with ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        user.setIsActive(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Successfully activated user: {}", user.getEmail());
    }

    /**
     * Check if email exists
     */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email.toLowerCase().trim());
    }

    /**
     * Get user count by role
     */
    public long getUserCountByRole(User.UserRole role) {
        return userRepository.findByRole(role).size();
    }

    /**
     * Get total user count
     */
    public long getTotalUserCount() {
        return userRepository.count();
    }
}