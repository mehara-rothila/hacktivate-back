// ==================================================
// 2. src/main/java/com/edulink/backend/controller/ChatMessageController.java - DISABLED VERSION
// ==================================================
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

// TEMPORARILY DISABLED TO FIX STARTUP ISSUES
//@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatMessageController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDTO chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        log.info("📨 WebSocket message received (disabled): {}", chatMessage);
        // WebSocket messaging is temporarily disabled
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessageDTO chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        log.info("🚪 WebSocket user join (disabled): {}", chatMessage.getSenderId());
        // WebSocket messaging is temporarily disabled
    }
}
