// src/main/java/com/edulink/backend/service/ResourceService.java
package com.edulink.backend.service;

import com.edulink.backend.exception.ResourceNotFoundException;
import com.edulink.backend.model.entity.Course;
import com.edulink.backend.model.entity.Resource;
import com.edulink.backend.model.entity.User;
import com.edulink.backend.repository.CourseRepository;
import com.edulink.backend.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final CourseRepository courseRepository;
    private final FileStorageService fileStorageService;

    /**
     * Stores an uploaded file and creates a corresponding database record.
     *
     * @param file The file uploaded by the lecturer.
     * @param courseId The ID of the course the file belongs to.
     * @param title A title for the resource.
     * @param description A description for the resource.
     * @param uploader The user entity of the lecturer uploading the file.
     * @return The created Resource entity.
     */
    public Resource createResource(MultipartFile file, String courseId, String title, String description, User uploader) {
        // 1. Verify the course exists and the uploader is the lecturer for that course.
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        if (!course.getLecturerId().equals(uploader.getId())) {
            // In a real app, you might use a more specific exception like NotAuthorizedException
            throw new SecurityException("User is not the lecturer of this course.");
        }

        // 2. Store the physical file using the FileStorageService.
        String storedFilename = fileStorageService.storeFile(file);

        // 3. Create the Resource metadata entity.
        Resource resource = Resource.builder()
                .courseId(courseId)
                .uploaderId(uploader.getId())
                .title(title)
                .description(description)
                .originalFilename(file.getOriginalFilename())
                .storedFilename(storedFilename)
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .build();

        // 4. Save the metadata to the database.
        return resourceRepository.save(resource);
    }

    /**
     * Retrieves a resource by its ID.
     *
     * @param resourceId The ID of the resource.
     * @return The Resource entity.
     */
    public Resource getResourceById(String resourceId) {
        return resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with ID: " + resourceId));
    }

    /**
     * Retrieves all resources for a specific course.
     *
     * @param courseId The ID of the course.
     * @return A list of Resource entities.
     */
    public List<Resource> getResourcesByCourse(String courseId) {
        // Verify the course exists before fetching resources
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with ID: " + courseId);
        }
        return resourceRepository.findAllByCourseIdOrderByUploadedAtDesc(courseId);
    }

    /**
     * Deletes a resource, including its physical file.
     *
     * @param resourceId The ID of the resource to delete.
     * @param currentUser The user attempting to delete the resource.
     */
    public void deleteResource(String resourceId, User currentUser) {
        // 1. Find the resource metadata in the database.
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with ID: " + resourceId));

        // 2. Security Check: Ensure the person deleting is the one who uploaded it.
        if (!resource.getUploaderId().equals(currentUser.getId())) {
            throw new SecurityException("User is not authorized to delete this resource.");
        }

        // 3. Delete the physical file from storage.
        fileStorageService.deleteFile(resource.getStoredFilename());

        // 4. Delete the metadata record from the database.
        resourceRepository.delete(resource);
    }
}