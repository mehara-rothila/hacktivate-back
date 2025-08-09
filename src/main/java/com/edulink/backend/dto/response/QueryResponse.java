// File: src/main/java/com/edulink/backend/dto/response/QueryResponse.java
package com.edulink.backend.dto.response;

import com.edulink.backend.model.entity.Query;
import com.edulink.backend.model.entity.User;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.format.DateTimeFormatter;
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
    private String submittedAt;
    private String lastUpdated;
    private int responseCount;
    private boolean readByLecturer;
    private boolean readByStudent;
    
    // Student info (for lecturer view)
    private StudentInfo student;
    
    // Lecturer info (for student view)
    private LecturerInfo lecturer;
    
    // Messages and status history (for detailed view)
    private List<QueryMessageResponse> messages;
    private List<StatusHistoryResponse> statusHistory;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static QueryResponse fromQuery(Query query, User student, User lecturer) {
        return QueryResponse.builder()
                .id(query.getId())
                .title(query.getTitle())
                .description(query.getDescription())
                .category(query.getCategory().getDisplayName())
                .priority(query.getPriority().getValue())
                .status(query.getStatus().getValue())
                .course(query.getCourse())
                .submittedAt(query.getSubmittedAt().format(FORMATTER))
                .lastUpdated(query.getLastUpdated().format(FORMATTER))
                .responseCount(query.getResponseCount())
                .readByLecturer(query.isReadByLecturer())
                .readByStudent(query.isReadByStudent())
                .student(student != null ? StudentInfo.fromUser(student) : null)
                .lecturer(lecturer != null ? LecturerInfo.fromUser(lecturer) : null)
                .build();
    }
    
    public static QueryResponse fromQueryDetailed(Query query, User student, User lecturer) {
        QueryResponse response = fromQuery(query, student, lecturer);
        
        response.setMessages(query.getMessages().stream()
                .map(QueryMessageResponse::fromQueryMessage)
                .collect(Collectors.toList()));
                
        response.setStatusHistory(query.getStatusHistory().stream()
                .map(StatusHistoryResponse::fromStatusHistory)
                .collect(Collectors.toList()));
                
        return response;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentInfo {
        private String id;
        private String name;
        private String email;
        private String studentId;
        private String year;
        private String major;
        private String department;

        public static StudentInfo fromUser(User user) {
            String fullName = "";
            if (user.getProfile() != null) {
                fullName = (user.getProfile().getFirstName() + " " + user.getProfile().getLastName()).trim();
            }
            
            return StudentInfo.builder()
                    .id(user.getId())
                    .name(fullName.isEmpty() ? user.getEmail() : fullName)
                    .email(user.getEmail())
                    .studentId(user.getProfile() != null ? user.getProfile().getStudentId() : null)
                    .year(user.getProfile() != null ? user.getProfile().getYear() : null)
                    .major(user.getProfile() != null ? user.getProfile().getMajor() : null)
                    .department(user.getProfile() != null ? user.getProfile().getDepartment() : null)
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
        private String employeeId;
        private String office;
        private String department;

        public static LecturerInfo fromUser(User user) {
            String fullName = "";
            if (user.getProfile() != null) {
                fullName = (user.getProfile().getFirstName() + " " + user.getProfile().getLastName()).trim();
            }
            
            return LecturerInfo.builder()
                    .id(user.getId())
                    .name(fullName.isEmpty() ? user.getEmail() : fullName)
                    .email(user.getEmail())
                    .employeeId(user.getProfile() != null ? user.getProfile().getEmployeeId() : null)
                    .office(user.getProfile() != null ? user.getProfile().getOffice() : null)
                    .department(user.getProfile() != null ? user.getProfile().getDepartment() : null)
                    .build();
        }
    }
}