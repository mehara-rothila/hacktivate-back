// src/main/java/com/edulink/backend/controller/ChatMessageController.java
package com.edulink.backend.controller;

import com.edulink.backend.dto.websocket.ChatMessageDTO;
import com.edulink.backend.model.entity.Conversation;
import com.edulink.backend.model.entity.Message;
import com.edulink.backend.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ConversationRepository conversationRepository;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDTO chatMessageDTO) {
        // 1. Find the conversation in the database
        Conversation conversation = conversationRepository.findById(chatMessageDTO.getConversationId())
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // 2. Create a new Message entity
        Message newMessage = Message.builder()
                .id(UUID.randomUUID().toString()) // Generate a unique ID for the message
                .senderId(chatMessageDTO.getSenderId())
                .content(chatMessageDTO.getContent())
                .timestamp(LocalDateTime.now())
                .isRead(false) // New messages are always unread initially
                .build();

        // 3. Add the new message to the conversation's message list
        conversation.getMessages().add(newMessage);

        // 4. Update the conversation's tracking fields
        conversation.setLastMessageContent(newMessage.getContent());
        conversation.setLastMessageAt(newMessage.getTimestamp());
        conversation.setLastMessageSenderId(newMessage.getSenderId());
        conversation.setStatus(Conversation.Status.ACTIVE); // Mark as active on new message

        // 5. Save the updated conversation back to the database
        conversationRepository.save(conversation);

        // 6. Broadcast the new message to all clients subscribed to this conversation's topic
        // The destination is /topic/chat/{conversationId}
        messagingTemplate.convertAndSend("/topic/chat/" + conversation.getId(), newMessage);
    }
}