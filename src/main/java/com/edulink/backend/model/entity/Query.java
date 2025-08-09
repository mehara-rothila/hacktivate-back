// File Path: src/main/java/com/edulink/backend/model/entity/Query.java
package com.edulink.backend.model.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Document(collection = "queries")
public class Query {
    
    @Id
    private String id;
    
    @Indexed
    private String studentId;
    
    @Indexed
    private String lecturerId;
    
    private String title;
    
    private String description;
    
    @Indexed
    private QueryCategory category;
    
    @Indexed
    private QueryPriority priority;
    
    @Indexed
    private QueryStatus status;
    
    private String course;
    
    @Indexed
    private LocalDateTime submittedAt;
    
    @Indexed
    private LocalDateTime lastUpdated;
    
    private List<QueryMessage> messages = new ArrayList<>();
    
    private List<StatusHistoryEntry> statusHistory = new ArrayList<>();
    
    @Indexed
    private boolean readByLecturer = false;
    
    @Indexed
    private boolean readByStudent = false;
    
    private List<Attachment> attachments = new ArrayList<>();
    
    private LocalDateTime autoCloseAt;
    
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Constructors
    public Query() {}

    public Query(String studentId, String lecturerId, String title, String description, 
                 QueryCategory category, QueryPriority priority, String course) {
        this.studentId = studentId;
        this.lecturerId = lecturerId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.course = course;
        this.status = QueryStatus.PENDING;
        this.submittedAt = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        // Add initial status history entry
        this.statusHistory.add(new StatusHistoryEntry(
            QueryStatus.PENDING, 
            LocalDateTime.now(), 
            "System", 
            "Query submitted"
        ));
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getLecturerId() { return lecturerId; }
    public void setLecturerId(String lecturerId) { this.lecturerId = lecturerId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public QueryCategory getCategory() { return category; }
    public void setCategory(QueryCategory category) { this.category = category; }

    public QueryPriority getPriority() { return priority; }
    public void setPriority(QueryPriority priority) { this.priority = priority; }

    public QueryStatus getStatus() { return status; }
    public void setStatus(QueryStatus status) { this.status = status; }

    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    public List<QueryMessage> getMessages() { return messages; }
    public void setMessages(List<QueryMessage> messages) { this.messages = messages; }

    public List<StatusHistoryEntry> getStatusHistory() { return statusHistory; }
    public void setStatusHistory(List<StatusHistoryEntry> statusHistory) { this.statusHistory = statusHistory; }

    public boolean isReadByLecturer() { return readByLecturer; }
    public void setReadByLecturer(boolean readByLecturer) { this.readByLecturer = readByLecturer; }

    public boolean isReadByStudent() { return readByStudent; }
    public void setReadByStudent(boolean readByStudent) { this.readByStudent = readByStudent; }

    public List<Attachment> getAttachments() { return attachments; }
    public void setAttachments(List<Attachment> attachments) { this.attachments = attachments; }

    public LocalDateTime getAutoCloseAt() { return autoCloseAt; }
    public void setAutoCloseAt(LocalDateTime autoCloseAt) { this.autoCloseAt = autoCloseAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public void addMessage(QueryMessage message) {
        this.messages.add(message);
        this.lastUpdated = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void addStatusHistory(QueryStatus newStatus, String changedBy, String note) {
        this.statusHistory.add(new StatusHistoryEntry(newStatus, LocalDateTime.now(), changedBy, note));
        this.status = newStatus;
        this.lastUpdated = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public int getResponseCount() {
        return messages.size();
    }

    // Nested classes for embedded documents
    public static class QueryMessage {
        private String id;
        private String sender; // User ID
        private String senderType; // STUDENT, LECTURER
        private String senderName;
        private String content;
        private LocalDateTime timestamp;
        private List<Attachment> attachments = new ArrayList<>();
        private boolean isRead = false;
        private LocalDateTime readAt;

        // Constructors
        public QueryMessage() {}

        public QueryMessage(String sender, String senderType, String senderName, String content) {
            this.id = java.util.UUID.randomUUID().toString();
            this.sender = sender;
            this.senderType = senderType;
            this.senderName = senderName;
            this.content = content;
            this.timestamp = LocalDateTime.now();
        }

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getSender() { return sender; }
        public void setSender(String sender) { this.sender = sender; }

        public String getSenderType() { return senderType; }
        public void setSenderType(String senderType) { this.senderType = senderType; }

        public String getSenderName() { return senderName; }
        public void setSenderName(String senderName) { this.senderName = senderName; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public List<Attachment> getAttachments() { return attachments; }
        public void setAttachments(List<Attachment> attachments) { this.attachments = attachments; }

        public boolean isRead() { return isRead; }
        public void setRead(boolean read) { isRead = read; }

        public LocalDateTime getReadAt() { return readAt; }
        public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
    }

    public static class StatusHistoryEntry {
        private QueryStatus status;
        private LocalDateTime timestamp;
        private String changedBy;
        private String changedByName;
        private String note;
        private String reason;

        // Constructors
        public StatusHistoryEntry() {}

        public StatusHistoryEntry(QueryStatus status, LocalDateTime timestamp, String changedBy, String note) {
            this.status = status;
            this.timestamp = timestamp;
            this.changedBy = changedBy;
            this.changedByName = changedBy; // Default to same as changedBy
            this.note = note;
        }

        // Getters and Setters
        public QueryStatus getStatus() { return status; }
        public void setStatus(QueryStatus status) { this.status = status; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public String getChangedBy() { return changedBy; }
        public void setChangedBy(String changedBy) { this.changedBy = changedBy; }

        public String getChangedByName() { return changedByName; }
        public void setChangedByName(String changedByName) { this.changedByName = changedByName; }

        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class Attachment {
        private String id;
        private String name;
        private String originalName;
        private String url;
        private String type;
        private String mimeType;
        private long size;
        private String uploadedBy;
        private LocalDateTime uploadedAt;

        // Constructors
        public Attachment() {}

        public Attachment(String name, String originalName, String url, String type, String mimeType, long size, String uploadedBy) {
            this.id = java.util.UUID.randomUUID().toString();
            this.name = name;
            this.originalName = originalName;
            this.url = url;
            this.type = type;
            this.mimeType = mimeType;
            this.size = size;
            this.uploadedBy = uploadedBy;
            this.uploadedAt = LocalDateTime.now();
        }

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getOriginalName() { return originalName; }
        public void setOriginalName(String originalName) { this.originalName = originalName; }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getMimeType() { return mimeType; }
        public void setMimeType(String mimeType) { this.mimeType = mimeType; }

        public long getSize() { return size; }
        public void setSize(long size) { this.size = size; }

        public String getUploadedBy() { return uploadedBy; }
        public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }

        public LocalDateTime getUploadedAt() { return uploadedAt; }
        public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    }

    // Enums
    public enum QueryCategory {
        ACADEMIC("Academic"),
        TECHNICAL("Technical"), 
        ADMINISTRATIVE("Administrative"),
        APPOINTMENT("Appointment"),
        COURSE_RELATED("Course-related");

        private final String displayName;

        QueryCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum QueryPriority {
        LOW("low"),
        MEDIUM("medium"),
        HIGH("high");

        private final String value;

        QueryPriority(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum QueryStatus {
        PENDING("pending"),
        IN_PROGRESS("in-progress"),
        RESOLVED("resolved"),
        CLOSED("closed");

        private final String value;

        QueryStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}