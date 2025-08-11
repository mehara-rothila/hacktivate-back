// src/main/java/com/edulink/backend/controller/AvailabilityController.java
package com.edulink.backend.controller;

import com.edulink.backend.dto.request.AvailabilitySlotRequest;
import com.edulink.backend.dto.request.AvailabilitySlotUpdateRequest;
import com.edulink.backend.dto.response.ApiResponse;
import com.edulink.backend.dto.response.AvailabilitySlotResponse;
import com.edulink.backend.dto.response.GeneratedTimeSlotResponse;
import com.edulink.backend.model.entity.LecturerAvailability;
import com.edulink.backend.service.AvailabilityService;
import com.edulink.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
@Slf4j
public class AvailabilityController {

    private final AvailabilityService availabilityService;
    private final UserService userService;

    // =================== CREATE AVAILABILITY SLOT ===================
    @PostMapping("/slots")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<ApiResponse<AvailabilitySlotResponse>> createAvailabilitySlot(
            @Valid @RequestBody AvailabilitySlotRequest request) {
        
        try {
            String lecturerId = userService.getCurrentUser().getId();
            AvailabilitySlotResponse response = availabilityService.createAvailabilitySlot(request, lecturerId);
            
            return new ResponseEntity<>(
                ApiResponse.<AvailabilitySlotResponse>builder()
                    .success(true)
                    .message("Availability slot created successfully")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build(),
                HttpStatus.CREATED
            );
        } catch (Exception e) {
            log.error("Error creating availability slot", e);
            return ResponseEntity.badRequest().body(
                ApiResponse.<AvailabilitySlotResponse>builder()
                    .success(false)
                    .message("Failed to create availability slot: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        }
    }

    // =================== GET MY AVAILABILITY SLOTS ===================
    @GetMapping("/slots")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<ApiResponse<List<AvailabilitySlotResponse>>> getMyAvailabilitySlots(
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly,
            @RequestParam(required = false) String type) {
        
        try {
            String lecturerId = userService.getCurrentUser().getId();
            
            LecturerAvailability.AvailabilityType typeFilter = null;
            if (type != null && !type.isEmpty()) {
                try {
                    typeFilter = LecturerAvailability.AvailabilityType.valueOf(type.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(
                        ApiResponse.<List<AvailabilitySlotResponse>>builder()
                            .success(false)
                            .message("Invalid availability type: " + type)
                            .timestamp(LocalDateTime.now())
                            .build()
                    );
                }
            }
            
            List<AvailabilitySlotResponse> slots = availabilityService.getLecturerAvailabilitySlots(
                lecturerId, activeOnly, typeFilter);

            return ResponseEntity.ok(
                ApiResponse.<List<AvailabilitySlotResponse>>builder()
                    .success(true)
                    .message("Availability slots retrieved successfully")
                    .data(slots)
                    .timestamp(LocalDateTime.now())
                    .build()
            );
            
        } catch (Exception e) {
            log.error("Error retrieving availability slots", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.<List<AvailabilitySlotResponse>>builder()
                    .success(false)
                    .message("Failed to retrieve availability slots: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        }
    }

    // =================== GET GENERATED TIME SLOTS FOR DATE ===================
    @GetMapping("/slots/generated")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<ApiResponse<List<GeneratedTimeSlotResponse>>> getGeneratedTimeSlots(
            @RequestParam String date) { // Format: YYYY-MM-DD
        
        try {
            String lecturerId = userService.getCurrentUser().getId();
            LocalDate requestedDate = LocalDate.parse(date);
            
            List<GeneratedTimeSlotResponse> timeSlots = availabilityService.getGeneratedTimeSlotsForDate(
                lecturerId, requestedDate);

            return ResponseEntity.ok(
                ApiResponse.<List<GeneratedTimeSlotResponse>>builder()
                    .success(true)
                    .message("Generated time slots retrieved successfully")
                    .data(timeSlots)
                    .timestamp(LocalDateTime.now())
                    .build()
            );
            
        } catch (Exception e) {
            log.error("Error retrieving generated time slots", e);
            return ResponseEntity.badRequest().body(
                ApiResponse.<List<GeneratedTimeSlotResponse>>builder()
                    .success(false)
                    .message("Failed to retrieve generated time slots: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        }
    }

    // =================== UPDATE AVAILABILITY SLOT ===================
    @PutMapping("/slots/{slotId}")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<ApiResponse<AvailabilitySlotResponse>> updateAvailabilitySlot(
            @PathVariable String slotId,
            @Valid @RequestBody AvailabilitySlotUpdateRequest request) {
        
        try {
            String lecturerId = userService.getCurrentUser().getId();
            AvailabilitySlotResponse response = availabilityService.updateAvailabilitySlot(slotId, request, lecturerId);

            return ResponseEntity.ok(
                ApiResponse.<AvailabilitySlotResponse>builder()
                    .success(true)
                    .message("Availability slot updated successfully")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        } catch (Exception e) {
            log.error("Error updating availability slot {}", slotId, e);
            return ResponseEntity.badRequest().body(
                ApiResponse.<AvailabilitySlotResponse>builder()
                    .success(false)
                    .message("Failed to update availability slot: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        }
    }

    // =================== TOGGLE AVAILABILITY SLOT STATUS ===================
    @PutMapping("/slots/{slotId}/toggle")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<ApiResponse<AvailabilitySlotResponse>> toggleAvailabilitySlot(@PathVariable String slotId) {
        try {
            String lecturerId = userService.getCurrentUser().getId();
            AvailabilitySlotResponse response = availabilityService.toggleAvailabilitySlot(slotId, lecturerId);

            return ResponseEntity.ok(
                ApiResponse.<AvailabilitySlotResponse>builder()
                    .success(true)
                    .message("Availability slot status updated successfully")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        } catch (Exception e) {
            log.error("Error toggling availability slot {}", slotId, e);
            return ResponseEntity.badRequest().body(
                ApiResponse.<AvailabilitySlotResponse>builder()
                    .success(false)
                    .message("Failed to toggle availability slot: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        }
    }

    // =================== DELETE AVAILABILITY SLOT ===================
    @DeleteMapping("/slots/{slotId}")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<ApiResponse<Void>> deleteAvailabilitySlot(@PathVariable String slotId) {
        try {
            String lecturerId = userService.getCurrentUser().getId();
            availabilityService.deleteAvailabilitySlot(slotId, lecturerId);

            return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                    .success(true)
                    .message("Availability slot deleted successfully")
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        } catch (Exception e) {
            log.error("Error deleting availability slot {}", slotId, e);
            return ResponseEntity.badRequest().body(
                ApiResponse.<Void>builder()
                    .success(false)
                    .message("Failed to delete availability slot: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        }
    }

    // =================== GET AVAILABILITY STATS ===================
    @GetMapping("/stats")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<ApiResponse<Object>> getAvailabilityStats() {
        try {
            String lecturerId = userService.getCurrentUser().getId();
            Object stats = availabilityService.getAvailabilityStats(lecturerId);

            return ResponseEntity.ok(
                ApiResponse.builder()
                    .success(true)
                    .message("Availability statistics retrieved successfully")
                    .data(stats)
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        } catch (Exception e) {
            log.error("Error retrieving availability statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.builder()
                    .success(false)
                    .message("Failed to retrieve availability statistics: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        }
    }

    // =================== BULK OPERATIONS ===================
    
    @PostMapping("/slots/bulk-create")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<ApiResponse<List<AvailabilitySlotResponse>>> createBulkAvailabilitySlots(
            @RequestBody List<@Valid AvailabilitySlotRequest> requests) {
        
        try {
            String lecturerId = userService.getCurrentUser().getId();
            List<AvailabilitySlotResponse> responses = availabilityService.createBulkAvailabilitySlots(requests, lecturerId);
            
            return ResponseEntity.ok(
                ApiResponse.<List<AvailabilitySlotResponse>>builder()
                    .success(true)
                    .message(responses.size() + " availability slots created successfully")
                    .data(responses)
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        } catch (Exception e) {
            log.error("Error creating bulk availability slots", e);
            return ResponseEntity.badRequest().body(
                ApiResponse.<List<AvailabilitySlotResponse>>builder()
                    .success(false)
                    .message("Failed to create bulk availability slots: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        }
    }

    @DeleteMapping("/slots/bulk-delete")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<ApiResponse<Void>> deleteBulkAvailabilitySlots(@RequestBody List<String> slotIds) {
        try {
            String lecturerId = userService.getCurrentUser().getId();
            availabilityService.deleteBulkAvailabilitySlots(slotIds, lecturerId);

            return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                    .success(true)
                    .message(slotIds.size() + " availability slots deleted successfully")
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        } catch (Exception e) {
            log.error("Error deleting bulk availability slots", e);
            return ResponseEntity.badRequest().body(
                ApiResponse.<Void>builder()
                    .success(false)
                    .message("Failed to delete bulk availability slots: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        }
    }
}