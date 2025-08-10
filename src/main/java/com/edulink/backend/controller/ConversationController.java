// src/main/java/com/edulink/backend/controller/ConversationController.java
package com.edulink.backend.controller;

import com.edulink.backend.dto.response.ApiResponse;
import com.edulink.backend.dto.response.ConversationDTO;
import com.edulink.backend.dto.response.UserProfileResponse;
import com.edulink.backend.model.entity.Conversation;
import com.edulink.backend.model.entity.Message;
import com.edulink.backend.model.entity.User;
import com.edulink.backend.repository.ConversationRepository;
import com.edulink.backend.repository.UserRepository;
import com.edulink.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> getMyConversations() {
        User currentUser = userService.getCurrentUser();
        List<Conversation> conversations = conversationRepository.findByParticipantIdsContainingOrderByLastMessageAtDesc(currentUser.getId());

        // Map to DTOs
        List<ConversationDTO> conversationDTOs = conversations.stream()
                .map(convo -> mapToConversationDTO(convo, currentUser.getId()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.builder().success(true).message("Conversations retrieved successfully.").data(conversationDTOs).build());
    }

    @GetMapping("/{conversationId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> getMessagesForConversation(@PathVariable String conversationId) {
        User currentUser = userService.getCurrentUser();
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // Security check: ensure the current user is a participant
        if (!conversation.getParticipantIds().contains(currentUser.getId())) {
            throw new SecurityException("User is not a participant in this conversation.");
        }

        // In a real app, you might implement pagination here
        List<Message> messages = conversation.getMessages();

        return ResponseEntity.ok(ApiResponse.builder().success(true).message("Messages retrieved successfully.").data(messages).build());
    }

    // Helper method to convert a Conversation entity to a DTO
    private ConversationDTO mapToConversationDTO(Conversation conversation, String currentUserId) {
        // Find the ID of the *other* person in the chat
        String otherParticipantId = conversation.getParticipantIds().stream()
                .filter(id -> !id.equals(currentUserId))
                .findFirst()
                .orElse(null);

        // Fetch the other person's profile
        UserProfileResponse otherParticipantProfile = userRepository.findById(otherParticipantId)
                .map(UserService::mapToUserProfileResponse)
                .orElse(null); // Handle case where user might be deleted

        // Calculate unread message count for the current user
        long unreadCount = conversation.getMessages().stream()
                .filter(message -> !message.isRead() && !message.getSenderId().equals(currentUserId))
                .count();

        return ConversationDTO.builder()
                .id(conversation.getId())
                .otherParticipant(otherParticipantProfile)
                .subject(conversation.getSubject())
                .courseId(conversation.getCourseId())
                .status(conversation.getStatus())
                .priority(conversation.getPriority())
                .lastMessageContent(conversation.getLastMessageContent())
                .lastMessageAt(conversation.getLastMessageAt())
                .lastMessageSenderId(conversation.getLastMessageSenderId())
                .unreadCount(unreadCount)
                .build();
    }
}