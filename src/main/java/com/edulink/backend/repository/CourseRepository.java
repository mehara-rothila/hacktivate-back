// src/main/java/com/edulink/backend/repository/CourseRepository.java
package com.edulink.backend.repository;

import com.edulink.backend.model.entity.Course;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends MongoRepository<Course, String> {

    /**
     * Finds a course by its unique course code.
     * @param code The course code (e.g., "CS101").
     * @return An Optional containing the course if found.
     */
    Optional<Course> findByCode(String code);

    /**
     * Finds all courses taught by a specific lecturer.
     * @param lecturerId The ID of the lecturer.
     * @return A list of courses.
     */
    List<Course> findAllByLecturerId(String lecturerId);

    /**
     * Finds all courses a specific student is enrolled in.
     * @param studentId The ID of the student.
     * @return A list of courses.
     */
    List<Course> findByEnrollmentStudentIdsContaining(String studentId);
}