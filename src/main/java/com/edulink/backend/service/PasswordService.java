// File Path: src/main/java/com/edulink/backend/service/PasswordService.java
package com.edulink.backend.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {

    private final PasswordEncoder passwordEncoder;

    public PasswordService() {
        this.passwordEncoder = new BCryptPasswordEncoder(12); // Strength 12 for security
    }

    /**
     * Hash a plain text password using BCrypt
     * @param plainPassword The plain text password to hash
     * @return The hashed password
     */
    public String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return passwordEncoder.encode(plainPassword);
    }

    /**
     * Verify a plain text password against a hashed password
     * @param plainPassword The plain text password to verify
     * @param hashedPassword The hashed password to compare against
     * @return true if passwords match, false otherwise
     */
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        return passwordEncoder.matches(plainPassword, hashedPassword);
    }

    /**
     * Check if password meets minimum requirements
     * @param password The password to validate
     * @return true if password is valid, false otherwise
     */
    public boolean isPasswordValid(String password) {
        if (password == null) {
            return false;
        }
        
        // Minimum requirements:
        // - At least 6 characters long
        // - Contains at least one letter
        // - Contains at least one digit
        return password.length() >= 6 &&
               password.chars().anyMatch(Character::isLetter) &&
               password.chars().anyMatch(Character::isDigit);
    }

    /**
     * Get password validation error message if password is invalid
     * @param password The password to validate
     * @return Error message if invalid, null if valid
     */
    public String getPasswordValidationError(String password) {
        if (password == null || password.trim().isEmpty()) {
            return "Password is required";
        }
        
        if (password.length() < 6) {
            return "Password must be at least 6 characters long";
        }
        
        if (!password.chars().anyMatch(Character::isLetter)) {
            return "Password must contain at least one letter";
        }
        
        if (!password.chars().anyMatch(Character::isDigit)) {
            return "Password must contain at least one digit";
        }
        
        return null; // Password is valid
    }

    /**
     * Generate a temporary password (for testing or password reset)
     * @return A randomly generated password
     */
    public String generateTemporaryPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        
        // Ensure at least one letter and one digit
        password.append(chars.charAt((int) (Math.random() * 26))); // Uppercase letter
        password.append(chars.charAt((int) (Math.random() * 26) + 26)); // Lowercase letter
        password.append(chars.charAt((int) (Math.random() * 10) + 52)); // Digit
        
        // Add remaining characters
        for (int i = 3; i < 8; i++) {
            password.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        
        return password.toString();
    }

    /**
     * Get the password encoder instance (for Spring Security integration)
     * @return The BCrypt password encoder
     */
    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }
}