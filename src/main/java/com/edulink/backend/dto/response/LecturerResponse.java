
// ============================================================================
// dto/response/LecturerResponse.java
// ============================================================================
package com.edulink.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LecturerResponse {
    private String id;
    private String name;
    private String title;
    private String email;
    private String phone;
    private String avatar;
    private String department;
    private List<String> specialization;
    private List<CourseInfo> courses;
    private String officeLocation;
    private List<String> officeHours;
    private String biography;
    private List<String> researchInterests;
    private Integer publications;
    private Integer yearsExperience;
    private Double rating;
    private String responseTime;
    private String availability;
    private String lastActive;
    private String preferredContactMethod;
    
    @Data
    @Builder
    public static class CourseInfo {
        private String code;
        private String name;
        private String semester;
    }
}

// ============================================================================
// Enhanced Message Entity (ADD TO YOUR EXISTING Message.java)
// ============================================================================
// Add these to your existing Message.java entity:

/*
@Document(collection = "messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    
    @Id
    private String id;
    
    @Indexed
    private String studentId;
    
    @Indexed
    private String lecturerId;
    
    private String subject;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    
    @Indexed
    private MessageType messageType;
    
    @Indexed
    private Priority priority;
    
    @Indexed
    private Status status = Status.ACTIVE;
    
    private boolean readByStudent = false;
    private boolean readByLecturer = false;
    private Integer unreadCount = 0;
    
    private String course;
    
    @Builder.Default
    private List<MessageThread> messages = new ArrayList<>();
    
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    public enum MessageType {
        ACADEMIC, ADMINISTRATIVE, GENERAL
    }
    
    public enum Priority {
        LOW, MEDIUM, HIGH
    }
    
    public enum Status {
        ACTIVE, RESOLVED, ARCHIVED
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageThread {
        private String id;
        private String sender; // "student" or "lecturer"
        private String senderName;
        private String content;
        private LocalDateTime timestamp;
        
        @Builder.Default
        private List<Attachment> attachments = new ArrayList<>();
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Attachment {
            private String name;
            private String url;
            private String type;
        }
    }
}
*/