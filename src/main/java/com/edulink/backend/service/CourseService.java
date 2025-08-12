// src/main/java/com/edulink/backend/service/CourseService.java
package com.edulink.backend.service;

import com.edulink.backend.dto.request.CourseRequest;
import com.edulink.backend.dto.response.CourseResponse;
import com.edulink.backend.dto.response.UserProfileResponse;
import com.edulink.backend.exception.ResourceNotFoundException;
import com.edulink.backend.model.entity.Course;
import com.edulink.backend.model.entity.User;
import com.edulink.backend.repository.CourseRepository;
import com.edulink.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Transactional
    public CourseResponse createCourse(CourseRequest courseRequest, User lecturer) {
        Course course = new Course();
        course.setCode(courseRequest.getCode());
        course.setName(courseRequest.getName());
        course.setDescription(courseRequest.getDescription());
        course.setDepartment(courseRequest.getDepartment());
        course.setCredits(courseRequest.getCredits());
        course.setSemester(courseRequest.getSemester());
        course.setDifficulty(courseRequest.getDifficulty());
        course.setTags(courseRequest.getTags());
        course.setPrerequisites(courseRequest.getPrerequisites());
        
        // Store the lecturer's ID as a String, not the whole object
        course.setLecturerId(lecturer.getId());

        // Map nested Schedule object
        Course.Schedule schedule = new Course.Schedule();
        schedule.setDays(courseRequest.getSchedule().getDays());
        schedule.setTime(courseRequest.getSchedule().getTime());
        schedule.setLocation(courseRequest.getSchedule().getLocation());
        course.setSchedule(schedule);

        // Map nested Enrollment object and initialize student IDs
        Course.Enrollment enrollment = new Course.Enrollment();
        enrollment.setCapacity(courseRequest.getEnrollment().getCapacity());
        enrollment.setStudentIds(new HashSet<>()); // Initialize with an empty set
        course.setEnrollment(enrollment);

        course.setStatus(Course.CourseStatus.UPCOMING);
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());

        Course savedCourse = courseRepository.save(course);
        return mapToCourseResponse(savedCourse);
    }

    @Transactional
    public CourseResponse enrollStudentInCourse(String courseId, User student) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        if (course.getEnrollment().getStudentIds().contains(student.getId())) {
            throw new IllegalStateException("Student is already enrolled in this course.");
        }

        if (course.getEnrollment().getStudentIds().size() >= course.getEnrollment().getCapacity()) {
            throw new IllegalStateException("Course has reached its maximum capacity.");
        }

        course.getEnrollment().getStudentIds().add(student.getId());
        course.setUpdatedAt(LocalDateTime.now());

        Course updatedCourse = courseRepository.save(course);
        return mapToCourseResponse(updatedCourse);
    }

    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::mapToCourseResponse)
                .collect(Collectors.toList());
    }

    public CourseResponse getCourseById(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
        return mapToCourseResponse(course);
    }

    public List<CourseResponse> getCoursesByLecturerId(String lecturerId) {
        return courseRepository.findAllByLecturerId(lecturerId).stream()
                .map(this::mapToCourseResponse)
                .collect(Collectors.toList());
    }

    public List<CourseResponse> getCoursesByStudentId(String studentId) {
        return courseRepository.findByEnrollmentStudentIdsContaining(studentId).stream()
                .map(this::mapToCourseResponse)
                .collect(Collectors.toList());
    }

    private CourseResponse mapToCourseResponse(Course course) {
        // Fetch the lecturer's full profile using the stored ID
        User lecturer = userRepository.findById(course.getLecturerId()).orElse(null);
        UserProfileResponse lecturerResponse = UserService.mapToUserProfileResponse(lecturer);

        // Map nested objects to their response DTOs
        CourseResponse.ScheduleResponse scheduleResponse = new CourseResponse.ScheduleResponse(
                course.getSchedule().getDays(),
                course.getSchedule().getTime(),
                course.getSchedule().getLocation()
        );

        // Calculate current enrollment from the size of the studentIds set
        int currentEnrollment = course.getEnrollment().getStudentIds() != null ? course.getEnrollment().getStudentIds().size() : 0;
        
        // Correctly creating the EnrollmentResponse with the right arguments
        CourseResponse.EnrollmentResponse enrollmentResponse = new CourseResponse.EnrollmentResponse(
                course.getEnrollment().getCapacity(),
                currentEnrollment
        );

        // Build the final response object
        return CourseResponse.builder()
                .id(course.getId())
                .code(course.getCode())
                .name(course.getName())
                .description(course.getDescription())
                .department(course.getDepartment())
                .credits(course.getCredits())
                .semester(course.getSemester())
                .lecturer(lecturerResponse)
                .schedule(scheduleResponse)
                .enrollment(enrollmentResponse)
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
