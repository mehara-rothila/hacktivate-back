
// File: src/main/java/com/edulink/backend/dto/response/QueryStatsResponse.java
package com.edulink.backend.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryStatsResponse {
    
    private long totalQueries;
    private long pendingQueries;
    private long inProgressQueries;
    private long resolvedQueries;
    private long unreadQueries;
    private long highPriorityQueries;
    
    // Category breakdown
    private CategoryStats categoryStats;
    
    // Recent activity
    private long queriesThisWeek;
    private long queriesThisMonth;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryStats {
        private long academic;
        private long technical;
        private long administrative;
        private long appointment;
        private long courseRelated;
    }
}