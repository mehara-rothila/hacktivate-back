package com.edulink.backend.dto.request;

import com.edulink.backend.model.entity.Query.QueryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryStatusUpdateRequest {
    
    @NotNull(message = "Status is required")
    private QueryStatus status;
    
    private String note;
    
    private String reason;
}