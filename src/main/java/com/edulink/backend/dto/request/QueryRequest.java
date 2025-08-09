// File Path: src/main/java/com/edulink/backend/dto/request/QueryRequest.java
package com.edulink.backend.dto.request;

import com.edulink.backend.model.entity.Query.QueryCategory;
import com.edulink.backend.model.entity.Query.QueryPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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

    // Constructors
    public QueryRequest() {}

    public QueryRequest(String title, String description, QueryCategory category, QueryPriority priority, String course, String lecturerId) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.course = course;
        this.lecturerId = lecturerId;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public QueryCategory getCategory() {
        return category;
    }

    public void setCategory(QueryCategory category) {
        this.category = category;
    }

    public QueryPriority getPriority() {
        return priority;
    }

    public void setPriority(QueryPriority priority) {
        this.priority = priority;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(String lecturerId) {
        this.lecturerId = lecturerId;
    }
}