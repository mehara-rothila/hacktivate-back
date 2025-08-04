package com.edulink.backend.model.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String email;
    
    private String password; // Will be bcrypt hashed
    
    private UserRole role;
    
    private UserProfile profile;
    
    private boolean isActive = true;
    
    private LocalDateTime lastLogin;
    
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    // Nested classes for embedded documents
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserProfile {
        private String firstName;
        private String lastName;
        private String avatar;
        private String department;
        private String studentId;  // For students
        private String employeeId; // For lecturers
        private String phone;
        private String office;     // For lecturers
        private String year;       // For students (Freshman, Sophomore, etc.)
        private String major;      // For students
    }
    
    // Enum for user roles
    public enum UserRole {
        STUDENT, LECTURER, ADMIN
    }
}