package com.edulink.backend.repository;

import com.edulink.backend.model.entity.User;
import com.edulink.backend.model.entity.User.UserRole;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    
    // Find user by email for authentication
    Optional<User> findByEmail(String email);
    
    // Check if email already exists
    boolean existsByEmail(String email);
    
    // Find users by role
    List<User> findByRole(User.UserRole role);
    
    // Find active users
    List<User> findByIsActiveTrue();
    
    // Find users by department
    @Query("{'profile.department': ?0}")
    List<User> findByDepartment(String department);
    
    // Find students by year
    @Query("{'role': 'STUDENT', 'profile.year': ?0}")
    List<User> findStudentsByYear(String year);
    
    // Find lecturers by department
    @Query("{'role': 'LECTURER', 'profile.department': ?0}")
    List<User> findLecturersByDepartment(String department);
    
    // Find users by role and department
    List<User> findByRoleAndProfile_Department(UserRole role, String department);
}