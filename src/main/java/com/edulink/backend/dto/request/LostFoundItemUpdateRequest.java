// src/main/java/com/edulink/backend/dto/request/LostFoundItemUpdateRequest.java
package com.edulink.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LostFoundItemUpdateRequest {
    
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;
    
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;
    
    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;
    
    @Size(max = 500, message = "Contact info must not exceed 500 characters")
    private String contactInfo;
    
    private String status; // "OPEN", "RESOLVED", "ARCHIVED"
    
    // For resolution
    private String resolvedBy;
    private String resolvedByName;
}