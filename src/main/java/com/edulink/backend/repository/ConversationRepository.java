// src/main/java/com/edulink/backend/repository/ConversationRepository.java
package com.edulink.backend.repository;

import com.edulink.backend.model.entity.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {

    /**
     * Finds all conversations that a specific user is a participant in.
     * The results are ordered by the last message timestamp in descending order (most recent first).
     *
     * @param userId The ID of the user.
     * @return A list of conversations.
     */
    List<Conversation> findByParticipantIdsContainingOrderByLastMessageAtDesc(String userId);

    // ARCHIVE: Methods to find conversations for a user that are NOT archived by them.
    List<Conversation> findByParticipantIdsContainingAndArchivedByUserIdsNotContainingOrderByLastMessageAtDesc(String participantId, String archivedById);

    // ARCHIVE: Methods to find conversations for a user that ARE archived by them.
    List<Conversation> findByParticipantIdsContainingAndArchivedByUserIdsContainingOrderByLastMessageAtDesc(String participantId, String archivedById);

    // DELETE: New method to find conversations for a user that are NOT deleted by them (excluding deleted)
    List<Conversation> findByParticipantIdsContainingAndDeletedByUserIdsNotContainingOrderByLastMessageAtDesc(String participantId, String deletedById);
    
    // DELETE: Combined method to find conversations that are NOT archived AND NOT deleted by the user
    List<Conversation> findByParticipantIdsContainingAndArchivedByUserIdsNotContainingAndDeletedByUserIdsNotContainingOrderByLastMessageAtDesc(
        String participantId, String archivedById, String deletedById);
    
    // DELETE: Combined method to find conversations that ARE archived but NOT deleted by the user
    List<Conversation> findByParticipantIdsContainingAndArchivedByUserIdsContainingAndDeletedByUserIdsNotContainingOrderByLastMessageAtDesc(
        String participantId, String archivedById, String deletedById);
}