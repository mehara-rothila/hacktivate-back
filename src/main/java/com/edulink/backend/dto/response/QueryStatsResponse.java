
// File Path: src/main/java/com/edulink/backend/dto/response/QueryStatsResponse.java
package com.edulink.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private int queriesThisWeek;
    private int queriesThisMonth;
    
    // Optional category-specific stats
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryStats {
        private String category;
        private long count;
        private long pending;
        private long inProgress;
        private long resolved;
    }
}