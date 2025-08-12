// src/main/java/com/edulink/backend/dto/response/CourseResponse.java
package com.edulink.backend.dto.response;

import com.edulink.backend.model.entity.Course.CourseStatus;
import com.edulink.backend.model.entity.Course.Difficulty;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor // Add this for the main builder
@NoArgsConstructor  // Add this for default constructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourseResponse {

    private String id;
    private String code;
    private String name;
    private String description;
    private String department;
    private Integer credits;
    private String semester;
    private UserProfileResponse lecturer;
    private ScheduleResponse schedule;
    private EnrollmentResponse enrollment;
    private CourseStatus status;
    private Difficulty difficulty;
    private List<String> prerequisites;
    private Set<String> tags;
    private String syllabus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @AllArgsConstructor // Ensures the constructor is public
    @NoArgsConstructor
    public static class ScheduleResponse {
        private List<String> days;
        private String time;
        private String location;
    }

    @Data
    @Builder
    @AllArgsConstructor // Ensures the constructor is public
    @NoArgsConstructor
    public static class EnrollmentResponse {
        private Integer capacity;
        private Integer currentEnrollment;
        // Removed studentIds as it's not needed in the response for now
    }
}
