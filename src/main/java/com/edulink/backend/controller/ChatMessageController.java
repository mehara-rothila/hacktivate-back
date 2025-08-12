// src/main/java/com/edulink/backend/controller/ChatMessageController.java
package com.edulink.backend.controller;

import com.edulink.backend.dto.websocket.ChatMessageDTO;
import com.edulink.backend.model.entity.Conversation;
import com.edulink.backend.model.entity.User;
import com.edulink.backend.repository.ConversationRepository;
import com.edulink.backend.repository.UserRepository;
import com.edulink.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatMessageController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDTO chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        try {
            log.info("Received WebSocket message: {}", chatMessage);
            
            // ADDED: Basic authentication check (optional, but recommended)
            String token = extractTokenFromHeaders(headerAccessor);
            if (token != null && !jwtUtil.isTokenValid(token)) {
                log.warn("Invalid JWT token in WebSocket message");
                // You could throw an exception here if you want strict auth
            }
            
            // Find the conversation
            Conversation conversation = conversationRepository.findById(chatMessage.getConversationId())
                    .orElseThrow(() -> new RuntimeException("Conversation not found: " + chatMessage.getConversationId()));

            // Verify sender is participant
            if (!conversation.getParticipantIds().contains(chatMessage.getSenderId())) {
                log.warn("Sender {} is not a participant in conversation {}", 
                    chatMessage.getSenderId(), chatMessage.getConversationId());
                throw new SecurityException("Sender is not a participant in this conversation");
            }

            // ADDED: Verify sender exists
            User sender = userRepository.findById(chatMessage.getSenderId())
                    .orElseThrow(() -> new RuntimeException("Sender not found: " + chatMessage.getSenderId()));

            // Create new message
            Conversation.Message newMessage = Conversation.Message.builder()
                    .id(UUID.randomUUID().toString())
                    .senderId(chatMessage.getSenderId())
                    .content(chatMessage.getContent())
                    .timestamp(LocalDateTime.now())
                    .isRead(false)
                    .build();

            // Add message to conversation
            conversation.getMessages().add(newMessage);
            
            // Update conversation last message info
            conversation.setLastMessageContent(newMessage.getContent());
            conversation.setLastMessageAt(newMessage.getTimestamp());
            conversation.setLastMessageSenderId(newMessage.getSenderId());

            // ADDED: Update conversation timestamp
            conversation.setUpdatedAt(LocalDateTime.now());

            // Save conversation
            Conversation savedConversation = conversationRepository.save(conversation);

            // IMPROVED: Send message to all participants via WebSocket with better structure
            messagingTemplate.convertAndSend(
                "/topic/chat/" + chatMessage.getConversationId(), 
                newMessage
            );

            log.info("Message sent successfully to conversation: {} from user: {}", 
                chatMessage.getConversationId(), sender.getEmail());

        } catch (Exception e) {
            log.error("Error sending WebSocket message: ", e);
            
            // IMPROVED: Send detailed error back to sender
            try {
                messagingTemplate.convertAndSendToUser(
                    chatMessage.getSenderId(), 
                    "/queue/errors", 
                    "Failed to send message: " + e.getMessage()
                );
            } catch (Exception sendError) {
                log.error("Failed to send error message to user: ", sendError);
            }
        }
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessageDTO chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        try {
            log.info("User {} joining conversation {}", chatMessage.getSenderId(), chatMessage.getConversationId());
            
            // Add user to WebSocket session
            headerAccessor.getSessionAttributes().put("userId", chatMessage.getSenderId());
            headerAccessor.getSessionAttributes().put("conversationId", chatMessage.getConversationId());
            
            // ADDED: Verify user and conversation exist
            User user = userRepository.findById(chatMessage.getSenderId())
                    .orElseThrow(() -> new RuntimeException("User not found: " + chatMessage.getSenderId()));
                    
            Conversation conversation = conversationRepository.findById(chatMessage.getConversationId())
                    .orElseThrow(() -> new RuntimeException("Conversation not found: " + chatMessage.getConversationId()));
            
            // Verify user is participant
            if (!conversation.getParticipantIds().contains(chatMessage.getSenderId())) {
                throw new SecurityException("User is not a participant in this conversation");
            }
            
            log.info("User {} ({}) successfully joined conversation {}", 
                user.getEmail(), chatMessage.getSenderId(), chatMessage.getConversationId());
            
        } catch (Exception e) {
            log.error("Error adding user to conversation: ", e);
        }
    }

    // ADDED: Helper method to extract JWT token from WebSocket headers
    private String extractTokenFromHeaders(SimpMessageHeaderAccessor headerAccessor) {
        try {
            // Try to get token from Authorization header
            String authHeader = headerAccessor.getFirstNativeHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
            
            // Try to get from session attributes (if set during connection)
            Object token = headerAccessor.getSessionAttributes().get("token");
            if (token instanceof String) {
                return (String) token;
            }
            
            return null;
        } catch (Exception e) {
            log.warn("Error extracting token from WebSocket headers: {}", e.getMessage());
            return null;
        }
    }
}