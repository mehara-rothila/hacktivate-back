// ============================================================================
// dto/request/MessageRequest.java
// ============================================================================
package com.edulink.backend.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
public class MessageRequest {
    
    @NotBlank(message = "Recipient ID is required")
    private String recipientId;
    
    @NotBlank(message = "Subject is required")
    @Size(max = 200, message = "Subject must not exceed 200 characters")
    private String subject;
    
    @NotBlank(message = "Content is required")
    @Size(max = 5000, message = "Content must not exceed 5000 characters")
    private String content;
    
    @NotBlank(message = "Course is required")
    private String course;
    
    @NotBlank(message = "Message type is required")
    private String type; // academic, administrative, general
    
    @NotBlank(message = "Priority is required")
    private String priority; // low, medium, high
}

