// src/main/java/com/edulink/backend/controller/CourseController.java
package com.edulink.backend.controller;

import com.edulink.backend.dto.request.CourseRequest;
import com.edulink.backend.dto.response.ApiResponse;
import com.edulink.backend.dto.response.CourseResponse;
import com.edulink.backend.model.entity.User;
import com.edulink.backend.service.CourseService;
import com.edulink.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<ApiResponse> createCourse(@Valid @RequestBody CourseRequest courseRequest) {
        User currentUser = userService.getCurrentUser();
        CourseResponse newCourse = courseService.createCourse(courseRequest, currentUser);
        return new ResponseEntity<>(new ApiResponse(true, "Course created successfully.", newCourse), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> getAllCourses() {
        List<CourseResponse> courses = courseService.getAllCourses();
        return ResponseEntity.ok(new ApiResponse(true, "Courses retrieved successfully.", courses));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> getCourseById(@PathVariable String id) {
        CourseResponse course = courseService.getCourseById(id);
        return ResponseEntity.ok(new ApiResponse(true, "Course retrieved successfully.", course));
    }

    @PostMapping("/{id}/enroll")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse> enrollInCourse(@PathVariable String id) {
        User currentUser = userService.getCurrentUser();
        CourseResponse updatedCourse = courseService.enrollStudentInCourse(id, currentUser);
        return ResponseEntity.ok(new ApiResponse(true, "Successfully enrolled in course.", updatedCourse));
    }

    // Endpoint to get courses for the logged-in lecturer
    @GetMapping("/my-courses/lecturer")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<ApiResponse> getLecturerCourses() {
        User currentUser = userService.getCurrentUser();
        List<CourseResponse> courses = courseService.getCoursesByLecturer(currentUser.getId());
        return ResponseEntity.ok(new ApiResponse(true, "Lecturer's courses retrieved successfully.", courses));
    }

    // Endpoint to get courses for the logged-in student
    @GetMapping("/my-courses/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse> getStudentCourses() {
        User currentUser = userService.getCurrentUser();
        List<CourseResponse> courses = courseService.getCoursesByStudent(currentUser.getId());
        return ResponseEntity.ok(new ApiResponse(true, "Student's enrolled courses retrieved successfully.", courses));
    }
}