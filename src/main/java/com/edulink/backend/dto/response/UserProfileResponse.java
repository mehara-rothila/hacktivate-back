// File Path: src/main/java/com/edulink/backend/dto/response/UserProfileResponse.java
package com.edulink.backend.dto.response;

import com.edulink.backend.model.entity.User;
import java.time.LocalDateTime;

public class UserProfileResponse {
    
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
    private LocalDateTime updatedAt;

    // Constructors
    public UserProfileResponse() {}

    // Static factory method
    public static UserProfileResponse fromUser(User user) {
        if (user == null) return null;
        
        UserProfileResponse response = new UserProfileResponse();
        response.id = user.getId();
        response.email = user.getEmail();
        response.firstName = user.getProfile() != null ? user.getProfile().getFirstName() : null;
        response.lastName = user.getProfile() != null ? user.getProfile().getLastName() : null;
        response.role = user.getRole() != null ? user.getRole().toString() : null;
        response.department = user.getProfile() != null ? user.getProfile().getDepartment() : null;
        response.avatar = user.getProfile() != null ? user.getProfile().getAvatar() : null;
        response.studentId = user.getProfile() != null ? user.getProfile().getStudentId() : null;
        response.employeeId = user.getProfile() != null ? user.getProfile().getEmployeeId() : null;
        response.year = user.getProfile() != null ? user.getProfile().getYear() : null;
        response.major = user.getProfile() != null ? user.getProfile().getMajor() : null;
        response.office = user.getProfile() != null ? user.getProfile().getOffice() : null;
        response.phone = user.getProfile() != null ? user.getProfile().getPhone() : null;
        response.isActive = user.isActive();
        response.lastLogin = user.getLastLogin();
        response.createdAt = user.getCreatedAt();
        response.updatedAt = user.getUpdatedAt();
        return response;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public String getOffice() { return office; }
    public void setOffice(String office) { this.office = office; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}