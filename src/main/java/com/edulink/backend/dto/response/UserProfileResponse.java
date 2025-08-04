// File Path: src/main/java/com/edulink/backend/dto/response/UserProfileResponse.java
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
public class UserProfileResponse {

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
    private String guardianContact;
    
    private boolean isActive;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Create UserProfileResponse from User entity
    public static UserProfileResponse fromUser(User user) {
        UserProfileResponse.UserProfileResponseBuilder builder = UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole().toString())
                .isActive(user.isActive())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt());

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