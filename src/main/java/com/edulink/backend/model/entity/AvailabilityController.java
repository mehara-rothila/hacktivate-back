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
                    .timestamp(LocalDateTime.now().toString())
                    .build(),
                HttpStatus.CREATED
            );
        }