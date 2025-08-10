// src/main/java/com/edulink/backend/service/FileStorageService.java
package com.edulink.backend.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * Stores a file on the server.
     *
     * @param file The file uploaded by the user.
     * @return The unique filename under which the file was stored.
     */
    String storeFile(MultipartFile file);

    /**
     * Loads a file as a Spring Resource.
     *
     * @param filename The unique name of the file to load.
     * @return The file as a Resource object.
     */
    Resource loadFileAsResource(String filename);

    /**
     * Deletes a file from the server.
     *
     * @param filename The unique name of the file to delete.
     */
    void deleteFile(String filename);
}