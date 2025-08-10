// src/main/java/com/edulink/backend/model/entity/Conversation.java
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Document(collection = "conversations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    private String id;

    @Indexed
    private Set<String> participantIds; // Set of User IDs (student and lecturer)

    private String subject;
    private String courseId; // Optional: link to a course

    @Indexed
    private Status status;
    private Priority priority;

    // --- Embedded Messages ---
    private List<Message> messages = new ArrayList<>();

    // --- Tracking Fields ---
    private String lastMessageContent;
    private LocalDateTime lastMessageAt;
    private String lastMessageSenderId;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum Status {
        ACTIVE,
        RESOLVED,
        ARCHIVED
    }

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH
    }
}