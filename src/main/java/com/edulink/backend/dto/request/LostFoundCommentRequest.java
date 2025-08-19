// src/main/java/com/edulink/backend/dto/request/LostFoundCommentRequest.java
package com.edulink.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LostFoundCommentRequest {
    
    @NotNull(message = "Lost/Found item ID is required")
    private String lostFoundItemId;
    
    @NotBlank(message = "Comment content is required")
    @Size(max = 1000, message = "Comment must not exceed 1000 characters")
    private String content;
    
    private String type; // "COMMENT", "CLAIM", "INFO"
    
    @Size(max = 500, message = "Contact info must not exceed 500 characters")
    private String contactInfo; // Optional contact info for claims
}
