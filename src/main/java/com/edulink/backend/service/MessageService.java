// MessageService.java - Complete Message Business Logic
package com.edulink.backend.service;

import com.edulink.backend.dto.request.MessageRequest;
import com.edulink.backend.dto.request.ReplyRequest;
import com.edulink.backend.dto.response.MessageResponse;
import com.edulink.backend.dto.response.MessageThreadResponse;
import com.edulink.backend.model.entity.User;
import com.edulink.backend.model.entity.Message;
import com.edulink.backend.model.entity.Course;
import com.edulink.backend.repository.MessageRepository;
import com.edulink.backend.repository.UserRepository;
import com.edulink.backend.repository.CourseRepository;
import com.edulink.backend.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {
    
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final MongoTemplate mongoTemplate;

    public List<MessageResponse> getMessagesForUser(String userId, String userRole, 
            String lecturer, String type, String priority, boolean unreadOnly, String search) {
        
        Query query = new Query();
        
        // Base criteria based on user role
        if ("STUDENT".equals(userRole)) {
            query.addCriteria(Criteria.where("studentId").is(userId));
        } else if ("LECTURER".equals(userRole)) {
            query.addCriteria(Criteria.where("lecturerId").is(userId));
        }
        
        // Apply filters
        if (lecturer != null && !lecturer.equals("All")) {
            User lecturerUser = userRepository.findByEmail(lecturer)
                .orElseThrow(() -> new ResourceNotFoundException("Lecturer not found"));
            query.addCriteria(Criteria.where("lecturerId").is(lecturerUser.getId()));
        }
        
        if (type != null && !type.equals("All")) {
            query.addCriteria(Criteria.where("messageType").is(type.toLowerCase()));
        }
        
        if (priority != null && !priority.equals("All")) {
            query.addCriteria(Criteria.where("priority").is(priority.toLowerCase()));
        }
        
        if (unreadOnly) {
            String readField = "STUDENT".equals(userRole) ? "readByStudent" : "readByLecturer";
            query.addCriteria(Criteria.where(readField).is(false));
        }
        
        if (search != null && !search.trim().isEmpty()) {
            Criteria searchCriteria = new Criteria().orOperator(
                Criteria.where("subject").regex(search, "i"),
                Criteria.where("lastMessage").regex(search, "i")
            );
            query.addCriteria(searchCriteria);
        }
        
        // Sort by unread first, then by last message time
        query.with(Sort.by(Sort.Order.desc("lastMessageTime")));
        
        List<Message> messages = mongoTemplate.find(query, Message.class);
        
        return messages.stream()
                .map(message -> convertToMessageResponse(message, userRole))
                .collect(Collectors.toList());
    }

    public MessageResponse sendMessage(String senderId, String senderRole, MessageRequest request) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));
        
        User recipient;
        String lecturerId, studentId;
        
        if ("STUDENT".equals(senderRole)) {
            // Student sending to lecturer
            recipient = userRepository.findById(request.getRecipientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lecturer not found"));
            if (!recipient.getRole().equals(User.UserRole.LECTURER)) {
                throw new IllegalArgumentException("Students can only send messages to lecturers");
            }
            lecturerId = recipient.getId();
            studentId = senderId;
        } else {
            // Lecturer sending to student
            recipient = userRepository.findById(request.getRecipientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
            if (!recipient.getRole().equals(User.UserRole.STUDENT)) {
                throw new IllegalArgumentException("Lecturers can only send messages to students");
            }
            lecturerId = senderId;
            studentId = recipient.getId();
        }

        Message message = Message.builder()
                .studentId(studentId)
                .lecturerId(lecturerId)
                .subject(request.getSubject())
                .lastMessage(request.getContent())
                .lastMessageTime(LocalDateTime.now())
                .messageType(Message.MessageType.valueOf(request.getType().toUpperCase()))
                .priority(Message.Priority.valueOf(request.getPriority().toUpperCase()))
                .readByStudent("STUDENT".equals(senderRole))
                .readByLecturer("LECTURER".equals(senderRole))
                .unreadCount("STUDENT".equals(senderRole) ? 0 : 1)
                .course(request.getCourse())
                .build();

        // Add initial message to thread
        Message.MessageThread initialMessage = Message.MessageThread.builder()
                .id(generateMessageId())
                .sender("STUDENT".equals(senderRole) ? "student" : "lecturer")
                .senderName(sender.getProfile().getFirstName() + " " + sender.getProfile().getLastName())
                .content(request.getContent())
                .timestamp(LocalDateTime.now())
                .build();
        
        message.getMessages().add(initialMessage);
        
        Message savedMessage = messageRepository.save(message);
        
        return convertToMessageResponse(savedMessage, senderRole);
    }

    public MessageResponse getMessageById(String messageId, String userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
        
        // Check if user has access to this message
        if (!message.getStudentId().equals(userId) && !message.getLecturerId().equals(userId)) {
            throw new IllegalArgumentException("Access denied to this message");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return convertToMessageResponse(message, user.getRole().toString());
    }

    public MessageThreadResponse getMessageThread(String messageId, String userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
        
        // Check access
        if (!message.getStudentId().equals(userId) && !message.getLecturerId().equals(userId)) {
            throw new IllegalArgumentException("Access denied to this message");
        }
        
        // Mark as read
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (user.getRole().equals(User.UserRole.STUDENT)) {
            message.setReadByStudent(true);
        } else {
            message.setReadByLecturer(true);
        }
        message.setUnreadCount(0);
        messageRepository.save(message);
        
        return MessageThreadResponse.builder()
                .id(message.getId())
                .messages(message.getMessages())
                .build();
    }

    public MessageResponse replyToMessage(String messageId, String senderId, String senderRole, ReplyRequest request) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
        
        // Check access
        if (!message.getStudentId().equals(senderId) && !message.getLecturerId().equals(senderId)) {
            throw new IllegalArgumentException("Access denied to this message");
        }
        
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));
        
        // Add reply to message thread
        Message.MessageThread reply = Message.MessageThread.builder()
                .id(generateMessageId())
                .sender("STUDENT".equals(senderRole) ? "student" : "lecturer")
                .senderName(sender.getProfile().getFirstName() + " " + sender.getProfile().getLastName())
                .content(request.getContent())
                .timestamp(LocalDateTime.now())
                .build();
        
        message.getMessages().add(reply);
        message.setLastMessage(request.getContent());
        message.setLastMessageTime(LocalDateTime.now());
        
        // Update read status
        if ("STUDENT".equals(senderRole)) {
            message.setReadByStudent(true);
            message.setReadByLecturer(false);
        } else {
            message.setReadByLecturer(true);
            message.setReadByStudent(false);
        }
        message.setUnreadCount(1);
        
        Message savedMessage = messageRepository.save(message);
        
        return convertToMessageResponse(savedMessage, senderRole);
    }

    public void markMessageAsRead(String messageId, String userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (user.getRole().equals(User.UserRole.STUDENT)) {
            message.setReadByStudent(true);
        } else {
            message.setReadByLecturer(true);
        }
        message.setUnreadCount(0);
        
        messageRepository.save(message);
    }

    public void markAllMessagesAsRead(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Query query = new Query();
        if (user.getRole().equals(User.UserRole.STUDENT)) {
            query.addCriteria(Criteria.where("studentId").is(userId));
        } else {
            query.addCriteria(Criteria.where("lecturerId").is(userId));
        }
        
        List<Message> messages = mongoTemplate.find(query, Message.class);
        
        messages.forEach(message -> {
            if (user.getRole().equals(User.UserRole.STUDENT)) {
                message.setReadByStudent(true);
            } else {
                message.setReadByLecturer(true);
            }
            message.setUnreadCount(0);
        });
        
        messageRepository.saveAll(messages);
    }

    public MessageResponse updateMessagePriority(String messageId, String priority, String userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
        
        // Only lecturers can update priority
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (!user.getRole().equals(User.UserRole.LECTURER) || !message.getLecturerId().equals(userId)) {
            throw new IllegalArgumentException("Only the assigned lecturer can update message priority");
        }
        
        message.setPriority(Message.Priority.valueOf(priority.toUpperCase()));
        Message savedMessage = messageRepository.save(message);
        
        return convertToMessageResponse(savedMessage, user.getRole().toString());
    }

    public MessageResponse updateMessageStatus(String messageId, String status, String userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
        
        // Only lecturers can update status
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (!user.getRole().equals(User.UserRole.LECTURER) || !message.getLecturerId().equals(userId)) {
            throw new IllegalArgumentException("Only the assigned lecturer can update message status");
        }
        
        message.setStatus(Message.Status.valueOf(status.toUpperCase()));
        Message savedMessage = messageRepository.save(message);
        
        return convertToMessageResponse(savedMessage, user.getRole().toString());
    }

    public void deleteMessage(String messageId, String userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
        
        // Check if user has permission to delete
        if (!message.getStudentId().equals(userId) && !message.getLecturerId().equals(userId)) {
            throw new IllegalArgumentException("Access denied to delete this message");
        }
        
        messageRepository.delete(message);
    }

    // Helper methods
    private MessageResponse convertToMessageResponse(Message message, String userRole) {
        User student = userRepository.findById(message.getStudentId()).orElse(null);
        User lecturer = userRepository.findById(message.getLecturerId()).orElse(null);
        
        String otherPartyName, otherPartyAvatar;
        boolean isRead;
        
        if ("STUDENT".equals(userRole)) {
            otherPartyName = lecturer != null ? 
                lecturer.getProfile().getFirstName() + " " + lecturer.getProfile().getLastName() : "Unknown Lecturer";
            otherPartyAvatar = "👩‍🏫"; // Default lecturer avatar
            isRead = message.isReadByStudent();
        } else {
            otherPartyName = student != null ? 
                student.getProfile().getFirstName() + " " + student.getProfile().getLastName() : "Unknown Student";
            otherPartyAvatar = "👨‍🎓"; // Default student avatar
            isRead = message.isReadByLecturer();
        }
        
        return MessageResponse.builder()
                .id(message.getId())
                .lecturer(otherPartyName)
                .lecturerAvatar(otherPartyAvatar)
                .subject(message.getSubject())
                .lastMessage(message.getLastMessage())
                .lastMessageTime(message.getLastMessageTime().toString())
                .unreadCount(isRead ? 0 : message.getUnreadCount())
                .isRead(isRead)
                .course(message.getCourse())
                .messageType(message.getMessageType().toString().toLowerCase())
                .priority(message.getPriority().toString().toLowerCase())
                .build();
    }

    private String generateMessageId() {
        return java.util.UUID.randomUUID().toString();
    }
}