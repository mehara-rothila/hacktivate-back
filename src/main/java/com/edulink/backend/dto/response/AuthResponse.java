// File Path: src/main/java/com/edulink/backend/dto/response/AuthResponse.java
package com.edulink.backend.dto.response;

import com.edulink.backend.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UserInfo user;
    private String message;
    private LocalDateTime timestamp;

    // Static factory methods
    public static AuthResponse success(String accessToken, String refreshToken, Long expiresIn, User user, String message) {
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

    public static AuthResponse registered(User user, String message) {
        return AuthResponse.builder()
                .user(UserInfo.fromUser(user))
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Nested UserInfo class
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
        private String studentId;
        private String employeeId;
        private String year;
        private String major;
        private String office;
        private String phone;
        private boolean isActive;
        private LocalDateTime lastLogin;
        private LocalDateTime createdAt;

        public static UserInfo fromUser(User user) {
            if (user == null) return null;
            
            return UserInfo.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .firstName(user.getProfile() != null ? user.getProfile().getFirstName() : null)
                    .lastName(user.getProfile() != null ? user.getProfile().getLastName() : null)
                    .role(user.getRole() != null ? user.getRole().toString() : null)
                    .department(user.getProfile() != null ? user.getProfile().getDepartment() : null)
                    .avatar(user.getProfile() != null ? user.getProfile().getAvatar() : null)
                    .studentId(user.getProfile() != null ? user.getProfile().getStudentId() : null)
                    .employeeId(user.getProfile() != null ? user.getProfile().getEmployeeId() : null)
                    .year(user.getProfile() != null ? user.getProfile().getYear() : null)
                    .major(user.getProfile() != null ? user.getProfile().getMajor() : null)
                    .office(user.getProfile() != null ? user.getProfile().getOffice() : null)
                    .phone(user.getProfile() != null ? user.getProfile().getPhone() : null)
                    .isActive(user.isActive())
                    .lastLogin(user.getLastLogin())
                    .createdAt(user.getCreatedAt())
                    .build();
        }
    }
}