package com.edulink.backend.controller;

import com.edulink.backend.model.entity.User;
import com.edulink.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private UserRepository userRepository;

    // Test basic API endpoint
    @GetMapping("/hello")
    public ResponseEntity<Map<String, Object>> hello() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "EduLink Pro Backend is running!");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    // Test MongoDB connection
    @GetMapping("/db-status")
    public ResponseEntity<Map<String, Object>> checkDatabaseConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Try to count users (this will test MongoDB connection)
            long userCount = userRepository.count();
            
            response.put("database", "connected");
            response.put("userCount", userCount);
            response.put("status", "success");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("database", "connection failed");
            response.put("error", e.getMessage());
            response.put("status", "error");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    // Test creating a user (POST)
    @PostMapping("/create-user")
    public ResponseEntity<Map<String, Object>> createTestUser(@RequestBody CreateUserRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if user already exists
            if (userRepository.existsByEmail(request.getEmail())) {
                response.put("message", "User already exists");
                response.put("status", "error");
                return ResponseEntity.badRequest().body(response);
            }

            User user = new User();
            user.setEmail(request.getEmail());
            user.setPassword("password123"); // In real app, this would be hashed
            user.setRole(User.UserRole.valueOf(request.getRole().toUpperCase()));
            
            User.UserProfile profile = new User.UserProfile();
            profile.setFirstName(request.getFirstName());
            profile.setLastName(request.getLastName());
            profile.setDepartment(request.getDepartment());
            user.setProfile(profile);

            User savedUser = userRepository.save(user);
            
            response.put("message", "User created successfully");
            response.put("userId", savedUser.getId());
            response.put("email", savedUser.getEmail());
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Failed to create user");
            response.put("error", e.getMessage());
            response.put("status", "error");
            
            return ResponseEntity.status(500).body(response);
        }
    }

    // Test getting all users (GET)
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<User> users = userRepository.findAll();
            
            response.put("users", users);
            response.put("count", users.size());
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Failed to fetch users");
            response.put("error", e.getMessage());
            response.put("status", "error");
            
            return ResponseEntity.status(500).body(response);
        }
    }

    // Request class for creating users
    public static class CreateUserRequest {
        private String email;
        private String firstName;
        private String lastName;
        private String role;
        private String department;

        // Getters and setters
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
    }
}