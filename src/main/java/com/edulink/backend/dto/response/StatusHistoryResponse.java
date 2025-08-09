
// File: src/main/java/com/edulink/backend/dto/response/StatusHistoryResponse.java
package com.edulink.backend.dto.response;

import com.edulink.backend.model.entity.Query.StatusHistoryEntry;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusHistoryResponse {
    
    private String status;
    private String timestamp;
    private String changedBy;
    private String changedByName;
    private String note;
    private String reason;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static StatusHistoryResponse fromStatusHistory(StatusHistoryEntry entry) {
        return StatusHistoryResponse.builder()
                .status(entry.getStatus().getValue())
                .timestamp(entry.getTimestamp().format(FORMATTER))
                .changedBy(entry.getChangedBy())
                .changedByName(entry.getChangedByName())
                .note(entry.getNote())
                .reason(entry.getReason())
                .build();
    }
}