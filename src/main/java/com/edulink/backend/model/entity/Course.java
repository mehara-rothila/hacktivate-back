// src/main/java/com/edulink/backend/model/entity/Course.java
package com.edulink.backend.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Document(collection = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    private String id;

    @Indexed(unique = true)
    private String code; // e.g., "CS101"

    private String name;
    private String description;
    private String department;
    private Integer credits;
    private String semester; // e.g., "Fall 2025"

    @Indexed
    private String lecturerId; // Reference to the User ID of the lecturer

    private Schedule schedule;
    private Enrollment enrollment;
    private CourseStatus status;
    private Difficulty difficulty;

    private List<String> prerequisites; // List of course codes
    private Set<String> tags;
    private String syllabus; // URL to the syllabus file

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // --- Embedded Documents & Enums ---

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Schedule {
        private List<String> days; // e.g., ["Monday", "Wednesday"]
        private String time; // e.g., "10:00-11:30"
        private String location; // e.g., "Building A, Room 101"
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Enrollment {
        private Integer capacity;
        private Set<String> studentIds; // Set of User IDs
    }

    public enum CourseStatus {
        UPCOMING,
        ACTIVE,
        COMPLETED,
        ARCHIVED
    }

    public enum Difficulty {
        BEGINNER,
        INTERMEDIATE,
        ADVANCED
    }
}