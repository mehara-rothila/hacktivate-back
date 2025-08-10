// File Path: src/main/java/com/edulink/backend/model/entity/User.java
package com.edulink.backend.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String email;
    
    private String password;
    
    @Indexed
    private UserRole role;
    
    private UserProfile profile;
    
    private boolean isActive = true;
    
    private LocalDateTime lastLogin;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Role enumeration
    public enum UserRole {
        STUDENT, LECTURER, ADMIN
    }

    // Nested UserProfile class
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserProfile {
        private String firstName;
        private String lastName;
        private String department;
        private String phone;
        private String avatar; // Profile picture filename
        private String bio;
        private String dateOfBirth;
        private String gender;
        private String address;
        private String city;
        private String country;
        private String postalCode;
        private String emergencyContact;
        private String emergencyPhone;
        private String linkedIn;
        private String github;
        private String portfolio;
        private String website;
        private String researchGate;
        private String orcid;
        
        // Student-specific fields
        private String studentId;
        private String year;
        private String major;
        private String minor;
        private String program;
        private String gpa;
        private String expectedGraduation;
        private String enrollmentStatus; // full-time, part-time, exchange
        private String academicStanding; // good, probation, honors
        
        // Lecturer-specific fields
        private String employeeId;
        private String office;
        private String title; // Dr., Prof., Mr., Ms., Mrs.
        private String position; // Professor, Associate Professor, etc.
        private String qualification;
        private String experience;
        private String employmentType; // full-time, part-time, visiting, adjunct
        private String status; // active, sabbatical, emeritus
        private String officeAddress;
        private String officeHours;
        private String campus;
        private String building;
        private String room;
        
        // Additional profile fields
        @Builder.Default
        private LocalDateTime updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public String getFullName() {
        if (profile != null && profile.getFirstName() != null && profile.getLastName() != null) {
            return profile.getFirstName() + " " + profile.getLastName();
        }
        return email;
    }

    public String getInitials() {
        if (profile != null && profile.getFirstName() != null && profile.getLastName() != null) {
            return profile.getFirstName().substring(0, 1).toUpperCase() + 
                   profile.getLastName().substring(0, 1).toUpperCase();
        }
        return email.substring(0, 2).toUpperCase();
    }

    public String getAvatarUrl() {
        if (profile != null && profile.getAvatar() != null && !profile.getAvatar().isEmpty()) {
            return "/api/files/" + profile.getAvatar();
        }
        return null;
    }

    public boolean hasAvatar() {
        return profile != null && profile.getAvatar() != null && !profile.getAvatar().isEmpty();
    }

    // Student-specific helper methods
    public boolean isStudent() {
        return role == UserRole.STUDENT;
    }

    public boolean isLecturer() {
        return role == UserRole.LECTURER;
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    // Update timestamp method
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
        if (this.profile != null) {
            this.profile.setUpdatedAt(LocalDateTime.now());
        }
    }
}