// src/main/java/com/edulink/backend/service/LostFoundItemService.java
package com.edulink.backend.service;

import com.edulink.backend.dto.request.LostFoundCommentRequest;
import com.edulink.backend.dto.request.LostFoundItemRequest;
import com.edulink.backend.dto.request.LostFoundItemUpdateRequest;
import com.edulink.backend.dto.request.LostFoundSearchRequest;
import com.edulink.backend.dto.response.LostFoundCommentResponse;
import com.edulink.backend.dto.response.LostFoundItemResponse;
import com.edulink.backend.dto.response.LostFoundStatsResponse;
import com.edulink.backend.model.entity.LostFoundComment;
import com.edulink.backend.model.entity.LostFoundItem;
import com.edulink.backend.model.entity.User;
import com.edulink.backend.repository.LostFoundCommentRepository;
import com.edulink.backend.repository.LostFoundItemRepository;
import com.edulink.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LostFoundItemService {

    private final LostFoundItemRepository lostFoundItemRepository;
    private final LostFoundCommentRepository commentRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    // Create new lost/found item
    public LostFoundItemResponse createItem(LostFoundItemRequest request, Authentication authentication) {
        log.info("Creating new lost/found item: {}", request.getTitle());
        
        User currentUser = getCurrentUser(authentication);
        
        LostFoundItem item = LostFoundItem.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription().trim())
                .type(LostFoundItem.ItemType.valueOf(request.getType().toUpperCase()))
                .status(LostFoundItem.ItemStatus.OPEN)
                .userId(currentUser.getId())
                .userName(currentUser.getFullName())
                .userAvatar(currentUser.getProfile() != null ? currentUser.getProfile().getAvatar() : null)
                .location(request.getLocation() != null ? request.getLocation().trim() : null)
                .lostFoundDateTime(request.getLostFoundDateTime())
                .contactInfo(request.getContactInfo() != null ? request.getContactInfo().trim() : null)
                .build();
        
        // Handle image attachment if provided
        if (request.getImageFilename() != null && !request.getImageFilename().trim().isEmpty()) {
            item.setImage(LostFoundItem.ImageAttachment.builder()
                    .filename(request.getImageFilename())
                    .build());
        }
        
        LostFoundItem savedItem = lostFoundItemRepository.save(item);
        log.info("Created lost/found item with ID: {}", savedItem.getId());
        
        return mapToResponse(savedItem, currentUser.getId(), currentUser.getRole().name());
    }

    // Get all items with optional filters
    public List<LostFoundItemResponse> getAllItems(LostFoundSearchRequest searchRequest, Authentication authentication) {
        log.info("Fetching lost/found items with filters: {}", searchRequest);
        
        User currentUser = getCurrentUser(authentication);
        List<LostFoundItem> items;
        
        if (searchRequest != null && hasFilters(searchRequest)) {
            items = lostFoundItemRepository.findWithFilters(
                    searchRequest.getType() != null ? LostFoundItem.ItemType.valueOf(searchRequest.getType().toUpperCase()) : null,
                    searchRequest.getStatus() != null ? LostFoundItem.ItemStatus.valueOf(searchRequest.getStatus().toUpperCase()) : null,
                    searchRequest.getQuery()
            );
        } else {
            items = lostFoundItemRepository.findAllByOrderByCreatedAtDesc();
        }
        
        return items.stream()
                .map(item -> mapToResponse(item, currentUser.getId(), currentUser.getRole().name()))
                .collect(Collectors.toList());
    }

    // Get item by ID with comments
    public LostFoundItemResponse getItemById(String id, Authentication authentication) {
        log.info("Fetching lost/found item with ID: {}", id);
        
        User currentUser = getCurrentUser(authentication);
        LostFoundItem item = lostFoundItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lost/Found item not found with ID: " + id));
        
        LostFoundItemResponse response = mapToResponse(item, currentUser.getId(), currentUser.getRole().name());
        
        // Load comments
        List<LostFoundComment> comments = commentRepository.findByLostFoundItemIdOrderByCreatedAtAsc(id);
        response.setComments(comments.stream()
                .map(comment -> mapCommentToResponse(comment, currentUser.getId(), currentUser.getRole().name()))
                .collect(Collectors.toList()));
        
        return response;
    }

    // Update item
    public LostFoundItemResponse updateItem(String id, LostFoundItemUpdateRequest request, Authentication authentication) {
        log.info("Updating lost/found item with ID: {}", id);
        
        User currentUser = getCurrentUser(authentication);
        LostFoundItem item = lostFoundItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lost/Found item not found with ID: " + id));
        
        // Check permissions
        if (!item.canBeEditedBy(currentUser.getId())) {
            throw new RuntimeException("You don't have permission to edit this item");
        }
        
        // Update fields
        if (request.getTitle() != null) {
            item.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            item.setDescription(request.getDescription().trim());
        }
        if (request.getLocation() != null) {
            item.setLocation(request.getLocation().trim());
        }
        if (request.getLostFoundDateTime() != null) {
            item.setLostFoundDateTime(request.getLostFoundDateTime());
        }
        if (request.getContactInfo() != null) {
            item.setContactInfo(request.getContactInfo().trim());
        }
        
        LostFoundItem savedItem = lostFoundItemRepository.save(item);
        log.info("Updated lost/found item with ID: {}", savedItem.getId());
        
        return mapToResponse(savedItem, currentUser.getId(), currentUser.getRole().name());
    }

    // Resolve item
    public LostFoundItemResponse resolveItem(String id, String resolvedByName, Authentication authentication) {
        log.info("Resolving lost/found item with ID: {}", id);
        
        User currentUser = getCurrentUser(authentication);
        LostFoundItem item = lostFoundItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lost/Found item not found with ID: " + id));
        
        // Check if item can be resolved
        if (!item.canBeResolvedBy(currentUser.getId())) {
            throw new RuntimeException("You cannot resolve this item");
        }
        
        item.setStatus(LostFoundItem.ItemStatus.RESOLVED);
        item.setResolvedBy(currentUser.getId());
        item.setResolvedByName(resolvedByName != null ? resolvedByName : 
                currentUser.getFullName());
        item.setResolvedAt(LocalDateTime.now());
        
        LostFoundItem savedItem = lostFoundItemRepository.save(item);
        log.info("Resolved lost/found item with ID: {}", savedItem.getId());
        
        return mapToResponse(savedItem, currentUser.getId(), currentUser.getRole().name());
    }

    // Delete item
    public void deleteItem(String id, Authentication authentication) {
        log.info("Deleting lost/found item with ID: {}", id);
        
        User currentUser = getCurrentUser(authentication);
        LostFoundItem item = lostFoundItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lost/Found item not found with ID: " + id));
        
        // Check permissions
        if (!item.canBeDeletedBy(currentUser.getId(), currentUser.getRole().name())) {
            throw new RuntimeException("You don't have permission to delete this item");
        }
        
        lostFoundItemRepository.delete(item);
        log.info("Deleted lost/found item with ID: {}", id);
    }

    // Add comment
    public LostFoundCommentResponse addComment(LostFoundCommentRequest request, Authentication authentication) {
        log.info("Adding comment to lost/found item with ID: {}", request.getLostFoundItemId());
        
        User currentUser = getCurrentUser(authentication);
        
        // Validate that the item exists
        if (!lostFoundItemRepository.existsById(request.getLostFoundItemId())) {
            throw new RuntimeException("Lost/Found item not found");
        }
        
        LostFoundComment comment = LostFoundComment.builder()
                .lostFoundItemId(request.getLostFoundItemId())
                .userId(currentUser.getId())
                .userName(currentUser.getFullName())
                .userAvatar(currentUser.getProfile() != null ? currentUser.getProfile().getAvatar() : null)
                .content(request.getContent().trim())
                .type(LostFoundComment.CommentType.valueOf(request.getType().toUpperCase()))
                .contactInfo(request.getContactInfo() != null ? request.getContactInfo().trim() : null)
                .build();
        
        LostFoundComment savedComment = commentRepository.save(comment);
        log.info("Added comment with ID: {}", savedComment.getId());
        
        return mapCommentToResponse(savedComment, currentUser.getId(), currentUser.getRole().name());
    }

    // Get comments for item
    public List<LostFoundCommentResponse> getCommentsForItem(String itemId, Authentication authentication) {
        log.info("Fetching comments for lost/found item with ID: {}", itemId);
        
        User currentUser = getCurrentUser(authentication);
        List<LostFoundComment> comments = commentRepository.findByLostFoundItemIdOrderByCreatedAtAsc(itemId);
        
        return comments.stream()
                .map(comment -> mapCommentToResponse(comment, currentUser.getId(), currentUser.getRole().name()))
                .collect(Collectors.toList());
    }

    // Delete comment
    public void deleteComment(String commentId, Authentication authentication) {
        log.info("Deleting comment with ID: {}", commentId);
        
        User currentUser = getCurrentUser(authentication);
        LostFoundComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with ID: " + commentId));
        
        // Check permissions
        if (!comment.canBeDeletedBy(currentUser.getId(), currentUser.getRole().name())) {
            throw new RuntimeException("You don't have permission to delete this comment");
        }
        
        commentRepository.delete(comment);
        log.info("Deleted comment with ID: {}", commentId);
    }

    // Get statistics
    public LostFoundStatsResponse getStats(Authentication authentication) {
        log.info("Fetching lost/found statistics");
        
        User currentUser = getCurrentUser(authentication);
        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
        
        return LostFoundStatsResponse.builder()
                .totalItems(lostFoundItemRepository.count())
                .openItems(lostFoundItemRepository.countByStatus(LostFoundItem.ItemStatus.OPEN))
                .resolvedItems(lostFoundItemRepository.countByStatus(LostFoundItem.ItemStatus.RESOLVED))
                .lostItems(lostFoundItemRepository.countByType(LostFoundItem.ItemType.LOST))
                .foundItems(lostFoundItemRepository.countByType(LostFoundItem.ItemType.FOUND))
                .itemsThisWeek(lostFoundItemRepository.findRecentItems(weekAgo).size())
                .myItems(lostFoundItemRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId()).size())
                .build();
    }

    // Helper methods
    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    private boolean hasFilters(LostFoundSearchRequest request) {
        return (request.getQuery() != null && !request.getQuery().trim().isEmpty()) ||
                request.getType() != null ||
                request.getStatus() != null ||
                request.getUserId() != null;
    }

    private LostFoundItemResponse mapToResponse(LostFoundItem item, String currentUserId, String currentUserRole) {
        LostFoundItemResponse.LostFoundItemResponseBuilder builder = LostFoundItemResponse.builder()
                .id(item.getId())
                .title(item.getTitle())
                .description(item.getDescription())
                .type(item.getType().name())
                .status(item.getStatus().name())
                .userId(item.getUserId())
                .userName(item.getUserName())
                .userAvatar(item.getUserAvatar())
                .location(item.getLocation())
                .lostFoundDateTime(item.getLostFoundDateTime())
                .contactInfo(item.getContactInfo())
                .resolvedBy(item.getResolvedBy())
                .resolvedByName(item.getResolvedByName())
                .resolvedAt(item.getResolvedAt())
                .commentCount(item.getCommentCount())
                .claimCount((int) commentRepository.countByLostFoundItemIdAndType(item.getId(), LostFoundComment.CommentType.CLAIM))
                .hasUserCommented(commentRepository.existsByLostFoundItemIdAndUserId(item.getId(), currentUserId))
                .hasUserClaimed(commentRepository.existsByLostFoundItemIdAndUserIdAndType(item.getId(), currentUserId, LostFoundComment.CommentType.CLAIM))
                .canEdit(item.canBeEditedBy(currentUserId))
                .canDelete(item.canBeDeletedBy(currentUserId, currentUserRole))
                .canResolve(item.canBeResolvedBy(currentUserId))
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt());

        // Add image if present
        if (item.getImage() != null) {
            builder.image(LostFoundItemResponse.ImageAttachmentResponse.builder()
                    .filename(item.getImage().getFilename())
                    .originalFilename(item.getImage().getOriginalFilename())
                    .contentType(item.getImage().getContentType())
                    .size(item.getImage().getSize())
                    .downloadUrl("/api/files/" + item.getImage().getFilename())
                    .build());
        }

        return builder.build();
    }

    private LostFoundCommentResponse mapCommentToResponse(LostFoundComment comment, String currentUserId, String currentUserRole) {
        return LostFoundCommentResponse.builder()
                .id(comment.getId())
                .lostFoundItemId(comment.getLostFoundItemId())
                .userId(comment.getUserId())
                .userName(comment.getUserName())
                .userAvatar(comment.getUserAvatar())
                .content(comment.getContent())
                .type(comment.getType().name())
                .contactInfo(comment.getContactInfo())
                .canEdit(comment.canBeEditedBy(currentUserId))
                .canDelete(comment.canBeDeletedBy(currentUserId, currentUserRole))
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    // Store image file for lost/found item
    public String storeImageFile(MultipartFile file) {
        log.info("Storing image file: {}", file.getOriginalFilename());
        return fileStorageService.storeFile(file);
    }
}