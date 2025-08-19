// src/main/java/com/edulink/backend/dto/request/LostFoundItemRequest.java
package com.edulink.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LostFoundItemRequest {
    
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;
    
    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;
    
    @NotBlank(message = "Type is required")
    private String type; // "LOST" or "FOUND"
    
    @NotBlank(message = "Location is required")
    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;
    
    @NotNull(message = "Lost/Found date and time is required")
    private LocalDateTime lostFoundDateTime;
    
    @Size(max = 500, message = "Contact info must not exceed 500 characters")
    private String contactInfo;
    
    private String imageUrl;
    
    private String imageFilename; // For file uploads
    
    private String category; // Optional category like "Electronics", "Books", etc.
}
