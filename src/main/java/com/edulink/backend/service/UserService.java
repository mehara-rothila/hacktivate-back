// src/main/java/com/edulink/backend/service/UserService.java
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
import java.util.stream.Collectors;

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
            if (request.getProgram() != null) {
                profile.setProgram(request.getProgram());
            }
            if (request.getMinor() != null) {
                profile.setMinor(request.getMinor());
            }
            // Set default values for students
            profile.setEnrollmentStatus("full-time");
            profile.setAcademicStanding("good");
        } else if (user.getRole() == User.UserRole.LECTURER) {
            profile.setEmployeeId(request.getEmployeeId());
            profile.setOffice(request.getOffice());
            profile.setPhone(request.getPhone());
            if (request.getTitle() != null) {
                profile.setTitle(request.getTitle());
            }
            if (request.getPosition() != null) {
                profile.setPosition(request.getPosition());
            }
            // Set default values for lecturers
            profile.setEmploymentType("full-time");
            profile.setStatus("active");
        }

        if (request.getAvatar() != null && !request.getAvatar().trim().isEmpty()) {
            profile.setAvatar(request.getAvatar().trim());
        }

        profile.setUpdatedAt(LocalDateTime.now());
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
        
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getProfile().getFirstName());
        response.setLastName(user.getProfile().getLastName());
        response.setRole(user.getRole().name());
        response.setDepartment(user.getProfile().getDepartment());
        response.setAvatar(user.getProfile().getAvatar());
        response.setPhone(user.getProfile().getPhone());
        response.setBio(user.getProfile().getBio());
        response.setDateOfBirth(user.getProfile().getDateOfBirth());
        response.setGender(user.getProfile().getGender());
        response.setAddress(user.getProfile().getAddress());
        response.setCity(user.getProfile().getCity());
        response.setCountry(user.getProfile().getCountry());
        response.setPostalCode(user.getProfile().getPostalCode());
        response.setEmergencyContact(user.getProfile().getEmergencyContact());
        response.setEmergencyPhone(user.getProfile().getEmergencyPhone());
        response.setLinkedIn(user.getProfile().getLinkedIn());
        response.setGithub(user.getProfile().getGithub());
        response.setPortfolio(user.getProfile().getPortfolio());
        response.setWebsite(user.getProfile().getWebsite());
        response.setResearchGate(user.getProfile().getResearchGate());
        response.setOrcid(user.getProfile().getOrcid());
        
        // Student-specific fields
        response.setStudentId(user.getProfile().getStudentId());
        response.setYear(user.getProfile().getYear());
        response.setMajor(user.getProfile().getMajor());
        response.setMinor(user.getProfile().getMinor());
        response.setProgram(user.getProfile().getProgram());
        response.setGpa(user.getProfile().getGpa());
        response.setExpectedGraduation(user.getProfile().getExpectedGraduation());
        response.setEnrollmentStatus(user.getProfile().getEnrollmentStatus());
        response.setAcademicStanding(user.getProfile().getAcademicStanding());
        
        // Lecturer-specific fields
        response.setEmployeeId(user.getProfile().getEmployeeId());
        response.setOffice(user.getProfile().getOffice());
        response.setTitle(user.getProfile().getTitle());
        response.setPosition(user.getProfile().getPosition());
        response.setQualification(user.getProfile().getQualification());
        response.setExperience(user.getProfile().getExperience());
        response.setEmploymentType(user.getProfile().getEmploymentType());
        response.setStatus(user.getProfile().getStatus());
        response.setOfficeAddress(user.getProfile().getOfficeAddress());
        response.setOfficeHours(user.getProfile().getOfficeHours());
        response.setCampus(user.getProfile().getCampus());
        response.setBuilding(user.getProfile().getBuilding());
        response.setRoom(user.getProfile().getRoom());
        
        response.setActive(user.isActive());
        response.setLastLogin(user.getLastLogin());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        
        return response;
    }

    // ===================
    // EXISTING METHODS
    // ===================
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

        // Update basic profile fields
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
        if (profileUpdate.getAvatar() != null) {
            currentProfile.setAvatar(profileUpdate.getAvatar().trim());
        }
        if (profileUpdate.getBio() != null) {
            currentProfile.setBio(profileUpdate.getBio().trim());
        }

        // Student-specific updates
        if (user.getRole() == User.UserRole.STUDENT) {
            if (profileUpdate.getYear() != null) {
                currentProfile.setYear(profileUpdate.getYear());
            }
            if (profileUpdate.getMajor() != null) {
                currentProfile.setMajor(profileUpdate.getMajor().trim());
            }
            if (profileUpdate.getMinor() != null) {
                currentProfile.setMinor(profileUpdate.getMinor().trim());
            }
            if (profileUpdate.getProgram() != null) {
                currentProfile.setProgram(profileUpdate.getProgram().trim());
            }
        }

        // Lecturer-specific updates
        if (user.getRole() == User.UserRole.LECTURER) {
            if (profileUpdate.getOffice() != null) {
                currentProfile.setOffice(profileUpdate.getOffice().trim());
            }
            if (profileUpdate.getTitle() != null) {
                currentProfile.setTitle(profileUpdate.getTitle().trim());
            }
            if (profileUpdate.getPosition() != null) {
                currentProfile.setPosition(profileUpdate.getPosition().trim());
            }
            if (profileUpdate.getQualification() != null) {
                currentProfile.setQualification(profileUpdate.getQualification().trim());
            }
            if (profileUpdate.getExperience() != null) {
                currentProfile.setExperience(profileUpdate.getExperience().trim());
            }
            if (profileUpdate.getOfficeHours() != null) {
                currentProfile.setOfficeHours(profileUpdate.getOfficeHours().trim());
            }
        }

        currentProfile.setUpdatedAt(LocalDateTime.now());
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

    // ===================
    // NEW METHODS FOR USER DIRECTORY
    // ===================
    
    /**
     * Get all students
     */
    public List<User> getAllStudents() {
        log.info("Getting all students");
        return userRepository.findByRole(User.UserRole.STUDENT);
    }

    /**
     * Get all lecturers
     */
    public List<User> getAllLecturers() {
        log.info("Getting all lecturers");
        return userRepository.findByRole(User.UserRole.LECTURER);
    }

    /**
     * Get all users
     */
    public List<User> findAllUsers() {
        log.info("Getting all users");
        return userRepository.findAll();
    }

    /**
     * Find users by role
     */
    public List<User> findUsersByRole(User.UserRole role) {
        log.info("Finding users by role: {}", role);
        return userRepository.findByRole(role);
    }

    /**
     * Find user by ID (returns null if not found)
     */
    public User findUserById(String userId) {
        log.info("Finding user by ID: {}", userId);
        return userRepository.findById(userId).orElse(null);
    }

    /**
     * Get users by department
     */
    public List<User> getUsersByDepartment(String department) {
        log.info("Getting users by department: {}", department);
        return userRepository.findByDepartment(department);
    }

    /**
     * Find users by department and role
     */
    public List<User> findUsersByDepartmentAndRole(String department, User.UserRole role) {
        log.info("Finding users by department: {} and role: {}", department, role);
        return userRepository.findByRoleAndProfile_Department(role, department);
    }

    /**
     * Search users by query string (name, email, department)
     */
    public List<User> searchUsers(String query) {
        log.info("Searching users with query: {}", query);
        
        if (query == null || query.trim().isEmpty()) {
            return userRepository.findAll();
        }
        
        String lowercaseQuery = query.toLowerCase().trim();
        
        return userRepository.findAll().stream()
                .filter(user -> 
                    (user.getProfile() != null && (
                        (user.getProfile().getFirstName() != null && user.getProfile().getFirstName().toLowerCase().contains(lowercaseQuery)) ||
                        (user.getProfile().getLastName() != null && user.getProfile().getLastName().toLowerCase().contains(lowercaseQuery)) ||
                        (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowercaseQuery)) ||
                        (user.getProfile().getDepartment() != null && user.getProfile().getDepartment().toLowerCase().contains(lowercaseQuery))
                    ))
                )
                .collect(Collectors.toList());
    }

    /**
     * Search users by query string and role
     */
    public List<User> searchUsersByRole(String query, User.UserRole role) {
        log.info("Searching users with query: {} and role: {}", query, role);
        return searchUsers(query).stream()
                .filter(user -> user.getRole() == role)
                .collect(Collectors.toList());
    }

    /**
     * Get all unique departments
     */
    public List<String> getAllDepartments() {
        log.info("Getting all departments");
        return userRepository.findAll().stream()
                .filter(user -> user.getProfile() != null)
                .map(user -> user.getProfile().getDepartment())
                .filter(department -> department != null && !department.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Search lecturers by department
     */
    public List<User> searchLecturersByDepartment(String department) {
        log.info("Searching lecturers by department: {}", department);
        
        if (department == null || department.trim().isEmpty()) {
            return getAllLecturers();
        }
        
        return userRepository.findByRoleAndProfile_Department(User.UserRole.LECTURER, department);
    }

    // ===================
    // UTILITY METHODS
    // ===================
    
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
        return userRepository.countByRole(role);
    }

    public long getTotalUserCount() {
        return userRepository.count();
    }
}