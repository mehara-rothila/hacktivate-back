// src/main/java/com/edulink/backend/controller/FileController.java
package com.edulink.backend.controller;

import com.edulink.backend.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileStorageService fileStorageService;

    /**
     * Serve files (like profile pictures) directly by filename
     * This endpoint serves files at /api/files/{filename}
     */
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            log.info("Serving file: {}", filename);
            
            // Load the physical file from storage
            Resource fileResource = fileStorageService.loadFileAsResource(filename);
            
            // Determine content type based on file extension
            String contentType = determineContentType(filename);
            
            // Build the response for inline display (not download)
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=3600") // Cache for 1 hour
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline") // Display inline, not download
                    .body(fileResource);
                    
        } catch (Exception e) {
            log.error("Error serving file: {}", filename, e);
            // Return 404 if file not found
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Determine content type based on file extension
     */
    private String determineContentType(String filename) {
        try {
            String fileExtension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
            switch (fileExtension) {
                case "jpg":
                case "jpeg":
                    return "image/jpeg";
                case "png":
                    return "image/png";
                case "gif":
                    return "image/gif";
                case "webp":
                    return "image/webp";
                case "svg":
                    return "image/svg+xml";
                case "pdf":
                    return "application/pdf";
                case "txt":
                    return "text/plain";
                case "doc":
                    return "application/msword";
                case "docx":
                    return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                case "xls":
                    return "application/vnd.ms-excel";
                case "xlsx":
                    return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                case "ppt":
                    return "application/vnd.ms-powerpoint";
                case "pptx":
                    return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
                default:
                    return "application/octet-stream";
            }
        } catch (Exception e) {
            return "application/octet-stream";
        }
    }
}