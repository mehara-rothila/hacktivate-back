// src/main/java/com/edulink/backend/controller/ChatMessageController.java
package com.edulink.backend.controller;

import com.edulink.backend.dto.websocket.ChatMessageDTO;
import com.edulink.backend.model.entity.Conversation;
import com.edulink.backend.model.entity.User;
import com.edulink.backend.repository.ConversationRepository;
import com.edulink.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatMessageController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ConversationRepository conversationRepository;
    private final UserService userService;

    /**
     * Handle incoming chat messages via WebSocket
     * Client sends to: /app/chat/{conversationId}
     * Message is broadcast to: /topic/conversation/{conversationId}
     */
    @MessageMapping("/chat/{conversationId}")
    public void sendMessage(@DestinationVariable String conversationId,
                           @Payload ChatMessageDTO chatMessage,
                           SimpMessageHeaderAccessor headerAccessor,
                           Principal principal) {
        try {
            log.info("📨 Received message for conversation: {}", conversationId);
            
            // Get current user from JWT auth
            User currentUser = userService.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

            // Find the conversation
            Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

            // Verify user is participant
            if (!conversation.getParticipantIds().contains(currentUser.getId())) {
                log.warn("⚠️ User {} is not a participant in conversation {}", currentUser.getId(), conversationId);
                return;
            }

            // Create new message
            Conversation.Message newMessage = Conversation.Message.builder()
                .id(UUID.randomUUID().toString())
                .senderId(currentUser.getId())
                .content(chatMessage.getContent())
                .timestamp(LocalDateTime.now())
                .isRead(false)
                .build();

            // Add message to conversation
            conversation.getMessages().add(newMessage);
            conversation.setLastMessageContent(newMessage.getContent());
            conversation.setLastMessageAt(newMessage.getTimestamp());
            conversation.setLastMessageSenderId(newMessage.getSenderId());

            // Unarchive conversation for all participants when new message is sent
            conversation.getArchivedByUserIds().clear();

            // Save conversation
            conversationRepository.save(conversation);

            // Prepare message with sender info for broadcast
            ChatMessageDTO responseMessage = ChatMessageDTO.builder()
                .conversationId(conversationId)
                .senderId(currentUser.getId())
                .content(chatMessage.getContent())
                .type(ChatMessageDTO.MessageType.CHAT)
                .build();

            // Broadcast to all participants in the conversation
            messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, responseMessage);
            
            log.info("✅ Message sent successfully to conversation: {}", conversationId);

        } catch (Exception e) {
            log.error("❌ Error sending message: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle user joining a conversation
     * Client sends to: /app/chat.join/{conversationId}
     */
    @MessageMapping("/chat.join/{conversationId}")
    public void joinConversation(@DestinationVariable String conversationId,
                                @Payload ChatMessageDTO chatMessage,
                                SimpMessageHeaderAccessor headerAccessor,
                                Principal principal) {
        try {
            User currentUser = userService.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

            // Verify user is participant in this conversation
            Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

            if (!conversation.getParticipantIds().contains(currentUser.getId())) {
                log.warn("⚠️ User {} tried to join conversation {} but is not a participant", 
                    currentUser.getId(), conversationId);
                return;
            }

            // Store conversation ID in WebSocket session
            headerAccessor.getSessionAttributes().put("conversationId", conversationId);
            headerAccessor.getSessionAttributes().put("userId", currentUser.getId());

            // Broadcast join message
            ChatMessageDTO joinMessage = ChatMessageDTO.builder()
                .conversationId(conversationId)
                .senderId(currentUser.getId())
                .content(currentUser.getFullName() + " joined the conversation")
                .type(ChatMessageDTO.MessageType.JOIN)
                .build();

            messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, joinMessage);
            log.info("👤 User {} joined conversation: {}", currentUser.getId(), conversationId);

        } catch (Exception e) {
            log.error("❌ Error joining conversation: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle user leaving a conversation
     * Client sends to: /app/chat.leave/{conversationId}
     */
    @MessageMapping("/chat.leave/{conversationId}")
    public void leaveConversation(@DestinationVariable String conversationId,
                                 @Payload ChatMessageDTO chatMessage,
                                 SimpMessageHeaderAccessor headerAccessor,
                                 Principal principal) {
        try {
            User currentUser = userService.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

            // Broadcast leave message
            ChatMessageDTO leaveMessage = ChatMessageDTO.builder()
                .conversationId(conversationId)
                .senderId(currentUser.getId())
                .content(currentUser.getFullName() + " left the conversation")
                .type(ChatMessageDTO.MessageType.LEAVE)
                .build();

            messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, leaveMessage);
            log.info("👋 User {} left conversation: {}", currentUser.getId(), conversationId);

        } catch (Exception e) {
            log.error("❌ Error leaving conversation: {}", e.getMessage(), e);
        }
    }
}