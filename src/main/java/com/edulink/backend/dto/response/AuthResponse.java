// File Path: src/main/java/com/edulink/backend/dto/response/AuthResponse.java
package com.edulink.backend.dto.response;

import com.edulink.backend.model.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn; // Token expiration time in seconds
    
    private UserInfo user;
    private String message;
    private LocalDateTime timestamp;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private String id;
        private String email;
        private String firstName;
        private String lastName;
        private String role;
        private String department;
        private String avatar;
        
        // Role-specific fields
        private String studentId;
        private String employeeId;
        private String year;
        private String major;
        private String office;
        private String phone;
        
        private boolean isActive;
        private LocalDateTime lastLogin;
        private LocalDateTime createdAt;

        // Create UserInfo from User entity
        public static UserInfo fromUser(User user) {
            UserInfo.UserInfoBuilder builder = UserInfo.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .role(user.getRole().toString())
                    .isActive(user.isActive())
                    .lastLogin(user.getLastLogin())
                    .createdAt(user.getCreatedAt());

            if (user.getProfile() != null) {
                User.UserProfile profile = user.getProfile();
                builder
                    .firstName(profile.getFirstName())
                    .lastName(profile.getLastName())
                    .department(profile.getDepartment())
                    .avatar(profile.getAvatar())
                    .studentId(profile.getStudentId())
                    .employeeId(profile.getEmployeeId())
                    .year(profile.getYear())
                    .major(profile.getMajor())
                    .office(profile.getOffice())
                    .phone(profile.getPhone());
            }

            return builder.build();
        }
    }

    // Factory method for successful login
    public static AuthResponse success(String accessToken, String refreshToken, 
                                     Long expiresIn, User user, String message) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .user(UserInfo.fromUser(user))
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Factory method for successful registration
    public static AuthResponse registered(User user, String message) {
        return AuthResponse.builder()
                .user(UserInfo.fromUser(user))
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}