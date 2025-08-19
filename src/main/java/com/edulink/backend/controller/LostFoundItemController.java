// src/main/java/com/edulink/backend/controller/LostFoundItemController.java
package com.edulink.backend.controller;

import com.edulink.backend.dto.request.LostFoundCommentRequest;
import com.edulink.backend.dto.request.LostFoundItemRequest;
import com.edulink.backend.dto.request.LostFoundItemUpdateRequest;
import com.edulink.backend.dto.request.LostFoundSearchRequest;
import com.edulink.backend.dto.response.ApiResponse;
import com.edulink.backend.dto.response.LostFoundCommentResponse;
import com.edulink.backend.dto.response.LostFoundItemResponse;
import com.edulink.backend.dto.response.LostFoundStatsResponse;
import com.edulink.backend.service.LostFoundItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/lost-found")
@RequiredArgsConstructor
@Slf4j
public class LostFoundItemController {

    private final LostFoundItemService lostFoundItemService;

    // Create new lost/found item
    @PostMapping
    public ResponseEntity<ApiResponse<LostFoundItemResponse>> createItem(
            @Valid @RequestBody LostFoundItemRequest request,
            Authentication authentication) {
        try {
            log.info("Creating new lost/found item: {}", request.getTitle());
            LostFoundItemResponse response = lostFoundItemService.createItem(request, authentication);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<LostFoundItemResponse>builder()
                            .success(true)
                            .message("Lost/Found item created successfully")
                            .data(response)
                            .timestamp(LocalDateTime.now())
                            .build());
        } catch (Exception e) {
            log.error("Error creating lost/found item: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<LostFoundItemResponse>builder()
                            .success(false)
                            .message("Failed to create lost/found item")
                            .error(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }

    // Get all items with optional search/filter
    @GetMapping
    public ResponseEntity<ApiResponse<List<LostFoundItemResponse>>> getAllItems(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String userId,
            Authentication authentication) {
        try {
            log.info("Fetching lost/found items with filters - query: {}, type: {}, status: {}, userId: {}", 
                    query, type, status, userId);
            
            LostFoundSearchRequest searchRequest = LostFoundSearchRequest.builder()
                    .query(query)
                    .type(type)
                    .status(status)
                    .userId(userId)
                    .build();
            
            List<LostFoundItemResponse> response = lostFoundItemService.getAllItems(searchRequest, authentication);
            
            return ResponseEntity.ok(ApiResponse.<List<LostFoundItemResponse>>builder()
                    .success(true)
                    .message("Lost/Found items retrieved successfully")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("Error fetching lost/found items: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<LostFoundItemResponse>>builder()
                            .success(false)
                            .message("Failed to retrieve lost/found items")
                            .error(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }

    // Get item by ID with comments
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LostFoundItemResponse>> getItemById(
            @PathVariable String id,
            Authentication authentication) {
        try {
            log.info("Fetching lost/found item with ID: {}", id);
            LostFoundItemResponse response = lostFoundItemService.getItemById(id, authentication);
            
            return ResponseEntity.ok(ApiResponse.<LostFoundItemResponse>builder()
                    .success(true)
                    .message("Lost/Found item retrieved successfully")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (RuntimeException e) {
            log.error("Error fetching lost/found item: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<LostFoundItemResponse>builder()
                            .success(false)
                            .message("Lost/Found item not found")
                            .error(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        } catch (Exception e) {
            log.error("Error fetching lost/found item: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<LostFoundItemResponse>builder()
                            .success(false)
                            .message("Failed to retrieve lost/found item")
                            .error(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }

    // Update item
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LostFoundItemResponse>> updateItem(
            @PathVariable String id,
            @Valid @RequestBody LostFoundItemUpdateRequest request,
            Authentication authentication) {
        try {
            log.info("Updating lost/found item with ID: {}", id);
            LostFoundItemResponse response = lostFoundItemService.updateItem(id, request, authentication);
            
            return ResponseEntity.ok(ApiResponse.<LostFoundItemResponse>builder()
                    .success(true)
                    .message("Lost/Found item updated successfully")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (RuntimeException e) {
            log.error("Error updating lost/found item: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<LostFoundItemResponse>builder()
                            .success(false)
                            .message("Failed to update lost/found item")
                            .error(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        } catch (Exception e) {
            log.error("Error updating lost/found item: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<LostFoundItemResponse>builder()
                            .success(false)
                            .message("Failed to update lost/found item")
                            .error(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }

    // Resolve item
    @PutMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<LostFoundItemResponse>> resolveItem(
            @PathVariable String id,
            @RequestParam(required = false) String resolvedByName,
            Authentication authentication) {
        try {
            log.info("Resolving lost/found item with ID: {}", id);
            LostFoundItemResponse response = lostFoundItemService.resolveItem(id, resolvedByName, authentication);
            
            return ResponseEntity.ok(ApiResponse.<LostFoundItemResponse>builder()
                    .success(true)
                    .message("Lost/Found item resolved successfully")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (RuntimeException e) {
            log.error("Error resolving lost/found item: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<LostFoundItemResponse>builder()
                            .success(false)
                            .message("Failed to resolve lost/found item")
                            .error(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        } catch (Exception e) {
            log.error("Error resolving lost/found item: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<LostFoundItemResponse>builder()
                            .success(false)
                            .message("Failed to resolve lost/found item")
                            .error(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }

    // Delete item
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteItem(
            @PathVariable String id,
            Authentication authentication) {
        try {
            log.info("Deleting lost/found item with ID: {}", id);
            lostFoundItemService.deleteItem(id, authentication);
            
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .success(true)
                    .message("Lost/Found item deleted successfully")
                    .data("Item deleted")
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (RuntimeException e) {
            log.error("Error deleting lost/found item: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<String>builder()
                            .success(false)
                            .message("Failed to delete lost/found item")
                            .error(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        } catch (Exception e) {
            log.error("Error deleting lost/found item: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<String>builder()
                            .success(false)
                            .message("Failed to delete lost/found item")
                            .error(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }

    // Add comment to item
    @PostMapping("/comments")
    public ResponseEntity<ApiResponse<LostFoundCommentResponse>> addComment(
            @Valid @RequestBody LostFoundCommentRequest request,
            Authentication authentication) {
        try {
            log.info("Adding comment to lost/found item with ID: {}", request.getLostFoundItemId());
            LostFoundCommentResponse response = lostFoundItemService.addComment(request, authentication);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<LostFoundCommentResponse>builder()
                            .success(true)
                            .message("Comment added successfully")
                            .data(response)
                            .timestamp(LocalDateTime.now())
                            .build());
        } catch (Exception e) {
            log.error("Error adding comment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<LostFoundCommentResponse>builder()
                            .success(false)
                            .message("Failed to add comment")
                            .error(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }

    // Get comments for item
    @GetMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<List<LostFoundCommentResponse>>> getCommentsForItem(
            @PathVariable String id,
            Authentication authentication) {
        try {
            log.info("Fetching comments for lost/found item with ID: {}", id);
            List<LostFoundCommentResponse> response = lostFoundItemService.getCommentsForItem(id, authentication);
            
            return ResponseEntity.ok(ApiResponse.<List<LostFoundCommentResponse>>builder()
                    .success(true)
                    .message("Comments retrieved successfully")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("Error fetching comments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<LostFoundCommentResponse>>builder()
                            .success(false)
                            .message("Failed to retrieve comments")
                            .error(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }

    // Delete comment
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<String>> deleteComment(
            @PathVariable String commentId,
            Authentication authentication) {
        try {
            log.info("Deleting comment with ID: {}", commentId);
            lostFoundItemService.deleteComment(commentId, authentication);
            
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .success(true)
                    .message("Comment deleted successfully")
                    .data("Comment deleted")
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (RuntimeException e) {
            log.error("Error deleting comment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<String>builder()
                            .success(false)
                            .message("Failed to delete comment")
                            .error(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        } catch (Exception e) {
            log.error("Error deleting comment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<String>builder()
                            .success(false)
                            .message("Failed to delete comment")
                            .error(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }

    // Get statistics
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<LostFoundStatsResponse>> getStats(Authentication authentication) {
        try {
            log.info("Fetching lost/found statistics");
            LostFoundStatsResponse response = lostFoundItemService.getStats(authentication);
            
            return ResponseEntity.ok(ApiResponse.<LostFoundStatsResponse>builder()
                    .success(true)
                    .message("Statistics retrieved successfully")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("Error fetching statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<LostFoundStatsResponse>builder()
                            .success(false)
                            .message("Failed to retrieve statistics")
                            .error(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }

    // Get my items (posted by current user)
    @GetMapping("/my-items")
    public ResponseEntity<ApiResponse<List<LostFoundItemResponse>>> getMyItems(Authentication authentication) {
        try {
            log.info("Fetching current user's lost/found items");
            
            // Get current user ID and search for their items
            LostFoundSearchRequest searchRequest = LostFoundSearchRequest.builder()
                    .userId(authentication.getName()) // This might need adjustment based on your auth setup
                    .build();
            
            List<LostFoundItemResponse> response = lostFoundItemService.getAllItems(searchRequest, authentication);
            
            return ResponseEntity.ok(ApiResponse.<List<LostFoundItemResponse>>builder()
                    .success(true)
                    .message("My lost/found items retrieved successfully")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("Error fetching my items: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<LostFoundItemResponse>>builder()
                            .success(false)
                            .message("Failed to retrieve my items")
                            .error(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }

    // Upload image for lost/found item
    @PostMapping("/upload-image")
    public ResponseEntity<ApiResponse<java.util.Map<String, String>>> uploadImage(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            Authentication authentication) {
        try {
            log.info("Uploading image for lost/found item");
            
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.<java.util.Map<String, String>>builder()
                                .success(false)
                                .message("Please select a file to upload")
                                .timestamp(LocalDateTime.now())
                                .build());
            }
            
            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.<java.util.Map<String, String>>builder()
                                .success(false)
                                .message("Only image files are allowed")
                                .timestamp(LocalDateTime.now())
                                .build());
            }
            
            // Validate file size (10MB max)
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.<java.util.Map<String, String>>builder()
                                .success(false)
                                .message("File size must be less than 10MB")
                                .timestamp(LocalDateTime.now())
                                .build());
            }
            
            // Store the file
            String filename = lostFoundItemService.storeImageFile(file);
            String imageUrl = "/uploads/" + filename; // Adjust path as needed
            
            java.util.Map<String, String> response = new java.util.HashMap<>();
            response.put("filename", filename);
            response.put("url", imageUrl);
            
            return ResponseEntity.ok(ApiResponse.<java.util.Map<String, String>>builder()
                    .success(true)
                    .message("Image uploaded successfully")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build());
                    
        } catch (Exception e) {
            log.error("Error uploading image: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<java.util.Map<String, String>>builder()
                            .success(false)
                            .message("Failed to upload image")
                            .error(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }
}