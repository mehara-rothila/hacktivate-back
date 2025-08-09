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

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public CourseResponse createCourse(CourseRequest request, User lecturer) {
        if (courseRepository.findByCode(request.getCode()).isPresent()) {
            throw new IllegalStateException("A course with code " + request.getCode() + " already exists.");
        }

        Course course = Course.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .department(request.getDepartment())
                .credits(request.getCredits())
                .semester(request.getSemester())
                .lecturerId(lecturer.getId())
                .schedule(new Course.Schedule(
                        request.getSchedule().getDays(),
                        request.getSchedule().getTime(),
                        request.getSchedule().getLocation()))
                .enrollment(new Course.Enrollment(
                        request.getCapacity(),
                        new HashSet<>()))
                .status(Course.CourseStatus.UPCOMING)
                .difficulty(request.getDifficulty())
                .prerequisites(request.getPrerequisites())
                .tags(request.getTags())
                .build();

        Course savedCourse = courseRepository.save(course);
        return mapToCourseResponse(savedCourse);
    }

    public CourseResponse enrollStudentInCourse(String courseId, User student) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

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

    public CourseResponse getCourseById(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));
        return mapToCourseResponse(course);
    }

    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::mapToCourseResponse)
                .collect(Collectors.toList());
    }

    public List<CourseResponse> getCoursesByLecturer(String lecturerId) {
        return courseRepository.findAllByLecturerId(lecturerId).stream()
                .map(this::mapToCourseResponse)
                .collect(Collectors.toList());
    }

    public List<CourseResponse> getCoursesByStudent(String studentId) {
        return courseRepository.findByEnrollmentStudentIdsContaining(studentId).stream()
                .map(this::mapToCourseResponse)
                .collect(Collectors.toList());
    }

    private CourseResponse mapToCourseResponse(Course course) {
        UserProfileResponse lecturerProfile = userRepository.findById(course.getLecturerId())
                .map(UserService::mapToUserProfileResponse) // This line is now correct
                .orElse(null);

        return CourseResponse.builder()
                .id(course.getId())
                .code(course.getCode())
                .name(course.getName())
                .description(course.getDescription())
                .department(course.getDepartment())
                .credits(course.getCredits())
                .semester(course.getSemester())
                .lecturer(lecturerProfile)
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