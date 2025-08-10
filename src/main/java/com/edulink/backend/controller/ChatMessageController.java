// src/main/java/com/edulink/backend/controller/ChatMessageController.java
package com.edulink.backend.controller;

import com.edulink.backend.dto.websocket.ChatMessageDTO;
import com.edulink.backend.model.entity.Conversation;
import com.edulink.backend.repository.ConversationRepository;
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

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDTO chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        try {
            log.info("Received message: {}", chatMessage);
            
            // Find the conversation
            Conversation conversation = conversationRepository.findById(chatMessage.getConversationId())
                    .orElseThrow(() -> new RuntimeException("Conversation not found"));

            // Verify sender is participant
            if (!conversation.getParticipantIds().contains(chatMessage.getSenderId())) {
                throw new SecurityException("Sender is not a participant in this conversation");
            }

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

            // Save conversation
            conversationRepository.save(conversation);

            // Send message to all participants via WebSocket
            messagingTemplate.convertAndSend(
                "/topic/chat/" + chatMessage.getConversationId(), 
                newMessage
            );

            log.info("Message sent successfully to conversation: {}", chatMessage.getConversationId());

        } catch (Exception e) {
            log.error("Error sending message: ", e);
            // You might want to send error message back to sender
            messagingTemplate.convertAndSendToUser(
                chatMessage.getSenderId(), 
                "/queue/errors", 
                "Failed to send message: " + e.getMessage()
            );
        }
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessageDTO chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        // Add user to WebSocket session
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSenderId());
        
        log.info("User {} joined conversation {}", chatMessage.getSenderId(), chatMessage.getConversationId());
    }
}