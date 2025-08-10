// src/main/java/com/edulink/backend/repository/ResourceRepository.java
package com.edulink.backend.repository;

import com.edulink.backend.model.entity.Resource;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRepository extends MongoRepository<Resource, String> {

    /**
     * Finds all resources associated with a specific course,
     * ordered by the upload date in descending order (newest first).
     *
     * @param courseId The ID of the course.
     * @return A list of resources for the given course.
     */
    List<Resource> findAllByCourseIdOrderByUploadedAtDesc(String courseId);

}