// File Path: src/main/java/com/edulink/backend/dto/response/QueryResponse.java
package com.edulink.backend.dto.response;

import com.edulink.backend.model.entity.Query;
import com.edulink.backend.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryResponse {
    
    private String id;
    private String title;
    private String description;
    private String category;
    private String priority;
    private String status;
    private String course;
    private LocalDateTime submittedAt;
    private LocalDateTime lastUpdated;
    private List<QueryMessageResponse> messages;
    private List<StatusHistoryResponse> statusHistory;
    private boolean readByLecturer;
    private boolean readByStudent;
    private int responseCount;
    private StudentInfo student;
    private LecturerInfo lecturer;
    private LocalDateTime autoCloseAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Factory methods
    public static QueryResponse fromQuery(Query query, User student, User lecturer) {
        return QueryResponse.builder()
                .id(query.getId())
                .title(query.getTitle())
                .description(query.getDescription())
                .category(query.getCategory() != null ? query.getCategory().name() : null)
                .priority(query.getPriority() != null ? query.getPriority().name() : null)
                .status(query.getStatus() != null ? query.getStatus().name() : null)
                .course(query.getCourse())
                .submittedAt(query.getSubmittedAt())
                .lastUpdated(query.getLastUpdated())
                .readByLecturer(query.isReadByLecturer())
                .readByStudent(query.isReadByStudent())
                .responseCount(query.getResponseCount())
                .student(student != null ? StudentInfo.fromUser(student) : null)
                .lecturer(lecturer != null ? LecturerInfo.fromUser(lecturer) : null)
                .autoCloseAt(query.getAutoCloseAt())
                .createdAt(query.getCreatedAt())
                .updatedAt(query.getUpdatedAt())
                .build();
    }

    public static QueryResponse fromQueryDetailed(Query query, User student, User lecturer) {
        return QueryResponse.builder()
                .id(query.getId())
                .title(query.getTitle())
                .description(query.getDescription())
                .category(query.getCategory() != null ? query.getCategory().name() : null)
                .priority(query.getPriority() != null ? query.getPriority().name() : null)
                .status(query.getStatus() != null ? query.getStatus().name() : null)
                .course(query.getCourse())
                .submittedAt(query.getSubmittedAt())
                .lastUpdated(query.getLastUpdated())
                .messages(query.getMessages() != null ? 
                    query.getMessages().stream()
                        .map(QueryMessageResponse::fromQueryMessage)
                        .collect(Collectors.toList()) : null)
                .statusHistory(query.getStatusHistory() != null ?
                    query.getStatusHistory().stream()
                        .map(StatusHistoryResponse::fromStatusHistoryEntry)
                        .collect(Collectors.toList()) : null)
                .readByLecturer(query.isReadByLecturer())
                .readByStudent(query.isReadByStudent())
                .responseCount(query.getResponseCount())
                .student(student != null ? StudentInfo.fromUser(student) : null)
                .lecturer(lecturer != null ? LecturerInfo.fromUser(lecturer) : null)
                .autoCloseAt(query.getAutoCloseAt())
                .createdAt(query.getCreatedAt())
                .updatedAt(query.getUpdatedAt())
                .build();
    }

    // Nested classes for user info
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentInfo {
        private String id;
        private String name;
        private String email;
        private String department;
        private String studentId;
        private String year;
        private String major;

        public static StudentInfo fromUser(User user) {
            if (user == null) return null;
            
            return StudentInfo.builder()
                    .id(user.getId())
                    .name(getFullName(user))
                    .email(user.getEmail())
                    .department(user.getProfile() != null ? user.getProfile().getDepartment() : null)
                    .studentId(user.getProfile() != null ? user.getProfile().getStudentId() : null)
                    .year(user.getProfile() != null ? user.getProfile().getYear() : null)
                    .major(user.getProfile() != null ? user.getProfile().getMajor() : null)
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LecturerInfo {
        private String id;
        private String name;
        private String email;
        private String department;
        private String employeeId;
        private String office;
        private String phone;

        public static LecturerInfo fromUser(User user) {
            if (user == null) return null;
            
            return LecturerInfo.builder()
                    .id(user.getId())
                    .name(getFullName(user))
                    .email(user.getEmail())
                    .department(user.getProfile() != null ? user.getProfile().getDepartment() : null)
                    .employeeId(user.getProfile() != null ? user.getProfile().getEmployeeId() : null)
                    .office(user.getProfile() != null ? user.getProfile().getOffice() : null)
                    .phone(user.getProfile() != null ? user.getProfile().getPhone() : null)
                    .build();
        }
    }

    // Helper method
    private static String getFullName(User user) {
        if (user.getProfile() != null) {
            String firstName = user.getProfile().getFirstName();
            String lastName = user.getProfile().getLastName();
            if (firstName != null && lastName != null) {
                return (firstName + " " + lastName).trim();
            }
        }
        return user.getEmail();
    }
}