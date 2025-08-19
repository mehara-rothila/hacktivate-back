// src/main/java/com/edulink/backend/repository/LostFoundCommentRepository.java
package com.edulink.backend.repository;

import com.edulink.backend.model.entity.LostFoundComment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LostFoundCommentRepository extends MongoRepository<LostFoundComment, String> {
    
    // Find comments by lost/found item
    List<LostFoundComment> findByLostFoundItemIdOrderByCreatedAtAsc(String lostFoundItemId);
    
    // Find comments by user
    List<LostFoundComment> findByUserIdOrderByCreatedAtDesc(String userId);
    
    // Find claims for an item
    List<LostFoundComment> findByLostFoundItemIdAndTypeOrderByCreatedAtAsc(
        String lostFoundItemId, 
        LostFoundComment.CommentType type
    );
    
    // Count comments for an item
    long countByLostFoundItemId(String lostFoundItemId);
    
    // Count claims for an item
    long countByLostFoundItemIdAndType(String lostFoundItemId, LostFoundComment.CommentType type);
    
    // Check if user has already commented on an item
    boolean existsByLostFoundItemIdAndUserId(String lostFoundItemId, String userId);
    
    // Check if user has already claimed an item
    boolean existsByLostFoundItemIdAndUserIdAndType(
        String lostFoundItemId, 
        String userId, 
        LostFoundComment.CommentType type
    );
}