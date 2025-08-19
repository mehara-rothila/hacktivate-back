// src/main/java/com/edulink/backend/model/entity/LostFoundComment.java
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

@Document(collection = "lost_found_comments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LostFoundComment {
    
    @Id
    private String id;
    
    @Indexed
    private String lostFoundItemId; // Reference to the lost/found item
    
    @Indexed
    private String userId; // Who posted this comment
    
    private String userName; // Name of the commenter
    
    private String userAvatar; // Avatar of the commenter
    
    private String content; // Comment content
    
    @Builder.Default
    private CommentType type = CommentType.COMMENT;
    
    private String contactInfo; // Optional contact info for claims
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    // Enum for comment types
    public enum CommentType {
        COMMENT,    // Regular comment
        CLAIM,      // Claiming the item (for found items)
        INFO        // Additional information
    }
    
    // Helper methods
    public boolean isClaim() {
        return type == CommentType.CLAIM;
    }
    
    public boolean canBeEditedBy(String currentUserId) {
        return userId.equals(currentUserId);
    }
    
    public boolean canBeDeletedBy(String currentUserId, String userRole) {
        // Original commenter can delete, or lecturers can moderate
        return userId.equals(currentUserId) || "LECTURER".equalsIgnoreCase(userRole);
    }
}