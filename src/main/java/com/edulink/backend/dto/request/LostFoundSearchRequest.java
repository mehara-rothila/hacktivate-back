// src/main/java/com/edulink/backend/dto/request/LostFoundSearchRequest.java
package com.edulink.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LostFoundSearchRequest {
    
    private String query; // Search in title and description
    private String type; // "LOST", "FOUND", or null for all
    private String status; // "OPEN", "RESOLVED", "ARCHIVED", or null for all
    private String userId; // Filter by specific user
}