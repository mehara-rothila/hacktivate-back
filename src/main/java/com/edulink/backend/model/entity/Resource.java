// src/main/java/com/edulink/backend/model/entity/Resource.java
package com.edulink.backend.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "resources")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resource {

    @Id
    private String id;

    @Indexed
    private String courseId; // The course this resource belongs to

    @Indexed
    private String uploaderId; // The lecturer who uploaded the file

    private String title; // A user-friendly title for the resource
    private String description;

    // --- File Metadata ---
    private String originalFilename; // The name of the file on the user's computer (e.g., "lecture_notes.pdf")
    private String storedFilename;   // The unique name of the file on our server (e.g., "uuid-xyz.pdf")
    private String fileType;         // The MIME type (e.g., "application/pdf")
    private long fileSize;           // File size in bytes

    @CreatedDate
    private LocalDateTime uploadedAt;
}