package com.foodsense.ai.controller;

import com.foodsense.ai.dto.ComplaintRequestDto;
import com.foodsense.ai.dto.ComplaintResponseDto;
import com.foodsense.ai.service.ComplaintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing customer complaints.
 *
 * <p>Provides endpoints for submitting new complaints, retrieving a complaint
 * by ID, and listing all complaints.</p>
 *
 * @author FoodSense AI Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
@Tag(name = "Complaints", description = "Endpoints for managing customer complaints")
public class ComplaintController {

    private final ComplaintService complaintService;

    /**
     * Submits a new customer complaint.
     *
     * <p>The complaint is persisted and an asynchronous AI analysis is triggered
     * via Kafka.</p>
     *
     * @param dto the validated complaint request DTO
     * @return the created complaint with HTTP 201 status
     */
    @Operation(summary = "Submit a new complaint",
            description = "Creates a new customer complaint and triggers asynchronous AI analysis via Kafka")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Complaint successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation failure")
    })
    @PostMapping
    public ResponseEntity<ComplaintResponseDto> submitComplaint(
            @Valid @RequestBody final ComplaintRequestDto dto) {
        final ComplaintResponseDto response = complaintService.submitComplaint(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves all complaints ordered by creation date (most recent first).
     *
     * @return a list of complaint response DTOs with HTTP 200 status
     */
    @Operation(summary = "Get all complaints",
            description = "Retrieves all customer complaints ordered by creation date descending")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all complaints")
    })
    @GetMapping
    public ResponseEntity<List<ComplaintResponseDto>> getAllComplaints() {
        final List<ComplaintResponseDto> complaints = complaintService.getAllComplaints();
        return ResponseEntity.ok(complaints);
    }

    /**
     * Retrieves a single complaint by its unique identifier.
     *
     * @param id the UUID of the complaint
     * @return the complaint response DTO with HTTP 200 status
     */
    @Operation(summary = "Get complaint by ID",
            description = "Retrieves a specific customer complaint by its UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the complaint"),
            @ApiResponse(responseCode = "404", description = "Complaint not found with the given ID")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ComplaintResponseDto> getComplaintById(@PathVariable final UUID id) {
        final ComplaintResponseDto complaint = complaintService.getComplaintById(id);
        return ResponseEntity.ok(complaint);
    }
}
