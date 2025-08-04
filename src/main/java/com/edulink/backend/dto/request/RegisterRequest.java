// File Path: src/main/java/com/edulink/backend/dto/request/RegisterRequest.java
package com.edulink.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "^(STUDENT|LECTURER)$", message = "Role must be either STUDENT or LECTURER")
    private String role;

    @NotBlank(message = "Department is required")
    @Size(min = 2, max = 100, message = "Department must be between 2 and 100 characters")
    private String department;

    // Optional fields for students
    private String studentId;
    private String year; // Freshman, Sophomore, Junior, Senior
    private String major;
    private String guardianContact;

    // Optional fields for lecturers
    private String employeeId;
    private String office;
    private String phone;

    // Common optional fields
    private String avatar; // URL or base64 encoded image
}