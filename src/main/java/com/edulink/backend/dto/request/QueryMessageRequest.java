// File Path: src/main/java/com/edulink/backend/dto/request/QueryMessageRequest.java
package com.edulink.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class QueryMessageRequest {
    
    @NotBlank(message = "Message content is required")
    @Size(max = 2000, message = "Message must not exceed 2000 characters")
    private String content;

    // Constructors
    public QueryMessageRequest() {}

    public QueryMessageRequest(String content) {
        this.content = content;
    }

    // Getters and Setters
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
