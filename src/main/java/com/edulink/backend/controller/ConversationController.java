// src/main/java/com/edulink/backend/controller/ConversationController.java
package com.edulink.backend.controller;

import com.edulink.backend.dto.response.ApiResponse;
import com.edulink.backend.dto.response.ConversationDTO;
import com.edulink.backend.dto.response.UserProfileResponse;
import com.edulink.backend.model.entity.Conversation;
import com.edulink.backend.model.entity.User;
import com.edulink.backend.repository.ConversationRepository;
import com.edulink.backend.repository.UserRepository;
import com.edulink.backend.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    // DTO Classes
    @Data
    static class StartConversationRequest {
        @NotBlank
        private String recipientId;
        @NotBlank
        private String subject;
        @NotBlank
        private String initialMessage;
        private String courseId;
    }

    @Data
    static class SendMessageRequest {
        @NotBlank
        private String content;
        private List<String> attachmentIds;
    }

    @Data
    static class UpdateStatusRequest {
        @NotBlank
        private String status; // ACTIVE, RESOLVED, ARCHIVED
    }

    @Data
    static class UpdatePriorityRequest {
        @NotBlank
        private String priority; // LOW, MEDIUM, HIGH
    }

    // =================== CREATE CONVERSATION ===================
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> startConversation(@Valid @RequestBody StartConversationRequest request) {
        User currentUser = userService.getCurrentUser();
        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        // Create participant set
        Set<String> participantIds = new HashSet<>();
        participantIds.add(currentUser.getId());
        participantIds.add(recipient.getId());

        // Create the first message - FIXED: Using Conversation.Message
        Conversation.Message firstMessage = Conversation.Message.builder()
                .id(UUID.randomUUID().toString())
                .senderId(currentUser.getId())
                .content(request.getInitialMessage())
                .timestamp(LocalDateTime.now())
                .isRead(false)
                .build();

        // Create the conversation
        Conversation conversation = Conversation.builder()
                .participantIds(participantIds)
                .subject(request.getSubject())
                .courseId(request.getCourseId())
                .status(Conversation.Status.ACTIVE)
                .priority(Conversation.Priority.MEDIUM)
                .lastMessageContent(firstMessage.getContent())
                .lastMessageAt(firstMessage.getTimestamp())
                .lastMessageSenderId(firstMessage.getSenderId())
                .build();
        conversation.getMessages().add(firstMessage);

        Conversation savedConversation = conversationRepository.save(conversation);

        return new ResponseEntity<>(
            ApiResponse.builder()
                .success(true)
                .message("Conversation started successfully.")
                .data(mapToConversationDTO(savedConversation, currentUser.getId()))
                .build(), 
            HttpStatus.CREATED
        );
    }

    // =================== GET ALL CONVERSATIONS ===================
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> getMyConversations() {
        User currentUser = userService.getCurrentUser();
        List<Conversation> conversations = conversationRepository.findByParticipantIdsContainingOrderByLastMessageAtDesc(currentUser.getId());

        List<ConversationDTO> conversationDTOs = conversations.stream()
                .map(convo -> mapToConversationDTO(convo, currentUser.getId()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(
            ApiResponse.builder()
                .success(true)
                .message("Conversations retrieved successfully.")
                .data(conversationDTOs)
                .build()
        );
    }

    // =================== GET SPECIFIC CONVERSATION ===================
    @GetMapping("/{conversationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> getConversation(@PathVariable String conversationId) {
        User currentUser = userService.getCurrentUser();
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        if (!conversation.getParticipantIds().contains(currentUser.getId())) {
            throw new SecurityException("User is not a participant in this conversation.");
        }

        ConversationDTO conversationDTO = mapToConversationDTO(conversation, currentUser.getId());
        
        return ResponseEntity.ok(
            ApiResponse.builder()
                .success(true)
                .message("Conversation retrieved successfully.")
                .data(conversationDTO)
                .build()
        );
    }

    // =================== GET MESSAGES FOR CONVERSATION ===================
    @GetMapping("/{conversationId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> getMessagesForConversation(@PathVariable String conversationId) {
        User currentUser = userService.getCurrentUser();
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        if (!conversation.getParticipantIds().contains(currentUser.getId())) {
            throw new SecurityException("User is not a participant in this conversation.");
        }

        // FIXED: Return Conversation.Message list directly
        List<Conversation.Message> messages = conversation.getMessages();
        return ResponseEntity.ok(
            ApiResponse.builder()
                .success(true)
                .message("Messages retrieved successfully.")
                .data(messages)
                .build()
        );
    }

    // =================== SEND MESSAGE TO EXISTING CONVERSATION ===================
    @PostMapping("/{conversationId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> sendMessage(
            @PathVariable String conversationId, 
            @Valid @RequestBody SendMessageRequest request) {
        
        User currentUser = userService.getCurrentUser();
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        if (!conversation.getParticipantIds().contains(currentUser.getId())) {
            throw new SecurityException("User is not a participant in this conversation.");
        }

        // Create new message - FIXED: Using Conversation.Message
        Conversation.Message newMessage = Conversation.Message.builder()
                .id(UUID.randomUUID().toString())
                .senderId(currentUser.getId())
                .content(request.getContent())
                .timestamp(LocalDateTime.now())
                .isRead(false)
                .build();

        // Add attachments if provided
        if (request.getAttachmentIds() != null && !request.getAttachmentIds().isEmpty()) {
            List<Conversation.Message.Attachment> attachments = request.getAttachmentIds().stream()
                    .map(attachmentId -> Conversation.Message.Attachment.builder()
                            .resourceId(attachmentId)
                            .originalFilename("attachment") // You might want to get this from resource service
                            .build())
                    .collect(Collectors.toList());
            newMessage.setAttachments(attachments);
        }

        // Add message to conversation and update last message info
        conversation.getMessages().add(newMessage);
        conversation.setLastMessageContent(newMessage.getContent());
        conversation.setLastMessageAt(newMessage.getTimestamp());
        conversation.setLastMessageSenderId(newMessage.getSenderId());

        Conversation savedConversation = conversationRepository.save(conversation);

        return ResponseEntity.ok(
            ApiResponse.builder()
                .success(true)
                .message("Message sent successfully.")
                .data(newMessage)
                .build()
        );
    }

    // =================== MARK MESSAGE AS READ ===================
    @PutMapping("/{conversationId}/messages/{messageId}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> markMessageAsRead(
            @PathVariable String conversationId,
            @PathVariable String messageId) {
        
        User currentUser = userService.getCurrentUser();
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        if (!conversation.getParticipantIds().contains(currentUser.getId())) {
            throw new SecurityException("User is not a participant in this conversation.");
        }

        // Find and mark message as read
        conversation.getMessages().stream()
                .filter(message -> message.getId().equals(messageId))
                .findFirst()
                .ifPresent(message -> message.setRead(true));

        conversationRepository.save(conversation);

        return ResponseEntity.ok(
            ApiResponse.builder()
                .success(true)
                .message("Message marked as read.")
                .build()
        );
    }

    // =================== MARK ALL MESSAGES AS READ ===================
    @PutMapping("/{conversationId}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> markAllMessagesAsRead(@PathVariable String conversationId) {
        User currentUser = userService.getCurrentUser();
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        if (!conversation.getParticipantIds().contains(currentUser.getId())) {
            throw new SecurityException("User is not a participant in this conversation.");
        }

        // Mark all messages as read for messages not sent by current user
        conversation.getMessages().stream()
                .filter(message -> !message.getSenderId().equals(currentUser.getId()))
                .forEach(message -> message.setRead(true));

        conversationRepository.save(conversation);

        return ResponseEntity.ok(
            ApiResponse.builder()
                .success(true)
                .message("All messages marked as read.")
                .build()
        );
    }

    // =================== UPDATE CONVERSATION STATUS ===================
    @PutMapping("/{conversationId}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> updateConversationStatus(
            @PathVariable String conversationId,
            @Valid @RequestBody UpdateStatusRequest request) {
        
        User currentUser = userService.getCurrentUser();
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        if (!conversation.getParticipantIds().contains(currentUser.getId())) {
            throw new SecurityException("User is not a participant in this conversation.");
        }

        try {
            Conversation.Status newStatus = Conversation.Status.valueOf(request.getStatus().toUpperCase());
            conversation.setStatus(newStatus);
            Conversation savedConversation = conversationRepository.save(conversation);

            return ResponseEntity.ok(
                ApiResponse.builder()
                    .success(true)
                    .message("Conversation status updated successfully.")
                    .data(mapToConversationDTO(savedConversation, currentUser.getId()))
                    .build()
            );
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status value: " + request.getStatus());
        }
    }

    // =================== UPDATE CONVERSATION PRIORITY ===================
    @PutMapping("/{conversationId}/priority")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> updateConversationPriority(
            @PathVariable String conversationId,
            @Valid @RequestBody UpdatePriorityRequest request) {
        
        User currentUser = userService.getCurrentUser();
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        if (!conversation.getParticipantIds().contains(currentUser.getId())) {
            throw new SecurityException("User is not a participant in this conversation.");
        }

        try {
            Conversation.Priority newPriority = Conversation.Priority.valueOf(request.getPriority().toUpperCase());
            conversation.setPriority(newPriority);
            Conversation savedConversation = conversationRepository.save(conversation);

            return ResponseEntity.ok(
                ApiResponse.builder()
                    .success(true)
                    .message("Conversation priority updated successfully.")
                    .data(mapToConversationDTO(savedConversation, currentUser.getId()))
                    .build()
            );
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid priority value: " + request.getPriority());
        }
    }

    // =================== DELETE CONVERSATION ===================
    @DeleteMapping("/{conversationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> deleteConversation(@PathVariable String conversationId) {
        User currentUser = userService.getCurrentUser();
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        if (!conversation.getParticipantIds().contains(currentUser.getId())) {
            throw new SecurityException("User is not a participant in this conversation.");
        }

        conversationRepository.delete(conversation);

        return ResponseEntity.ok(
            ApiResponse.builder()
                .success(true)
                .message("Conversation deleted successfully.")
                .build()
        );
    }

    // =================== HELPER METHODS ===================
    private ConversationDTO mapToConversationDTO(Conversation conversation, String currentUserId) {
        String otherParticipantId = conversation.getParticipantIds().stream()
                .filter(id -> !id.equals(currentUserId))
                .findFirst()
                .orElse(null);

        UserProfileResponse otherParticipantProfile = userRepository.findById(otherParticipantId)
                .map(UserService::mapToUserProfileResponse)
                .orElse(null);

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