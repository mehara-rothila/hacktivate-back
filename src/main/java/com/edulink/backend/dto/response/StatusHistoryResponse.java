
// File Path: src/main/java/com/edulink/backend/dto/response/StatusHistoryResponse.java
package com.edulink.backend.dto.response;

import com.edulink.backend.model.entity.Query.StatusHistoryEntry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusHistoryResponse {
    private String status;
    private LocalDateTime timestamp;
    private String changedBy;
    private String changedByName;
    private String note;
    private String reason;

    public static StatusHistoryResponse fromStatusHistoryEntry(StatusHistoryEntry entry) {
        if (entry == null) return null;
        
        return StatusHistoryResponse.builder()
                .status(entry.getStatus() != null ? entry.getStatus().name() : null)
                .timestamp(entry.getTimestamp())
                .changedBy(entry.getChangedBy())
                .changedByName(entry.getChangedByName())
                .note(entry.getNote())
                .reason(entry.getReason())
                .build();
    }
}