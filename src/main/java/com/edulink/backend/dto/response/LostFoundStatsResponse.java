
// src/main/java/com/edulink/backend/dto/response/LostFoundStatsResponse.java
package com.edulink.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LostFoundStatsResponse {
    
    // Overall stats
    private long totalItems;
    private long openItems;
    private long resolvedItems;
    
    // By type
    private long lostItems;
    private long foundItems;
    private long openLostItems;
    private long openFoundItems;
    
    // Recent activity
    private long itemsThisWeek;
    private long resolvedThisWeek;
    private long commentsThisWeek;
    
    // User stats (for current user)
    private long myItems;
    private long myResolvedItems;
    private long myComments;
}