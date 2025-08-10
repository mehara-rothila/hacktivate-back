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

}