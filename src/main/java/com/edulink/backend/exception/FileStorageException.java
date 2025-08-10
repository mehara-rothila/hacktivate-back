// src/main/java/com/edulink/backend/exception/FileStorageException.java
package com.edulink.backend.exception;

public class FileStorageException extends RuntimeException {
    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}