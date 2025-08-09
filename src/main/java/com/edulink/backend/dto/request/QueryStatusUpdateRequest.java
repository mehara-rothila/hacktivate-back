
// File Path: src/main/java/com/edulink/backend/dto/request/QueryStatusUpdateRequest.java
package com.edulink.backend.dto.request;

import com.edulink.backend.model.entity.Query.QueryStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class QueryStatusUpdateRequest {
    
    @NotNull(message = "Status is required")
    private QueryStatus status;
    
    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String note;

    // Constructors
    public QueryStatusUpdateRequest() {}

    public QueryStatusUpdateRequest(QueryStatus status, String note) {
        this.status = status;
        this.note = note;
    }

    // Getters and Setters
    public QueryStatus getStatus() {
        return status;
    }

    public void setStatus(QueryStatus status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}