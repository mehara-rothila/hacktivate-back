// src/main/java/com/edulink/backend/repository/LostFoundItemRepository.java
package com.edulink.backend.repository;

import com.edulink.backend.model.entity.LostFoundItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LostFoundItemRepository extends MongoRepository<LostFoundItem, String> {
    
    // Find all items ordered by creation date (newest first)
    List<LostFoundItem> findAllByOrderByCreatedAtDesc();
    
    // Find items by type
    List<LostFoundItem> findByTypeOrderByCreatedAtDesc(LostFoundItem.ItemType type);
    
    // Find items by status
    List<LostFoundItem> findByStatusOrderByCreatedAtDesc(LostFoundItem.ItemStatus status);
    
    // Find items by type and status
    List<LostFoundItem> findByTypeAndStatusOrderByCreatedAtDesc(
        LostFoundItem.ItemType type, 
        LostFoundItem.ItemStatus status
    );
    
    // Find items by user
    List<LostFoundItem> findByUserIdOrderByCreatedAtDesc(String userId);
    
    // Search items by title or description (case-insensitive)
    @Query("{'$or': [{'title': {'$regex': ?0, '$options': 'i'}}, {'description': {'$regex': ?0, '$options': 'i'}}]}")
    List<LostFoundItem> searchByTitleOrDescription(String query);
    
    // Search with filters
    @Query("{'$and': [" +
           "{'$or': [{'type': {'$exists': false}}, {'type': ?0}]}, " +
           "{'$or': [{'status': {'$exists': false}}, {'status': ?1}]}, " +
           "{'$or': [" +
           "{'$expr': {'$eq': [?2, null]}}, " +
           "{'$expr': {'$eq': [?2, '']}}, " +
           "{'title': {'$regex': ?2, '$options': 'i'}}, " +
           "{'description': {'$regex': ?2, '$options': 'i'}}" +
           "]}" +
           "]}")
    List<LostFoundItem> findWithFilters(
        LostFoundItem.ItemType type,
        LostFoundItem.ItemStatus status,
        String query
    );
    
    // Count items by status
    long countByStatus(LostFoundItem.ItemStatus status);
    
    // Count items by type
    long countByType(LostFoundItem.ItemType type);
    
    // Find recent items (within last N days)
    @Query("{'createdAt': {'$gte': ?0}}")
    List<LostFoundItem> findRecentItems(LocalDateTime since);
}
