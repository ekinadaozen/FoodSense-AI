package com.foodsense.ai.controller;

import com.foodsense.ai.dto.AnalysisResponseDto;
import com.foodsense.ai.service.AnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for retrieving complaint analysis results.
 *
 * <p>Provides endpoints for listing all analyses and retrieving
 * a specific analysis by the associated complaint ID.</p>
 *
 * @author FoodSense AI Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/analyses")
@RequiredArgsConstructor
@Tag(name = "Analyses", description = "Endpoints for retrieving AI-generated complaint analyses")
public class AnalysisController {

    private final AnalysisService analysisService;

    /**
     * Retrieves all complaint analyses ordered by analysis date (most recent first).
     *
     * @return a list of analysis response DTOs with HTTP 200 status
     */
    @Operation(summary = "Get all analyses",
            description = "Retrieves all AI-generated complaint analyses ordered by analysis date descending")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all analyses")
    })
    @GetMapping
    public ResponseEntity<List<AnalysisResponseDto>> getAllAnalyses() {
        final List<AnalysisResponseDto> analyses = analysisService.getAllAnalyses();
        return ResponseEntity.ok(analyses);
    }

    /**
     * Retrieves the analysis for a specific complaint.
     *
     * @param complaintId the UUID of the complaint
     * @return the analysis response DTO with HTTP 200 status
     */
    @Operation(summary = "Get analysis by complaint ID",
            description = "Retrieves the AI-generated analysis for a specific complaint")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the analysis"),
            @ApiResponse(responseCode = "404", description = "Analysis not found for the given complaint ID")
    })
    @GetMapping("/complaint/{complaintId}")
    public ResponseEntity<AnalysisResponseDto> getAnalysisByComplaintId(
            @PathVariable final UUID complaintId) {
        final AnalysisResponseDto analysis = analysisService.getAnalysisByComplaintId(complaintId);
        return ResponseEntity.ok(analysis);
    }
}
