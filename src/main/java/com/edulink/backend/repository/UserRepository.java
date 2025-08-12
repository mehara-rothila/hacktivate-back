// src/main/java/com/edulink/backend/repository/UserRepository.java
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
    
    // =================== EXISTING METHODS ===================
    // Find user by email for authentication
    Optional<User> findByEmail(String email);
    
    // Check if email already exists
    boolean existsByEmail(String email);
    
    // Find users by role
    List<User> findByRole(User.UserRole role);
    
    // Find active users
    List<User> findByIsActiveTrue();
    
    // =================== ENHANCED DEPARTMENT QUERIES ===================
    
    // Find users by department (using profile.department path)
    @Query("{'profile.department': ?0}")
    List<User> findByDepartment(String department);
    
    // Find users by department and role
    @Query("{'role': ?0, 'profile.department': ?1}")
    List<User> findByRoleAndDepartment(UserRole role, String department);
    
    // ADDED: Alias method for backward compatibility with service code
    @Query("{'role': ?0, 'profile.department': ?1}")
    List<User> findByRoleAndProfile_Department(UserRole role, String department);
    
    // Find active users by department
    @Query("{'profile.department': ?0, 'isActive': true}")
    List<User> findByDepartmentAndIsActiveTrue(String department);
    
    // Find active users by role and department
    @Query("{'role': ?0, 'profile.department': ?1, 'isActive': true}")
    List<User> findByRoleAndDepartmentAndIsActiveTrue(UserRole role, String department);
    
    // =================== SPECIALIZED QUERIES ===================
    
    // Find students by year
    @Query("{'role': 'STUDENT', 'profile.year': ?0}")
    List<User> findStudentsByYear(String year);
    
    // Find lecturers by department
    @Query("{'role': 'LECTURER', 'profile.department': ?0}")
    List<User> findLecturersByDepartment(String department);
    
    // Find active lecturers only
    @Query("{'role': 'LECTURER', 'isActive': true}")
    List<User> findActiveLecturers();
    
    // Find active students only
    @Query("{'role': 'STUDENT', 'isActive': true}")
    List<User> findActiveStudents();
    
    // =================== SEARCH QUERIES ===================
    
    // Search users by name (first name or last name)
    @Query("{'$or': [" +
           "{'profile.firstName': {'$regex': ?0, '$options': 'i'}}, " +
           "{'profile.lastName': {'$regex': ?0, '$options': 'i'}}" +
           "]}")
    List<User> findByNameContainingIgnoreCase(String name);
    
    // Search users by email containing
    List<User> findByEmailContainingIgnoreCase(String email);
    
    // Search users by multiple fields (name, email, department)
    @Query("{'$or': [" +
           "{'profile.firstName': {'$regex': ?0, '$options': 'i'}}, " +
           "{'profile.lastName': {'$regex': ?0, '$options': 'i'}}, " +
           "{'email': {'$regex': ?0, '$options': 'i'}}, " +
           "{'profile.department': {'$regex': ?0, '$options': 'i'}}" +
           "]}")
    List<User> searchByMultipleFields(String query);
    
    // Search lecturers by specialization/qualification
    @Query("{'role': 'LECTURER', 'profile.qualification': {'$regex': ?0, '$options': 'i'}}")
    List<User> findLecturersByQualification(String qualification);
    
    // =================== AGGREGATE QUERIES ===================
    
    // Get all distinct departments
    @Query(value = "{}", fields = "{'profile.department': 1}")
    List<User> findAllForDepartments();
    
    // Count users by role
    long countByRole(UserRole role);
    
    // Count active users by role
    long countByRoleAndIsActiveTrue(UserRole role);
    
    // Count users by department
    @Query(value = "{'profile.department': ?0}", count = true)
    long countByDepartment(String department);
    
    // =================== ADVANCED SEARCH ===================
    
    // Find lecturers with office hours
    @Query("{'role': 'LECTURER', 'profile.officeHours': {'$exists': true, '$ne': null}}")
    List<User> findLecturersWithOfficeHours();
    
    // Find users by role with profile data populated
    @Query("{'role': ?0, 'profile': {'$exists': true}}")
    List<User> findByRoleWithProfile(UserRole role);
    
    // Find recently active users (within last N days)
    @Query("{'lastLogin': {'$gte': ?0}}")
    List<User> findRecentlyActiveUsers(java.time.LocalDateTime since);
    
    // Find users by employment type (for lecturers)
    @Query("{'role': 'LECTURER', 'profile.employmentType': ?0}")
    List<User> findLecturersByEmploymentType(String employmentType);
    
    // Find students by enrollment status
    @Query("{'role': 'STUDENT', 'profile.enrollmentStatus': ?0}")
    List<User> findStudentsByEnrollmentStatus(String enrollmentStatus);
    
    // =================== CONTACT INFORMATION QUERIES ===================
    
    // Find users with phone numbers
    @Query("{'profile.phone': {'$exists': true, '$ne': null}}")
    List<User> findUsersWithPhoneNumbers();
    
    // Find lecturers by office location
    @Query("{'role': 'LECTURER', 'profile.office': {'$regex': ?0, '$options': 'i'}}")
    List<User> findLecturersByOffice(String office);
    
    // Find users by campus
    @Query("{'profile.campus': ?0}")
    List<User> findUsersByCampus(String campus);
    
    // =================== ROLE-BASED CONVENIENCE METHODS ===================
    
    // These use the existing findByRole method but add type safety
    default List<User> findAllStudents() {
        return findByRole(UserRole.STUDENT);
    }
    
    default List<User> findAllLecturers() {
        return findByRole(UserRole.LECTURER);
    }
    
    default List<User> findAllAdmins() {
        return findByRole(UserRole.ADMIN);
    }
}