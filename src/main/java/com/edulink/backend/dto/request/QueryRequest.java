// File Path: src/main/java/com/edulink/backend/dto/request/QueryRequest.java
package com.edulink.backend.dto.request;

import com.edulink.backend.model.entity.Query.QueryCategory;
import com.edulink.backend.model.entity.Query.QueryPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryRequest {
    
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;
    
    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;
    
    @NotNull(message = "Category is required")
    private QueryCategory category;
    
    @NotNull(message = "Priority is required")
    private QueryPriority priority;
    
    private String course;
    
    private String lecturerId; // Optional - if specific lecturer is requested
}

// File Path: src/main/java/com/edulink/backend/dto/request/QueryMessageRequest.java
package com.edulink.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryMessageRequest {
    
    @NotBlank(message = "Message content is required")
    @Size(max = 2000, message = "Message must not exceed 2000 characters")
    private String content;
}

// File Path: src/main/java/com/edulink/backend/dto/request/QueryStatusUpdateRequest.java
package com.edulink.backend.dto.request;

import com.edulink.backend.model.entity.Query.QueryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryStatusUpdateRequest {
    
    @NotNull(message = "Status is required")
    private QueryStatus status;
    
    private String note;
    
    private String reason;
}

// File Path: src/main/java/com/edulink/backend/dto/response/QueryResponse.java
package com.edulink.backend.dto.response;

import com.edulink.backend.model.entity.Query;
import com.edulink.backend.model.entity.User;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
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

// File Path: src/main/java/com/edulink/backend/dto/response/QueryMessageResponse.java
package com.edulink.backend.dto.response;

import com.edulink.backend.model.entity.Query.QueryMessage;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryMessageResponse {
    
    private String id;
    private String sender;
    private String senderType;
    private String senderName;
    private String content;
    private String timestamp;
    private boolean isRead;
    private String readAt;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static QueryMessageResponse fromQueryMessage(QueryMessage message) {
        return QueryMessageResponse.builder()
                .id(message.getId())
                .sender(message.getSender())
                .senderType(message.getSenderType())
                .senderName(message.getSenderName())
                .content(message.getContent())
                .timestamp(message.getTimestamp().format(FORMATTER))
                .isRead(message.isRead())
                .readAt(message.getReadAt() != null ? message.getReadAt().format(FORMATTER) : null)
                .build();
    }
}

// File Path: src/main/java/com/edulink/backend/dto/response/StatusHistoryResponse.java
package com.edulink.backend.dto.response;

import com.edulink.backend.model.entity.Query.StatusHistoryEntry;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusHistoryResponse {
    
    private String status;
    private String timestamp;
    private String changedBy;
    private String changedByName;
    private String note;
    private String reason;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static StatusHistoryResponse fromStatusHistory(StatusHistoryEntry entry) {
        return StatusHistoryResponse.builder()
                .status(entry.getStatus().getValue())
                .timestamp(entry.getTimestamp().format(FORMATTER))
                .changedBy(entry.getChangedBy())
                .changedByName(entry.getChangedByName())
                .note(entry.getNote())
                .reason(entry.getReason())
                .build();
    }
}

// File Path: src/main/java/com/edulink/backend/dto/response/QueryStatsResponse.java
package com.edulink.backend.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryStatsResponse {
    
    private long totalQueries;
    private long pendingQueries;
    private long inProgressQueries;
    private long resolvedQueries;
    private long unreadQueries;
    private long highPriorityQueries;
    
    // Category breakdown
    private CategoryStats categoryStats;
    
    // Recent activity
    private long queriesThisWeek;
    private long queriesThisMonth;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryStats {
        private long academic;
        private long technical;
        private long administrative;
        private long appointment;
        private long courseRelated;
    }
}