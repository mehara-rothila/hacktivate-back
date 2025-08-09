// src/main/java/com/edulink/backend/dto/request/CourseRequest.java
package com.edulink.backend.dto.request;

import com.edulink.backend.model.entity.Course.Difficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class CourseRequest {

    @NotBlank(message = "Course code is required.")
    private String code;

    @NotBlank(message = "Course name is required.")
    private String name;

    private String description;

    @NotBlank(message = "Department is required.")
    private String department;

    @NotNull(message = "Credits are required.")
    @Positive(message = "Credits must be a positive number.")
    private Integer credits;

    @NotBlank(message = "Semester is required.")
    private String semester;

    @NotNull(message = "Schedule is required.")
    private ScheduleRequest schedule;

    @NotNull(message = "Enrollment capacity is required.")
    @Positive(message = "Capacity must be a positive number.")
    private Integer capacity;

    @NotNull(message = "Difficulty is required.")
    private Difficulty difficulty;

    private List<String> prerequisites;
    private Set<String> tags;

    @Data
    public static class ScheduleRequest {
        @NotNull
        private List<String> days;
        @NotBlank
        private String time;
        private String location;
    }
}