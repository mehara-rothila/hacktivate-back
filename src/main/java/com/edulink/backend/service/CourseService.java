// src/main/java/com/edulink/backend/service/CourseService.java
package com.edulink.backend.service;

import com.edulink.backend.dto.request.CourseRequest;
import com.edulink.backend.dto.response.CourseResponse;
import com.edulink.backend.dto.response.UserProfileResponse;
import com.edulink.backend.model.entity.Course;
import com.edulink.backend.model.entity.User;
import com.edulink.backend.repository.CourseRepository;
import com.edulink.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    /**
     * Creates a new course.
     *
     * @param request The request DTO containing course details.
     * @param lecturer The user entity of the lecturer creating the course.
     * @return A DTO of the newly created course.
     */
    public CourseResponse createCourse(CourseRequest request, User lecturer) {
        // 1. Check if a course with the same code already exists
        if (courseRepository.findByCode(request.getCode()).isPresent()) {
            throw new IllegalStateException("A course with code " + request.getCode() + " already exists.");
        }

        // 2. Build the Course entity from the request
        Course course = Course.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .department(request.getDepartment())
                .credits(request.getCredits())
                .semester(request.getSemester())
                .lecturerId(lecturer.getId()) // Set the lecturer's ID
                .schedule(new Course.Schedule(
                        request.getSchedule().getDays(),
                        request.getSchedule().getTime(),
                        request.getSchedule().getLocation()))
                .enrollment(new Course.Enrollment(
                        request.getCapacity(),
                        new HashSet<>())) // Start with an empty set of students
                .status(Course.CourseStatus.UPCOMING) // New courses start as UPCOMING
                .difficulty(request.getDifficulty())
                .prerequisites(request.getPrerequisites())
                .tags(request.getTags())
                .build();

        // 3. Save the new course to the database
        Course savedCourse = courseRepository.save(course);

        // 4. Map the saved entity to a response DTO and return it
        return mapToCourseResponse(savedCourse);
    }

    /**
     * Enrolls the current student in a specific course.
     *
     * @param courseId The ID of the course to enroll in.
     * @param student The user entity of the student.
     * @return A DTO of the updated course.
     */
    public CourseResponse enrollStudentInCourse(String courseId, User student) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with ID: " + courseId));

        if (course.getEnrollment().getStudentIds().size() >= course.getEnrollment().getCapacity()) {
            throw new IllegalStateException("Course is already full.");
        }

        if (course.getEnrollment().getStudentIds().contains(student.getId())) {
            throw new IllegalStateException("Student is already enrolled in this course.");
        }

        course.getEnrollment().getStudentIds().add(student.getId());
        Course updatedCourse = courseRepository.save(course);

        return mapToCourseResponse(updatedCourse);
    }

    /**
     * Retrieves a single course by its ID.
     *
     * @param courseId The ID of the course.
     * @return A DTO of the course.
     */
    public CourseResponse getCourseById(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with ID: " + courseId));
        return mapToCourseResponse(course);
    }

    /**
     * Retrieves all courses.
     *
     * @return A list of all course DTOs.
     */
    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::mapToCourseResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all courses taught by a specific lecturer.
     *
     * @param lecturerId The ID of the lecturer.
     * @return A list of course DTOs.
     */
    public List<CourseResponse> getCoursesByLecturer(String lecturerId) {
        return courseRepository.findAllByLecturerId(lecturerId).stream()
                .map(this::mapToCourseResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all courses a specific student is enrolled in.
     *
     * @param studentId The ID of the student.
     * @return A list of course DTOs.
     */
    public List<CourseResponse> getCoursesByStudent(String studentId) {
        return courseRepository.findByEnrollmentStudentIdsContaining(studentId).stream()
                .map(this::mapToCourseResponse)
                .collect(Collectors.toList());
    }


    /**
     * Helper method to map a Course entity to a CourseResponse DTO.
     * This is important for populating lecturer details.
     *
     * @param course The Course entity.
     * @return The CourseResponse DTO.
     */
    private CourseResponse mapToCourseResponse(Course course) {
        // Find the lecturer's user profile to include in the response
        UserProfileResponse lecturerProfile = userRepository.findById(course.getLecturerId())
                .map(UserService::mapToUserProfileResponse) // Re-use the mapping logic from UserService
                .orElse(null); // Handle case where lecturer might not be found

        return CourseResponse.builder()
                .id(course.getId())
                .code(course.getCode())
                .name(course.getName())
                .description(course.getDescription())
                .department(course.getDepartment())
                .credits(course.getCredits())
                .semester(course.getSemester())
                .lecturer(lecturerProfile) // Set the populated lecturer profile
                .schedule(CourseResponse.ScheduleResponse.builder()
                        .days(course.getSchedule().getDays())
                        .time(course.getSchedule().getTime())
                        .location(course.getSchedule().getLocation())
                        .build())
                .enrollment(CourseResponse.EnrollmentResponse.builder()
                        .capacity(course.getEnrollment().getCapacity())
                        .studentIds(course.getEnrollment().getStudentIds())
                        .currentEnrollment(course.getEnrollment().getStudentIds() != null ? course.getEnrollment().getStudentIds().size() : 0)
                        .build())
                .status(course.getStatus())
                .difficulty(course.getDifficulty())
                .prerequisites(course.getPrerequisites())
                .tags(course.getTags())
                .syllabus(course.getSyllabus())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
}