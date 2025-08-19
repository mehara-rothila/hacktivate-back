// src/main/java/com/edulink/backend/model/entity/LostFoundItem.java
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

@Document(collection = "lost_found_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LostFoundItem {
    
    @Id
    private String id;
    
    @Indexed
    private String title;
    
    private String description;
    
    @Indexed
    private ItemType type;
    
    @Indexed
    @Builder.Default
    private ItemStatus status = ItemStatus.OPEN;
    
    @Indexed
    private String userId; // Who posted this item
    
    private String userName; // Name of the user who posted
    
    private String userAvatar; // Avatar of the user who posted
    
    private String location; // Where it was lost/found
    
    private LocalDateTime lostFoundDateTime; // When the item was actually lost or found
    
    private String contactInfo; // Optional contact information
    
    private ImageAttachment image;
    
    private String resolvedBy; // User ID who resolved/claimed the item
    
    private String resolvedByName; // Name of user who resolved
    
    private LocalDateTime resolvedAt;
    
    @Builder.Default
    private List<LostFoundComment> comments = new ArrayList<>();
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    // Enums
    public enum ItemType {
        LOST, FOUND
    }
    
    public enum ItemStatus {
        OPEN, RESOLVED, ARCHIVED
    }
    
    // Embedded class for image attachment
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageAttachment {
        private String filename;
        private String originalFilename;
        private String contentType;
        private Long size;
    }
    
    // Helper methods
    public boolean isResolved() {
        return status == ItemStatus.RESOLVED;
    }
    
    public boolean isOpen() {
        return status == ItemStatus.OPEN;
    }
    
    public boolean canBeResolvedBy(String currentUserId) {
        // Only allow resolution if it's open and not posted by the same user
        return status == ItemStatus.OPEN && !userId.equals(currentUserId);
    }
    
    public boolean canBeEditedBy(String currentUserId) {
        // Only the original poster can edit, and only if it's still open
        return userId.equals(currentUserId) && status == ItemStatus.OPEN;
    }
    
    public boolean canBeDeletedBy(String currentUserId, String userRole) {
        // Original poster can delete if open, or lecturers can moderate (delete any)
        return (userId.equals(currentUserId) && status == ItemStatus.OPEN) || 
               "LECTURER".equalsIgnoreCase(userRole);
    }
    
    public int getCommentCount() {
        return comments != null ? comments.size() : 0;
    }
}