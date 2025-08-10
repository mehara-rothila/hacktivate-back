// src/main/java/com/edulink/backend/controller/ResourceController.java
package com.edulink.backend.controller;

import com.edulink.backend.dto.response.ApiResponse;
import com.edulink.backend.model.entity.Resource;
import com.edulink.backend.model.entity.User;
import com.edulink.backend.service.FileStorageService;
import com.edulink.backend.service.ResourceService;
import com.edulink.backend.service.UserService;
import lombok.RequiredArgsConstructor;
// CORRECTED: Removed the incorrect import alias
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;
    private final UserService userService;
    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<ApiResponse> uploadResource(@RequestParam("file") MultipartFile file,
                                                      @RequestParam("courseId") String courseId,
                                                      @RequestParam("title") String title,
                                                      @RequestParam("description") String description) {
        User currentUser = userService.getCurrentUser();
        Resource resource = resourceService.createResource(file, courseId, title, description, currentUser);
        // CORRECTED: Use the builder for ApiResponse
        return ResponseEntity.ok(ApiResponse.builder().success(true).message("File uploaded successfully.").data(resource).build());
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> getResourcesForCourse(@PathVariable String courseId) {
        List<Resource> resources = resourceService.getResourcesByCourse(courseId);
        // CORRECTED: Use the builder for ApiResponse
        return ResponseEntity.ok(ApiResponse.builder().success(true).message("Resources retrieved successfully.").data(resources).build());
    }

    @GetMapping("/{resourceId}/download")
    @PreAuthorize("isAuthenticated()")
    // CORRECTED: Use the fully qualified name for Spring's Resource to avoid naming conflicts
    public ResponseEntity<org.springframework.core.io.Resource> downloadFile(@PathVariable String resourceId) {
        // 1. Get resource metadata from the database
        Resource resource = resourceService.getResourceById(resourceId);

        // 2. Load the physical file from storage
        org.springframework.core.io.Resource fileResource = fileStorageService.loadFileAsResource(resource.getStoredFilename());

        // 3. Build the response with correct headers
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(resource.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getOriginalFilename() + "\"")
                .body(fileResource);
    }

    @DeleteMapping("/{resourceId}")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<ApiResponse> deleteResource(@PathVariable String resourceId) {
        User currentUser = userService.getCurrentUser();
        resourceService.deleteResource(resourceId, currentUser);
        // CORRECTED: Use the builder for ApiResponse
        return ResponseEntity.ok(ApiResponse.builder().success(true).message("Resource deleted successfully.").data(null).build());
    }
}