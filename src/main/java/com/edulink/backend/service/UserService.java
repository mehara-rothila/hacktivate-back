// File Path: src/main/java/com/edulink/backend/service/UserService.java
package com.edulink.backend.service;

import com.edulink.backend.dto.request.RegisterRequest;
import com.edulink.backend.dto.response.UserProfileResponse;
import com.edulink.backend.model.entity.User;
import com.edulink.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        String passwordError = passwordService.getPasswordValidationError(request.getPassword());
        if (passwordError != null) {
            throw new RuntimeException("Invalid password: " + passwordError);
        }

        User user = new User();
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setPassword(passwordService.hashPassword(request.getPassword()));
        user.setRole(User.UserRole.valueOf(request.getRole().toUpperCase()));
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User.UserProfile profile = new User.UserProfile();
        profile.setFirstName(request.getFirstName().trim());
        profile.setLastName(request.getLastName().trim());
        profile.setDepartment(request.getDepartment().trim());

        if (user.getRole() == User.UserRole.STUDENT) {
            profile.setStudentId(request.getStudentId());
            profile.setYear(request.getYear());
            profile.setMajor(request.getMajor());
        } else if (user.getRole() == User.UserRole.LECTURER) {
            profile.setEmployeeId(request.getEmployeeId());
            profile.setOffice(request.getOffice());
            profile.setPhone(request.getPhone());
        }

        if (request.getAvatar() != null && !request.getAvatar().trim().isEmpty()) {
            profile.setAvatar(request.getAvatar().trim());
        }

        user.setProfile(profile);

        User savedUser = userRepository.save(user);
        log.info("Successfully registered user: {} with ID: {}", savedUser.getEmail(), savedUser.getId());

        return savedUser;
    }

    /**
     * Authenticate user login
     */
    public User authenticateUser(String email, String password) {
        log.info("Attempting to authenticate user: {}", email);

        Optional<User> userOptional = userRepository.findByEmail(email.toLowerCase().trim());
        if (userOptional.isEmpty()) {
            log.warn("Authentication failed: User not found with email: {}", email);
            throw new RuntimeException("Invalid email or password");
        }

        User user = userOptional.get();

        if (!user.isActive()) {
            log.warn("Authentication failed: User account is inactive: {}", email);
            throw new RuntimeException("Account is inactive. Please contact administrator.");
        }

        if (!passwordService.verifyPassword(password, user.getPassword())) {
            log.warn("Authentication failed: Invalid password for user: {}", email);
            throw new RuntimeException("Invalid email or password");
        }

        user.setLastLogin(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Successfully authenticated user: {}", email);
        return user;
    }

    public User getCurrentUser() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userEmail));
    }

    /**
     * Maps a User entity to a UserProfileResponse DTO.
     * This is a public static helper method so it can be reused by other services (like CourseService).
     *
     * @param user The User entity.
     * @return The UserProfileResponse DTO.
     */
    public static UserProfileResponse mapToUserProfileResponse(User user) {
        if (user == null || user.getProfile() == null) {
            return null;
        }
        // CORRECTED: Use the no-argument constructor and setters
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getProfile().getFirstName());
        response.setLastName(user.getProfile().getLastName());
        response.setRole(user.getRole().name());
        response.setDepartment(user.getProfile().getDepartment());
        response.setAvatar(user.getProfile().getAvatar());
        response.setStudentId(user.getProfile().getStudentId());
        response.setEmployeeId(user.getProfile().getEmployeeId());
        response.setYear(user.getProfile().getYear());
        response.setMajor(user.getProfile().getMajor());
        response.setOffice(user.getProfile().getOffice());
        response.setPhone(user.getProfile().getPhone());
        response.setActive(user.isActive());
        response.setLastLogin(user.getLastLogin());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase().trim());
    }

    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    public User getUserProfile(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    public User updateUserProfile(String userId, User.UserProfile profileUpdate) {
        log.info("Updating profile for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

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

    public void changePassword(String userId, String currentPassword, String newPassword) {
        log.info("Attempting to change password for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        if (!passwordService.verifyPassword(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        String passwordError = passwordService.getPasswordValidationError(newPassword);
        if (passwordError != null) {
            throw new RuntimeException("Invalid new password: " + passwordError);
        }

        user.setPassword(passwordService.hashPassword(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Successfully changed password for user: {}", user.getEmail());
    }

    public List<User> getAllStudents() {
        return userRepository.findByRole(User.UserRole.STUDENT);
    }

    public List<User> getAllLecturers() {
        return userRepository.findByRole(User.UserRole.LECTURER);
    }

    public List<User> getUsersByDepartment(String department) {
        return userRepository.findByDepartment(department);
    }

    public void deactivateUser(String userId) {
        log.info("Deactivating user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        user.setIsActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Successfully deactivated user: {}", user.getEmail());
    }

    public void activateUser(String userId) {
        log.info("Activating user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        user.setIsActive(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Successfully activated user: {}", user.getEmail());
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email.toLowerCase().trim());
    }

    public long getUserCountByRole(User.UserRole role) {
        return userRepository.findByRole(role).size();
    }

    public long getTotalUserCount() {
        return userRepository.count();
    }
}