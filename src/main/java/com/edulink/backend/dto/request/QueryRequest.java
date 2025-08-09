// File Path: src/main/java/com/edulink/backend/dto/request/QueryRequest.java
package com.edulink.backend.dto.request;

import com.edulink.backend.model.entity.Query.QueryCategory;
import com.edulink.backend.model.entity.Query.QueryPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryRequest {
    
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;
    
    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;
    
    @NotNull(message = "Category is required")
    private QueryCategory category;
    
    @NotNull(message = "Priority is required")
    private QueryPriority priority;
    
    private String course;
    
    private String lecturerId; // Optional - if specific lecturer is requested
}
